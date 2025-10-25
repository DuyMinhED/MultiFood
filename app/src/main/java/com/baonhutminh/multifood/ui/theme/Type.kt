// file: com/baonhutminh/multifood/ui/theme/Type.kt

package com.baonhutminh.multifood.ui.theme

// Đảm bảo import từ androidx.compose.material3.Typography
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    // Bạn có thể định nghĩa thêm các kiểu chữ khác ở đây
    // ví dụ: titleLarge, labelSmall, ...
)