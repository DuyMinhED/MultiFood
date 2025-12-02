package com.baonhutminh.multifood.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Đại diện cho mô hình dữ liệu người dùng đầy đủ trên Firestore.
 * Đây là một DTO (Data Transfer Object).
 */
data class User(
    val id: String = "",
    val name: String = "",
    val email: String? = null,
    val avatarUrl: String = "",
    val bio: String = "",
    val phoneNumber: String? = null,

    val postCount: Int = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val totalLikesReceived: Int = 0, // <-- Đã thêm

    val likedPostIds: List<String> = emptyList(),
    val bookmarkedPostIds: List<String> = emptyList(),
    val recentSearchKeywords: List<String> = emptyList(),

    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)
