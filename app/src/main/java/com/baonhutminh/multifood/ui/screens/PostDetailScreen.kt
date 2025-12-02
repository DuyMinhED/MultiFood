package com.baonhutminh.multifood.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.baonhutminh.multifood.ui.components.CommentItem
import com.baonhutminh.multifood.viewmodel.PostDetailEvent
import com.baonhutminh.multifood.viewmodel.PostDetailViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PostDetailScreen(
    viewModel: PostDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val post = uiState.post
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Lắng nghe các sự kiện từ ViewModel
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PostDetailEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    // Hiển thị Snackbar khi có lỗi
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = post?.placeName ?: "Chi tiết") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    // Chỉ hiển thị nút xóa nếu là chủ bài viết
                    if (uiState.post?.userId == uiState.currentUser?.id) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Xóa bài viết")
                        }
                    }
                }
            )
        },
        bottomBar = {
            CommentInputField(uiState, viewModel)
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (post != null) {
                LazyColumn(contentPadding = it) {
                    // ... (Nội dung màn hình giữ nguyên)
                }
            }
            // Loading overlay khi đang xóa
            if (uiState.isDeleting) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    // Hộp thoại xác nhận xóa
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa bài viết này không? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePost()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun CommentInputField(uiState: com.baonhutminh.multifood.viewmodel.PostDetailUiState, viewModel: PostDetailViewModel) {
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
                enabled = !uiState.isAddingComment // <-- Vô hiệu hóa khi đang gửi
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                if (uiState.isAddingComment) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp)) // <-- Hiển thị loading
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
