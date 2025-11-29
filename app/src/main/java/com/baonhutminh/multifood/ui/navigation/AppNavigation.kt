package com.baonhutminh.multifood.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.baonhutminh.multifood.ui.screens.auth.LoginScreen
import com.baonhutminh.multifood.ui.screens.auth.SignUpScreen
import com.baonhutminh.multifood.ui.screens.account.AccountScreen
import com.baonhutminh.multifood.ui.screens.create.CreateReviewScreen
import com.baonhutminh.multifood.ui.screens.detail.DetailScreen
import com.baonhutminh.multifood.ui.screens.home.HomeScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser != null) {
        // Người dùng đã đăng nhập trước đó, cho vào thẳng Home
        Screen.Home.route
    } else {
        // Chưa đăng nhập, chuyển tới màn Login
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
            // Authentication guard
            if (auth.currentUser == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            } else {
                HomeScreen(
                    onDetailClick = { reviewId ->
                        navController.navigate(Screen.Detail.createRoute(reviewId))
                    },
                    onAccountClick = {
                        navController.navigate(Screen.Account.route)
                    },
                    onCreateClick = {
                        navController.navigate(Screen.CreateReview.route)
                    }
                )
            }
        }

        composable(Screen.Account.route) {
            if (auth.currentUser == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Account.route) { inclusive = true }
                    }
                }
            } else {
                AccountScreen(navController = navController)
            }
        }

        composable(Screen.Detail.route) { backStackEntry ->
            // Authentication guard - Detail có thể xem không cần đăng nhập
            DetailScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CreateReview.route) {
            // Authentication guard
            if (auth.currentUser == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.CreateReview.route) { inclusive = true }
                    }
                }
            } else {
                CreateReviewScreen(
                    onBack = { navController.popBackStack() },
                    onReviewCreated = {
                        // Quay về Home và clear backstack để trigger reload
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

    }
}