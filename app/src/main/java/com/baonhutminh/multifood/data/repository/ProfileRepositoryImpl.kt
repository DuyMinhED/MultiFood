package com.baonhutminh.multifood.data.repository

import android.net.Uri
import android.util.Log
import com.baonhutminh.multifood.data.local.UserDao
import com.baonhutminh.multifood.data.model.User
import com.baonhutminh.multifood.data.model.UserProfile
import com.baonhutminh.multifood.util.Resource
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val userDao: UserDao
) : ProfileRepository {

    private val usersCollection = firestore.collection("users")
    private val postsCollection = firestore.collection("posts")

    override fun getUserProfile(): Flow<Resource<UserProfile?>> {
        val currentUser = auth.currentUser ?: return kotlinx.coroutines.flow.flowOf(Resource.Error("Chưa đăng nhập"))
        return userDao.getUserProfile(currentUser.uid).map { profile ->
            Resource.Success(profile)
        }
    }

    override suspend fun toggleLike(postId: String): Resource<Unit> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        return try {
            firestore.runTransaction {
                transaction ->
                val userRef = usersCollection.document(currentUser.uid)
                val postRef = postsCollection.document(postId)

                val userDoc = transaction.get(userRef)
                val user = userDoc.toObject<User>() ?: throw Exception("Không tìm thấy người dùng.")

                val isLiked = user.likedPostIds.contains(postId)

                if (isLiked) {
                    // Bỏ thích
                    transaction.update(userRef, "likedPostIds", FieldValue.arrayRemove(postId))
                    transaction.update(postRef, "likeCount", FieldValue.increment(-1))
                } else {
                    // Thích
                    transaction.update(userRef, "likedPostIds", FieldValue.arrayUnion(postId))
                    transaction.update(postRef, "likeCount", FieldValue.increment(1))
                }
                null
            }.await()

            // Cập nhật lại profile và bài đăng cục bộ
            refreshUserProfile()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("ProfileRepositoryImpl", "Error toggling like", e)
            Resource.Error(e.message ?: "Lỗi khi thích bài viết")
        }
    }

    override suspend fun refreshUserProfile(): Resource<Unit> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")

        return try {
            val doc = usersCollection.document(currentUser.uid).get().await()
            val userDto = doc.toObject(User::class.java)

            if (userDto != null) {
                val userProfile = UserProfile(
                    id = userDto.id,
                    name = userDto.name,
                    email = userDto.email ?: "",
                    avatarUrl = userDto.avatarUrl,
                    bio = userDto.bio,
                    postCount = userDto.postCount,
                    followerCount = userDto.followerCount,
                    followingCount = userDto.followingCount,
                    likedPostIds = userDto.likedPostIds
                )
                userDao.upsert(userProfile)
            } else {
                val newUser = User(
                    id = currentUser.uid,
                    name = currentUser.displayName ?: currentUser.email?.substringBefore('@') ?: "",
                    email = currentUser.email
                )
                usersCollection.document(currentUser.uid).set(newUser).await()
                val newUserProfile = UserProfile(id = newUser.id, name = newUser.name, email = newUser.email ?: "")
                userDao.upsert(newUserProfile)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("ProfileRepositoryImpl", "Error refreshing user profile", e)
            Resource.Error(e.message ?: "Lỗi làm mới hồ sơ")
        }
    }

    override suspend fun updateName(newName: String): Resource<Unit> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        return try {
            val profileUpdates = userProfileChangeRequest { displayName = newName }
            currentUser.updateProfile(profileUpdates).await()
            usersCollection.document(currentUser.uid).update("name", newName).await()
            refreshUserProfile()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("ProfileRepositoryImpl", "Error updating name", e)
            Resource.Error(e.message ?: "Lỗi cập nhật tên")
        }
    }

    override suspend fun updateBio(newBio: String): Resource<Unit> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        return try {
            usersCollection.document(currentUser.uid).update("bio", newBio).await()
            refreshUserProfile()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("ProfileRepositoryImpl", "Error updating bio", e)
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
            refreshUserProfile()
            Resource.Success(downloadUrl)
        } catch (e: Exception) {
            Log.e("ProfileRepositoryImpl", "Error uploading avatar", e)
            Resource.Error(e.message ?: "Lỗi tải ảnh lên")
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
            Log.e("ProfileRepositoryImpl", "Error changing password", e)
            Resource.Error(e.message ?: "Lỗi đổi mật khẩu")
        }
    }
}