package com.baonhutminh.multifood.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.data.model.relations.CommentWithAuthor
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {

    @Upsert
    suspend fun upsertAll(comments: List<Comment>)

    @Transaction
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun getCommentsForPost(postId: String): Flow<List<CommentWithAuthor>>

    @Query("DELETE FROM comments WHERE postId = :postId")
    suspend fun deleteCommentsForPost(postId: String)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun delete(commentId: String)

    @Query("DELETE FROM comments")
    suspend fun clearAll()
}
