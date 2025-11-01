package com.baonhutminh.multifood.ui

import android.transition.Explode
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ExplodeScreen() {
    Box(modifier = Modifier.fillMaxSize()){
        Text(text = "Explode Screen")
    }
}