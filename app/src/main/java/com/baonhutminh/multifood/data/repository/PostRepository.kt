package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.util.Resource
import kotlinx.coroutines.flow.Flow

interface PostRepository {

    fun getAllPosts(): Flow<Resource<List<Post>>>

    fun getPostById(postId: String): Flow<Resource<Post?>>

    fun getPostsForUser(userId: String): Flow<Resource<List<Post>>>

    fun getLikedPosts(postIds: List<String>): Flow<Resource<List<Post>>> // Hàm mới

    fun getCommentsForPost(postId: String): Flow<Resource<List<Comment>>>

    suspend fun refreshAllPosts(): Resource<Unit>

    suspend fun refreshCommentsForPost(postId: String): Resource<Unit>

    suspend fun createPost(post: Post): Resource<String> // Trả về ID của post mới

    suspend fun addComment(comment: Comment): Resource<Unit>

}
