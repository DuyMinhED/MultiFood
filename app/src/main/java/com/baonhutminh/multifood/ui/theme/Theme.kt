package com.baonhutminh.multifood.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.baonhutminh.multifood.data.model.AppTheme

private val DarkGreenColorScheme = darkColorScheme(
    primary = GreenMain, 
    secondary = GreenSecondary, 
    tertiary = GreenTertiary
)

private val LightGreenColorScheme = lightColorScheme(
    primary = GreenMain, 
    secondary = GreenSecondary, 
    tertiary = GreenTertiary
)

private val DarkBlueColorScheme = darkColorScheme(
    primary = BlueMain, 
    secondary = BlueSecondary, 
    tertiary = BlueTertiary
)

private val LightBlueColorScheme = lightColorScheme(
    primary = BlueMain, 
    secondary = BlueSecondary, 
    tertiary = BlueTertiary
)

private val DarkOrangeColorScheme = darkColorScheme(
    primary = OrangeMain, 
    secondary = OrangeSecondary, 
    tertiary = OrangeTertiary
)

private val LightOrangeColorScheme = lightColorScheme(
    primary = OrangeMain, 
    secondary = OrangeSecondary, 
    tertiary = OrangeTertiary
)

private val DarkPinkColorScheme = darkColorScheme(
    primary = PinkMain, 
    secondary = PinkSecondary, 
    tertiary = PinkTertiary
)

private val LightPinkColorScheme = lightColorScheme(
    primary = PinkMain, 
    secondary = PinkSecondary, 
    tertiary = PinkTertiary
)

@Composable
fun MultiFoodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appTheme: AppTheme = AppTheme.ORANGE, // Default theme
    dynamicColor: Boolean = false, // Dynamic color is available on Android 12+
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> when (appTheme) {
            AppTheme.GREEN -> DarkGreenColorScheme
            AppTheme.BLUE -> DarkBlueColorScheme
            AppTheme.ORANGE -> DarkOrangeColorScheme
            AppTheme.PINK -> DarkPinkColorScheme
        }
        else -> when (appTheme) {
            AppTheme.GREEN -> LightGreenColorScheme
            AppTheme.BLUE -> LightBlueColorScheme
            AppTheme.ORANGE -> LightOrangeColorScheme
            AppTheme.PINK -> LightPinkColorScheme
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}