package com.baonhutminh.multifood.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.local.PostImageDao
import com.baonhutminh.multifood.data.model.PostLikeEntity
import com.baonhutminh.multifood.data.model.UserProfile
import com.baonhutminh.multifood.data.model.relations.PostWithAuthor
import com.baonhutminh.multifood.data.repository.PostRepository
import com.baonhutminh.multifood.data.repository.ProfileRepository
import com.baonhutminh.multifood.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HomeViewModel"
private const val LIKE_DEBOUNCE_MS = 500L // Debounce 500ms để tránh spam

enum class PostFilterTab(val title: String) {
    ALL("Tất cả"),
    MY_POSTS("Của tôi"),
    LIKED("Đã thích")
}

data class HomeUiState(
    val posts: List<PostWithAuthor> = emptyList(),
    val userProfile: UserProfile? = null,
    val likedPosts: List<PostLikeEntity> = emptyList(),
    val postImages: Map<String, List<String>> = emptyMap(),
    val selectedTab: PostFilterTab = PostFilterTab.ALL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

private data class HomeIntermediateState(
    val posts: List<PostWithAuthor>,
    val userProfile: UserProfile?,
    val likedPosts: List<PostLikeEntity>,
    val postImages: Map<String, List<String>>,
    val selectedTab: PostFilterTab
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val profileRepository: ProfileRepository,
    private val postImageDao: PostImageDao
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(PostFilterTab.ALL)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    
    // Debounce jobs cho từng post để tránh spam
    private val likeDebounceJobs = mutableMapOf<String, Job>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val postsFlow = _selectedTab.flatMapLatest { tab ->
        when (tab) {
            PostFilterTab.ALL -> postRepository.getAllPosts()
            PostFilterTab.MY_POSTS -> {
                profileRepository.getUserProfile().first().data?.id?.let {
                    postRepository.getPostsForUser(it)
                } ?: flowOf(Resource.Success(emptyList()))
            }
            PostFilterTab.LIKED -> {
                profileRepository.getLikedPostsForCurrentUser().flatMapLatest { likedPosts ->
                    if (likedPosts.isEmpty()) {
                        flowOf(Resource.Success(emptyList()))
                    } else {
                        postRepository.getLikedPosts(likedPosts.map { it.postId })
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val postImagesFlow = postsFlow.flatMapLatest { postsRes ->
        val posts = (postsRes as? Resource.Success<List<PostWithAuthor>>)?.data ?: emptyList()
        if (posts.isEmpty()) {
            flowOf(emptyMap<String, List<String>>())
        } else {
            combine(
                posts.map { post ->
                    postImageDao.getImagesForPost(post.post.id)
                        .map { images -> post.post.id to images.map { it.url } }
                }
            ) { imageLists ->
                imageLists.associate { it.first to it.second }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = combine(
        combine(
            postsFlow,
            profileRepository.getUserProfile(),
            profileRepository.getLikedPostsForCurrentUser(),
            postImagesFlow,
            _selectedTab
        ) { postsRes, profileRes, likedPosts, postImages, tab ->
            HomeIntermediateState(
                posts = (postsRes as? Resource.Success<List<PostWithAuthor>>)?.data ?: emptyList(),
                userProfile = (profileRes as? Resource.Success<UserProfile?>)?.data,
                likedPosts = likedPosts,
                postImages = postImages,
                selectedTab = tab
            )
        },
        _isLoading,
        _errorMessage
    ) { intermediateState, isLoading, errorMessage ->
        HomeUiState(
            posts = intermediateState.posts,
            userProfile = intermediateState.userProfile,
            likedPosts = intermediateState.likedPosts,
            postImages = intermediateState.postImages,
            selectedTab = intermediateState.selectedTab,
            isLoading = isLoading,
            errorMessage = errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        // Realtime sync posts
        viewModelScope.launch {
            postRepository.observePostsRealtime()
                .catch { e -> Log.e(TAG, "Realtime sync error", e) }
                .collect()
        }
        
        // Sync user profile lần đầu
        viewModelScope.launch {
            _isLoading.value = true
            profileRepository.refreshUserProfile()
            // Likes sẽ được sync tự động qua getLikedPostsForCurrentUser() flow
            _isLoading.value = false
        }
    }

    fun onTabSelected(tab: PostFilterTab) {
        if (_selectedTab.value != tab) {
            _selectedTab.value = tab
        }
    }

    fun refreshPosts() {
        viewModelScope.launch {
            _errorMessage.value = null
            _isLoading.value = true
            val result = postRepository.refreshAllPosts()
            if (result is Resource.Error) {
                _errorMessage.value = result.message ?: "Không thể làm mới danh sách bài đăng"
            }
            _isLoading.value = false
        }
    }

    fun toggleLike(postId: String) {
        // Cancel job cũ nếu đang pending
        likeDebounceJobs[postId]?.cancel()
        
        likeDebounceJobs[postId] = viewModelScope.launch {
            // Debounce - đợi user ngừng spam
            delay(LIKE_DEBOUNCE_MS)
            
            val isLiked = uiState.value.likedPosts.any { it.postId == postId }
            val result = profileRepository.toggleLike(postId, isLiked)
            if (result is Resource.Error) {
                _errorMessage.value = result.message ?: "Không thể cập nhật trạng thái yêu thích"
            }
            
            likeDebounceJobs.remove(postId)
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
