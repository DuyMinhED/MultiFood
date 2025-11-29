package com.baonhutminh.multifood.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Đại diện cho mô hình dữ liệu của một bài đăng trên Firestore.
 * Đây là một DTO (Data Transfer Object).
 */
data class Post(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val rating: Float = 0.0f,
    val content: String = "",
    val imageUrls: List<String> = emptyList(),
    val pricePerPerson: Int = 0,
    val visitTimestamp: Long = 0L,

    // Dữ liệu được cache lại
    val userName: String = "",
    val userAvatarUrl: String = "",
    val placeName: String = "",
    val placeAddress: String = "",
    val placeCoverImage: String = "",

    val likeCount: Int = 0,
    val commentCount: Int = 0,

    val status: PostStatus = PostStatus.PUBLISHED,

    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)

enum class PostStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED
}
