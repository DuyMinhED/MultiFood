package com.baonhutminh.multifood.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "user_profiles")
@TypeConverters(Converters::class)
data class UserProfile(
    @PrimaryKey
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val bio: String = "",
    val totalPosts: Int = 0,
    val totalFavorites: Int = 0,
    val favoritePostIds: List<String> = emptyList(), // Thêm trường này
    val createdAt: Long = System.currentTimeMillis()
)