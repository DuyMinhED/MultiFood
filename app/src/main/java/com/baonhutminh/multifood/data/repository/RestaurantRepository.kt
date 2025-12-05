package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.data.model.RestaurantEntity
import com.baonhutminh.multifood.common.Resource
import kotlinx.coroutines.flow.Flow

interface RestaurantRepository {
    
    /**
     * Tìm hoặc tạo restaurant dựa trên tên và địa chỉ.
     * Trả về restaurantId nếu thành công.
     */
    suspend fun findOrCreateRestaurant(name: String, address: String): Resource<String>
    
    /**
     * Lấy thông tin restaurant theo ID
     */
    fun getRestaurantById(restaurantId: String): Flow<Resource<RestaurantEntity?>>
    
    /**
     * Làm mới thông tin restaurant từ Firestore
     */
    suspend fun refreshRestaurant(restaurantId: String): Resource<Unit>
    
    /**
     * Tìm kiếm restaurants theo query
     */
    fun searchRestaurants(query: String): Flow<Resource<List<RestaurantEntity>>>
    
    /**
     * Tìm kiếm restaurants trong Firestore theo tên hoặc địa chỉ
     * Sử dụng để tìm restaurants có thể đã tồn tại nhưng chưa có trong Room
     */
    suspend fun searchRestaurantsInFirestore(name: String, address: String): Resource<List<RestaurantEntity>>
}

