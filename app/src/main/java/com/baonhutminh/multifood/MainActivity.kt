package com.baonhutminh.multifood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.baonhutminh.multifood.ui.navigation.AppBottomBar
import com.baonhutminh.multifood.ui.navigation.AppNavGraph
import com.baonhutminh.multifood.ui.navigation.AppTopBar
import com.baonhutminh.multifood.ui.theme.MultiFoodTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiFoodApp()
        }
    }
}

@Composable
fun MultiFoodApp() {
    MultiFoodTheme {
        val navController = rememberNavController()
        Scaffold(
            topBar = { AppTopBar("MultiFood") },
            bottomBar = { AppBottomBar(navController) }
        ) { innerPadding ->
            AppNavGraph(navController, innerPadding)
        }
    }
}
