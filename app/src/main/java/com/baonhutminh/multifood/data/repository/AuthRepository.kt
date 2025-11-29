package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.util.Resource

interface AuthRepository {
    suspend fun login(email: String, pass: String): Resource<String>
    suspend fun signup(email: String, pass: String, name: String): Resource<String>

    suspend fun sendPasswordReset(email: String): Resource<Unit>

    fun signOut()
}

