package com.baonhutminh.multifood

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.baonhutminh.multifood.ui.*
import com.baonhutminh.multifood.ui.theme.MultiFoodTheme
import com.baonhutminh.multifood.viewmodel.HomeViewModel

// ----------------------
//  ROUTE ĐỊNH NGHĨA
// ----------------------
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Explore : Screen("explore")
    object Add : Screen("add")
    object Favorite : Screen("favorite")
    object Profile : Screen("profile")
    object Detail : Screen("detail/{postId}") {
        fun createRoute(postId: String) = "detail/$postId"
    }
}

// ----------------------
//  ITEM BOTTOM NAV
// ----------------------
data class BottomNavigationItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MultiFoodApp() }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiFoodApp() {
    MultiFoodTheme {
        val navController = rememberNavController()
        val homeViewModel: HomeViewModel = viewModel()
        var searchText by remember { mutableStateOf("") }

        // 🧭 Khai báo các tab dưới cùng
        val items = listOf(
            BottomNavigationItem("Home", Screen.Home.route, Icons.Filled.Home, Icons.Outlined.Home),
            BottomNavigationItem("Explore", Screen.Explore.route, Icons.Filled.DateRange, Icons.Outlined.DateRange),
            BottomNavigationItem("Add", Screen.Add.route, Icons.Filled.AddCircle, Icons.Outlined.AddCircle),
            BottomNavigationItem("Favorite", Screen.Favorite.route, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
            BottomNavigationItem("Profile", Screen.Profile.route, Icons.Filled.Person, Icons.Outlined.Person)
        )

        var selectedItemIndex by remember { mutableStateOf(0) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "Tìm kiếm món ăn...",
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Icon",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            },
                            trailingIcon = {
                                if (searchText.isNotEmpty()) {
                                    IconButton(onClick = { searchText = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear search",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            singleLine = true
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedItemIndex == index,
                            onClick = {
                                selectedItemIndex = index
                                navController.navigate(item.route)
                            },
                            label = { Text(item.title) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedItemIndex == index)
                                        item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->

            // 🌈 Điều hướng các màn hình
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        modifier = Modifier,
                        viewModel = homeViewModel,
                        navController = navController
                    )
                }

                composable(
                    Screen.Detail.route,
                    arguments = listOf(navArgument("postId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("postId")
                    PostDetailScreen(postId, homeViewModel)
                }

                composable(Screen.Explore.route) {
                    ExplodeScreen()
                }

                // Các màn hình khác (Add, Favorite, Profile)
                composable(Screen.Add.route) { Text("Thêm bài viết (Add Screen)") }
                composable(Screen.Favorite.route) { Text("Yêu thích (Favorite Screen)") }
                composable(Screen.Profile.route) { Text("Trang cá nhân (Profile Screen)") }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppScreenPreview() {
    MultiFoodApp()
}
