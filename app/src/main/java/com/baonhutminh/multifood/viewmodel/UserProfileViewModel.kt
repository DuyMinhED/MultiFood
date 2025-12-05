package com.baonhutminh.multifood.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.local.PostImageDao
import com.baonhutminh.multifood.data.model.UserProfile
import com.baonhutminh.multifood.data.model.relations.PostWithAuthor
import com.baonhutminh.multifood.data.repository.FollowRepository
import com.baonhutminh.multifood.data.repository.PostRepository
import com.baonhutminh.multifood.data.repository.ProfileRepository
import com.baonhutminh.multifood.util.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileUiState(
    val userProfile: UserProfile? = null,
    val posts: List<PostWithAuthor> = emptyList(),
    val postImages: Map<String, List<String>> = emptyMap(),
    val likedPostIds: Set<String> = emptySet(),
    val isFollowing: Boolean = false,
    val isCurrentUser: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val postRepository: PostRepository,
    private val postImageDao: PostImageDao,
    private val followRepository: FollowRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    
    private val userId: String = savedStateHandle.get<String>("userId") ?: ""
    private val currentUserId = auth.currentUser?.uid ?: ""
    
    private val likedPostsFlow = profileRepository.getLikedPostsForCurrentUser()
    private val isFollowingFlow = followRepository.isFollowing(userId)
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UserProfileUiState> = combine(
        profileRepository.getUserProfileById(userId),
        postRepository.getPostsForUser(userId),
        likedPostsFlow,
        isFollowingFlow
    ) { userRes, postsRes, likedPosts, isFollowing ->
        val posts = (postsRes as? Resource.Success)?.data ?: emptyList()
        val likedIds = likedPosts.map { it.postId }.toSet()
        
        data class IntermediateState(
            val user: UserProfile?,
            val posts: List<PostWithAuthor>,
            val likedIds: Set<String>,
            val isFollowing: Boolean
        )
        
        IntermediateState(
            (userRes as? Resource.Success)?.data,
            posts,
            likedIds,
            isFollowing
        )
    }.flatMapLatest { state ->
        if (state.posts.isEmpty()) {
            flowOf(
                UserProfileUiState(
                    userProfile = state.user,
                    posts = emptyList(),
                    postImages = emptyMap(),
                    likedPostIds = state.likedIds,
                    isFollowing = state.isFollowing,
                    isCurrentUser = userId == currentUserId,
                    isLoading = false
                )
            )
        } else {
            // Lấy images cho mỗi post
            val imageFlows = state.posts.map { postWithAuthor ->
                postImageDao.getImagesForPost(postWithAuthor.post.id)
                    .map { images -> postWithAuthor.post.id to (images?.map { it.url } ?: emptyList()) }
            }
            
            combine(imageFlows) { imagesArray ->
                UserProfileUiState(
                    userProfile = state.user,
                    posts = state.posts,
                    postImages = imagesArray.toMap(),
                    likedPostIds = state.likedIds,
                    isFollowing = state.isFollowing,
                    isCurrentUser = userId == currentUserId,
                    isLoading = false
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfileUiState()
    )
    
    fun toggleFollow() {
        viewModelScope.launch {
            val currentlyFollowing = uiState.value.isFollowing
            val result = followRepository.toggleFollow(userId, currentlyFollowing)
            
            // Refresh user profile để cập nhật followerCount
            if (result is Resource.Success) {
                profileRepository.refreshUserProfileById(userId)
                // Nếu đang xem profile của chính mình, refresh current user profile để cập nhật followingCount
                if (userId == currentUserId) {
                    profileRepository.refreshUserProfile()
                }
            }
        }
    }
    
    init {
        // Sync follow status từ Firestore khi khởi tạo
        viewModelScope.launch {
            followRepository.syncFollowsFromFirestore()
            // Refresh user profile để đảm bảo có dữ liệu mới nhất
            profileRepository.refreshUserProfileById(userId)
        }
    }
}

