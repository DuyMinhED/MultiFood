package com.baonhutminh.multifood.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.baonhutminh.multifood.ui.components.AuthorHeader
import com.baonhutminh.multifood.ui.components.CommentItem
import com.baonhutminh.multifood.ui.components.FullscreenImageGallery
import com.baonhutminh.multifood.ui.screens.PostDetailConstants.IMAGE_HEIGHT_DP
import com.baonhutminh.multifood.ui.screens.PostDetailConstants.BOTTOM_SPACER_DP
import com.baonhutminh.multifood.ui.screens.PostDetailConstants.IMAGE_INDICATOR_SIZE_DP
import com.baonhutminh.multifood.viewmodel.PostDetailEvent
import com.baonhutminh.multifood.viewmodel.PostDetailViewModel
import com.baonhutminh.multifood.viewmodel.PostDetailUiState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PostDetailScreen(
    viewModel: PostDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val postWithAuthor = uiState.postWithAuthor
    val snackbarHostState = remember { SnackbarHostState() }
    var showFullscreenGallery by remember { mutableStateOf(false) }
    var fullscreenImageIndex by remember { mutableStateOf(0) }
    var isLikedAnimating by remember { mutableStateOf(false) }
    
    // Trigger animation when like state changes
    LaunchedEffect(uiState.isLiked) {
        if (uiState.isLiked) {
            isLikedAnimating = true
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            viewModel.clearErrorMessage()
        }
    }

    // Handle navigation events (e.g., after deleting post)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PostDetailEvent.NavigateBack -> {
                    onNavigateBack()
                }
                is PostDetailEvent.NavigateToHome -> {
                    onNavigateToHome()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = postWithAuthor?.post?.restaurantName?.ifEmpty { "Chi tiết" } ?: "Chi tiết") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    // Chỉ hiển thị nút khi postWithAuthor không null
                    postWithAuthor?.let {
                        if (it.post.userId == uiState.currentUser?.id) {
                            IconButton(onClick = { onNavigateToEdit(it.post.id) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Chỉnh sửa bài viết")
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            CommentInputField(uiState, viewModel)
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (postWithAuthor != null) {
                val post = postWithAuthor.post
                LazyColumn(contentPadding = paddingValues) {
                    // Author Header
                    item {
                        AuthorHeader(
                            author = postWithAuthor.author,
                            createdAt = post.createdAt
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    // Images
                    item {
                        if (uiState.images.isNotEmpty()) {
                            val pagerState = rememberPagerState { uiState.images.size }
                            Box(
                                modifier = Modifier
                                    .height(IMAGE_HEIGHT_DP.dp)
                                    .clickable {
                                        fullscreenImageIndex = pagerState.currentPage
                                        showFullscreenGallery = true
                                    }
                            ) {
                                HorizontalPager(state = pagerState) { page ->
                                    AsyncImage(
                                        model = uiState.images[page],
                                        contentDescription = "Post Image ${page + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                if (pagerState.pageCount > 1) {
                                    Row(
                                        Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 8.dp)
                                    ) {
                                        repeat(pagerState.pageCount) { iteration ->
                                            val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                                            Box(
                                                modifier = Modifier
                                                    .padding(2.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                                    .size(IMAGE_INDICATOR_SIZE_DP.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Empty state khi không có ảnh
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IMAGE_HEIGHT_DP.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ImageNotSupported,
                                        contentDescription = "Không có hình ảnh",
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Không có hình ảnh",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    // Restaurant Information Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Section Header
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Store,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Thông tin nhà hàng",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Restaurant Name
                                Text(
                                    post.restaurantName.ifEmpty { "Nhà hàng" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Address
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = "Địa chỉ",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        post.restaurantAddress.ifEmpty { "Địa chỉ" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Post Review Information Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Section Header
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Đánh giá của bạn",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Title
                                Text(
                                    post.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Rating
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Đánh giá",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color(0xFFFFC107)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "%.1f / 5.0".format(post.rating),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                // Price
                                if (post.pricePerPerson > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.AccountBalanceWallet,
                                                contentDescription = "Price",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = formatPrice(post.pricePerPerson) + " / người",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Content
                                Text(
                                    post.content,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Like button with animation
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val scale by animateFloatAsState(
                                        targetValue = if (isLikedAnimating) 1.3f else 1f,
                                        animationSpec = tween(200),
                                        finishedListener = { isLikedAnimating = false }
                                    )
                                    
                                    IconButton(
                                        onClick = {
                                            if (!uiState.isLiked) {
                                                isLikedAnimating = true
                                            }
                                            viewModel.toggleLike()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (uiState.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Thích",
                                            tint = if (uiState.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.scale(scale)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${post.likeCount} lượt thích",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    item {
                        Text(
                            "Bình luận (${uiState.comments.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }

                    if (uiState.comments.isEmpty()) {
                        item {
                            Text(
                                "Chưa có bình luận nào. Hãy là người đầu tiên!",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        items(uiState.comments, key = { it.comment.id }) { commentWithAuthor ->
                            val isOwner = commentWithAuthor.comment.userId == uiState.currentUser?.id
                            val isEditing = uiState.editingCommentId == commentWithAuthor.comment.id
                            
                            CommentItem(
                                commentWithAuthor = commentWithAuthor,
                                isOwner = isOwner,
                                isEditing = isEditing,
                                editingText = uiState.editingCommentText,
                                onEditingTextChange = viewModel::onEditingCommentTextChange,
                                onEditClick = {
                                    viewModel.startEditingComment(
                                        commentWithAuthor.comment.id,
                                        commentWithAuthor.comment.content
                                    )
                                },
                                onDeleteClick = {
                                    viewModel.deleteComment(commentWithAuthor.comment.id)
                                },
                                onSaveEdit = viewModel::updateComment,
                                onCancelEdit = viewModel::cancelEditingComment,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(BOTTOM_SPACER_DP.dp))
                    }
                }
            }
        }
        
        // Fullscreen Image Gallery
        if (showFullscreenGallery && uiState.images.isNotEmpty()) {
            FullscreenImageGallery(
                images = uiState.images,
                initialIndex = fullscreenImageIndex,
                onDismiss = { showFullscreenGallery = false }
            )
        }
    }
}

private fun formatPrice(price: Int): String {
    return NumberFormat.getNumberInstance(Locale("vi", "VN")).format(price) + "đ"
}

@Composable
private fun CommentInputField(uiState: PostDetailUiState, viewModel: PostDetailViewModel) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.commentInput,
                onValueChange = viewModel::onCommentInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Viết bình luận...") },
                shape = RoundedCornerShape(24.dp),
                enabled = !uiState.isAddingComment
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                if (uiState.isAddingComment) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    IconButton(
                        onClick = { viewModel.addComment() },
                        enabled = uiState.commentInput.isNotBlank()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Gửi bình luận",
                            tint = if (uiState.commentInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
