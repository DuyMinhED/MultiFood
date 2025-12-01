package com.baonhutminh.multifood.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.baonhutminh.multifood.ui.navigation.Screen

@Composable
fun AppTopBar(screen: Screen) {
    Surface(
        color = MaterialTheme.colorScheme.surface, // Trắng (#FFFFFF)
        contentColor = MaterialTheme.colorScheme.primary, // Đen (#000000)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = screen.title.orEmpty(),
                style = MaterialTheme.typography.headlineMedium // Font Bold 28sp
            )
        }
    }
}


@Composable
fun AppBottomBar(
    onClickHome: () -> Unit,
    onAccountClick: () -> Unit,
    _selectehome: Boolean = true
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected =_selectehome ,
            onClick = onClickHome,
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = {
                Text("Trang chủ", style = MaterialTheme.typography.labelMedium)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        NavigationBarItem(
            selected = !_selectehome,
            onClick = onAccountClick,
            icon = { Icon(Icons.Outlined.Person, contentDescription = "Account") },
            label = {
                Text("Tài khoản", style = MaterialTheme.typography.labelMedium)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}