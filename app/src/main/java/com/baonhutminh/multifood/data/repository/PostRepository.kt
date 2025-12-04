package com.baonhutminh.multifood.data.repository

import android.net.Uri
import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.data.model.PostImage
import com.baonhutminh.multifood.data.model.relations.PostWithAuthor
import com.baonhutminh.multifood.util.Resource
import kotlinx.coroutines.flow.Flow

interface PostRepository {

    fun getAllPosts(): Flow<Resource<List<PostWithAuthor>>>

    fun getPostById(postId: String): Flow<Resource<PostWithAuthor?>>

    fun getPostsForUser(userId: String): Flow<Resource<List<PostWithAuthor>>>

    fun getLikedPosts(postIds: List<String>): Flow<Resource<List<PostWithAuthor>>>

    fun searchPosts(query: String, minRating: Float, minPrice: Int, maxPrice: Int): Flow<Resource<List<PostWithAuthor>>>

    suspend fun refreshAllPosts(): Resource<Unit>

    suspend fun refreshPost(postId: String): Resource<Unit>

    suspend fun createPost(post: Post, images: List<PostImage>): Resource<String>

    suspend fun updatePost(post: Post): Resource<Unit>

    suspend fun uploadPostImage(imageUri: Uri): Resource<String>

    suspend fun deletePost(postId: String): Resource<Unit>
}
