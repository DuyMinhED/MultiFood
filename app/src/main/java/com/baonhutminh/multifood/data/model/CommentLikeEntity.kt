package com.baonhutminh.multifood.data.model

import androidx.room.Entity

/**
 * Bảng này chỉ lưu các bình luận mà người dùng HIỆN TẠI đã thích.
 * Dùng để kiểm tra trạng thái isLiked một cách nhanh chóng trên client.
 */
@Entity(tableName = "comment_likes", primaryKeys = ["commentId", "userId"])
data class CommentLikeEntity(
    val commentId: String,
    val userId: String // userId của người dùng hiện tại
)

