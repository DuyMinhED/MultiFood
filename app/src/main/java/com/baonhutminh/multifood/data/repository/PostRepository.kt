package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.data.model.Post // DTO
import com.baonhutminh.multifood.data.model.PostEntity
import com.baonhutminh.multifood.util.Resource
import kotlinx.coroutines.flow.Flow

interface PostRepository {

    fun getAllPosts(): Flow<Resource<List<PostEntity>>>

    fun getPostById(postId: String): Flow<Resource<PostEntity?>>

    fun getPostsForUser(userId: String): Flow<Resource<List<PostEntity>>>

    fun getLikedPosts(postIds: List<String>): Flow<Resource<List<PostEntity>>>

    fun getCommentsForPost(postId: String): Flow<Resource<List<Comment>>>

    suspend fun refreshAllPosts(): Resource<Unit>

    suspend fun refreshCommentsForPost(postId: String): Resource<Unit>

    suspend fun createPost(post: Post): Resource<String> // Nhận vào DTO

    suspend fun addComment(comment: Comment): Resource<Unit>

}
