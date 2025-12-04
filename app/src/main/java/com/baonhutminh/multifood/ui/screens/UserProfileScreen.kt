package com.baonhutminh.multifood.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.baonhutminh.multifood.ui.components.PostItemCard
import com.baonhutminh.multifood.viewmodel.UserProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onNavigateBack: () -> Unit,
    onPostClick: (String) -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.userProfile?.name ?: "Trang cá nhân") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.userProfile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Không tìm thấy người dùng")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header - Profile Info
                item {
                    ProfileHeader(
                        userProfile = uiState.userProfile!!,
                        postCount = uiState.posts.size,
                        totalLikes = uiState.userProfile!!.totalLikesReceived,
                        followerCount = uiState.userProfile!!.followerCount,
                        isFollowing = uiState.isFollowing,
                        isCurrentUser = uiState.isCurrentUser,
                        onFollowClick = { viewModel.toggleFollow() }
                    )
                }
                
                // Divider
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                }
                
                // Section title
                item {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Article,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Bài viết (${uiState.posts.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Posts
                if (uiState.posts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Chưa có bài viết nào",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(uiState.posts, key = { it.post.id }) { postWithAuthor ->
                        PostItemCard(
                            postWithAuthor = postWithAuthor,
                            isLiked = uiState.likedPostIds.contains(postWithAuthor.post.id),
                            onLikeClick = { /* Không cho like từ đây để đơn giản */ },
                            onItemClick = onPostClick,
                            images = uiState.postImages[postWithAuthor.post.id] ?: emptyList(),
                            likeCount = postWithAuthor.post.likeCount
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    userProfile: com.baonhutminh.multifood.data.model.UserProfile,
    postCount: Int,
    totalLikes: Int,
    followerCount: Int,
    isFollowing: Boolean,
    isCurrentUser: Boolean,
    onFollowClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        AsyncImage(
            model = userProfile.avatarUrl?.ifEmpty { null },
            contentDescription = "Avatar",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Name
        Text(
            text = userProfile.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        // Bio
        if (!userProfile.bio.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = userProfile.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        
        // Follow Button (chỉ hiện khi không phải current user)
        if (!isCurrentUser) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) 
                        MaterialTheme.colorScheme.surfaceVariant 
                    else 
                        MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
            ) {
                Icon(
                    imageVector = if (isFollowing) Icons.Default.Check else Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isFollowing) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFollowing) "Đang theo dõi" else "Theo dõi",
                    color = if (isFollowing) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.Article,
                value = postCount.toString(),
                label = "Bài viết"
            )
            StatItem(
                icon = Icons.Default.Favorite,
                value = totalLikes.toString(),
                label = "Lượt thích"
            )
            StatItem(
                icon = Icons.Default.Person,
                value = followerCount.coerceAtLeast(0).toString(),
                label = "Người theo dõi"
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

