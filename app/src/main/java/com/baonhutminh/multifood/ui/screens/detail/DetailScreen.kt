package com.baonhutminh.multifood.ui.screens.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.data.model.Review
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Màu sắc theo yêu cầu
val ColorOrange = Color(0xFFF39C12) // Cam chủ đạo
val ColorTextDark = Color(0xFF2C3E50)
val ColorTextGray = Color.Gray
val ColorBackgroundLight = Color(0xFFFAFAFA)
val ColorSegmentBg = Color(0xFFF5F6FA)

@Composable
fun DetailScreen(
    viewModel: DetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val state = viewModel.state.value
    val review = state.review

    // State quản lý Tab (0: Thông tin, 1: Đánh giá)
    var selectedTab by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = ColorOrange)
        } else if (state.error.isNotBlank()) {
            // Hiển thị error state
            Column(
                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodyLarge,
                    color = ColorTextDark,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = { 
                        state.review?.let { viewModel.loadReviewDetail(it.id) } ?: onBackClick() 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorOrange)
                ) {
                    Text("Thử lại")
                }
            }
        } else if (review != null) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // --- PHẦN A: HEADER (Ảnh + Info) ---
                DetailHeaderSection(
                    imageUrl = review.imageUrls.firstOrNull() ?: "",
                    onBackClick = onBackClick,
                    isLiked = state.isLiked,
                    onLikeClick = { viewModel.onToggleLike() }
                )

                DetailInfoTitle(
                    title = review.placeName,
                    rating = review.rating.toDouble(),
                    address = review.placeAddress,
                    pricePerPerson = review.pricePerPerson
                )

                // --- PHẦN B: TAB NAVIGATION (Segment Control) ---
                SegmentedControl(
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                // --- PHẦN C: NỘI DUNG ĐỘNG ---
                if (selectedTab == 0) {
                    // C1. Tab Thông tin
                    InfoTabContent(review = review)
                } else {
                    // C2. Tab Đánh giá
                    ReviewTabContent(
                        userRating = state.userRating,
                        userComment = state.userComment,
                        comments = state.comments,
                        isSubmittingComment = state.isSubmittingComment,
                        onRatingChange = { viewModel.onRatingChange(it) },
                        onCommentChange = { viewModel.onCommentChange(it) },
                        onSubmitComment = { viewModel.submitComment() },
                        onCancelComment = {
                            viewModel.onRatingChange(0)
                            viewModel.onCommentChange("")
                        }
                    )
                }

                // Khoảng trắng dưới cùng
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// ==========================================================
// PHẦN A: HEADER COMPONENTS
// ==========================================================
@Composable
fun DetailHeaderSection(
    imageUrl: String,
    onBackClick: () -> Unit,
    isLiked: Boolean,
    onLikeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp) // Chiều cao 250-300px
    ) {
        // Hero Image
        AsyncImage(
            model = imageUrl,
            contentDescription = "Cover",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay Buttons (Top Bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Nút Back
            SmallCircleButton(icon = Icons.Default.ArrowBack, onClick = onBackClick)

            // Nút Heart & Share
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallCircleButton(
                    icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    onClick = onLikeClick,
                    tint = if (isLiked) Color.Red else Color.Black
                )
                SmallCircleButton(icon = Icons.Outlined.Share, onClick = { /* Share */ })
            }
        }
    }
}

@Composable
fun SmallCircleButton(
    icon: ImageVector,
    onClick: () -> Unit,
    tint: Color = Color.Black
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun DetailInfoTitle(title: String, rating: Double, address: String, pricePerPerson: Int) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextDark,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = address.ifBlank { "Đang cập nhật địa chỉ" },
            style = MaterialTheme.typography.bodyMedium,
            color = ColorTextGray
        )

        if (pricePerPerson > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Chi phí trung bình: ${pricePerPerson}₫/người",
                style = MaterialTheme.typography.bodyMedium,
                color = ColorTextDark
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Rating Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Vẽ 5 ngôi sao
            repeat(5) { index ->
                val icon = if (index < rating.toInt()) Icons.Default.Star else Icons.Outlined.Star
                val color = if (index < rating.toInt()) ColorOrange else Color.Gray
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

// ==========================================================
// PHẦN B: TAB NAVIGATION (SEGMENT CONTROL)
// ==========================================================
@Composable
fun SegmentedControl(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ColorSegmentBg) // Nền xám nhạt
            .padding(4.dp), // Padding bao quanh
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tab 1: Thông tin
        TabButton(
            text = "Thông tin",
            isSelected = selectedIndex == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Tab 2: Đánh giá
        TabButton(
            text = "Đánh giá",
            isSelected = selectedIndex == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val bgColor = if (isSelected) Color.White else Color.Transparent
    val textColor = if (isSelected) Color.Black else Color.Gray
    val shadow = if (isSelected) 2.dp else 0.dp

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        shadowElevation = shadow,
        modifier = modifier.fillMaxHeight()
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                fontSize = 14.sp
            )
        }
    }
}

// ==========================================================
// PHẦN C1: NỘI DUNG TAB THÔNG TIN
// ==========================================================
@Composable
fun InfoTabContent(review: Review) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (review.content.isNotBlank()) {
            Text(
                text = review.content,
                fontSize = 14.sp,
                color = ColorTextDark,
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (review.imageUrls.size > 1) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(review.imageUrls) { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        InfoRowItem(icon = Icons.Default.LocationOn, text = review.placeAddress.ifBlank { "Đang cập nhật" })

        if (review.pricePerPerson > 0) {
            InfoRowItem(
                icon = Icons.Default.AttachMoney,
                text = "${review.pricePerPerson}₫ / người"
            )
        }

        InfoRowItem(
            icon = Icons.Default.Schedule,
            text = formatDate(review.createdAt)
        )
    }
}

@Composable
fun InfoRowItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ColorOrange,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = ColorTextDark
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}

// ==========================================================
// PHẦN C2: NỘI DUNG TAB ĐÁNH GIÁ (FORM)
// ==========================================================
@Composable
fun ReviewTabContent(
    userRating: Int,
    userComment: String,
    comments: List<com.baonhutminh.multifood.data.model.Comment>,
    isSubmittingComment: Boolean,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmitComment: () -> Unit,
    onCancelComment: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Card "Viết đánh giá"
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, ColorBackgroundLight, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Viết đánh giá của bạn",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Rating Input (5 ngôi sao lớn)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    repeat(5) { index ->
                        val starIdx = index + 1
                        val icon = if (starIdx <= userRating) Icons.Default.Star else Icons.Outlined.Star
                        val color = if (starIdx <= userRating) ColorOrange else Color.LightGray

                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onRatingChange(starIdx) }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Text Area
                OutlinedTextField(
                    value = userComment,
                    onValueChange = onCommentChange,
                    placeholder = { Text("Chia sẻ trải nghiệm của bạn...", color = Color.Gray, fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(ColorBackgroundLight, RoundedCornerShape(8.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = ColorOrange.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Nút Hủy
                    OutlinedButton(
                        onClick = onCancelComment,
                        enabled = !isSubmittingComment,
                        modifier = Modifier.weight(0.4f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text("Hủy", color = Color.Black)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Nút Gửi
                    Button(
                        onClick = onSubmitComment,
                        enabled = !isSubmittingComment && (userComment.isNotBlank() || userRating > 0),
                        modifier = Modifier.weight(0.6f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorOrange)
                    ) {
                        if (isSubmittingComment) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Gửi bình luận", color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Danh sách comments
        if (comments.isNotEmpty()) {
            Text(
                text = "Bình luận (${comments.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ColorTextDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            comments.forEach { comment ->
                CommentItem(comment = comment)
                Spacer(modifier = Modifier.height(12.dp))
            }
        } else {
            Text(
                text = "Chưa có bình luận nào",
                modifier = Modifier.fillMaxWidth(),
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// Component hiển thị một comment item
@Composable
fun CommentItem(comment: Comment) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ColorBackgroundLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar
            AsyncImage(
                model = comment.userAvatarUrl.ifBlank { "https://i.imgur.com/6VBx3io.png" },
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = comment.userName.ifBlank { "Người dùng" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ColorTextDark
                )
                if (comment.rating > 0) {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(comment.rating) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = ColorOrange,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
                Text(
                    text = comment.content,
                    fontSize = 14.sp,
                    color = ColorTextDark,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = formatDate(comment.createdAt),
                    fontSize = 12.sp,
                    color = ColorTextGray
                )
            }
        }
    }
}