package com.baonhutminh.multifood.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.baonhutminh.multifood.ui.components.AppBottomBar
import com.baonhutminh.multifood.ui.components.AppTopBar
import com.baonhutminh.multifood.ui.navigation.Screen
import com.baonhutminh.multifood.viewmodel.ProfileUiEvent
import com.baonhutminh.multifood.viewmodel.ProfileViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    onLogout: () -> Unit = {},
    onClickHome: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userProfile = uiState.userProfile
    val isLoading = uiState.isLoading
    val isUpdating = uiState.isUpdating

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditBioDialog by remember { mutableStateOf(false) }
    var showEditPhoneDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLinkGoogleDialog by remember { mutableStateOf(false) }
    var showLinkEmailPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var currentPasswordError by remember { mutableStateOf<String?>(null) }
    
    val hasPasswordProvider = viewModel.hasPasswordProvider()
    val hasGoogleProvider = viewModel.hasGoogleProvider()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadAvatar(it) }
    }
    
    // Google Sign-In for linking account
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1051570676237-n8mtu8j191pbt17me07q3pcrajnt58pc.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }
    
    val googleLinkLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { idToken ->
                viewModel.linkGoogleAccount(idToken)
            } ?: run {
                scope.launch {
                    snackbarHostState.showSnackbar("Không thể lấy ID token từ Google")
                }
            }
        } catch (e: ApiException) {
            scope.launch {
                snackbarHostState.showSnackbar("Lỗi liên kết Google: ${e.message}")
            }
        }
    }
    
    fun linkGoogleAccount() {
        val signInIntent = googleSignInClient.signInIntent
        googleLinkLauncher.launch(signInIntent)
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ProfileUiEvent.UpdateSuccess -> {
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
                    if (event.message.contains("mật khẩu")) {
                        showChangePasswordDialog = false
                        currentPasswordError = null
                    }
                    if (event.message.contains("tên")) showEditNameDialog = false
                    if (event.message.contains("giới thiệu")) showEditBioDialog = false
                    if (event.message.contains("số điện thoại")) showEditPhoneDialog = false
                    if (event.message.contains("liên kết")) {
                        showLinkGoogleDialog = false
                        showLinkEmailPasswordDialog = false
                    }
                }
                is ProfileUiEvent.ShowError -> {
                    if (showChangePasswordDialog && event.message.contains("Mật khẩu hiện tại không đúng")) {
                        currentPasswordError = event.message
                    } else {
                        scope.launch { snackbarHostState.showSnackbar(event.message) }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                screen = Screen.Profile,
                showSearch = false
            )
        },
        bottomBar = {
            AppBottomBar(
                currentScreen = Screen.Profile,
                onNavigate = { screen ->
                    if (screen.route == Screen.Home.route) {
                        onClickHome()
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading && userProfile == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (userProfile != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { imagePickerLauncher.launch("image/*") }
                            ) {
                                if (userProfile.avatarUrl?.isNotEmpty() == true) {
                                    AsyncImage(
                                        model = userProfile.avatarUrl,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Avatar mặc định",
                                        modifier = Modifier
                                            .size(60.dp)
                                            .align(Alignment.Center),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(36.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    shadowElevation = 4.dp
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Đổi ảnh",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = isUpdating,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    LinearProgressIndicator(
                                        modifier = Modifier.width(120.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Đang xử lý...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = userProfile.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(onClick = { showEditNameDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Sửa tên",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Text(
                                text = userProfile.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (userProfile.phoneNumber?.isNotEmpty() == true) {
                                Text(
                                    text = userProfile.phoneNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (userProfile.bio?.isNotEmpty() == true) {
                                Text(
                                    text = userProfile.bio,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }

                            TextButton(onClick = { showEditBioDialog = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (userProfile.bio?.isNotEmpty() == true)
                                        "Sửa giới thiệu"
                                    else
                                        "Thêm giới thiệu"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.AutoMirrored.Filled.Article,
                            count = userProfile.postCount,
                            label = "Bài viết"
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Favorite,
                            count = userProfile.totalLikesReceived,
                            label = "Lượt thích"
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.People,
                            count = userProfile.followerCount,
                            label = "Follower"
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.PersonAdd,
                            count = userProfile.followingCount,
                            label = "Following"
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Tài khoản & Bảo mật",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = MaterialTheme.shapes.large,
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = Icons.Default.Phone,
                                title = "Số điện thoại",
                                subtitle = userProfile.phoneNumber ?: "Thêm số điện thoại",
                                onClick = { showEditPhoneDialog = true }
                            )
                            HorizontalDivider()
                            ProfileMenuItem(
                                icon = Icons.Default.Settings,
                                title = "Cài đặt",
                                subtitle = "Tùy chỉnh ứng dụng",
                                onClick = onNavigateToSettings
                            )
                            // Chỉ hiển thị "Đổi mật khẩu" nếu có password provider
                            if (hasPasswordProvider) {
                                HorizontalDivider()
                                ProfileMenuItem(
                                    icon = Icons.Default.Lock,
                                    title = "Đổi mật khẩu",
                                    subtitle = "Bảo mật tài khoản của bạn",
                                    onClick = { showChangePasswordDialog = true }
                                )
                            }
                            
                            // Hiển thị phần liên kết tài khoản
                            HorizontalDivider()
                            Text(
                                text = "Liên kết tài khoản",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            
                            // Liên kết Google nếu chưa có
                            if (!hasGoogleProvider) {
                                ProfileMenuItem(
                                    icon = Icons.Default.AccountCircle,
                                    title = "Liên kết Google",
                                    subtitle = "Đăng nhập bằng tài khoản Google",
                                    onClick = { showLinkGoogleDialog = true }
                                )
                                HorizontalDivider()
                            } else {
                                ProfileMenuItem(
                                    icon = Icons.Default.CheckCircle,
                                    title = "Đã liên kết Google",
                                    subtitle = "Tài khoản Google đã được liên kết",
                                    onClick = { },
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                HorizontalDivider()
                            }
                            
                            // Liên kết Email/Password nếu chưa có
                            if (!hasPasswordProvider) {
                                ProfileMenuItem(
                                    icon = Icons.Default.Email,
                                    title = "Liên kết Email/Password",
                                    subtitle = "Thêm mật khẩu để đăng nhập",
                                    onClick = { showLinkEmailPasswordDialog = true }
                                )
                                HorizontalDivider()
                            } else {
                                ProfileMenuItem(
                                    icon = Icons.Default.CheckCircle,
                                    title = "Đã liên kết Email/Password",
                                    subtitle = "Tài khoản email đã được liên kết",
                                    onClick = { },
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                HorizontalDivider()
                            }
                            
                            HorizontalDivider()
                            ProfileMenuItem(
                                icon = Icons.AutoMirrored.Filled.Help,
                                title = "Trợ giúp & Hỗ trợ",
                                subtitle = "Câu hỏi thường gặp",
                                onClick = { showHelpDialog = true }
                            )
                            HorizontalDivider()
                            ProfileMenuItem(
                                icon = Icons.Default.Info,
                                title = "Về ứng dụng",
                                subtitle = "Phiên bản 1.0.0",
                                onClick = { showAboutDialog = true }
                            )
                            HorizontalDivider()
                            ProfileMenuItem(
                                icon = Icons.AutoMirrored.Filled.Logout,
                                title = "Đăng xuất",
                                subtitle = "Thoát khỏi tài khoản",
                                onClick = { showLogoutDialog = true },
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showEditNameDialog) {
        userProfile?.let {
            EditTextDialog(
                title = "Đổi tên hiển thị",
                currentValue = it.name,
                onDismiss = { showEditNameDialog = false },
                onConfirm = { newName ->
                    viewModel.updateName(newName)
                }
            )
        }
    }

    if (showEditBioDialog) {
        userProfile?.let {
            EditTextDialog(
                title = "Giới thiệu bản thân",
                currentValue = it.bio ?: "",
                onDismiss = { showEditBioDialog = false },
                onConfirm = { newBio ->
                    viewModel.updateBio(newBio)
                },
                maxLines = 3
            )
        }
    }
    if (showEditPhoneDialog) {
        userProfile?.let {
            EditTextDialog(
                title = "Cập nhật số điện thoại",
                currentValue = it.phoneNumber ?: "",
                onDismiss = { showEditPhoneDialog = false },
                onConfirm = { newPhone ->
                    viewModel.updatePhoneNumber(newPhone)
                },
                keyboardType = KeyboardType.Phone
            )
        }
    }


    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            isCurrentPasswordError = currentPasswordError != null,
            currentPasswordErrorText = currentPasswordError,
            onDismiss = {
                showChangePasswordDialog = false
                currentPasswordError = null // Reset error on dismiss
            },
            onConfirm = { current, new, confirm ->
                currentPasswordError = null // Reset error on new submission
                viewModel.changePassword(current, new, confirm)
            }
        )
    }
    
    if (showLinkGoogleDialog) {
        AlertDialog(
            onDismissRequest = { showLinkGoogleDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { 
                Text(
                    "Liên kết tài khoản Google",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Bạn sẽ được chuyển đến trang đăng nhập Google để liên kết tài khoản.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLinkGoogleDialog = false
                        linkGoogleAccount()
                    }
                ) {
                    Text("Tiếp tục", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkGoogleDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
    
    if (showLinkEmailPasswordDialog) {
        userProfile?.let { profile ->
            LinkEmailPasswordDialog(
                currentEmail = profile.email,
                onDismiss = { showLinkEmailPasswordDialog = false },
                onConfirm = { email, password ->
                    viewModel.linkEmailPassword(email, password)
                },
                isLoading = isUpdating
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Đăng xuất", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn đăng xuất khỏi tài khoản?", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Đăng xuất", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Hủy") } }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            icon = { Icon(imageVector = Icons.AutoMirrored.Filled.Help, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) },
            title = { Text("Trợ giúp & Hỗ trợ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Liên hệ hỗ trợ:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📧 Email: multifood@gmail.com")
                    Text("📞 Hotline: 082-741-0398")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Câu hỏi thường gặp:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Làm sao để đăng bài review?")
                    Text("• Làm sao để thay đổi mật khẩu?")
                    Text("• Cách xóa bài viết của tôi?")
                }
            },
            confirmButton = { TextButton(onClick = { showHelpDialog = false }) { Text("Đóng", fontWeight = FontWeight.Bold) } }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { Text("🍜", style = MaterialTheme.typography.displayMedium) },
            title = { Text("MultiFood", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Phiên bản 1.0.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Ứng dụng chia sẻ trải nghiệm ẩm thực hàng đầu Việt Nam.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("© 2024 MultiFood Team", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text("Đóng", fontWeight = FontWeight.Bold) } }
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    count: Int,
    label: String
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = count.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, title: String, subtitle: String = "", onClick: () -> Unit, tint: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = tint.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier
                .padding(8.dp)
                .size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = tint)
            if (subtitle.isNotEmpty()) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun EditTextDialog(
    title: String,
    currentValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var text by remember { mutableStateOf(currentValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                maxLines = maxLines,
                singleLine = maxLines == 1,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                placeholder = { Text("Nhập nội dung...") }
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) { Text("Lưu", fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
private fun LinkEmailPasswordDialog(
    currentEmail: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    isLoading: Boolean
) {
    var email by remember { mutableStateOf(currentEmail) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Liên kết Email/Password",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false, // Disable vì email phải trùng với email hiện tại
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it) } } ?: { Text("Email phải trùng với email của tài khoản hiện tại") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    label = { Text("Mật khẩu") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !isLoading,
                    isError = passwordError != null,
                    supportingText = passwordError?.let { { Text(it) } }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = null
                    },
                    label = { Text("Xác nhận mật khẩu") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !isLoading,
                    isError = confirmPasswordError != null,
                    supportingText = confirmPasswordError?.let { { Text(it) } }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    var valid = true
                    if (email.isBlank()) {
                        emailError = "Vui lòng nhập email"
                        valid = false
                    }
                    if (password.isBlank()) {
                        passwordError = "Vui lòng nhập mật khẩu"
                        valid = false
                    } else if (password.length < 6) {
                        passwordError = "Mật khẩu phải có ít nhất 6 ký tự"
                        valid = false
                    }
                    if (confirmPassword.isBlank()) {
                        confirmPasswordError = "Vui lòng xác nhận mật khẩu"
                        valid = false
                    } else if (password != confirmPassword) {
                        confirmPasswordError = "Mật khẩu không khớp"
                        valid = false
                    }
                    if (valid) {
                        onConfirm(email, password)
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Liên kết", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Hủy")
            }
        }
    )
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
    isCurrentPasswordError: Boolean,
    currentPasswordErrorText: String?
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isCurrentPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val doPasswordsMatch = newPassword == confirmPassword
    val isConfirmEnabled = currentPassword.isNotBlank() && newPassword.length >= 6 && doPasswordsMatch

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đổi mật khẩu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Mật khẩu hiện tại") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = isCurrentPasswordError,
                    visualTransformation = if (isCurrentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (isCurrentPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { isCurrentPasswordVisible = !isCurrentPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = if (isCurrentPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu")
                        }
                    }
                )
                if (isCurrentPasswordError) {
                    currentPasswordErrorText?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Mật khẩu mới (ít nhất 6 ký tự)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (isNewPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = if (isNewPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Xác nhận mật khẩu mới") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = !doPasswordsMatch && confirmPassword.isNotEmpty(),
                    trailingIcon = {
                        val image = if (isConfirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = if (isConfirmPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu")
                        }
                    }
                )
                if (!doPasswordsMatch && confirmPassword.isNotEmpty()) {
                    Text(
                        text = "Mật khẩu xác nhận không khớp.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(currentPassword, newPassword, confirmPassword) },
                enabled = isConfirmEnabled
            ) {
                Text("Lưu", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
