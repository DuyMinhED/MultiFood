package com.baonhutminh.multifood.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.PostEntity
import com.baonhutminh.multifood.data.model.UserProfile
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
    val posts: List<PostEntity> = emptyList(),
    val userProfile: UserProfile? = null,
    val selectedTab: PostFilterTab = PostFilterTab.ALL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(PostFilterTab.ALL)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    // Lấy danh sách bài đăng dựa trên tab được chọn
    private val postsFlow = _selectedTab.flatMapLatest { tab ->
        when (tab) {
            PostFilterTab.ALL -> postRepository.getAllPosts()
            PostFilterTab.MY_POSTS -> {
                profileRepository.getUserProfile().first().data?.id?.let {
                    postRepository.getPostsForUser(it)
                } ?: flowOf(Resource.Success(emptyList()))
            }
            PostFilterTab.LIKED -> {
                profileRepository.getUserProfile().first().data?.likedPostIds?.let {
                    postRepository.getLikedPosts(it)
                } ?: flowOf(Resource.Success(emptyList()))
            }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        postsFlow,
        profileRepository.getUserProfile(),
        _selectedTab,
        _isLoading,
        _errorMessage
    ) { postsRes, profileRes, tab, loading, error ->
        HomeUiState(
            posts = (postsRes as? Resource.Success)?.data ?: emptyList(),
            userProfile = (profileRes as? Resource.Success)?.data,
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
        refreshPosts(isInitialLoad = true)
    }

    fun onTabSelected(tab: PostFilterTab) {
        if (_selectedTab.value != tab) {
            _selectedTab.value = tab
        }
    }

    fun refreshPosts(isInitialLoad: Boolean = false) {
        viewModelScope.launch {
            if (isInitialLoad) _isLoading.value = true

            // Refresh cả 2 cùng lúc
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
            // Optimistic update: Cập nhật UI ngay lập tức (nếu cần)
            // Ở đây ta chỉ cần gọi đến repo, vì Flow sẽ tự động cập nhật lại
            profileRepository.toggleLike(postId)
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
