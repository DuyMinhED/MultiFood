# ĐÁNH GIÁ RỦI RO VÀ CÁC VẤN ĐỀ DỰ ÁN MULTIFOOD

**Ngày đánh giá:** $(date)  
**Người đánh giá:** AI Assistant  
**Phiên bản dự án:** 1.0

---

## 📋 TỔNG QUAN DỰ ÁN

### Mô tả
Ứng dụng Android mạng xã hội chia sẻ trải nghiệm quán ăn/nhà hàng

### Kiến trúc
- **Pattern:** MVVM + Repository Pattern
- **UI:** 100% Jetpack Compose
- **Local DB:** Room Database
- **Backend:** Firebase (Auth, Firestore, Storage)
- **DI:** Hilt
- **Async:** Coroutines + Flow

### Điểm mạnh
✅ Kiến trúc rõ ràng, tách bạch các layer  
✅ Sử dụng Flow cho reactive data  
✅ Có cache local (Room) để hỗ trợ offline  
✅ Có error handling cơ bản  
✅ Sử dụng Hilt cho dependency injection  

---

## 🚨 CÁC NGUY CƠ LỖI ĐANG CÓ VÀ CÓ THỂ XẢY RA

### 🔴 **CRITICAL - CẦN SỬA NGAY**

#### 1. Database Migration - Mất dữ liệu khi thay đổi schema

**File:** `app/src/main/java/com/baonhutminh/multifood/di/DatabaseModule.kt:33`

**Vấn đề:**
```kotlin
.fallbackToDestructiveMigration()
```
Đang sử dụng `fallbackToDestructiveMigration()` sẽ xóa toàn bộ dữ liệu khi schema thay đổi.

**Hậu quả:**
- ❌ Mất toàn bộ dữ liệu local khi nâng version database
- ❌ Không phù hợp cho production

**Giải pháp:**
- Viết migration thủ công cho từng version database
- Sử dụng `Migration` objects thay vì `fallbackToDestructiveMigration()`

---

#### 2. Cache Fields trong PostEntity không được populate

**File:** `app/src/main/java/com/baonhutminh/multifood/data/repository/PostRepositoryImpl.kt:181-201`

**Vấn đề:**
```kotlin
fun Post.toEntity(): PostEntity {
    return PostEntity(
        // ...
        // Các trường cache sẽ được populate khi có thông tin đầy đủ từ User và Restaurant
        userName = "",
        userAvatarUrl = "",
        restaurantName = "",
        restaurantAddress = ""
    )
}
```
Các trường cache (`userName`, `userAvatarUrl`, `restaurantName`, `restaurantAddress`) luôn là empty string.

**Hậu quả:**
- ❌ UI hiển thị thiếu thông tin tác giả và nhà hàng
- ❌ Phải join UserProfile và RestaurantEntity mỗi lần hiển thị
- ❌ Mất mục đích của việc cache

**Giải pháp:**
- Populate các trường này khi sync từ Firestore
- Fetch User và Restaurant data khi refresh posts
- Hoặc sử dụng Room relations thay vì cache fields

---

#### 3. Lỗi Logic trong uploadPostImage - Gọi downloadUrl.await() 2 lần

**File:** `app/src/main/java/com/baonhutminh/multifood/data/repository/PostRepositoryImpl.kt:147-160`

**Vấn đề:**
```kotlin
storageRef.putFile(imageUri).await()
storageRef.downloadUrl.await().toString()  // Dòng 153: Gọi nhưng không dùng
Resource.Success(storageRef.downloadUrl.await().toString())  // Dòng 154: Gọi lại lần 2
```

**Hậu quả:**
- ❌ Tăng thời gian xử lý không cần thiết
- ❌ Có thể gây lỗi nếu URL thay đổi giữa 2 lần gọi

**Giải pháp:**
```kotlin
val downloadUrl = storageRef.downloadUrl.await().toString()
Resource.Success(downloadUrl)
```

---

#### 4. Race Condition khi Toggle Like

**File:** `app/src/main/java/com/baonhutminh/multifood/data/repository/ProfileRepositoryImpl.kt:61-91`

**Vấn đề:**
`isCurrentlyLiked` có thể đã thay đổi giữa lúc check và thực thi batch write.

**Hậu quả:**
- ❌ Like/unlike không nhất quán
- ❌ Có thể tạo duplicate hoặc xóa nhầm

**Giải pháp:**
- Sử dụng Firestore Transaction thay vì batch write
- Hoặc kiểm tra lại trạng thái trước khi thực thi

---

### 🟠 **HIGH - NÊN SỬA SỚM**

#### 5. Không có Pagination cho Posts và Comments

**File:** `app/src/main/java/com/baonhutminh/multifood/data/repository/PostRepositoryImpl.kt:60-79`

**Vấn đề:**
Load tất cả posts/comments một lúc không có giới hạn.

**Hậu quả:**
- ❌ Chậm khi có nhiều dữ liệu
- ❌ Tốn băng thông và bộ nhớ
- ❌ Firestore có giới hạn 1MB/query

**Giải pháp:**
- Thêm pagination với `startAfter()` và `limit()`
- Implement infinite scroll trong UI

---

#### 6. Like System phức tạp - 2 Collections

**File:** `app/src/main/java/com/baonhutminh/multifood/data/repository/ProfileRepositoryImpl.kt:61-91`

**Vấn đề:**
Like được lưu ở 2 nơi:
- Root collection `likes`
- Sub-collection `posts/{postId}/likes`

**Hậu quả:**
- ❌ Khó đồng bộ
- ❌ Tốn tài nguyên
- ❌ Dễ lệch dữ liệu

**Giải pháp:**
- Chọn 1 cách lưu (khuyến nghị: chỉ dùng sub-collection)
- Hoặc dùng Cloud Function để sync tự động

---

#### 7. Firestore Rules có thể chặn Batch Writes

**File:** `firestore.rules:53-60`

**Vấn đề:**
Khi tạo post mới trong batch write, rule check `!exists()` có thể không đúng với batch write.

**Hậu quả:**
- ❌ Có thể bị từ chối khi tạo post mới kèm images

**Giải pháp:**
- Điều chỉnh rule để hỗ trợ batch write
- Hoặc tách thành 2 bước: tạo post trước, sau đó thêm images

---

#### 8. Clear All Images khi Refresh - Mất dữ liệu tạm thời

**File:** `app/src/main/java/com/baonhutminh/multifood/data/repository/PostRepositoryImpl.kt:67-71`

**Vấn đề:**
```kotlin
postImageDao.clearAll()
for (post in postDTOs) {
    syncPostImages(post.id)
}
```

**Hậu quả:**
- ❌ UI có thể hiển thị thiếu ảnh trong lúc sync
- ❌ Nếu sync lỗi, mất toàn bộ ảnh

**Giải pháp:**
- Sử dụng upsert thay vì clear + insert
- Hoặc sync từng post và xóa những ảnh không còn tồn tại

---

### 🟡 **MEDIUM - NÊN CẢI THIỆN**

#### 9. Không có Retry Mechanism

**Vấn đề:**
Khi network lỗi, không có retry tự động.

**Hậu quả:**
- ❌ Trải nghiệm kém khi mạng yếu
- ❌ Mất dữ liệu nếu upload thất bại

**Giải pháp:**
- Thêm retry với exponential backoff
- Sử dụng WorkManager cho background tasks

---

#### 10. Restaurant Search không chính xác do Case-Sensitive

**File:** `app/src/main/java/com/baonhutminh/multifood/data/repository/RestaurantRepositoryImpl.kt:166-175`

**Vấn đề:**
Firestore query case-sensitive, nhưng normalize ở client side.

**Hậu quả:**
- ❌ Có thể bỏ sót kết quả
- ❌ Kết quả không nhất quán

**Giải pháp:**
- Lưu thêm field normalized trong Firestore
- Hoặc sử dụng Algolia/Elasticsearch cho search

---

#### 11. Không có Validation cho Input Data

**Vấn đề:**
Không validate:
- Title/content rỗng hoặc quá dài
- Rating ngoài 0-5
- Price âm
- Image size/format

**Hậu quả:**
- ❌ Dữ liệu không hợp lệ
- ❌ Tốn tài nguyên

**Giải pháp:**
- Thêm validation ở ViewModel và Repository
- Validate ở UI level

---

#### 12. Thiếu Offline Sync Strategy

**Vấn đề:**
Không có cơ chế sync khi online lại sau khi offline.

**Hậu quả:**
- ❌ Dữ liệu không đồng bộ
- ❌ Mất thay đổi khi offline

**Giải pháp:**
- Sử dụng WorkManager để sync định kỳ
- Implement queue cho các thao tác cần sync

---

### 🟢 **LOW - TỐI ƯU**

#### 13. Thiếu Error Handling cho một số Edge Cases

**Vấn đề:**
Một số trường hợp chưa xử lý:
- User bị xóa nhưng vẫn có posts
- Restaurant bị xóa nhưng vẫn có posts
- Image URL không hợp lệ

**Giải pháp:**
- Thêm fallback và error handling rõ ràng
- Sử dụng default values

---

#### 14. PostImageDao.getImagesForPost() có thể null

**File:** `app/src/main/java/com/baonhutminh/multifood/viewmodel/CreatePostViewModel.kt:103-105`

**Vấn đề:**
```kotlin
originalImageUrls = postImageDao.getImagesForPost(postId)
    .first()
    .map { it.url }
```

**Hậu quả:**
- ❌ Có thể gây lỗi nếu xử lý không đúng

**Giải pháp:**
- Kiểm tra null/empty trước khi map
- Sử dụng safe call operators

---

## 📊 TÓM TẮT MỨC ĐỘ NGHIÊM TRỌNG

| Mức độ | Số lượng | Ưu tiên |
|--------|----------|---------|
| 🔴 Critical | 4 | Sửa ngay |
| 🟠 High | 4 | Sửa sớm |
| 🟡 Medium | 4 | Cải thiện |
| 🟢 Low | 2 | Tối ưu |

**Tổng cộng:** 14 vấn đề

---

## 📝 GHI CHÚ THÊM

### Các vấn đề đã được xác nhận trong code:
- ✅ Database version: 11
- ✅ Đang sử dụng `fallbackToDestructiveMigration()`
- ✅ Cache fields trong PostEntity không được populate
- ✅ Upload image có lỗi logic
- ✅ Không có pagination
- ✅ Like system phức tạp với 2 collections

### Các vấn đề cần kiểm tra thêm:
- ⚠️ Cloud Functions có tồn tại và hoạt động đúng không?
- ⚠️ Firestore indexes đã được tạo chưa?
- ⚠️ ProGuard rules đã đầy đủ chưa?

---

## 🎯 KHUYẾN NGHỊ HÀNH ĐỘNG

### Ngay lập tức (Tuần này):
1. ✅ Sửa lỗi upload image (gọi downloadUrl 2 lần)
2. ✅ Populate cache fields trong PostEntity
3. ✅ Viết migration cho database version 11

### Trong tháng này:
4. ✅ Thêm pagination cho posts và comments
5. ✅ Đơn giản hóa like system
6. ✅ Sửa Firestore rules cho batch writes
7. ✅ Thêm retry mechanism

### Trong quý này:
8. ✅ Thêm validation cho input
9. ✅ Implement offline sync strategy
10. ✅ Cải thiện restaurant search

---

**Lần cập nhật cuối:** $(date)  
**Trạng thái:** Đang theo dõi

