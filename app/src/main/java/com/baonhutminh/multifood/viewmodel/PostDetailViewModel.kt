package com.baonhutminh.multifood.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.local.PostImageDao
import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.data.model.UserProfile
import com.baonhutminh.multifood.data.model.relations.CommentWithAuthor
import com.baonhutminh.multifood.data.model.relations.PostWithAuthor
import com.baonhutminh.multifood.data.repository.CommentRepository
import com.baonhutminh.multifood.data.repository.PostRepository
import com.baonhutminh.multifood.data.repository.ProfileRepository
import com.baonhutminh.multifood.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

sealed class PostDetailEvent {
    object NavigateBack : PostDetailEvent()
    object NavigateToHome : PostDetailEvent()
}

data class PostDetailUiState(
    val postWithAuthor: PostWithAuthor? = null,
    val comments: List<CommentWithAuthor> = emptyList(),
    val currentUser: UserProfile? = null,
    val isLiked: Boolean = false,
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val commentInput: String = "",
    val isAddingComment: Boolean = false,
    val images: List<String> = emptyList()
)

private data class PostDetailIntermediateState(
    val postWithAuthor: PostWithAuthor? = null,
    val comments: List<CommentWithAuthor> = emptyList(),
    val currentUser: UserProfile? = null,
    val isLiked: Boolean = false,
    val isLoading: Boolean = true,
    val images: List<String> = emptyList()
)

private data class ViewModelState(
    val commentInput: String = "",
    val isAddingComment: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val profileRepository: ProfileRepository,
    postImageDao: PostImageDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: String = savedStateHandle.get<String>("postId")!!
    private val viewModelState = MutableStateFlow(ViewModelState())
    private val _events = MutableSharedFlow<PostDetailEvent>()
    val events = _events.asSharedFlow()

    private val likedPostsFlow = profileRepository.getLikedPostsForCurrentUser()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PostDetailUiState> = combine(
        postRepository.getPostById(postId),
        commentRepository.getCommentsForPost(postId),
        profileRepository.getUserProfile(),
        postImageDao.getImagesForPost(postId).mapLatest { it ?: emptyList() },
        likedPostsFlow
    ) { postRes, commentsRes, userRes, images, likedPosts ->
        val isLiked = likedPosts.any { it.postId == postId }
        PostDetailIntermediateState(
            postWithAuthor = (postRes as? Resource.Success)?.data,
            comments = (commentsRes as? Resource.Success)?.data ?: emptyList(),
            currentUser = (userRes as? Resource.Success)?.data,
            isLiked = isLiked,
            isLoading = (postRes as? Resource.Success)?.data == null,
            images = images.map { it.url }
        )
    }.combine(viewModelState) { intermediateState, vmState ->
        PostDetailUiState(
            postWithAuthor = intermediateState.postWithAuthor,
            comments = intermediateState.comments,
            currentUser = intermediateState.currentUser,
            isLiked = intermediateState.isLiked,
            isLoading = intermediateState.isLoading,
            images = intermediateState.images,
            isDeleting = vmState.isDeleting,
            commentInput = vmState.commentInput,
            isAddingComment = vmState.isAddingComment,
            errorMessage = vmState.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PostDetailUiState()
    )

    init {
        // Start realtime sync cho post này
        viewModelScope.launch {
            postRepository.observePostRealtime(postId)
                .catch { /* ignore errors */ }
                .collect()
        }
        
        // Load comments
        viewModelScope.launch {
            commentRepository.refreshCommentsForPost(postId)
        }
    }

    fun onCommentInputChange(text: String) {
        viewModelState.update { it.copy(commentInput = text) }
    }

    fun addComment() {
        if (viewModelState.value.isAddingComment || viewModelState.value.commentInput.isBlank()) return

        viewModelScope.launch {
            viewModelState.update { it.copy(isAddingComment = true, errorMessage = null) }
            val user = profileRepository.getUserProfile().first().data
            if (user == null) {
                viewModelState.update { it.copy(isAddingComment = false, errorMessage = "Vui lòng đăng nhập để bình luận") }
                return@launch
            }

            val commentText = viewModelState.value.commentInput
            viewModelState.update { it.copy(commentInput = "") }

            val newComment = Comment(
                postId = postId,
                content = commentText,
                createdAt = Date(),
                updatedAt = Date()
            )

            val result = commentRepository.createComment(newComment, user.id)

            if (result is Resource.Success) {
                commentRepository.refreshCommentsForPost(postId)
            } else if (result is Resource.Error) {
                viewModelState.update {
                    it.copy(commentInput = commentText, errorMessage = result.message)
                }
            }
            viewModelState.update { it.copy(isAddingComment = false) }
        }
    }

    fun deletePost() {
        viewModelScope.launch {
            val postToDelete = uiState.value.postWithAuthor?.post ?: return@launch
            if (postToDelete.userId != uiState.value.currentUser?.id) {
                viewModelState.update { it.copy(errorMessage = "Bạn không có quyền xóa bài viết này") }
                return@launch
            }

            viewModelState.update { it.copy(isDeleting = true, errorMessage = null) }

            val result = postRepository.deletePost(postToDelete.id)

            if (result is Resource.Success) {
                _events.emit(PostDetailEvent.NavigateToHome)
            } else {
                viewModelState.update { it.copy(errorMessage = result.message ?: "Lỗi xóa bài viết") }
            }
            viewModelState.update { it.copy(isDeleting = false) }
        }
    }

    fun toggleLike() {
        viewModelScope.launch {
            val currentLiked = uiState.value.isLiked
            val result = profileRepository.toggleLike(postId, currentLiked)
            if (result is Resource.Error) {
                viewModelState.update { it.copy(errorMessage = result.message) }
            }
            // likedPosts Flow tự động cập nhật từ Room → UI tự update
        }
    }

    fun clearErrorMessage() {
        viewModelState.update { it.copy(errorMessage = null) }
    }
}
