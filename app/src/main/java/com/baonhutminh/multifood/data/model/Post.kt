package com.baonhutminh.multifood.data.model

/**
 * Model dữ liệu bài viết trên Firestore.
 */
data class Post(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val rating: Float = 0f,
    val address: String = "",
    val imageUrls: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val userId: String = ""     // ✅ thêm dòng này
)
