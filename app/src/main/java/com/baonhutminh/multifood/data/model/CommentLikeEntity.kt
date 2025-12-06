package com.baonhutminh.multifood.data.model

import androidx.room.Entity

@Entity(tableName = "comment_likes", primaryKeys = ["commentId", "userId"])
data class CommentLikeEntity(
    val commentId: String,
    val userId: String
)





