package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminModule
import com.example.data.model.DashboardStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    stats: DashboardStats,
    onNavigateToModule: (AdminModule) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNotificationDialog by remember { mutableStateOf(false) }

    val currentDateStr = remember {
        val sdf = SimpleDateFormat("d MMMM yyyy, EEEE", Locale("tr", "TR"))
        sdf.format(Date())
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Compact Header
        item(span = { GridItemSpan(2) }) {
            DashboardGreetingHeader(
                currentDate = currentDateStr,
                unreadNotificationCount = 3,
                onOpenNotifications = { showNotificationDialog = true }
            )
        }

        // ==================== 5 CORE KPI CARDS ====================

        // KPI 1: Hero Revenue Card (Bu Ay Gelir) with Refined Sparkline
        item(span = { GridItemSpan(2) }) {
            RevenueHeroBentoCard(
                revenueText = stats.buAyGelir,
                growthPercentage = "+12%",
                onCardClick = { onNavigateToModule(AdminModule.FINANS) }
            )
        }

        // KPI 2: Bugünkü Randevu
        item {
            TodayAppointmentsBentoCard(
                count = stats.bugunkuRandevu,
                onClick = { onNavigateToModule(AdminModule.RANDEVULAR) }
            )
        }

        // KPI 3: Açık Alacak
        item {
            ReceivablesBentoCard(
                amountText = stats.acikAlacak,
                onClick = { onNavigateToModule(AdminModule.FINANS) }
            )
        }

        // KPI 4: Bekleyen Onay
        item {
            CompactMetricBentoCard(
                title = "BEKLEYEN ONAY",
                value = stats.bekleyenOnay.toString(),
                icon = Icons.Default.PendingActions,
                accentColor = Color(0xFFF59E0B),
                badgeText = "Aksiyon",
                testTag = "stat_pending_approvals",
                onClick = { onNavigateToModule(AdminModule.TEKLIFLER) }
            )
        }

        // KPI 5: Haftalık Tamamlanan
        item {
            CompactMetricBentoCard(
                title = "HAFTALIK TAMAMLANAN",
                value = stats.buHaftaTamamlanan.toString(),
                icon = Icons.Default.CheckCircle,
                accentColor = Color(0xFF10B981),
                badgeText = "Tamamlanan",
                testTag = "stat_weekly_completed",
                onClick = { onNavigateToModule(AdminModule.RANDEVULAR) }
            )
        }

        // ==================== HIZLI İŞLEMLER (YÖNETİM MODÜLLERİ YERİNE AŞAĞIDA) ====================
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hızlı İşlemler",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Sık Kullanılanlar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        item {
            QuickActionGridCard(
                title = "Yeni Randevu",
                subtitle = "Randevu ekle & planla",
                icon = Icons.Default.DateRange,
                accentColor = Color(0xFF0288D1),
                onClick = { onNavigateToModule(AdminModule.RANDEVULAR) }
            )
        }

        item {
            QuickActionGridCard(
                title = "Teklif Oluştur",
                subtitle = "Hızlı teklif sun",
                icon = Icons.Default.Description,
                accentColor = Color(0xFF8B5CF6),
                onClick = { onNavigateToModule(AdminModule.TEKLIFLER) }
            )
        }

        item {
            QuickActionGridCard(
                title = "Müşteri Ekle",
                subtitle = "Yeni müşteri kaydı",
                icon = Icons.Default.PersonAdd,
                accentColor = Color(0xFF10B981),
                onClick = { onNavigateToModule(AdminModule.MUSTERILER) }
            )
        }

        item {
            QuickActionGridCard(
                title = "Kasa & Gelir",
                subtitle = "Finansal hareket",
                icon = Icons.Default.Wallet,
                accentColor = Color(0xFF059669),
                onClick = { onNavigateToModule(AdminModule.FINANS) }
            )
        }

        item {
            QuickActionGridCard(
                title = "WhatsApp Gönder",
                subtitle = "Müşteriye bildir",
                icon = Icons.AutoMirrored.Filled.Chat,
                accentColor = Color(0xFF25D366),
                onClick = { onNavigateToModule(AdminModule.MESAJ_SISTEMI) }
            )
        }

        item {
            QuickActionGridCard(
                title = "Bakım Takvimi",
                subtitle = "Periyodik bakımlar",
                icon = Icons.Default.Handyman,
                accentColor = Color(0xFFF97316),
                onClick = { onNavigateToModule(AdminModule.BAKIM_TAKVIMLERI) }
            )
        }

        // Bottom Spacing for Floating Nav Bar
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    if (showNotificationDialog) {
        NotificationCenterDialog(onDismiss = { showNotificationDialog = false })
    }
}

@Composable
private fun DashboardGreetingHeader(
    currentDate: String,
    unreadNotificationCount: Int,
    onOpenNotifications: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile Avatar Box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SK",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Günaydın, Sancak Kombi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentDate,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Notification Bell with Badge Counter
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(
                    onClick = onOpenNotifications,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Bildirimler",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (unreadNotificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unreadNotificationCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OperationalAlertBanner(
    pendingCount: Int,
    pendingApprovals: Int,
    onActionClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onActionClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PulseEffectDot(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Operasyon Durumu",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Bugün $pendingCount aktif randevu ve $pendingApprovals onay bekleyen teklifiniz var.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun QuickActionGridCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun RevenueHeroBentoCard(
    revenueText: String,
    growthPercentage: String,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("stat_monthly_revenue"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "BU AY GELİR (CİRO)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.4.sp
                    )
                }

                // Green Trend Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = growthPercentage,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = revenueText,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Geçen aya kıyasla +%12 artış",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Dedicated Sparkline Mini Chart Container
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.05f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    SparklineCanvasChart(
                        modifier = Modifier.fillMaxSize(),
                        lineColor = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayAppointmentsBentoCard(
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("stat_today_appointments"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0288D1).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color(0xFF0288D1),
                        modifier = Modifier.size(18.dp)
                    )
                }

                PulseEffectDot(color = Color(0xFF22C55E))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "BUGÜNKÜ RANDEVU",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = count.toString(),
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tamamlanmayı bekliyor",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReceivablesBentoCard(
    amountText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("stat_receivables"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Surface(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Tahsil Et",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "AÇIK ALACAK",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = amountText,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFEF4444)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Müşteri ödemeleri takibi",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CompactMetricBentoCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    badgeText: String,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CompactBentoModuleCard(
    module: AdminModule,
    stats: DashboardStats,
    onClick: () -> Unit
) {
    val (moduleAccent, badgeText) = when (module) {
        AdminModule.MUSTERILER -> Color(0xFF0288D1) to "123 Kayıtlı"
        AdminModule.RANDEVULAR -> Color(0xFF10B981) to "${stats.bugunkuRandevu} Bugünkü"
        AdminModule.MESAJ_SISTEMI -> Color(0xFF8B5CF6) to "SMS & WA"
        AdminModule.ISTATISTIKLER -> Color(0xFF3B82F6) to "Raporlar"
        AdminModule.FINANS -> Color(0xFF059669) to stats.acikAlacak
        AdminModule.TEKLIFLER -> Color(0xFF6366F1) to "3 Aktif"
        AdminModule.BAKIM_TAKVIMLERI -> Color(0xFFEC4899) to "2 Yaklaşan"
        AdminModule.GOOGLE_ADS -> Color(0xFFEA4335) to "%98 Performans"
        AdminModule.WHATSAPP_CONNECT -> Color(0xFF25D366) to "Bağlı"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("module_card_${module.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(moduleAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = module.icon,
                        contentDescription = module.title,
                        tint = moduleAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = moduleAccent.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = moduleAccent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = module.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
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
    CompactMetricBentoCard(
        title = label,
        value = value,
        icon = icon,
        accentColor = accentColor,
        badgeText = badgeText,
        testTag = testTag,
        onClick = {}
    )
}

@Composable
fun ModuleCard(
    module: AdminModule,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompactBentoModuleCard(
        module = module,
        stats = DashboardStats(1, 2, 14, "₺500", 28, "₺8.870"),
        onClick = onClick
    )
}

@Composable
private fun SparklineCanvasChart(
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF10B981)
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val points = listOf(
            0.05f to 0.80f,
            0.22f to 0.65f,
            0.40f to 0.70f,
            0.58f to 0.42f,
            0.75f to 0.50f,
            0.92f to 0.18f
        )

        val path = Path()
        val fillPath = Path()

        val firstX = points[0].first * width
        val firstY = points[0].second * height

        path.moveTo(firstX, firstY)
        fillPath.moveTo(firstX, height)
        fillPath.lineTo(firstX, firstY)

        for (i in 1 until points.size) {
            val prevX = points[i - 1].first * width
            val prevY = points[i - 1].second * height
            val currX = points[i].first * width
            val currY = points[i].second * height

            val controlX1 = prevX + (currX - prevX) / 2f
            val controlY1 = prevY
            val controlX2 = prevX + (currX - prevX) / 2f
            val controlY2 = currY

            path.cubicTo(controlX1, controlY1, controlX2, controlY2, currX, currY)
            fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, currX, currY)
        }

        val lastX = points.last().first * width
        val lastY = points.last().second * height

        fillPath.lineTo(lastX, height)
        fillPath.close()

        // Gradient Fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.35f),
                    lineColor.copy(alpha = 0.02f)
                )
            )
        )

        // Curve Stroke
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Endpoint Glow Dot
        drawCircle(
            color = lineColor.copy(alpha = 0.25f),
            radius = 6.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(lastX, lastY)
        )
        drawCircle(
            color = lineColor,
            radius = 3.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(lastX, lastY)
        )
    }
}

@Composable
private fun PulseEffectDot(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF22C55E)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha * 0.45f))
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
private fun NotificationCenterDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bildirim Merkezi", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                NotificationItemCard(
                    title = "Teklif Onayı Bekliyor",
                    desc = "Teklif #1024 müşteriden yanıt beklemektedir.",
                    time = "10 dk önce",
                    isUnread = true
                )
                NotificationItemCard(
                    title = "Yaklaşan Periyodik Bakım",
                    desc = "Ahmet Yılmaz müşterisinin 1 yıllık kombi bakımı geldi.",
                    time = "1 saat önce",
                    isUnread = true
                )
                NotificationItemCard(
                    title = "Servis Tamamlandı",
                    desc = "Kombisi tamir edilen servis tamamlandı olarak işaretlendi.",
                    time = "3 saat önce",
                    isUnread = false
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun NotificationItemCard(
    title: String,
    desc: String,
    time: String,
    isUnread: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isUnread) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, if (isUnread) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = time, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
