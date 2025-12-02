package com.baonhutminh.multifood.data.repository

import android.net.Uri
import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.data.model.PostEntity
import com.baonhutminh.multifood.util.Resource
import kotlinx.coroutines.flow.Flow

interface PostRepository {

    fun getAllPosts(): Flow<Resource<List<PostEntity>>>

    fun getPostById(postId: String): Flow<Resource<PostEntity?>>

    fun getPostsForUser(userId: String): Flow<Resource<List<PostEntity>>>

    fun getLikedPosts(postIds: List<String>): Flow<Resource<List<PostEntity>>>

    suspend fun refreshAllPosts(): Resource<Unit>

    suspend fun createPost(post: Post): Resource<String>

    suspend fun uploadPostImage(imageUri: Uri): Resource<String>

    suspend fun deletePost(postId: String, authorId: String): Resource<Unit>
}
