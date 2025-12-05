package com.baonhutminh.multifood.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.preferences.SettingsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsPreferences.setOnboardingCompleted(true)
        }
    }
}




