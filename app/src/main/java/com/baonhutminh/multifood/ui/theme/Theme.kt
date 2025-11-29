package com.baonhutminh.multifood.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.baonhutminh.multifood.data.model.AppTheme

private fun getColorScheme(theme: AppTheme, isDark: Boolean) = when (theme) {
    AppTheme.ORANGE -> if (isDark) {
        darkColorScheme(
            primary = OrangeLight,
            onPrimary = Black,
            primaryContainer = OrangeDark,
            onPrimaryContainer = White,
            secondary = OrangeLight,
            tertiary = OrangeMain
        )
    } else {
        lightColorScheme(
            primary = OrangeMain,
            onPrimary = White,
            primaryContainer = OrangeLight,
            onPrimaryContainer = Black,
            secondary = OrangeDark,
            tertiary = OrangeLight
        )
    }

    AppTheme.BLUE -> if (isDark) {
        darkColorScheme(
            primary = BlueLight,
            onPrimary = Black,
            primaryContainer = BlueDark,
            onPrimaryContainer = White,
            secondary = BlueLight,
            tertiary = BlueMain
        )
    } else {
        lightColorScheme(
            primary = BlueMain,
            onPrimary = White,
            primaryContainer = BlueLight,
            onPrimaryContainer = Black,
            secondary = BlueDark,
            tertiary = BlueLight
        )
    }

    AppTheme.GREEN -> if (isDark) {
        darkColorScheme(
            primary = GreenLight,
            onPrimary = Black,
            primaryContainer = GreenDark,
            onPrimaryContainer = White,
            secondary = GreenLight,
            tertiary = GreenMain
        )
    } else {
        lightColorScheme(
            primary = GreenMain,
            onPrimary = White,
            primaryContainer = GreenLight,
            onPrimaryContainer = Black,
            secondary = GreenDark,
            tertiary = GreenLight
        )
    }

    AppTheme.PINK -> if (isDark) {
        darkColorScheme(
            primary = PinkLight,
            onPrimary = Black,
            primaryContainer = PinkDark,
            onPrimaryContainer = White,
            secondary = PinkLight,
            tertiary = PinkMain
        )
    } else {
        lightColorScheme(
            primary = PinkMain,
            onPrimary = White,
            primaryContainer = PinkLight,
            onPrimaryContainer = Black,
            secondary = PinkDark,
            tertiary = PinkLight
        )
    }
}

@Composable
fun MultiFoodTheme(
    appTheme: AppTheme = AppTheme.ORANGE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = getColorScheme(appTheme, darkTheme)
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
