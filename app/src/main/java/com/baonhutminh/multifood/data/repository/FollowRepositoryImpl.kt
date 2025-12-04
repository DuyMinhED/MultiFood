package com.baonhutminh.multifood.data.repository

import android.util.Log
import com.baonhutminh.multifood.data.local.FollowDao
import com.baonhutminh.multifood.data.local.UserDao
import com.baonhutminh.multifood.data.model.FollowEntity
import com.baonhutminh.multifood.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FollowRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val followDao: FollowDao,
    private val userDao: UserDao
) : FollowRepository {

    private val followsCollection = firestore.collection("follows")
    private val usersCollection = firestore.collection("users")

    override fun isFollowing(userId: String): Flow<Boolean> {
        val currentUserId = auth.currentUser?.uid ?: return flowOf(false)
        return followDao.isFollowing(currentUserId, userId)
    }

    override suspend fun toggleFollow(userId: String, isCurrentlyFollowing: Boolean): Resource<Unit> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        val currentUserId = currentUser.uid
        
        // Không cho follow chính mình
        if (userId == currentUserId) {
            return Resource.Error("Không thể theo dõi chính mình")
        }

        // Optimistic update Room trước
        val delta = if (isCurrentlyFollowing) -1 else 1
        if (isCurrentlyFollowing) {
            followDao.delete(currentUserId, userId)
        } else {
            followDao.insert(FollowEntity(followerId = currentUserId, followingId = userId))
        }
        
        // Update followerCount trong Room ngay lập tức
        userDao.updateFollowerCount(userId, delta)

        // Sync với Firestore (không rollback nếu fail - để giữ UX mượt)
        try {
            val followDocId = "${currentUserId}_$userId"
            val followRef = followsCollection.document(followDocId)
            val firestoreDelta = if (isCurrentlyFollowing) -1L else 1L

            if (isCurrentlyFollowing) {
                // Unfollow - xóa document
                followRef.delete().await()
            } else {
                // Follow - tạo document mới
                val followData = hashMapOf(
                    "followerId" to currentUserId,
                    "followingId" to userId,
                    "timestamp" to System.currentTimeMillis()
                )
                followRef.set(followData).await()
            }
            
            // Update followerCount của user được follow trên Firestore
            try {
                usersCollection.document(userId)
                    .set(mapOf("followerCount" to FieldValue.increment(firestoreDelta)), SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.e("FollowRepository", "Error updating followerCount on Firestore", e)
            }
            
            // Update followingCount của current user trên Firestore
            try {
                usersCollection.document(currentUserId)
                    .set(mapOf("followingCount" to FieldValue.increment(firestoreDelta)), SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.e("FollowRepository", "Error updating followingCount on Firestore", e)
            }
            
            Log.d("FollowRepository", "Toggle follow success: $userId, wasFollowing: $isCurrentlyFollowing")
        } catch (e: Exception) {
            // Log lỗi nhưng KHÔNG rollback Room - để UI vẫn mượt
            Log.e("FollowRepository", "Error syncing follow to Firestore (keeping local state)", e)
        }
        
        return Resource.Success(Unit)
    }

    override suspend fun syncFollowsFromFirestore() {
        val currentUserId = auth.currentUser?.uid ?: return
        try {
            val snapshot = followsCollection
                .whereEqualTo("followerId", currentUserId)
                .get()
                .await()
            
            val follows = snapshot.documents.mapNotNull { doc ->
                doc.getString("followingId")?.let { followingId ->
                    FollowEntity(followerId = currentUserId, followingId = followingId)
                }
            }
            
            followDao.clearFollowingForUser(currentUserId)
            if (follows.isNotEmpty()) {
                followDao.insertAll(follows)
            }
        } catch (e: Exception) {
            Log.e("FollowRepository", "Error syncing follows", e)
        }
    }
}

