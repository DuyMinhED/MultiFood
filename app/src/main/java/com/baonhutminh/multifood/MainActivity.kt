package com.baonhutminh.multifood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.baonhutminh.multifood.data.model.AppTheme
import com.baonhutminh.multifood.data.preferences.SettingsPreferences
import com.baonhutminh.multifood.ui.LoginScreen
import com.baonhutminh.multifood.ui.PostsScreen
import com.baonhutminh.multifood.ui.ProfileScreen
import com.baonhutminh.multifood.ui.SettingsScreen
import com.baonhutminh.multifood.ui.theme.MultiFoodTheme
import com.baonhutminh.multifood.viewmodel.AuthViewModel
import com.baonhutminh.multifood.viewmodel.MainScreenType
import com.baonhutminh.multifood.viewmodel.MainViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsPreferences = SettingsPreferences(this)

        setContent {
            val authViewModel: AuthViewModel = viewModel()
            val currentUser by authViewModel.currentUser

            var appTheme by remember { mutableStateOf(AppTheme.ORANGE) }
            var darkMode by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                settingsPreferences.appTheme.collect { theme ->
                    appTheme = theme
                }
            }

            LaunchedEffect(Unit) {
                settingsPreferences.darkModeEnabled.collect { enabled ->
                    darkMode = enabled
                }
            }

            MultiFoodTheme(
                appTheme = appTheme,
                darkTheme = darkMode
            ) {
                if (currentUser == null) {
                    LoginScreen(onLoginSuccess = { })
                } else {
                    MainScreen(onLogout = { authViewModel.logout() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit = {},
    mainViewModel: MainViewModel = viewModel()
) {
    val currentScreen by mainViewModel.currentScreen

    Scaffold(
        bottomBar = {
            if (currentScreen != MainScreenType.SETTINGS) {
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
        }
    ) { innerPadding ->
        when (currentScreen) {
            MainScreenType.POSTS -> PostsScreen(
                modifier = Modifier.padding(innerPadding)
            )
            MainScreenType.PROFILE -> ProfileScreen(
                modifier = Modifier.padding(innerPadding),
                onNavigateToSettings = { mainViewModel.navigateTo(MainScreenType.SETTINGS) },
                onLogout = onLogout
            )
            MainScreenType.SETTINGS -> SettingsScreen(
                modifier = Modifier.padding(innerPadding),
                onNavigateBack = { mainViewModel.navigateTo(MainScreenType.PROFILE) }
            )
        }
    }
}
