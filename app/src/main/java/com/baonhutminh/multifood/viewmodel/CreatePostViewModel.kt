package com.baonhutminh.multifood.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
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
    private val profileRepository: ProfileRepository
) : ViewModel() {

    // Trạng thái cho các trường nhập liệu
    val title = mutableStateOf("")
    val content = mutableStateOf("")
    val rating = mutableStateOf(0f)
    val pricePerPerson = mutableStateOf("")
    val placeName = mutableStateOf("")
    val placeAddress = mutableStateOf("")
    val imageUris = mutableStateOf<List<Uri>>(emptyList())

    private val _uiState = mutableStateOf<CreatePostUiState>(CreatePostUiState.Idle)
    val uiState = _uiState

    private val _events = MutableSharedFlow<CreatePostEvent>()
    val events = _events.asSharedFlow()

    fun onImageSelected(uris: List<Uri>) {
        imageUris.value = uris
    }

    fun submitPost() {
        viewModelScope.launch {
            _uiState.value = CreatePostUiState.Loading

            val userProfileResource = profileRepository.getUserProfile().first()
            if (userProfileResource !is Resource.Success || userProfileResource.data == null) {
                _uiState.value = CreatePostUiState.Error("Không thể lấy thông tin người dùng.")
                return@launch
            }
            val user = userProfileResource.data

            val imageUrls = mutableListOf<String>()
            for (uri in imageUris.value) {
                when (val result = postRepository.uploadPostImage(uri)) {
                    is Resource.Success -> imageUrls.add(result.data!!)
                    is Resource.Error -> {
                        _uiState.value = CreatePostUiState.Error("Lỗi tải ảnh lên: ${result.message}")
                        return@launch
                    }
                    else -> {}
                }
            }

            val post = Post(
                title = title.value,
                content = content.value,
                rating = rating.value,
                pricePerPerson = pricePerPerson.value.toIntOrNull() ?: 0,
                placeName = placeName.value,
                placeAddress = placeAddress.value,
                imageUrls = imageUrls,
                userName = user.name,
                userAvatarUrl = user.avatarUrl ?: "",
                createdAt = Date(),
                updatedAt = Date()
            )

            when (val result = postRepository.createPost(post)) {
                is Resource.Success -> {
                    // Làm mới cả hai nguồn dữ liệu
                    profileRepository.refreshUserProfile()
                    postRepository.refreshAllPosts()
                    
                    _uiState.value = CreatePostUiState.Success
                    _events.emit(CreatePostEvent.NavigateBack)
                }
                is Resource.Error -> {
                    _uiState.value = CreatePostUiState.Error(result.message ?: "Lỗi không xác định")
                }
                else -> {}
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
