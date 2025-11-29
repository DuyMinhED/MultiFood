package com.baonhutminh.multifood.ui.screens.detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.data.model.Review
import com.baonhutminh.multifood.data.model.User
import com.baonhutminh.multifood.domain.repository.CommentRepository
import com.baonhutminh.multifood.domain.repository.ReviewRepository
import com.baonhutminh.multifood.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class DetailState(
    val isLoading: Boolean = false,
    val review: Review? = null,
    val isLiked: Boolean = false,
    val error: String = "",
    val userRating: Int = 0,
    val userComment: String = "",
    val comments: List<Comment> = emptyList(),
    val isSubmittingComment: Boolean = false
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val commentRepository: CommentRepository,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(DetailState())
    val state: State<DetailState> = _state
    private val currentUserId = firebaseAuth.currentUser?.uid ?: ""
    private var currentUser: User? = null

    init {
        loadCurrentUser()
        // Lấy ID từ navigation argument "reviewId"
        val reviewId = savedStateHandle.get<String>("reviewId")
        if (reviewId != null) {
            loadReviewDetail(reviewId)
        }
    }
    fun onRatingChange(rating: Int) {
        _state.value = _state.value.copy(userRating = rating)
    }

    fun onCommentChange(text: String) {
        _state.value = _state.value.copy(userComment = text)
    }

    fun loadReviewDetail(reviewId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = "")

            // 1. Lấy thông tin bài viết
            when (val result = reviewRepository.getReviewById(reviewId)) {
                is Resource.Success -> {
                    val review = result.data
                    // 2. Check xem user đã like chưa
                    var isLiked = false
                    if (currentUserId.isNotEmpty()) {
                        try {
                            val userSnap = firestore.collection("users").document(currentUserId).get().await()
                            val user = userSnap.toObject(User::class.java)
                            isLiked = user?.likedReviewIds?.contains(reviewId) == true
                        } catch (e: Exception) {}
                    }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        review = review,
                        isLiked = isLiked,
                        error = ""
                    )
                    // Load comments
                    loadComments(reviewId)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message ?: "Lỗi")
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun loadComments(reviewId: String) {
        viewModelScope.launch {
            when (val result = commentRepository.getCommentsByReview(reviewId)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(comments = result.data ?: emptyList())
                }
                is Resource.Error -> {
                    // Không hiển thị lỗi nếu chỉ là load comments thất bại
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun loadCurrentUser() {
        val uid = currentUserId
        if (uid.isEmpty()) return
        
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users").document(uid).get().await()
                currentUser = snapshot.toObject(User::class.java)
            } catch (e: Exception) {
                // Log error if needed
            }
        }
    }

    fun submitComment() {
        val review = _state.value.review ?: return
        val user = currentUser ?: return
        val commentText = _state.value.userComment.trim()
        
        if (commentText.isEmpty()) {
            _state.value = _state.value.copy(error = "Vui lòng nhập nội dung bình luận")
            return
        }
        
        if (currentUserId.isEmpty()) {
            _state.value = _state.value.copy(error = "Vui lòng đăng nhập để bình luận")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmittingComment = true, error = "")
            
            val newComment = Comment(
                id = "",
                reviewId = review.id,
                userId = user.id,
                userName = user.name,
                userAvatarUrl = user.avatarUrl,
                content = commentText,
                rating = _state.value.userRating
            )

            when (val result = commentRepository.createComment(newComment)) {
                is Resource.Success -> {
                    // Clear form và reload comments
                    _state.value = _state.value.copy(
                        userComment = "",
                        userRating = 0,
                        isSubmittingComment = false
                    )
                    // Reload review để update commentCount
                    review.id.let { loadReviewDetail(it) }
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isSubmittingComment = false,
                        error = result.message ?: "Lỗi gửi bình luận"
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun onToggleLike() {
        val review = _state.value.review ?: return
        if (currentUserId.isEmpty()) return
        val currentLikeStatus = _state.value.isLiked
        val currentLikeCount = review.likeCount

        // Optimistic Update
        val newLikeCount = if (currentLikeStatus) currentLikeCount - 1 else currentLikeCount + 1
        _state.value = _state.value.copy(
            isLiked = !currentLikeStatus,
            review = review.copy(likeCount = newLikeCount)
        )

        viewModelScope.launch {
            when (val result = reviewRepository.toggleLikeReview(review.id, currentUserId, currentLikeStatus)) {
                is Resource.Success -> {
                    // Success - giữ optimistic update
                }
                is Resource.Error -> {
                    // Revert optimistic update khi lỗi
                    _state.value = _state.value.copy(
                        isLiked = currentLikeStatus,
                        review = review.copy(likeCount = currentLikeCount),
                        error = result.message ?: "Lỗi thao tác like"
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }
}