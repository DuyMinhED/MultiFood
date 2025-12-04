package com.baonhutminh.multifood.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baonhutminh.multifood.ui.components.PostItemCard
import com.baonhutminh.multifood.viewmodel.SearchUiState
import com.baonhutminh.multifood.viewmodel.SearchViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onDetailClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChanged,
                            placeholder = { Text("Tìm kiếm...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Xóa")
                                    }
                                }
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Bộ lọc")
                        }
                    }
                )
                if (showFilters) {
                    FilterPanel(uiState = uiState, viewModel = viewModel)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.searchQuery.isNotBlank() && uiState.results.isEmpty() -> {
                    Text(
                        text = "Không tìm thấy kết quả nào cho \"${uiState.searchQuery}\"",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.results, key = { it.post.id }) { postWithAuthor ->
                            val isLiked = uiState.likedPosts.any { it.postId == postWithAuthor.post.id }
                            val images = uiState.postImages[postWithAuthor.post.id] ?: emptyList()
                            PostItemCard(
                                postWithAuthor = postWithAuthor,
                                images = images,
                                isLiked = isLiked,
                                onLikeClick = { viewModel.toggleLike(postWithAuthor.post.id) },
                                onItemClick = { onDetailClick(postWithAuthor.post.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterPanel(uiState: SearchUiState, viewModel: SearchViewModel) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Đánh giá từ ${uiState.minRating.toInt()} sao trở lên", style = MaterialTheme.typography.titleSmall)
        Slider(
            value = uiState.minRating,
            onValueChange = viewModel::onRatingChanged,
            valueRange = 0f..5f,
            steps = 4
        )

        Spacer(modifier = Modifier.height(8.dp))

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        Text("Giá từ ${currencyFormat.format(uiState.priceRange.start)} đến ${currencyFormat.format(uiState.priceRange.endInclusive)}", style = MaterialTheme.typography.titleSmall)
        RangeSlider(
            value = uiState.priceRange,
            onValueChange = viewModel::onPriceRangeChanged,
            valueRange = 0f..1000000f, // Max 1 triệu
            steps = 19 // (1,000,000 / 50,000) - 1
        )
    }
}
