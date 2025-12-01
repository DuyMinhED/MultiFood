package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : CommentRepository {

    private val commentCollection = db.collection("comments")
    private val reviewCollection = db.collection("reviews")

    override suspend fun getCommentsByReview(reviewId: String): Resource<List<Comment>> {
        return try {
            val snapshot = commentCollection
                .whereEqualTo("reviewId", reviewId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val comments = snapshot.toObjects(Comment::class.java)
            Resource.Success(comments)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi tải bình luận")
        }
    }

    override suspend fun createComment(comment: Comment): Resource<String> {
        return try {
            val document = commentCollection.document()
            // Sửa ở đây: Sử dụng Date() thay vì System.currentTimeMillis()
            val payload = comment.copy(
                id = document.id,
                createdAt = Date(),
                updatedAt = Date()
            )
            
            // Tạo comment và update commentCount trong cùng một transaction
            db.runTransaction { transaction ->
                transaction.set(document, payload)
                
                // Tăng commentCount trong review
                val reviewRef = reviewCollection.document(comment.reviewId)
                val reviewSnap = transaction.get(reviewRef)
                val currentCount = reviewSnap.getLong("commentCount") ?: 0
                transaction.update(reviewRef, "commentCount", currentCount + 1)
            }.await()

            Resource.Success(document.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi tạo bình luận")
        }
    }

    override suspend fun deleteComment(commentId: String): Resource<Unit> {
        return try {
            // Lấy comment để biết reviewId trước khi xóa
            val commentSnap = commentCollection.document(commentId).get().await()
            val comment = commentSnap.toObject(Comment::class.java)
            
            if (comment == null) {
                return Resource.Error("Bình luận không tồn tại")
            }

            // Xóa comment và giảm commentCount trong cùng một transaction
            db.runTransaction { transaction ->
                transaction.delete(commentCollection.document(commentId))
                
                // Giảm commentCount trong review
                val reviewRef = reviewCollection.document(comment.reviewId)
                val reviewSnap = transaction.get(reviewRef)
                val currentCount = reviewSnap.getLong("commentCount") ?: 0
                if (currentCount > 0) {
                    transaction.update(reviewRef, "commentCount", currentCount - 1)
                }
            }.await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi xóa bình luận")
        }
    }

    override suspend fun toggleLikeComment(commentId: String, userId: String, isCurrentlyLiked: Boolean): Resource<Boolean> {
        return try {
            val userRef = db.collection("users").document(userId)
            val commentRef = commentCollection.document(commentId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(commentRef)
                val currentLikes = snapshot.getLong("likeCount") ?: 0

                if (isCurrentlyLiked) {
                    transaction.update(userRef, "likedCommentIds", FieldValue.arrayRemove(commentId))
                    transaction.update(commentRef, "likeCount", currentLikes - 1)
                } else {
                    transaction.update(userRef, "likedCommentIds", FieldValue.arrayUnion(commentId))
                    transaction.update(commentRef, "likeCount", currentLikes + 1)
                }
            }.await()

            Resource.Success(!isCurrentlyLiked)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi thao tác like")
        }
    }
}
