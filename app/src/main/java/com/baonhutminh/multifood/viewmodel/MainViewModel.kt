package com.baonhutminh.multifood.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

enum class MainScreenType {
    POSTS, PROFILE
}

class MainViewModel : ViewModel() {
    // Màn hình hiện tại
    val currentScreen = mutableStateOf(MainScreenType.POSTS)

    fun navigateTo(screen: MainScreenType) {
        currentScreen.value = screen
    }
}
