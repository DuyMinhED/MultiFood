package com.baonhutminh.multifood.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.ui.theme.MultiFoodTheme
import com.baonhutminh.multifood.ui.theme.Red
import com.baonhutminh.multifood.ui.theme.White
import com.baonhutminh.multifood.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    navController: NavController
) {
    MultiFoodTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF787A7B)),
            contentAlignment = Alignment.Center
        ) {
            val articles by viewModel.articles
            val isLoading by viewModel.isLoading

            // 🔥 Gọi getArticles() chỉ 1 lần duy nhất khi vào màn hình
            LaunchedEffect(key1 = true) {
                if (articles.isEmpty()) {
                    viewModel.getArticles()
                }
            }

            when {
                isLoading -> {
                    CircularProgressIndicator()
                }

                articles.isEmpty() -> {
                    Text(text = "Không có dữ liệu")
                }

                else -> {
                    LazyColumn(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(articles.size) { index ->
                            val article = articles[index]
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                CardArticle(
                                    article = article,
                                    onClick = {
                                        // Điều hướng tới chi tiết bài viết
                                        navController.navigate("detail/${article.id}")
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CardArticle(article: Post, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column {
            // ✅ Bọc AsyncImage trong try-catch Compose-friendly
            AsyncImage(
                model = article.images.firstOrNull() ?: "",
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(MaterialTheme.shapes.large),
                contentScale = ContentScale.Crop,
                onError = { /* hiển thị ảnh fallback nếu lỗi */ }
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1
                )

                Text(
                    text = article.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )

                Text(
                    text = "Đánh giá: ${article.rating} /10 điểm",
                    color = Red,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Thích")
                    }

                    OutlinedButton(
                        onClick = { onClick() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Chi tiết")
                    }
                }
            }
        }
    }
}



