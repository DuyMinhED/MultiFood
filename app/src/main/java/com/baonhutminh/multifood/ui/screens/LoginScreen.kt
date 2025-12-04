package com.baonhutminh.multifood.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.baonhutminh.multifood.ui.components.CustomButton
import com.baonhutminh.multifood.ui.components.CustomTextField
import com.baonhutminh.multifood.ui.navigation.Screen
import com.baonhutminh.multifood.util.Resource
import com.baonhutminh.multifood.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val loginState by viewModel.loginState.collectAsState()
    val resetPasswordState by viewModel.resetPasswordState.collectAsState()
    val context = LocalContext.current

    // Lắng nghe kết quả đăng nhập
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is Resource.Success -> {
                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true } // Xóa Login khỏi backstack
                }
            }
            is Resource.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    // Lắng nghe kết quả gửi email đặt lại mật khẩu
    LaunchedEffect(resetPasswordState) {
        when (val state = resetPasswordState) {
            is Resource.Success -> {
                Toast.makeText(context, "Đã gửi email đặt lại mật khẩu", Toast.LENGTH_LONG).show()
            }
            is Resource.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Food Review App", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        CustomTextField(value = email, onValueChange = { 
            email = it
            emailError = null
        }, label = "Email")
        if (emailError != null) {
            Text(emailError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(8.dp))
        CustomTextField(
            value = password,
            onValueChange = { 
                password = it
                passwordError = null
            },
            label = "Mật khẩu",
            visualTransformation = PasswordVisualTransformation()
        )
        if (passwordError != null) {
            Text(passwordError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(24.dp))
        CustomButton(
            text = "Đăng nhập",
            onClick = { 
                var valid = true
                if (email.isBlank()) {
                    emailError = "Vui lòng nhập email"
                    valid = false
                }
                if (password.isBlank()) {
                    passwordError = "Vui lòng nhập mật khẩu"
                    valid = false
                }
                if (valid) {
                    viewModel.login(email, password)
                }
            },
            isLoading = loginState is Resource.Loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { viewModel.resetPassword(email) }) {
            Text("Quên mật khẩu?")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { navController.navigate(Screen.SignUp.route) }) {
            Text("Chưa có tài khoản? Đăng ký ngay")
        }
    }
}