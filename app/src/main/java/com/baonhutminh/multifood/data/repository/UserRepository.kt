package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.data.model.User
import com.baonhutminh.multifood.util.Resource

interface UserRepository {
    suspend fun getCurrentUser(): Resource<User?>
    suspend fun updateUser(user: User): Resource<Unit>
    suspend fun updateLastActive(): Resource<Unit>
}



