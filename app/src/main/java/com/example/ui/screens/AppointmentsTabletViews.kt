package com.example.ui.screens

import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// ============================================================================
// TABLET HEADER & 5 WIDE STAT CARDS
// ============================================================================

@Composable
fun TabletHeaderAndStats(
    t: AppointmentTokens,
    appointments: List<Appointment>,
    tabletView: String,
    onTabletViewChange: (String) -> Unit,
    statusFilter: AppointmentStatus?,
    onStatusFilterChange: (AppointmentStatus?) -> Unit,
    onBackClick: () -> Unit,
    onOpenNewAppointment: () -> Unit,
    onOpenServiceReceipt: () -> Unit
) {
    val totalCount = appointments.size
    val pendingCount = appointments.count { it.status == AppointmentStatus.BEKLIYOR }
    val confirmedCount = appointments.count { it.status == AppointmentStatus.ONAYLANDI }
    val completedCount = appointments.count { it.status == AppointmentStatus.TAMAMLANDI }
    val cancelledCount = appointments.count { it.status == AppointmentStatus.IPTAL }
    val cancelRate = if (totalCount > 0) ((cancelledCount * 100.0) / totalCount).roundToInt() else 0

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isWideHeader = maxWidth >= 780.dp

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Top Row: Back Button + Breadcrumb (and View Switcher when in compact portrait tablet)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = onBackClick,
                        shape = RoundedCornerShape(10.dp),
                        color = t.surface,
                        border = BorderStroke(1.dp, t.border),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("back_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "←",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = t.text
                            )
                            Text(
                                text = "Ana Panel",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = t.text,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Text(
                        text = "/  Saha Randevuları",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = t.text2,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                if (!isWideHeader) {
                    // On narrower portrait tablets, put View Switcher on the top bar so CTAs have full room below
                    TabletViewSwitcherBar(
                        t = t,
                        tabletView = tabletView,
                        onTabletViewChange = onTabletViewChange
                    )
                }
            }

            // Main Title & Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Randevu & Servis Yönetimi",
                    fontSize = if (isWideHeader) 21.sp else 19.sp,
                    fontWeight = FontWeight.Black,
                    color = t.text,
                    letterSpacing = (-0.5).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isWideHeader) {
                        TabletViewSwitcherBar(
                            t = t,
                            tabletView = tabletView,
                            onTabletViewChange = onTabletViewChange
                        )
                    }

                    // Servis Fişi Button
                    Surface(
                        onClick = onOpenServiceReceipt,
                        shape = RoundedCornerShape(14.dp),
                        color = t.amberC,
                        border = BorderStroke(1.dp, t.amber),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("service_receipt_button")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 15.dp)
                        ) {
                            Text(
                                text = "🧾 Servis Fişi",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = t.amber,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Yeni Randevu Button (Identical height & non-wrapping as Servis Fişi)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(t.primaryGradient)
                            .clickable { onOpenNewAppointment() }
                            .padding(horizontal = 16.dp)
                            .testTag("new_appointment_button")
                    ) {
                        Text(
                            text = "📅 Yeni Randevu",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // 5 Wide Stat Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TabletStatCard(
                t = t,
                label = "TOPLAM RANDEVU",
                icon = "📅",
                value = totalCount.toString(),
                accent = t.text,
                isActive = statusFilter == null,
                onClick = { onStatusFilterChange(null) },
                modifier = Modifier.weight(1f)
            )
            TabletStatCard(
                t = t,
                label = "ONAY BEKLİYOR",
                icon = "⏳",
                value = pendingCount.toString(),
                accent = t.amber,
                isActive = statusFilter == AppointmentStatus.BEKLIYOR,
                onClick = {
                    onStatusFilterChange(
                        if (statusFilter == AppointmentStatus.BEKLIYOR) null else AppointmentStatus.BEKLIYOR
                    )
                },
                modifier = Modifier.weight(1f)
            )
            TabletStatCard(
                t = t,
                label = "ONAYLANDI · SAHADA",
                icon = "🚐",
                value = confirmedCount.toString(),
                accent = t.primary,
                isActive = statusFilter == AppointmentStatus.ONAYLANDI,
                onClick = {
                    onStatusFilterChange(
                        if (statusFilter == AppointmentStatus.ONAYLANDI) null else AppointmentStatus.ONAYLANDI
                    )
                },
                modifier = Modifier.weight(1f)
            )
            TabletStatCard(
                t = t,
                label = "TAMAMLANDI",
                icon = "✅",
                value = completedCount.toString(),
                accent = t.green,
                isActive = statusFilter == AppointmentStatus.TAMAMLANDI,
                onClick = {
                    onStatusFilterChange(
                        if (statusFilter == AppointmentStatus.TAMAMLANDI) null else AppointmentStatus.TAMAMLANDI
                    )
                },
                modifier = Modifier.weight(1f)
            )
            TabletStatCard(
                t = t,
                label = "İPTAL ORANI",
                icon = "❌",
                value = "%$cancelRate",
                accent = t.red,
                isActive = statusFilter == AppointmentStatus.IPTAL,
                onClick = {
                    onStatusFilterChange(
                        if (statusFilter == AppointmentStatus.IPTAL) null else AppointmentStatus.IPTAL
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabletViewSwitcherBar(
    t: AppointmentTokens,
    tabletView: String,
    onTabletViewChange: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = t.surface,
        border = BorderStroke(1.dp, t.border),
        modifier = Modifier.height(42.dp)
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val views = listOf(
                "list" to "☰ Liste",
                "calendar" to "🗓️ Takvim",
                "map" to "🗺️ Harita"
            )
            views.forEach { (key, label) ->
                val isActive = tabletView == key
                Surface(
                    onClick = { onTabletViewChange(key) },
                    shape = RoundedCornerShape(11.dp),
                    color = if (isActive) t.primary else Color.Transparent,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isActive) Color.White else t.text2,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabletStatCard(
    t: AppointmentTokens,
    label: String,
    icon: String,
    value: String,
    accent: Color,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = t.surface,
        border = BorderStroke(if (isActive) 1.5.dp else 1.dp, if (isActive) accent else t.border),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = t.text2,
                    letterSpacing = 0.4.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(text = icon, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = accent
            )
        }
    }
}

// ============================================================================
// TABLET LANDSCAPE SPLIT VIEW (440dp LIST | 1fr DETAIL)
// ============================================================================

@Composable
fun TabletLandscapeSplitView(
    t: AppointmentTokens,
    allAppointments: List<Appointment>,
    filteredAppointments: List<Appointment>,
    selectedAppointment: Appointment?,
    onSelectAppointment: (Appointment) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    statusFilter: AppointmentStatus?,
    onStatusFilterChange: (AppointmentStatus?) -> Unit,
    customerAppointmentCounts: Map<String, Int>,
    onEditAppointment: (Appointment) -> Unit,
    onCancelAppointment: (Appointment) -> Unit,
    onCompleteOrReceipt: (Appointment) -> Unit,
    onDeleteAppointment: (Appointment) -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Left List Column (420.dp)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = t.surface,
            border = BorderStroke(1.dp, t.border),
            modifier = Modifier
                .width(420.dp)
                .fillMaxHeight()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppointmentsSearchBar(
                        t = t,
                        searchQuery = searchQuery,
                        onSearchQueryChange = onSearchQueryChange
                    )
                    StatusFilterChipsRow(
                        t = t,
                        allAppointments = allAppointments,
                        selectedStatus = statusFilter,
                        onSelectStatus = onStatusFilterChange
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(t.border)
                )

                if (filteredAppointments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "☕ Bu filtrede randevu bulunamadı.",
                            fontSize = 13.sp,
                            color = t.text2,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredAppointments, key = { it.id }) { appt ->
                            val isSelected = selectedAppointment?.id == appt.id
                            val st = statusMeta(appt.status, t)
                            val (startT, _) = parseSlotTimes(appt.timeSlot)

                            Surface(
                                onClick = { onSelectAppointment(appt) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) t.primaryC else Color.Transparent,
                                border = BorderStroke(1.dp, if (isSelected) t.primary else Color.Transparent),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.width(50.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = formatDateShort(appt.date),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = t.text2
                                        )
                                        Text(
                                            text = startT,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = t.text
                                        )
                                    }

                                    CustomerAvatarCircle(name = appt.customerName, size = 38.dp)

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = appt.customerName,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = t.text,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${appt.serviceType} · ${appt.district}",
                                            fontSize = 11.sp,
                                            color = t.text2,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = st.bg
                                    ) {
                                        Text(
                                            text = st.label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = st.color,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Right Detail Pane (1f)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = t.surface,
            border = BorderStroke(1.dp, t.border),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            val sel = selectedAppointment ?: filteredAppointments.firstOrNull()
            if (sel != null) {
                val isReturning = (customerAppointmentCounts[sel.customerName.lowercase(TrLocale)] ?: 0) > 1
                AppointmentDetailPane(
                    t = t,
                    appointment = sel,
                    isReturningCustomer = isReturning,
                    onEdit = { onEditAppointment(sel) },
                    onCancel = { onCancelAppointment(sel) },
                    onCompleteOrReceipt = { onCompleteOrReceipt(sel) },
                    onDelete = { onDeleteAppointment(sel) },
                    onShowToast = onShowToast
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Randevu seçin", fontSize = 14.sp, color = t.text2)
                }
            }
        }
    }
}

// ============================================================================
// APPOINTMENT DETAIL PANE (USED IN TABLET RIGHT PANE & BOTTOM SHEET)
// ============================================================================

@Composable
fun AppointmentDetailPane(
    t: AppointmentTokens,
    appointment: Appointment,
    isReturningCustomer: Boolean,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onCompleteOrReceipt: () -> Unit,
    onDelete: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cloudWhatsApp = LocalCloudWhatsAppActions.current
    val st = statusMeta(appointment.status, t)
    val svcCol = serviceColor(appointment.serviceType, t)
    var activeViewerPhotoUrl by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Avatar + Name + Tag + Phone + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CustomerAvatarCircle(
                    name = appointment.customerName,
                    size = 54.dp,
                    fontSize = 18.sp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = appointment.customerName,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            color = t.text
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = t.surface2,
                            border = BorderStroke(1.dp, t.border)
                        ) {
                            Text(
                                text = if (isReturningCustomer) "Eski müşteri" else "Yeni",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = t.text2,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = appointment.phone,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = t.text2
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = st.bg
                ) {
                    Text(
                        text = st.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = st.color,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // 3 Big Action Buttons: Ara / WhatsApp / Rota
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = {
                        onShowToast("📞 ${appointment.customerName} aranıyor (${appointment.phone})")
                        launchDialer(context, appointment.phone)
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = t.primaryC,
                    border = BorderStroke(1.dp, t.primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("📞", fontSize = 16.sp)
                        Text("Ara", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = t.primary)
                    }
                }

                Surface(
                    onClick = {
                        val message = "Merhaba ${appointment.customerName}, Sancak Kombi olarak ${appointment.date} ${appointment.timeSlot} randevunuz hakkında iletişime geçiyoruz."
                        cloudWhatsApp?.sendMessage(
                            appointment.phone, message, appointment.customerId, appointment.id
                        ) { result ->
                            onShowToast(if (result.isSuccess) "WhatsApp mesajı Cloud API ile gönderildi" else "WhatsApp gönderilemedi: ${result.exceptionOrNull()?.message.orEmpty()}")
                        } ?: onShowToast("WhatsApp Cloud API bağlantısı hazır değil")
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = t.greenC,
                    border = BorderStroke(1.dp, t.green),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("💬", fontSize = 16.sp)
                        Text("WhatsApp", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = t.green)
                    }
                }

                Surface(
                    onClick = {
                        onShowToast("🧭 Yol tarifi: ${appointment.district}")
                        launchNavigation(context, appointment.fullAddressDisplay, appointment.district)
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = t.surface2,
                    border = BorderStroke(1.dp, t.border),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🧭", fontSize = 16.sp)
                        Text("Rota", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = t.text)
                    }
                }
            }

            // 2x2 Info Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = t.surface2,
                    border = BorderStroke(1.dp, t.border),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("📅 TARİH & SAAT", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = t.text2)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatDateShort(appointment.date), fontSize = 15.sp, fontWeight = FontWeight.Black, color = t.text)
                        Text(
                            text = appointment.timeSlot,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = t.primary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = t.surface2,
                    border = BorderStroke(1.dp, t.border),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("🔧 HİZMET", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = t.text2)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(svcCol)
                            )
                            Text(
                                text = appointment.serviceType,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = t.text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "Randevu No: #${appointment.id.takeLast(6).uppercase()}",
                            fontSize = 11.5.sp,
                            color = t.text2
                        )
                    }
                }
            }

            // Address Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = t.surface2,
                border = BorderStroke(1.dp, t.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📍 HİZMET ADRESİ · ${appointment.district.uppercase(TrLocale)}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = t.text2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = appointment.fullAddressDisplay,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = t.text,
                        lineHeight = 19.sp
                    )
                }
            }

            // Fault & Note Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = t.surface2,
                border = BorderStroke(1.dp, if (appointment.noteDisplay.isNotBlank()) t.amber else t.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "⚠️ ARIZA & NOTLAR",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (appointment.noteDisplay.isNotBlank()) t.amber else t.text2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = appointment.noteDisplay.ifBlank { "Ek arıza notu bulunmuyor." },
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (appointment.noteDisplay.isNotBlank()) t.amber else t.text2
                    )
                }
            }

            // Servis Fotoğrafları Galerisi (Tamamlanmış / Raporlu Randevular için)
            val photos = appointment.jobReport?.photoUris ?: emptyList()
            if (photos.isNotEmpty()) {
                AppointmentPhotoGallery(
                    photoUris = photos,
                    t = t,
                    onPhotoClick = { activeViewerPhotoUrl = it }
                )
            }

            // Map Route Preview Strip
            Surface(
                onClick = {
                    onShowToast("🧭 Yol tarifi: ${appointment.district}")
                    launchNavigation(context, appointment.fullAddressDisplay, appointment.district)
                },
                shape = RoundedCornerShape(14.dp),
                color = if (t.isDark) Color(0xFF0E2238) else Color(0xFFE0F2FE),
                border = BorderStroke(1.dp, t.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = t.surface,
                        border = BorderStroke(1.dp, t.border),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "📍 ${appointment.district} · Yol Tarifi Al →",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = t.text,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Bottom Sticky Action Bar
        Surface(
            color = t.surface,
            border = BorderStroke(1.dp, t.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = onEdit,
                    shape = RoundedCornerShape(12.dp),
                    color = t.surface2,
                    border = BorderStroke(1.dp, t.border),
                    modifier = Modifier.height(44.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                        Text("✏️ Düzenle", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = t.text)
                    }
                }

                Surface(
                    onClick = onCancel,
                    shape = RoundedCornerShape(12.dp),
                    color = t.redC,
                    border = BorderStroke(1.dp, t.red),
                    modifier = Modifier.height(44.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                        Text("❌ İptal", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = t.red)
                    }
                }

                Surface(
                    onClick = onDelete,
                    shape = RoundedCornerShape(12.dp),
                    color = t.surface2,
                    border = BorderStroke(1.dp, t.border),
                    modifier = Modifier.height(44.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                        Text("🗑️ Sil", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = t.text2)
                    }
                }

                Surface(
                    onClick = onCompleteOrReceipt,
                    shape = RoundedCornerShape(12.dp),
                    color = t.green,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("complete_job_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (appointment.status == AppointmentStatus.TAMAMLANDI) "🧾 Servis Fişi" else "✓ Tamamla & Fiş Kes",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        if (activeViewerPhotoUrl != null) {
            PhotoViewerDialog(
                photoUrl = activeViewerPhotoUrl!!,
                onDismiss = { activeViewerPhotoUrl = null }
            )
        }
    }
}

// ============================================================================
// TABLET WEEK CALENDAR VIEW (7-DAY GRID 09:00..19:00)
// ============================================================================

@Composable
fun WeekCalendarView(
    t: AppointmentTokens,
    todayIso: String,
    appointments: List<Appointment>,
    isPortrait: Boolean,
    onSelectAppointment: (Appointment) -> Unit,
    modifier: Modifier = Modifier
) {
    var weekOffset by remember { mutableIntStateOf(0) }

    // Calculate Monday..Sunday of the target week around todayIso
    val weekDays = remember(todayIso, weekOffset) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val baseDate = try { sdf.parse(todayIso) } catch (_: Exception) { null } ?: Date()
        val cal = Calendar.getInstance().apply {
            time = baseDate
            val dow = get(Calendar.DAY_OF_WEEK)
            val diffToMonday = if (dow == Calendar.SUNDAY) -6 else (Calendar.MONDAY - dow)
            add(Calendar.DAY_OF_YEAR, diffToMonday + weekOffset * 7)
        }
        val dayNames = listOf("PZT", "SAL", "ÇAR", "PER", "CUM", "CMT", "PAZ")
        (0..6).map { idx ->
            val iso = sdf.format(cal.time)
            val dayNum = cal.get(Calendar.DAY_OF_MONTH)
            cal.add(Calendar.DAY_OF_YEAR, 1)
            Triple(iso, dayNames[idx], dayNum)
        }
    }

    val weekLabel = remember(weekDays) {
        val first = weekDays.first()
        val last = weekDays.last()
        "${first.third} – ${formatDateShort(last.first)} ${last.first.take(4)}"
    }

    val weekIsoSet = remember(weekDays) { weekDays.map { it.first }.toSet() }
    val weekApptCount = remember(appointments, weekIsoSet) {
        appointments.count { normalizeToIsoDate(it.date) in weekIsoSet }
    }

    val hours = remember { (9..19).toList() }
    val rowHeight: Dp = if (isPortrait) 64.dp else 46.dp

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = t.surface,
        border = BorderStroke(1.dp, t.border),
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = { weekOffset -= 1 },
                        shape = RoundedCornerShape(10.dp),
                        color = t.surface2,
                        border = BorderStroke(1.dp, t.border),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("‹", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = t.text)
                        }
                    }
                    Surface(
                        onClick = { weekOffset += 1 },
                        shape = RoundedCornerShape(10.dp),
                        color = t.surface2,
                        border = BorderStroke(1.dp, t.border),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("›", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = t.text)
                        }
                    }
                    Text(
                        text = weekLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = t.text
                    )
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = t.primaryC
                    ) {
                        Text(
                            text = "$weekApptCount randevu",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = t.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                Surface(
                    onClick = { weekOffset = 0 },
                    shape = RoundedCornerShape(10.dp),
                    color = t.surface2,
                    border = BorderStroke(1.dp, t.border)
                ) {
                    Text(
                        text = "Bu Hafta",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = t.text,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(t.border))

            // 7-Day Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(t.surface2)
            ) {
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("SAAT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = t.text2)
                }

                weekDays.forEach { (iso, dayName, dayNum) ->
                    val isToday = iso == todayIso
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isToday) t.primaryC else Color.Transparent)
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dayName,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isToday) t.primary else t.text2
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (isToday) t.primary else Color.Transparent,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = dayNum.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isToday) Color.White else t.text
                                )
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(t.border))

            // Scrollable Hour Grid
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Left Hour Column
                Column(modifier = Modifier.width(52.dp)) {
                    hours.forEach { h ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(rowHeight)
                                .padding(top = 4.dp, end = 6.dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Text(
                                text = "%02d:00".format(h),
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = t.text2
                            )
                        }
                    }
                }

                // 7 Day Columns
                weekDays.forEach { (iso, _, _) ->
                    val isToday = iso == todayIso
                    val dayAppts = appointments.filter { normalizeToIsoDate(it.date) == iso }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(rowHeight * hours.size)
                            .background(if (isToday) t.primaryC.copy(alpha = 0.05f) else Color.Transparent)
                    ) {
                        // Hour grid cells
                        Column(modifier = Modifier.fillMaxSize()) {
                            hours.forEach { _ ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(rowHeight)
                                        .border(0.5.dp, t.border.copy(alpha = 0.5f))
                                )
                            }
                        }

                        // Red current-time indicator line on today's column
                        if (isToday) {
                            val nowOffset = rowHeight * 5.4f
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = nowOffset),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(t.red)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .background(t.red)
                                )
                            }
                        }

                        // Appointment Blocks
                        dayAppts.forEach { appt ->
                            val (startT, _) = parseSlotTimes(appt.timeSlot)
                            val hourInt = (startT.take(2).toIntOrNull() ?: 9).coerceIn(9, 19)
                            val topOffset = rowHeight * (hourInt - 9) + 2.dp
                            val st = statusMeta(appt.status, t)

                            Surface(
                                onClick = { onSelectAppointment(appt) },
                                shape = RoundedCornerShape(8.dp),
                                color = st.bg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 3.dp)
                                    .offset(y = topOffset)
                                    .height(rowHeight - 4.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .fillMaxHeight()
                                            .background(st.color)
                                    )
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 5.dp, vertical = 3.dp),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "$startT · ${appt.customerName}",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = t.text,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = appt.district,
                                            fontSize = 9.5.sp,
                                            color = t.text2,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// TABLET ROUTE MAP VIEW (STOP LIST + INTERACTIVE ROUTE MAP CANVAS)
// ============================================================================

private val DistrictMapOffsets = mapOf(
    "Bayrampaşa" to (0.48f to 0.42f),
    "Gaziosmanpaşa" to (0.36f to 0.24f),
    "Eyüpsultan" to (0.58f to 0.20f),
    "Esenler" to (0.24f to 0.44f),
    "Fatih" to (0.68f to 0.62f),
    "Zeytinburnu" to (0.46f to 0.76f),
    "Küçükçekmece" to (0.16f to 0.72f),
    "Güngören" to (0.30f to 0.62f),
    "Bağcılar" to (0.18f to 0.52f),
    "Bahçelievler" to (0.28f to 0.74f),
    "Kağıthane" to (0.72f to 0.28f),
    "Şişli" to (0.78f to 0.40f),
    "Sultangazi" to (0.32f to 0.14f)
)

private val DistrictMapCenters = mapOf(
    "Bayrampaşa" to LatLng(41.0482, 28.9003),
    "Gaziosmanpaşa" to LatLng(41.0759, 28.9120),
    "Eyüpsultan" to LatLng(41.0478, 28.9339),
    "Esenler" to LatLng(41.0437, 28.8760),
    "Fatih" to LatLng(41.0193, 28.9497),
    "Zeytinburnu" to LatLng(40.9926, 28.9047),
    "Küçükçekmece" to LatLng(41.0006, 28.7800),
    "Güngören" to LatLng(41.0220, 28.8717),
    "Bağcılar" to LatLng(41.0390, 28.8567),
    "Bahçelievler" to LatLng(40.9971, 28.8506),
    "Kağıthane" to LatLng(41.0859, 28.9720),
    "Şişli" to LatLng(41.0602, 28.9877),
    "Sultangazi" to LatLng(41.1065, 28.8684)
)

private fun fallbackMapPositions(stops: List<Appointment>): Map<String, LatLng> =
    stops.mapIndexed { index, appointment ->
        val center = DistrictMapCenters[appointment.district] ?: LatLng(41.0482, 28.9003)
        val latJitter = ((index % 3) - 1) * 0.0025
        val lngJitter = (((index / 2) % 3) - 1) * 0.0035
        appointment.id to LatLng(center.latitude + latJitter, center.longitude + lngJitter)
    }.toMap()

@Suppress("DEPRECATION")
private suspend fun geocodeMapPositions(
    context: Context,
    stops: List<Appointment>
): Map<String, LatLng> = withContext(Dispatchers.IO) {
    val fallback = fallbackMapPositions(stops)
    val geocoder = Geocoder(context, TrLocale)
    stops.associate { appointment ->
        val query = listOf(appointment.fullAddressDisplay, appointment.district, "İstanbul")
            .filter { it.isNotBlank() }
            .joinToString(", ")
        val address = runCatching { geocoder.getFromLocationName(query, 1)?.firstOrNull() }.getOrNull()
        appointment.id to if (address != null) {
            LatLng(address.latitude, address.longitude)
        } else {
            fallback.getValue(appointment.id)
        }
    }
}

@Composable
fun RouteMapView(
    t: AppointmentTokens,
    todayIso: String,
    appointments: List<Appointment>,
    selectedAppointment: Appointment?,
    onSelectAppointment: (Appointment) -> Unit,
    onOpenDetailSheet: (Appointment) -> Unit,
    onShowToast: (String) -> Unit,
    isPortrait: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val activeDates = remember(appointments, todayIso) {
        appointments
            .filter { it.status != AppointmentStatus.IPTAL }
            .map { normalizeToIsoDate(it.date) }
            .distinct()
            .sortedDescending()
            .ifEmpty { listOf(todayIso) }
    }

    var selectedDayIso by remember(activeDates, todayIso) {
        mutableStateOf(if (todayIso in activeDates) todayIso else activeDates.first())
    }

    val stops = remember(appointments, selectedDayIso) {
        appointments
            .filter {
                it.status != AppointmentStatus.IPTAL &&
                    (selectedDayIso == "all" || normalizeToIsoDate(it.date) == selectedDayIso)
            }
            .sortedBy { it.timeSlot }
    }

    val activeStop = remember(stops, selectedAppointment) {
        stops.find { it.id == selectedAppointment?.id } ?: stops.firstOrNull()
    }

    val doneCount = stops.count { it.status == AppointmentStatus.TAMAMLANDI }

    if (isPortrait) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RouteMapCanvasPane(
                t = t,
                stops = stops,
                activeStop = activeStop,
                selectedDayIso = selectedDayIso,
                onSelectStop = onSelectAppointment,
                onOpenDetail = onOpenDetailSheet,
                onShowToast = onShowToast,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )
            RouteStopListPane(
                t = t,
                stops = stops,
                doneCount = doneCount,
                activeStop = activeStop,
                activeDates = activeDates,
                todayIso = todayIso,
                selectedDayIso = selectedDayIso,
                onSelectDay = { selectedDayIso = it },
                onSelectStop = onSelectAppointment,
                onOpenDetail = onOpenDetailSheet,
                onShowToast = onShowToast,
                onLaunchAllStops = {
                    onShowToast("🗺️ Tüm duraklar Google Maps rotasında açılıyor")
                    launchFullRouteInGoogleMaps(context, stops)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    } else {
        Row(
            modifier = modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            RouteStopListPane(
                t = t,
                stops = stops,
                doneCount = doneCount,
                activeStop = activeStop,
                activeDates = activeDates,
                todayIso = todayIso,
                selectedDayIso = selectedDayIso,
                onSelectDay = { selectedDayIso = it },
                onSelectStop = onSelectAppointment,
                onOpenDetail = onOpenDetailSheet,
                onShowToast = onShowToast,
                onLaunchAllStops = {
                    onShowToast("🗺️ Tüm duraklar Google Maps rotasında açılıyor")
                    launchFullRouteInGoogleMaps(context, stops)
                },
                modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight()
            )
            RouteMapCanvasPane(
                t = t,
                stops = stops,
                activeStop = activeStop,
                selectedDayIso = selectedDayIso,
                onSelectStop = onSelectAppointment,
                onOpenDetail = onOpenDetailSheet,
                onShowToast = onShowToast,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun RouteStopListPane(
    t: AppointmentTokens,
    stops: List<Appointment>,
    doneCount: Int,
    activeStop: Appointment?,
    activeDates: List<String>,
    todayIso: String,
    selectedDayIso: String,
    onSelectDay: (String) -> Unit,
    onSelectStop: (Appointment) -> Unit,
    onOpenDetail: (Appointment) -> Unit,
    onShowToast: (String) -> Unit,
    onLaunchAllStops: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = t.surface,
        border = BorderStroke(1.dp, t.border),
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🧭 Günün Rotası",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = t.text
                    )
                    Text(
                        text = "${stops.size} durak · $doneCount tamamlandı",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = t.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    activeDates.take(8).forEach { dIso ->
                        val isSel = selectedDayIso == dIso
                        val label = if (dIso == todayIso) "Bugün (${formatDateShort(dIso)})" else formatDateShort(dIso)
                        Surface(
                            onClick = { onSelectDay(dIso) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSel) t.primary else t.surface2,
                            border = BorderStroke(1.dp, if (isSel) t.primary else t.border)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSel) Color.White else t.text2,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                    val isAllSel = selectedDayIso == "all"
                    Surface(
                        onClick = { onSelectDay("all") },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isAllSel) t.primary else t.surface2,
                        border = BorderStroke(1.dp, if (isAllSel) t.primary else t.border)
                    ) {
                        Text(
                            text = "Tümü",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isAllSel) Color.White else t.text2,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(t.border))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(stops, key = { _, item -> item.id }) { idx, stop ->
                    val isSel = activeStop?.id == stop.id
                    val st = statusMeta(stop.status, t)
                    val isDone = stop.status == AppointmentStatus.TAMAMLANDI

                    Surface(
                        onClick = { onSelectStop(stop) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSel) t.primaryC else t.surface2,
                        border = BorderStroke(1.dp, if (isSel) t.primary else t.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isDone) t.green else t.primary,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = (idx + 1).toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stop.customerName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = t.text,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = stop.timeSlot,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = t.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "${stop.district} · ${stop.fullAddressDisplay}",
                                    fontSize = 11.5.sp,
                                    color = t.text2,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        onClick = {
                                            onShowToast("🧭 Yol tarifi: ${stop.district}")
                                            launchNavigation(context, stop.fullAddressDisplay, stop.district)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = t.surface,
                                        border = BorderStroke(1.dp, t.border)
                                    ) {
                                        Text(
                                            text = "🧭 Yol Tarifi",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = t.text,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Surface(
                                        onClick = { onOpenDetail(stop) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = t.surface,
                                        border = BorderStroke(1.dp, t.border)
                                    ) {
                                        Text(
                                            text = "Detay",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = t.text2,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = st.bg
                                    ) {
                                        Text(
                                            text = st.label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = st.color,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(t.primaryGradient)
                    .clickable { onLaunchAllStops() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🗺️ Tüm rotayı Google Maps'te aç",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun RouteMapCanvasPane(
    t: AppointmentTokens,
    stops: List<Appointment>,
    activeStop: Appointment?,
    selectedDayIso: String,
    onSelectStop: (Appointment) -> Unit,
    onOpenDetail: (Appointment) -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (BuildConfig.MAPS_API_KEY.isBlank()) {
        Box(modifier = modifier) {
            StylizedRouteMapCanvasPane(
                t = t,
                stops = stops,
                activeStop = activeStop,
                selectedDayIso = selectedDayIso,
                onSelectStop = onSelectStop,
                onOpenDetail = onOpenDetail,
                onShowToast = onShowToast,
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = t.amberC,
                border = BorderStroke(1.dp, t.amber),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 62.dp)
            ) {
                Text(
                    text = "Gerçek harita için MAPS_API_KEY ekleyin",
                    color = t.amber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
        return
    }

    val fallbackPositions = remember(stops) { fallbackMapPositions(stops) }
    var mapPositions by remember(stops) { mutableStateOf(fallbackPositions) }
    var isGeocoding by remember(stops) { mutableStateOf(stops.isNotEmpty()) }
    val initialPosition = activeStop?.let { fallbackPositions[it.id] }
        ?: fallbackPositions.values.firstOrNull()
        ?: LatLng(41.0482, 28.9003)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 12.5f)
    }

    LaunchedEffect(stops) {
        mapPositions = if (stops.isEmpty()) emptyMap() else geocodeMapPositions(context, stops)
        isGeocoding = false
    }

    LaunchedEffect(activeStop?.id, mapPositions) {
        val coordinate = activeStop?.let { mapPositions[it.id] } ?: return@LaunchedEffect
        runCatching {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(coordinate, 14f), 500)
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = t.surface,
        border = BorderStroke(1.dp, t.border),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isTrafficEnabled = true),
                uiSettings = MapUiSettings(
                    compassEnabled = true,
                    mapToolbarEnabled = false,
                    zoomControlsEnabled = true
                )
            ) {
                val routePoints = stops.mapNotNull { mapPositions[it.id] }
                if (routePoints.size > 1) {
                    Polyline(
                        points = routePoints,
                        color = t.primary,
                        width = 8f,
                        geodesic = true
                    )
                }

                stops.forEachIndexed { index, stop ->
                    val coordinate = mapPositions[stop.id] ?: return@forEachIndexed
                    val isDone = stop.status == AppointmentStatus.TAMAMLANDI
                    Marker(
                        state = remember(stop.id, coordinate) { MarkerState(position = coordinate) },
                        title = "${index + 1}. ${stop.customerName}",
                        snippet = "${stop.timeSlot} · ${stop.district}",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            if (isDone) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_AZURE
                        ),
                        zIndex = if (activeStop?.id == stop.id) 2f else 1f,
                        onClick = {
                            onSelectStop(stop)
                            false
                        },
                        onInfoWindowClick = { onOpenDetail(stop) }
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = t.surface.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, t.border),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = "📍 Saha Rotası · ${if (selectedDayIso == "all") "Tüm Günler" else formatDateShort(selectedDayIso)}" +
                        if (isGeocoding) " · Adresler bulunuyor…" else "",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = t.text,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            if (activeStop != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = t.surface.copy(alpha = 0.97f),
                    border = BorderStroke(1.dp, t.border),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CustomerAvatarCircle(name = activeStop.customerName, size = 42.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${activeStop.customerName} · ${activeStop.timeSlot}",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black,
                                color = t.text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${activeStop.district} · ${activeStop.fullAddressDisplay}",
                                fontSize = 11.5.sp,
                                color = t.text2,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            onClick = {
                                onShowToast("🧭 Yol tarifi: ${activeStop.district}")
                                launchNavigation(context, activeStop.fullAddressDisplay, activeStop.district)
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = t.primary
                        ) {
                            Text(
                                text = "🧭 Rota",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                        Surface(
                            onClick = { onOpenDetail(activeStop) },
                            shape = RoundedCornerShape(10.dp),
                            color = t.surface2,
                            border = BorderStroke(1.dp, t.border)
                        ) {
                            Text(
                                text = "Detay",
                                color = t.text,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StylizedRouteMapCanvasPane(
    t: AppointmentTokens,
    stops: List<Appointment>,
    activeStop: Appointment?,
    selectedDayIso: String,
    onSelectStop: (Appointment) -> Unit,
    onOpenDetail: (Appointment) -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val pinCoordinates = remember(stops) {
        stops.mapIndexed { idx, appt ->
            val base = DistrictMapOffsets[appt.district] ?: (0.50f to 0.50f)
            val jitterX = ((idx % 3) - 1) * 0.055f
            val jitterY = (((idx / 2) % 3) - 1) * 0.045f
            val x = (base.first + jitterX).coerceIn(0.12f, 0.88f)
            val y = (base.second + jitterY).coerceIn(0.14f, 0.82f)
            x to y
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (t.isDark) Color(0xFF0A1626) else Color(0xFFDCEEFB),
        border = BorderStroke(1.dp, t.border),
        modifier = modifier
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val boxW = maxWidth
            val boxH = maxHeight

            // Stylized Roads & Route Polyline Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val roadColor = if (t.isDark) Color.White.copy(alpha = 0.06f) else Color(0xFF0F172A).copy(alpha = 0.07f)
                // Background grid
                val stepPx = 38.dp.toPx()
                var gx = 0f
                while (gx < size.width) {
                    drawLine(roadColor, Offset(gx, 0f), Offset(gx, size.height), strokeWidth = 1f)
                    gx += stepPx
                }
                var gy = 0f
                while (gy < size.height) {
                    drawLine(roadColor, Offset(0f, gy), Offset(size.width, gy), strokeWidth = 1f)
                    gy += stepPx
                }

                // Highway curves
                val highwayPath = Path().apply {
                    moveTo(0f, size.height * 0.72f)
                    quadraticTo(size.width * 0.42f, size.height * 0.52f, size.width, size.height * 0.25f)
                }
                drawPath(
                    path = highwayPath,
                    color = if (t.isDark) Color(0xFF1E3A5F) else Color(0xFFBAE6FD),
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )

                // Route dashed polyline connecting stops
                if (pinCoordinates.size > 1) {
                    val routePath = Path().apply {
                        pinCoordinates.forEachIndexed { idx, (rx, ry) ->
                            val px = rx * size.width
                            val py = ry * size.height
                            if (idx == 0) moveTo(px, py) else lineTo(px, py)
                        }
                    }
                    drawPath(
                        path = routePath,
                        color = t.primary,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
                        )
                    )
                }
            }

            // District Watermarks
            DistrictMapOffsets.entries.take(7).forEach { (distName, pos) ->
                Text(
                    text = distName.uppercase(TrLocale),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    color = if (t.isDark) Color.White.copy(alpha = 0.22f) else Color(0xFF0F172A).copy(alpha = 0.28f),
                    modifier = Modifier.offset(
                        x = boxW * pos.first - 36.dp,
                        y = boxH * pos.second + 18.dp
                    )
                )
            }

            // Interactive Numbered Pins
            stops.forEachIndexed { idx, stop ->
                val (rx, ry) = pinCoordinates[idx]
                val isSel = activeStop?.id == stop.id
                val isDone = stop.status == AppointmentStatus.TAMAMLANDI
                val pinSize = if (isSel) 42.dp else 32.dp
                val pinColor = if (isDone) t.green else t.primary

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset(
                            x = boxW * rx - (pinSize / 2),
                            y = boxH * ry - (pinSize / 2)
                        )
                        .clickable { onSelectStop(stop) }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = pinColor,
                        border = BorderStroke(3.dp, Color.White),
                        shadowElevation = if (isSel) 8.dp else 4.dp,
                        modifier = Modifier.size(pinSize)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = (idx + 1).toString(),
                                fontSize = if (isSel) 15.sp else 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    if (isSel) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = t.surface,
                            border = BorderStroke(1.dp, t.primary),
                            shadowElevation = 6.dp
                        ) {
                            Text(
                                text = "${stop.customerName} · ${stop.timeSlot.take(5)}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = t.text,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Top Overlay Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = t.surface.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, t.border)
                ) {
                    Text(
                        text = "📍 Saha Rotası · ${if (selectedDayIso == "all") "Tüm Günler" else formatDateShort(selectedDayIso)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = t.text,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = t.surface.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, t.border)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(t.primary))
                            Text("Bekleyen Durak", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = t.text)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(t.green))
                            Text("Tamamlandı", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = t.text)
                        }
                    }
                }
            }

            // Bottom Selected Stop Action Card
            if (activeStop != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = t.surface.copy(alpha = 0.96f),
                    border = BorderStroke(1.dp, t.border),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CustomerAvatarCircle(name = activeStop.customerName, size = 44.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${activeStop.customerName} · ${activeStop.timeSlot}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = t.text
                            )
                            Text(
                                text = "${activeStop.district} · ${activeStop.fullAddressDisplay}",
                                fontSize = 12.sp,
                                color = t.text2,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            onClick = {
                                onShowToast("📞 ${activeStop.customerName} aranıyor")
                                launchDialer(context, activeStop.phone)
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = t.primaryC,
                            border = BorderStroke(1.dp, t.primary),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                                Text("📞 Ara", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = t.primary)
                            }
                        }
                        Surface(
                            onClick = {
                                onShowToast("🧭 Yol tarifi: ${activeStop.district}")
                                launchNavigation(context, activeStop.fullAddressDisplay, activeStop.district)
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = t.primary,
                            modifier = Modifier.height(40.dp)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                                Text("🧭 Yol Tarifi", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                        Surface(
                            onClick = { onOpenDetail(activeStop) },
                            shape = RoundedCornerShape(10.dp),
                            color = t.surface2,
                            border = BorderStroke(1.dp, t.border),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                                Text("Detay", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = t.text)
                            }
                        }
                    }
                }
            }
        }
    }
}
