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
import androidx.compose.material.icons.filled.Bolt
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.saveable.rememberSaveable
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
    appointments: List<com.example.data.model.Appointment> = emptyList(),
    onNavigateToModule: (AdminModule) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNotificationDialog by remember { mutableStateOf(false) }

    val currentDateStr = remember {
        val sdf = SimpleDateFormat("d MMMM yyyy, EEEE", Locale("tr", "TR"))
        sdf.format(Date())
    }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    val todayAppts = remember(appointments, todayStr) {
        appointments.filter { it.date == todayStr || it.date.contains(todayStr) }.ifEmpty { appointments.take(4) }
    }

    val dynamicNotifications = remember(stats, todayAppts) {
        val list = mutableListOf<DynamicNotificationItem>()

        // 1. 09:00 Sabah Bildirimi
        val firstAppt = todayAppts.firstOrNull()
        val morningDesc = if (firstAppt != null) {
            "Bugün toplam ${todayAppts.size} randevunuz var. İlk servis: ${firstAppt.timeSlot} - ${firstAppt.customerName} (${firstAppt.district})"
        } else {
            "Bugün için planlanmış randevunuz bulunmuyor. İyi çalışmalar dileriz!"
        }
        list.add(
            DynamicNotificationItem(
                title = "☀️ Günün Randevu Özeti (09:00)",
                desc = morningDesc,
                time = "09:00",
                isUnread = true,
                icon = Icons.Default.DateRange,
                iconColor = Color(0xFF0288D1),
                targetModule = AdminModule.RANDEVULAR
            )
        )

        // 2. 12:00 Öğlen Takibi
        list.add(
            DynamicNotificationItem(
                title = "🕛 Gün Ortası Durumu (12:00)",
                desc = "Günün ilk yarısı tamamlandı. Kalan servislerinizi ve günün akışını kontrol edebilirsiniz.",
                time = "12:00",
                isUnread = true,
                icon = Icons.Default.PendingActions,
                iconColor = Color(0xFFF59E0B),
                targetModule = AdminModule.RANDEVULAR
            )
        )

        // 3. Sıradaki Yaklaşan Randevu
        if (firstAppt != null) {
            list.add(
                DynamicNotificationItem(
                    title = "⏰ Yaklaşan Randevu",
                    desc = "${firstAppt.customerName} • ${firstAppt.serviceType} (${firstAppt.district}, ${firstAppt.timeSlot})",
                    time = "1 saat önce",
                    isUnread = true,
                    icon = Icons.Default.Bolt,
                    iconColor = Color(0xFF10B981),
                    targetModule = AdminModule.RANDEVULAR
                )
            )
        }

        // 4. Teklif Onayları
        if (stats.bekleyenOnay > 0) {
            list.add(
                DynamicNotificationItem(
                    title = "📋 Bekleyen Teklif Yanıtı",
                    desc = "${stats.bekleyenOnay} adet müşteri teklifi yanıt beklemektedir.",
                    time = "Bugün",
                    isUnread = false,
                    icon = Icons.Default.Description,
                    iconColor = Color(0xFF8B5CF6),
                    targetModule = AdminModule.TEKLIFLER
                )
            )
        }

        // 5. Periyodik Bakım
        list.add(
            DynamicNotificationItem(
                title = "🔧 Yaklaşan Periyodik Bakımlar",
                desc = "Yıllık kombi bakımı yaklaşan müşterilerinizi Bakım Takvimi üzerinden inceleyebilirsiniz.",
                time = "Dün",
                isUnread = false,
                icon = Icons.Default.Handyman,
                iconColor = Color(0xFFF97316),
                targetModule = AdminModule.BAKIM_TAKVIMLERI
            )
        )

        list
    }

    val unreadCount = dynamicNotifications.count { it.isUnread }

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
                unreadNotificationCount = unreadCount,
                onOpenNotifications = { showNotificationDialog = true }
            )
        }

        // ==================== HIZLI İŞLEMLER (GÜNAYDIN BÖLÜMÜNÜN HEMEN ALTINDA) ====================
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hızlı İşlemler",
                    fontSize = 15.sp,
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

        // Bottom Spacing for Floating Nav Bar
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    if (showNotificationDialog) {
        NotificationCenterDialog(
            notifications = dynamicNotifications,
            onNotificationClick = { module ->
                onNavigateToModule(module)
            },
            onDismiss = { showNotificationDialog = false }
        )
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
    var isRevenueVisible by rememberSaveable { mutableStateOf(true) }

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
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable { isRevenueVisible = !isRevenueVisible }
                            .testTag("toggle_revenue_visibility"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRevenueVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isRevenueVisible) "Geliri Gizle" else "Geliri Göster",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
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
                        text = if (isRevenueVisible) revenueText else "₺ ••••••",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = if (isRevenueVisible) (-0.5).sp else 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isRevenueVisible) "Geçen aya kıyasla +%12 artış" else "Tutar gizlendi",
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
        AdminModule.MUSTERILER -> Color(0xFF0288D1) to "Müşteri listesi"
        AdminModule.RANDEVULAR -> Color(0xFF10B981) to "${stats.bugunkuRandevu} Bugünkü"
        AdminModule.MESAJ_SISTEMI -> Color(0xFF8B5CF6) to "WhatsApp"
        AdminModule.ISTATISTIKLER -> Color(0xFF3B82F6) to "Raporlar"
        AdminModule.FINANS -> Color(0xFF059669) to stats.acikAlacak
        AdminModule.TEKLIFLER -> Color(0xFF6366F1) to
            if (stats.bekleyenOnay > 0) "${stats.bekleyenOnay} Bekleyen" else "Bekleyen yok"
        AdminModule.BAKIM_TAKVIMLERI -> Color(0xFFEC4899) to "Bakım listesi"
        AdminModule.GOOGLE_ADS -> Color(0xFFEA4335) to "Reklam paneli"
        AdminModule.STOK -> Color(0xFF0EA5E9) to "Stok hareketleri"
        AdminModule.HIZLI_IBAN -> Color(0xFF22C55E) to "Hızlı Gönder"
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
        stats = DashboardStats(0, 0, 0, "₺0", 0, "₺0"),
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

data class DynamicNotificationItem(
    val title: String,
    val desc: String,
    val time: String,
    val isUnread: Boolean,
    val icon: ImageVector,
    val iconColor: Color,
    val targetModule: AdminModule
)

@Composable
private fun NotificationCenterDialog(
    notifications: List<DynamicNotificationItem>,
    onNotificationClick: (AdminModule) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Usta Bildirimleri", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF22C55E).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "09:00 & 12:00 Aktif",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                notifications.forEach { item ->
                    NotificationItemCard(
                        item = item,
                        onClick = {
                            onNotificationClick(item.targetModule)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun NotificationItemCard(
    item: DynamicNotificationItem,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (item.isUnread) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, if (item.isUnread) item.iconColor.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(item.iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.time,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.desc,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
