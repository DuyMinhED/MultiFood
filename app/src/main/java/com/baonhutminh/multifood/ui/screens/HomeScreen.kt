package com.baonhutminh.multifood.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baonhutminh.multifood.ui.components.AppBottomBar
import com.baonhutminh.multifood.ui.components.Header
import com.baonhutminh.multifood.ui.components.PostItemCard
import com.baonhutminh.multifood.ui.navigation.Screen
import com.baonhutminh.multifood.viewmodel.HomeFilter
import com.baonhutminh.multifood.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onDetailClick: (String) -> Unit,
    onAccountClick: () -> Unit,
    onCreateClick: () -> Unit
) {
    val state = viewModel.state.value

    // Reload khi screen được focus (khi quay lại từ CreateReview)
    LaunchedEffect(Unit) {
        viewModel.loadReviews()
    }

    Scaffold(
        // Lấy màu nền từ Theme (Xám nhạt #FAFAFA)
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Tạo bài viết", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. Header
                Header(Screen.Home)

                // 2. Bộ lọc (Tabs)
                HomeFilterSection(
                    currentFilter = state.currentFilter,
                    onFilterSelected = { filter -> viewModel.onFilterChange(filter) }
                )
            }
        },
        bottomBar = {
            // 4. Thanh điều hướng
            AppBottomBar(
                onHomeClick = {},
                onAccountClick = onAccountClick
            )
        }
    ) { paddingValues ->
        // 3. Nội dung chính (Feed)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (state.error.isNotBlank()) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.filteredReviews) { review ->
                        // Check xem bài này user đã like chưa để tô đỏ
                        val isLiked = state.likedReviewIds.contains(review.id)

                        PostItemCard(
                            post = review,
                            isLiked = isLiked,
                            onLikeClick = {},
                            onItemClick = {}
                        )
                    }
                }
            }
        }
    }
}




@Composable
fun HomeFilterSection(
    currentFilter: HomeFilter,
    onFilterSelected: (HomeFilter) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    TabRow(
        selectedTabIndex = currentFilter.ordinal,
        containerColor = surfaceColor,
        contentColor = primaryColor,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[currentFilter.ordinal]),
                color = primaryColor,
                height = 3.dp
            )
        },
        divider = {
            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        }
    ) {
        HomeFilter.entries.forEach { filter ->
            val isSelected = currentFilter == filter
            Tab(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                text = {
                    Text(
                        text = filter.title,
                        style = MaterialTheme.typography.titleMedium, // Font Medium 16sp
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                selectedContentColor = primaryColor, // Màu Cam khi chọn
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant // Màu xám khi chưa chọn
            )
        }
    }
}

