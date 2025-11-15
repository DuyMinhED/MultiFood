package com.baonhutminh.multifood.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Repository xử lý đăng nhập / đăng ký FirebaseAuth.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Đăng ký tài khoản mới.
     */
    suspend fun register(email: String, password: String): FirebaseUser? {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user
    }

    /**
     * Đăng nhập tài khoản có sẵn.
     */
    suspend fun login(email: String, password: String): FirebaseUser? {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user
    }

    /**
     * Đăng xuất.
     */
    fun logout() {
        auth.signOut()
    }
}
