package com.baonhutminh.multifood.viewmodel

import android.net.Uri
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.local.PostImageDao
import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.data.repository.PostRepository
import com.baonhutminh.multifood.data.repository.ProfileRepository
import com.baonhutminh.multifood.data.repository.RestaurantRepository
import com.baonhutminh.multifood.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.baonhutminh.multifood.data.model.RestaurantEntity
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val profileRepository: ProfileRepository,
    private val restaurantRepository: RestaurantRepository,
    private val postImageDao: PostImageDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val editingPostId = mutableStateOf(savedStateHandle.get<String>("postId"))
    val isEditing = derivedStateOf { editingPostId.value != null }

    val title = mutableStateOf("")
    val content = mutableStateOf("")
    val rating = mutableStateOf(0f)
    val pricePerPerson = mutableStateOf("")
    val placeName = mutableStateOf("")
    val placeAddress = mutableStateOf("")
    val imageUris = mutableStateOf<List<Uri>>(emptyList())
    private var originalImageUrls = listOf<String>()
    
    // Restaurant autocomplete
    val restaurantSuggestions = mutableStateOf<List<RestaurantEntity>>(emptyList())
    val selectedRestaurant = mutableStateOf<RestaurantEntity?>(null)
    val isSearchingRestaurants = mutableStateOf(false)
    private var searchJob: Job? = null

    val isFormValid = derivedStateOf {
        placeName.value.isNotBlank() &&
        placeAddress.value.isNotBlank() &&
        title.value.isNotBlank() &&
        content.value.isNotBlank()
    }

    private val _uiState = mutableStateOf<CreatePostUiState>(CreatePostUiState.Idle)
    val uiState = _uiState

    private val _events = MutableSharedFlow<CreatePostEvent>()
    val events = _events.asSharedFlow()

    init {
        editingPostId.value?.let { postId ->
            loadPostForEditing(postId)
        }
    }

    private fun loadPostForEditing(postId: String) {
        viewModelScope.launch {
            postRepository.getPostById(postId).first().data?.let { postWithAuthor ->
                val post = postWithAuthor.post
                title.value = post.title
                content.value = post.content
                rating.value = post.rating
                pricePerPerson.value = post.pricePerPerson.toString()
                
                // Load restaurant từ restaurantId để set vào selectedRestaurant
                // Sau đó update placeName và placeAddress từ restaurant để đảm bảo đồng bộ
                if (post.restaurantId.isNotBlank()) {
                    restaurantRepository.getRestaurantById(post.restaurantId)
                        .first()
                        .data?.let { restaurant ->
                            selectedRestaurant.value = restaurant
                            // Update placeName và placeAddress từ restaurant entity để đảm bảo đồng bộ
                            placeName.value = restaurant.name
                            placeAddress.value = restaurant.address
                        } ?: run {
                            // Nếu không tìm thấy restaurant, dùng cache từ post
                            placeName.value = post.restaurantName
                            placeAddress.value = post.restaurantAddress
                        }
                } else {
                    // Nếu không có restaurantId, dùng cache từ post
                    placeName.value = post.restaurantName
                    placeAddress.value = post.restaurantAddress
                }
                
                // Load images từ PostImageDao
                originalImageUrls = postImageDao.getImagesForPost(postId)
                    .first()
                    .map { it.url }
            }
        }
    }

    fun onImageSelected(uris: List<Uri>) {
        imageUris.value = uris
    }
    
    /**
     * Tìm kiếm restaurants khi người dùng gõ tên hoặc địa chỉ
     * Sử dụng debounce để tránh query quá nhiều
     */
    fun onPlaceNameChanged(newValue: String) {
        placeName.value = newValue
        selectedRestaurant.value = null // Reset selection khi người dùng thay đổi
        searchRestaurantsDebounced()
    }
    
    fun onPlaceAddressChanged(newValue: String) {
        placeAddress.value = newValue
        selectedRestaurant.value = null // Reset selection khi người dùng thay đổi
        searchRestaurantsDebounced()
    }
    
    /**
     * Tìm kiếm restaurants với debounce (500ms)
     */
    private fun searchRestaurantsDebounced() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Debounce 500ms
            searchRestaurants()
        }
    }
    
    /**
     * Tìm kiếm restaurants trong Room và Firestore
     */
    private suspend fun searchRestaurants() {
        val nameQuery = placeName.value.trim()
        val addressQuery = placeAddress.value.trim()
        
        // Nếu cả hai đều rỗng, không tìm kiếm
        if (nameQuery.isEmpty() && addressQuery.isEmpty()) {
            restaurantSuggestions.value = emptyList()
            isSearchingRestaurants.value = false
            return
        }
        
        isSearchingRestaurants.value = true
        
        try {
            // Tìm trong Room trước (nhanh)
            val roomResults = restaurantRepository.searchRestaurants(
                if (nameQuery.isNotEmpty()) nameQuery else addressQuery
            ).first().data ?: emptyList()
            
            // Nếu có kết quả từ Room, hiển thị ngay
            if (roomResults.isNotEmpty()) {
                restaurantSuggestions.value = roomResults.take(10) // Giới hạn 10 kết quả
            }
            
            // Tìm trong Firestore (chậm hơn nhưng đầy đủ hơn)
            val firestoreResults = restaurantRepository.searchRestaurantsInFirestore(
                name = nameQuery,
                address = addressQuery
            )
            
            when (firestoreResults) {
                is Resource.Success -> {
                    val firestoreData = firestoreResults.data ?: emptyList()
                    val allResults = (roomResults + firestoreData).distinctBy { restaurant: RestaurantEntity -> restaurant.id }
                    restaurantSuggestions.value = allResults.take(10)
                }
                else -> {
                    // Nếu lỗi Firestore, vẫn dùng kết quả từ Room
                    if (restaurantSuggestions.value.isEmpty()) {
                        restaurantSuggestions.value = roomResults.take(10)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore errors, giữ suggestions hiện tại
        } finally {
            isSearchingRestaurants.value = false
        }
    }
    
    /**
     * Chọn restaurant từ suggestions
     */
    fun selectRestaurant(restaurant: RestaurantEntity) {
        selectedRestaurant.value = restaurant
        placeName.value = restaurant.name
        placeAddress.value = restaurant.address
        restaurantSuggestions.value = emptyList() // Clear suggestions
    }
    
    /**
     * Clear restaurant selection
     */
    fun clearRestaurantSelection() {
        selectedRestaurant.value = null
    }

    fun submitPost() {
        if (!isFormValid.value) {
            _uiState.value = CreatePostUiState.Error("Vui lòng điền đầy đủ các trường bắt buộc.")
            return
        }

        if (isEditing.value) {
            updateExistingPost()
        } else {
            createNewPost()
        }
    }

    private fun createNewPost() {
        viewModelScope.launch {
            _uiState.value = CreatePostUiState.Loading

            val user = profileRepository.getUserProfile().first().data
            if (user == null) {
                _uiState.value = CreatePostUiState.Error("Không thể lấy thông tin người dùng.")
                return@launch
            }

            val imageUrls = uploadImages()
            if (imageUrls == null) return@launch

            // Tìm hoặc tạo restaurant
            // Nếu đã chọn restaurant, dùng ID của nó
            val restaurantId = if (selectedRestaurant.value != null) {
                selectedRestaurant.value!!.id
            } else {
                // Nếu chưa chọn, tìm hoặc tạo mới
                val restaurantResult = restaurantRepository.findOrCreateRestaurant(
                    name = placeName.value,
                    address = placeAddress.value
                )
                
                if (restaurantResult is com.baonhutminh.multifood.util.Resource.Error) {
                    _uiState.value = CreatePostUiState.Error(restaurantResult.message ?: "Lỗi tìm hoặc tạo nhà hàng")
                    return@launch
                }
                
                (restaurantResult as com.baonhutminh.multifood.util.Resource.Success<String>).data ?: ""
            }
            
            if (restaurantId.isBlank()) {
                _uiState.value = CreatePostUiState.Error("Không thể tạo hoặc tìm nhà hàng")
                return@launch
            }

            val post = Post(
                title = title.value,
                content = content.value,
                rating = rating.value,
                pricePerPerson = pricePerPerson.value.toIntOrNull() ?: 0,
                restaurantId = restaurantId,
                createdAt = Date(),
                updatedAt = Date()
            )

            val postImages = imageUrls.mapIndexed { index, url ->
                com.baonhutminh.multifood.data.model.PostImage(
                    url = url,
                    order = index
                )
            }

            when (postRepository.createPost(post, postImages)) {
                is Resource.Success -> {
                    postRepository.refreshAllPosts()
                    _events.emit(CreatePostEvent.NavigateBack)
                }
                is Resource.Error -> _uiState.value = CreatePostUiState.Error("Lỗi không xác định")
                else -> {}
            }
        }
    }

    private fun updateExistingPost() {
        viewModelScope.launch {
            _uiState.value = CreatePostUiState.Loading

            val user = profileRepository.getUserProfile().first().data
            if (user == null) {
                _uiState.value = CreatePostUiState.Error("Không thể lấy thông tin người dùng.")
                return@launch
            }

            val imageUrls = uploadImages()
            if (imageUrls == null) return@launch

            // Tìm hoặc tạo restaurant
            // Nếu đã chọn restaurant, dùng ID của nó
            val restaurantId = if (selectedRestaurant.value != null) {
                selectedRestaurant.value!!.id
            } else {
                // Nếu chưa chọn, tìm hoặc tạo mới
                val restaurantResult = restaurantRepository.findOrCreateRestaurant(
                    name = placeName.value,
                    address = placeAddress.value
                )
                
                if (restaurantResult is com.baonhutminh.multifood.util.Resource.Error) {
                    _uiState.value = CreatePostUiState.Error(restaurantResult.message ?: "Lỗi tìm hoặc tạo nhà hàng")
                    return@launch
                }
                
                (restaurantResult as com.baonhutminh.multifood.util.Resource.Success<String>).data ?: ""
            }
            
            if (restaurantId.isBlank()) {
                _uiState.value = CreatePostUiState.Error("Không thể tạo hoặc tìm nhà hàng")
                return@launch
            }

            val updatedPost = Post(
                id = editingPostId.value!!,
                userId = user.id,
                title = title.value,
                content = content.value,
                rating = rating.value,
                pricePerPerson = pricePerPerson.value.toIntOrNull() ?: 0,
                restaurantId = restaurantId,
                updatedAt = Date()
                // Các trường khác sẽ được giữ nguyên nhờ SetOptions.merge()
            )

            when (postRepository.updatePost(updatedPost)) {
                is Resource.Success -> {
                    postRepository.refreshAllPosts()
                    _events.emit(CreatePostEvent.NavigateBack)
                }
                is Resource.Error -> _uiState.value = CreatePostUiState.Error("Lỗi không xác định")
                else -> {}
            }
        }
    }

    private suspend fun uploadImages(): List<String>? {
        val uploadedUrls = mutableListOf<String>()
        for (uri in imageUris.value) {
            when (val result = postRepository.uploadPostImage(uri)) {
                is Resource.Success -> uploadedUrls.add(result.data!!)
                is Resource.Error -> {
                    _uiState.value = CreatePostUiState.Error("Lỗi tải ảnh lên: ${result.message}")
                    return null
                }
                else -> {}
            }
        }
        return uploadedUrls
    }

    fun deletePost() {
        viewModelScope.launch {
            val postId = editingPostId.value ?: return@launch

            _uiState.value = CreatePostUiState.Loading // Hiển thị loading

            val result = postRepository.deletePost(postId)
            if (result is Resource.Success) {
                postRepository.refreshAllPosts()
                profileRepository.refreshUserProfile()
                _events.emit(CreatePostEvent.NavigateBack)
            } else {
                _uiState.value = CreatePostUiState.Error(result.message ?: "Lỗi xóa bài viết")
            }
        }
    }
}

sealed class CreatePostUiState {
    object Idle : CreatePostUiState()
    object Loading : CreatePostUiState()
    object Success : CreatePostUiState()
    data class Error(val message: String) : CreatePostUiState()
}

sealed class CreatePostEvent {
    object NavigateBack : CreatePostEvent()
}
