package com.baonhutminh.multifood.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.baonhutminh.multifood.ui.screens.LoginScreen
import com.baonhutminh.multifood.ui.screens.SignUpScreen
import com.baonhutminh.multifood.ui.screens.AccountScreen
import com.baonhutminh.multifood.ui.screens.HomeScreen
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
                        navController.navigate(Screen.Profile.route)
                    },
                    onCreateClick = {
                        navController.navigate(Screen.CreateReview.route)
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
                AccountScreen(navController = navController)
            }
        }

        composable(Screen.Detail.route) { backStackEntry ->

        }

        composable(Screen.CreateReview.route) {
            // Authentication guard

        }

    }
}


