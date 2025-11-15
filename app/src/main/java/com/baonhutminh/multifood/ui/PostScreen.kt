package com.baonhutminh.multifood.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.baonhutminh.multifood.ui.composable.PostCard
import com.baonhutminh.multifood.viewmodel.PostFilterType
import com.baonhutminh.multifood.viewmodel.PostsViewModel

/**
 * Màn hình hiển thị danh sách bài viết (Posts)
 * - Lọc: tất cả, đã thích, của tôi
 * - Realtime update từ Firestore
 * - Toggle thích / bỏ thích
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostsScreen(
    modifier: Modifier = Modifier,
    viewModel: PostsViewModel = viewModel()
) {
    val postList by viewModel.posts
    val isLoading by viewModel.isLoading
    val selectedFilter by viewModel.filterType

    val tabs = listOf("Tất cả", "Đã thích", "Của tôi")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Bài viết") })
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
        ) {
            // --- Thanh Tab lọc ---
            TabRow(selectedTabIndex = selectedFilter.ordinal) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = selectedFilter.ordinal == index,
                        onClick = { viewModel.setFilter(PostFilterType.entries[index]) }
                    )
                }
            }

            // --- Nội dung danh sách ---
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator()
                    }

                    postList.isEmpty() -> {
                        Text("Không có bài viết nào.", style = MaterialTheme.typography.bodyMedium)
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(postList) { post ->
                                PostCard(
                                    post = post,
                                    onSaveClick = {
                                        viewModel.toggleFavorite(post.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
