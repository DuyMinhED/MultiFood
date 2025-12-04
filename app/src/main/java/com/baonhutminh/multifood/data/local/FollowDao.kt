package com.baonhutminh.multifood.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.baonhutminh.multifood.data.model.FollowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(follow: FollowEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(follows: List<FollowEntity>)

    @Query("DELETE FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun delete(followerId: String, followingId: String)

    @Query("SELECT COUNT(*) > 0 FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    fun isFollowing(followerId: String, followingId: String): Flow<Boolean>

    @Query("SELECT * FROM follows WHERE followerId = :userId")
    fun getFollowing(userId: String): Flow<List<FollowEntity>>

    @Query("SELECT * FROM follows WHERE followingId = :userId")
    fun getFollowers(userId: String): Flow<List<FollowEntity>>

    @Query("SELECT COUNT(*) FROM follows WHERE followerId = :userId")
    fun getFollowingCount(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM follows WHERE followingId = :userId")
    fun getFollowerCount(userId: String): Flow<Int>

    @Query("DELETE FROM follows WHERE followerId = :userId")
    suspend fun clearFollowingForUser(userId: String)
}

