package com.baonhutminh.multifood.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baonhutminh.multifood.data.model.AppTheme
import com.baonhutminh.multifood.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Phần Giao diện ---
            SettingsHeader("Giao diện")
            SettingsItemWithAction(
                icon = Icons.Default.Palette,
                title = "Màu chủ đạo",
                subtitle = uiState.appTheme.displayName,
                onClick = { showThemeDialog = true }
            ) {
                ThemeColorPreview(theme = uiState.appTheme)
            }
            SettingsItemWithSwitch(
                icon = if (uiState.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                title = "Chế độ tối",
                subtitle = if (uiState.isDarkMode) "Đang bật" else "Đang tắt",
                checked = uiState.isDarkMode,
                onCheckedChange = { viewModel.setDarkMode(it) }
            )

            // --- Phần Tài khoản ---
            SettingsHeader("Tài khoản")
            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Đổi mật khẩu",
                subtitle = "Thay đổi mật khẩu đăng nhập của bạn",
                onClick = { showPasswordDialog = true }
            )

            // --- Phần Thông báo ---
            SettingsHeader("Thông báo")
            SettingsItemWithSwitch(
                icon = Icons.Default.Notifications,
                title = "Bật thông báo",
                subtitle = if (uiState.notificationsEnabled) "Nhận thông báo về các hoạt động mới" else "Không nhận thông báo",
                checked = uiState.notificationsEnabled,
                onCheckedChange = { viewModel.setNotificationsEnabled(it) }
            )

            // --- Phần Giới thiệu ---
            SettingsHeader("Giới thiệu")
            SettingsItem(
                icon = Icons.Default.Description,
                title = "Điều khoản sử dụng",
                subtitle = "Các quy định khi sử dụng ứng dụng",
                onClick = { showTermsDialog = true }
            )
            SettingsItem(
                icon = Icons.Default.Shield,
                title = "Chính sách bảo mật",
                subtitle = "Cách chúng tôi bảo vệ dữ liệu của bạn",
                onClick = { showPrivacyDialog = true }
            )
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Phiên bản",
                subtitle = "1.0.0",
                onClick = { }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Đăng xuất ---
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng xuất")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // --- Dialogs ---
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.appTheme,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = {
                viewModel.setAppTheme(it)
                showThemeDialog = false
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Đăng xuất") },
            text = { Text("Bạn có chắc chắn muốn đăng xuất không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
                        // Điều hướng sẽ được xử lý ở màn hình cha
                    }
                ) {
                    Text("Đăng xuất")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { current, new, confirm ->
                viewModel.changePassword(current, new, confirm)
            }
        )
    }

    if (showTermsDialog) {
        PolicyContentDialog(
            title = "Điều khoản sử dụng",
            content = TERMS_OF_SERVICE_CONTENT,
            onDismiss = { showTermsDialog = false }
        )
    }

    if (showPrivacyDialog) {
        PolicyContentDialog(
            title = "Chính sách bảo mật",
            content = PRIVACY_POLICY_CONTENT,
            onDismiss = { showPrivacyDialog = false }
        )
    }
}

@Composable
private fun SettingsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsItemWithSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsItemWithAction(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        action()
    }
}

@Composable
private fun ThemeColorPreview(theme: AppTheme) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(theme.previewColor) // Sửa ở đây
            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onDismiss: () -> Unit,
    onThemeSelected: (AppTheme) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn màu chủ đạo") },
        text = {
            Column {
                AppTheme.entries.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(theme.previewColor) // Sửa ở đây
                                .let { // Sửa ở đây để khắc phục lỗi trình biên dịch
                                    if (theme == currentTheme) {
                                        it.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    } else it
                                }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = theme.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (theme == currentTheme) FontWeight.Bold else FontWeight.Normal
                        )
                        if (theme == currentTheme) {
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (currentPassword: String, newPassword: String, confirmPassword: String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isCurrentPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val isConfirmEnabled = currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đổi mật khẩu") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mật khẩu hiện tại
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Mật khẩu hiện tại") },
                    singleLine = true,
                    visualTransformation = if (isCurrentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (isCurrentPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        IconButton(onClick = { isCurrentPasswordVisible = !isCurrentPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = if (isCurrentPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu")
                        }
                    }
                )

                // Mật khẩu mới
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Mật khẩu mới") },
                    singleLine = true,
                    visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (isNewPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = if (isNewPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu")
                        }
                    }
                )

                // Xác nhận mật khẩu mới
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Xác nhận mật khẩu mới") },
                    singleLine = true,
                    visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (isConfirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = if (isConfirmPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu")
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(currentPassword, newPassword, confirmPassword)
                    onDismiss()
                },
                enabled = isConfirmEnabled
            ) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
private fun PolicyContentDialog(
    title: String,
    content: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

private const val TERMS_OF_SERVICE_CONTENT = """
Cập nhật lần cuối: 02/12/2025

Chào mừng bạn đến với MultiFood!

Bằng cách truy cập hoặc sử dụng ứng dụng của chúng tôi, bạn đồng ý tuân thủ các điều khoản và điều kiện này.

1. Sử dụng Ứng dụng
- Bạn phải đủ 13 tuổi để sử dụng ứng dụng này.
- Bạn chịu trách nhiệm về mọi hoạt động diễn ra dưới tài khoản của mình.

2. Nội dung Người dùng
- Bạn cấp cho chúng tôi giấy phép không độc quyền, trên toàn thế giới để sử dụng, lưu trữ, hiển thị, tái sản xuất nội dung bạn đăng tải.
- Bạn không được đăng tải nội dung bất hợp pháp, xúc phạm hoặc vi phạm quyền của người khác.

3. Chấm dứt
- Chúng tôi có thể chấm dứt hoặc tạm ngưng quyền truy cập vào ứng dụng của chúng tôi ngay lập tức, không cần thông báo trước, vì bất kỳ lý do gì, bao gồm cả việc bạn vi phạm Điều khoản.

4. Giới hạn Trách nhiệm
- Ứng dụng được cung cấp "nguyên trạng". Chúng tôi không đưa ra bất kỳ bảo đảm nào về tính chính xác hoặc độ tin cậy của dịch vụ.

5. Thay đổi Điều khoản
- Chúng tôi có quyền sửa đổi các điều khoản này bất kỳ lúc nào. Chúng tôi sẽ thông báo cho bạn về bất kỳ thay đổi nào bằng cách đăng các điều khoản mới trên trang này.
"""

private const val PRIVACY_POLICY_CONTENT = """
Cập nhật lần cuối: 02/12/2025

MultiFood tôn trọng quyền riêng tư của bạn.

1. Thông tin chúng tôi thu thập
- Thông tin Cá nhân: Tên, địa chỉ email, mật khẩu (được mã hóa) khi bạn đăng ký.
- Dữ liệu Sử dụng: Thông tin về cách bạn tương tác với ứng dụng, chẳng hạn như các tính năng được sử dụng và thời gian dành cho ứng dụng.

2. Cách chúng tôi sử dụng thông tin
- Để cung cấp và duy trì Dịch vụ.
- Để thông báo cho bạn về những thay đổi đối với Dịch vụ của chúng tôi.
- Để cung cấp hỗ trợ khách hàng.
- Để thu thập phân tích hoặc thông tin có giá trị để chúng tôi có thể cải thiện Dịch vụ.

3. Bảo mật Dữ liệu
- An toàn dữ liệu của bạn rất quan trọng đối với chúng tôi. Chúng tôi sử dụng các biện pháp bảo mật hợp lý về mặt thương mại để bảo vệ thông tin cá nhân của bạn nhưng hãy nhớ rằng không có phương thức truyền qua Internet hoặc phương pháp lưu trữ điện tử nào là an toàn 100%.

4. Liên kết đến các trang web khác
- Dịch vụ của chúng tôi có thể chứa các liên kết đến các trang web khác không do chúng tôi điều hành. Nếu bạn nhấp vào một liên kết của bên thứ ba, bạn sẽ được chuyển đến trang web của bên thứ ba đó.

5. Quyền riêng tư của trẻ em
- Dịch vụ của chúng tôi không dành cho bất kỳ ai dưới 13 tuổi.
"""
