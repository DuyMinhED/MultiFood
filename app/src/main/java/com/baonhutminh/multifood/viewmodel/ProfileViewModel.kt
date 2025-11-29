package com.baonhutminh.multifood.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.UserProfile
import com.baonhutminh.multifood.data.repository.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _userProfile = mutableStateOf<UserProfile?>(null)
    val userProfile: State<UserProfile?> = _userProfile

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isUpdating = mutableStateOf(false)
    val isUpdating: State<Boolean> = _isUpdating

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _successMessage = mutableStateOf<String?>(null)
    val successMessage: State<String?> = _successMessage

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val profile = profileRepository.getUserProfile()
                if (profile != null) {
                    val postsCount = profileRepository.getUserPostsCount()
                    val favoritesCount = profileRepository.getUserFavoritesCount()
                    _userProfile.value = profile.copy(
                        totalPosts = postsCount,
                        totalFavorites = favoritesCount
                    )
                } else {
                    _errorMessage.value = "Không thể tải thông tin người dùng"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateDisplayName(newName: String) {
        if (newName.isBlank()) {
            _errorMessage.value = "Tên không được để trống"
            return
        }

        viewModelScope.launch {
            _isUpdating.value = true
            _errorMessage.value = null

            val success = profileRepository.updateDisplayName(newName)
            if (success) {
                _userProfile.value = _userProfile.value?.copy(displayName = newName)
                _successMessage.value = "Cập nhật tên thành công"
            } else {
                _errorMessage.value = "Không thể cập nhật tên"
            }

            _isUpdating.value = false
        }
    }

    fun updateBio(newBio: String) {
        viewModelScope.launch {
            _isUpdating.value = true
            _errorMessage.value = null

            val success = profileRepository.updateBio(newBio)
            if (success) {
                _userProfile.value = _userProfile.value?.copy(bio = newBio)
                _successMessage.value = "Cập nhật giới thiệu thành công"
            } else {
                _errorMessage.value = "Không thể cập nhật giới thiệu"
            }

            _isUpdating.value = false
        }
    }

    fun uploadAvatar(imageUri: Uri) {
        viewModelScope.launch {
            _isUpdating.value = true
            _errorMessage.value = null

            val downloadUrl = profileRepository.uploadAvatar(imageUri)
            if (downloadUrl != null) {
                _userProfile.value = _userProfile.value?.copy(avatarUrl = downloadUrl)
                _successMessage.value = "Cập nhật ảnh đại diện thành công"
            } else {
                _errorMessage.value = "Không thể tải lên ảnh đại diện"
            }

            _isUpdating.value = false
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
