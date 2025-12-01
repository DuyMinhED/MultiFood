package com.baonhutminh.multifood.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.baonhutminh.multifood.ui.screens.CreatePostScreen
import com.baonhutminh.multifood.ui.screens.LoginScreen
import com.baonhutminh.multifood.ui.screens.PostDetailScreen
import com.baonhutminh.multifood.ui.screens.SignUpScreen
import com.baonhutminh.multifood.ui.screens.HomeScreen
import com.baonhutminh.multifood.ui.screens.ProfileScreen
import com.baonhutminh.multifood.ui.screens.SettingsScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser != null) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(navController = navController, startDestination = startDestination) {

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
                        navController.navigate(Screen.CreatePost.route)
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
            // HiltViewModel sẽ tự động lấy postId từ backStackEntry
            PostDetailScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.CreatePost.route) {
            CreatePostScreen(onNavigateBack = { navController.popBackStack() })
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
    }
}
