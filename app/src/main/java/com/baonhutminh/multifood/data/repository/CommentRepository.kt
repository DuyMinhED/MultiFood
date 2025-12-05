package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.data.model.CommentLikeEntity
import com.baonhutminh.multifood.data.model.relations.CommentWithAuthor
import com.baonhutminh.multifood.common.Resource
import kotlinx.coroutines.flow.Flow

interface CommentRepository {

    fun getCommentsForPost(postId: String): Flow<Resource<List<CommentWithAuthor>>>

    fun getRepliesForComment(commentId: String): Flow<Resource<List<CommentWithAuthor>>>

    fun getLikedCommentsForCurrentUser(): Flow<List<CommentLikeEntity>>

    suspend fun refreshCommentsForPost(postId: String): Resource<Unit>

    suspend fun createComment(comment: Comment, authorId: String): Resource<Unit>

    suspend fun replyToComment(parentComment: Comment, replyContent: String, authorId: String): Resource<Unit>

    suspend fun toggleCommentLike(commentId: String, isCurrentlyLiked: Boolean): Resource<Unit>

    suspend fun updateComment(comment: Comment): Resource<Unit>

    suspend fun deleteComment(commentId: String, postId: String): Resource<Unit>
}
