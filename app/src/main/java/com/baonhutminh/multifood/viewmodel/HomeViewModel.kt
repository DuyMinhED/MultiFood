package com.baonhutminh.multifood.viewmodel



import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.Article
import com.baonhutminh.multifood.data.repository.ArticleRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val articleRepository:ArticleRepository = ArticleRepository()
) : ViewModel() {
    private val _articles = mutableStateOf<List<Article>>(emptyList())
    val articles: State<List<Article>> = _articles

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading



    fun getArticles() {
        viewModelScope.launch {
            _isLoading.value = true
            _articles.value = articleRepository.getArtilce()
            _isLoading.value = false
        }
    }
}


