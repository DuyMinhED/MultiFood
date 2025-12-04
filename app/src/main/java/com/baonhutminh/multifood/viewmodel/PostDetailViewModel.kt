package com.baonhutminh.multifood.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.local.PostImageDao
import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.data.model.CommentLikeEntity
import com.baonhutminh.multifood.data.model.UserProfile
import com.baonhutminh.multifood.data.model.relations.CommentWithAuthor
import com.baonhutminh.multifood.data.model.relations.PostWithAuthor
import com.baonhutminh.multifood.data.repository.CommentRepository
import com.baonhutminh.multifood.data.repository.PostRepository
import com.baonhutminh.multifood.data.repository.ProfileRepository
import com.baonhutminh.multifood.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

private const val TAG = "PostDetailViewModel"
private const val LIKE_DEBOUNCE_MS = 500L

sealed class PostDetailEvent {
    object NavigateBack : PostDetailEvent()
    object NavigateToHome : PostDetailEvent()
}

data class PostDetailUiState(
    val postWithAuthor: PostWithAuthor? = null,
    val comments: List<CommentWithAuthor> = emptyList(),
    val likedComments: List<CommentLikeEntity> = emptyList(),
    val currentUser: UserProfile? = null,
    val isLiked: Boolean = false,
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val commentInput: String = "",
    val isAddingComment: Boolean = false,
    val images: List<String> = emptyList(),
    val replyingToComment: CommentWithAuthor? = null,
    val replyInput: String = ""
)

private data class PostDetailIntermediateState(
    val postWithAuthor: PostWithAuthor? = null,
    val comments: List<CommentWithAuthor> = emptyList(),
    val likedComments: List<CommentLikeEntity> = emptyList(),
    val currentUser: UserProfile? = null,
    val isLiked: Boolean = false,
    val isLoading: Boolean = true,
    val images: List<String> = emptyList()
)

private data class ViewModelState(
    val commentInput: String = "",
    val isAddingComment: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val replyingToComment: CommentWithAuthor? = null,
    val replyInput: String = ""
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
    
    private var likeDebounceJob: Job? = null

    private val likedPostsFlow = profileRepository.getLikedPostsForCurrentUser()
    private val likedCommentsFlow = commentRepository.getLikedCommentsForCurrentUser()

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
            likedComments = emptyList(), // Will be filled in next combine
            currentUser = (userRes as? Resource.Success)?.data,
            isLiked = isLiked,
            isLoading = (postRes as? Resource.Success)?.data == null,
            images = images.map { it.url }
        )
    }.combine(likedCommentsFlow) { state, likedComments ->
        state.copy(likedComments = likedComments)
    }.combine(viewModelState) { intermediateState, vmState ->
        PostDetailUiState(
            postWithAuthor = intermediateState.postWithAuthor,
            comments = intermediateState.comments,
            likedComments = intermediateState.likedComments,
            currentUser = intermediateState.currentUser,
            isLiked = intermediateState.isLiked,
            isLoading = intermediateState.isLoading,
            images = intermediateState.images,
            isDeleting = vmState.isDeleting,
            commentInput = vmState.commentInput,
            isAddingComment = vmState.isAddingComment,
            errorMessage = vmState.errorMessage,
            replyingToComment = vmState.replyingToComment,
            replyInput = vmState.replyInput
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PostDetailUiState()
    )

    init {
        // Realtime sync cho post này
        viewModelScope.launch {
            postRepository.observePostRealtime(postId)
                .catch { e -> Log.e(TAG, "Realtime sync error", e) }
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
        // Cancel job cũ nếu đang pending
        likeDebounceJob?.cancel()
        
        likeDebounceJob = viewModelScope.launch {
            // Debounce - đợi user ngừng spam
            delay(LIKE_DEBOUNCE_MS)
            
            val currentLiked = uiState.value.isLiked
            val result = profileRepository.toggleLike(postId, currentLiked)
            if (result is Resource.Error) {
                viewModelState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun clearErrorMessage() {
        viewModelState.update { it.copy(errorMessage = null) }
    }

    // Comment Like
    fun toggleCommentLike(commentId: String) {
        viewModelScope.launch {
            val isLiked = uiState.value.likedComments.any { it.commentId == commentId }
            val result = commentRepository.toggleCommentLike(commentId, isLiked)
            if (result is Resource.Error) {
                viewModelState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    // Reply functions
    fun startReply(comment: CommentWithAuthor) {
        viewModelState.update { it.copy(replyingToComment = comment, replyInput = "") }
    }

    fun cancelReply() {
        viewModelState.update { it.copy(replyingToComment = null, replyInput = "") }
    }

    fun onReplyInputChange(text: String) {
        viewModelState.update { it.copy(replyInput = text) }
    }

    fun submitReply() {
        val replyingTo = viewModelState.value.replyingToComment ?: return
        val replyText = viewModelState.value.replyInput
        if (replyText.isBlank()) return

        viewModelScope.launch {
            viewModelState.update { it.copy(isAddingComment = true) }
            val user = profileRepository.getUserProfile().first().data
            if (user == null) {
                viewModelState.update { it.copy(isAddingComment = false, errorMessage = "Vui lòng đăng nhập") }
                return@launch
            }

            val result = commentRepository.replyToComment(replyingTo.comment, replyText, user.id)

            if (result is Resource.Success) {
                viewModelState.update { it.copy(replyingToComment = null, replyInput = "") }
                commentRepository.refreshCommentsForPost(postId)
            } else if (result is Resource.Error) {
                viewModelState.update { it.copy(errorMessage = result.message) }
            }
            viewModelState.update { it.copy(isAddingComment = false) }
        }
    }
}
