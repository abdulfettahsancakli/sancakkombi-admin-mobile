package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminModule
import com.example.data.model.DashboardStats

@Composable
fun DashboardScreen(
    stats: DashboardStats,
    onNavigateToModule: (AdminModule) -> Unit,
    modifier: Modifier = Modifier
) {
    val modules = AdminModule.values().toList()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 158.dp),
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Modernized Hero Welcome Header
        item(span = { GridItemSpan(maxLineSpan) }) {
            DashboardHeroHeader()
        }

        // Section Title: Quick Stats
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Performans ve Özet",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // 6 Modern Stat Cards
        item {
            ModernStatCard(
                label = "BUGÜNKÜ RANDEVU",
                value = stats.bugunkuRandevu.toString(),
                icon = Icons.Default.Event,
                accentColor = Color(0xFF0288D1),
                badgeText = "Günlük",
                testTag = "stat_today_appointments"
            )
        }

        item {
            ModernStatCard(
                label = "BEKLEYEN ONAY",
                value = stats.bekleyenOnay.toString(),
                icon = Icons.Default.PendingActions,
                accentColor = Color(0xFFF59E0B),
                badgeText = "Aksiyon",
                testTag = "stat_pending_approvals"
            )
        }

        item {
            ModernStatCard(
                label = "BU HAFTA TAMAMLANAN",
                value = stats.buHaftaTamamlanan.toString(),
                icon = Icons.Default.CheckCircle,
                accentColor = Color(0xFF10B981),
                badgeText = "Haftalık",
                testTag = "stat_weekly_completed"
            )
        }

        item {
            ModernStatCard(
                label = "AÇIK ALACAK",
                value = stats.acikAlacak,
                icon = Icons.Default.AccountBalanceWallet,
                accentColor = Color(0xFFEF4444),
                badgeText = "Takip Et",
                testTag = "stat_receivables"
            )
        }

        item {
            ModernStatCard(
                label = "BU AY SERVİS",
                value = stats.buAyServis.toString(),
                icon = Icons.Default.Handyman,
                accentColor = Color(0xFF8B5CF6),
                badgeText = "Aylık",
                testTag = "stat_monthly_services"
            )
        }

        item {
            ModernStatCard(
                label = "BU AY GELİR",
                value = stats.buAyGelir,
                icon = Icons.Default.TrendingUp,
                accentColor = Color(0xFF059669),
                badgeText = "Ciro",
                testTag = "stat_monthly_revenue"
            )
        }

        // Section Title: Admin Modules
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Yönetim Modülleri",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }

        // 9 Modernized Admin Module Cards
        items(modules) { module ->
            ModernModuleCard(
                module = module,
                onClick = { onNavigateToModule(module) }
            )
        }

        // Bottom Spacing
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun DashboardHeroHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0288D1),
                            Color(0xFF01579B)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Sancak Kombi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Operasyonel süreçlerinize ve saha modüllerinize anında erişin.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ModernStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    badgeText: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (accentColor == Color(0xFFEF4444) || accentColor == Color(0xFF10B981) || accentColor == Color(0xFF059669)) accentColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ModuleCard(
    module: AdminModule,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernModuleCard(module = module, onClick = onClick, modifier = modifier)
}

@Composable
fun ModernModuleCard(
    module: AdminModule,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val moduleAccent = when (module) {
        AdminModule.MUSTERILER -> Color(0xFF0288D1)
        AdminModule.RANDEVULAR -> Color(0xFF10B981)
        AdminModule.MESAJ_SISTEMI -> Color(0xFF8B5CF6)
        AdminModule.ISTATISTIKLER -> Color(0xFF3B82F6)
        AdminModule.FINANS -> Color(0xFF059669)
        AdminModule.TEKLIFLER -> Color(0xFF6366F1)
        AdminModule.BAKIM_TAKVIMLERI -> Color(0xFFEC4899)
        AdminModule.GOOGLE_ADS -> Color(0xFFEA4335)
        AdminModule.WHATSAPP_CONNECT -> Color(0xFF25D366)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("module_card_${module.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(moduleAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = module.icon,
                    contentDescription = module.title,
                    tint = moduleAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = module.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = module.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Aç",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = moduleAccent
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(moduleAccent.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Aç",
                        tint = moduleAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

