package com.baonhutminh.multifood.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String) {
    TopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}


@Composable
fun AppBottomBar(navController: NavController) {
    val items = listOf(
        Screen.Home,
        Screen.Posted,
        Screen.Add,
        Screen.Favorite,
        Screen.Profile
    )

    // Quan sát route hiện tại
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Áp dụng màu theo theme
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary, // nền
        contentColor = MaterialTheme.colorScheme.onPrimary    // màu icon & text
    ) {
        items.forEach { screen ->
            val selected = currentDestination?.route == screen.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    when (screen) {
                        Screen.Home -> Icon(Icons.Default.Home, contentDescription = "Trang chủ")
                        Screen.Posted -> Icon(Icons.Default.List, contentDescription = "Đã đăng")
                        Screen.Add -> Icon(Icons.Default.AddCircle, contentDescription = "Thêm")
                        Screen.Favorite -> Icon(Icons.Default.Favorite, contentDescription = "Yêu thích")
                        Screen.Profile -> Icon(Icons.Default.Person, contentDescription = "Cá nhân")
                        else -> Icon(Icons.Default.Home, contentDescription = null)
                    }
                },
                label = { Text(screen.title ?: "") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

