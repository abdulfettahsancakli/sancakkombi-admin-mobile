package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus
import com.example.data.model.BankAccount
import com.example.data.model.Customer
import com.example.data.model.StockItem
import com.example.data.model.JobReport
import com.example.ui.components.CompleteJobDialog
import com.example.ui.components.EditAppointmentDialog
import com.example.ui.components.AppointmentDialogMode
import com.example.ui.components.NewAppointmentDialog
import com.example.utils.parseLocalizedDouble
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    onCreateServiceReceipt: (Appointment, JobReport, (Result<Unit>) -> Unit) -> Unit,
    onDeleteAppointment: (String) -> Unit,
    onSendBankTransfer: (appointmentId: String, accountKey: String, amount: Double?, date: String?, onResult: (Result<String>) -> Unit) -> Unit = { _, _, _, _, _ -> },
    onGetAvailableSlots: (dateIso: String, onResult: (Result<List<String>>) -> Unit) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val defaultMonthRange = remember { currentMonthRangeTr() }

    var selectedStatusFilter by remember { mutableStateOf<AppointmentStatus?>(null) }
    var viewMode by remember { mutableStateOf("liste") } // liste, takvim, harita
    var dateTimeline by remember { mutableStateOf("month") }
    var startDate by remember { mutableStateOf(defaultMonthRange.first) }
    var endDate by remember { mutableStateOf(defaultMonthRange.second) }
    var searchQuery by remember { mutableStateOf("") }
    var districtFilter by remember { mutableStateOf("Tümü") }
    var serviceFilter by remember { mutableStateOf("Tümü") }
    var showDateRangeDialog by remember { mutableStateOf(false) }

    var showNewDialog by remember { mutableStateOf(false) }
    var newAppointmentInitialDate by remember { mutableStateOf<String?>(null) }
    var showServiceReceiptIntake by remember { mutableStateOf(false) }
    var serviceReceiptDraft by remember { mutableStateOf<Appointment?>(null) }
    var editingAppointment by remember { mutableStateOf<Appointment?>(null) }
    var completingAppointment by remember { mutableStateOf<Appointment?>(null) }
    var bankTransferAppointment by remember { mutableStateOf<Appointment?>(null) }

    var expandedFilter by remember { mutableStateOf(false) }

    val timelineAppointments = remember(appointments, startDate, endDate) {
        val startVal = parseDateToComparableInt(startDate)
        val endVal = parseDateToComparableInt(endDate)

        appointments.filter { appt ->
            val apptDateVal = parseDateToComparableInt(appt.date)
            val matchesDate = if (startVal != null && endVal != null && apptDateVal != null) {
                apptDateVal in startVal..endVal
            } else true
            matchesDate
        }
    }

    val districtOptions = remember(appointments) {
        listOf("Tümü") + appointments.map { it.district.trim() }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val serviceOptions = remember(appointments) {
        listOf("Tümü") + appointments.map { it.serviceType.trim() }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val filteredAppointments = remember(
        timelineAppointments,
        selectedStatusFilter,
        districtFilter,
        serviceFilter,
        searchQuery
    ) {
        val query = searchQuery.trim().lowercase(Locale.forLanguageTag("tr-TR"))
        val queryDigits = query.filter(Char::isDigit)
        timelineAppointments.filter { appt ->
            val matchesStatus = selectedStatusFilter == null || appt.status == selectedStatusFilter
            val matchesDistrict = districtFilter == "Tümü" || appt.district.equals(districtFilter, ignoreCase = true)
            val matchesService = serviceFilter == "Tümü" || appt.serviceType.equals(serviceFilter, ignoreCase = true)
            val searchable = listOf(
                appt.customerName,
                appt.phone,
                appt.district,
                appt.neighborhood,
                appt.addressDetail,
                appt.problemNote,
                appt.serviceType
            ).joinToString(" ").lowercase(Locale.forLanguageTag("tr-TR"))
            val matchesSearch = query.isBlank() || searchable.contains(query) ||
                (queryDigits.isNotBlank() && appt.phone.filter(Char::isDigit).contains(queryDigits))
            matchesStatus && matchesDistrict && matchesService && matchesSearch
        }
    }

    val countTotal = timelineAppointments.size
    val countBekliyor = timelineAppointments.count { it.status == AppointmentStatus.BEKLIYOR }
    val countOnaylandi = timelineAppointments.count { it.status == AppointmentStatus.ONAYLANDI }
    val countTamamlandi = timelineAppointments.count { it.status == AppointmentStatus.TAMAMLANDI }
    val countIptal = timelineAppointments.count { it.status == AppointmentStatus.IPTAL }
    val todayCount = timelineAppointments.count { it.date == currentDateTr() }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Geri")
                }

                Spacer(modifier = Modifier.weight(1f))
                Text("Randevu Yönetimi", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title & Subtitle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Randevular",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Seçili aralıkta $countTotal randevu • ${filteredAppointments.size} kayıt gösteriliyor",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (countBekliyor > 0) {
                    Surface(shape = RoundedCornerShape(50), color = Color(0xFFFFF7ED)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                            Spacer(Modifier.width(5.dp))
                            Text("$countBekliyor bekliyor", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Web yönetim ekranındaki iki ana işlem mobilde tek bir aksiyon kartında.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .16f))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showServiceReceiptIntake = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f).testTag("service_receipt_button")
                        ) {
                            Icon(Icons.Default.ReceiptLong, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text("Servis Fişi Oluştur", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("Randevusuz hızlı iş", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Button(
                            onClick = { newAppointmentInitialDate = null; showNewDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f).testTag("new_appointment_button")
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text("Yeni Randevu", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("Planlı servis oluştur", fontSize = 9.sp, color = Color.White.copy(alpha = .8f))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(3.dp)
                    ) {
                        ViewModeButton("liste", "Liste", Icons.Default.List, viewMode, Modifier.weight(1f)) { viewMode = "liste" }
                        ViewModeButton("takvim", "Takvim", Icons.Default.CalendarMonth, viewMode, Modifier.weight(1f)) { viewMode = "takvim" }
                        ViewModeButton("harita", "Harita", Icons.Default.Map, viewMode, Modifier.weight(1f)) { viewMode = "harita" }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Web ekranındaki zaman çizelgesi, arama ve dinamik filtreler.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .14f))
            ) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        val presets = listOf(
                            Triple("today", "Bugün", currentDateTr()),
                            Triple("tomorrow", "Yarın", currentDateTr(1)),
                            Triple("week", "Bu Hafta", currentWeekRangeTr().first),
                            Triple("month", "Bu Ay", defaultMonthRange.first),
                            Triple("all", "Tüm Zamanlar", "")
                        )
                        items(presets) { (key, label, _) ->
                            FilterChip(
                                selected = dateTimeline == key,
                                onClick = {
                                    dateTimeline = key
                                    when (key) {
                                        "today" -> { startDate = currentDateTr(); endDate = startDate }
                                        "tomorrow" -> { startDate = currentDateTr(1); endDate = startDate }
                                        "week" -> currentWeekRangeTr().also { startDate = it.first; endDate = it.second }
                                        "month" -> { startDate = defaultMonthRange.first; endDate = defaultMonthRange.second }
                                        else -> { startDate = ""; endDate = "" }
                                    }
                                },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = .14f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = dateTimeline == "custom",
                                onClick = { showDateRangeDialog = true },
                                label = { Text("Özel Aralık", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.CalendarMonth, null, Modifier.size(15.dp)) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Müşteri, telefon, mahalle veya arıza ara") },
                        leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(19.dp)) },
                        trailingIcon = if (searchQuery.isNotBlank()) {
                            { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, "Aramayı temizle", Modifier.size(18.dp)) } }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppointmentFilterDropdown(
                            label = "İlçe / Bölge",
                            value = districtFilter,
                            options = districtOptions,
                            onSelect = { districtFilter = it },
                            modifier = Modifier.weight(1f)
                        )
                        AppointmentFilterDropdown(
                            label = "Hizmet Türü",
                            value = serviceFilter,
                            options = serviceOptions,
                            onSelect = { serviceFilter = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (dateTimeline == "custom") {
                        Text("Özel aralık: $startDate – $endDate", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stat Summary Cards Row - Optimized for Mobile
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    StatChipCard(
                        label = "TOPLAM RANDEVU",
                        count = countTotal,
                        accentColor = Color(0xFF64748B),
                        progress = 1f,
                        supportingText = "Bugün $todayCount iş emri",
                        isSelected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null }
                    )
                }
                item {
                    StatChipCard(
                        label = "BEKLİYOR",
                        count = countBekliyor,
                        accentColor = Color(0xFFF59E0B),
                        progress = statusProgress(countBekliyor, countTotal),
                        supportingText = if (countBekliyor == 0) "Tümü cevaplandı" else "$countBekliyor talep",
                        isSelected = selectedStatusFilter == AppointmentStatus.BEKLIYOR,
                        onClick = { selectedStatusFilter = if (selectedStatusFilter == AppointmentStatus.BEKLIYOR) null else AppointmentStatus.BEKLIYOR }
                    )
                }
                item {
                    StatChipCard(
                        label = "ONAYLANDI",
                        count = countOnaylandi,
                        accentColor = Color(0xFF0288D1),
                        progress = statusProgress(countOnaylandi, countTotal),
                        supportingText = "%${statusPercent(countOnaylandi, countTotal)} aktif",
                        isSelected = selectedStatusFilter == AppointmentStatus.ONAYLANDI,
                        onClick = { selectedStatusFilter = if (selectedStatusFilter == AppointmentStatus.ONAYLANDI) null else AppointmentStatus.ONAYLANDI }
                    )
                }
                item {
                    StatChipCard(
                        label = "TAMAMLANDI",
                        count = countTamamlandi,
                        accentColor = Color(0xFF2E7D32),
                        progress = statusProgress(countTamamlandi, countTotal),
                        supportingText = "$countTamamlandi servis fişi",
                        isSelected = selectedStatusFilter == AppointmentStatus.TAMAMLANDI,
                        onClick = { selectedStatusFilter = if (selectedStatusFilter == AppointmentStatus.TAMAMLANDI) null else AppointmentStatus.TAMAMLANDI }
                    )
                }
                item {
                    StatChipCard(
                        label = "İPTAL",
                        count = countIptal,
                        accentColor = Color(0xFFD32F2F),
                        progress = statusProgress(countIptal, countTotal),
                        supportingText = "%${statusPercent(countIptal, countTotal)} iptal",
                        isSelected = selectedStatusFilter == AppointmentStatus.IPTAL,
                        onClick = { selectedStatusFilter = if (selectedStatusFilter == AppointmentStatus.IPTAL) null else AppointmentStatus.IPTAL }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Content Area (List vs Calendar)
            if (viewMode == "liste") {
                if (filteredAppointments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .5f), modifier = Modifier.size(34.dp))
                            Spacer(Modifier.height(6.dp))
                            Text("Filtrelerle eşleşen randevu bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    selectedStatusFilter = null
                                    districtFilter = "Tümü"
                                    serviceFilter = "Tümü"
                                    searchQuery = ""
                                }
                            ) { Text("Filtreleri Temizle", fontSize = 11.sp) }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredAppointments, key = { it.id }) { appt ->
                            AppointmentCard(
                                appointment = appt,
                                onCall = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${appt.phone.replace(" ", "")}"))
                                    context.startActivity(intent)
                                },
                                onWhatsapp = {
                                    val cleanPhone = appt.phone.replace(" ", "").replace("^0".toRegex(), "90")
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanPhone"))
                                    context.startActivity(intent)
                                },
                                onRoute = {
                                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(appt.addressDetail)}"))
                                    context.startActivity(mapIntent)
                                },
                                onConfirm = { onUpdateStatus(appt.id, AppointmentStatus.ONAYLANDI) },
                                onComplete = { completingAppointment = appt },
                                onSendIban = { bankTransferAppointment = appt },
                                onCancel = { onUpdateStatus(appt.id, AppointmentStatus.IPTAL) },
                                onEdit = {
                                    if (appt.status == AppointmentStatus.TAMAMLANDI) {
                                        completingAppointment = appt
                                    } else {
                                        editingAppointment = appt
                                    }
                                },
                                onDelete = { onDeleteAppointment(appt.id) }
                            )
                        }
                    }
                }
            } else if (viewMode == "takvim") {
                // Modernized Mobile Calendar View
                ModernMobileCalendarView(
                    appointments = filteredAppointments,
                    selectedStatusFilter = null,
                    onAppointmentClick = {
                        if (it.status == AppointmentStatus.TAMAMLANDI) {
                            completingAppointment = it
                        } else {
                            editingAppointment = it
                        }
                    },
                    onNewAppointmentForDate = {
                        newAppointmentInitialDate = it
                        showNewDialog = true
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                MobileRouteView(
                    appointments = filteredAppointments,
                    onAppointmentClick = {
                        if (it.status == AppointmentStatus.TAMAMLANDI) completingAppointment = it
                        else editingAppointment = it
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Dialog Triggers
    if (showDateRangeDialog) {
        DateRangeDialog(
            initialStartDate = startDate,
            initialEndDate = endDate,
            onDismiss = { showDateRangeDialog = false },
            onApply = { s, e ->
                startDate = s
                endDate = e
                dateTimeline = "custom"
            }
        )
    }
    if (showNewDialog) {
        NewAppointmentDialog(
            customers = customers,
            initialDate = newAppointmentInitialDate,
            onDismiss = { showNewDialog = false },
            onSave = { newAppt, callback ->
                onAddAppointment(newAppt) { result ->
                    callback(result)
                }
            },
            onGetAvailableSlots = onGetAvailableSlots
        )
    }

    if (showServiceReceiptIntake) {
        NewAppointmentDialog(
            customers = customers,
            mode = AppointmentDialogMode.SERVICE_RECEIPT,
            onDismiss = { showServiceReceiptIntake = false },
            onSave = { draft, callback ->
                serviceReceiptDraft = draft
                showServiceReceiptIntake = false
                callback(Result.success(Unit))
            }
        )
    }

    serviceReceiptDraft?.let { draft ->
        CompleteJobDialog(
            appointment = draft,
            createMode = true,
            bankAccounts = bankAccounts,
            stockItems = stockItems,
            onDismiss = { serviceReceiptDraft = null },
            onComplete = { report, callback ->
                onCreateServiceReceipt(draft, report, callback)
            }
        )
    }

    editingAppointment?.let { appt ->
        EditAppointmentDialog(
            appointment = appt,
            customers = customers,
            onDismiss = { editingAppointment = null },
            onSave = { updated, callback ->
                onUpdateAppointment(updated) { result ->
                    callback(result)
                }
            },
            onDelete = { id ->
                onDeleteAppointment(id)
                editingAppointment = null
            }
        )
    }

    completingAppointment?.let { appt ->
        CompleteJobDialog(
            appointment = appt,
            bankAccounts = bankAccounts,
            stockItems = stockItems,
            onDismiss = { completingAppointment = null },
            onComplete = { report, callback ->
                onCompleteJob(appt.id, report) { result ->
                    callback(result)
                }
            },
            onSendBankTransfer = { accountKey, amount, date, onResult ->
                onSendBankTransfer(appt.id, accountKey, amount, date, onResult)
            }
        )
    }

    bankTransferAppointment?.let { appt ->
        SendBankTransferDialog(
            appointment = appt,
            bankAccounts = bankAccounts,
            onDismiss = { bankTransferAppointment = null },
            onSend = { accountKey, amount, date, onResult ->
                onSendBankTransfer(appt.id, accountKey, amount, date, onResult)
            }
        )
    }
}

@Composable
private fun ViewModeButton(
    mode: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedMode: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val selected = mode == selectedMode
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, label, tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppointmentFilterDropdown(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 10.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
            shape = RoundedCornerShape(10.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 12.sp) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun MobileRouteView(
    appointments: List<Appointment>,
    onAppointmentClick: (Appointment) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val routeAppointments = remember(appointments) {
        appointments.filter { it.status != AppointmentStatus.IPTAL && it.addressDetail.isNotBlank() }
    }
    val routeDates = remember(routeAppointments) {
        routeAppointments.map { it.date }.distinct().sortedBy { parseDateToComparableInt(it) ?: Int.MAX_VALUE }
    }
    var selectedDate by remember(routeDates) {
        mutableStateOf(routeDates.firstOrNull { it == currentDateTr() } ?: routeDates.firstOrNull().orEmpty())
    }
    val stops = remember(routeAppointments, selectedDate) {
        routeAppointments.filter { it.date == selectedDate }.sortedBy { it.timeSlot }
    }

    fun openAllStops() {
        if (stops.isEmpty()) return
        val destination = Uri.encode(stops.last().addressDetail)
        val waypoints = stops.dropLast(1).take(8).joinToString("|") { it.addressDetail }
        val url = buildString {
            append("https://www.google.com/maps/dir/?api=1&travelmode=driving&destination=")
            append(destination)
            if (waypoints.isNotBlank()) append("&waypoints=${Uri.encode(waypoints)}")
        }
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .14f))
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)) {
                    Icon(Icons.Default.Map, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(9.dp).size(20.dp))
                }
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text("Günlük Servis Rotası", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Durakları sırayla açın veya tüm rotayı başlatın", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (routeDates.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Seçili filtrelerde adresi olan rota kaydı yok.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(routeDates) { date ->
                        val count = routeAppointments.count { it.date == date }
                        FilterChip(
                            selected = selectedDate == date,
                            onClick = { selectedDate = date },
                            label = { Text("${if (date == currentDateTr()) "Bugün • " else ""}$date ($count)", fontSize = 10.sp) }
                        )
                    }
                }

                Button(
                    onClick = { openAllStops() },
                    enabled = stops.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Navigation, null, Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("${stops.size} Duraklı Rotayı Başlat", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                LazyColumn(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(stops, key = { it.id }) { appointment ->
                        val stopNumber = stops.indexOf(appointment) + 1
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onAppointmentClick(appointment) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .34f))
                        ) {
                            Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                                    Text("$stopNumber", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp))
                                }
                                Spacer(Modifier.width(9.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("${appointment.timeSlot} • ${appointment.customerName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(appointment.addressDetail, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                                IconButton(
                                    onClick = {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(appointment.addressDetail)}")))
                                    }
                                ) {
                                    Icon(Icons.Default.Navigation, "Konuma git", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun istanbulCalendar(): Calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Istanbul"))

private fun formatTrDate(calendar: Calendar): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.forLanguageTag("tr-TR")).apply {
        timeZone = TimeZone.getTimeZone("Europe/Istanbul")
    }.format(calendar.time)

private fun currentDateTr(offsetDays: Int = 0): String =
    formatTrDate(istanbulCalendar().apply { add(Calendar.DAY_OF_YEAR, offsetDays) })

private fun currentMonthRangeTr(): Pair<String, String> {
    val start = istanbulCalendar().apply { set(Calendar.DAY_OF_MONTH, 1) }
    val end = (start.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH)) }
    return formatTrDate(start) to formatTrDate(end)
}

private fun currentWeekRangeTr(): Pair<String, String> {
    val start = istanbulCalendar().apply {
        firstDayOfWeek = Calendar.MONDAY
        val offset = (get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
        add(Calendar.DAY_OF_YEAR, -offset)
    }
    val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 6) }
    return formatTrDate(start) to formatTrDate(end)
}

private fun statusProgress(count: Int, total: Int): Float = if (total > 0) count.toFloat() / total else 0f

private fun statusPercent(count: Int, total: Int): Int = if (total > 0) ((count * 100f) / total).toInt() else 0

private fun parseDateToComparableInt(dateStr: String): Int? {
    val regex = Regex("""(\d{2})\.(\d{2})\.(\d{4})""")
    val match = regex.find(dateStr) ?: return null
    val (dd, mm, yyyy) = match.destructured
    return (yyyy + mm + dd).toIntOrNull()
}

@Composable
private fun StatChipCard(
    label: String,
    count: Int,
    accentColor: Color,
    progress: Float,
    supportingText: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(min = 148.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "$count",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Spacer(Modifier.height(7.dp))
            Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.outline.copy(alpha = .12f))) {
                Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().background(accentColor))
            }
            Spacer(Modifier.height(5.dp))
            Text(supportingText, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = accentColor)
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: Appointment,
    onCall: () -> Unit,
    onWhatsapp: () -> Unit,
    onRoute: () -> Unit,
    onConfirm: () -> Unit,
    onComplete: () -> Unit,
    onSendIban: () -> Unit,
    onCancel: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(true) }

    val statusColor = when (appointment.status) {
        AppointmentStatus.BEKLIYOR -> Color(0xFFF59E0B)
        AppointmentStatus.ONAYLANDI -> Color(0xFF0288D1)
        AppointmentStatus.TAMAMLANDI -> Color(0xFF2E7D32)
        AppointmentStatus.IPTAL -> Color(0xFFD32F2F)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Left Status Color Accent Bar
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                // Header Row (Click to toggle expand/collapse)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${appointment.date} | ${appointment.timeSlot}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Status Badge (Guaranteed single line, no character wrapping)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = appointment.status.label,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Daralt" else "Genişlet",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Customer Info Area (Header info always visible, click to toggle)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appointment.customerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = appointment.serviceType,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "📍 ${appointment.district}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Collapsible Content
                AnimatedVisibility(visible = isExpanded) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (appointment.addressDetail.isNotBlank()) {
                            Text(
                                text = appointment.addressDetail,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }

                        if (appointment.problemNote.isNotBlank()) {
                            Text(
                                text = "Not: ${appointment.problemNote}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Action Dock (Saha Dock'u: 48dp height minimum touch targets)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = onCall,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ara",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = onWhatsapp,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF25D366)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_whatsapp),
                                    contentDescription = "WhatsApp",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "WhatsApp",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF25D366),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = onRoute,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Rota",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Primary Management Action Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (appointment.status == AppointmentStatus.BEKLIYOR) {
                                Button(
                                    onClick = onConfirm,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("✓ Onayla", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (appointment.status != AppointmentStatus.TAMAMLANDI && appointment.status != AppointmentStatus.IPTAL) {
                                Button(
                                    onClick = onComplete,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .testTag("complete_job_button")
                                ) {
                                    Text("✓ İş Kapanış & İmza", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (appointment.status == AppointmentStatus.TAMAMLANDI) {
                                Button(
                                    onClick = onComplete,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .testTag("edit_completed_job_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Servis Fişini Düzenle", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (appointment.status != AppointmentStatus.TAMAMLANDI && appointment.status != AppointmentStatus.IPTAL) {
                                OutlinedButton(
                                    onClick = onSendIban,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .testTag("send_iban_button")
                                ) {
                                    Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("IBAN", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Toolbar Icons: Hatırlatıcı (30s Test), Düzenle, İptal, Sil
                            IconButton(
                                onClick = {
                                    com.example.utils.ReminderManager.scheduleTestReminder(context, 30)
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "30s Test Bildirimi", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            }

                            IconButton(
                                onClick = onEdit,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Düzenle", modifier = Modifier.size(18.dp))
                            }

                            if (appointment.status != AppointmentStatus.TAMAMLANDI && appointment.status != AppointmentStatus.IPTAL) {
                                IconButton(
                                    onClick = onCancel,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Cancel, contentDescription = "Randevuyu İptal Et", tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                                }
                            }

                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernMobileCalendarView(
    appointments: List<Appointment>,
    selectedStatusFilter: AppointmentStatus?,
    onAppointmentClick: (Appointment) -> Unit,
    onNewAppointmentForDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(currentDateTr()) }

    val weekDays = remember(selectedDate) {
        calculateWeekDaysForDate(selectedDate)
    }

    val monthYearText = remember(selectedDate) {
        getMonthYearText(selectedDate)
    }

    val filteredByStatus = remember(appointments, selectedStatusFilter) {
        if (selectedStatusFilter == null) appointments
        else appointments.filter { it.status == selectedStatusFilter }
    }

    val dayAppointments = remember(filteredByStatus, selectedDate) {
        filteredByStatus.filter { appt ->
            appt.date.contains(selectedDate) || appt.date.startsWith(selectedDate.take(5))
        }
    }

    fun openDatePicker() {
        val cal = java.util.Calendar.getInstance()
        val parts = selectedDate.split(".")
        if (parts.size == 3) {
            val now = Calendar.getInstance(TimeZone.getTimeZone("Europe/Istanbul"))
            val d = parts[0].toIntOrNull() ?: now.get(Calendar.DAY_OF_MONTH)
            val m = (parts[1].toIntOrNull() ?: (now.get(Calendar.MONTH) + 1)) - 1
            val y = parts[2].toIntOrNull() ?: now.get(Calendar.YEAR)
            cal.set(y, m, d)
        }

        android.app.DatePickerDialog(
            context,
            { _, year, monthOfYear, dayOfMonth ->
                val formattedDay = String.format("%02d", dayOfMonth)
                val formattedMonth = String.format("%02d", monthOfYear + 1)
                selectedDate = "$formattedDay.$formattedMonth.$year"
            },
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH),
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Month Header & Today Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { openDatePicker() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Takvimden Tarih Seç",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = monthYearText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Tarih Seç ▾",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.clickable { selectedDate = currentDateTr() }
                ) {
                    Text(
                        text = "Bugün",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day Selector Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach { day ->
                    val isSelected = day.fullDate == selectedDate
                    val hasAppts = appointments.any { it.date.contains(day.fullDate) || it.date.startsWith(day.fullDate.take(5)) }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                            .clickable { selectedDate = day.fullDate }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = day.dayName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = day.dayNum,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Badge Dot
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color.White
                                        else if (hasAppts) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Agenda Header for selected day
            val formattedDateHeader = remember(selectedDate) {
                formatFullDateHeader(selectedDate)
            }
            val headerText = "$formattedDateHeader — ${dayAppointments.size} Randevu"

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = headerText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = { onNewAppointmentForDate(selectedDate) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Ekle", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Daily Agenda List
            if (dayAppointments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bu tarihte randevu yok.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onNewAppointmentForDate(selectedDate) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Bu Güne Ekle", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dayAppointments, key = { it.id }) { appt ->
                        val statusColor = when (appt.status) {
                            AppointmentStatus.BEKLIYOR -> Color(0xFFF59E0B)
                            AppointmentStatus.ONAYLANDI -> Color(0xFF0288D1)
                            AppointmentStatus.TAMAMLANDI -> Color(0xFF2E7D32)
                            AppointmentStatus.IPTAL -> Color(0xFFD32F2F)
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAppointmentClick(appt) },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = statusColor.copy(alpha = 0.15f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = appt.timeSlot.take(5),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = appt.customerName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${appt.serviceType} • ${appt.district}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = statusColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = when (appt.status) {
                                            AppointmentStatus.BEKLIYOR -> "Bekliyor"
                                            AppointmentStatus.ONAYLANDI -> "Onaylandı"
                                            AppointmentStatus.TAMAMLANDI -> "Tamamlandı"
                                            AppointmentStatus.IPTAL -> "İptal"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
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

private data class CalendarDay(
    val dayName: String,
    val dayNum: String,
    val fullDate: String
)

private fun calculateWeekDaysForDate(selectedDateStr: String): List<CalendarDay> {
    val cal = istanbulCalendar()
    val fallbackDay = cal.get(Calendar.DAY_OF_MONTH)
    val fallbackMonth = cal.get(Calendar.MONTH) + 1
    val fallbackYear = cal.get(Calendar.YEAR)
    val parts = selectedDateStr.split(".")
    if (parts.size == 3) {
        val d = parts[0].toIntOrNull() ?: fallbackDay
        val m = (parts[1].toIntOrNull() ?: fallbackMonth) - 1
        val y = parts[2].toIntOrNull() ?: fallbackYear
        cal.set(y, m, d)
    }

    // Set to Monday of the week
    val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK) // Sunday = 1, Monday = 2
    val daysFromMonday = if (dayOfWeek == java.util.Calendar.SUNDAY) 6 else dayOfWeek - java.util.Calendar.MONDAY
    cal.add(java.util.Calendar.DAY_OF_MONTH, -daysFromMonday)

    val dayNames = arrayOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
    val result = mutableListOf<CalendarDay>()

    for (i in 0 until 7) {
        val dayNum = String.format("%02d", cal.get(java.util.Calendar.DAY_OF_MONTH))
        val monthNum = String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1)
        val yearNum = cal.get(java.util.Calendar.YEAR)
        val fullDate = "$dayNum.$monthNum.$yearNum"
        result.add(CalendarDay(dayName = dayNames[i], dayNum = dayNum, fullDate = fullDate))
        cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
    }
    return result
}

private fun getMonthYearText(selectedDateStr: String): String {
    val monthNames = arrayOf("Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık")
    val parts = selectedDateStr.split(".")
    if (parts.size == 3) {
        val now = istanbulCalendar()
        val m = (parts[1].toIntOrNull() ?: (now.get(Calendar.MONTH) + 1)) - 1
        val y = parts[2].toIntOrNull() ?: now.get(Calendar.YEAR)
        if (m in 0..11) {
            return "${monthNames[m]} $y"
        }
    }
    return getMonthYearText(currentDateTr())
}

private fun formatFullDateHeader(selectedDateStr: String): String {
    val monthNames = arrayOf("Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık")
    val dayNames = arrayOf("Pazar", "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi")

    val cal = istanbulCalendar()
    val fallbackDay = cal.get(Calendar.DAY_OF_MONTH)
    val fallbackMonth = cal.get(Calendar.MONTH) + 1
    val fallbackYear = cal.get(Calendar.YEAR)
    val parts = selectedDateStr.split(".")
    if (parts.size == 3) {
        val d = parts[0].toIntOrNull() ?: fallbackDay
        val m = (parts[1].toIntOrNull() ?: fallbackMonth) - 1
        val y = parts[2].toIntOrNull() ?: fallbackYear
        cal.set(y, m, d)
        val dayOfWeekIndex = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1
        val dayOfWeekStr = dayNames[dayOfWeekIndex]
        val monthStr = monthNames[m]
        return "$d $monthStr $dayOfWeekStr"
    }
    return selectedDateStr
}

@Composable
private fun DateRangeDialog(
    initialStartDate: String,
    initialEndDate: String,
    onDismiss: () -> Unit,
    onApply: (start: String, end: String) -> Unit
) {
    val monthRange = remember { currentMonthRangeTr() }
    var startInput by remember { mutableStateOf(initialStartDate.ifBlank { monthRange.first }) }
    var endInput by remember { mutableStateOf(initialEndDate.ifBlank { monthRange.second }) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tarih Aralığı Seç",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Hızlı Seçim",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        SuggestionChip(
                            onClick = {
                                startInput = currentDateTr()
                                endInput = startInput
                            },
                            label = { Text("Bugün", fontSize = 11.sp) }
                        )
                    }
                    item {
                        SuggestionChip(
                            onClick = {
                                currentWeekRangeTr().also { startInput = it.first; endInput = it.second }
                            },
                            label = { Text("Bu Hafta", fontSize = 11.sp) }
                        )
                    }
                    item {
                        SuggestionChip(
                            onClick = {
                                startInput = monthRange.first
                                endInput = monthRange.second
                            },
                            label = { Text("Bu Ay", fontSize = 11.sp) }
                        )
                    }
                    item {
                        SuggestionChip(
                            onClick = {
                                val year = istanbulCalendar().get(Calendar.YEAR)
                                startInput = "01.01.$year"
                                endInput = "31.12.$year"
                            },
                            label = { Text("Tüm Yıl", fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = startInput,
                    onValueChange = { startInput = it },
                    label = { Text("Başlangıç Tarihi (GG.AA.YYYY)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = endInput,
                    onValueChange = { endInput = it },
                    label = { Text("Bitiş Tarihi (GG.AA.YYYY)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("İptal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onApply(startInput.trim(), endInput.trim())
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Uygula", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendBankTransferDialog(
    appointment: Appointment,
    bankAccounts: List<BankAccount>,
    initialAmount: String = "",
    onDismiss: () -> Unit,
    onSend: (accountKey: String, amount: Double?, date: String?, onResult: (Result<String>) -> Unit) -> Unit
) {
    val context = LocalContext.current

    val accountOptions = remember(bankAccounts) {
        bankAccounts
            .filter { it.isReady && it.bankName.isNotBlank() && it.accountHolder.isNotBlank() && it.iban.isNotBlank() }
            .map { acc -> acc.id to "${acc.cardTitle} - ${acc.bankName} (${acc.accountHolder})" }
    }

    var selectedAccountKey by remember { mutableStateOf(accountOptions.firstOrNull()?.first.orEmpty()) }
    var expandedAccountDropdown by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf(initialAmount) }
    var promisedDateText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun openDatePicker() {
        val cal = java.util.Calendar.getInstance()
        val parts = promisedDateText.trim().split(".")
        if (parts.size == 3) {
            val d = parts[0].toIntOrNull()
            val m = parts[1].toIntOrNull()
            val y = parts[2].toIntOrNull()
            if (d != null && m != null && y != null) {
                cal.set(y, m - 1, d)
            }
        }

        android.app.DatePickerDialog(
            context,
            { _, year, monthOfYear, dayOfMonth ->
                val formattedDay = String.format("%02d", dayOfMonth)
                val formattedMonth = String.format("%02d", monthOfYear + 1)
                promisedDateText = "$formattedDay.$formattedMonth.$year"
            },
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH),
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "IBAN / Ödeme Bilgisi Gönder",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${appointment.customerName} (${appointment.phone})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { if (!isLoading) onDismiss() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Account Selection Dropdown
                Text(
                    text = "Banka Hesabı *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expandedAccountDropdown,
                    onExpandedChange = {
                        if (accountOptions.isNotEmpty()) expandedAccountDropdown = !expandedAccountDropdown
                    }
                ) {
                    val currentLabel = accountOptions.find { it.first == selectedAccountKey }?.second
                        ?: "Tanımlı ve hazır banka hesabı yok"
                    OutlinedTextField(
                        value = currentLabel,
                        onValueChange = {},
                        readOnly = true,
                        enabled = accountOptions.isNotEmpty(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccountDropdown) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("bank_account_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = expandedAccountDropdown,
                        onDismissRequest = { expandedAccountDropdown = false }
                    ) {
                        accountOptions.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label, fontSize = 13.sp) },
                                onClick = {
                                    selectedAccountKey = key
                                    expandedAccountDropdown = false
                                }
                            )
                        }
                    }
                }

                if (accountOptions.isEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Gönderim için web yönetim panelinde eksiksiz bir banka hesabı tanımlanmalı.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount Input (Optional)
                Text(
                    text = "Tutar (TL) — Opsiyonel",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    placeholder = { Text("Örn: 1500", fontSize = 13.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Promised Date Input (Optional)
                Text(
                    text = "Söz Verilen Ödeme Tarihi — Opsiyonel",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = promisedDateText,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Takvimden tarih seçin", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Takvimden Seç",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("promised_date_input")
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { openDatePicker() }
                        )
                    }
                    if (promisedDateText.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { promisedDateText = "" },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tarihi Temizle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (!isLoading) onDismiss() },
                        enabled = !isLoading && accountOptions.isNotEmpty(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("İptal")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            val amountVal = parseLocalizedDouble(amountText)
                            val dateVal = promisedDateText.trim().ifBlank { null }

                            onSend(selectedAccountKey, amountVal, dateVal) { result ->
                                isLoading = false
                                result.onSuccess { channel ->
                                    val msg = if (channel.lowercase() == "sms") "SMS ile başarıyla gönderildi." else "WhatsApp üzerinden başarıyla gönderildi."
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    onDismiss()
                                }.onFailure { err ->
                                    errorMessage = err.message ?: "Gönderim sırasında hata oluştu."
                                }
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("send_bank_transfer_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gönder", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
