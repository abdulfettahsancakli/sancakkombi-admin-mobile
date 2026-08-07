package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminModule

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val module: AdminModule? = null
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String, AdminModule?) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            route = "dashboard",
            title = "Ana Sayfa",
            icon = Icons.Default.Dashboard
        ),
        BottomNavItem(
            route = "appointments",
            title = "Randevular",
            icon = Icons.Default.DateRange,
            module = AdminModule.RANDEVULAR
        ),
        BottomNavItem(
            route = "customers",
            title = "Müşteriler",
            icon = Icons.Default.People,
            module = AdminModule.MUSTERILER
        ),
        BottomNavItem(
            route = "finance",
            title = "Finans",
            icon = Icons.Default.AccountBalance,
            module = AdminModule.FINANS
        ),
        BottomNavItem(
            route = "modules",
            title = "Modüller",
            icon = Icons.Default.GridView
        )
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_nav_bar")
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route, item.module) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
