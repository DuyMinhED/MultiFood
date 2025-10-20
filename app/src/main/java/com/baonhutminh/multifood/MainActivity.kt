package com.baonhutminh.multifood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.baonhutminh.multifood.ui.theme.MultiFoodTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiFoodTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home"){


                }
            }
        }
    }
}

