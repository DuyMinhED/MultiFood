package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.util.Resource
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    // Đọc từ Room
    fun getCommentsForPost(postId: String): Flow<Resource<List<Comment>>>

    // Tải dữ liệu mới từ Firestore
    suspend fun refreshCommentsForPost(postId: String): Resource<Unit>

    // Tạo bình luận mới
    suspend fun createComment(comment: Comment, authorId: String): Resource<Unit>
}
