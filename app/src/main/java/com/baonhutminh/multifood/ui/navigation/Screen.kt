package com.baonhutminh.multifood.ui.navigation

sealed class Screen(val route: String, val name:String="") {
    data object Login : Screen("login_screen", "Đăng nhập")
    data object SignUp : Screen("signup_screen","Đăng ký")
    data object Home : Screen("home_screen", "Trang chủ")
    data object CreateReview : Screen("create_review_screen", "Tạo bài viết")
    data object Detail : Screen("detail_screen/{reviewId}") {
        fun createRoute(reviewId: String) = "detail_screen/$reviewId"
    }
    data object Account : Screen("account_screen", "Tài khoản")
}