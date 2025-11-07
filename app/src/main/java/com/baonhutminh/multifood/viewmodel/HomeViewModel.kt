package com.baonhutminh.multifood.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.data.repository.PostRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val articleRepository: PostRepository = PostRepository()
) : ViewModel() {

    private val _posts = mutableStateOf<List<Post>>(emptyList())
    val posts: State<List<Post>> = _posts

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // >>> Thêm 2 dòng dưới đây <<<
    private val _favoritePosts = mutableStateOf<Set<String>>(emptySet())
    val favoritePosts: State<Set<String>> = _favoritePosts

    fun getPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _posts.value = articleRepository.getPosts()
            } catch (_: Exception) {
                _posts.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // >>> Thêm 2 hàm dưới đây <<<
    fun toggleFavorite(post: Post) {
        val cur = _favoritePosts.value.toMutableSet()
        if (cur.contains(post.id)) cur.remove(post.id) else cur.add(post.id)
        _favoritePosts.value = cur
    }

    fun isFavorite(post: Post): Boolean = _favoritePosts.value.contains(post.id)
}
