package com.baonhutminh.multifood.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.baonhutminh.multifood.data.model.PostImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostImageDao {

    @Upsert
    suspend fun upsert(postImage: PostImageEntity)

    @Upsert
    suspend fun upsertAll(postImages: List<PostImageEntity>)

    @Query("SELECT * FROM post_images WHERE postId = :postId ORDER BY `order` ASC")
    fun getImagesForPost(postId: String): Flow<List<PostImageEntity>>

    @Query("DELETE FROM post_images WHERE postId = :postId")
    suspend fun deleteImagesForPost(postId: String)

    @Query("DELETE FROM post_images WHERE dbId = :dbId")
    suspend fun delete(dbId: Long)

    @Query("DELETE FROM post_images")
    suspend fun clearAll()
}







