package com.baonhutminh.multifood.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val currentUser: UserProfile? = null
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _minRating = MutableStateFlow(0f)
    private val _priceRange = MutableStateFlow(0f..500000f)

    val uiState: StateFlow<SearchUiState> = combine(
        _searchQuery.debounce(500),
        _minRating,
        _priceRange,
        profileRepository.getUserProfile()
    ) { query, rating, price, userProfileRes ->
        val user = (userProfileRes as? Resource.Success)?.data
        if (query.isBlank()) {
            flowOf(SearchUiState(searchQuery = query, minRating = rating, priceRange = price, currentUser = user))
        } else {
            postRepository.searchPosts(
                query = query,
                minRating = rating,
                minPrice = price.start.toInt(),
                maxPrice = price.endInclusive.toInt()
            ).map { resource ->
                when (resource) {
                    is Resource.Success -> SearchUiState(
                        results = resource.data ?: emptyList(),
                        searchQuery = query,
                        minRating = rating,
                        priceRange = price,
                        currentUser = user
                    )
                    is Resource.Error -> SearchUiState(
                        errorMessage = resource.message,
                        searchQuery = query,
                        minRating = rating,
                        priceRange = price,
                        currentUser = user
                    )
                    else -> SearchUiState(
                        isLoading = true,
                        searchQuery = query,
                        minRating = rating,
                        priceRange = price,
                        currentUser = user
                    )
                }
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
            profileRepository.toggleLike(postId)
        }
    }
}
