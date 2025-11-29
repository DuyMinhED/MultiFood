package com.baonhutminh.multifood.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = Post::class,
            parentColumns = ["id"],
            childColumns = ["reviewId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.NO_ACTION // Để tránh lỗi xóa chuỗi (CASCADE)
        )
    ]
)
@TypeConverters(Converters::class)
data class Comment(
    @PrimaryKey
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
