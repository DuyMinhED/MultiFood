package com.baonhutminh.multifood.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.data.model.User
import com.baonhutminh.multifood.data.repository.PostRepository
import com.baonhutminh.multifood.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Định nghĩa các loại bộ lọc
enum class HomeFilter(val title: String) {
    ALL("Tất cả"),
    MY_POSTS("Của bạn"),
    SAVED("Đã lưu")
}

// State quản lý toàn bộ dữ liệu màn hình Home
data class HomeState(
    val isLoading: Boolean = false,
    val reviews: List<Post> = emptyList(),          // Danh sách gốc
    val filteredReviews: List<Post> = emptyList(),  // Danh sách đang hiển thị
    val currentFilter: HomeFilter = HomeFilter.ALL,   // Tab đang chọn
    val likedReviewIds: Set<String> = emptySet(),     // Danh sách ID các bài đã like (để tô đỏ tim)
    val error: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        loadReviews()
        fetchUserData()
    }

    // 1. Tải danh sách bài viết (public để có thể gọi từ bên ngoài)
    fun loadReviews() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val result = postRepository.getAllPosts()) {
                is Resource.Success -> {
                    val allReviews = result.data ?: emptyList()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        reviews = allReviews,
                        filteredReviews = allReviews // Mặc định hiển thị tất cả
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message ?: "Lỗi tải dữ liệu"
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    // 2. Tải thông tin User (để biết đã like bài nào)
    private fun fetchUserData() {
        if (currentUserId.isEmpty()) return

        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users").document(currentUserId).get().await()
                val user = snapshot.toObject(User::class.java)

                // Lấy danh sách ID đã like đưa vào Set để tra cứu cho nhanh
                val likedIds = user?.likedReviewIds?.toSet() ?: emptySet()
                _state.value = _state.value.copy(likedReviewIds = likedIds)
            } catch (e: Exception) {
                // Log lỗi nếu cần
            }
        }
    }

    // 3. Xử lý sự kiện bấm Like (Optimistic Update)
    fun onToggleLike(reviewId: String) {
        if (currentUserId.isEmpty()) return // Chưa đăng nhập thì bỏ qua

        val isLiked = _state.value.likedReviewIds.contains(reviewId)

        // A. Cập nhật UI ngay lập tức (không chờ mạng)
        val newLikedIds = if (isLiked) {
            _state.value.likedReviewIds - reviewId // Xóa khỏi set
        } else {
            _state.value.likedReviewIds + reviewId // Thêm vào set
        }
        _state.value = _state.value.copy(likedReviewIds = newLikedIds)

        // B. Gọi xuống Server để lưu
        viewModelScope.launch {
            val result = postRepository.toggleLikePost(reviewId, currentUserId, isLiked)
            if (result is Resource.Error) {
                // Nếu lỗi server -> Hoàn tác lại UI (Revert)
                val revertedIds = if (isLiked) newLikedIds + reviewId else newLikedIds - reviewId
                _state.value = _state.value.copy(likedReviewIds = revertedIds)
            }
        }
    }

    // 4. Xử lý chuyển Tab lọc
    fun onFilterChange(filter: HomeFilter) {
        val allReviews = _state.value.reviews

        val newFilteredList = when (filter) {
            HomeFilter.ALL -> allReviews

            HomeFilter.MY_POSTS -> allReviews.filter { it.userId == currentUserId }

            HomeFilter.SAVED -> {
                // Lọc các bài đã like
                allReviews.filter { _state.value.likedReviewIds.contains(it.id) }
            }
        }

        _state.value = _state.value.copy(
            currentFilter = filter,
            filteredReviews = newFilteredList
        )
    }
}