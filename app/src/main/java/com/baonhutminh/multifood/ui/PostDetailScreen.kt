package com.baonhutminh.multifood.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.baonhutminh.multifood.viewmodel.HomeViewModel

@Composable
fun PostDetailScreen(postId: String?, viewModel: HomeViewModel = viewModel()) {
    val post = viewModel.articles.value.find { it.id == postId }

    if (post == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy bài viết")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            item {
                AsyncImage(
                    model = post.images.firstOrNull(),
                    contentDescription = post.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(MaterialTheme.shapes.large),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(12.dp))
                Text(post.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("⭐ ${post.rating} điểm | ${post.address}")
                Spacer(Modifier.height(8.dp))
                Text(post.content)
                Spacer(Modifier.height(8.dp))
                Text("Người đăng: ${post.author} - ${post.date}", color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Text("Bình luận:", fontWeight = FontWeight.Bold)
                post.comments.forEach {
                    Text("• $it")
                }
            }
        }
    }
}
