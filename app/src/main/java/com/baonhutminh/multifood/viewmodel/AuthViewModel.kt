package com.baonhutminh.multifood.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    val isLoading = mutableStateOf(false)
    val loginError = mutableStateOf<String?>(null)
    val currentUser = mutableStateOf(authRepository.currentUser)

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                loginError.value = null
                val user = authRepository.login(email, password)
                currentUser.value = user
            } catch (e: Exception) {
                loginError.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                loginError.value = null
                val user = authRepository.register(email, password)
                currentUser.value = user
            } catch (e: Exception) {
                loginError.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun logout() {
        authRepository.logout()
        currentUser.value = null
    }
}
