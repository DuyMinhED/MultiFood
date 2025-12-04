package com.baonhutminh.multifood.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.User
import com.baonhutminh.multifood.data.repository.AuthRepository
import com.baonhutminh.multifood.data.repository.UserRepository
import com.baonhutminh.multifood.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<String>?>(null)
    val loginState = _loginState.asStateFlow()

    private val _signupState = MutableStateFlow<Resource<String>?>(null)
    val signupState = _signupState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<Resource<Unit>?>(null)
    val resetPasswordState = _resetPasswordState.asStateFlow()

    private val _userState = MutableStateFlow<Resource<User?>?>(null)
    val userState = _userState.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            if (email.isBlank() || pass.isBlank()) {
                _loginState.value = Resource.Error("Vui lòng nhập đầy đủ thông tin")
                return@launch
            }
            // Email validation
            val emailPattern = Patterns.EMAIL_ADDRESS
            if (!emailPattern.matcher(email).matches()) {
                _loginState.value = Resource.Error("Email không hợp lệ")
                return@launch
            }
            _loginState.value = Resource.Loading()
            _loginState.value = authRepository.login(email, pass)
        }
    }

    fun signup(name: String, email: String, pass: String, confirmPass: String) {
        viewModelScope.launch {
            if (name.isBlank() || email.isBlank() || pass.isBlank()) {
                _signupState.value = Resource.Error("Vui lòng nhập đầy đủ thông tin")
                return@launch
            }
            // Email validation
            val emailPattern = Patterns.EMAIL_ADDRESS
            if (!emailPattern.matcher(email).matches()) {
                _signupState.value = Resource.Error("Email không hợp lệ")
                return@launch
            }
            if (pass != confirmPass) {
                _signupState.value = Resource.Error("Mật khẩu nhập lại không khớp")
                return@launch
            }
            if (pass.length < 6) {
                _signupState.value = Resource.Error("Mật khẩu phải từ 6 ký tự trở lên")
                return@launch
            }

            _signupState.value = Resource.Loading()
            _signupState.value = authRepository.signup(email, pass, name)
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            _loginState.value = authRepository.signInWithGoogle(idToken)
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            if (email.isBlank()) {
                _resetPasswordState.value = Resource.Error("Vui lòng nhập email")
                return@launch
            }
            val emailPattern = Patterns.EMAIL_ADDRESS
            if (!emailPattern.matcher(email).matches()) {
                _resetPasswordState.value = Resource.Error("Email không hợp lệ")
                return@launch
            }
            _resetPasswordState.value = Resource.Loading()
            _resetPasswordState.value = authRepository.sendPasswordReset(email)
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _userState.value = Resource.Loading()
            val result = userRepository.getCurrentUser()
            _userState.value = when (result) {
                is Resource.Success -> {
                    userRepository.updateLastActive()
                    Resource.Success(result.data)
                }
                is Resource.Error -> Resource.Error(result.message ?: "Không thể tải thông tin người dùng")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun logout() {
        authRepository.signOut()
        _userState.value = null
    }
}