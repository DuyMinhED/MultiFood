package com.baonhutminh.multifood.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.baonhutminh.multifood.data.model.Post
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    @Upsert
    suspend fun upsert(post: Post)

    @Upsert
    suspend fun upsertAll(posts: List<Post>)

    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPostsForUser(userId: String): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE id = :postId")
    fun getPostById(postId: String): Flow<Post?>

    @Query("SELECT * FROM posts WHERE id IN (:postIds) ORDER BY createdAt DESC")
    fun getPostsByIds(postIds: List<String>): Flow<List<Post>> // Hàm mới

    @Query("SELECT COUNT(id) FROM posts WHERE userId = :userId")
    suspend fun getPostCountForUser(userId: String): Int

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun delete(postId: String)
}