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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val results: List<PostWithAuthor> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val minRating: Float = 0f,
    val priceRange: ClosedFloatingPointRange<Float> = 0f..500000f,
    val currentUser: UserProfile? = null,
    val likedPosts: List<PostLikeEntity> = emptyList(),
    val postImages: Map<String, List<String>> = emptyMap() // Map postId -> List of image URLs
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val profileRepository: ProfileRepository,
    private val postImageDao: PostImageDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _minRating = MutableStateFlow(0f)
    private val _priceRange = MutableStateFlow(0f..500000f)

    val uiState: StateFlow<SearchUiState> = combine(
        _searchQuery.debounce(500),
        _minRating,
        _priceRange,
        profileRepository.getUserProfile(),
        profileRepository.getLikedPostsForCurrentUser() // <-- Đã thêm
    ) { query, rating, price, userProfileRes, likedPosts ->
        val user = (userProfileRes as? Resource.Success)?.data
        if (query.isBlank()) {
            return@combine flowOf(SearchUiState(
                searchQuery = query,
                minRating = rating,
                priceRange = price,
                currentUser = user,
                likedPosts = likedPosts,
                postImages = emptyMap()
            ))
        }

        postRepository.searchPosts(
            query = query,
            minRating = rating,
            minPrice = price.start.toInt(),
            maxPrice = price.endInclusive.toInt()
        ).flatMapLatest { resource ->
            when (resource) {
                is Resource.Success -> {
                    val posts = resource.data ?: emptyList()
                    // Load images cho tất cả posts
                    if (posts.isEmpty()) {
                        flowOf(SearchUiState(
                            results = emptyList(),
                            searchQuery = query,
                            minRating = rating,
                            priceRange = price,
                            currentUser = user,
                            likedPosts = likedPosts,
                            postImages = emptyMap()
                        ))
                    } else {
                        combine(
                            posts.map { post ->
                                postImageDao.getImagesForPost(post.post.id)
                                    .map { images -> post.post.id to images.map { it.url } }
                            }
                        ) { imageLists ->
                            val postImagesMap = imageLists.associate { it.first to it.second }
                            SearchUiState(
                                results = posts,
                                searchQuery = query,
                                minRating = rating,
                                priceRange = price,
                                currentUser = user,
                                likedPosts = likedPosts,
                                postImages = postImagesMap
                            )
                        }
                    }
                }
                is Resource.Error -> flowOf(SearchUiState(
                    errorMessage = resource.message,
                    searchQuery = query,
                    minRating = rating,
                    priceRange = price,
                    currentUser = user,
                    likedPosts = likedPosts,
                    postImages = emptyMap()
                ))
                else -> flowOf(SearchUiState(
                    isLoading = true,
                    searchQuery = query,
                    minRating = rating,
                    priceRange = price,
                    currentUser = user,
                    likedPosts = likedPosts,
                    postImages = emptyMap()
                ))
            }
        }
    }.flatMapLatest { it }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onRatingChanged(rating: Float) {
        _minRating.value = rating
    }

    fun onPriceRangeChanged(range: ClosedFloatingPointRange<Float>) {
        _priceRange.value = range
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
}
