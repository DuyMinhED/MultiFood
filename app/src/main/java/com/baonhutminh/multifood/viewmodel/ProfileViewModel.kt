package com.baonhutminh.multifood.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.UserProfile
import com.baonhutminh.multifood.data.repository.ProfileRepository
import com.baonhutminh.multifood.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
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

            when (val profileResult = profileRepository.getUserProfile()) {
                is Resource.Success -> {
                    val profile = profileResult.data
                    var finalProfile = profile

                    if (profile != null) {
                        val postsCountResult = profileRepository.getUserPostsCount()
                        val favoritesCountResult = profileRepository.getUserFavoritesCount()

                        val postsCount = (postsCountResult as? Resource.Success)?.data ?: profile.totalPosts
                        val favoritesCount = (favoritesCountResult as? Resource.Success)?.data ?: profile.totalFavorites

                        finalProfile = profile.copy(
                            totalPosts = postsCount,
                            totalFavorites = favoritesCount
                        )
                    }
                    _userProfile.value = finalProfile
                }
                is Resource.Error -> {
                    _errorMessage.value = profileResult.message ?: "Không thể tải thông tin người dùng"
                }
                else -> {}
            }
            _isLoading.value = false
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

            when (val result = profileRepository.updateDisplayName(newName)) {
                is Resource.Success -> {
                    _userProfile.value = _userProfile.value?.copy(displayName = newName)
                    _successMessage.value = "Cập nhật tên thành công"
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message ?: "Không thể cập nhật tên"
                }
                else -> {}
            }

            _isUpdating.value = false
        }
    }

    fun updateBio(newBio: String) {
        viewModelScope.launch {
            _isUpdating.value = true
            _errorMessage.value = null

            when (val result = profileRepository.updateBio(newBio)) {
                is Resource.Success -> {
                    _userProfile.value = _userProfile.value?.copy(bio = newBio)
                    _successMessage.value = "Cập nhật giới thiệu thành công"
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message ?: "Không thể cập nhật giới thiệu"
                }
                else -> {}
            }

            _isUpdating.value = false
        }
    }

    fun uploadAvatar(imageUri: Uri) {
        viewModelScope.launch {
            _isUpdating.value = true
            _errorMessage.value = null

            when (val result = profileRepository.uploadAvatar(imageUri)) {
                is Resource.Success -> {
                    result.data?.let { newAvatarUrl ->
                        _userProfile.value = _userProfile.value?.copy(avatarUrl = newAvatarUrl)
                        _successMessage.value = "Cập nhật ảnh đại diện thành công"
                    }
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message ?: "Không thể tải lên ảnh đại diện"
                }
                else -> {}
            }

            _isUpdating.value = false
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}