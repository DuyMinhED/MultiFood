package com.baonhutminh.multifood.data.repository

import android.net.Uri
import android.util.Log
import com.baonhutminh.multifood.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ProfileRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    suspend fun getUserProfile(): UserProfile? {
        val currentUser = auth.currentUser ?: return null

        return try {
            val doc = firestore.collection("users").document(currentUser.uid).get().await()

            if (doc.exists()) {
                UserProfile(
                    id = currentUser.uid,
                    displayName = doc.getString("displayName") ?: currentUser.displayName ?: "",
                    email = currentUser.email ?: "",
                    avatarUrl = doc.getString("avatarUrl") ?: currentUser.photoUrl?.toString() ?: "",
                    bio = doc.getString("bio") ?: "",
                    totalPosts = (doc.getLong("totalPosts") ?: 0).toInt(),
                    totalFavorites = (doc.get("favoritePosts") as? List<*>)?.size ?: 0,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } else {
                UserProfile(
                    id = currentUser.uid,
                    displayName = currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "",
                    email = currentUser.email ?: "",
                    avatarUrl = currentUser.photoUrl?.toString() ?: "",
                    bio = "",
                    totalPosts = 0,
                    totalFavorites = 0
                )
            }
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error getting user profile: $e")
            null
        }
    }

    suspend fun updateDisplayName(newName: String): Boolean {
        val currentUser = auth.currentUser ?: return false

        return try {
            val profileUpdates = userProfileChangeRequest {
                displayName = newName
            }
            currentUser.updateProfile(profileUpdates).await()

            firestore.collection("users").document(currentUser.uid)
                .update("displayName", newName).await()

            true
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error updating display name: $e")
            false
        }
    }

    suspend fun updateBio(newBio: String): Boolean {
        val currentUser = auth.currentUser ?: return false

        return try {
            firestore.collection("users").document(currentUser.uid)
                .update("bio", newBio).await()
            true
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error updating bio: $e")
            false
        }
    }

    suspend fun uploadAvatar(imageUri: Uri): String? {
        val currentUser = auth.currentUser ?: return null

        return try {
            val storageRef = storage.reference
                .child("avatars")
                .child("${currentUser.uid}.jpg")

            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            firestore.collection("users").document(currentUser.uid)
                .update("avatarUrl", downloadUrl).await()

            val profileUpdates = userProfileChangeRequest {
                photoUri = Uri.parse(downloadUrl)
            }
            currentUser.updateProfile(profileUpdates).await()

            downloadUrl
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error uploading avatar: $e")
            null
        }
    }

    suspend fun getUserPostsCount(): Int {
        val currentUser = auth.currentUser ?: return 0

        return try {
            val snapshot = firestore.collection("posts")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error getting posts count: $e")
            0
        }
    }

    suspend fun getUserFavoritesCount(): Int {
        val currentUser = auth.currentUser ?: return 0

        return try {
            val doc = firestore.collection("users").document(currentUser.uid).get().await()
            (doc.get("favoritePosts") as? List<*>)?.size ?: 0
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error getting favorites count: $e")
            0
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("Chưa đăng nhập"))
        val email = currentUser.email ?: return Result.failure(Exception("Email không hợp lệ"))

        return try {
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)
            currentUser.reauthenticate(credential).await()

            currentUser.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error changing password: $e")
            Result.failure(e)
        }
    }
}
