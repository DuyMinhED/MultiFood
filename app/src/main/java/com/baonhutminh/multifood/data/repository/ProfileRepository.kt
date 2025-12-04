package com.baonhutminh.multifood.data.repository

import android.net.Uri
import com.baonhutminh.multifood.data.model.PostLikeEntity
import com.baonhutminh.multifood.data.model.UserProfile
import com.baonhutminh.multifood.util.Resource
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    fun getUserProfile(): Flow<Resource<UserProfile?>>

    fun getLikedPostsForCurrentUser(): Flow<List<PostLikeEntity>>

    suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean): Resource<Unit>

    suspend fun refreshUserProfile(): Resource<Unit>

    suspend fun updateName(newName: String): Resource<Unit>

    suspend fun updateBio(newBio: String): Resource<Unit>

    suspend fun uploadAvatar(imageUri: Uri): Resource<String>

    suspend fun changePassword(currentPassword: String, newPassword: String): Resource<Unit>

    suspend fun updatePhoneNumber(newPhoneNumber: String): Resource<Unit>
}
