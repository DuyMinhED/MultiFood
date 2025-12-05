package com.baonhutminh.multifood.data.repository

import android.net.Uri
import android.util.Log
import com.baonhutminh.multifood.data.local.CommentDao
import com.baonhutminh.multifood.data.local.PostDao
import com.baonhutminh.multifood.data.local.PostImageDao
import com.baonhutminh.multifood.data.model.Post
import com.baonhutminh.multifood.data.model.PostEntity
import com.baonhutminh.multifood.data.model.PostImage
import com.baonhutminh.multifood.data.model.PostImageEntity
import com.baonhutminh.multifood.data.model.relations.PostWithAuthor
import com.baonhutminh.multifood.common.Resource
import com.baonhutminh.multifood.common.retryWithBackoff
import com.baonhutminh.multifood.common.DEFAULT_UPLOAD_RETRY_CONFIG
import com.baonhutminh.multifood.common.DEFAULT_NETWORK_RETRY_CONFIG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.CancellationException
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val postDao: PostDao,
    private val commentDao: CommentDao,
    private val postImageDao: PostImageDao,
    private val restaurantRepository: RestaurantRepository,
    private val profileRepository: ProfileRepository
) : PostRepository {

    private val postsCollection = firestore.collection("posts")

    override fun getAllPosts(): Flow<Resource<List<PostWithAuthor>>> {
        return postDao.getAllPosts().map { Resource.Success(it) }
    }

    override fun getPostById(postId: String): Flow<Resource<PostWithAuthor?>> {
        return postDao.getPostById(postId).map { Resource.Success(it) }
    }

    override fun getPostsForUser(userId: String): Flow<Resource<List<PostWithAuthor>>> {
        return postDao.getPostsForUser(userId).map { Resource.Success(it) }
    }

    override fun getLikedPosts(postIds: List<String>): Flow<Resource<List<PostWithAuthor>>> {
        if (postIds.isEmpty()) {
            return kotlinx.coroutines.flow.flowOf(Resource.Success(emptyList()))
        }
        return postDao.getPostsByIds(postIds).map { Resource.Success(it) }
    }

    override fun searchPosts(query: String, minRating: Float, minPrice: Int, maxPrice: Int): Flow<Resource<List<PostWithAuthor>>> {
        return postDao.searchPosts(query, minRating, minPrice, maxPrice).map { Resource.Success(it) }
    }

    override suspend fun refreshAllPosts(): Resource<Unit> {
        return try {
            retryWithBackoff(config = DEFAULT_NETWORK_RETRY_CONFIG) {
                // Thêm limit để tránh load quá nhiều dữ liệu (Firestore limit 1MB/query)
                // TODO: Implement pagination đầy đủ với startAfter() cho load more
                postsCollection
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(50) // Load 50 posts mỗi lần để tránh timeout và tốn băng thông
                    .get()
                    .await()
            }.let { snapshot ->
            val postDTOs = snapshot.toObjects(Post::class.java)
            
            // Collect unique restaurant IDs
            val uniqueRestaurantIds = postDTOs
                .map { it.restaurantId }
                .filter { it.isNotBlank() }
                .toSet()
            
            // Collect unique user IDs (authors)
            val uniqueUserIds = postDTOs
                .map { it.userId }
                .filter { it.isNotBlank() }
                .toSet()
            
            // Fetch all restaurants in parallel
            val restaurantMap = mutableMapOf<String, Pair<String, String>>() // restaurantId -> (name, address)
            
            coroutineScope {
                // Fetch restaurants
                uniqueRestaurantIds.map { restaurantId ->
                    async {
                        try {
                            val restaurantResult = restaurantRepository.getRestaurantById(restaurantId).first()
                            when (restaurantResult) {
                                is Resource.Success -> {
                                    restaurantResult.data?.let { restaurant ->
                                        restaurantId to (restaurant.name to restaurant.address)
                                    }
                                }
                                else -> null
                            }
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            Log.e("PostRepositoryImpl", "Error fetching restaurant $restaurantId", e)
                            null
                        }
                    }
                }.awaitAll().forEach { result ->
                    result?.let { (id, nameAddress) ->
                        restaurantMap[id] = nameAddress
                    }
                }
                
                // Sync user profiles of all authors in parallel
                uniqueUserIds.map { userId ->
                    async {
                        try {
                            profileRepository.refreshUserProfileById(userId)
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            Log.e("PostRepositoryImpl", "Error syncing user profile $userId", e)
                        }
                    }
                }.awaitAll()
            }
            
            // Map posts to entities with restaurant info
            val postEntities = postDTOs.map { post ->
                val (restaurantName, restaurantAddress) = if (post.restaurantId.isNotBlank()) {
                    restaurantMap[post.restaurantId] ?: ("" to "")
                } else {
                    "" to ""
                }
                
                post.toEntity(
                    restaurantName = restaurantName,
                    restaurantAddress = restaurantAddress
                )
            }
            
            postDao.syncPosts(postEntities)
            
            // Sync images cho tất cả posts (không clearAll để tránh mất dữ liệu tạm thời)
            // Mỗi syncPostImages sẽ tự xóa images cũ của post đó trước khi upsert
            val postIds = postDTOs.map { it.id }.toSet()
            for (post in postDTOs) {
                syncPostImages(post.id)
            }
            
            // Cleanup: Xóa images của posts không còn tồn tại
            // Lưu ý: Chỉ xóa nếu post không còn trong danh sách để tránh mất dữ liệu
            // (Có thể skip bước này nếu muốn giữ lại images của deleted posts)
            
            Unit
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("PostRepositoryImpl", "Error refreshing posts", e)
            Resource.Error(e.message ?: "Lỗi làm mới bài đăng")
        }
    }
    
    override suspend fun refreshPost(postId: String): Resource<Unit> {
        return try {
            val doc = postsCollection.document(postId).get().await()
            val post = doc.toObject(Post::class.java)
            if (post != null) {
                // Fetch restaurant info để populate restaurantName và restaurantAddress
                var restaurantName = ""
                var restaurantAddress = ""
                
                if (post.restaurantId.isNotBlank()) {
                    val restaurantResult = restaurantRepository.getRestaurantById(post.restaurantId).first()
                    when (restaurantResult) {
                        is Resource.Success -> {
                            restaurantResult.data?.let { restaurant ->
                                restaurantName = restaurant.name
                                restaurantAddress = restaurant.address
                            }
                        }
                        else -> {
                            // Giữ giá trị mặc định (rỗng) nếu không tìm thấy restaurant
                        }
                    }
                }
                
                // Sync user profile of author
                if (post.userId.isNotBlank()) {
                    try {
                        profileRepository.refreshUserProfileById(post.userId)
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        Log.e("PostRepositoryImpl", "Error syncing author profile for post $postId", e)
                    }
                }
                
                postDao.upsert(post.toEntity(
                    restaurantName = restaurantName,
                    restaurantAddress = restaurantAddress
                ))
                syncPostImages(postId)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("PostRepositoryImpl", "Error refreshing post $postId", e)
            Resource.Error(e.message ?: "Lỗi làm mới bài đăng")
        }
    }
    
    private suspend fun syncPostImages(postId: String) {
        try {
            val imagesSnapshot = postsCollection.document(postId)
                .collection("images")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val postImages = imagesSnapshot.documents.mapIndexed { index, doc ->
                val image = doc.toObject(PostImage::class.java)
                PostImageEntity(
                    dbId = 0, // Auto-generated
                    postId = postId,
                    url = image?.url ?: "",
                    order = image?.order ?: index
                )
            }
            
            // Xóa images cũ trước khi insert mới để tránh duplicate
            postImageDao.deleteImagesForPost(postId)
            
            if (postImages.isNotEmpty()) {
                postImageDao.upsertAll(postImages)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("PostRepositoryImpl", "Error syncing images for post $postId", e)
        }
    }

    override suspend fun createPost(post: Post, images: List<PostImage>): Resource<String> {
        val currentUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        return try {
            retryWithBackoff(config = DEFAULT_NETWORK_RETRY_CONFIG) {
                val newPostRef = postsCollection.document()
                val newPost = post.copy(id = newPostRef.id, userId = currentUser.uid)

                // Chạy batch write để tạo bài viết và sub-collection images
                firestore.batch().apply {
                    set(newPostRef, newPost)
                    images.forEachIndexed { index, image ->
                        val imageRef = newPostRef.collection("images").document()
                        set(imageRef, image.copy(order = index))
                    }
                }.commit().await()

                // Sync images vào Room
                syncPostImages(newPostRef.id)

                // Logic tăng postCount sẽ do Cloud Function xử lý

                newPostRef.id
            }.let { postId ->
                Resource.Success(postId)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("PostRepositoryImpl", "Error creating post after retries", e)
            Resource.Error(e.message ?: "Lỗi tạo bài đăng sau nhiều lần thử")
        }
    }

    override suspend fun updatePost(post: Post): Resource<Unit> {
        return try {
            postsCollection.document(post.id).set(post, SetOptions.merge()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("PostRepositoryImpl", "Error updating post", e)
            Resource.Error(e.message ?: "Lỗi cập nhật bài đăng")
        }
    }

    override suspend fun uploadPostImage(imageUri: Uri): Resource<String> {
        auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        return try {
            retryWithBackoff(config = DEFAULT_UPLOAD_RETRY_CONFIG) {
                val imageId = UUID.randomUUID().toString()
                val storageRef = storage.reference.child("post_images/$imageId.jpg")
                storageRef.putFile(imageUri).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()
                downloadUrl
            }.let { downloadUrl ->
                Resource.Success(downloadUrl)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("PostRepositoryImpl", "Error uploading post image after retries", e)
            Resource.Error(e.message ?: "Lỗi tải ảnh bài đăng lên sau nhiều lần thử")
        }
    }

    override suspend fun deletePost(postId: String): Resource<Unit> {
        return try {
            // Chỉ cần xóa document `post`. Cloud Function sẽ xử lý việc xóa sub-collections
            // và cập nhật các counter liên quan.
            postsCollection.document(postId).delete().await()
            postDao.delete(postId)
            postImageDao.deleteImagesForPost(postId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("PostRepositoryImpl", "Error deleting post", e)
            Resource.Error(e.message ?: "Lỗi xóa bài viết")
        }
    }
    
    override fun observePostsRealtime(): Flow<Unit> = callbackFlow {
        var isFirstSync = true
        
        val listener = postsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("PostRepositoryImpl", "Realtime sync error", error)
                    return@addSnapshotListener
                }
                
                snapshot?.let { snap ->
                    launch {
                        try {
                            val postDTOs = snap.toObjects(Post::class.java)
                            
                            // Fetch restaurant info for all posts (only on first sync for performance)
                            val postEntities = if (isFirstSync) {
                                // First sync: fetch restaurant info in parallel
                                val uniqueRestaurantIds = postDTOs
                                    .map { it.restaurantId }
                                    .filter { it.isNotBlank() }
                                    .toSet()
                                
                                val restaurantMap = mutableMapOf<String, Pair<String, String>>()
                                
                                // Collect unique user IDs (authors)
                                val uniqueUserIds = postDTOs
                                    .map { it.userId }
                                    .filter { it.isNotBlank() }
                                    .toSet()
                                
                                coroutineScope {
                                    // Fetch restaurants
                                    uniqueRestaurantIds.map { restaurantId ->
                                        async {
                                            try {
                                                val restaurantResult = restaurantRepository.getRestaurantById(restaurantId).first()
                                                when (restaurantResult) {
                                                    is Resource.Success -> {
                                                        restaurantResult.data?.let { restaurant ->
                                                            restaurantId to (restaurant.name to restaurant.address)
                                                        }
                                                    }
                                                    else -> null
                                                }
                                            } catch (e: Exception) {
                                                if (e is CancellationException) throw e
                                                null
                                            }
                                        }
                                    }.awaitAll().forEach { result ->
                                        result?.let { (id, nameAddress) ->
                                            restaurantMap[id] = nameAddress
                                        }
                                    }
                                    
                                    // Sync user profiles of all authors
                                    uniqueUserIds.map { userId ->
                                        async {
                                            try {
                                                profileRepository.refreshUserProfileById(userId)
                                            } catch (e: Exception) {
                                                if (e is CancellationException) throw e
                                                Log.e("PostRepositoryImpl", "Error syncing user profile $userId in realtime", e)
                                            }
                                        }
                                    }.awaitAll()
                                }
                                
                                postDTOs.map { post ->
                                    val (restaurantName, restaurantAddress) = if (post.restaurantId.isNotBlank()) {
                                        restaurantMap[post.restaurantId] ?: ("" to "")
                                    } else {
                                        "" to ""
                                    }
                                    post.toEntity(
                                        restaurantName = restaurantName,
                                        restaurantAddress = restaurantAddress
                                    )
                                }
                            } else {
                                // Subsequent updates: preserve existing restaurant info from database
                                // Hoặc fetch cho posts mới (không có trong database)
                                val existingPostsMap = try {
                                    postDTOs.mapNotNull { post ->
                                        try {
                                            postDao.getPostById(post.id).first()?.post?.let { 
                                                post.id to it 
                                            }
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }.toMap()
                                } catch (e: Exception) {
                                    emptyMap()
                                }
                                
                                // Tìm posts mới (không có trong database) để fetch restaurant info
                                val newPostIds = postDTOs
                                    .filter { it.id !in existingPostsMap }
                                    .map { it.id to it.restaurantId }
                                    .filter { (_, restaurantId) -> restaurantId.isNotBlank() }
                                
                                val newRestaurantMap = if (newPostIds.isNotEmpty()) {
                                    val uniqueRestaurantIds = newPostIds.map { it.second }.toSet()
                                    val restaurantMap = mutableMapOf<String, Pair<String, String>>()
                                    
                                    // Collect unique user IDs from new posts
                                    val newPostUserIds = postDTOs
                                        .filter { it.id !in existingPostsMap }
                                        .map { it.userId }
                                        .filter { it.isNotBlank() }
                                        .toSet()
                                    
                                    try {
                                        coroutineScope {
                                            // Fetch restaurants
                                            uniqueRestaurantIds.map { restaurantId ->
                                                async {
                                                    try {
                                                        val restaurantResult = restaurantRepository.getRestaurantById(restaurantId).first()
                                                        when (restaurantResult) {
                                                            is Resource.Success -> {
                                                                restaurantResult.data?.let { restaurant ->
                                                                    restaurantId to (restaurant.name to restaurant.address)
                                                                }
                                                            }
                                                            else -> null
                                                        }
                                                    } catch (e: Exception) {
                                                        if (e is CancellationException) throw e
                                                        null
                                                    }
                                                }
                                            }.awaitAll().forEach { result ->
                                                result?.let { (id, nameAddress) ->
                                                    restaurantMap[id] = nameAddress
                                                }
                                            }
                                            
                                            // Sync user profiles of new post authors
                                            newPostUserIds.map { userId ->
                                                async {
                                                    try {
                                                        profileRepository.refreshUserProfileById(userId)
                                                    } catch (e: Exception) {
                                                        if (e is CancellationException) throw e
                                                        Log.e("PostRepositoryImpl", "Error syncing user profile $userId for new post", e)
                                                    }
                                                }
                                            }.awaitAll()
                                        }
                                    } catch (e: Exception) {
                                        if (e is CancellationException) throw e
                                        Log.w("PostRepositoryImpl", "Error fetching restaurants for new posts", e)
                                    }
                                    
                                    restaurantMap
                                } else {
                                    emptyMap()
                                }
                                
                                postDTOs.map { post ->
                                    val (restaurantName, restaurantAddress) = when {
                                        // Case 1: Post đã có trong database, preserve restaurant info
                                        existingPostsMap[post.id]?.let { existingPost ->
                                            if (existingPost.restaurantName.isNotEmpty()) {
                                                existingPost.restaurantName to existingPost.restaurantAddress
                                            } else null
                                        } != null -> {
                                            val existingPost = existingPostsMap[post.id]!!
                                            existingPost.restaurantName to existingPost.restaurantAddress
                                        }
                                        // Case 2: Post mới, fetch restaurant info
                                        post.restaurantId.isNotBlank() && newRestaurantMap.containsKey(post.restaurantId) -> {
                                            newRestaurantMap[post.restaurantId]!!
                                        }
                                        // Case 3: Không có info, để rỗng (sẽ populate khi user views post detail)
                                        else -> "" to ""
                                    }
                                    
                                    post.toEntity(
                                        restaurantName = restaurantName,
                                        restaurantAddress = restaurantAddress
                                    )
                                }
                            }
                            
                            // Dùng upsertAll thay vì syncPosts để không xóa dữ liệu local
                            // Giữ lại optimistic update
                            if (isFirstSync) {
                                postDao.syncPosts(postEntities) // Lần đầu sync đầy đủ
                                for (post in postDTOs) {
                                    syncPostImages(post.id)
                                }
                                isFirstSync = false
                            } else {
                                postDao.upsertAll(postEntities) // Các lần sau chỉ update
                            }
                            
                            trySend(Unit)
                        } catch (e: Exception) {
                            Log.e("PostRepositoryImpl", "Error processing realtime update", e)
                        }
                    }
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    override fun observePostRealtime(postId: String): Flow<Unit> = callbackFlow {
        var isFirstSync = true
        
        val listener = postsCollection.document(postId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("PostRepositoryImpl", "Realtime sync error for post $postId", error)
                    return@addSnapshotListener
                }
                
                snapshot?.let { snap ->
                    launch {
                        try {
                            val post = snap.toObject(Post::class.java)
                            if (post != null) {
                                // Fetch restaurant info để populate restaurantName và restaurantAddress
                                var restaurantName = ""
                                var restaurantAddress = ""
                                
                                if (post.restaurantId.isNotBlank()) {
                                    try {
                                        val restaurantResult = restaurantRepository.getRestaurantById(post.restaurantId).first()
                                        when (restaurantResult) {
                                            is Resource.Success -> {
                                                restaurantResult.data?.let { restaurant ->
                                                    restaurantName = restaurant.name
                                                    restaurantAddress = restaurant.address
                                                }
                                            }
                                            else -> {
                                                // Giữ giá trị mặc định (rỗng) nếu không tìm thấy restaurant
                                            }
                                        }
                                    } catch (e: Exception) {
                                        if (e is CancellationException) throw e
                                        Log.w("PostRepositoryImpl", "Error fetching restaurant in realtime update", e)
                                    }
                                }
                                
                                // Sync user profile of author
                                if (post.userId.isNotBlank()) {
                                    try {
                                        profileRepository.refreshUserProfileById(post.userId)
                                    } catch (e: Exception) {
                                        if (e is CancellationException) throw e
                                        Log.w("PostRepositoryImpl", "Error syncing author profile in realtime update", e)
                                    }
                                }
                                
                                postDao.upsert(post.toEntity(
                                    restaurantName = restaurantName,
                                    restaurantAddress = restaurantAddress
                                ))
                                
                                // Chỉ sync images lần đầu
                                if (isFirstSync) {
                                    syncPostImages(postId)
                                    isFirstSync = false
                                }
                            }
                            trySend(Unit)
                        } catch (e: Exception) {
                            Log.e("PostRepositoryImpl", "Error processing realtime update", e)
                        }
                    }
                }
            }
        
        awaitClose { listener.remove() }
    }
}

// Hàm này cần được cập nhật vì PostEntity đã thay đổi
// Các trường cache (userName, userAvatarUrl, restaurantName, restaurantAddress) 
// sẽ được populate khi sync từ Firestore với thông tin đầy đủ
fun Post.toEntity(
    restaurantName: String = "",
    restaurantAddress: String = "",
    userName: String = "",
    userAvatarUrl: String = ""
): PostEntity {
    return PostEntity(
        id = this.id,
        userId = this.userId,
        restaurantId = this.restaurantId,
        title = this.title,
        content = this.content,
        rating = this.rating,
        pricePerPerson = this.pricePerPerson,
        visitDate = this.visitDate,
        likeCount = this.likeCount,
        commentCount = this.commentCount,
        status = this.status,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        // Các trường cache sẽ được populate khi có thông tin đầy đủ từ User và Restaurant
        userName = userName,
        userAvatarUrl = userAvatarUrl,
        restaurantName = restaurantName,
        restaurantAddress = restaurantAddress
    )
}
