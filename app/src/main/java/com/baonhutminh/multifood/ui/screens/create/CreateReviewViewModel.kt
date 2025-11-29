package com.baonhutminh.multifood.ui.screens.create

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baonhutminh.multifood.data.model.Review
import com.baonhutminh.multifood.data.model.ReviewStatus
import com.baonhutminh.multifood.data.model.User
import com.baonhutminh.multifood.domain.repository.ReviewRepository
import com.baonhutminh.multifood.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class CreateReviewState(
    val title: String = "",
    val content: String = "",
    val placeName: String = "",
    val placeAddress: String = "",
    val rating: Float = 4f,
    val pricePerPersonInput: String = "",
    val selectedImageUris: List<Uri> = emptyList(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val createdReviewId: String? = null
)

@HiltViewModel
class CreateReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage
) : ViewModel() {

    private val _state = mutableStateOf(CreateReviewState())
    val state: State<CreateReviewState> = _state

    private var currentUser: User? = null

    init {
        loadCurrentUser()
    }

    fun onTitleChange(value: String) = updateState { copy(title = value) }
    fun onContentChange(value: String) = updateState { copy(content = value) }
    fun onPlaceNameChange(value: String) = updateState { copy(placeName = value) }
    fun onPlaceAddressChange(value: String) = updateState { copy(placeAddress = value) }
    fun onPriceChange(value: String) = updateState { copy(pricePerPersonInput = value.filter { it.isDigit() }) }
    fun onRatingChange(value: Float) = updateState { copy(rating = value) }
    fun onImagesPicked(uris: List<Uri>) = updateState { copy(selectedImageUris = uris) }
    fun onRemovePickedImage(uri: Uri) = updateState { copy(selectedImageUris = selectedImageUris.filterNot { it == uri }) }

    fun consumeCreatedReview() = updateState { copy(createdReviewId = null) }

    fun submitReview() {
        val user = currentUser
        if (user == null) {
            updateState { copy(errorMessage = "Không tìm thấy thông tin người dùng") }
            return
        }

        val currentState = state.value
        val title = currentState.title.trim()
        val content = currentState.content.trim()
        val placeName = currentState.placeName.trim()
        val placeAddress = currentState.placeAddress.trim()

        if (title.isEmpty() || content.isEmpty() || placeName.isEmpty()) {
            updateState { copy(errorMessage = "Vui lòng nhập đầy đủ tiêu đề, nội dung và tên quán") }
            return
        }

        if (currentState.selectedImageUris.isEmpty()) {
            updateState { copy(errorMessage = "Vui lòng chọn ít nhất một ảnh từ thiết bị") }
            return
        }

        val rating = currentState.rating.toInt().coerceIn(1, 5)
        val pricePerPerson = currentState.pricePerPersonInput.toIntOrNull() ?: 0
        val imageUris = currentState.selectedImageUris

        viewModelScope.launch {
            updateState { copy(isSubmitting = true, errorMessage = null) }
            val uploadedUrls = try {
                uploadSelectedImages(imageUris, user.id)
            } catch (e: Exception) {
                updateState { copy(isSubmitting = false, errorMessage = e.message ?: "Không thể tải ảnh lên, vui lòng thử lại") }
                return@launch
            }

            val newReview = Review(
                id = "",
                userId = user.id,
                title = title,
                rating = rating,
                content = content,
                imageUrls = uploadedUrls,
                pricePerPerson = pricePerPerson,
                visitTimestamp = System.currentTimeMillis(),
                userName = user.name,
                userAvatarUrl = user.avatarUrl,
                placeName = placeName,
                placeAddress = placeAddress,
                placeCoverImage = uploadedUrls.firstOrNull().orEmpty(),
                likeCount = 0,
                commentCount = 0,
                status = ReviewStatus.PUBLISHED
            )

            when (val result = reviewRepository.createReview(newReview)) {
                is Resource.Success -> {
                    updateState {
                        CreateReviewState(createdReviewId = result.data)
                    }
                }
                is Resource.Error -> {
                    updateState { copy(isSubmitting = false, errorMessage = result.message ?: "Lỗi tạo bài viết") }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun loadCurrentUser() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users").document(uid).get().await()
                currentUser = snapshot.toObject(User::class.java)
            } catch (_: Exception) {
            }
        }
    }

    private inline fun updateState(transform: CreateReviewState.() -> CreateReviewState) {
        _state.value = _state.value.transform()
    }

    private suspend fun uploadSelectedImages(uris: List<Uri>, userId: String): List<String> {
        if (uris.isEmpty()) return emptyList()
        val downloadUrls = mutableListOf<String>()
        for ((index, uri) in uris.withIndex()) {
            val fileRef = firebaseStorage.reference
                .child("reviews/$userId/${System.currentTimeMillis()}_$index.jpg")
            fileRef.putFile(uri).await()
            val url = fileRef.downloadUrl.await().toString()
            downloadUrls.add(url)
        }
        return downloadUrls
    }
}

