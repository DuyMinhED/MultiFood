package com.baonhutminh.multifood.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.baonhutminh.multifood.ui.components.AuthorHeader
import com.baonhutminh.multifood.ui.components.CommentItem
import com.baonhutminh.multifood.ui.components.FullscreenImageGallery
import com.baonhutminh.multifood.viewmodel.PostDetailEvent
import com.baonhutminh.multifood.viewmodel.PostDetailViewModel
import com.baonhutminh.multifood.viewmodel.PostDetailUiState
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PostDetailScreen(
    viewModel: PostDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onUserProfileClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val postWithAuthor = uiState.postWithAuthor
    val snackbarHostState = remember { SnackbarHostState() }
    
    // State cho fullscreen image gallery
    var showFullscreenGallery by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf(0) }
    
    // State cho like animation
    var isLikedAnimating by remember { mutableStateOf(false) }

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
                        postWithAuthor.author?.let { author ->
                            AuthorHeader(
                                author = author,
                                createdAt = post.createdAt,
                                onAuthorClick = onUserProfileClick
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    // Images section
                    item {
                        if (uiState.images.isNotEmpty()) {
                            val pagerState = rememberPagerState { uiState.images.size }
                            Box(
                                modifier = Modifier
                                    .height(PostDetailConstants.IMAGE_HEIGHT_DP.dp)
                                    .clickable {
                                        selectedImageIndex = pagerState.currentPage
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
                                            .padding(bottom = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        repeat(pagerState.pageCount) { iteration ->
                                            val color = if (pagerState.currentPage == iteration) 
                                                MaterialTheme.colorScheme.primary 
                                            else 
                                                Color.LightGray.copy(alpha = 0.5f)
                                            Box(
                                                modifier = Modifier
                                                    .size(PostDetailConstants.IMAGE_INDICATOR_SIZE_DP.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
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
                                    .height(PostDetailConstants.IMAGE_HEIGHT_DP.dp)
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

                    // Restaurant Info Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Store,
                                        contentDescription = "Thông tin nhà hàng",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
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
                                Text(
                                    text = post.restaurantName.ifEmpty { "Không có tên nhà hàng" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = "Địa chỉ",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        post.restaurantAddress.ifEmpty { "Không có địa chỉ" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Post Content Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = "Đánh giá của bạn",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp)
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
                                Text(
                                    text = post.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Đánh giá",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFFFFC107)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "%.1f / 5.0".format(post.rating),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (post.pricePerPerson > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.AccountBalanceWallet,
                                                contentDescription = "Price",
                                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = formatPrice(post.pricePerPerson) + " / người",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = post.content,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // Like button with animation
                                    val interactionSource = remember { MutableInteractionSource() }
                                    val isPressed by interactionSource.collectIsPressedAsState()

                                    LaunchedEffect(isPressed) {
                                        if (isPressed) {
                                            isLikedAnimating = true
                                            delay(200) // Short delay for visual feedback
                                            isLikedAnimating = false
                                        }
                                    }

                                    // Animation cho like icon
                                    val scale by animateFloatAsState(
                                        targetValue = if (isLikedAnimating) 1.3f else 1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "likeScale"
                                    )

                                    IconButton(
                                        onClick = { viewModel.toggleLike() },
                                        modifier = Modifier.size(48.dp),
                                        interactionSource = interactionSource
                                    ) {
                                        Icon(
                                            imageVector = if (uiState.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = if (uiState.isLiked) "Bỏ thích" else "Thích",
                                            tint = if (uiState.isLiked || isLikedAnimating) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .graphicsLayer {
                                                    scaleX = scale
                                                    scaleY = scale
                                                }
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

                    // Tách comments gốc và replies
                    val rootComments = uiState.comments.filter { it.comment.parentId == null }
                    val repliesMap = uiState.comments
                        .filter { it.comment.parentId != null }
                        .groupBy { it.comment.parentId }

                    if (rootComments.isEmpty()) {
                        item {
                            Text(
                                "Chưa có bình luận nào. Hãy là người đầu tiên!",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        rootComments.forEach { commentWithAuthor ->
                            // Comment gốc
                            item(key = commentWithAuthor.comment.id) {
                                val isLiked = uiState.likedComments.any { it.commentId == commentWithAuthor.comment.id }
                                CommentItem(
                                    commentWithAuthor = commentWithAuthor,
                                    isLiked = isLiked,
                                    onLikeClick = { viewModel.toggleCommentLike(commentWithAuthor.comment.id) },
                                    onReplyClick = { viewModel.startReply(commentWithAuthor) },
                                    onAuthorClick = onUserProfileClick,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                            
                            // Replies cho comment này (indent thêm)
                            val replies = repliesMap[commentWithAuthor.comment.id] ?: emptyList()
                            replies.forEach { reply ->
                                item(key = reply.comment.id) {
                                    val isReplyLiked = uiState.likedComments.any { it.commentId == reply.comment.id }
                                    CommentItem(
                                        commentWithAuthor = reply,
                                        isLiked = isReplyLiked,
                                        onLikeClick = { viewModel.toggleCommentLike(reply.comment.id) },
                                        onReplyClick = { viewModel.startReply(commentWithAuthor) }, // Reply vẫn trả lời comment gốc
                                        onAuthorClick = onUserProfileClick,
                                        modifier = Modifier.padding(start = 48.dp, end = 16.dp) // Indent cho reply
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(PostDetailConstants.BOTTOM_SPACER_DP.dp))
                    }
                }
            }
        }
        
        // Fullscreen Image Gallery
        if (showFullscreenGallery && uiState.images.isNotEmpty()) {
            FullscreenImageGallery(
                images = uiState.images,
                initialIndex = selectedImageIndex,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Reply indicator
            if (uiState.replyingToComment != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Đang trả lời ${uiState.replyingToComment.author?.name ?: "người dùng"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { viewModel.cancelReply() }) {
                        Text("Hủy", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = if (uiState.replyingToComment != null) uiState.replyInput else uiState.commentInput,
                    onValueChange = { 
                        if (uiState.replyingToComment != null) {
                            viewModel.onReplyInputChange(it)
                        } else {
                            viewModel.onCommentInputChange(it)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { 
                        Text(if (uiState.replyingToComment != null) "Viết trả lời..." else "Viết bình luận...") 
                    },
                    shape = RoundedCornerShape(24.dp),
                    enabled = !uiState.isAddingComment
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    if (uiState.isAddingComment) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        val inputText = if (uiState.replyingToComment != null) uiState.replyInput else uiState.commentInput
                        IconButton(
                            onClick = { 
                                if (uiState.replyingToComment != null) {
                                    viewModel.submitReply()
                                } else {
                                    viewModel.addComment()
                                }
                            },
                            enabled = inputText.isNotBlank()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Gửi",
                                tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}
