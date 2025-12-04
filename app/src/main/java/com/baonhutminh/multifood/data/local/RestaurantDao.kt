package com.baonhutminh.multifood.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.baonhutminh.multifood.data.model.RestaurantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {

    @Upsert
    suspend fun upsert(restaurant: RestaurantEntity)

    @Upsert
    suspend fun upsertAll(restaurants: List<RestaurantEntity>)

    @Query("SELECT * FROM restaurants WHERE id = :restaurantId")
    fun getRestaurantById(restaurantId: String): Flow<RestaurantEntity?>

    @Query("SELECT * FROM restaurants ORDER BY averageRating DESC, reviewCount DESC")
    fun getAllRestaurants(): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%'")
    fun searchRestaurants(query: String): Flow<List<RestaurantEntity>>

    @Query("DELETE FROM restaurants WHERE id = :restaurantId")
    suspend fun delete(restaurantId: String)

    @Query("DELETE FROM restaurants")
    suspend fun clearAll()
}


