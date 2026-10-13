package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus
import com.example.data.model.BankAccount
import com.example.data.model.Customer
import com.example.data.model.JobReport
import com.example.data.model.StockItem
import com.example.utils.parseLocalizedDouble
import kotlinx.coroutines.delay
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    appointments: List<Appointment>,
    customers: List<Customer> = emptyList(),
    stockItems: List<StockItem> = emptyList(),
    bankAccounts: List<BankAccount> = emptyList(),
    onBackClick: () -> Unit,
    onAddAppointment: (Appointment, (Result<Unit>) -> Unit) -> Unit,
    onUpdateAppointment: (Appointment, (Result<Unit>) -> Unit) -> Unit,
    onUpdateStatus: (String, AppointmentStatus) -> Unit,
    onCompleteJob: (String, JobReport, (Result<Unit>) -> Unit) -> Unit,
    onCreateServiceReceipt: ((Appointment, JobReport, (Result<Unit>) -> Unit) -> Unit)? = null,
    onDeleteAppointment: (String) -> Unit,
    onSendBankTransfer: ((String, String, Double?, String?, (Result<String>) -> Unit) -> Unit)? = null,
    onGetAvailableSlots: ((String, (Result<List<String>>) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val t = rememberAppointmentTokens()
    val systemTodayIso = remember { todayIsoString() }

    // Reference today: if appointments exist and none match systemTodayIso's month, we still know systemTodayIso
    val todayIso = systemTodayIso

    // Filter & UI state
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<AppointmentStatus?>(null) }
    var dateRange by remember { mutableStateOf("month") } // "today", "tomorrow", "week", "month", "all"
    var expandedId by remember { mutableStateOf<String?>(appointments.firstOrNull()?.id) }
    var selectedAppointmentId by remember { mutableStateOf<String?>(appointments.firstOrNull()?.id) }

    // Tablet View state: "list", "calendar", "map"
    var tabletView by remember { mutableStateOf("list") }
    var detailSheetAppointment by remember { mutableStateOf<Appointment?>(null) }

    // Modals state
    var showQuickAppointmentModal by remember { mutableStateOf(false) }
    var editingAppointment by remember { mutableStateOf<Appointment?>(null) }
    var showServiceReceiptModal by remember { mutableStateOf(false) }
    var receiptTargetAppointment by remember { mutableStateOf<Appointment?>(null) }
    var ibanTargetAppointment by remember { mutableStateOf<Appointment?>(null) }
    var deleteConfirmAppointment by remember { mutableStateOf<Appointment?>(null) }

    // Toast banner state
    var toastMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2400L)
            toastMessage = null
        }
    }
    val showToast: (String) -> Unit = { msg -> toastMessage = msg }

    // Count appointments per customer name to determine "Eski müşteri" vs "Yeni"
    val customerAppointmentCounts = remember(appointments, customers) {
        val map = mutableMapOf<String, Int>()
        appointments.forEach { a ->
            val key = a.customerName.trim().lowercase(TrLocale)
            map[key] = (map[key] ?: 0) + 1
        }
        customers.forEach { c ->
            val key = c.name.trim().lowercase(TrLocale)
            if ((map[key] ?: 0) < 2 && c.appointmentCount > 1) {
                map[key] = c.appointmentCount
            }
        }
        map
    }

    // "Bu Ay" always means the current Istanbul calendar month.
    val activeMonthPrefix = remember(todayIso) { todayIso.take(7) }

    // Filtered appointments
    val filteredAppointments = remember(
        appointments,
        searchQuery,
        statusFilter,
        dateRange,
        todayIso,
        activeMonthPrefix
    ) {
        val q = searchQuery.trim().lowercase(TrLocale)
        val tomorrowIso = addDaysIso(todayIso, 1)
        appointments.filter { a ->
            if (statusFilter != null && a.status != statusFilter) return@filter false
            val normDate = normalizeToIsoDate(a.date)
            when (dateRange) {
                "today" -> if (normDate != todayIso) return@filter false
                "tomorrow" -> if (normDate != tomorrowIso) return@filter false
                "week" -> if (abs(daysBetweenIso(normDate, todayIso)) > 6) return@filter false
                "month" -> if (!normDate.startsWith(activeMonthPrefix)) return@filter false
            }
            if (q.isNotEmpty()) {
                val hay = "${a.customerName} ${a.phone} ${a.fullAddressDisplay} ${a.district} ${a.noteDisplay} ${a.serviceType}"
                    .lowercase(TrLocale)
                if (!hay.contains(q)) return@filter false
            }
            true
        }.sortedWith(
            compareByDescending<Appointment> { normalizeToIsoDate(it.date) }
                .thenBy { it.timeSlot }
        )
    }

    // Grouped by ISO date (newest date first)
    val groupedAppointments = remember(filteredAppointments) {
        filteredAppointments
            .groupBy { normalizeToIsoDate(it.date) }
            .entries
            .sortedByDescending { it.key }
    }

    val selectedAppointment = remember(appointments, filteredAppointments, selectedAppointmentId) {
        appointments.find { it.id == selectedAppointmentId }
            ?: filteredAppointments.firstOrNull()
            ?: appointments.firstOrNull()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(t.bg)
    ) {
        val isTablet = maxWidth >= 600.dp
        val isTabletLandscape = maxWidth >= 840.dp

        if (!isTablet) {
            // ================================================================
            // PHONE LAYOUT (< 600.dp)
            // ================================================================
            PhoneAppointmentsListContent(
                t = t,
                todayIso = todayIso,
                allAppointments = appointments,
                groupedAppointments = groupedAppointments,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                statusFilter = statusFilter,
                onStatusFilterChange = { statusFilter = it },
                dateRange = dateRange,
                onDateRangeChange = { dateRange = it },
                expandedId = expandedId,
                onToggleExpand = { id -> expandedId = if (expandedId == id) null else id },
                customerAppointmentCounts = customerAppointmentCounts,
                hasBankAccounts = bankAccounts.isNotEmpty(),
                onBackClick = onBackClick,
                onOpenNewAppointment = {
                    editingAppointment = null
                    showQuickAppointmentModal = true
                },
                onOpenServiceReceipt = {
                    receiptTargetAppointment = null
                    showServiceReceiptModal = true
                },
                onEditAppointment = { appt ->
                    editingAppointment = appt
                    showQuickAppointmentModal = true
                },
                onCancelOrReopenAppointment = { appt ->
                    val nextStatus = if (appt.status == AppointmentStatus.IPTAL) {
                        AppointmentStatus.ONAYLANDI
                    } else {
                        AppointmentStatus.IPTAL
                    }
                    onUpdateStatus(appt.id, nextStatus)
                    showToast(
                        if (nextStatus == AppointmentStatus.IPTAL) "❌ Randevu iptal edildi"
                        else "🔄 Randevu tekrar açıldı"
                    )
                },
                onCompleteOrReceipt = { appt ->
                    receiptTargetAppointment = appt
                    showServiceReceiptModal = true
                },
                onOpenIbanDialog = { appt ->
                    ibanTargetAppointment = appt
                },
                onDeleteAppointment = { appt ->
                    deleteConfirmAppointment = appt
                },
                onShowToast = showToast
            )
        } else {
            // ================================================================
            // TABLET LAYOUT (>= 600.dp: LANDSCAPE & PORTRAIT)
            // ================================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TabletHeaderAndStats(
                    t = t,
                    appointments = appointments,
                    tabletView = tabletView,
                    onTabletViewChange = { tabletView = it },
                    statusFilter = statusFilter,
                    onStatusFilterChange = { statusFilter = it },
                    onBackClick = onBackClick,
                    onOpenNewAppointment = {
                        editingAppointment = null
                        showQuickAppointmentModal = true
                    },
                    onOpenServiceReceipt = {
                        receiptTargetAppointment = null
                        showServiceReceiptModal = true
                    }
                )

                Box(modifier = Modifier.weight(1f)) {
                    when (tabletView) {
                        "calendar" -> {
                            WeekCalendarView(
                                t = t,
                                todayIso = todayIso,
                                appointments = appointments,
                                isPortrait = !isTabletLandscape,
                                onSelectAppointment = { appt ->
                                    selectedAppointmentId = appt.id
                                    detailSheetAppointment = appt
                                }
                            )
                        }
                        "map" -> {
                            RouteMapView(
                                t = t,
                                todayIso = todayIso,
                                appointments = appointments,
                                selectedAppointment = selectedAppointment,
                                onSelectAppointment = { appt ->
                                    selectedAppointmentId = appt.id
                                },
                                onOpenDetailSheet = { appt ->
                                    selectedAppointmentId = appt.id
                                    detailSheetAppointment = appt
                                },
                                onShowToast = showToast,
                                isPortrait = !isTabletLandscape
                            )
                        }
                        else -> {
                            if (isTabletLandscape) {
                                TabletLandscapeSplitView(
                                    t = t,
                                    allAppointments = appointments,
                                    filteredAppointments = filteredAppointments,
                                    selectedAppointment = selectedAppointment,
                                    onSelectAppointment = { appt -> selectedAppointmentId = appt.id },
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { searchQuery = it },
                                    statusFilter = statusFilter,
                                    onStatusFilterChange = { statusFilter = it },
                                    customerAppointmentCounts = customerAppointmentCounts,
                                    onEditAppointment = { appt ->
                                        editingAppointment = appt
                                        showQuickAppointmentModal = true
                                    },
                                    onCancelAppointment = { appt ->
                                        val next = if (appt.status == AppointmentStatus.IPTAL) {
                                            AppointmentStatus.ONAYLANDI
                                        } else AppointmentStatus.IPTAL
                                        onUpdateStatus(appt.id, next)
                                        showToast("❌ Randevu durumu güncellendi")
                                    },
                                    onCompleteOrReceipt = { appt ->
                                        receiptTargetAppointment = appt
                                        showServiceReceiptModal = true
                                    },
                                    onDeleteAppointment = { appt ->
                                        deleteConfirmAppointment = appt
                                    },
                                    onShowToast = showToast
                                )
                            } else {
                                // Tablet Portrait 2-Column Grid + BottomSheet Detail
                                TabletPortraitGridContent(
                                    t = t,
                                    allAppointments = appointments,
                                    filteredAppointments = filteredAppointments,
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { searchQuery = it },
                                    statusFilter = statusFilter,
                                    onStatusFilterChange = { statusFilter = it },
                                    dateRange = dateRange,
                                    onDateRangeChange = { dateRange = it },
                                    customerAppointmentCounts = customerAppointmentCounts,
                                    hasBankAccounts = bankAccounts.isNotEmpty(),
                                    onSelectCard = { appt ->
                                        selectedAppointmentId = appt.id
                                        detailSheetAppointment = appt
                                    },
                                    onCompleteOrReceipt = { appt ->
                                        receiptTargetAppointment = appt
                                        showServiceReceiptModal = true
                                    },
                                    onShowToast = showToast
                                )
                            }
                        }
                    }
                }
            }
        }

        // ====================================================================
        // MODALS (PHONE FULL-SCREEN SLIDE-UP vs TABLET 760dp DIALOG)
        // ====================================================================

        if (!isTablet) {
            // Phone Full-Screen Slide-Up: Quick Appointment
            AnimatedVisibility(
                visible = showQuickAppointmentModal,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                QuickAppointmentContent(
                    t = t,
                    todayIso = todayIso,
                    appointments = appointments,
                    customers = customers,
                    editingAppointment = editingAppointment,
                    onGetAvailableSlots = onGetAvailableSlots,
                    onShowToast = showToast,
                    onDismiss = {
                        showQuickAppointmentModal = false
                        editingAppointment = null
                    },
                    onSave = { savedAppt, isEdit, onFinished ->
                        val handleResult: (Result<Unit>) -> Unit = { result ->
                            onFinished(result)
                            if (result.isSuccess) {
                                showToast(if (isEdit) "✅ Randevu güncellendi" else "✅ Randevu kaydedildi, ekibe iletildi")
                                expandedId = savedAppt.id
                                selectedAppointmentId = savedAppt.id
                                showQuickAppointmentModal = false
                                editingAppointment = null
                            } else {
                                showToast("⚠️ ${result.exceptionOrNull()?.message ?: if (isEdit) "Randevu güncellenemedi" else "Randevu kaydedilemedi"}")
                            }
                        }
                        if (isEdit) {
                            onUpdateAppointment(savedAppt, handleResult)
                        } else {
                            onAddAppointment(savedAppt, handleResult)
                        }
                    }
                )
            }

            // Phone Full-Screen Slide-Up: 4-Step Service Receipt
            AnimatedVisibility(
                visible = showServiceReceiptModal,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                ServiceReceiptFlowContent(
                    t = t,
                    todayIso = todayIso,
                    targetAppointment = receiptTargetAppointment,
                    customers = customers,
                    stockItems = stockItems,
                    bankAccounts = bankAccounts,
                    onShowToast = showToast,
                    onDismiss = {
                        showServiceReceiptModal = false
                        receiptTargetAppointment = null
                    },
                    onOpenIbanDialog = { appt ->
                        ibanTargetAppointment = appt
                    },
                    onSubmitReceipt = { apptObj, jobReport, sendWa, onFinished ->
                        val target = receiptTargetAppointment
                        val formattedTotal = formatTL(parseLocalizedDouble(jobReport.collectedAmount) ?: 0.0)
                        val whatsappSuffix = if (sendWa) " · WhatsApp gönderimi istendi" else ""
                        val handleResult: (Result<Unit>) -> Unit = { result ->
                            onFinished(result)
                            if (result.isSuccess) {
                                showToast("🧾 Servis fişi kesildi ($formattedTotal ₺)$whatsappSuffix")
                                showServiceReceiptModal = false
                                receiptTargetAppointment = null
                            } else {
                                showToast("⚠️ ${result.exceptionOrNull()?.message ?: "Servis fişi oluşturulamadı"}")
                            }
                        }
                        if (target != null) {
                            onCompleteJob(target.id, jobReport, handleResult)
                        } else if (onCreateServiceReceipt != null) {
                            onCreateServiceReceipt(apptObj, jobReport, handleResult)
                        } else {
                            onAddAppointment(apptObj, handleResult)
                        }
                    }
                )
            }
        } else {
            // Tablet Centered Dialog: Quick Appointment
            if (showQuickAppointmentModal) {
                Dialog(
                    onDismissRequest = {
                        showQuickAppointmentModal = false
                        editingAppointment = null
                    },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = t.bg,
                        border = BorderStroke(1.dp, t.border),
                        shadowElevation = 24.dp,
                        modifier = Modifier
                            .widthIn(max = 760.dp)
                            .fillMaxWidth(0.92f)
                            .fillMaxHeight(0.92f)
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        QuickAppointmentContent(
                            t = t,
                            todayIso = todayIso,
                            appointments = appointments,
                            customers = customers,
                            editingAppointment = editingAppointment,
                            onGetAvailableSlots = onGetAvailableSlots,
                            onShowToast = showToast,
                            onDismiss = {
                                showQuickAppointmentModal = false
                                editingAppointment = null
                            },
                            onSave = { savedAppt, isEdit, onFinished ->
                                val handleResult: (Result<Unit>) -> Unit = { result ->
                                    onFinished(result)
                                    if (result.isSuccess) {
                                        showToast(if (isEdit) "✅ Randevu güncellendi" else "✅ Randevu kaydedildi, ekibe iletildi")
                                        selectedAppointmentId = savedAppt.id
                                        showQuickAppointmentModal = false
                                        editingAppointment = null
                                    } else {
                                        showToast("⚠️ ${result.exceptionOrNull()?.message ?: if (isEdit) "Randevu güncellenemedi" else "Randevu kaydedilemedi"}")
                                    }
                                }
                                if (isEdit) {
                                    onUpdateAppointment(savedAppt, handleResult)
                                } else {
                                    onAddAppointment(savedAppt, handleResult)
                                }
                            }
                        )
                    }
                }
            }

            // Tablet Centered Dialog: 4-Step Service Receipt
            if (showServiceReceiptModal) {
                Dialog(
                    onDismissRequest = {
                        showServiceReceiptModal = false
                        receiptTargetAppointment = null
                    },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = t.bg,
                        border = BorderStroke(1.dp, t.border),
                        shadowElevation = 24.dp,
                        modifier = Modifier
                            .widthIn(max = 760.dp)
                            .fillMaxWidth(0.92f)
                            .fillMaxHeight(0.92f)
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        ServiceReceiptFlowContent(
                            t = t,
                            todayIso = todayIso,
                            targetAppointment = receiptTargetAppointment,
                            customers = customers,
                            stockItems = stockItems,
                            bankAccounts = bankAccounts,
                            onShowToast = showToast,
                            onDismiss = {
                                showServiceReceiptModal = false
                                receiptTargetAppointment = null
                            },
                            onOpenIbanDialog = { appt ->
                                ibanTargetAppointment = appt
                            },
                            onSubmitReceipt = { apptObj, jobReport, sendWa, onFinished ->
                                val target = receiptTargetAppointment
                                val formattedTotal = formatTL(parseLocalizedDouble(jobReport.collectedAmount) ?: 0.0)
                                val whatsappSuffix = if (sendWa) " · WhatsApp gönderimi istendi" else ""
                                val handleResult: (Result<Unit>) -> Unit = { result ->
                                    onFinished(result)
                                    if (result.isSuccess) {
                                        showToast("🧾 Servis fişi kesildi ($formattedTotal ₺)$whatsappSuffix")
                                        showServiceReceiptModal = false
                                        receiptTargetAppointment = null
                                    } else {
                                        showToast("⚠️ ${result.exceptionOrNull()?.message ?: "Servis fişi oluşturulamadı"}")
                                    }
                                }
                                if (target != null) {
                                    onCompleteJob(target.id, jobReport, handleResult)
                                } else if (onCreateServiceReceipt != null) {
                                    onCreateServiceReceipt(apptObj, jobReport, handleResult)
                                } else {
                                    onAddAppointment(apptObj, handleResult)
                                }
                            }
                        )
                    }
                }
            }

            // Tablet Detail BottomSheet (for Portrait List, Calendar, and Map)
            if (detailSheetAppointment != null) {
                val sheetAppt = detailSheetAppointment!!
                val isReturning = (customerAppointmentCounts[sheetAppt.customerName.lowercase(TrLocale)] ?: 0) > 1
                ModalBottomSheet(
                    onDismissRequest = { detailSheetAppointment = null },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = t.surface,
                    contentColor = t.text,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(540.dp)
                    ) {
                        AppointmentDetailPane(
                            t = t,
                            appointment = sheetAppt,
                            isReturningCustomer = isReturning,
                            onEdit = {
                                detailSheetAppointment = null
                                editingAppointment = sheetAppt
                                showQuickAppointmentModal = true
                            },
                            onCancel = {
                                onUpdateStatus(sheetAppt.id, AppointmentStatus.IPTAL)
                                showToast("❌ Randevu iptal edildi")
                                detailSheetAppointment = null
                            },
                            onCompleteOrReceipt = {
                                detailSheetAppointment = null
                                receiptTargetAppointment = sheetAppt
                                showServiceReceiptModal = true
                            },
                            onDelete = {
                                detailSheetAppointment = null
                                deleteConfirmAppointment = sheetAppt
                            },
                            onShowToast = showToast
                        )
                    }
                }
            }
        }

        // IBAN Transfer Dialog
        if (ibanTargetAppointment != null && bankAccounts.isNotEmpty()) {
            val target = ibanTargetAppointment!!
            BankTransferModalDialog(
                t = t,
                appointment = target,
                bankAccounts = bankAccounts,
                onDismiss = { ibanTargetAppointment = null },
                onSend = { accountKey, amount, promised ->
                    onSendBankTransfer?.invoke(target.id, accountKey, amount, promised) { res ->
                        if (res.isSuccess) {
                            showToast("📲 IBAN bilgisi müşteriye gönderildi")
                        } else {
                            showToast("⚠️ ${res.exceptionOrNull()?.message ?: "IBAN gönderilemedi"}")
                        }
                    }
                    ibanTargetAppointment = null
                }
            )
        }

        // Delete Confirmation Dialog
        if (deleteConfirmAppointment != null) {
            val toDel = deleteConfirmAppointment!!
            AlertDialog(
                onDismissRequest = { deleteConfirmAppointment = null },
                containerColor = t.surface,
                titleContentColor = t.text,
                textContentColor = t.text2,
                title = {
                    Text(
                        text = "🗑️ Randevuyu Sil",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = t.text
                    )
                },
                text = {
                    Text(
                        text = "${toDel.customerName} (${toDel.date} · ${toDel.timeSlot}) randevusunu silmek istediğinize emin misiniz?",
                        fontSize = 13.sp,
                        color = t.text2
                    )
                },
                confirmButton = {
                    Surface(
                        onClick = {
                            onDeleteAppointment(toDel.id)
                            showToast("🗑️ Randevu silindi: ${toDel.customerName}")
                            deleteConfirmAppointment = null
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = t.red
                    ) {
                        Text(
                            text = "Evet, Sil",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                },
                dismissButton = {
                    Surface(
                        onClick = { deleteConfirmAppointment = null },
                        shape = RoundedCornerShape(10.dp),
                        color = t.surface2,
                        border = BorderStroke(1.dp, t.border)
                    ) {
                        Text(
                            text = "Vazgeç",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = t.text2,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            )
        }

        // Top Floating Toast Notification Pill
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, t.primary),
                shadowElevation = 12.dp
            ) {
                Text(
                    text = toastMessage ?: "",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

// ============================================================================
// PHONE LIST SCREEN (SCREEN 1)
// ============================================================================

@Composable
private fun PhoneAppointmentsListContent(
    t: AppointmentTokens,
    todayIso: String,
    allAppointments: List<Appointment>,
    groupedAppointments: List<Map.Entry<String, List<Appointment>>>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    statusFilter: AppointmentStatus?,
    onStatusFilterChange: (AppointmentStatus?) -> Unit,
    dateRange: String,
    onDateRangeChange: (String) -> Unit,
    expandedId: String?,
    onToggleExpand: (String) -> Unit,
    customerAppointmentCounts: Map<String, Int>,
    hasBankAccounts: Boolean,
    onBackClick: () -> Unit,
    onOpenNewAppointment: () -> Unit,
    onOpenServiceReceipt: () -> Unit,
    onEditAppointment: (Appointment) -> Unit,
    onCancelOrReopenAppointment: (Appointment) -> Unit,
    onCompleteOrReceipt: (Appointment) -> Unit,
    onOpenIbanDialog: (Appointment) -> Unit,
    onDeleteAppointment: (Appointment) -> Unit,
    onShowToast: (String) -> Unit
) {
    val totalCount = allAppointments.size
    val confirmedCount = allAppointments.count { it.status == AppointmentStatus.ONAYLANDI }
    val completedCount = allAppointments.count { it.status == AppointmentStatus.TAMAMLANDI }
    val cancelledCount = allAppointments.count { it.status == AppointmentStatus.IPTAL }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Header Row
        item(key = "header_row") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        onClick = onBackClick,
                        shape = RoundedCornerShape(12.dp),
                        color = t.surface,
                        border = BorderStroke(1.dp, t.border),
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("back_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "←",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = t.text
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Randevular",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = t.text,
                            letterSpacing = (-0.4).sp
                        )
                        Text(
                            text = "Saha ekibi anlık senkron 🟢",
                            fontSize = 11.5.sp,
                            color = t.text2
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = t.primaryC
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(t.primary)
                        )
                        Text(
                            text = "$totalCount Randevu",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = t.primary
                        )
                    }
                }
            }
        }

        // 2. Two-Column Quick CTA Grid
        item(key = "quick_cta_grid") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Yeni Randevu (Blue Gradient)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF0EA5E9))
                        .clip(RoundedCornerShape(16.dp))
                        .background(t.primaryGradient)
                        .clickable { onOpenNewAppointment() }
                        .padding(12.dp)
                        .testTag("new_appointment_button")
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "📅", fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Yeni Randevu",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Telefonla gelen iş",
                            fontSize = 10.5.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // Servis Fişi Kes (Amber Gradient)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFFF59E0B))
                        .clip(RoundedCornerShape(16.dp))
                        .background(t.amberGradient)
                        .clickable { onOpenServiceReceipt() }
                        .padding(12.dp)
                        .testTag("service_receipt_button")
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🧾", fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Servis Fişi Kes",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Randevusuz · Acil / Yerinde",
                            fontSize = 10.5.sp,
                            color = Color.White.copy(alpha = 0.88f)
                        )
                    }
                }
            }
        }

        // 3. Four-Column Stat Bar
        item(key = "stat_bar") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val stats = listOf(
                    Triple("Toplam", totalCount, t.text to null),
                    Triple("Sahada", confirmedCount, t.primary to AppointmentStatus.ONAYLANDI),
                    Triple("Bitti", completedCount, t.green to AppointmentStatus.TAMAMLANDI),
                    Triple("İptal", cancelledCount, t.red to AppointmentStatus.IPTAL)
                )
                stats.forEach { (label, count, pair) ->
                    val (accent, targetStatus) = pair
                    val progressFraction = if (totalCount > 0) {
                        (count.toFloat() / totalCount.toFloat()).coerceIn(0.12f, 1f)
                    } else 0.12f

                    Surface(
                        onClick = {
                            onStatusFilterChange(
                                if (statusFilter == targetStatus) null else targetStatus
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = t.surface,
                        border = BorderStroke(
                            1.dp,
                            if (statusFilter == targetStatus && targetStatus != null) accent else t.border
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = count.toString(),
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black,
                                color = accent,
                                lineHeight = 21.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = label,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = t.text2
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(t.surface2)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progressFraction)
                                        .height(3.dp)
                                        .background(accent)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Search Bar
        item(key = "search_bar") {
            AppointmentsSearchBar(
                t = t,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange
            )
        }

        // 5. Status Filter Chips Row
        item(key = "status_chips") {
            StatusFilterChipsRow(
                t = t,
                allAppointments = allAppointments,
                selectedStatus = statusFilter,
                onSelectStatus = onStatusFilterChange
            )
        }

        // 6. Date Range Segmented Bar
        item(key = "date_range_bar") {
            DateRangeSegmentedBar(
                t = t,
                dateRange = dateRange,
                onDateRangeChange = onDateRangeChange
            )
        }

        // 7. Day Groups & Appointment Cards
        if (groupedAppointments.isEmpty()) {
            item(key = "empty_state") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = t.surface,
                    border = BorderStroke(1.dp, t.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "☕", fontSize = 30.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bu aralıkta randevu yok.\nFiltreyi değiştir ya da yeni randevu ekle.",
                            fontSize = 13.sp,
                            color = t.text2,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            groupedAppointments.forEach { (dateIsoKey, dayAppts) ->
                item(key = "day_header_$dateIsoKey") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formatDateHeader(dateIsoKey, todayIso),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = t.primary,
                            letterSpacing = 0.6.sp
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(t.border)
                        )
                        Text(
                            text = "${dayAppts.size} iş",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = t.text2
                        )
                    }
                }

                items(dayAppts, key = { it.id }) { appt ->
                    val isExpanded = expandedId == appt.id
                    val isReturning = (customerAppointmentCounts[appt.customerName.lowercase(TrLocale)] ?: 0) > 1
                    AppointmentCard(
                        t = t,
                        appointment = appt,
                        isExpanded = isExpanded,
                        isReturningCustomer = isReturning,
                        hasBankAccounts = hasBankAccounts,
                        onToggleExpand = { onToggleExpand(appt.id) },
                        onCompleteOrReceipt = { onCompleteOrReceipt(appt) },
                        onEdit = { onEditAppointment(appt) },
                        onCancelOrReopen = { onCancelOrReopenAppointment(appt) },
                        onOpenIban = { onOpenIbanDialog(appt) },
                        onDelete = { onDeleteAppointment(appt) },
                        onShowToast = onShowToast
                    )
                }
            }
        }
    }
}

// ============================================================================
// TABLET PORTRAIT 2-COLUMN GRID CONTENT
// ============================================================================

@Composable
private fun TabletPortraitGridContent(
    t: AppointmentTokens,
    allAppointments: List<Appointment>,
    filteredAppointments: List<Appointment>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    statusFilter: AppointmentStatus?,
    onStatusFilterChange: (AppointmentStatus?) -> Unit,
    dateRange: String,
    onDateRangeChange: (String) -> Unit,
    customerAppointmentCounts: Map<String, Int>,
    hasBankAccounts: Boolean,
    onSelectCard: (Appointment) -> Unit,
    onCompleteOrReceipt: (Appointment) -> Unit,
    onShowToast: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = t.surface,
            border = BorderStroke(1.dp, t.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        AppointmentsSearchBar(
                            t = t,
                            searchQuery = searchQuery,
                            onSearchQueryChange = onSearchQueryChange
                        )
                    }
                    Box(modifier = Modifier.width(300.dp)) {
                        DateRangeSegmentedBar(
                            t = t,
                            dateRange = dateRange,
                            onDateRangeChange = onDateRangeChange
                        )
                    }
                }
                StatusFilterChipsRow(
                    t = t,
                    allAppointments = allAppointments,
                    selectedStatus = statusFilter,
                    onSelectStatus = onStatusFilterChange
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 84.dp)
        ) {
            items(filteredAppointments, key = { it.id }) { appt ->
                val isReturning = (customerAppointmentCounts[appt.customerName.lowercase(TrLocale)] ?: 0) > 1
                AppointmentCard(
                    t = t,
                    appointment = appt,
                    isExpanded = false,
                    isReturningCustomer = isReturning,
                    hasBankAccounts = hasBankAccounts,
                    onToggleExpand = { onSelectCard(appt) },
                    onCompleteOrReceipt = { onCompleteOrReceipt(appt) },
                    onEdit = { onSelectCard(appt) },
                    onCancelOrReopen = { onSelectCard(appt) },
                    onOpenIban = { onSelectCard(appt) },
                    onDelete = { onSelectCard(appt) },
                    onShowToast = onShowToast
                )
            }
        }
    }
}

// ============================================================================
// SHARED FILTER BAR COMPONENTS & APPOINTMENT CARD
// ============================================================================

@Composable
fun AppointmentsSearchBar(
    t: AppointmentTokens,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = t.surface,
        border = BorderStroke(1.dp, t.border),
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "🔎", fontSize = 14.sp)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Müşteri, telefon, mahalle ara...",
                        fontSize = 13.sp,
                        color = t.text2
                    )
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    cursorBrush = SolidColor(t.primary),
                    textStyle = TextStyle(
                        color = t.text,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (searchQuery.isNotEmpty()) {
                Text(
                    text = "✕",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = t.text2,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onSearchQueryChange("") }
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun StatusFilterChipsRow(
    t: AppointmentTokens,
    allAppointments: List<Appointment>,
    selectedStatus: AppointmentStatus?,
    onSelectStatus: (AppointmentStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    val chips = remember(allAppointments, t) {
        listOf(
            Triple(null, "Tümü" to allAppointments.size, t.primary),
            Triple(
                AppointmentStatus.BEKLIYOR,
                "Bekliyor" to allAppointments.count { it.status == AppointmentStatus.BEKLIYOR },
                t.amber
            ),
            Triple(
                AppointmentStatus.ONAYLANDI,
                "Onaylandı" to allAppointments.count { it.status == AppointmentStatus.ONAYLANDI },
                t.primary
            ),
            Triple(
                AppointmentStatus.TAMAMLANDI,
                "Tamamlandı" to allAppointments.count { it.status == AppointmentStatus.TAMAMLANDI },
                t.green
            ),
            Triple(
                AppointmentStatus.IPTAL,
                "İptal" to allAppointments.count { it.status == AppointmentStatus.IPTAL },
                t.red
            )
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        chips.forEach { (statusVal, labelAndCount, dotColor) ->
            val (label, count) = labelAndCount
            val isSelected = selectedStatus == statusVal
            Surface(
                onClick = { onSelectStatus(statusVal) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) t.primary else t.surface,
                border = BorderStroke(1.dp, if (isSelected) t.primary else t.border)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else dotColor)
                    )
                    Text(
                        text = label,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else t.text2
                    )
                    Text(
                        text = count.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = (if (isSelected) Color.White else t.text2).copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

@Composable
fun DateRangeSegmentedBar(
    t: AppointmentTokens,
    dateRange: String,
    onDateRangeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ranges = listOf(
        "today" to "Bugün",
        "tomorrow" to "Yarın",
        "week" to "Hafta",
        "month" to "Bu Ay",
        "all" to "Tümü"
    )
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = t.surface2,
        border = BorderStroke(1.dp, t.border),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ranges.forEach { (key, label) ->
                val isSelected = dateRange == key
                Surface(
                    onClick = { onDateRangeChange(key) },
                    shape = RoundedCornerShape(9.dp),
                    color = if (isSelected) t.surface else Color.Transparent,
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) t.text else t.text2,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(
    t: AppointmentTokens,
    appointment: Appointment,
    isExpanded: Boolean,
    isReturningCustomer: Boolean,
    hasBankAccounts: Boolean,
    onToggleExpand: () -> Unit,
    onCompleteOrReceipt: () -> Unit,
    onEdit: () -> Unit,
    onCancelOrReopen: () -> Unit,
    onOpenIban: () -> Unit,
    onDelete: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val st = statusMeta(appointment.status, t)
    val svcCol = serviceColor(appointment.serviceType, t)
    val (startT, endT) = parseSlotTimes(appointment.timeSlot)
    val isOpen = appointment.status == AppointmentStatus.ONAYLANDI ||
        appointment.status == AppointmentStatus.BEKLIYOR

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = t.surface,
        border = BorderStroke(1.dp, if (isExpanded) t.primary else t.border),
        shadowElevation = if (isExpanded) 8.dp else 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row (Clickable to toggle expand)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left Time Box (54.dp)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = t.surface2,
                    border = BorderStroke(1.dp, t.border),
                    modifier = Modifier.width(54.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = startT,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = t.text,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = endT,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = t.text2
                        )
                    }
                }

                // Middle Customer + District + Tag
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.customerName,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = t.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📍 ${appointment.district}",
                            fontSize = 11.5.sp,
                            color = t.text2
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = t.surface2,
                            border = BorderStroke(1.dp, t.border)
                        ) {
                            Text(
                                text = if (isReturningCustomer) "Eski müşteri" else "Yeni",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = t.text2,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Right Status Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = st.bg
                ) {
                    Text(
                        text = st.label,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = st.color,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Service + Address + Fault Inner Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = t.surface2,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(svcCol)
                        )
                        Text(
                            text = appointment.serviceType,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = t.text
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = appointment.fullAddressDisplay,
                        fontSize = 12.sp,
                        color = t.text2,
                        lineHeight = 16.sp
                    )
                    if (appointment.noteDisplay.isNotBlank()) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "⚠️ ${appointment.noteDisplay}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = t.amber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4-Button Primary Action Row (1f : 1f : 1f : 1.3f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Ara
                Surface(
                    onClick = {
                        onShowToast("📞 ${appointment.customerName} aranıyor (${appointment.phone})")
                        launchDialer(context, appointment.phone)
                    },
                    shape = RoundedCornerShape(11.dp),
                    color = t.primaryC,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "📞 Ara",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = t.primary
                        )
                    }
                }

                // WA
                Surface(
                    onClick = {
                        onShowToast("💬 WhatsApp mesajı açılıyor: ${appointment.customerName}")
                        launchWhatsApp(context, appointment.phone)
                    },
                    shape = RoundedCornerShape(11.dp),
                    color = t.greenC,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "💬 WA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = t.green
                        )
                    }
                }

                // Rota
                Surface(
                    onClick = {
                        onShowToast("🧭 Yol tarifi: ${appointment.district}")
                        launchNavigation(context, appointment.fullAddressDisplay, appointment.district)
                    },
                    shape = RoundedCornerShape(11.dp),
                    color = t.surface2,
                    border = BorderStroke(1.dp, t.border),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "🧭 Rota",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = t.text
                        )
                    }
                }

                // Primary Action: Tamamla or Fiş
                if (isOpen) {
                    Surface(
                        onClick = onCompleteOrReceipt,
                        shape = RoundedCornerShape(11.dp),
                        color = t.green,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(40.dp)
                            .testTag("complete_job_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "✓ Tamamla",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Surface(
                        onClick = onCompleteOrReceipt,
                        shape = RoundedCornerShape(11.dp),
                        color = t.surface2,
                        border = BorderStroke(1.dp, t.border),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(40.dp)
                            .testTag("edit_completed_job_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "🧾 Fiş",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = t.text
                            )
                        }
                    }
                }
            }

            // Expanded Secondary Actions Row
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                    ) {
                        drawLine(
                            color = t.border,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            onClick = onEdit,
                            shape = RoundedCornerShape(10.dp),
                            color = t.surface2,
                            border = BorderStroke(1.dp, t.border),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "✏️ Düzenle",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = t.text
                                )
                            }
                        }

                        Surface(
                            onClick = onCancelOrReopen,
                            shape = RoundedCornerShape(10.dp),
                            color = t.surface2,
                            border = BorderStroke(1.dp, t.border),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (appointment.status == AppointmentStatus.IPTAL) "🔄 Aç" else "❌ İptal Et",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = t.red
                                )
                            }
                        }

                        if (hasBankAccounts) {
                            Surface(
                                onClick = onOpenIban,
                                shape = RoundedCornerShape(10.dp),
                                color = t.surface2,
                                border = BorderStroke(1.dp, t.border),
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(36.dp)
                                    .testTag("send_iban_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "🏦 IBAN",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = t.primary
                                    )
                                }
                            }
                        }

                        Surface(
                            onClick = onDelete,
                            shape = RoundedCornerShape(10.dp),
                            color = t.surface2,
                            border = BorderStroke(1.dp, t.border),
                            modifier = Modifier
                                .weight(0.8f)
                                .height(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "🗑️ Sil",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = t.text2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
