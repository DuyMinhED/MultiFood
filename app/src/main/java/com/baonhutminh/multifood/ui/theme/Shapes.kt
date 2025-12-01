package com.baonhutminh.multifood.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // For small chips, badges
    small = RoundedCornerShape(8.dp),        // For buttons, small cards
    medium = RoundedCornerShape(12.dp),      // For standard cards
    large = RoundedCornerShape(16.dp),       // For large cards, bottom sheets
    extraLarge = RoundedCornerShape(24.dp)   // For dialogs, hero images
)