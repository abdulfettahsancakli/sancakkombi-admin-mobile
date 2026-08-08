package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("bottom_nav_bar")
    ) {
        // Floating 28dp Radius Glassmorphism Bar
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route ||
                            (item.route == "appointments" && currentRoute == "randevular") ||
                            (item.route == "customers" && currentRoute == "musteriler")

                    val animatedScale by animateFloatAsState(targetValue = if (isSelected) 1.1f else 1.0f, label = "scale")
                    val activeColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "color"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                onNavigate(item.route, item.module)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.scale(animatedScale)
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp, 30.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                )
                            }
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = activeColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = item.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = activeColor
                        )

                        // Active Dot Indicator
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

