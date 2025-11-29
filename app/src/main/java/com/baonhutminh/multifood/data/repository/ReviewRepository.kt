package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.data.model.Review
import com.baonhutminh.multifood.domain.repository.ReviewRepository
import com.baonhutminh.multifood.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ReviewRepository {

    private val reviewCollection = db.collection("reviews")

    override suspend fun getAllReviews(): Resource<List<Review>> {
        return try {
            val snapshot = reviewCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val reviews = snapshot.toObjects(Review::class.java)
            Resource.Success(reviews)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi tải bài viết")
        }
    }

    override suspend fun toggleLikeReview(reviewId: String, userId: String, isCurrentlyLiked: Boolean): Resource<Boolean> {
        return try {
            val userRef = db.collection("users").document(userId)
            val reviewRef = db.collection("reviews").document(reviewId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(reviewRef)
                // Lấy số like hiện tại (đề phòng null thì lấy 0)
                val currentLikes = snapshot.getLong("likeCount") ?: 0

                if (isCurrentlyLiked) {
                    // Nếu đang Like -> Bấm phát nữa thành UNLIKE
                    transaction.update(userRef, "likedReviewIds", com.google.firebase.firestore.FieldValue.arrayRemove(reviewId))
                    transaction.update(reviewRef, "likeCount", currentLikes - 1)
                } else {
                    // Nếu chưa Like -> Bấm thành LIKE
                    transaction.update(userRef, "likedReviewIds", com.google.firebase.firestore.FieldValue.arrayUnion(reviewId))
                    transaction.update(reviewRef, "likeCount", currentLikes + 1)
                }
            }.await()

            Resource.Success(!isCurrentlyLiked) // Trả về trạng thái mới
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi thao tác like")
        }
    }
    override suspend fun getReviewById(reviewId: String): Resource<Review> {
        return try {
            val snapshot = db.collection("reviews").document(reviewId).get().await()
            val review = snapshot.toObject(Review::class.java)
            if (review != null) {
                Resource.Success(review)
            } else {
                Resource.Error("Bài viết không tồn tại")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi tải chi tiết bài viết")
        }
    }

    override suspend fun createReview(review: Review): Resource<String> {
        return try {
            val document = reviewCollection.document()
            val timestamp = System.currentTimeMillis()
            val payload = review.copy(
                id = document.id,
                createdAt = timestamp,
                updatedAt = timestamp
            )
            document.set(payload).await()
            Resource.Success(document.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi tạo bài viết")
        }
    }
}