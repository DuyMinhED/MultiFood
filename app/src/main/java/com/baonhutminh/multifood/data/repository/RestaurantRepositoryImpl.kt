package com.baonhutminh.multifood.data.repository

import android.util.Log
import com.baonhutminh.multifood.data.local.RestaurantDao
import com.baonhutminh.multifood.data.model.RestaurantEntity
import com.baonhutminh.multifood.common.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.CancellationException
import javax.inject.Inject

class RestaurantRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val restaurantDao: RestaurantDao,
    private val auth: FirebaseAuth
) : RestaurantRepository {

    private val restaurantsCollection = firestore.collection("restaurants")
    
    /**
     * Normalize string để so sánh: trim, lowercase, remove extra spaces
     */
    private fun normalizeString(str: String): String {
        return str.trim().lowercase().replace(Regex("\\s+"), " ")
    }
    
    /**
     * Kiểm tra xem hai restaurant có giống nhau không (sau khi normalize)
     */
    private fun isRestaurantMatch(restaurant: RestaurantEntity, name: String, address: String): Boolean {
        val normalizedRestaurantName = normalizeString(restaurant.name)
        val normalizedRestaurantAddress = normalizeString(restaurant.address)
        val normalizedName = normalizeString(name)
        val normalizedAddress = normalizeString(address)
        
        // Khớp nếu tên giống nhau và địa chỉ giống nhau (sau khi normalize)
        return normalizedRestaurantName == normalizedName && 
               normalizedRestaurantAddress == normalizedAddress
    }

    override suspend fun findOrCreateRestaurant(name: String, address: String): Resource<String> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        
        return try {
            // Bước 1: Tìm trong Room trước (nhanh hơn và không cần permission)
            val roomResults = restaurantDao.searchRestaurants(name).first()
            val existingRestaurantInRoom = roomResults.firstOrNull { 
                isRestaurantMatch(it, name, address)
            }
            
            val restaurantId = if (existingRestaurantInRoom != null) {
                // Restaurant đã tồn tại trong Room
                existingRestaurantInRoom.id
            } else {
                // Bước 2: Tìm trong Firestore (có thể đã tồn tại nhưng chưa sync vào Room)
                val firestoreSearchResult = searchRestaurantsInFirestore(name, address)
                val existingRestaurantInFirestore = when (firestoreSearchResult) {
                    is Resource.Success -> {
                        firestoreSearchResult.data?.firstOrNull { restaurant ->
                            isRestaurantMatch(restaurant, name, address)
                        }
                    }
                    else -> null
                }
                
                if (existingRestaurantInFirestore != null) {
                    // Restaurant đã tồn tại trong Firestore, sync vào Room
                    restaurantDao.upsert(existingRestaurantInFirestore)
                    existingRestaurantInFirestore.id
                } else {
                    // Bước 3: Tạo restaurant mới trong Firestore
                    val newRestaurantRef = restaurantsCollection.document()
                    val newRestaurant = mapOf(
                        "name" to name,
                        "address" to address,
                        "lat" to 0.0,
                        "lng" to 0.0,
                        "phone" to null,
                        "coverImageUrl" to null,
                        "priceRange" to null,
                        "cuisineTypes" to emptyList<String>(),
                        "totalRatingPoints" to 0.0,
                        "reviewCount" to 0L,
                        "averageRating" to 0.0,
                        "createdBy" to currentUser.uid,
                        "createdAt" to Date()
                    )
                    newRestaurantRef.set(newRestaurant).await()
                    val newId = newRestaurantRef.id
                    
                    // Lưu vào Room ngay lập tức
                    val restaurantEntity = RestaurantEntity(
                        id = newId,
                        name = name,
                        address = address,
                        lat = 0.0,
                        lng = 0.0,
                        phone = null,
                        coverImageUrl = null,
                        priceRange = null,
                        cuisineTypes = emptyList(),
                        totalRatingPoints = 0.0,
                        reviewCount = 0L,
                        averageRating = 0.0,
                        createdBy = currentUser.uid,
                        createdAt = Date()
                    )
                    restaurantDao.upsert(restaurantEntity)
                    
                    newId
                }
            }

            Resource.Success(restaurantId)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("RestaurantRepositoryImpl", "Error finding or creating restaurant", e)
            Resource.Error("Lỗi tìm hoặc tạo nhà hàng: ${e.message}")
        }
    }

    override fun getRestaurantById(restaurantId: String): Flow<Resource<RestaurantEntity?>> {
        return restaurantDao.getRestaurantById(restaurantId).flatMapLatest { restaurant ->
            if (restaurant != null) {
                flow { emit(Resource.Success(restaurant)) }
            } else {
                // Nếu chưa có trong Room, fetch từ Firestore
                flow {
                    try {
                        val doc = restaurantsCollection.document(restaurantId).get().await()
                        if (doc.exists()) {
                            val firestoreRestaurant = doc.toObject(RestaurantEntity::class.java)
                            if (firestoreRestaurant != null) {
                                val restaurantEntity = firestoreRestaurant.copy(id = restaurantId)
                                restaurantDao.upsert(restaurantEntity)
                                emit(Resource.Success(restaurantEntity))
                            } else {
                                emit(Resource.Success(null))
                            }
                        } else {
                            emit(Resource.Success(null))
                        }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        Log.e("RestaurantRepositoryImpl", "Error fetching restaurant from Firestore: $restaurantId", e)
                        emit(Resource.Error(e.message ?: "Không thể tải thông tin nhà hàng"))
                    }
                }
            }
        }
    }

    override suspend fun refreshRestaurant(restaurantId: String): Resource<Unit> {
        return try {
            val doc = restaurantsCollection.document(restaurantId).get().await()
            if (doc.exists()) {
                val restaurant = doc.toObject(RestaurantEntity::class.java)
                if (restaurant != null) {
                    restaurantDao.upsert(restaurant.copy(id = restaurantId))
                }
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("RestaurantRepositoryImpl", "Error refreshing restaurant", e)
            Resource.Error(e.message ?: "Lỗi làm mới thông tin nhà hàng")
        }
    }

    override fun searchRestaurants(query: String): Flow<Resource<List<RestaurantEntity>>> {
        return restaurantDao.searchRestaurants(query).map { restaurants ->
            Resource.Success(restaurants)
        }
    }
    
    override suspend fun searchRestaurantsInFirestore(name: String, address: String): Resource<List<RestaurantEntity>> {
        return try {
            // Query Firestore: tìm restaurants có tên bắt đầu với name hoặc địa chỉ bắt đầu với address
            // Lưu ý: Firestore queries là case-sensitive, nên ta query với giá trị gốc
            // Sau đó filter bằng normalized strings ở client side để tìm matches chính xác hơn
            
            val normalizedName = normalizeString(name)
            val normalizedAddress = normalizeString(address)
            
            // Query theo tên (prefix match) - case-insensitive bằng cách query cả lowercase và uppercase
            val nameQueries = if (name.isNotBlank()) {
                val nameLower = name.lowercase()
                val nameUpper = name.uppercase()
                val nameCapitalized = name.lowercase().replaceFirstChar { it.uppercaseChar() }
                
                // Query với nhiều variants để tăng khả năng tìm thấy (case-insensitive)
                listOfNotNull(
                    try {
                        restaurantsCollection
                            .whereGreaterThanOrEqualTo("name", nameLower)
                            .whereLessThanOrEqualTo("name", nameLower + "\uf8ff")
                            .limit(30)
                            .get()
                            .await()
                    } catch (e: Exception) {
                        null
                    },
                    if (nameLower != nameUpper) {
                        try {
                            restaurantsCollection
                                .whereGreaterThanOrEqualTo("name", nameUpper)
                                .whereLessThanOrEqualTo("name", nameUpper + "\uf8ff")
                                .limit(30)
                                .get()
                                .await()
                        } catch (e: Exception) {
                            null
                        }
                    } else null,
                    if (nameCapitalized != nameLower && nameCapitalized != nameUpper) {
                        try {
                            restaurantsCollection
                                .whereGreaterThanOrEqualTo("name", nameCapitalized)
                                .whereLessThanOrEqualTo("name", nameCapitalized + "\uf8ff")
                                .limit(30)
                                .get()
                                .await()
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                )
            } else {
                emptyList()
            }
            
            // Query theo địa chỉ (prefix match) - case-insensitive
            val addressQueries = if (address.isNotBlank()) {
                val addressLower = address.lowercase()
                val addressUpper = address.uppercase()
                
                listOfNotNull(
                    try {
                        restaurantsCollection
                            .whereGreaterThanOrEqualTo("address", addressLower)
                            .whereLessThanOrEqualTo("address", addressLower + "\uf8ff")
                            .limit(30)
                            .get()
                            .await()
                    } catch (e: Exception) {
                        null
                    },
                    if (addressLower != addressUpper) {
                        try {
                            restaurantsCollection
                                .whereGreaterThanOrEqualTo("address", addressUpper)
                                .whereLessThanOrEqualTo("address", addressUpper + "\uf8ff")
                                .limit(30)
                                .get()
                                .await()
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                )
            } else {
                emptyList()
            }
            
            // Merge và deduplicate kết quả từ tất cả queries
            val allDocs = mutableSetOf<String>()
            val restaurantsMap = mutableMapOf<String, RestaurantEntity>()
            
            // Process name queries
            nameQueries.forEach { querySnapshot ->
                querySnapshot?.documents?.forEach { doc ->
                    if (!allDocs.contains(doc.id)) {
                        allDocs.add(doc.id)
                        try {
                            val restaurant = doc.toObject(RestaurantEntity::class.java)
                            if (restaurant != null) {
                                restaurantsMap[doc.id] = restaurant.copy(id = doc.id)
                            }
                        } catch (e: Exception) {
                            Log.e("RestaurantRepositoryImpl", "Error parsing restaurant document ${doc.id}", e)
                        }
                    }
                }
            }
            
            // Process address queries
            addressQueries.forEach { querySnapshot ->
                querySnapshot?.documents?.forEach { doc ->
                    if (!allDocs.contains(doc.id)) {
                        allDocs.add(doc.id)
                        try {
                            val restaurant = doc.toObject(RestaurantEntity::class.java)
                            if (restaurant != null) {
                                restaurantsMap[doc.id] = restaurant.copy(id = doc.id)
                            }
                        } catch (e: Exception) {
                            Log.e("RestaurantRepositoryImpl", "Error parsing restaurant document ${doc.id}", e)
                        }
                    }
                }
            }
            
            // Filter bằng normalized strings để tìm matches chính xác hơn
            val restaurants = restaurantsMap.values.filter { restaurant ->
                val restaurantNameNormalized = normalizeString(restaurant.name)
                val restaurantAddressNormalized = normalizeString(restaurant.address)
                
                // Match nếu tên hoặc địa chỉ chứa normalized name/address
                (name.isNotBlank() && restaurantNameNormalized.contains(normalizedName)) ||
                (address.isNotBlank() && restaurantAddressNormalized.contains(normalizedAddress))
            }
            
            // Sync vào Room để cache
            if (restaurants.isNotEmpty()) {
                restaurantDao.upsertAll(restaurants)
            }
            
            Resource.Success(restaurants)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("RestaurantRepositoryImpl", "Error searching restaurants in Firestore", e)
            // Nếu lỗi permission hoặc network, trả về empty list thay vì error
            // để app vẫn có thể hoạt động (sẽ tạo restaurant mới)
            Resource.Success(emptyList())
        }
    }
}

