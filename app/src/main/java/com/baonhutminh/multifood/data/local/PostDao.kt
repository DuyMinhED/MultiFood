package com.baonhutminh.multifood.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.baonhutminh.multifood.data.model.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    @Upsert
    suspend fun upsert(post: PostEntity)

    @Upsert
    suspend fun upsertAll(posts: List<PostEntity>)

    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPostsForUser(userId: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :postId")
    fun getPostById(postId: String): Flow<PostEntity?>

    @Query("SELECT * FROM posts WHERE id IN (:postIds) ORDER BY createdAt DESC")
    fun getPostsByIds(postIds: List<String>): Flow<List<PostEntity>>

    @Query("SELECT COUNT(id) FROM posts WHERE userId = :userId")
    suspend fun getPostCountForUser(userId: String): Int

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun delete(postId: String)

    @Query("DELETE FROM posts")
    suspend fun clearAll()

    @Transaction
    suspend fun syncPosts(posts: List<PostEntity>) {
        // Xóa tất cả dữ liệu cũ và chèn dữ liệu mới trong một giao dịch
        clearAll()
        upsertAll(posts)
    }
}