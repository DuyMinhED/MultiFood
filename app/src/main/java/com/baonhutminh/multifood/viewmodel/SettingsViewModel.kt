package com.baonhutminh.multifood.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.AppTheme
import com.baonhutminh.multifood.data.preferences.SettingsPreferences
import com.baonhutminh.multifood.data.repository.AuthRepository
import com.baonhutminh.multifood.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. Định nghĩa một data class để chứa toàn bộ trạng thái của UI
data class SettingsUiState(
    val appTheme: AppTheme = AppTheme.ORANGE,
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsPreferences: SettingsPreferences,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // 2. Kết hợp các Flow từ Preferences thành một StateFlow<SettingsUiState> duy nhất
    val uiState: StateFlow<SettingsUiState> = combine(
        settingsPreferences.appTheme,
        settingsPreferences.darkModeEnabled,
        settingsPreferences.notificationsEnabled
    ) { theme, isDark, notificationsOn ->
        SettingsUiState(
            appTheme = theme,
            isDarkMode = isDark,
            notificationsEnabled = notificationsOn
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState() // Cung cấp giá trị khởi tạo
    )

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsPreferences.setAppTheme(theme)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setNotificationsEnabled(enabled)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setDarkModeEnabled(enabled)
        }
    }

    fun logout() {
        // Gọi signOut qua AuthRepository để đảm bảo Google Sign-In cũng được sign out
        authRepository.signOut()
        // Việc điều hướng sẽ được xử lý bởi AppNavigation hoặc Activity
    }

    fun changePassword(current: String, new: String, confirm: String) {
        // TODO: Thêm logic đổi mật khẩu ở đây nếu cần, có thể phát ra event để UI xử lý
    }
}
