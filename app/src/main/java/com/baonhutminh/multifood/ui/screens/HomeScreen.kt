package com.baonhutminh.multifood.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.baonhutminh.multifood.ui.components.CardPost
import com.baonhutminh.multifood.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val posts by viewModel.posts
    val isLoading by viewModel.isLoading
    val listState = rememberLazyListState()

    // 🔥 Gọi chỉ 1 lần khi mở màn hình
    LaunchedEffect(Unit) {
        viewModel.getPosts()
    }

        Box(
            Modifier
                .padding(10.dp)
                .fillMaxSize()
                .padding(horizontal = 10.dp)
        ) {
            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                posts.isEmpty() -> Text("Không có bài viết.", Modifier.align(Alignment.Center))
                else -> LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(posts) { post ->
                        CardPost(
                            post = post,
                            isFavorite = viewModel.isFavorite(post),
                            onClick = { navController.navigate("detail/${post.id}") },
                            onFavoriteClick = { viewModel.toggleFavorite(post) }
                        )
                    }

            }
        }
    }
}
