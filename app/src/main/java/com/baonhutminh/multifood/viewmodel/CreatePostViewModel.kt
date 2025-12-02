package com.baonhutminh.multifood.viewmodel

import android.net.Uri
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.data.repository.PostRepository
import com.baonhutminh.multifood.data.repository.ProfileRepository
import com.baonhutminh.multifood.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val profileRepository: ProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val editingPostId = mutableStateOf(savedStateHandle.get<String>("postId"))
    val isEditing = derivedStateOf { editingPostId.value != null }

    val title = mutableStateOf("")
    val content = mutableStateOf("")
    val rating = mutableStateOf(0f)
    val pricePerPerson = mutableStateOf("")
    val placeName = mutableStateOf("")
    val placeAddress = mutableStateOf("")
    val imageUris = mutableStateOf<List<Uri>>(emptyList())
    private var originalImageUrls = listOf<String>()

    val isFormValid = derivedStateOf {
        placeName.value.isNotBlank() &&
        placeAddress.value.isNotBlank() &&
        title.value.isNotBlank() &&
        content.value.isNotBlank()
    }

    private val _uiState = mutableStateOf<CreatePostUiState>(CreatePostUiState.Idle)
    val uiState = _uiState

    private val _events = MutableSharedFlow<CreatePostEvent>()
    val events = _events.asSharedFlow()

    init {
        editingPostId.value?.let { postId ->
            loadPostForEditing(postId)
        }
    }

    private fun loadPostForEditing(postId: String) {
        viewModelScope.launch {
            postRepository.getPostById(postId).first().data?.let {
                postWithAuthor ->
                val post = postWithAuthor.post
                title.value = post.title
                content.value = post.content
                rating.value = post.rating
                pricePerPerson.value = post.pricePerPerson.toString()
                placeName.value = post.placeName
                placeAddress.value = post.placeAddress
                originalImageUrls = post.imageUrls
            }
        }
    }

    fun onImageSelected(uris: List<Uri>) {
        imageUris.value = uris
    }

    fun submitPost() {
        if (!isFormValid.value) {
            _uiState.value = CreatePostUiState.Error("Vui lòng điền đầy đủ các trường bắt buộc.")
            return
        }

        if (isEditing.value) {
            updateExistingPost()
        } else {
            createNewPost()
        }
    }

    private fun createNewPost() {
        viewModelScope.launch {
            _uiState.value = CreatePostUiState.Loading

            val user = profileRepository.getUserProfile().first().data
            if (user == null) {
                _uiState.value = CreatePostUiState.Error("Không thể lấy thông tin người dùng.")
                return@launch
            }

            val imageUrls = uploadImages()
            if (imageUrls == null) return@launch

            val post = Post(
                title = title.value,
                content = content.value,
                rating = rating.value,
                pricePerPerson = pricePerPerson.value.toIntOrNull() ?: 0,
                placeName = placeName.value,
                placeAddress = placeAddress.value,
                imageUrls = imageUrls,
                createdAt = Date(),
                updatedAt = Date()
            )

            when (postRepository.createPost(post)) {
                is Resource.Success -> {
                    postRepository.refreshAllPosts()
                    _events.emit(CreatePostEvent.NavigateBack)
                }
                is Resource.Error -> _uiState.value = CreatePostUiState.Error("Lỗi không xác định")
                else -> {}
            }
        }
    }

    private fun updateExistingPost() {
        viewModelScope.launch {
            _uiState.value = CreatePostUiState.Loading

            val user = profileRepository.getUserProfile().first().data
            if (user == null) {
                _uiState.value = CreatePostUiState.Error("Không thể lấy thông tin người dùng.")
                return@launch
            }

            val imageUrls = uploadImages()
            if (imageUrls == null) return@launch

            val updatedPost = Post(
                id = editingPostId.value!!,
                userId = user.id,
                title = title.value,
                content = content.value,
                rating = rating.value,
                pricePerPerson = pricePerPerson.value.toIntOrNull() ?: 0,
                placeName = placeName.value,
                placeAddress = placeAddress.value,
                imageUrls = imageUrls + originalImageUrls, // Kết hợp ảnh mới và cũ
                updatedAt = Date()
                // Các trường khác sẽ được giữ nguyên nhờ SetOptions.merge()
            )

            when (postRepository.updatePost(updatedPost)) {
                is Resource.Success -> {
                    postRepository.refreshAllPosts()
                    _events.emit(CreatePostEvent.NavigateBack)
                }
                is Resource.Error -> _uiState.value = CreatePostUiState.Error("Lỗi không xác định")
                else -> {}
            }
        }
    }

    private suspend fun uploadImages(): List<String>? {
        val uploadedUrls = mutableListOf<String>()
        for (uri in imageUris.value) {
            when (val result = postRepository.uploadPostImage(uri)) {
                is Resource.Success -> uploadedUrls.add(result.data!!)
                is Resource.Error -> {
                    _uiState.value = CreatePostUiState.Error("Lỗi tải ảnh lên: ${result.message}")
                    return null
                }
                else -> {}
            }
        }
        return uploadedUrls
    }

    fun deletePost() {
        viewModelScope.launch {
            val postId = editingPostId.value ?: return@launch
            val authorId = profileRepository.getUserProfile().first().data?.id ?: return@launch

            _uiState.value = CreatePostUiState.Loading // Hiển thị loading

            val result = postRepository.deletePost(postId, authorId)
            if (result is Resource.Success) {
                postRepository.refreshAllPosts()
                profileRepository.refreshUserProfile()
                _events.emit(CreatePostEvent.NavigateBack)
            } else {
                _uiState.value = CreatePostUiState.Error(result.message ?: "Lỗi xóa bài viết")
            }
        }
    }
}

sealed class CreatePostUiState {
    object Idle : CreatePostUiState()
    object Loading : CreatePostUiState()
    object Success : CreatePostUiState()
    data class Error(val message: String) : CreatePostUiState()
}

sealed class CreatePostEvent {
    object NavigateBack : CreatePostEvent()
}
