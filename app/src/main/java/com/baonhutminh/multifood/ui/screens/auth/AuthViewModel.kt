package com.baonhutminh.multifood.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.domain.repository.AuthRepository
import com.baonhutminh.multifood.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<String>?>(null)
    val loginState = _loginState.asStateFlow()

    private val _signupState = MutableStateFlow<Resource<String>?>(null)
    val signupState = _signupState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<Resource<Unit>?>(null)
    val resetPasswordState = _resetPasswordState.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            if (email.isBlank() || pass.isBlank()) {
                _loginState.value = Resource.Error("Vui lòng nhập đầy đủ thông tin")
                return@launch
            }
            // Email validation
            val emailPattern = android.util.Patterns.EMAIL_ADDRESS
            if (!emailPattern.matcher(email).matches()) {
                _loginState.value = Resource.Error("Email không hợp lệ")
                return@launch
            }
            _loginState.value = Resource.Loading()
            _loginState.value = repository.login(email, pass)
        }
    }

    fun signup(name: String, email: String, pass: String, confirmPass: String) {
        viewModelScope.launch {
            if (name.isBlank() || email.isBlank() || pass.isBlank()) {
                _signupState.value = Resource.Error("Vui lòng nhập đầy đủ thông tin")
                return@launch
            }
            // Email validation
            val emailPattern = android.util.Patterns.EMAIL_ADDRESS
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
            _signupState.value = repository.signup(email, pass, name)
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            if (email.isBlank()) {
                _resetPasswordState.value = Resource.Error("Vui lòng nhập email")
                return@launch
            }
            val emailPattern = android.util.Patterns.EMAIL_ADDRESS
            if (!emailPattern.matcher(email).matches()) {
                _resetPasswordState.value = Resource.Error("Email không hợp lệ")
                return@launch
            }
            _resetPasswordState.value = Resource.Loading()
            _resetPasswordState.value = repository.sendPasswordReset(email)
        }
    }

}