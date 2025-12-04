package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.data.model.relations.CommentWithAuthor
import com.baonhutminh.multifood.util.Resource
import kotlinx.coroutines.flow.Flow

interface CommentRepository {

    fun getCommentsForPost(postId: String): Flow<Resource<List<CommentWithAuthor>>>

    suspend fun refreshCommentsForPost(postId: String): Resource<Unit>

    suspend fun createComment(comment: Comment, authorId: String): Resource<Unit>

    suspend fun updateComment(comment: Comment): Resource<Unit>

    suspend fun deleteComment(commentId: String, postId: String): Resource<Unit>
}
