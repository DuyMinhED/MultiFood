package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.util.Resource

interface CommentRepository {
    suspend fun getCommentsByReview(reviewId: String): Resource<List<Comment>>
    suspend fun createComment(comment: Comment): Resource<String>
    suspend fun deleteComment(commentId: String): Resource<Unit>
    suspend fun toggleLikeComment(commentId: String, userId: String, isCurrentlyLiked: Boolean): Resource<Boolean>
}


