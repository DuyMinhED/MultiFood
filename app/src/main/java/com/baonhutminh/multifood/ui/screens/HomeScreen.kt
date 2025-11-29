package com.baonhutminh.multifood.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baonhutminh.multifood.ui.components.AppBottomBar
import com.baonhutminh.multifood.ui.components.Header
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
    val posts = viewModel.posts.value
    val isLoading = viewModel.isLoading.value
    val selectedTab = viewModel.selectedTab.value
    val errorMessage = viewModel.errorMessage.value

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
            Column(modifier = Modifier.fillMaxWidth()) {
                Header(Screen.Home)
            }
        },
        bottomBar = {
            AppBottomBar(
                onHomeClick = {},
                onAccountClick = onAccountClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val tabs = PostFilterTab.values()
            TabRow(selectedTabIndex = tabs.indexOf(selectedTab)) {
                tabs.forEach { tab ->
                    Tab(
                        selected = tab == selectedTab,
                        onClick = { viewModel.onTabSelected(tab) },
                        text = { Text(text = tab.title) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(posts, key = { it.id }) { post ->
                            PostItemCard(
                                post = post,
                                isLiked = false, // Sẽ được cập nhật sau
                                onLikeClick = {},
                                onItemClick = { onDetailClick(post.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}