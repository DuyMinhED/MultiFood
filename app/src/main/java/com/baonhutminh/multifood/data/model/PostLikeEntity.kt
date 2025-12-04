package com.baonhutminh.multifood.data.model

import androidx.room.Entity

/**
 * Bảng này chỉ lưu các bài viết mà người dùng HIỆN TẠI đã thích.
 * Dùng để kiểm tra trạng thái isLiked một cách nhanh chóng trên client.
 */
@Entity(tableName = "post_likes", primaryKeys = ["postId", "userId"])
data class PostLikeEntity(
    val postId: String,
    val userId: String // userId của người dùng hiện tại
)
