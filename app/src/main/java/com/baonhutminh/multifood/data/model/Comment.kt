package com.baonhutminh.multifood.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Đại diện cho một bình luận, có thể dùng cho cả Firestore DTO và Room Entity.
 */
@Entity(tableName = "comments")
@TypeConverters(Converters::class)
data class Comment(
    @PrimaryKey
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val content: String = "",
    val parentId: String? = null,
    val imageUrl: String? = null,

    val likeCount: Int = 0,

    @ServerTimestamp
    var createdAt: Date? = null,
    @ServerTimestamp
    var updatedAt: Date? = null
)
