package com.baonhutminh.multifood.viewmodel



import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.data.repository.PostRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val articleRepository:PostRepository = PostRepository()
) : ViewModel() {
    private val _articles = mutableStateOf<List<Post>>(emptyList())
    val articles: State<List<Post>> = _articles

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading



    fun getArticles() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = articleRepository.getArtilce()
                _articles.value = result ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                _articles.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}


