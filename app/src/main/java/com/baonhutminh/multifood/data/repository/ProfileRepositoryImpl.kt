package com.baonhutminh.multifood.data.repository

import android.net.Uri
import android.util.Log
import com.baonhutminh.multifood.data.local.PostDao
import com.baonhutminh.multifood.data.local.PostLikeDao
import com.baonhutminh.multifood.data.local.UserDao
import com.baonhutminh.multifood.data.model.Like
import com.baonhutminh.multifood.data.model.PostLikeEntity
import com.baonhutminh.multifood.data.model.User
import com.baonhutminh.multifood.data.model.UserProfile
import com.baonhutminh.multifood.util.Resource
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val userDao: UserDao,
    private val postLikeDao: PostLikeDao,
    private val postDao: PostDao
) : ProfileRepository {

    private val usersCollection = firestore.collection("users")
    private val postsCollection = firestore.collection("posts")
    private val likesCollection = firestore.collection("likes")

    override fun getUserProfile(): Flow<Resource<UserProfile?>> {
        val currentUser = auth.currentUser ?: return kotlinx.coroutines.flow.flowOf(Resource.Error("Chưa đăng nhập"))
        return userDao.getUserProfile(currentUser.uid).map { profile ->
            Resource.Success(profile)
        }
    }

    override fun getLikedPostsForCurrentUser(): Flow<List<PostLikeEntity>> {
        val currentUserId = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return likesCollection.whereEqualTo("userId", currentUserId)
            .snapshots()
            .flatMapLatest { snapshot ->
                val likes = snapshot.documents.mapNotNull { doc ->
                    doc.getString("postId")?.let { postId ->
                        PostLikeEntity(postId = postId, userId = currentUserId)
                    }
                }
                postLikeDao.clearAllForUser(currentUserId)
                if (likes.isNotEmpty()) {
                    postLikeDao.insertAll(likes)
                }
                postLikeDao.getLikedPosts(currentUserId)
            }
            .onStart {
                emitAll(postLikeDao.getLikedPosts(currentUserId))
            }
            .catch { e ->
                Log.e("ProfileRepositoryImpl", "Error getting liked posts", e)
                emitAll(postLikeDao.getLikedPosts(currentUserId))
            }
    }

    override suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean): Resource<Unit> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        val delta = if (isCurrentlyLiked) -1 else 1
        
        // Optimistic update Room TRƯỚC → UI cập nhật ngay (cả icon và số lượt)
        if (isCurrentlyLiked) {
            postLikeDao.delete(postId, currentUser.uid)
        } else {
            postLikeDao.insert(PostLikeEntity(postId = postId, userId = currentUser.uid))
        }
        postDao.updateLikeCount(postId, delta) // Update số lượt ngay
        
        return try {
            val likeDocId = "${currentUser.uid}_$postId"
            val rootLikeRef = likesCollection.document(likeDocId)
            val postLikeRef = postsCollection.document(postId).collection("likes").document(currentUser.uid)

            firestore.runBatch { batch ->
                if (isCurrentlyLiked) {
                    batch.delete(rootLikeRef)
                    batch.delete(postLikeRef)
                } else {
                    val likeData = hashMapOf(
                        "userId" to currentUser.uid,
                        "postId" to postId,
                        "timestamp" to System.currentTimeMillis()
                    )
                    batch.set(rootLikeRef, likeData)
                    batch.set(postLikeRef, Like())
                }
            }.await()
            
            // Firestore realtime sync sẽ confirm giá trị đúng từ Cloud Function

            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("ProfileRepositoryImpl", "Error toggling like", e)
            
            // Rollback Room nếu Firestore fail
            if (isCurrentlyLiked) {
                postLikeDao.insert(PostLikeEntity(postId = postId, userId = currentUser.uid))
            } else {
                postLikeDao.delete(postId, currentUser.uid)
            }
            postDao.updateLikeCount(postId, -delta) // Rollback số lượt
            
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
                    username = userDto.username,
                    email = userDto.email ?: "",
                    phoneNumber = userDto.phoneNumber,
                    avatarUrl = userDto.avatarUrl,
                    bio = userDto.bio,
                    isVerified = userDto.isVerified,
                    postCount = userDto.postCount,
                    followerCount = userDto.followerCount,
                    followingCount = userDto.followingCount,
                    totalLikesReceived = userDto.totalLikesReceived,
                    createdAt = userDto.createdAt,
                    updatedAt = userDto.updatedAt
                )
                userDao.upsert(userProfile)
            } else {
                val newUser = User(
                    id = currentUser.uid,
                    name = currentUser.displayName ?: "",
                    username = currentUser.email?.substringBefore('@') ?: "",
                    email = currentUser.email
                )
                usersCollection.document(currentUser.uid).set(newUser).await()
                val newUserProfile = UserProfile(
                    id = newUser.id,
                    name = newUser.name,
                    username = newUser.username,
                    email = newUser.email ?: ""
                )
                userDao.upsert(newUserProfile)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
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
            if (e is CancellationException) throw e
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
            if (e is CancellationException) throw e
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
            if (e is CancellationException) throw e
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
            if (e is CancellationException) throw e
            Log.e("ProfileRepositoryImpl", "Error changing password", e)
            val errorMessage = when (e) {
                is FirebaseAuthInvalidCredentialsException -> "Mật khẩu hiện tại không đúng. Vui lòng thử lại."
                is FirebaseNetworkException -> "Lỗi kết nối mạng. Vui lòng kiểm tra lại kết nối và thử lại."
                else -> e.message ?: "Đã có lỗi xảy ra. Vui lòng thử lại sau."
            }
            Resource.Error(errorMessage)
        }
    }

    override suspend fun updatePhoneNumber(newPhoneNumber: String): Resource<Unit> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        return try {
            usersCollection.document(currentUser.uid).update("phoneNumber", newPhoneNumber).await()
            refreshUserProfile()
            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("ProfileRepositoryImpl", "Error updating phone number", e)
            Resource.Error(e.message ?: "Lỗi cập nhật số điện thoại")
        }
    }
}
