package com.baonhutminh.multifood.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.data.repository.PostRepository_Firebase
import com.baonhutminh.multifood.data.repository.UserRepository
import kotlinx.coroutines.launch

enum class PostFilterType {
    ALL, FAVORITE, MY_POSTS
}

class PostsViewModel(
    private val postRepository: PostRepository_Firebase = PostRepository_Firebase(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _posts = mutableStateOf<List<Post>>(emptyList())
    val posts: State<List<Post>> = _posts

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _filterType = mutableStateOf(PostFilterType.ALL)
    val filterType: State<PostFilterType> = _filterType

    private var allPostsCache: List<Post> = emptyList()

    init {
        observePostsRealtime()
    }

    /**
     * 🔄 Lắng nghe thay đổi realtime từ Firestore
     */
    private fun observePostsRealtime() {
        _isLoading.value = true
        postRepository.observePosts { posts ->
            viewModelScope.launch {
                val favoriteIds = userRepository.getFavoritePostIds()
                allPostsCache = posts.map { post ->
                    post.copy(isFavorite = post.id in favoriteIds)
                }
                _posts.value = filterPosts(allPostsCache, _filterType.value)
                _isLoading.value = false
            }
        }
    }

    /**
     * 🔁 Thay đổi bộ lọc (Tất cả / Đã thích / Của tôi)
     */
    fun setFilter(type: PostFilterType) {
        _filterType.value = type
        viewModelScope.launch {
            _isLoading.value = true
            _posts.value = filterPosts(allPostsCache, type)
            _isLoading.value = false
        }
    }

    /**
     * ❤️ Thích / bỏ thích bài viết (và lưu Firestore)
     */
    fun toggleFavorite(postId: String) {
        viewModelScope.launch {
            val updatedPosts = allPostsCache.map { post ->
                if (post.id == postId) {
                    val newFavorite = !post.isFavorite
                    // ✅ Ghi Firestore thật
                    userRepository.toggleFavorite(postId, newFavorite)
                    post.copy(isFavorite = newFavorite)
                } else post
            }
            allPostsCache = updatedPosts
            _posts.value = filterPosts(updatedPosts, _filterType.value)
        }
    }

    /**
     * 🔍 Bộ lọc bài viết theo loại (bất đồng bộ để lấy từ Firestore)
     */
    private suspend fun filterPosts(posts: List<Post>, type: PostFilterType): List<Post> {
        return when (type) {
            PostFilterType.ALL -> posts
            PostFilterType.FAVORITE -> userRepository.getFavoritePosts(posts)
            PostFilterType.MY_POSTS -> userRepository.getUserPosts(posts)
        }
    }

    /**
     * 📦 Lấy danh sách bài viết ban đầu (lần đầu load app)
     */
    fun getPost() {
        viewModelScope.launch {
            _isLoading.value = true
            allPostsCache = postRepository.getPosts()

            // ✅ Cập nhật trạng thái yêu thích thật từ Firestore
            val favoriteIds = userRepository.getFavoritePostIds()
            allPostsCache = allPostsCache.map { post ->
                post.copy(isFavorite = post.id in favoriteIds)
            }

            _posts.value = filterPosts(allPostsCache, _filterType.value)
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        postRepository.removeListener()
    }
}
