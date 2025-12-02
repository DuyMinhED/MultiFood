package com.baonhutminh.multifood.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.baonhutminh.multifood.data.model.PostLikeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostLikeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(postLike: PostLikeEntity)

    @Query("DELETE FROM post_likes WHERE postId = :postId AND userId = :userId")
    suspend fun delete(postId: String, userId: String)

    @Query("SELECT * FROM post_likes WHERE userId = :userId")
    fun getLikedPosts(userId: String): Flow<List<PostLikeEntity>>

    @Query("SELECT COUNT(*) > 0 FROM post_likes WHERE postId = :postId AND userId = :userId")
    fun isLiked(postId: String, userId: String): Flow<Boolean>
    
    // Thêm hàm xóa tất cả để dọn dẹp khi người dùng đăng xuất
    @Query("DELETE FROM post_likes WHERE userId = :userId")
    suspend fun clearAllForUser(userId: String)
}
