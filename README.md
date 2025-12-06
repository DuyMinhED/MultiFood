# MultiFood 🍜

Ứng dụng mạng xã hội chia sẻ trải nghiệm quán ăn/nhà hàng được xây dựng bằng **Kotlin 2.0** và **Jetpack Compose**.

## 📱 Giới thiệu

MultiFood là nền tảng cho phép người dùng:
- Chia sẻ đánh giá và trải nghiệm về các quán ăn/nhà hàng
- Khám phá các địa điểm ăn uống mới
- Theo dõi người dùng khác để xem các bài đánh giá của họ
- Tìm kiếm nhà hàng theo tên, địa chỉ, rating, giá cả
- Tương tác với bài viết (like, comment)

## 🏗️ Kiến trúc

Dự án tuân theo **Clean Architecture** với **MVVM** pattern:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (UI Screens, ViewModels, Components)  │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│          Domain Layer                    │
│  (Use Cases, Repository Interfaces)      │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│          Data Layer                     │
│  (Repositories, Data Sources, DAOs)     │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│      Remote (Firestore) + Local (Room)  │
└─────────────────────────────────────────┘
```

### Module Structure

Dự án được chia thành các module độc lập:

- **`:app`** - Application module chứa UI, ViewModels, và DI setup
- **`:common`** - Common utilities (Resource, StringUtils, RetryUtils)
- **`:core`** - Core functionality và base classes
- **`:data`** - Data layer (Repositories, DAOs, Models)
- **`:domain`** - Domain layer (Use Cases, Repository Interfaces)
- **`:design-system`** - Design system components và themes

## 🛠️ Công nghệ sử dụng

### Core
- **Kotlin 2.0.21** - Ngôn ngữ lập trình
- **Jetpack Compose** - UI framework (100% Compose, không XML)
- **Material Design 3** - Design system

### Architecture & DI
- **MVVM Pattern** - Kiến trúc presentation layer
- **Clean Architecture** - Phân tầng rõ ràng
- **Hilt** - Dependency Injection

### Data & Storage
- **Room Database** - Local database (SQLite)
- **Firebase Firestore** - Cloud database
- **Firebase Storage** - Lưu trữ ảnh
- **DataStore Preferences** - Lưu trữ settings

### Navigation
- **Jetpack Navigation Compose** - Navigation framework
- **Typed NavArgs** - Type-safe navigation arguments

### Async & Reactive
- **Kotlin Coroutines** - Asynchronous programming
- **StateFlow** - Reactive state management
- **SharedFlow** - Event handling

### Image Loading
- **Coil Compose** - Image loading library
- **Landscapist** - Placeholder/shimmer effects

### Firebase Services
- **Firebase Authentication** - User authentication (Email/Password, Google Sign-In)
- **Firebase Firestore** - NoSQL database
- **Firebase Storage** - File storage
- **Firebase Functions** - Serverless functions (auto-update counters)

### Testing
- **JUnit 5** - Unit testing framework
- **MockK** - Mocking library
- **Turbine** - Flow testing
- **Compose UI Testing** - UI testing

### Code Quality
- **ktlint** - Code style checker
- **KSP** - Kotlin Symbol Processing (thay thế KAPT)

## 📦 Yêu cầu hệ thống

- **Android Studio** Hedgehog (2023.1.1) trở lên
- **JDK 11** trở lên
- **Android SDK** 28 (Android 9.0) trở lên
- **Gradle** 8.13.1
- **Kotlin** 2.0.21
- **Node.js** 24 (cho Firebase Functions)

## 🚀 Hướng dẫn cài đặt

### 1. Clone repository

```bash
git clone <repository-url>
cd MultiFoods
```

### 2. Cấu hình Firebase

1. Tạo project mới trên [Firebase Console](https://console.firebase.google.com/)
2. Thêm Android app với package name: `com.baonhutminh.multifood`
3. Tải file `google-services.json` và đặt vào `app/`
4. Bật các services:
   - Authentication (Email/Password, Google Sign-In)
   - Firestore Database
   - Storage
   - Functions

### 3. Cấu hình Firestore Security Rules

Deploy Firestore rules:

```bash
firebase deploy --only firestore:rules
```

Hoặc copy nội dung từ `firestore.rules` vào Firebase Console.

### 4. Cấu hình Firebase Functions

```bash
cd functions
npm install
cd ..
```

Deploy functions:

```bash
firebase deploy --only functions
```

### 5. Build và chạy

```bash
./gradlew build
./gradlew installDebug
```

Hoặc mở project trong Android Studio và chạy trực tiếp.

## 📁 Cấu trúc thư mục

```
MultiFoods/
├── app/
│   ├── src/main/java/com/baonhutminh/multifood/
│   │   ├── data/              # Data layer
│   │   │   ├── local/         # Room DAOs, Database
│   │   │   ├── model/         # Data models, Entities
│   │   │   └── repository/    # Repository implementations
│   │   ├── di/                # Dependency Injection modules
│   │   ├── ui/                # UI layer
│   │   │   ├── components/    # Reusable components
│   │   │   ├── navigation/    # Navigation setup
│   │   │   ├── screens/       # Screen composables
│   │   │   └── theme/         # Theme, colors, typography
│   │   └── viewmodel/         # ViewModels
│   └── build.gradle.kts
├── common/                    # Common utilities
├── core/                      # Core functionality
├── data/                      # Data layer module
├── domain/                    # Domain layer module
├── design-system/             # Design system module
├── functions/                 # Firebase Cloud Functions
│   └── index.js
├── firestore.rules           # Firestore security rules
├── firebase.json             # Firebase configuration
└── build.gradle.kts
```

## 🎯 Tính năng chính

### Authentication
- ✅ Đăng ký/Đăng nhập với Email/Password
- ✅ Google Sign-In
- ✅ Onboarding screen cho người dùng mới

### Posts (Bài viết)
- ✅ Tạo bài viết với ảnh, đánh giá, giá cả
- ✅ Xem danh sách bài viết (Home feed)
- ✅ Xem chi tiết bài viết
- ✅ Chỉnh sửa/Xóa bài viết của mình
- ✅ Tìm kiếm bài viết theo tên, địa chỉ, rating, giá

### Restaurants (Nhà hàng)
- ✅ Tự động tìm hoặc tạo nhà hàng khi đăng bài
- ✅ Autocomplete khi nhập tên/địa chỉ nhà hàng
- ✅ Cache thông tin nhà hàng trong Room

### Interactions
- ✅ Like/Unlike bài viết
- ✅ Comment trên bài viết
- ✅ Like/Unlike comment
- ✅ Optimistic updates cho UX mượt mà

### User Profiles
- ✅ Xem profile của mình
- ✅ Xem profile người khác
- ✅ Follow/Unfollow người dùng
- ✅ Xem danh sách bài viết của user
- ✅ Xem bài viết đã like

### Settings
- ✅ Dark mode
- ✅ Chọn theme màu (Orange, Green, Blue, Pink)
- ✅ Đăng xuất

### Data Sync
- ✅ Realtime sync từ Firestore
- ✅ Offline support với Room cache
- ✅ Optimistic updates
- ✅ Retry mechanism với exponential backoff

## 💾 Cấu trúc dữ liệu

### Firestore Collections

```
users/
  └── {userId}/
      ├── name, email, avatarUrl, bio
      ├── postCount, followerCount, followingCount
      └── totalLikesReceived

posts/
  └── {postId}/
      ├── userId, restaurantId, title, content
      ├── rating, pricePerPerson, visitDate
      ├── likeCount, commentCount
      └── images/
          └── {imageId}/
              └── url, order

restaurants/
  └── {restaurantId}/
      ├── name, address, lat, lng
      ├── phone, coverImageUrl, priceRange
      ├── cuisineTypes, averageRating, reviewCount
      └── createdBy, createdAt

likes/
  └── {userId}_{postId}/
      └── userId, postId, timestamp

follows/
  └── {followerId}_{followingId}/
      └── followerId, followingId, timestamp
```

### Room Database Tables

- `user_profiles` - Cache user profiles
- `posts` - Cache posts với denormalized fields (userName, userAvatarUrl, restaurantName, restaurantAddress)
- `restaurants` - Cache restaurant info
- `post_images` - Cache post images
- `comments` - Cache comments
- `post_likes` - Cache likes
- `comment_likes` - Cache comment likes
- `follows` - Cache follow relationships

### Data Flow

```
Firestore (Remote) 
    ↓
Repository (Sync)
    ↓
Room (Local Cache)
    ↓
ViewModel (StateFlow)
    ↓
UI (Compose)
```

## 🔐 Security

### Firestore Security Rules

- Users chỉ đọc được public data
- Users chỉ tạo/sửa/xóa data của chính mình
- Like/Comment có validation đầy đủ
- Follow relationships được bảo vệ

Xem chi tiết trong `firestore.rules`.

## 🧪 Testing

### Unit Tests

```bash
./gradlew test
```

### UI Tests

```bash
./gradlew connectedAndroidTest
```

### Test Coverage

- ViewModels: Unit tests với MockK
- Repositories: Unit tests với mocked Firestore/Room
- Use Cases: Unit tests
- UI Components: Compose UI tests

## 📝 Code Style

Dự án tuân theo:
- **Google Kotlin Style Guide**
- **ktlint** rules
- **Clean Code** principles

Format code:

```bash
./gradlew ktlintFormat
```

## 🚢 Deployment

### Build Release APK

```bash
./gradlew assembleRelease
```

APK sẽ được tạo tại: `app/build/outputs/apk/release/`

### Build AAB (Google Play)

```bash
./gradlew bundleRelease
```

AAB sẽ được tạo tại: `app/build/outputs/bundle/release/`

## 🔄 Firebase Functions

Cloud Functions tự động cập nhật:
- `postCount` khi tạo/xóa post
- `likeCount` khi like/unlike
- `commentCount` khi comment/delete comment
- `totalLikesReceived` của author
- Xóa sub-collections khi xóa post

Deploy functions:

```bash
cd functions
npm install
cd ..
firebase deploy --only functions
```

## 🐛 Troubleshooting

### Lỗi Firebase

- Kiểm tra `google-services.json` đã đặt đúng vị trí
- Kiểm tra Firestore rules đã được deploy
- Kiểm tra Firebase Functions đã được deploy

### Lỗi Build

- Clean project: `./gradlew clean`
- Invalidate caches trong Android Studio
- Xóa `.gradle` và `build` folders

### Lỗi Room Migration

- Kiểm tra `AppDatabase.version` đã được tăng
- Tạo migration nếu cần


