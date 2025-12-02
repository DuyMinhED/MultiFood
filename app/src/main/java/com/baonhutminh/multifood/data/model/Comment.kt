package com.baonhutminh.multifood.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "comments")
@TypeConverters(Converters::class)
data class Comment(
    @PrimaryKey
    val id: String = "",
    val reviewId: String = "",
    val parentCommentId: String? = null,
    val userId: String = "",
    // Các trường cache về tác giả đã được xóa
    val rating: Int = 0,
    val content: String = "",
    val imageUrls: List<String> = emptyList(),
    val likeCount: Int = 0,
    val flagged: Boolean = false,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)
