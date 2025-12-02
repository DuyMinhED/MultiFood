package com.baonhutminh.multifood.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

/**
 * Đại diện cho một bài đăng được lưu trong cơ sở dữ liệu Room.
 * Đây là một Entity.
 */
@Entity(tableName = "posts")
@TypeConverters(Converters::class)
data class PostEntity(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val rating: Float = 0.0f,
    val content: String = "",
    val imageUrls: List<String> = emptyList(),
    val pricePerPerson: Int = 0,
    val visitTimestamp: Long = 0L,

    // Các trường cache về tác giả đã được xóa

    val placeName: String = "",
    val placeAddress: String = "",
    val placeCoverImage: String = "",

    val likeCount: Int = 0,
    val commentCount: Int = 0,

    val status: PostStatus = PostStatus.PUBLISHED,
    
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)
