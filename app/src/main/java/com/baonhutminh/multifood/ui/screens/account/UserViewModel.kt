package com.baonhutminh.multifood.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.User
import com.baonhutminh.multifood.domain.repository.AuthRepository
import com.baonhutminh.multifood.domain.repository.UserRepository
import com.baonhutminh.multifood.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<Resource<User?>?>(null)
    val userState = _userState.asStateFlow()

    fun loadCurrentUser() {
        viewModelScope.launch {
            _userState.value = Resource.Loading()
            val result = userRepository.getCurrentUser()
            _userState.value = when (result) {
                is Resource.Success -> {
                    // Cập nhật lastActiveAt nếu load thành công
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


