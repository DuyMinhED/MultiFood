package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.util.Resource

interface PostRepository {
    suspend fun getAllPosts(): Resource<List<Post>>
    suspend fun toggleLikePost(
        postId: String,
        userId: String,
        isCurrentlyLiked: Boolean
    ): Resource<Unit>

    suspend fun getPostById(postId: String): Resource<Post>
    suspend fun createPost(post: Post): Resource<String>
}