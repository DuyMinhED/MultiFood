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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.baonhutminh.multifood.R
import com.baonhutminh.multifood.data.model.Article
import com.baonhutminh.multifood.ui.theme.MultiFoodTheme
import com.baonhutminh.multifood.ui.theme.Red
import com.baonhutminh.multifood.ui.theme.White
import com.baonhutminh.multifood.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel
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

        if (articles.isEmpty()) {
            LaunchedEffect(Unit) {
                viewModel.getArticles()
            }
        }

             when{
                 isLoading-> CircularProgressIndicator()
                 articles.isEmpty()->{
                     Text(text = "Không có dữ liệu")
                 }
                 else->
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
                                         // 🔥 Tại đây bạn có thể điều hướng sang màn chi tiết
                                         // Ví dụ: navController.navigate("detail/${article.id}")
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

@Composable
fun CardArticle(article: Article, onClick: () -> Unit = {}) {
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
            // Hình ảnh
            Image(
                painter = painterResource(id = article.imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(MaterialTheme.shapes.large),
                contentScale = ContentScale.Crop
            )

            // Nội dung bài viết
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
                    text = "Đánh giá: ${article.point} /10 điểm",
                    color = Red,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Hàng nút hành động
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
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Chi tiết")
                    }
                }
            }
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    val vm = HomeViewModel()
    HomeScreen(viewModel = vm)
}
*/
