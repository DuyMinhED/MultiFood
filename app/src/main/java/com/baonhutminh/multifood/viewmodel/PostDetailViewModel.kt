package com.baonhutminh.multifood.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.data.model.PostEntity
import com.baonhutminh.multifood.data.model.UserProfile
import com.baonhutminh.multifood.data.repository.PostRepository
import com.baonhutminh.multifood.data.repository.ProfileRepository
import com.baonhutminh.multifood.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class PostDetailUiState(
    val post: PostEntity? = null,
    val comments: List<Comment> = emptyList(),
    val currentUser: UserProfile? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val commentInput: String = "",
    val isAddingComment: Boolean = false
)

private data class ViewModelState(
    val commentInput: String = "",
    val isAddingComment: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val profileRepository: ProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: String = savedStateHandle.get<String>("postId")!!
    private val _viewModel_state = MutableStateFlow(ViewModelState())

    val uiState: StateFlow<PostDetailUiState> = combine(
        postRepository.getPostById(postId),
        postRepository.getCommentsForPost(postId),
        profileRepository.getUserProfile(),
        _viewModel_state
    ) { postRes, commentsRes, userRes, vmState ->

        val post = (postRes as? Resource.Success)?.data
        val comments = (commentsRes as? Resource.Success)?.data ?: emptyList()
        val user = (userRes as? Resource.Success)?.data

        PostDetailUiState(
            post = post,
            comments = comments,
            currentUser = user,
            isLoading = post == null,
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
            postRepository.refreshCommentsForPost(postId)
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
                createdAt = Date(), // Sửa ở đây
                updatedAt = Date(), // Sửa ở đây
                userName = user.name,
                userAvatarUrl = user.avatarUrl ?: ""
            )

            when (val result = postRepository.addComment(newComment)) {
                is Resource.Success -> {
                    postRepository.refreshCommentsForPost(postId)
                }
                is Resource.Error -> {
                    _viewModel_state.update {
                        it.copy(
                            commentInput = commentText,
                            errorMessage = result.message ?: "Lỗi không xác định"
                        )
                    }
                }
                else -> {}
            }
            _viewModel_state.update { it.copy(isAddingComment = false) }
        }
    }

    fun clearErrorMessage() {
        _viewModel_state.update { it.copy(errorMessage = null) }
    }
}
