package com.baonhutminh.multifood.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.data.repository.PostRepository
import com.baonhutminh.multifood.data.repository.ProfileRepository
import com.baonhutminh.multifood.util.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PostFilterTab(val title: String) {
    ALL("Tất cả"),
    MY_POSTS("Của tôi"),
    LIKED("Đã thích")
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val profileRepository: ProfileRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _posts = mutableStateOf<List<Post>>(emptyList())
    val posts: State<List<Post>> = _posts

    private val _selectedTab = mutableStateOf(PostFilterTab.ALL)
    val selectedTab: State<PostFilterTab> = _selectedTab

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    init {
        observePosts()
        refreshPosts(isInitialLoad = true)
    }

    private fun observePosts() {
        viewModelScope.launch {
            snapshotFlow { _selectedTab.value }.collectLatest { tab ->
                _isLoading.value = true
                val currentUser = auth.currentUser

                val sourceFlow = when (tab) {
                    PostFilterTab.ALL -> postRepository.getAllPosts()
                    PostFilterTab.MY_POSTS -> {
                        if (currentUser != null) {
                            postRepository.getPostsForUser(currentUser.uid)
                        } else {
                            kotlinx.coroutines.flow.flowOf(Resource.Success<List<Post>>(emptyList()))
                        }
                    }
                    PostFilterTab.LIKED -> {
                        val profileResource = profileRepository.getUserProfile().first() // Lấy dữ liệu profile một lần
                        val likedIds = (profileResource as? Resource.Success)?.data?.favoritePostIds ?: emptyList()
                        postRepository.getLikedPosts(likedIds)
                    }
                }

                sourceFlow.collect { resource ->
                    if (resource is Resource.Success) {
                        _posts.value = resource.data ?: emptyList()
                    }
                    _isLoading.value = false
                }
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

            // Luôn làm mới cả profile và posts để đảm bảo dữ liệu nhất quán
            profileRepository.refreshUserProfile()
            val result = postRepository.refreshAllPosts()

            if (result is Resource.Error) {
                _errorMessage.value = result.message ?: "Không thể làm mới danh sách bài đăng"
            }
            if (isInitialLoad) _isLoading.value = false
        }
    }

}