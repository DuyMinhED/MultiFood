package com.baonhutminh.multifood.util

import android.util.Log
import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

object DataSeeder {
    private val db = FirebaseFirestore.getInstance()

    // --- DỮ LIỆU MẪU (Mock Data) ---
    private val sampleNames = listOf("Bảo Minh", "Nhựt Hào", "Minh Tú", "Hồng Hạnh", "Thánh Ăn", "Food Boy", "Cô Ba Sài Gòn", "Chú Bảy Bình Dương")
    private val sampleAvatars = listOf(
        "https://i.pravatar.cc/150?img=1", "https://i.pravatar.cc/150?img=2",
        "https://i.pravatar.cc/150?img=3", "https://i.pravatar.cc/150?img=4"
    )
    private val samplePlaceNames = listOf("Phở Thìn", "Cơm Tấm Cali", "Bún Bò Gánh", "Pizza 4P's", "Highlands Coffee", "Phúc Long", "Bánh Mì Huỳnh Hoa", "Katinat")
    private val sampleAddresses = listOf("Quận 1, TP.HCM", "Quận 3, TP.HCM", "Lò Đúc, Hà Nội", "Thảo Điền, Quận 2", "Hải Châu, Đà Nẵng")
    private val sampleFoodImages = listOf(
        "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=500",
        "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=500",
        "https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?w=500",
        "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=500"
    )
    private val sampleReviews = listOf("Ngon tuyệt vời!", "Quán hơi đông nhưng đồ ăn ngon.", "Phục vụ chậm.", "Không gian đẹp, sống ảo tốt.", "Giá hơi chát nhưng đáng tiền.")

    // --- HÀM CHÍNH ĐỂ CHẠY ---
    fun seedAllData(onComplete: () -> Unit) {
        val batch = db.batch() // Dùng Batch để ghi 1 lần cho nhanh và an toàn

        val createdUsers = mutableListOf<User>()

        // 1. TẠO 10 USER GIẢ
        repeat(10) {
            val userRef = db.collection("users").document() // Tự sinh ID
            val user = User(
                id = userRef.id,
                name = sampleNames.random(),
                email = "user${Random.nextInt(1000)}@test.com",
                avatarUrl = sampleAvatars.random(),
                bio = "Người đam mê ăn uống",
                createdAt = System.currentTimeMillis()
            )
            batch.set(userRef, user)
            createdUsers.add(user)
        }

        // 2. TẠO 50 REVIEW
        repeat(50) {
            val reviewRef = db.collection("reviews").document()

            // Lấy ngẫu nhiên User
            val randomUser = createdUsers.random()

            val review = Post(
                id = reviewRef.id,
                userId = randomUser.id,
                rating = Random.nextInt(3, 6), // 3 đến 5 sao
                content = sampleReviews.random(),
                imageUrls = listOf(sampleFoodImages.random()), // Mỗi review 1 ảnh

                // CACHE DATA (Lấy từ User)
                userName = randomUser.name,
                userAvatarUrl = randomUser.avatarUrl,
                placeName = samplePlaceNames.random(),
                placeAddress = sampleAddresses.random(),

                createdAt = System.currentTimeMillis() - Random.nextLong(0, 100000000) // Thời gian ngẫu nhiên trong quá khứ
            )
            batch.set(reviewRef, review)
        }

        // 4. COMMIT (Đẩy lên Firebase)
        batch.commit()
            .addOnSuccessListener {
                Log.d("DataSeeder", "Đã tạo dữ liệu mẫu thành công!")
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e("DataSeeder", "Lỗi tạo dữ liệu", e)
            }
    }
}