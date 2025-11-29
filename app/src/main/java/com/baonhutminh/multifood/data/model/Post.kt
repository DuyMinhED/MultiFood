package com.baonhutminh.multifood.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(
    tableName = "posts",
    foreignKeys = [
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(Converters::class)
data class Post(
    @PrimaryKey
    val id: String = "",

    // Thông tin định danh (Foreign Keys)
    val userId: String = "",

    // Metadata phục vụ UI/UX
    val title: String = "",
    val rating: Float = 0.0f,
    val content: String = "",
    val imageUrls: List<String> = emptyList(),
    val pricePerPerson: Int = 0,
    val visitTimestamp: Long = 0L,

    // --- CACHE DATA ---
    val userName: String = "",
    val userAvatarUrl: String = "",
    val placeName: String = "",
    val placeAddress: String = "",
    val placeCoverImage: String = "",

    // Thống kê nhanh
    val likeCount: Int = 0,
    val commentCount: Int = 0,

    val status: PostStatus = PostStatus.PUBLISHED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class PostStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED
}
