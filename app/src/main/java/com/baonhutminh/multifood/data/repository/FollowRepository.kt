package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.util.Resource
import kotlinx.coroutines.flow.Flow

interface FollowRepository {
    
    fun isFollowing(userId: String): Flow<Boolean>
    
    suspend fun toggleFollow(userId: String, isCurrentlyFollowing: Boolean): Resource<Unit>
    
    suspend fun syncFollowsFromFirestore()
}

