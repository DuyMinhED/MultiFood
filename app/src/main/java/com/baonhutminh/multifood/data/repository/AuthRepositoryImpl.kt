package com.baonhutminh.multifood.data.repository

import android.content.Context
import com.baonhutminh.multifood.data.model.User
import com.baonhutminh.multifood.util.Resource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : AuthRepository {

    override suspend fun login(email: String, pass: String): Resource<String> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            Resource.Success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi không xác định")
        }
    }

    override suspend fun signup(email: String, pass: String, name: String): Resource<String> {
        return try {
            // 1. Tạo tài khoản trên Authentication
            val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: return Resource.Error("Không lấy được UID")

            // 2. Tạo User Model
            val user = User(
                id = uid,
                name = name,
                email = email,
                avatarUrl = "https://i.imgur.com/6VBx3io.png" // Avatar mặc định
            )

            // 3. Lưu vào Firestore (Collection "users")
            firestore.collection("users").document(uid).set(user).await()

            Resource.Success(uid)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Lỗi đăng ký không xác định")
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Resource<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user ?: return Resource.Error("Không lấy được thông tin người dùng")
            
            // Kiểm tra xem user đã tồn tại trong Firestore chưa
            val userDoc = firestore.collection("users").document(user.uid).get().await()
            
            if (!userDoc.exists()) {
                // Tạo User mới trong Firestore
                val newUser = User(
                    id = user.uid,
                    name = user.displayName ?: "Người dùng Google",
                    email = user.email,
                    avatarUrl = user.photoUrl?.toString() ?: "https://i.imgur.com/6VBx3io.png"
                )
                firestore.collection("users").document(user.uid).set(newUser).await()
            }
            
            Resource.Success(user.uid)
        } catch (e: FirebaseAuthException) {
            // Kiểm tra error code để xử lý các trường hợp khác nhau
            when (e.errorCode) {
                "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> {
                    Resource.Error("Email này đã được sử dụng. Vui lòng liên kết tài khoản trong phần Cài đặt.")
                }
                else -> Resource.Error(e.message ?: "Lỗi đăng nhập Google")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi đăng nhập Google")
        }
    }
    
    override fun getCurrentUserProviders(): List<String> {
        return firebaseAuth.currentUser?.providerData?.map { it.providerId } ?: emptyList()
    }
    
    override suspend fun linkGoogleAccount(idToken: String): Resource<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: return Resource.Error("Chưa đăng nhập")
            
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            currentUser.linkWithCredential(credential).await()
            
            Resource.Success(Unit)
        } catch (e: FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_CREDENTIAL_ALREADY_IN_USE" -> {
                    Resource.Error("Tài khoản Google này đã được liên kết với tài khoản khác")
                }
                "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> {
                    Resource.Error("Email này đã được sử dụng bởi tài khoản khác")
                }
                else -> Resource.Error(e.message ?: "Lỗi liên kết tài khoản Google")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi liên kết tài khoản Google")
        }
    }
    
    override suspend fun linkEmailPassword(email: String, password: String): Resource<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: return Resource.Error("Chưa đăng nhập")
            
            // Kiểm tra email phải trùng với email của tài khoản hiện tại
            val currentEmail = currentUser.email
            if (currentEmail != null && currentEmail != email) {
                return Resource.Error("Email phải trùng với email của tài khoản hiện tại ($currentEmail)")
            }
            
            val credential = EmailAuthProvider.getCredential(email, password)
            currentUser.linkWithCredential(credential).await()
            
            Resource.Success(Unit)
        } catch (e: FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_CREDENTIAL_ALREADY_IN_USE" -> {
                    Resource.Error("Email này đã được liên kết với tài khoản khác")
                }
                "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> {
                    Resource.Error("Email này đã được sử dụng bởi tài khoản khác")
                }
                "ERROR_INVALID_EMAIL" -> {
                    Resource.Error("Email không hợp lệ")
                }
                "ERROR_WEAK_PASSWORD" -> {
                    Resource.Error("Mật khẩu quá yếu. Vui lòng sử dụng mật khẩu mạnh hơn")
                }
                else -> Resource.Error(e.message ?: "Lỗi liên kết tài khoản email/password")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi liên kết tài khoản email/password")
        }
    }

    override suspend fun sendPasswordReset(email: String): Resource<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Không thể gửi email đặt lại mật khẩu")
        }
    }
    
    override suspend fun signOutGoogle() {
        try {
            // Lấy Web Client ID từ context (có thể lưu trong strings.xml hoặc BuildConfig)
            // Tạm thời hardcode, nên di chuyển sang BuildConfig hoặc strings.xml
            val webClientId = "1051570676237-n8mtu8j191pbt17me07q3pcrajnt58pc.apps.googleusercontent.com"
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            // Sign out và revoke access để xóa hoàn toàn session
            // Điều này đảm bảo user có thể chọn account khác lần sau
            googleSignInClient.signOut().await()
            // Revoke access để xóa hoàn toàn, buộc user phải chọn account lại
            googleSignInClient.revokeAccess().await()
        } catch (e: Exception) {
            // Không throw error vì có thể user không đăng nhập bằng Google
            // Chỉ log để debug
            android.util.Log.e("AuthRepositoryImpl", "Error signing out from Google", e)
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
        // Sign out Google trong coroutine scope riêng để không block
        // Sử dụng runBlocking để đảm bảo signOutGoogle() hoàn thành trước khi return
        CoroutineScope(Dispatchers.IO).launch {
            signOutGoogle()
        }
    }
}