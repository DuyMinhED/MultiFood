package com.baonhutminh.multifood.data.model

data class Comment(
    val id: String = "",
    val reviewId: String = "",
    val parentCommentId: String? = null,
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val rating: Int = 0,
    val content: String = "",
    val imageUrls: List<String> = emptyList(),
    val likeCount: Int = 0,
    val flagged: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

