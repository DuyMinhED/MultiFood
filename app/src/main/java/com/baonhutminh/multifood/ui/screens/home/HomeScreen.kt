package com.baonhutminh.multifood.ui.screens.home

import BaiViet
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.baonhutminh.multifood.data.source.local.layDulieu
import kotlin.collections.mutableListOf

@Composable
fun HomeScreen(){

    val articles = remember { mutableStateListOf<BaiViet>().apply {
        addAll(layDulieu.layToanBoBaiViet())
    }}

    LazyColumn {
        for (item in articles){
            item{
                ArticleCard(item,{},{})
            }
        }
    }



}

@Composable
fun ArticleCard(
    article: BaiViet,
    onLikeClick: () -> Unit,
    onViewClick: () -> Unit,
    modifier: Modifier = Modifier // Thêm modifier làm tham số
) {
    // Dùng Card để có nền, bo góc và đổ bóng đẹp hơn
    Card(
        // SỬA 5: Dùng modifier linh hoạt
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            // Thêm padding bên trong Card
            modifier = Modifier.padding(16.dp)
        ) {
            // SỬA 6: Bỏ comment và hiển thị hình ảnh đúng cách
            Image(
                painter = painterResource(id = article.imageResId),
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp), // Giới hạn chiều cao của ảnh
                contentScale = ContentScale.Crop // Đảm bảo ảnh lấp đầy mà không bị méo
            )

            Spacer(modifier = Modifier.height(16.dp)) // Thêm khoảng cách

            // SỬA 7: Áp dụng style cho Text để trông đẹp hơn
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = article.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            // SỬA 8: Thêm khoảng cách giữa các nút
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Tự động thêm khoảng cách 8.dp giữa các item
            ) {
                Button(
                    onClick = onLikeClick,
                    modifier = Modifier.weight(1f) // Chia đều không gian cho các nút
                ) {
                    Text(text = "Thích")
                }
                Button(
                    onClick = onViewClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Xem")
                }
            }
        }
    }
}

