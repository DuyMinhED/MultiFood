package com.baonhutminh.multifood.data.repository

import android.util.Log
import com.baonhutminh.multifood.data.local.CommentDao
import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.data.model.relations.CommentWithAuthor
import com.baonhutminh.multifood.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val commentDao: CommentDao
) : CommentRepository {

    override fun getCommentsForPost(postId: String): Flow<Resource<List<CommentWithAuthor>>> {
        return commentDao.getCommentsForPost(postId).map { Resource.Success(it) }
    }

    override suspend fun refreshCommentsForPost(postId: String): Resource<Unit> {
        return try {
            val snapshot = firestore.collection("posts").document(postId).collection("comments")
                .orderBy("createdAt", Query.Direction.ASCENDING).get().await()
            val comments = snapshot.toObjects(Comment::class.java)
            commentDao.upsertAll(comments)
            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("CommentRepositoryImpl", "Error refreshing comments", e)
            Resource.Error(e.message ?: "Lỗi làm mới bình luận")
        }
    }

    override suspend fun createComment(comment: Comment, authorId: String): Resource<Unit> {
        return try {
            val newCommentRef = firestore.collection("posts").document(comment.postId).collection("comments").document()
            val newComment = comment.copy(id = newCommentRef.id, userId = authorId)
            newCommentRef.set(newComment).await()
            // Logic cập nhật commentCount sẽ do Cloud Function xử lý
            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("CommentRepositoryImpl", "Error creating comment", e)
            Resource.Error(e.message ?: "Lỗi tạo bình luận")
        }
    }
}
