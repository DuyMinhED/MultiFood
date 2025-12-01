package com.baonhutminh.multifood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.baonhutminh.multifood.data.model.AppTheme
import com.baonhutminh.multifood.data.preferences.SettingsPreferences
import com.baonhutminh.multifood.ui.navigation.AppNavigation
import com.baonhutminh.multifood.ui.theme.MultiFoodTheme
import com.baonhutminh.multifood.ui.theme.Typography
import com.baonhutminh.multifood.ui.theme.md_theme_dark_primary
import com.baonhutminh.multifood.ui.theme.md_theme_light_primary
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Define color schemes for other themes directly here for simplicity
val LightGreenColorScheme = lightColorScheme(primary = Color(0xFF4CAF50))
val DarkGreenColorScheme = darkColorScheme(primary = Color(0xFF81C784))
val LightBlueColorScheme = lightColorScheme(primary = Color(0xFF2196F3))
val DarkBlueColorScheme = darkColorScheme(primary = Color(0xFF64B5F6))
val LightPinkColorScheme = lightColorScheme(primary = Color(0xFFE91E63))
val DarkPinkColorScheme = darkColorScheme(primary = Color(0xFFF48FB1))

// The default Orange theme is now defined in ui.theme.Theme.kt

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsPreferences: SettingsPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        setContent {
            val currentTheme by settingsPreferences.appTheme.collectAsState(initial = AppTheme.ORANGE)
            val darkTheme by settingsPreferences.darkModeEnabled.collectAsState(initial = false) // Sửa ở đây

            MultiFoodTheme(darkTheme = darkTheme) {
                // This is a simplified approach. For a full theme, you'd define
                // complete color schemes for each theme choice like in Theme.kt
                val colorScheme = when (currentTheme) {
                    AppTheme.GREEN -> if (darkTheme) DarkGreenColorScheme else LightGreenColorScheme
                    AppTheme.BLUE -> if (darkTheme) DarkBlueColorScheme else LightBlueColorScheme
                    AppTheme.PINK -> if (darkTheme) DarkPinkColorScheme else LightPinkColorScheme
                    AppTheme.ORANGE -> MaterialTheme.colorScheme // Use the one from MultiFoodTheme
                }

                MaterialTheme(colorScheme = colorScheme, typography = Typography) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}
