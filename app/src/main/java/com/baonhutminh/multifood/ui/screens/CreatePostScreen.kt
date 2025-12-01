package com.baonhutminh.multifood.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.baonhutminh.multifood.viewmodel.CreatePostEvent
import com.baonhutminh.multifood.viewmodel.CreatePostUiState
import com.baonhutminh.multifood.viewmodel.CreatePostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    viewModel: CreatePostViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.events.collect {
            if (it is CreatePostEvent.NavigateBack) {
                onNavigateBack()
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> -> viewModel.onImageSelected(uris) }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo bài đăng mới") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.submitPost() },
                        enabled = uiState !is CreatePostUiState.Loading
                    ) {
                        Text(
                            "ĐĂNG",
                            fontWeight = FontWeight.Bold,
                            color = if (uiState is CreatePostUiState.Loading)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Error message at top
                if (uiState is CreatePostUiState.Error) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = (uiState as CreatePostUiState.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Place Information Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Thông tin địa điểm",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = viewModel.placeName.value,
                            onValueChange = { viewModel.placeName.value = it },
                            label = { Text("Tên nhà hàng / quán ăn *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = viewModel.placeAddress.value,
                            onValueChange = { viewModel.placeAddress.value = it },
                            label = { Text("Địa chỉ *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = viewModel.pricePerPerson.value,
                            onValueChange = {
                                // Only allow numbers
                                val filtered = it.filter { char -> char.isDigit() }
                                viewModel.pricePerPerson.value = filtered
                            },
                            label = { Text("Chi phí TB/người (VNĐ)") },
                            placeholder = { Text("Ví dụ: 50000") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            suffix = {
                                if (viewModel.pricePerPerson.value.isNotEmpty()) {
                                    Text("đ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        )
                    }
                }

                // Review Content Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Đánh giá của bạn",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = viewModel.title.value,
                            onValueChange = { viewModel.title.value = it },
                            label = { Text("Tiêu đề *") },
                            placeholder = { Text("Món ăn ngon, không gian đẹp...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = viewModel.content.value,
                            onValueChange = { viewModel.content.value = it },
                            label = { Text("Nội dung đánh giá *") },
                            placeholder = { Text("Chia sẻ trải nghiệm của bạn...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            maxLines = 8
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Rating Section
                        Text(
                            text = "Xếp hạng",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        RatingBar(
                            rating = viewModel.rating.value,
                            onRatingChange = { viewModel.rating.value = it }
                        )
                    }
                }

                // Images Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hình ảnh",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (viewModel.imageUris.value.isNotEmpty()) {
                                Text(
                                    text = "${viewModel.imageUris.value.size} ảnh",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        ImageSelector(
                            selectedUris = viewModel.imageUris.value,
                            onAddImageClick = { imagePickerLauncher.launch("image/*") },
                            onRemoveImage = { uri ->
                                viewModel.imageUris.value = viewModel.imageUris.value - uri
                            }
                        )
                    }
                }

                // Bottom spacing
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Loading overlay
            if (uiState is CreatePostUiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(enabled = false) { },
                    contentAlignment = Alignment.Center
                ) {
                    Card {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Đang đăng bài...")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RatingBar(rating: Float, onRatingChange: (Float) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        (1..5).forEach { index ->
            IconButton(
                onClick = { onRatingChange(index.toFloat()) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (index <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Star $index",
                    tint = if (index <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (rating > 0) "${rating.toInt()}/5" else "Chưa đánh giá",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (rating > 0) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ImageSelector(
    selectedUris: List<Uri>,
    onAddImageClick: () -> Unit,
    onRemoveImage: (Uri) -> Unit
) {
    Column {
        OutlinedButton(
            onClick = onAddImageClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AddAPhoto, contentDescription = "Thêm ảnh")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Thêm hình ảnh")
        }

        if (selectedUris.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(selectedUris) { uri ->
                    Box {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                        // Remove button
                        IconButton(
                            onClick = { onRemoveImage(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(28.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    shape = MaterialTheme.shapes.small
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove image",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}