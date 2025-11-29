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
import kotlinx.coroutines.flow.collect
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
        observeUserProfile()
        refreshProfile(isInitialLoad = true)
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            profileRepository.getUserProfile().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _userProfile.value = resource.data
                    }
                    is Resource.Error -> {
                        if (_userProfile.value == null) { // Chỉ hiển thị lỗi nếu chưa có dữ liệu gì
                            _errorMessage.value = resource.message ?: "Không thể tải thông tin người dùng"
                        }
                    }
                    is Resource.Loading -> {
                        // Dữ liệu đang được tải từ Room, không cần làm gì ở đây vì UI đã hiển thị dữ liệu cũ.
                    }
                }
            }
        }
    }

    fun refreshProfile(isInitialLoad: Boolean = false) {
        viewModelScope.launch {
            if (isInitialLoad) {
                _isLoading.value = true
            }
            val result = profileRepository.refreshUserProfile()
            if (result is Resource.Error) {
                _errorMessage.value = result.message ?: "Không thể làm mới dữ liệu"
            }
            if (isInitialLoad) {
                _isLoading.value = false
            }
        }
    }

    fun updateName(newName: String) { // Đổi tên hàm
        if (newName.isBlank()) {
            _errorMessage.value = "Tên không được để trống"
            return
        }

        viewModelScope.launch {
            _isUpdating.value = true
            _errorMessage.value = null

            when (val result = profileRepository.updateName(newName)) { // Gọi hàm mới
                is Resource.Success -> {
                    _successMessage.value = "Cập nhật tên thành công"
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message ?: "Không thể cập nhật tên"
                }
                else -> { /* Trạng thái Loading không cần xử lý ở đây */ }
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
                    _successMessage.value = "Cập nhật giới thiệu thành công"
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message ?: "Không thể cập nhật giới thiệu"
                }
                 else -> { /* Trạng thái Loading không cần xử lý ở đây */ }
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
                    _successMessage.value = "Cập nhật ảnh đại diện thành công"
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message ?: "Không thể tải lên ảnh đại diện"
                }
                 else -> { /* Trạng thái Loading không cần xử lý ở đây */ }
            }
            _isUpdating.value = false
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}