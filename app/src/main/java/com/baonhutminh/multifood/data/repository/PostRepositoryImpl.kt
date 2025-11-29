package com.baonhutminh.multifood.data.repository

import android.util.Log
import com.baonhutminh.multifood.data.local.CommentDao
import com.baonhutminh.multifood.data.local.PostDao
import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.data.model.PostEntity
import com.baonhutminh.multifood.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val postDao: PostDao,
    private val commentDao: CommentDao
) : PostRepository {

    private val postsCollection = firestore.collection("posts")
    private val commentsCollection = firestore.collection("comments")

    override fun getAllPosts(): Flow<Resource<List<PostEntity>>> {
        return postDao.getAllPosts().map { Resource.Success(it) }
    }

    override fun getPostById(postId: String): Flow<Resource<PostEntity?>> {
        return postDao.getPostById(postId).map { Resource.Success(it) }
    }

    override fun getPostsForUser(userId: String): Flow<Resource<List<PostEntity>>> {
        return postDao.getPostsForUser(userId).map { Resource.Success(it) }
    }

    override fun getLikedPosts(postIds: List<String>): Flow<Resource<List<PostEntity>>> {
        if (postIds.isEmpty()) {
            return kotlinx.coroutines.flow.flowOf(Resource.Success(emptyList()))
        }
        return postDao.getPostsByIds(postIds).map { Resource.Success(it) }
    }

    override fun getCommentsForPost(postId: String): Flow<Resource<List<Comment>>> {
        return commentDao.getCommentsForPost(postId).map { Resource.Success(it) }
    }

    override suspend fun refreshAllPosts(): Resource<Unit> {
        return try {
            val snapshot = postsCollection.orderBy("createdAt", Query.Direction.DESCENDING).get().await()
            val postDTOs = snapshot.toObjects(Post::class.java)
            // Ánh xạ từ List<Post> (DTO) sang List<PostEntity>
            val postEntities = postDTOs.map { it.toEntity() }
            postDao.upsertAll(postEntities)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("PostRepositoryImpl", "Error refreshing posts", e)
            Resource.Error(e.message ?: "Lỗi làm mới bài đăng")
        }
    }

    override suspend fun refreshCommentsForPost(postId: String): Resource<Unit> {
        return try {
            val snapshot = commentsCollection.whereEqualTo("reviewId", postId)
                .orderBy("createdAt", Query.Direction.ASCENDING).get().await()
            val comments = snapshot.toObjects(Comment::class.java)
            commentDao.upsertAll(comments)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("PostRepositoryImpl", "Error refreshing comments", e)
            Resource.Error(e.message ?: "Lỗi làm mới bình luận")
        }
    }

    override suspend fun createPost(post: Post): Resource<String> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        return try {
            val newPostRef = postsCollection.document()
            val newPost = post.copy(id = newPostRef.id, userId = currentUser.uid)
            newPostRef.set(newPost).await()
            refreshAllPosts()
            Resource.Success(newPost.id)
        } catch (e: Exception) {
            Log.e("PostRepositoryImpl", "Error creating post", e)
            Resource.Error(e.message ?: "Lỗi tạo bài đăng")
        }
    }

    override suspend fun addComment(comment: Comment): Resource<Unit> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        return try {
            val newCommentRef = commentsCollection.document()
            val newComment = comment.copy(id = newCommentRef.id, userId = currentUser.uid)
            newCommentRef.set(newComment).await()
            refreshCommentsForPost(comment.reviewId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("PostRepositoryImpl", "Error adding comment", e)
            Resource.Error(e.message ?: "Lỗi thêm bình luận")
        }
    }
}

// Hàm mở rộng để ánh xạ từ DTO sang Entity
fun Post.toEntity(): PostEntity {
    return PostEntity(
        id = this.id,
        userId = this.userId,
        title = this.title,
        rating = this.rating,
        content = this.content,
        imageUrls = this.imageUrls,
        pricePerPerson = this.pricePerPerson,
        visitTimestamp = this.visitTimestamp,
        userName = this.userName,
        userAvatarUrl = this.userAvatarUrl,
        placeName = this.placeName,
        placeAddress = this.placeAddress,
        placeCoverImage = this.placeCoverImage,
        likeCount = this.likeCount,
        commentCount = this.commentCount,
        status = this.status,
        createdAt = this.createdAt?.time ?: 0L,
        updatedAt = this.updatedAt?.time ?: 0L
    )
}