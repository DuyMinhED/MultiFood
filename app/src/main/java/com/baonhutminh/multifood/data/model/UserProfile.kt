package com.baonhutminh.multifood.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

/**
 * Đại diện cho một tập hợp con dữ liệu người dùng được tối ưu hóa để lưu trữ cục bộ
 * và hiển thị trên giao diện người dùng. Nó khác với lớp `User` đầy đủ (DTO).
 */
@Entity(tableName = "user_profiles")
@TypeConverters(Converters::class)
data class UserProfile(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val email: String = "", // <-- Đã thêm lại
    val avatarUrl: String = "",
    val bio: String = "",

    val postCount: Int = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0,

    val likedPostIds: List<String> = emptyList()
)