package com.baonhutminh.multifood.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import com.baonhutminh.multifood.ui.components.LoadingScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.first
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.baonhutminh.multifood.data.preferences.SettingsPreferences
import com.baonhutminh.multifood.ui.screens.CreatePostScreen
import com.baonhutminh.multifood.ui.screens.HomeScreen
import com.baonhutminh.multifood.ui.screens.LoginScreen
import com.baonhutminh.multifood.ui.screens.OnboardingScreen
import com.baonhutminh.multifood.ui.screens.PostDetailScreen
import com.baonhutminh.multifood.ui.screens.ProfileScreen
import com.baonhutminh.multifood.ui.screens.SearchScreen
import com.baonhutminh.multifood.ui.screens.SettingsScreen
import com.baonhutminh.multifood.ui.screens.SignUpScreen
import com.baonhutminh.multifood.ui.screens.UserProfileScreen
import com.baonhutminh.multifood.viewmodel.OnboardingViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(
    settingsPreferences: SettingsPreferences
) {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    
    // Check onboarding status - đợi load xong trước khi quyết định
    var isCheckingOnboarding by remember { mutableStateOf(true) }
    var onboardingCompleted by remember { mutableStateOf(false) }
    var startDestination by remember { mutableStateOf<String?>(null) }
    
    // Load onboarding status một lần
    LaunchedEffect(Unit) {
        val completed = settingsPreferences.onboardingCompleted.first()
        onboardingCompleted = completed
        startDestination = when {
            !completed -> Screen.Onboarding.route
            auth.currentUser != null -> Screen.Home.route
            else -> Screen.Login.route
        }
        isCheckingOnboarding = false
    }
    
    // Hiển thị loading trong khi đang check onboarding
    val currentStartDestination = startDestination
    if (isCheckingOnboarding || currentStartDestination == null) {
        LoadingScreen()
        return
    }

    NavHost(navController = navController, startDestination = currentStartDestination) {
        
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    // Navigate to appropriate screen after onboarding
                    val destination = if (auth.currentUser != null) {
                        Screen.Home.route
                    } else {
                        Screen.Login.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route){
            LoginScreen(navController)
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }

        composable(Screen.Home.route) {
            if (auth.currentUser == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            } else {
                HomeScreen(
                    onDetailClick = { postId ->
                        navController.navigate(Screen.Detail.createRoute(postId))
                    },
                    onAccountClick = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onCreateClick = {
                        navController.navigate(Screen.CreatePost.createRoute())
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    },
                    onUserProfileClick = { userId ->
                        navController.navigate(Screen.UserProfile.createRoute(userId))
                    }
                )
            }
        }

        composable(Screen.Profile.route) {
            if (auth.currentUser == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Profile.route) { inclusive = true }
                    }
                }
            } else {
                ProfileScreen(
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onLogout = {
                        auth.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    },
                    onClickHome={
                        navController.navigate(Screen.Home.route)
                    }
                )
            }
        }

        composable(Screen.Detail.route) { backStackEntry ->
            PostDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onNavigateToEdit = { postId ->
                    navController.navigate(Screen.CreatePost.createRoute(postId))
                },
                onUserProfileClick = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                }
            )
        }

        composable(
            route = Screen.CreatePost.route,
            arguments = listOf(navArgument("postId") { nullable = true })
        ) {
            CreatePostScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            if (auth.currentUser == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Settings.route) { inclusive = true }
                    }
                }
            } else {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onDetailClick = { postId ->
                    navController.navigate(Screen.Detail.createRoute(postId))
                }
            )
        }

        composable(
            route = Screen.UserProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onPostClick = { postId ->
                    navController.navigate(Screen.Detail.createRoute(postId))
                }
            )
        }
    }
}
