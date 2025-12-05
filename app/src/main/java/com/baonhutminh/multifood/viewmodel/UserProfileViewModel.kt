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
import com.baonhutminh.multifood.common.Resource
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

// Sealed class cho các sự kiện chỉ xảy ra một lần (side-effects) như Toast, Snackbar
sealed class UserProfileUiEvent {
    data class ShowError(val message: String) : UserProfileUiEvent()
    data class ShowSuccess(val message: String) : UserProfileUiEvent()
}

data class UserProfileUiState(
    val userProfile: UserProfile? = null,
    val posts: List<PostWithAuthor> = emptyList(),
    val postImages: Map<String, List<String>> = emptyMap(),
    val likedPostIds: Set<String> = emptySet(),
    val isFollowing: Boolean = false,
    val isCurrentUser: Boolean = false,
    val isLoading: Boolean = true,
    val isFollowingLoading: Boolean = false,
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
    
    // Channel để gửi các sự kiện một lần, đảm bảo mỗi sự kiện chỉ được xử lý một lần
    private val _eventChannel = Channel<UserProfileUiEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()
    
    // MutableStateFlow để cập nhật loading state cho follow action
    private val _isFollowingLoading = kotlinx.coroutines.flow.MutableStateFlow(false)
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UserProfileUiState> = combine(
        profileRepository.getUserProfileById(userId),
        postRepository.getPostsForUser(userId),
        likedPostsFlow,
        isFollowingFlow,
        _isFollowingLoading
    ) { userRes, postsRes, likedPosts, isFollowing, isFollowingLoading ->
        val posts = (postsRes as? Resource.Success)?.data ?: emptyList()
        val likedIds = likedPosts.map { it.postId }.toSet()
        
        data class IntermediateState(
            val user: UserProfile?,
            val posts: List<PostWithAuthor>,
            val likedIds: Set<String>,
            val isFollowing: Boolean,
            val isFollowingLoading: Boolean
        )
        
        IntermediateState(
            (userRes as? Resource.Success)?.data,
            posts,
            likedIds,
            isFollowing,
            isFollowingLoading
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
                    isLoading = false,
                    isFollowingLoading = state.isFollowingLoading
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
                    isLoading = false,
                    isFollowingLoading = state.isFollowingLoading
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
            // Không cho phép follow nếu đang loading
            if (_isFollowingLoading.value) return@launch
            
            val currentlyFollowing = uiState.value.isFollowing
            _isFollowingLoading.value = true
            
            try {
                val result = followRepository.toggleFollow(userId, currentlyFollowing)
                
                when (result) {
                    is Resource.Success -> {
                        // Refresh user profile để cập nhật followerCount
                        profileRepository.refreshUserProfileById(userId)
                        // Nếu đang xem profile của chính mình, refresh current user profile để cập nhật followingCount
                        if (userId == currentUserId) {
                            profileRepository.refreshUserProfile()
                        }
                        // Gửi success message
                        val message = if (currentlyFollowing) "Đã bỏ theo dõi" else "Đã theo dõi"
                        _eventChannel.send(UserProfileUiEvent.ShowSuccess(message))
                    }
                    is Resource.Error -> {
                        // Gửi error message
                        _eventChannel.send(UserProfileUiEvent.ShowError(result.message ?: "Không thể thực hiện thao tác"))
                    }
                    is Resource.Loading -> {
                        // Không cần xử lý
                    }
                }
            } catch (e: Exception) {
                _eventChannel.send(UserProfileUiEvent.ShowError("Đã xảy ra lỗi: ${e.message ?: "Không xác định"}"))
            } finally {
                _isFollowingLoading.value = false
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

