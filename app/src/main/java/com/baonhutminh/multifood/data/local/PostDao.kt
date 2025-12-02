package com.baonhutminh.multifood.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.baonhutminh.multifood.data.model.PostEntity
import com.baonhutminh.multifood.data.model.relations.PostWithAuthor
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    @Upsert
    suspend fun upsertAll(posts: List<PostEntity>)

    @Transaction
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostWithAuthor>>

    @Transaction
    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPostsForUser(userId: String): Flow<List<PostWithAuthor>>

    @Transaction
    @Query("SELECT * FROM posts WHERE id = :postId")
    fun getPostById(postId: String): Flow<PostWithAuthor?>

    @Transaction
    @Query("SELECT * FROM posts WHERE id IN (:postIds) ORDER BY createdAt DESC")
    fun getPostsByIds(postIds: List<String>): Flow<List<PostWithAuthor>>

    @Transaction
    @Query("""
        SELECT * FROM posts 
        WHERE 
            (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
            AND rating >= :minRating
            AND pricePerPerson BETWEEN :minPrice AND :maxPrice
        ORDER BY createdAt DESC
    """)
    fun searchPosts(query: String, minRating: Float, minPrice: Int, maxPrice: Int): Flow<List<PostWithAuthor>>

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun delete(postId: String)

    @Query("DELETE FROM posts")
    suspend fun clearAll()

    @Transaction
    suspend fun syncPosts(posts: List<PostEntity>) {
        clearAll()
        upsertAll(posts)
    }
}
