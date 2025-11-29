package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.data.model.User
import com.baonhutminh.multifood.util.Resource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PostRepository {

    private val postsCollection = firestore.collection("posts")
    private val usersCollection = firestore.collection("users")

    override suspend fun createPost(post: Post): Resource<String> {
        return try {
            val docRef = postsCollection.add(post).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi không xác định")
        }
    }

    override suspend fun getAllPosts(): Resource<List<Post>> {
        return try {
            val snapshot = postsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()

            val posts = snapshot.documents.mapNotNull { doc ->
                val post = doc.toObject(Post::class.java)?.copy(id = doc.id)
                if (post != null) {
                    // Lấy thông tin user
                    val userSnapshot = usersCollection.document(post.userId).get().await()
                    val user = userSnapshot.toObject(User::class.java)
                    post.copy(
                        userName = user?.name ?: "Người dùng ẩn",
                        userAvatarUrl = user?.avatarUrl ?: ""
                    )
                } else {
                    null
                }
            }
            Resource.Success(posts)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Không thể tải danh sách bài viết")
        }
    }

    override suspend fun getPostById(postId: String): Resource<Post> {
        return try {
            val doc = postsCollection.document(postId).get().await()
            val post = doc.toObject(Post::class.java)?.copy(id = doc.id)
            if (post != null) {
                // Lấy thông tin user
                val userSnapshot = usersCollection.document(post.userId).get().await()
                val user = userSnapshot.toObject(User::class.java)
                val finalPost = post.copy(
                    userName = user?.name ?: "Người dùng ẩn",
                    userAvatarUrl = user?.avatarUrl ?: ""
                )
                Resource.Success(finalPost)
            } else {
                Resource.Error("Không tìm thấy bài viết")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi khi tải bài viết")
        }
    }

    override suspend fun toggleLikePost(postId: String, userId: String, isLiked: Boolean): Resource<Unit> {
        return try {
            val docRef = postsCollection.document(postId)
            val update = if (isLiked) {
                docRef.update("likedBy", FieldValue.arrayRemove(userId))
            } else {
                docRef.update("likedBy", FieldValue.arrayUnion(userId))
            }
            update.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi khi thích/bỏ thích")
        }
    }
}