package com.baonhutminh.multifood.data.repository

import android.net.Uri
import com.baonhutminh.multifood.data.model.UserProfile
import com.baonhutminh.multifood.util.Resource

interface ProfileRepository {
    suspend fun getUserProfile(): Resource<UserProfile>
    suspend fun updateDisplayName(newName: String): Resource<Unit>
    suspend fun updateBio(newBio: String): Resource<Unit>
    suspend fun uploadAvatar(imageUri: Uri): Resource<String>
    suspend fun getUserPostsCount(): Resource<Int>
    suspend fun getUserFavoritesCount(): Resource<Int>
    suspend fun changePassword(currentPassword: String, newPassword: String): Resource<Unit>

}
