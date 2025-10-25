package com.baonhutminh.multifood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.baonhutminh.multifood.ui.theme.BackgroundTopAppBar
import com.baonhutminh.multifood.ui.theme.MultiFoodTheme

// THÊM MỚI 1: Tạo một data class để quản lý thông tin cho mỗi mục
data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiFoodApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiFoodApp() {
    MultiFoodTheme {
        // THÊM MỚI 2: Tạo danh sách các mục cho bottom bar
        val items = listOf(
            BottomNavigationItem(
                title = "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home
            ),
            BottomNavigationItem(
                title = "Explore",
                selectedIcon = Icons.Filled.DateRange,
                unselectedIcon = Icons.Outlined.DateRange
            ),
            BottomNavigationItem(
                title = "Add",
                selectedIcon = Icons.Filled.AddCircle,
                unselectedIcon = Icons.Outlined.AddCircle
            ),
            BottomNavigationItem(
                title = "Favorite",
                selectedIcon = Icons.Filled.Favorite,
                unselectedIcon = Icons.Outlined.FavoriteBorder
            ),
            BottomNavigationItem(
                title = "Profile",
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person
            )
        )

        // THÊM MỚI 3: State để theo dõi mục nào đang được chọn
        var selectedItemIndex by remember { mutableStateOf(0) }

        val navController = rememberNavController()
        var searchText by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Tìm kiếm món ăn...", color = Color.White.copy(alpha = 0.7f)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Icon",
                                    tint = Color.White
                                )
                            },
                            trailingIcon = {
                                if (searchText.isNotEmpty()) {
                                    IconButton(onClick = { searchText = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear search",
                                            tint = Color.White
                                        )
                                    }
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            singleLine = true
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BackgroundTopAppBar
                    )
                )
            },
            // THÊM MỚI 4: Thêm tham số `bottomBar` vào Scaffold
            bottomBar = {
                NavigationBar(containerColor = BackgroundTopAppBar) {
                    // Dùng vòng lặp để tạo 5 mục một cách tự động
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            // `selected` quyết định mục có đang được chọn hay không
                            selected = selectedItemIndex == index,
                            // Khi nhấn vào, cập nhật lại `selectedItemIndex`
                            onClick = {
                                selectedItemIndex = index
                                // Ở đây bạn có thể thêm logic điều hướng, ví dụ:
                                // navController.navigate(item.title)
                            },
                            label = {
                                Text(text = item.title)
                            },
                            icon = {
                                Icon(
                                    // Hiển thị icon tương ứng với trạng thái selected/unselected
                                    imageVector = if (index == selectedItemIndex) {
                                        item.selectedIcon
                                    } else {
                                        item.unselectedIcon
                                    },
                                    contentDescription = item.title
                                )
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") {
                    HomeScreen(searchText = searchText)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(searchText: String = "") {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Nội dung tìm kiếm: $searchText",
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppScreenPreview() {
    MultiFoodApp()
}