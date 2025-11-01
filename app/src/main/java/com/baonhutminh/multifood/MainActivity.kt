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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.baonhutminh.multifood.ui.ExplodeScreen
import com.baonhutminh.multifood.ui.HomeScreen
import com.baonhutminh.multifood.ui.theme.MultiFoodTheme
import com.baonhutminh.multifood.viewmodel.HomeViewModel

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

@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiFoodApp() {
    MultiFoodTheme {
        val items = listOf(
            BottomNavigationItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
            BottomNavigationItem("Explore", Icons.Filled.DateRange, Icons.Outlined.DateRange),
            BottomNavigationItem("Add", Icons.Filled.AddCircle, Icons.Outlined.AddCircle),
            BottomNavigationItem("Favorite", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
            BottomNavigationItem("Profile", Icons.Filled.Person, Icons.Outlined.Person)
        )

        var selectedItemIndex by remember { mutableStateOf(0) }
        val navController = rememberNavController()
        var searchText by remember { mutableStateOf("") }

        val homeViewModel= HomeViewModel()

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            TextField(
                                value = searchText,
                                onValueChange = { searchText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    // THAY ĐỔI: Dùng màu onPrimary từ theme với độ mờ
                                    Text(
                                        text = "Tìm kiếm món ăn...",
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search Icon",
                                        // THAY ĐỔI: Dùng màu onPrimary (trắng) cho icon
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                },
                                trailingIcon = {
                                    if (searchText.isNotEmpty()) {
                                        IconButton(onClick = { searchText = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear search",
                                                // THAY ĐỔI: Dùng màu onPrimary (trắng) cho icon
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                },
                                // THAY ĐỔI: TextField giờ sẽ lấy màu trực tiếp từ theme
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                    cursorColor = MaterialTheme.colorScheme.onPrimary,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                                singleLine = true
                            )
                        },
                        // THAY ĐỔI: Lấy màu nền `primary` (cam) từ theme
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                bottomBar = {
                    // THAY ĐỔI: Lấy màu nền `primary` (cam) từ theme cho nhất quán
                    NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
                        items.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = selectedItemIndex == index,
                                onClick = {
                                    selectedItemIndex = index
                                    navController.navigate(item.title)
                                },
                                label = { Text(text = item.title) },
                                icon = {
                                    Icon(
                                        imageVector = if (index == selectedItemIndex) {
                                            item.selectedIcon
                                        } else {
                                            item.unselectedIcon
                                        },
                                        contentDescription = item.title
                                    )
                                },
                                // THAY ĐỔI: Tùy chỉnh màu cho item để nổi bật trên nền cam
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary, // Màu trắng khi được chọn
                                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f), // Màu trắng mờ khi không được chọn
                                    unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                    indicatorColor = MaterialTheme.colorScheme.primary // Bỏ màu nền của indicator
                                )
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
                        HomeScreen(Modifier, homeViewModel)
                    }

                    composable("explore") {
                        ExplodeScreen()
                    }

                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AppScreenPreview() {
    MultiFoodApp()
}