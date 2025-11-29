package com.baonhutminh.multifood.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.baonhutminh.multifood.data.model.Comment
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {

    @Upsert
    suspend fun upsertAll(comments: List<Comment>)

    @Query("SELECT * FROM comments WHERE reviewId = :postId ORDER BY createdAt ASC")
    fun getCommentsForPost(postId: String): Flow<List<Comment>>

    @Query("DELETE FROM comments WHERE reviewId = :postId")
    suspend fun deleteCommentsForPost(postId: String)
}