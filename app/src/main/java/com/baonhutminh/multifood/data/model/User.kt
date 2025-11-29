package com.baonhutminh.multifood.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val handle: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val avatarUrl: String = "",
    val coverImageUrl: String = "",
    val bio: String = "",
    val city: String = "",

    val preferredCategories: List<String> = emptyList(),
    val likedReviewIds: List<String> = emptyList(),
    val bookmarkedReviewIds: List<String> = emptyList(),
    val draftReviewIds: List<String> = emptyList(),
    val recentSearchKeywords: List<String> = emptyList(),

    val followerCount: Int = 0,
    val followingCount: Int = 0,

    val lastActiveAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

