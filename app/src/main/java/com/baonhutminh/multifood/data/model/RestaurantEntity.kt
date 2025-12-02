package com.baonhutminh.multifood.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

/**
 * Đại diện cho một nhà hàng/quán ăn được lưu trữ cục bộ trong Room.
 * Dữ liệu này được cache từ Firestore để hiển thị nhanh và offline.
 */
@Entity(tableName = "restaurants")
@TypeConverters(Converters::class)
data class RestaurantEntity(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val phone: String? = null,
    val coverImageUrl: String? = null,
    val priceRange: String? = null, // "", "$", "$$", "$$$"
    val cuisineTypes: List<String> = emptyList(),
    val totalRatingPoints: Double = 0.0,
    val reviewCount: Long = 0L,
    val averageRating: Double = 0.0,
    val createdBy: String = "",
    val createdAt: Date? = null
)

