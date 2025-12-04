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
    val restaurantId: String = "", // Thêm theo thiết kế mới
    val title: String = "",
    val content: String = "",
    val rating: Float = 0.0f,
    val pricePerPerson: Int = 0,
    @ServerTimestamp
    val visitDate: Date? = null, // Đổi sang Date theo thiết kế

    // Các trường đếm sẽ được cập nhật bởi Cloud Functions
    val likeCount: Int = 0,
    val commentCount: Int = 0,

    val status: PostStatus = PostStatus.PUBLISHED,

    @ServerTimestamp
    var createdAt: Date? = null,
    @ServerTimestamp
    var updatedAt: Date? = null
)

enum class PostStatus {
    DRAFT,
    PUBLISHED,
    DELETED, // Thêm trạng thái DELETED
    ARCHIVED
}
