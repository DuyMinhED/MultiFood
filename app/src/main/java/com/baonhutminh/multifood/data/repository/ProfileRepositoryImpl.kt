package com.baonhutminh.multifood.data.repository

import android.net.Uri
import android.util.Log
import com.baonhutminh.multifood.data.model.UserProfile
import com.baonhutminh.multifood.util.Resource
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ProfileRepository {

    private val usersCollection = firestore.collection("users")
    private val postsCollection = firestore.collection("posts")

    override suspend fun getUserProfile(): Resource<UserProfile> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")

        return try {
            val doc = usersCollection.document(currentUser.uid).get().await()

            val userProfile = if (doc.exists()) {
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
                // Tạo hồ sơ mới nếu chưa có
                val newUserProfile = UserProfile(
                    id = currentUser.uid,
                    displayName = currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "",
                    email = currentUser.email ?: "",
                    avatarUrl = currentUser.photoUrl?.toString() ?: ""
                )
                usersCollection.document(currentUser.uid).set(newUserProfile).await()
                newUserProfile
            }
            Resource.Success(userProfile)
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error getting user profile", e)
            Resource.Error(e.message ?: "Lỗi tải hồ sơ")
        }
    }

    override suspend fun updateDisplayName(newName: String): Resource<Unit> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        return try {
            val profileUpdates = userProfileChangeRequest { displayName = newName }
            currentUser.updateProfile(profileUpdates).await()
            usersCollection.document(currentUser.uid).update("displayName", newName).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error updating display name", e)
            Resource.Error(e.message ?: "Lỗi cập nhật tên")
        }
    }

    override suspend fun updateBio(newBio: String): Resource<Unit> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        return try {
            usersCollection.document(currentUser.uid).update("bio", newBio).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error updating bio", e)
            Resource.Error(e.message ?: "Lỗi cập nhật tiểu sử")
        }
    }

    override suspend fun uploadAvatar(imageUri: Uri): Resource<String> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        return try {
            val storageRef = storage.reference.child("avatars/${currentUser.uid}.jpg")
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            val profileUpdates = userProfileChangeRequest { photoUri = Uri.parse(downloadUrl) }
            currentUser.updateProfile(profileUpdates).await()
            usersCollection.document(currentUser.uid).update("avatarUrl", downloadUrl).await()

            Resource.Success(downloadUrl)
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error uploading avatar", e)
            Resource.Error(e.message ?: "Lỗi tải ảnh lên")
        }
    }

    override suspend fun getUserPostsCount(): Resource<Int> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        return try {
            val snapshot = postsCollection.whereEqualTo("userId", currentUser.uid).get().await()
            Resource.Success(snapshot.size())
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error getting posts count", e)
            Resource.Error(e.message ?: "Lỗi đếm bài viết")
        }
    }

    override suspend fun getUserFavoritesCount(): Resource<Int> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        return try {
            val doc = usersCollection.document(currentUser.uid).get().await()
            val count = (doc.get("favoritePosts") as? List<*>)?.size ?: 0
            Resource.Success(count)
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error getting favorites count", e)
            Resource.Error(e.message ?: "Lỗi đếm yêu thích")
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Resource<Unit> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        val email = currentUser.email ?: return Resource.Error("Email không hợp lệ")
        return try {
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            currentUser.reauthenticate(credential).await()
            currentUser.updatePassword(newPassword).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error changing password", e)
            Resource.Error(e.message ?: "Lỗi đổi mật khẩu")
        }
    }
}