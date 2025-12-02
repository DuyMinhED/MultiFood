package com.baonhutminh.multifood.data.repository

import android.util.Log
import com.baonhutminh.multifood.data.local.CommentDao
import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.util.Resource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val commentDao: CommentDao // <-- Thêm DAO
) : CommentRepository {

    private val commentCollection = firestore.collection("comments")
    private val postCollection = firestore.collection("posts")

    // Đọc từ Room thay vì Firestore trực tiếp
    override fun getCommentsForPost(postId: String): Flow<Resource<List<Comment>>> {
        return commentDao.getCommentsForPost(postId).map { Resource.Success(it) }
    }

    // Thêm hàm refresh riêng cho comments
    override suspend fun refreshCommentsForPost(postId: String): Resource<Unit> {
        return try {
            val snapshot = commentCollection.whereEqualTo("reviewId", postId)
                .orderBy("createdAt", Query.Direction.ASCENDING).get().await()
            val comments = snapshot.toObjects(Comment::class.java)
            commentDao.upsertAll(comments)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("CommentRepositoryImpl", "Error refreshing comments", e)
            Resource.Error(e.message ?: "Lỗi làm mới bình luận")
        }
    }

    // Giữ nguyên logic Transaction
    override suspend fun createComment(comment: Comment, authorId: String): Resource<Unit> {
        return try {
            firestore.runTransaction {
                transaction ->
                val newCommentRef = commentCollection.document()
                val postRef = postCollection.document(comment.reviewId)

                transaction.get(postRef) // Đọc trước khi ghi

                val newComment = comment.copy(id = newCommentRef.id, userId = authorId)

                transaction.set(newCommentRef, newComment)
                transaction.update(postRef, "commentCount", FieldValue.increment(1))
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("CommentRepositoryImpl", "Error creating comment", e)
            Resource.Error(e.message ?: "Lỗi tạo bình luận")
        }
    }
}
