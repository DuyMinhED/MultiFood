package com.baonhutminh.multifood.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FavoriteRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun toggleFavorite(userId: String, postId: String, isFavorite: Boolean) {
        try {
            val userDoc = firestore.collection("users").document(userId)

            // ✅ Kiểm tra nếu chưa có document -> tự tạo
            val snapshot = userDoc.get().await()
            if (!snapshot.exists()) {
                Log.w("FavoriteRepo", "User $userId chưa có trên Firestore — tạo mới.")
                val initialData = hashMapOf(
                    "favoritePosts" to emptyList<String>(),
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
                userDoc.set(initialData).await()
            }

            // ✅ Cập nhật thích / bỏ thích
            if (isFavorite) {
                userDoc.update("favoritePosts", FieldValue.arrayUnion(postId)).await()
            } else {
                userDoc.update("favoritePosts", FieldValue.arrayRemove(postId)).await()
            }

            Log.d("FavoriteRepo", "🔥 Cập nhật favoritePosts thành công cho user $userId → $isFavorite ($postId)")
        } catch (e: Exception) {
            Log.e("FavoriteRepo", "❌ Lỗi cập nhật favorite: $e")
        }
    }
}
