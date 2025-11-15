package com.baonhutminh.multifood.data.repository

import android.util.Log
import com.baonhutminh.multifood.data.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class PostRepository_Firebase {

    private val firestore = FirebaseFirestore.getInstance()
    private val postsCollection = firestore.collection("posts")
    private var listenerRegistration: ListenerRegistration? = null

    /**
     * Lấy dữ liệu 1 lần (nếu không cần realtime)
     */
    suspend fun getPosts(): List<Post> {
        return try {
            val snapshot = postsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                parsePost(doc.data, doc.id)
            }
        } catch (e: Exception) {
            Log.e("PostRepoFirebase", "Lỗi getPosts: $e")
            emptyList()
        }
    }

    /**
     * Quan sát realtime thay đổi trong collection "posts"
     */
    fun observePosts(onChange: (List<Post>) -> Unit) {
        listenerRegistration?.remove() // tránh trùng listener

        listenerRegistration = postsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("PostRepoFirebase", "Lỗi listener Firestore: $error")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val posts = snapshot.documents.mapNotNull { doc ->
                        parsePost(doc.data, doc.id)
                    }
                    onChange(posts)
                }
            }
    }

    /**
     * Dừng lắng nghe realtime (gọi khi ViewModel bị huỷ)
     */
    fun removeListener() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    /**
     * Hàm parse dữ liệu từ Firestore document -> Post model
     */
    private fun parsePost(data: Map<String, Any>?, docId: String): Post? {
        return try {
            if (data == null) return null
            Post(
                id = data["id"] as? String ?: docId,
                title = data["title"] as? String ?: "(Không tiêu đề)",
                content = data["content"] as? String ?: "",
                rating = (data["rating"] as? Double ?: 0.0).toFloat(),
                address = data["address"] as? String ?: "",
                imageUrls = data["imageUrls"] as? List<String> ?: emptyList(),
                isFavorite = data["isFavorite"] as? Boolean ?: false
            )
        } catch (e: Exception) {
            Log.e("PostRepoFirebase", "Lỗi parsePost($docId): $e")
            null
        }
    }
}
