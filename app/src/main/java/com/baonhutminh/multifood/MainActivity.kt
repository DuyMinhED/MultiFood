package com.baonhutminh.multifood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.baonhutminh.multifood.ui.LoginScreen
import com.baonhutminh.multifood.ui.PostsScreen
import com.baonhutminh.multifood.ui.ProfileScreen
import com.baonhutminh.multifood.ui.theme.MultiFoodTheme
import com.baonhutminh.multifood.viewmodel.AuthViewModel
import com.baonhutminh.multifood.viewmodel.MainScreenType
import com.baonhutminh.multifood.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val authViewModel: AuthViewModel = viewModel()
            val currentUser by authViewModel.currentUser

            if (currentUser == null) {
                LoginScreen(onLoginSuccess = { /* refresh UI */ })
            } else {
                MainScreen(onLogout = { authViewModel.logout() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit = {},mainViewModel: MainViewModel = viewModel()) {
    val currentScreen by mainViewModel.currentScreen

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == MainScreenType.POSTS,
                    onClick = { mainViewModel.navigateTo(MainScreenType.POSTS) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Bài viết") },
                    label = { Text("Bài viết") }
                )
                NavigationBarItem(
                    selected = currentScreen == MainScreenType.PROFILE,
                    onClick = { mainViewModel.navigateTo(MainScreenType.PROFILE) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Tài khoản") },
                    label = { Text("Tài khoản") }
                )
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            MainScreenType.POSTS -> PostsScreen(modifier = Modifier.padding(innerPadding))
            MainScreenType.PROFILE -> ProfileScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}
