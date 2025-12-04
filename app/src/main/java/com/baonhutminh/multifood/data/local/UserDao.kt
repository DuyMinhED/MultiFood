package com.baonhutminh.multifood.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.baonhutminh.multifood.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Upsert
    suspend fun upsert(userProfile: UserProfile)

    @Upsert
    suspend fun upsertAll(userProfiles: List<UserProfile>)

    @Query("SELECT * FROM user_profiles WHERE id = :userId")
    fun getUserProfile(userId: String): Flow<UserProfile?>

    @Query("DELETE FROM user_profiles WHERE id = :userId")
    suspend fun delete(userId: String)
    
    @Query("UPDATE user_profiles SET followerCount = MAX(0, followerCount + :delta) WHERE id = :userId")
    suspend fun updateFollowerCount(userId: String, delta: Int)
}