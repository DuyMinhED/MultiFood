package com.baonhutminh.multifood.data.repository

import android.util.Log
import com.baonhutminh.multifood.data.local.CommentDao
import com.baonhutminh.multifood.data.local.CommentLikeDao
import com.baonhutminh.multifood.data.local.UserDao
import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.data.model.CommentLikeEntity
import com.baonhutminh.multifood.data.model.User
import com.baonhutminh.multifood.data.model.UserProfile
import com.baonhutminh.multifood.data.model.relations.CommentWithAuthor
import com.baonhutminh.multifood.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.CancellationException
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val commentDao: CommentDao,
    private val commentLikeDao: CommentLikeDao,
    private val userDao: UserDao
) : CommentRepository {

    override fun getCommentsForPost(postId: String): Flow<Resource<List<CommentWithAuthor>>> {
        return commentDao.getCommentsForPost(postId).map { Resource.Success(it) }
    }

    override fun getRepliesForComment(commentId: String): Flow<Resource<List<CommentWithAuthor>>> {
        return commentDao.getRepliesForComment(commentId).map { Resource.Success(it) }
    }

    override fun getLikedCommentsForCurrentUser(): Flow<List<CommentLikeEntity>> {
        val currentUserId = auth.currentUser?.uid ?: return flowOf(emptyList())
        return commentLikeDao.getLikedComments(currentUserId)
    }

    override suspend fun refreshCommentsForPost(postId: String): Resource<Unit> {
        return try {
            val snapshot = firestore.collection("posts").document(postId).collection("comments")
                .orderBy("createdAt", Query.Direction.ASCENDING).get().await()
            val comments = snapshot.toObjects(Comment::class.java)
            
            // Collect unique user IDs để sync UserProfiles
            val userIds = comments.map { it.userId }.filter { it.isNotBlank() }.toSet()
            syncUserProfiles(userIds)
            
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

    override suspend fun replyToComment(parentComment: Comment, replyContent: String, authorId: String): Resource<Unit> {
        return try {
            val newCommentRef = firestore.collection("posts").document(parentComment.postId).collection("comments").document()
            val reply = Comment(
                id = newCommentRef.id,
                postId = parentComment.postId,
                userId = authorId,
                content = replyContent,
                parentId = parentComment.id,
                createdAt = Date(),
                updatedAt = Date()
            )
            newCommentRef.set(reply).await()
            commentDao.upsert(reply)
            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("CommentRepositoryImpl", "Error replying to comment", e)
            Resource.Error(e.message ?: "Lỗi trả lời bình luận")
        }
    }

    override suspend fun toggleCommentLike(commentId: String, isCurrentlyLiked: Boolean): Resource<Unit> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        val delta = if (isCurrentlyLiked) -1 else 1
        
        // Optimistic update Room
        if (isCurrentlyLiked) {
            commentLikeDao.delete(commentId, currentUser.uid)
        } else {
            commentLikeDao.insert(CommentLikeEntity(commentId = commentId, userId = currentUser.uid))
        }
        commentDao.updateLikeCount(commentId, delta)
        
        return try {
            // TODO: Cập nhật Firestore - cần tìm postId của comment
            // Tạm thời chỉ update local, Cloud Function sẽ xử lý sync
            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("CommentRepositoryImpl", "Error toggling comment like", e)
            
            // Rollback
            if (isCurrentlyLiked) {
                commentLikeDao.insert(CommentLikeEntity(commentId = commentId, userId = currentUser.uid))
            } else {
                commentLikeDao.delete(commentId, currentUser.uid)
            }
            commentDao.updateLikeCount(commentId, -delta)
            
            Resource.Error(e.message ?: "Lỗi thích bình luận")
        }
    }

    override suspend fun updateComment(comment: Comment): Resource<Unit> {
        return try {
            firestore.collection("posts").document(comment.postId)
                .collection("comments").document(comment.id)
                .set(comment)
                .await()
            commentDao.upsertAll(listOf(comment))
            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("CommentRepositoryImpl", "Error updating comment", e)
            Resource.Error(e.message ?: "Lỗi cập nhật bình luận")
        }
    }

    override suspend fun deleteComment(commentId: String, postId: String): Resource<Unit> {
        return try {
            firestore.collection("posts").document(postId)
                .collection("comments").document(commentId)
                .delete()
                .await()
            commentDao.delete(commentId)
            // Logic cập nhật commentCount sẽ do Cloud Function xử lý
            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("CommentRepositoryImpl", "Error deleting comment", e)
            Resource.Error(e.message ?: "Lỗi xóa bình luận")
        }
    }

    /**
     * Sync UserProfiles từ Firestore vào Room database
     * Fetch users in batches để tránh vượt quá giới hạn Firestore
     */
    private suspend fun syncUserProfiles(userIds: Set<String>) {
        if (userIds.isEmpty()) return

        try {
            val usersCollection = firestore.collection("users")
            val userProfiles = mutableListOf<UserProfile>()

            // Fetch users in batches
            userIds.chunked(10).forEach { batch ->
                val futures = batch.map { userId ->
                    usersCollection.document(userId).get()
                }
                val snapshots = futures.map { it.await() }

                snapshots.forEach { snapshot ->
                    if (snapshot.exists()) {
                        val userDto = snapshot.toObject(User::class.java)
                        userDto?.let { user ->
                            userProfiles.add(
                                UserProfile(
                                    id = user.id,
                                    name = user.name,
                                    username = user.username,
                                    email = user.email ?: "",
                                    phoneNumber = user.phoneNumber,
                                    avatarUrl = user.avatarUrl,
                                    bio = user.bio,
                                    isVerified = user.isVerified,
                                    postCount = user.postCount,
                                    followerCount = user.followerCount,
                                    followingCount = user.followingCount,
                                    totalLikesReceived = user.totalLikesReceived,
                                    createdAt = user.createdAt,
                                    updatedAt = user.updatedAt
                                )
                            )
                        }
                    }
                }
            }

            if (userProfiles.isNotEmpty()) {
                userDao.upsertAll(userProfiles)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("CommentRepositoryImpl", "Error syncing user profiles", e)
            // Không throw error để không làm gián đoạn refresh comments
        }
    }
}
