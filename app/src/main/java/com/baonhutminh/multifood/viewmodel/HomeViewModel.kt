package com.baonhutminh.multifood.viewmodel

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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PostFilterTab(val title: String) {
    ALL("Tất cả"),
    MY_POSTS("Của tôi"),
    LIKED("Đã thích")
}

data class HomeUiState(
    val posts: List<PostWithAuthor> = emptyList(),
    val userProfile: UserProfile? = null,
    val likedPosts: List<PostLikeEntity> = emptyList(),
    val postImages: Map<String, List<String>> = emptyMap(), // Map postId -> List of image URLs
    val selectedTab: PostFilterTab = PostFilterTab.ALL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
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

    private val postsFlow = _selectedTab.flatMapLatest { tab ->
        when (tab) {
            PostFilterTab.ALL -> postRepository.getAllPosts()
            PostFilterTab.MY_POSTS -> {
                profileRepository.getUserProfile().first().data?.id?.let {
                    postRepository.getPostsForUser(it)
                } ?: flowOf(Resource.Success(emptyList()))
            }
            PostFilterTab.LIKED -> {
                profileRepository.getLikedPostsForCurrentUser().first().map { it.postId }?.let {
                    postRepository.getLikedPosts(it)
                } ?: flowOf(Resource.Success(emptyList()))
            }
        }
    }

    // Flow để load images cho tất cả posts
    private val postImagesFlow = postsFlow.flatMapLatest { postsRes ->
        val posts = (postsRes as? Resource.Success<List<PostWithAuthor>>)?.data ?: emptyList()
        if (posts.isEmpty()) {
            flowOf(emptyMap<String, List<String>>())
        } else {
            // Load images cho tất cả posts
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

    val uiState: StateFlow<HomeUiState> = combine(
        postsFlow,
        profileRepository.getUserProfile(),
        profileRepository.getLikedPostsForCurrentUser(),
        postImagesFlow,
        _selectedTab,
        _isLoading,
        _errorMessage
    ) { values ->
        val postsRes = values[0] as Resource<List<PostWithAuthor>>
        val profileRes = values[1] as Resource<UserProfile?>
        val likedPosts = values[2] as List<PostLikeEntity>
        val postImages = values[3] as Map<String, List<String>>
        val tab = values[4] as PostFilterTab
        val loading = values[5] as Boolean
        val error = values[6] as String?
        
        HomeUiState(
            posts = (postsRes as? Resource.Success<List<PostWithAuthor>>)?.data ?: emptyList(),
            userProfile = (profileRes as? Resource.Success<UserProfile?>)?.data,
            likedPosts = likedPosts,
            postImages = postImages,
            selectedTab = tab,
            isLoading = loading,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        viewModelScope.launch {
            val initialPostsResult = postsFlow.first()

            if ((initialPostsResult as? Resource.Success)?.data.isNullOrEmpty()) {
                refreshPosts(isInitialLoad = true)
            }
        }
    }

    fun onTabSelected(tab: PostFilterTab) {
        if (_selectedTab.value != tab) {
            _selectedTab.value = tab
        }
    }

    fun refreshPosts(isInitialLoad: Boolean = false) {
        viewModelScope.launch {
            if (isInitialLoad) _isLoading.value = true

            profileRepository.refreshUserProfile()
            val result = postRepository.refreshAllPosts()

            if (result is Resource.Error) {
                _errorMessage.value = result.message ?: "Không thể làm mới danh sách bài đăng"
            }
            if (isInitialLoad) _isLoading.value = false
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val isLiked = uiState.value.likedPosts.any { it.postId == postId }
            val result = profileRepository.toggleLike(postId, isLiked)
            // Refresh post để cập nhật likeCount từ Firestore (Cloud Functions đã cập nhật)
            if (result is Resource.Success) {
                postRepository.refreshAllPosts()
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
