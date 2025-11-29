package com.baonhutminh.multifood.ui.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.baonhutminh.multifood.data.model.Post // Import đúng model của bạn

@Composable
fun ReviewItemCard(
    review: Post,                 // Dùng trực tiếp model Review
    isLiked: Boolean,               // Trạng thái tim (từ User.likedReviewIds)
    onDetailClick: (String) -> Unit,
    onLikeClick: (String) -> Unit
) {
    // Màu sắc
    val BrandOrange = Color(0xFFFF9800)
    val TextMainColor = Color(0xFF333333)
    val TextSubColor = Color(0xFF757575)

    Card(
        onClick = { onDetailClick(review.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column {
            // --- 1. ẢNH USER ĐĂNG ---
            Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                // Lấy ảnh đầu tiên trong list ảnh user up
                val displayImage = review.imageUrls.firstOrNull()
                    ?: "https://via.placeholder.com/400x200" // Ảnh placeholder nếu ko có ảnh

                AsyncImage(
                    model = displayImage,
                    contentDescription = "Review Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Badge Rating (Góc trên phải)
                Surface(
                    shape = RoundedCornerShape(bottomStart = 12.dp),
                    color = BrandOrange,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = review.rating.toString(), // VD: 5
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // --- 2. NỘI DUNG REVIEW ---
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = review.placeName.ifBlank { "Đang cập nhật" },
                    style = MaterialTheme.typography.titleMedium,
                    color = TextMainColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (review.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = review.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMainColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = BrandOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${review.rating}/5",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMainColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = TextSubColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = review.placeAddress.ifBlank { "Đang cập nhật" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSubColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = { onLikeClick(review.id) }) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) BrandOrange else TextSubColor
                        )
                    }
                }
            }
        }
    }
}