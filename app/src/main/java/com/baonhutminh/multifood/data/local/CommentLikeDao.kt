package com.baonhutminh.multifood.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.baonhutminh.multifood.data.model.CommentLikeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentLikeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(commentLike: CommentLikeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(commentLikes: List<CommentLikeEntity>)

    @Query("DELETE FROM comment_likes WHERE commentId = :commentId AND userId = :userId")
    suspend fun delete(commentId: String, userId: String)

    @Query("SELECT * FROM comment_likes WHERE userId = :userId")
    fun getLikedComments(userId: String): Flow<List<CommentLikeEntity>>

    @Query("SELECT COUNT(*) > 0 FROM comment_likes WHERE commentId = :commentId AND userId = :userId")
    fun isLiked(commentId: String, userId: String): Flow<Boolean>

    @Query("DELETE FROM comment_likes WHERE userId = :userId")
    suspend fun clearAllForUser(userId: String)
}


