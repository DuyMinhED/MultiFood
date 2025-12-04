package com.baonhutminh.multifood.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "posts")
@TypeConverters(Converters::class)
data class PostEntity(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val restaurantId: String = "",
    val title: String = "",
    val content: String = "",
    val rating: Float = 0.0f,
    val pricePerPerson: Int = 0,
    val visitDate: Date? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val status: PostStatus = PostStatus.PUBLISHED,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    
    // Các trường cache để hiển thị nhanh mà không cần join
    val userName: String = "",
    val userAvatarUrl: String = "",
    val restaurantName: String = "",
    val restaurantAddress: String = ""
)
