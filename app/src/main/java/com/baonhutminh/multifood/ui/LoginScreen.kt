package com.baonhutminh.multifood.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.baonhutminh.multifood.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val isLoading by viewModel.isLoading
    val loginError by viewModel.loginError
    val currentUser by viewModel.currentUser

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    if (currentUser != null) {
        onLoginSuccess()
        return
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Đăng nhập") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.login(email, password) },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp))
                else Text("Đăng nhập")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { viewModel.register(email, password) }) {
                Text("Đăng ký tài khoản mới")
            }

            if (loginError != null) {
                Text(loginError!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
