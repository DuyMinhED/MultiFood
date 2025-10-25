// file: com/baonhutminh/multifood/ui/theme/Theme.kt

package com.baonhutminh.multifood.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme // <-- Thay đổi import
import androidx.compose.material3.darkColorScheme // <-- Thay đổi import
import androidx.compose.material3.lightColorScheme // <-- Thay đổi import
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Sử dụng darkColorScheme của Material 3
private val DarkColorScheme = darkColorScheme(
    primary = Orange400,
    secondary = Blue400,
    background = Neutral800,
    surface = Neutral700,
    onPrimary = Neutral900,
    onSecondary = Neutral900,
    onBackground = Neutral100,
    onSurface = Neutral100
)

// Sử dụng lightColorScheme của Material 3
private val LightColorScheme = lightColorScheme(
    primary = Orange500,
    secondary = Blue700,
    background = Neutral50,
    surface = androidx.compose.ui.graphics.Color.White,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onBackground = Neutral900,
    onSurface = Neutral900
)

@Composable
fun MultiFoodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Đoạn code này giúp đổi màu thanh trạng thái (status bar)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    // Sử dụng MaterialTheme của Material 3
    MaterialTheme(
        colorScheme = colorScheme, // <-- Tham số là 'colorScheme'
        typography = Typography,
        shapes = Shapes, // <-- Giữ nguyên, Shapes vẫn tương thích
        content = content
    )
}