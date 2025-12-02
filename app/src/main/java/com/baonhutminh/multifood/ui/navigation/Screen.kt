package com.baonhutminh.multifood.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String?=null,
    val icon: ImageVector? = null
) {
    data object Login : Screen("login_screen", "Đăng nhập")
    data object SignUp : Screen("signup_screen", "Đăng ký")

    data object Home : Screen("home_screen", "Trang chủ", Icons.Default.Home)
    data object Search : Screen("search_screen", "Tìm kiếm", Icons.Default.Search)
    data object Profile : Screen("profile_screen", "Tài khoản", Icons.Default.Person)
    data object Settings : Screen("settings_screen", "Cài đặt", Icons.Default.Settings)

    data object CreatePost : Screen("create_post_screen?postId={postId}") { // <-- Đã sửa
        fun createRoute(postId: String? = null): String {
            return if (postId != null) {
                "create_post_screen?postId=$postId"
            } else {
                "create_post_screen"
            }
        }
    }

    data object Detail : Screen("detail_screen/{postId}") {
        fun createRoute(postId: String) = "detail_screen/$postId"
    }
}
