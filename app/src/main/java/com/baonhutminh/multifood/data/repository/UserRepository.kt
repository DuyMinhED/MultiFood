package com.baonhutminh.multifood.data.repository

import android.util.Log
import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.data.model.User
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val authRepository: AuthRepository = AuthRepository(),
    private val favoriteRepository: FavoriteRepository = FavoriteRepository()
) {
    private val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

    /**
     * Lấy user hiện tại từ FirebaseAuth
     */
    fun getCurrentUser(): User? {
        val firebaseUser = authRepository.currentUser ?: return null
        return User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName ?: firebaseUser.email.orEmpty(),
            email = firebaseUser.email.orEmpty(),
            idFavoritePost = emptyList()
        )
    }

    /**
     * ✅ Lấy danh sách postId user đã thích từ Firestore
     */
    suspend fun getFavoritePostIds(): List<String> {
        val uid = authRepository.currentUser?.uid ?: return emptyList()
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.get("favoritePosts") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            Log.e("UserRepository", "Lỗi lấy favoritePosts: $e")
            emptyList()
        }
    }

    /**
     * ✅ Lọc ra danh sách bài viết user đã thích (dựa theo postId lấy từ Firestore)
     */
    suspend fun getFavoritePosts(allPosts: List<Post>): List<Post> {
        val favoriteIds = getFavoritePostIds()
        return allPosts.filter { it.id in favoriteIds }
    }

    /**
     * ✅ Lọc bài viết do user đăng
     */
    fun getUserPosts(allPosts: List<Post>): List<Post> {
        val uid = authRepository.currentUser?.uid ?: return emptyList()
        return allPosts.filter { it.userId == uid }
    }

    /**
     * ✅ Cập nhật thích / bỏ thích thật
     */
    suspend fun toggleFavorite(postId: String, isFavorite: Boolean) {
        val uid = authRepository.currentUser?.uid ?: return
        favoriteRepository.toggleFavorite(uid, postId, isFavorite)
    }
}
