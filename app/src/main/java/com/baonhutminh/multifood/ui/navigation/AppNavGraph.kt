package com.baonhutminh.multifood.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.ui.screens.*
import com.baonhutminh.multifood.viewmodel.HomeViewModel


sealed class Screen(val route: String, val title: String? = null) {
    object Home : Screen("home", "Home")
    object Posted : Screen("posted", "Posted")
    object Add : Screen("add", "Add")
    object Favorite : Screen("favorite", "Favorite")
    object Profile : Screen("profile", "Profile")
    object Detail : Screen("detail/{postId}", "Detail") {
        fun createRoute(postId: String) = "detail/$postId"
    }
}


@Composable
fun AppNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel()
            HomeScreen(navController = navController, viewModel = homeViewModel)
        }
        composable(Screen.Detail.route) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            PostDetailScreen(navController = navController, postId = postId)
        }

        composable(Screen.Profile.route) {
        }
        composable(Screen.Favorite.route) {
        }
        composable(Screen.Add.route) {
        }
        composable(Screen.Posted.route) {

        }
    }
}
