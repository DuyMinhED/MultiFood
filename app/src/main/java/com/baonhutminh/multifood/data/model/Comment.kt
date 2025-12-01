package com.baonhutminh.multifood.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["reviewId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.NO_ACTION
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
    val createdAt: Date? = null, // Sửa ở đây
    val updatedAt: Date? = null  // Sửa ở đây
)
