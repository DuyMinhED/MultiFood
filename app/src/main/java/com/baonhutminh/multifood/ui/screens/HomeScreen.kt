package com.baonhutminh.multifood.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baonhutminh.multifood.ui.components.AppBottomBar
import com.baonhutminh.multifood.ui.components.AppTopBar
import com.baonhutminh.multifood.ui.components.PostItemCard
import com.baonhutminh.multifood.ui.navigation.Screen
import com.baonhutminh.multifood.viewmodel.HomeViewModel
import com.baonhutminh.multifood.viewmodel.PostFilterTab

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onDetailClick: (String) -> Unit,
    onAccountClick: () -> Unit,
    onCreateClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    val showFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex < 3
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = onCreateClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Tạo bài viết"
                    )
                }
            }
        },
        topBar = {
            AppTopBar(
                screen = Screen.Home,
                showSearch = true,
                onSearchClick = onSearchClick
            )
        },
        bottomBar = {
            AppBottomBar(
                currentScreen = Screen.Home,
                onNavigate = { screen ->
                    if (screen.route == Screen.Profile.route) {
                        onAccountClick()
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                val tabs = PostFilterTab.entries.toTypedArray()
                TabRow(
                    selectedTabIndex = tabs.indexOf(uiState.selectedTab),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[tabs.indexOf(uiState.selectedTab)]),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    tabs.forEach { tab ->
                        Tab(
                            selected = tab == uiState.selectedTab,
                            onClick = { viewModel.onTabSelected(tab) },
                            text = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (tab == uiState.selectedTab)
                                        FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    uiState.isLoading -> {
                        LoadingState(modifier = Modifier.align(Alignment.Center))
                    }
                    uiState.errorMessage != null -> {
                        ErrorState(
                            message = uiState.errorMessage!!,
                            onRetry = { viewModel.onTabSelected(uiState.selectedTab) },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.posts.isEmpty() -> {
                        EmptyState(
                            tab = uiState.selectedTab,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = 88.dp // Space for FAB
                            ),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = uiState.posts,
                                key = { it.post.id }
                            ) { postWithAuthor ->
                                val isLiked = uiState.userProfile?.likedPostIds?.contains(postWithAuthor.post.id) == true
                                PostItemCard(
                                    postWithAuthor = postWithAuthor,
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
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Đang tải...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Đã xảy ra lỗi",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Thử lại")
        }
    }
}

@Composable
private fun EmptyState(
    tab: PostFilterTab,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📭",
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (tab) {
                PostFilterTab.ALL -> "Chưa có bài viết nào"
                PostFilterTab.MY_POSTS -> "Bạn chưa có bài viết nào"
                PostFilterTab.LIKED -> "Bạn chưa thích bài viết nào"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (tab) {
                PostFilterTab.ALL -> "Hãy là người đầu tiên chia sẻ trải nghiệm!"
                PostFilterTab.MY_POSTS -> "Tìm và theo dõi những người bạn quan tâm"
                PostFilterTab.LIKED -> "Khám phá và thích những bài viết hay"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
