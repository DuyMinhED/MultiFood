package com.baonhutminh.multifood.domain.repository

import com.baonhutminh.multifood.data.model.Review
import com.baonhutminh.multifood.util.Resource

interface ReviewRepository {
    suspend fun getAllReviews(): Resource<List<Review>>
    suspend fun toggleLikeReview(
        reviewId: String,
        userId: String,
        isCurrentlyLiked: Boolean
    ): Resource<Boolean>

    suspend fun getReviewById(reviewId: String): Resource<Review>
    suspend fun createReview(review: Review): Resource<String>
}

