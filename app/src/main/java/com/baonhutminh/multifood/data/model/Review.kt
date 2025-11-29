package com.baonhutminh.multifood.data.model

data class Review(
    val id: String = "",

    // Thông tin định danh (Foreign Keys)
    val userId: String = "",

    // Metadata phục vụ UI/UX
    val title: String = "",
    val rating: Int = 0,
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

    val status: ReviewStatus = ReviewStatus.PUBLISHED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ReviewStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED
}

