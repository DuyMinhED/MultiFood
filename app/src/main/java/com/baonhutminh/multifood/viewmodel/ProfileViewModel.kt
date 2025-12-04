package com.baonhutminh.multifood.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.UserProfile
import com.baonhutminh.multifood.data.repository.ProfileRepository
import com.baonhutminh.multifood.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class để giữ toàn bộ trạng thái của giao diện Profile một cách nhất quán
data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false, // Chỉ dùng cho lần tải dữ liệu đầu tiên
    val isUpdating: Boolean = false, // Dùng cho tất cả các hoạt động cập nhật (tên, bio, avatar, sđt)
    val providers: List<String> = emptyList() // Danh sách providers (password, google.com, etc.)
)

// Sealed class cho các sự kiện chỉ xảy ra một lần (side-effects) như Toast, Snackbar
sealed class ProfileUiEvent {
    data class UpdateSuccess(val message: String) : ProfileUiEvent()
    data class ShowError(val message: String) : ProfileUiEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    // StateFlow để giữ trạng thái của UI, tuân thủ nguyên tắc "single source of truth"
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Channel để gửi các sự kiện một lần, đảm bảo mỗi sự kiện chỉ được xử lý một lần
    private val _eventChannel = Channel<ProfileUiEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    init {
        // Bắt đầu lắng nghe thay đổi dữ liệu từ local database (Room)
        observeUserProfile()
        // Kích hoạt làm mới dữ liệu từ server ngay khi ViewModel được tạo
        refreshProfile(isInitialLoad = true)
        // Load providers
        loadProviders()
    }
    
    private fun loadProviders() {
        val providers = profileRepository.getCurrentUserProviders()
        _uiState.update { it.copy(providers = providers) }
    }

    private fun observeUserProfile() {
        profileRepository.getUserProfile().onEach { resource ->
            when (resource) {
                is Resource.Success -> {
                    _uiState.update { it.copy(userProfile = resource.data) }
                }
                is Resource.Error -> {
                    // Nếu chưa có dữ liệu nào trong cache, hiển thị lỗi qua event
                    if (_uiState.value.userProfile == null) {
                        _eventChannel.send(ProfileUiEvent.ShowError(resource.message ?: "Không thể tải thông tin người dùng"))
                    }
                }
                is Resource.Loading -> {
                    // Không cần làm gì khi đang tải từ Room để tránh màn hình nhấp nháy
                }
            }
        }.launchIn(viewModelScope)
    }

    fun refreshProfile(isInitialLoad: Boolean = false) {
        viewModelScope.launch {
            if (isInitialLoad) {
                _uiState.update { it.copy(isLoading = true) }
            }
            // Chỉ gọi làm mới. `observeUserProfile` sẽ tự động cập nhật UI khi dữ liệu thay đổi
            val result = profileRepository.refreshUserProfile()
            if (result is Resource.Error) {
                _eventChannel.send(ProfileUiEvent.ShowError(result.message ?: "Không thể làm mới dữ liệu"))
            }
            if (isInitialLoad) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateName(newName: String) {
        if (newName.isBlank()) {
            viewModelScope.launch { _eventChannel.send(ProfileUiEvent.ShowError("Tên không được để trống")) }
            return
        }
        viewModelScope.launch {
            handleUpdate(
                successMessage = "Cập nhật tên thành công",
                errorMessage = "Không thể cập nhật tên",
                updateAction = { profileRepository.updateName(newName) }
            )
        }
    }

    fun updateBio(newBio: String) {
        viewModelScope.launch {
            handleUpdate(
                successMessage = "Cập nhật giới thiệu thành công",
                errorMessage = "Không thể cập nhật giới thiệu",
                updateAction = { profileRepository.updateBio(newBio) }
            )
        }
    }

    fun updatePhoneNumber(newPhoneNumber: String) {
        // Bạn có thể thêm các quy tắc kiểm tra số điện thoại ở đây, ví dụ:
        if (newPhoneNumber.length < 10 || !newPhoneNumber.all { it.isDigit() }) {
            viewModelScope.launch { _eventChannel.send(ProfileUiEvent.ShowError("Số điện thoại không hợp lệ")) }
            return
        }
        viewModelScope.launch {
            handleUpdate(
                successMessage = "Cập nhật số điện thoại thành công",
                errorMessage = "Không thể cập nhật số điện thoại",
                updateAction = { profileRepository.updatePhoneNumber(newPhoneNumber) }
            )
        }
    }

    fun uploadAvatar(imageUri: Uri) {
        viewModelScope.launch {
            handleUpdate(
                successMessage = "Cập nhật ảnh đại diện thành công",
                errorMessage = "Không thể tải lên ảnh đại diện",
                updateAction = { profileRepository.uploadAvatar(imageUri) }
            )
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmNewPassword: String) {
        viewModelScope.launch {
            if (newPassword != confirmNewPassword) {
                _eventChannel.send(ProfileUiEvent.ShowError("Mật khẩu mới không khớp."))
                return@launch
            }
            if (newPassword.length < 6) {
                _eventChannel.send(ProfileUiEvent.ShowError("Mật khẩu mới phải có ít nhất 6 ký tự."))
                return@launch
            }

            handleUpdate(
                successMessage = "Đổi mật khẩu thành công",
                errorMessage = "Lỗi đổi mật khẩu",
                updateAction = { profileRepository.changePassword(currentPassword, newPassword) }
            )
        }
    }


    // Hàm private chung để xử lý logic update, loại bỏ hoàn toàn code lặp lại
    private suspend fun <T> handleUpdate(
        successMessage: String,
        errorMessage: String,
        updateAction: suspend () -> Resource<T>
    ) {
        _uiState.update { it.copy(isUpdating = true) }
        val result = updateAction()
        when (result) {
            is Resource.Success -> {
                _eventChannel.send(ProfileUiEvent.UpdateSuccess(successMessage))
            }
            is Resource.Error -> {
                _eventChannel.send(ProfileUiEvent.ShowError(result.message ?: errorMessage))
            }
            else -> { /* Trạng thái Loading không cần xử lý ở đây */ }
        }

        _uiState.update { it.copy(isUpdating = false) }
    }

    fun linkGoogleAccount(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            val result = profileRepository.linkGoogleAccount(idToken)
            when (result) {
                is Resource.Success -> {
                    _eventChannel.send(ProfileUiEvent.UpdateSuccess("Liên kết tài khoản Google thành công"))
                    loadProviders() // Reload providers sau khi liên kết thành công
                }
                is Resource.Error -> {
                    _eventChannel.send(ProfileUiEvent.ShowError(result.message ?: "Lỗi liên kết tài khoản Google"))
                }
                else -> {}
            }
            _uiState.update { it.copy(isUpdating = false) }
        }
    }
    
    fun linkEmailPassword(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            val result = profileRepository.linkEmailPassword(email, password)
            when (result) {
                is Resource.Success -> {
                    _eventChannel.send(ProfileUiEvent.UpdateSuccess("Liên kết tài khoản email/password thành công"))
                    loadProviders() // Reload providers sau khi liên kết thành công
                }
                is Resource.Error -> {
                    _eventChannel.send(ProfileUiEvent.ShowError(result.message ?: "Lỗi liên kết tài khoản email/password"))
                }
                else -> {}
            }
            _uiState.update { it.copy(isUpdating = false) }
        }
    }
    
    fun hasPasswordProvider(): Boolean {
        return _uiState.value.providers.contains("password")
    }
    
    fun hasGoogleProvider(): Boolean {
        return _uiState.value.providers.contains("google.com")
    }

    // Hàm clearMessages() không còn cần thiết nữa vì các thông báo đã được xử lý dưới dạng event
}
