# MultiFood - Các Công Việc Tiếp Theo

## 📋 Tổng Quan Dự Án

**MultiFood** là ứng dụng Android review đồ ăn/nhà hàng được xây dựng với:
- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM + Repository Pattern
- **DI**: Hilt
- **Database**: Room (local) + Firebase Firestore (cloud)
- **Auth**: Firebase Authentication
- **Storage**: Firebase Storage

## 🎯 Các Tính Năng Hiện Có

### ✅ Đã Hoàn Thành
- [x] Đăng nhập/Đăng ký với Firebase Authentication
- [x] Quên mật khẩu (gửi email reset)
- [x] Xem danh sách bài đăng (tất cả, của tôi, đã thích)
- [x] Trang Profile cơ bản (xem/sửa tên, bio, avatar)
- [x] Cài đặt ứng dụng (theme màu sắc, dark mode, thông báo)
- [x] Đổi mật khẩu
- [x] Xem điều khoản sử dụng và chính sách bảo mật
- [x] Bottom navigation bar
- [x] Hỗ trợ đa theme (Orange, Blue, Green, Pink)

### 🔲 Cần Hoàn Thiện (Có Code Nhưng Chưa Hoàn Chỉnh)
- [ ] **Màn hình Detail Post** - Route đã có nhưng UI chưa được implement (`Screen.Detail`)
- [ ] **Màn hình Create Post** - Route đã có nhưng UI chưa được implement (`Screen.CreatePost`)
- [ ] **Chức năng Like bài đăng** - UI có nút like nhưng `onLikeClick` chưa xử lý
- [ ] **Hiển thị trạng thái liked** - `isLiked` luôn là `false` trong HomeScreen

---

## 🚀 Công Việc Ưu Tiên Cao (Priority 1)

### 1. Hoàn thiện màn hình Chi tiết Bài Đăng (PostDetailScreen)
**File cần tạo/sửa**: `ui/screens/PostDetailScreen.kt`

**Yêu cầu**:
- Hiển thị đầy đủ thông tin bài đăng (tiêu đề, nội dung, hình ảnh, rating)
- Hiển thị thông tin nhà hàng/địa điểm
- Hiển thị danh sách bình luận
- Thêm chức năng thêm bình luận
- Nút like/unlike
- Nút chia sẻ (optional)

**ViewModel**: Sử dụng `PostDetailViewModel.kt` (đã có)

### 2. Hoàn thiện màn hình Tạo Bài Đăng (CreatePostScreen)
**File cần tạo**: `ui/screens/CreatePostScreen.kt`

**Yêu cầu**:
- Form nhập tiêu đề bài viết
- Form nhập nội dung review
- Chọn/chụp ảnh từ gallery hoặc camera
- Rating (sao) cho địa điểm
- Nhập tên nhà hàng/địa điểm
- Nhập địa chỉ
- Nhập giá trung bình/người
- Nút đăng bài

**ViewModel cần tạo**: `CreatePostViewModel.kt`

### 3. Hoàn thiện chức năng Like bài đăng
**Files cần sửa**:
- `ProfileRepository.kt` - thêm method `toggleLike(postId: String)`
- `ProfileRepositoryImpl.kt` - implement method
- `HomeViewModel.kt` - thêm function xử lý like
- `HomeScreen.kt` - kết nối UI với ViewModel

---

## 📝 Công Việc Ưu Tiên Trung Bình (Priority 2)

### 4. Thêm chức năng Tìm Kiếm
**Files cần tạo**:
- `ui/screens/SearchScreen.kt`
- `viewmodel/SearchViewModel.kt`

**Yêu cầu**:
- Thanh tìm kiếm
- Tìm theo tên nhà hàng, địa điểm
- Lọc theo rating
- Lịch sử tìm kiếm (đã có field `recentSearchKeywords` trong User model)

### 5. Thêm chức năng Bookmark/Lưu bài viết
**Yêu cầu**:
- Nút bookmark trên mỗi bài đăng
- Tab "Đã lưu" trong ProfileScreen
- Lưu trữ trong `bookmarkedPostIds` (đã có trong User model)

### 6. Thêm chức năng Follow người dùng
**Yêu cầu**:
- Xem profile người dùng khác
- Nút follow/unfollow
- Danh sách followers/following
- Tab "Đang theo dõi" hiển thị bài từ người follow

### 7. Cải thiện UI/UX
- [ ] Thêm Pull-to-refresh cho danh sách bài đăng
- [ ] Thêm loading skeleton khi tải dữ liệu
- [ ] Thêm empty state khi không có bài đăng
- [ ] Thêm error state với nút retry
- [ ] Thêm animation chuyển trang

---

## 🔧 Công Việc Ưu Tiên Thấp (Priority 3)

### 8. Thêm Notification System
- Push notification cho like/comment mới
- Notification khi có người follow
- Tích hợp Firebase Cloud Messaging

### 9. Thêm tính năng Map/Location
- Tích hợp Google Maps
- Hiển thị vị trí nhà hàng trên bản đồ
- Tìm nhà hàng gần vị trí hiện tại

### 10. Thêm tính năng Report/Moderation
- Report bài viết không phù hợp
- Report bình luận
- Flagged content management

### 11. Offline Support Enhancement
- Sync data khi có internet
- Queue actions khi offline
- Conflict resolution

### 12. Thêm Unit Tests
**Files cần tạo**:
- Tests cho ViewModels
- Tests cho Repositories
- UI tests với Compose Testing

---

## 🐛 Bugs/Issues Cần Sửa

### Lỗi nhỏ
- [x] ~~`_selectehome` trong `ProfileScreen.kt` - typo trong tên biến~~ (Đã sửa thành `isHomeSelected`)
- [ ] Validation thiếu cho một số trường nhập liệu

### Cải tiến code
- [ ] Thêm handling error chi tiết hơn trong Repository
- [ ] Thêm logging cho debugging
- [ ] Clean up unused imports

---

## 📁 Cấu Trúc Files Gợi Ý Cho Tính Năng Mới

```
app/src/main/java/com/baonhutminh/multifood/
├── data/
│   ├── model/
│   │   └── SearchHistory.kt (nếu cần)
│   └── repository/
│       ├── SearchRepository.kt
│       └── SearchRepositoryImpl.kt
├── ui/
│   └── screens/
│       ├── PostDetailScreen.kt ⭐ (Cần tạo)
│       ├── CreatePostScreen.kt ⭐ (Cần tạo)
│       └── SearchScreen.kt
└── viewmodel/
    ├── CreatePostViewModel.kt ⭐ (Cần tạo)
    └── SearchViewModel.kt
```

---

## 📌 Ghi Chú

1. **Firebase Collections đã có**:
   - `users` - Thông tin người dùng
   - `posts` - Bài đăng
   - `comments` - Bình luận

2. **Room Tables đã có**:
   - `user_profiles`
   - `posts`
   - `comments`

3. **Dependency Injection**: Sử dụng Hilt, cần thêm `@Inject` và `@HiltViewModel` cho các class mới

4. **Navigation**: Sử dụng Navigation Compose, routes định nghĩa trong `Screen.kt`

---

## 🎯 Lộ Trình Đề Xuất

### Sprint 1 (1-2 tuần)
- Hoàn thiện PostDetailScreen
- Hoàn thiện CreatePostScreen
- Fix chức năng Like

### Sprint 2 (1-2 tuần)
- Thêm SearchScreen
- Thêm chức năng Bookmark
- Cải thiện UI/UX cơ bản

### Sprint 3 (2-3 tuần)
- Thêm Follow system
- Thêm Notification
- Thêm Map integration

### Sprint 4 (Ongoing)
- Unit tests
- Performance optimization
- Bug fixes

---

*Cập nhật lần cuối: Tháng 12, 2024*
