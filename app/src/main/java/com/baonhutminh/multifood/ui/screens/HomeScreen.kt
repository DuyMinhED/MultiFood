package com.baonhutminh.multifood.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baonhutminh.multifood.ui.components.AppBottomBar
import com.baonhutminh.multifood.ui.components.AppTopBar
import com.baonhutminh.multifood.ui.components.PostItemCard
import com.baonhutminh.multifood.ui.navigation.Screen
import com.baonhutminh.multifood.viewmodel.HomeViewModel
import com.baonhutminh.multifood.viewmodel.PostFilterTab

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onDetailClick: (String) -> Unit,
    onAccountClick: () -> Unit,
    onCreateClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Tạo bài viết",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        topBar = {
            AppTopBar(Screen.Home)
        },
        bottomBar = {
            AppBottomBar(
                onClickHome = {},
                onAccountClick = onAccountClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val tabs = PostFilterTab.entries.toTypedArray()
            TabRow(selectedTabIndex = tabs.indexOf(uiState.selectedTab)) {
                tabs.forEach { tab ->
                    Tab(
                        selected = tab == uiState.selectedTab,
                        onClick = { viewModel.onTabSelected(tab) },
                        text = { Text(text = tab.title) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp), // Thêm padding dưới để không bị che bởi FAB
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.posts, key = { it.id }) { post ->
                            val isLiked = uiState.userProfile?.likedPostIds?.contains(post.id) == true
                            PostItemCard(
                                post = post,
                                isLiked = isLiked,
                                onLikeClick = { viewModel.toggleLike(post.id) },
                                onItemClick = { onDetailClick(post.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}