package com.baonhutminh.multifood.data.repository

import android.net.Uri
import android.util.Log
import com.baonhutminh.multifood.data.local.CommentDao
import com.baonhutminh.multifood.data.local.PostDao
import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.data.model.PostEntity
import com.baonhutminh.multifood.data.model.relations.PostWithAuthor
import com.baonhutminh.multifood.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.CancellationException
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val postDao: PostDao,
    private val commentDao: CommentDao
) : PostRepository {

    private val postsCollection = firestore.collection("posts")
    private val usersCollection = firestore.collection("users")

    override fun getAllPosts(): Flow<Resource<List<PostWithAuthor>>> {
        return postDao.getAllPosts().map { Resource.Success(it) }
    }

    override fun getPostById(postId: String): Flow<Resource<PostWithAuthor?>> {
        return postDao.getPostById(postId).map { Resource.Success(it) }
    }

    override fun getPostsForUser(userId: String): Flow<Resource<List<PostWithAuthor>>> {
        return postDao.getPostsForUser(userId).map { Resource.Success(it) }
    }

    override fun getLikedPosts(postIds: List<String>): Flow<Resource<List<PostWithAuthor>>> {
        if (postIds.isEmpty()) {
            return kotlinx.coroutines.flow.flowOf(Resource.Success(emptyList()))
        }
        return postDao.getPostsByIds(postIds).map { Resource.Success(it) }
    }

    override fun searchPosts(query: String, minRating: Float, minPrice: Int, maxPrice: Int): Flow<Resource<List<PostWithAuthor>>> {
        return postDao.searchPosts(query, minRating, minPrice, maxPrice).map { Resource.Success(it) }
    }

    override suspend fun refreshAllPosts(): Resource<Unit> {
        return try {
            val snapshot = postsCollection.orderBy("createdAt", Query.Direction.DESCENDING).get().await()
            val postDTOs = snapshot.toObjects(Post::class.java)
            val postEntities = postDTOs.map { it.toEntity() }
            postDao.syncPosts(postEntities)
            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("PostRepositoryImpl", "Error refreshing posts", e)
            Resource.Error(e.message ?: "Lỗi làm mới bài đăng")
        }
    }

    override suspend fun createPost(post: Post): Resource<String> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        return try {
            val newPostId = firestore.runTransaction {
                transaction ->
                val userRef = usersCollection.document(currentUser.uid)
                transaction.get(userRef)

                val newPostRef = postsCollection.document()
                val newPost = post.copy(id = newPostRef.id, userId = currentUser.uid)
                transaction.set(newPostRef, newPost)
                transaction.update(userRef, "postCount", FieldValue.increment(1))

                newPostRef.id
            }.await()

            Resource.Success(newPostId)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("PostRepositoryImpl", "Error creating post", e)
            Resource.Error(e.message ?: "Lỗi tạo bài đăng")
        }
    }

    override suspend fun updatePost(post: Post): Resource<Unit> {
        return try {
            postsCollection.document(post.id).set(post, SetOptions.merge()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("PostRepositoryImpl", "Error updating post", e)
            Resource.Error(e.message ?: "Lỗi cập nhật bài đăng")
        }
    }

    override suspend fun uploadPostImage(imageUri: Uri): Resource<String> {
        auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        return try {
            val imageId = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("post_images/$imageId.jpg")
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Resource.Success(downloadUrl)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("PostRepositoryImpl", "Error uploading post image", e)
            Resource.Error(e.message ?: "Lỗi tải ảnh bài đăng lên")
        }
    }

    override suspend fun deletePost(postId: String, authorId: String): Resource<Unit> {
        return try {
            val commentsToDelete = firestore.collection("comments").whereEqualTo("reviewId", postId).get().await()

            firestore.runTransaction {
                transaction ->
                val postRef = postsCollection.document(postId)
                val userRef = usersCollection.document(authorId)

                transaction.get(postRef)

                for (doc in commentsToDelete) {
                    transaction.delete(doc.reference)
                }

                transaction.delete(postRef)
                transaction.update(userRef, "postCount", FieldValue.increment(-1))

            }.await()

            commentDao.deleteCommentsForPost(postId)
            postDao.delete(postId)

            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("PostRepositoryImpl", "Error deleting post", e)
            Resource.Error(e.message ?: "Lỗi xóa bài viết")
        }
    }
}

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
        placeName = this.placeName,
        placeAddress = this.placeAddress,
        placeCoverImage = this.placeCoverImage,
        likeCount = this.likeCount,
        commentCount = this.commentCount,
        status = this.status,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
