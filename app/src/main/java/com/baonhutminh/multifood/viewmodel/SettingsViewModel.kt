package com.baonhutminh.multifood.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.AppTheme
import com.baonhutminh.multifood.data.preferences.SettingsPreferences
import com.baonhutminh.multifood.data.repository.ProfileRepository
import com.baonhutminh.multifood.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application, // AndroidViewModel requires Application
    private val settingsPreferences: SettingsPreferences,
    private val profileRepository: ProfileRepository
) : AndroidViewModel(application) {

    private val _appTheme = mutableStateOf(AppTheme.ORANGE)
    val appTheme: State<AppTheme> = _appTheme

    private val _notificationsEnabled = mutableStateOf(true)
    val notificationsEnabled: State<Boolean> = _notificationsEnabled

    private val _darkModeEnabled = mutableStateOf(false)
    val darkModeEnabled: State<Boolean> = _darkModeEnabled

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _successMessage = mutableStateOf<String?>(null)
    val successMessage: State<String?> = _successMessage

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _appTheme.value = settingsPreferences.appTheme.first()
            _notificationsEnabled.value = settingsPreferences.notificationsEnabled.first()
            _darkModeEnabled.value = settingsPreferences.darkModeEnabled.first()
        }
    }

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsPreferences.setAppTheme(theme)
            _appTheme.value = theme
            _successMessage.value = "Đã đổi màu chủ đạo sang ${theme.displayName}"
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setNotificationsEnabled(enabled)
            _notificationsEnabled.value = enabled
            _successMessage.value = if (enabled) "Đã bật thông báo" else "Đã tắt thông báo"
        }
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setDarkModeEnabled(enabled)
            _darkModeEnabled.value = enabled
            _successMessage.value = if (enabled) "Đã bật chế độ tối" else "Đã tắt chế độ tối"
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            _errorMessage.value = "Vui lòng điền đầy đủ thông tin"
            return
        }

        if (newPassword.length < 6) {
            _errorMessage.value = "Mật khẩu mới phải có ít nhất 6 ký tự"
            return
        }

        if (newPassword != confirmPassword) {
            _errorMessage.value = "Mật khẩu xác nhận không khớp"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = profileRepository.changePassword(currentPassword, newPassword)) {
                is Resource.Success -> {
                    _successMessage.value = "Đổi mật khẩu thành công"
                }
                is Resource.Error -> {
                    val friendlyMessage = when {
                        result.message?.contains("wrong-password", ignoreCase = true) == true ->
                            "Mật khẩu hiện tại không đúng"
                        result.message?.contains("requires-recent-login", ignoreCase = true) == true ->
                            "Vui lòng đăng nhập lại để thực hiện thao tác này"
                        else -> result.message ?: "Lỗi đổi mật khẩu"
                    }
                    _errorMessage.value = friendlyMessage
                }
                else -> {}
            }

            _isLoading.value = false
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}