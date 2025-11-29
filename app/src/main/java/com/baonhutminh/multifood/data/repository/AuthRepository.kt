package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.data.model.User
import com.baonhutminh.multifood.domain.repository.AuthRepository
import com.baonhutminh.multifood.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun login(email: String, pass: String): Resource<String> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            Resource.Success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi không xác định")
        }
    }

    override suspend fun signup(email: String, pass: String, name: String): Resource<String> {
        return try {
            // 1. Tạo tài khoản trên Authentication
            val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: return Resource.Error("Không lấy được UID")

            // 2. Tạo User Model
            val user = User(
                id = uid,
                name = name,
                email = email,
                avatarUrl = "https://i.imgur.com/6VBx3io.png" // Avatar mặc định
            )

            // 3. Lưu vào Firestore (Collection "users")
            firestore.collection("users").document(uid).set(user).await()

            Resource.Success(uid)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Lỗi đăng ký không xác định")
        }
    }

    override suspend fun sendPasswordReset(email: String): Resource<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Không thể gửi email đặt lại mật khẩu")
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}