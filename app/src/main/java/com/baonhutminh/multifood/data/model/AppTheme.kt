package com.baonhutminh.multifood.data.model

import androidx.compose.ui.graphics.Color

// Add the previewColor property to the enum
enum class AppTheme(
    val displayName: String,
    val previewColor: Color
) {
    ORANGE("Cam", Color(0xFFFF6B35)),
    BLUE("Xanh dương", Color(0xFF2196F3)),
    GREEN("Xanh lá", Color(0xFF4CAF50)),
    PINK("Hồng", Color(0xFFE91E63))
}
