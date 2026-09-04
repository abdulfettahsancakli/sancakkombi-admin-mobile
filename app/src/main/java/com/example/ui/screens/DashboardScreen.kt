package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.LaunchedEffect
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
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

// ============================================================================
// CHANGES IN THIS FILE (blue-accent restyle, warmer copy, micro-animations)
// 1. Greeting copy is warmer/more personal ("Günaydın, Usta! 👋" instead of
//    "Günaydın, Sancak Kombi") — see DashboardGreetingHeader.
// 2. Revenue card subtitle is more encouraging ("Harika gidiyorsun...").
// 3. QuickActionGridCard now has a press-scale micro-interaction (spring).
// 4. The whole grid fades + expands in on first composition.
// 5. Brand accent (green -> sky blue) is NOT changed here — it comes from
//    MaterialTheme.colorScheme.primary, so update ui/theme/Color.kt and
//    ui/theme/Theme.kt (see the two files delivered alongside this one).
// 6. The 🔔 notification panel is now wired to the real clock: the 09:00 /
//    12:00 daily cards only appear once that time has actually passed, their
//    label switches to a real "X dakika/saat önce", the next-appointment card
//    shows a live countdown, and the whole thing re-evaluates every minute
//    while the screen is open (see nowMillis / LaunchedEffect below).
// ============================================================================

@Composable
fun DashboardScreen(
    stats: DashboardStats,
    appointments: List<com.example.data.model.Appointment> = emptyList(),
    onNavigateToModule: (AdminModule) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNotificationDialog by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) { contentVisible = true }

    // Live clock for the notification panel — ticks every minute so labels
    // ("12 dakika önce", "38 dakika sonra"...) stay accurate while the screen is open.
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            nowMillis = System.currentTimeMillis()
        }
    }

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

    val dynamicNotifications = remember(stats, todayAppts, nowMillis) {
        val list = mutableListOf<DynamicNotificationItem>()
        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }

        val firstAppt = todayAppts.firstOrNull()
        val morningDesc = if (firstAppt != null) {
            "Bugün toplam ${todayAppts.size} randevunuz var, hepsini birlikte hallederiz! İlk servis: ${firstAppt.timeSlot} - ${firstAppt.customerName} (${firstAppt.district})"
        } else {
            "Bugün için planlanmış randevunuz bulunmuyor. İyi çalışmalar dileriz!"
        }
        // 09:00 card — only once 09:00 has actually passed; label = real time-ago
        if (isAtOrAfter(now, 9, 0)) {
            list.add(
                DynamicNotificationItem(
                    title = "☀️ Günün Randevu Özeti (09:00)",
                    desc = morningDesc,
                    time = timeAgoLabel(now, 9, 0),
                    isUnread = minutesSince(now, 9, 0) < 120,
                    icon = Icons.Default.DateRange,
                    iconColor = Color(0xFF0288D1),
                    targetModule = AdminModule.RANDEVULAR
                )
            )
        }

        // 12:00 card — only once noon has actually passed
        if (isAtOrAfter(now, 12, 0)) {
            list.add(
                DynamicNotificationItem(
                    title = "🕛 Gün Ortası Durumu (12:00)",
                    desc = "Günün ilk yarısı tamamlandı. Kalan servislerinizi ve günün akışını kontrol edebilirsiniz.",
                    time = timeAgoLabel(now, 12, 0),
                    isUnread = minutesSince(now, 12, 0) < 120,
                    icon = Icons.Default.PendingActions,
                    iconColor = Color(0xFFF59E0B),
                    targetModule = AdminModule.RANDEVULAR
                )
            )
        }

        // Next-appointment card — live countdown/elapsed based on its real time slot
        if (firstAppt != null) {
            val (h, m) = parseSlotStart(firstAppt.timeSlot)
            val diffMin = minutesUntil(now, h, m)
            val liveLabel = when {
                diffMin > 0 && diffMin < 60 -> "$diffMin dakika sonra"
                diffMin >= 60 -> "${diffMin / 60} saat ${diffMin % 60} dk sonra"
                diffMin == 0 -> "Şimdi"
                else -> "${-diffMin} dakika önce"
            }
            list.add(
                DynamicNotificationItem(
                    title = "⏰ Yaklaşan Randevu",
                    desc = "${firstAppt.customerName} • ${firstAppt.serviceType} (${firstAppt.district}, ${firstAppt.timeSlot})",
                    time = liveLabel,
                    isUnread = diffMin > -120,
                    icon = Icons.Default.Bolt,
                    iconColor = Color(0xFF10B981),
                    targetModule = AdminModule.RANDEVULAR
                )
            )
        }

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

    AnimatedVisibility(
        visible = contentVisible,
        enter = fadeIn(tween(400)) + expandVertically(tween(400))
    ) {
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
            item(span = { GridItemSpan(2) }) {
                DashboardGreetingHeader(
                    currentDate = currentDateStr,
                    unreadNotificationCount = unreadCount,
                    onOpenNotifications = { showNotificationDialog = true }
                )
            }

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
                    subtitle = "Hemen bir randevu planla",
                    icon = "📅",
                    accentColor = Color(0xFF0288D1),
                    onClick = { onNavigateToModule(AdminModule.RANDEVULAR) }
                )
            }

            item {
                QuickActionGridCard(
                    title = "Teklif Oluştur",
                    subtitle = "Hızlı teklif sun",
                    icon = "📝",
                    accentColor = Color(0xFF8B5CF6),
                    onClick = { onNavigateToModule(AdminModule.TEKLIFLER) }
                )
            }

            item {
                QuickActionGridCard(
                    title = "Müşteri Ekle",
                    subtitle = "Yeni müşteri kaydı",
                    icon = "🙋",
                    accentColor = Color(0xFF10B981),
                    onClick = { onNavigateToModule(AdminModule.MUSTERILER) }
                )
            }

            item {
                QuickActionGridCard(
                    title = "Kasa & Gelir",
                    subtitle = "Finansal hareket",
                    icon = "💰",
                    accentColor = Color(0xFF059669),
                    onClick = { onNavigateToModule(AdminModule.FINANS) }
                )
            }

            item {
                QuickActionGridCard(
                    title = "WhatsApp Gönder",
                    subtitle = "Müşteriye bildir",
                    icon = "💬",
                    accentColor = Color(0xFF25D366),
                    onClick = { onNavigateToModule(AdminModule.MESAJ_SISTEMI) }
                )
            }

            item {
                QuickActionGridCard(
                    title = "Bakım Takvimi",
                    subtitle = "Periyodik bakımlar",
                    icon = "🛠️",
                    accentColor = Color(0xFFF97316),
                    onClick = { onNavigateToModule(AdminModule.BAKIM_TAKVIMLERI) }
                )
            }

            item(span = { GridItemSpan(2) }) {
                RevenueHeroBentoCard(
                    revenueText = stats.buAyGelir,
                    growthPercentage = "+12%",
                    onCardClick = { onNavigateToModule(AdminModule.FINANS) }
                )
            }

            item {
                TodayAppointmentsBentoCard(
                    count = stats.bugunkuRandevu,
                    onClick = { onNavigateToModule(AdminModule.RANDEVULAR) }
                )
            }

            item {
                ReceivablesBentoCard(
                    amountText = stats.acikAlacak,
                    onClick = { onNavigateToModule(AdminModule.FINANS) }
                )
            }

            item {
                CompactMetricBentoCard(
                    title = "BEKLEYEN ONAY",
                    value = stats.bekleyenOnay.toString(),
                    icon = "⏳",
                    accentColor = Color(0xFFF59E0B),
                    badgeText = "Aksiyon",
                    testTag = "stat_pending_approvals",
                    onClick = { onNavigateToModule(AdminModule.TEKLIFLER) }
                )
            }

            item {
                CompactMetricBentoCard(
                    title = "HAFTALIK TAMAMLANAN",
                    value = stats.buHaftaTamamlanan.toString(),
                    icon = "✅",
                    accentColor = Color(0xFF10B981),
                    badgeText = "Tamamlanan",
                    testTag = "stat_weekly_completed",
                    onClick = { onNavigateToModule(AdminModule.RANDEVULAR) }
                )
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(72.dp))
            }
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
                    // Warmer, more personal greeting for the field technician
                    Text(
                        text = "Günaydın, Usta! 👋",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$currentDate · bugün harika bir gün olacak",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(
                    onClick = onOpenNotifications,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(text = "🔔", fontSize = 17.sp)
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
private fun QuickActionGridCard(
    title: String,
    subtitle: String,
    icon: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(),
        label = "card_press_scale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
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
                Text(text = icon, fontSize = 18.sp)
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
                        Text(text = "📈", fontSize = 15.sp)
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
                        Text(text = if (isRevenueVisible) "👁️" else "🙈", fontSize = 13.sp)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "▲ $growthPercentage",
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
                        // Warmer, encouraging microcopy instead of a flat stat line
                        text = if (isRevenueVisible) "🎉 Harika gidiyorsun, geçen aya göre +%12 artış" else "Tutar gizlendi",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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
                    Text(text = "📅", fontSize = 15.sp)
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
                text = "Seni bekliyor 💪",
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
                    Text(text = "🧾", fontSize = 15.sp)
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
    icon: String,
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
                    Text(text = icon, fontSize = 14.sp)
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
fun ModernStatCard(
    label: String,
    value: String,
    icon: String,
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
    // unchanged — kept for compatibility with callers outside DashboardScreen
    CompactBentoModuleCardStub(module, onClick)
}

@Composable
private fun CompactBentoModuleCardStub(module: AdminModule, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = module.icon, contentDescription = module.title, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = module.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
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

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.35f),
                    lineColor.copy(alpha = 0.02f)
                )
            )
        )

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

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

// ---- Real-clock helpers for the notification panel ----

private fun isAtOrAfter(now: Calendar, hour: Int, minute: Int): Boolean {
    val target = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0)
    }
    return now.timeInMillis >= target.timeInMillis
}

private fun minutesSince(now: Calendar, hour: Int, minute: Int): Int {
    val target = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0)
    }
    return ((now.timeInMillis - target.timeInMillis) / 60_000L).toInt()
}

private fun minutesUntil(now: Calendar, hour: Int, minute: Int): Int {
    val target = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0)
    }
    return ((target.timeInMillis - now.timeInMillis) / 60_000L).toInt()
}

private fun timeAgoLabel(now: Calendar, hour: Int, minute: Int): String {
    val diff = minutesSince(now, hour, minute)
    return when {
        diff < 1 -> "Az önce"
        diff < 60 -> "$diff dakika önce"
        else -> "${diff / 60} saat önce"
    }
}

/** Parses a slot like "14:00" or "14:00-16:00" into (hour, minute). */
private fun parseSlotStart(timeSlot: String): Pair<Int, Int> {
    val start = timeSlot.split("-").firstOrNull()?.trim() ?: timeSlot
    val parts = start.split(":")
    val hour = parts.getOrNull(0)?.filter { it.isDigit() }?.toIntOrNull() ?: 9
    val minute = parts.getOrNull(1)?.filter { it.isDigit() }?.toIntOrNull() ?: 0
    return hour to minute
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
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "09:00 & 12:00 Aktif",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
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
