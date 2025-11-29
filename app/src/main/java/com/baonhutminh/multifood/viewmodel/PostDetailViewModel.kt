package com.baonhutminh.multifood.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.data.repository.PostRepository
import com.baonhutminh.multifood.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _post = mutableStateOf<Post?>(null)
    val post: State<Post?> = _post

    private val _comments = mutableStateOf<List<Comment>>(emptyList())
    val comments: State<List<Comment>> = _comments

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val postId: String = checkNotNull(savedStateHandle["postId"])

    init {
        observePostDetails()
        observeComments()
        refreshData(isInitialLoad = true)
    }

    private fun observePostDetails() {
        viewModelScope.launch {
            postRepository.getPostById(postId).collect { resource ->
                if (resource is Resource.Success) {
                    _post.value = resource.data
                }
            }
        }
    }

    private fun observeComments() {
        viewModelScope.launch {
            postRepository.getCommentsForPost(postId).collect { resource ->
                if (resource is Resource.Success) {
                    _comments.value = resource.data ?: emptyList()
                }
            }
        }
    }

    fun refreshData(isInitialLoad: Boolean = false) {
        viewModelScope.launch {
            if (isInitialLoad) _isLoading.value = true
            postRepository.refreshAllPosts() // Cũng có thể tạo hàm chỉ refresh 1 post
            postRepository.refreshCommentsForPost(postId)
            if (isInitialLoad) _isLoading.value = false
        }
    }

    fun addComment(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            val newComment = Comment(reviewId = postId, content = content)
            val result = postRepository.addComment(newComment)
            if (result is Resource.Error) {
                _errorMessage.value = result.message ?: "Thêm bình luận thất bại"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}