package com.baonhutminh.multifood.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.data.model.UserProfile
import com.baonhutminh.multifood.data.model.relations.CommentWithAuthor
import com.baonhutminh.multifood.data.model.relations.PostWithAuthor
import com.baonhutminh.multifood.data.repository.CommentRepository
import com.baonhutminh.multifood.data.repository.PostRepository
import com.baonhutminh.multifood.data.repository.ProfileRepository
import com.baonhutminh.multifood.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

sealed class PostDetailEvent {
    object NavigateBack : PostDetailEvent()
}

data class PostDetailUiState(
    val postWithAuthor: PostWithAuthor? = null,
    val comments: List<CommentWithAuthor> = emptyList(),
    val currentUser: UserProfile? = null,
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val commentInput: String = "",
    val isAddingComment: Boolean = false
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: String = savedStateHandle.get<String>("postId")!!
    private val _viewModel_state = MutableStateFlow(ViewModelState())
    private val _events = MutableSharedFlow<PostDetailEvent>()
    val events = _events.asSharedFlow()

    val uiState: StateFlow<PostDetailUiState> = combine(
        postRepository.getPostById(postId),
        commentRepository.getCommentsForPost(postId),
        profileRepository.getUserProfile(),
        _viewModel_state
    ) { postRes, commentsRes, userRes, vmState ->
        PostDetailUiState(
            postWithAuthor = (postRes as? Resource.Success)?.data,
            comments = (commentsRes as? Resource.Success)?.data ?: emptyList(),
            currentUser = (userRes as? Resource.Success)?.data,
            isLoading = (postRes as? Resource.Success)?.data == null,
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
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            postRepository.refreshAllPosts()
            commentRepository.refreshCommentsForPost(postId)
        }
    }

    fun onCommentInputChange(text: String) {
        _viewModel_state.update { it.copy(commentInput = text) }
    }

    fun addComment() {
        if (_viewModel_state.value.isAddingComment || _viewModel_state.value.commentInput.isBlank()) return

        viewModelScope.launch {
            _viewModel_state.update { it.copy(isAddingComment = true, errorMessage = null) }
            val user = profileRepository.getUserProfile().first().data
            if (user == null) {
                _viewModel_state.update { it.copy(isAddingComment = false, errorMessage = "Vui lòng đăng nhập để bình luận") }
                return@launch
            }

            val commentText = _viewModel_state.value.commentInput
            _viewModel_state.update { it.copy(commentInput = "") }

            val newComment = Comment(
                reviewId = postId,
                content = commentText,
                createdAt = Date(),
                updatedAt = Date()
            )

            val result = commentRepository.createComment(newComment, user.id)

            if (result is Resource.Success) {
                val refreshResult = commentRepository.refreshCommentsForPost(postId)
                if (refreshResult is Resource.Error) {
                    _viewModel_state.update { it.copy(errorMessage = "Đã gửi bình luận, nhưng không thể làm mới danh sách.") }
                }
            } else if (result is Resource.Error) {
                _viewModel_state.update {
                    it.copy(commentInput = commentText, errorMessage = result.message)
                }
            }
            _viewModel_state.update { it.copy(isAddingComment = false) }
        }
    }

    fun deletePost() {
        viewModelScope.launch {
            val postToDelete = uiState.value.postWithAuthor?.post ?: return@launch
            if (postToDelete.userId != uiState.value.currentUser?.id) {
                _viewModel_state.update { it.copy(errorMessage = "Bạn không có quyền xóa bài viết này") }
                return@launch
            }

            _viewModel_state.update { it.copy(isDeleting = true, errorMessage = null) }

            val result = postRepository.deletePost(postToDelete.id, postToDelete.userId)

            if (result is Resource.Success) {
                postRepository.refreshAllPosts()
                profileRepository.refreshUserProfile()
                _events.emit(PostDetailEvent.NavigateBack)
            } else {
                _viewModel_state.update { it.copy(errorMessage = result.message ?: "Lỗi xóa bài viết") }
            }
            _viewModel_state.update { it.copy(isDeleting = false) }
        }
    }

    fun clearErrorMessage() {
        _viewModel_state.update { it.copy(errorMessage = null) }
    }
}
