package com.baonhutminh.multifood.data.model

data class UserProfile(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val bio: String = "",
    val totalPosts: Int = 0,
    val totalFavorites: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
