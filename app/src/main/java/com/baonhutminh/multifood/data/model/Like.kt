package com.baonhutminh.multifood.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Đại diện cho một lượt thích trong sub-collection "likes" của một bài viết.
 * Đây là một DTO (Data Transfer Object).
 */
data class Like(
    @ServerTimestamp
    val likedAt: Date? = null
)
