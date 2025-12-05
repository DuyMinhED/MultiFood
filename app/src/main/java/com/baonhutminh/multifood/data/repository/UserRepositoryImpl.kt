package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.data.model.User
import com.baonhutminh.multifood.common.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun getCurrentUser(): Resource<User?> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Chưa đăng nhập")
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            val user = snapshot.toObject(User::class.java)
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Không thể tải thông tin người dùng")
        }
    }

    override suspend fun updateUser(user: User): Resource<Unit> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Chưa đăng nhập")
        return try {
            firestore.collection("users").document(uid).set(user.copy(id = uid)).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Không thể cập nhật thông tin người dùng")
        }
    }

    override suspend fun updateLastActive(): Resource<Unit> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Chưa đăng nhập")
        return try {
            firestore.collection("users").document(uid)
                .update("lastActiveAt", System.currentTimeMillis())
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Không thể cập nhật trạng thái hoạt động")
        }
    }
}



