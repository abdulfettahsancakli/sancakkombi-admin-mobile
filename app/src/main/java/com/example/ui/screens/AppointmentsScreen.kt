package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus
import com.example.data.model.BankAccount
import com.example.data.model.JobReport
import com.example.ui.components.CompleteJobDialog
import com.example.ui.components.EditAppointmentDialog
import com.example.ui.components.NewAppointmentDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    appointments: List<Appointment>,
    bankAccounts: List<BankAccount> = emptyList(),
    onBackClick: () -> Unit,
    onAddAppointment: (Appointment) -> Unit,
    onUpdateAppointment: (Appointment) -> Unit,
    onUpdateStatus: (String, AppointmentStatus) -> Unit,
    onCompleteJob: (String, JobReport) -> Unit,
    onDeleteAppointment: (String) -> Unit,
    onSendBankTransfer: (appointmentId: String, accountKey: String, amount: Double?, date: String?, onResult: (Result<String>) -> Unit) -> Unit = { _, _, _, _, _ -> },
    onGetAvailableSlots: (dateIso: String, onResult: (Result<List<String>>) -> Unit) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedStatusFilter by remember { mutableStateOf<AppointmentStatus?>(null) }
    var viewMode by remember { mutableStateOf("liste") } // "liste" vs "takvim"
    var startDate by remember { mutableStateOf("03.08.2026") }
    var endDate by remember { mutableStateOf("09.08.2026") }

    var showNewDialog by remember { mutableStateOf(false) }
    var editingAppointment by remember { mutableStateOf<Appointment?>(null) }
    var completingAppointment by remember { mutableStateOf<Appointment?>(null) }
    var bankTransferAppointment by remember { mutableStateOf<Appointment?>(null) }

    var expandedFilter by remember { mutableStateOf(false) }

    val filteredAppointments = remember(appointments, selectedStatusFilter) {
        if (selectedStatusFilter == null) appointments
        else appointments.filter { it.status == selectedStatusFilter }
    }

    val countTotal = appointments.size
    val countBekliyor = appointments.count { it.status == AppointmentStatus.BEKLIYOR }
    val countOnaylandi = appointments.count { it.status == AppointmentStatus.ONAYLANDI }
    val countTamamlandi = appointments.count { it.status == AppointmentStatus.TAMAMLANDI }
    val countIptal = appointments.count { it.status == AppointmentStatus.IPTAL }

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

                Button(
                    onClick = { showNewDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("new_appointment_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Yeni Randevu", fontWeight = FontWeight.Bold)
                }
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
                        text = "Görünen liste: ${filteredAppointments.size} kayıt | Toplam: $countTotal randevu",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // View Mode Switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (viewMode == "liste") MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { viewMode = "liste" }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Liste",
                                tint = if (viewMode == "liste") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Liste",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (viewMode == "liste") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (viewMode == "takvim") MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { viewMode = "takvim" }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Takvim",
                                tint = if (viewMode == "takvim") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Takvim",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (viewMode == "takvim") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips Bar (Horizontal Scrollable Chips)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null },
                        label = { Text("Tümü ($countTotal)", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == AppointmentStatus.BEKLIYOR,
                        onClick = { selectedStatusFilter = AppointmentStatus.BEKLIYOR },
                        label = { Text("Bekleyen ($countBekliyor)", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B))
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFF59E0B),
                            selectedLabelColor = Color.White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == AppointmentStatus.ONAYLANDI,
                        onClick = { selectedStatusFilter = AppointmentStatus.ONAYLANDI },
                        label = { Text("Onaylanan ($countOnaylandi)", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0288D1))
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0288D1),
                            selectedLabelColor = Color.White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == AppointmentStatus.TAMAMLANDI,
                        onClick = { selectedStatusFilter = AppointmentStatus.TAMAMLANDI },
                        label = { Text("Tamamlandı ($countTamamlandi)", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E7D32))
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2E7D32),
                            selectedLabelColor = Color.White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == AppointmentStatus.IPTAL,
                        onClick = { selectedStatusFilter = AppointmentStatus.IPTAL },
                        label = { Text("İptal ($countIptal)", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD32F2F))
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD32F2F),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date Range Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tarih Aralığı: $startDate - $endDate",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Değiştir",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { /* Date picker trigger */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stat Summary Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChipCard("TOPLAM", countTotal, Color(0xFF64748B), Modifier.weight(1f))
                StatChipCard("BEKLİYOR", countBekliyor, Color(0xFFF59E0B), Modifier.weight(1f))
                StatChipCard("ONAYLANDI", countOnaylandi, Color(0xFF3B82F6), Modifier.weight(1f))
                StatChipCard("TAMAMLANDI", countTamamlandi, Color(0xFF10B981), Modifier.weight(1f))
                StatChipCard("İPTAL", countIptal, Color(0xFFEF4444), Modifier.weight(1f))
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
                        Text(
                            text = "Henüz kayıtlı randevu bulunamadı.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                                onEdit = { editingAppointment = appt },
                                onDelete = { onDeleteAppointment(appt.id) }
                            )
                        }
                    }
                }
            } else {
                // Takvim (Weekly Grid View)
                WeeklyCalendarView(
                    appointments = filteredAppointments,
                    onAppointmentClick = { editingAppointment = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Dialog Triggers
    if (showNewDialog) {
        NewAppointmentDialog(
            onDismiss = { showNewDialog = false },
            onSave = { newAppt ->
                onAddAppointment(newAppt)
                showNewDialog = false
            },
            onGetAvailableSlots = onGetAvailableSlots
        )
    }

    editingAppointment?.let { appt ->
        EditAppointmentDialog(
            appointment = appt,
            onDismiss = { editingAppointment = null },
            onSave = { updated ->
                onUpdateAppointment(updated)
                editingAppointment = null
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
            onDismiss = { completingAppointment = null },
            onComplete = { report ->
                onCompleteJob(appt.id, report)
                completingAppointment = null
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
private fun StatChipCard(
    label: String,
    count: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    val statusColor = when (appointment.status) {
        AppointmentStatus.BEKLIYOR -> Color(0xFFF59E0B)
        AppointmentStatus.ONAYLANDI -> Color(0xFF0288D1)
        AppointmentStatus.TAMAMLANDI -> Color(0xFF2E7D32)
        AppointmentStatus.IPTAL -> Color(0xFFD32F2F)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
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
                // Header Row: Time & Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Customer Info Area
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

                if (appointment.addressDetail.isNotBlank()) {
                    Text(
                        text = appointment.addressDetail,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCall,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ara", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onWhatsapp,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF25D366)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF25D366))
                    }

                    OutlinedButton(
                        onClick = onRoute,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rota", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("✓ Onayla", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (appointment.status != AppointmentStatus.TAMAMLANDI && appointment.status != AppointmentStatus.IPTAL) {
                        Button(
                            onClick = onComplete,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("complete_job_button")
                        ) {
                            Text("✓ İş Kapanış & İmza", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onCancel,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("İptal", fontSize = 12.sp, color = Color(0xFFD32F2F))
                        }
                    }

                    OutlinedButton(
                        onClick = onSendIban,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("send_iban_button")
                    ) {
                        Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("IBAN Gönder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Düzenle", modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyCalendarView(
    appointments: List<Appointment>,
    onAppointmentClick: (Appointment) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf("PZT 3", "SAL 4", "ÇAR 5", "PER 6", "CUM 7", "CMT 8", "PAZ 9")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize()
        ) {
            // Days Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                days.forEach { day ->
                    Text(
                        text = day,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Calendar Slots Grid
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                val hours = (8..19).map { "%02d:00".format(it) }
                items(hours) { hr ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = hr,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .width(45.dp)
                                .padding(start = 4.dp)
                        )

                        // Sample card matching appointment time
                        val matching = appointments.firstOrNull { it.timeSlot.startsWith(hr.take(2)) || (hr == "13:00" && it.timeSlot.contains("13:00")) }
                        if (matching != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable { onAppointmentClick(matching) }
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = "${matching.customerName} - ${matching.serviceType}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
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
        if (bankAccounts.isNotEmpty()) {
            bankAccounts.map { acc -> acc.id to "${acc.cardTitle} - ${acc.bankName} (${acc.accountHolder})" }
        } else {
            listOf(
                "fatih" to "Fatih Sancaklı - Ziraat Bankası",
                "fettah" to "Fettah Sancaklı - Vakıfbank",
                "abdullah" to "Abdullah Sancaklı - İş Bankası"
            )
        }
    }

    var selectedAccountKey by remember { mutableStateOf(accountOptions.first().first) }
    var expandedAccountDropdown by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf(initialAmount) }
    var promisedDateText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    onExpandedChange = { expandedAccountDropdown = !expandedAccountDropdown }
                ) {
                    val currentLabel = accountOptions.find { it.first == selectedAccountKey }?.second ?: selectedAccountKey
                    OutlinedTextField(
                        value = currentLabel,
                        onValueChange = {},
                        readOnly = true,
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
                OutlinedTextField(
                    value = promisedDateText,
                    onValueChange = { promisedDateText = it },
                    placeholder = { Text("GG.AA.YYYY (Örn: 15.08.2026)", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("promised_date_input")
                )

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
                        enabled = !isLoading,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("İptal")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            val amountVal = amountText.trim().replace(",", ".").toDoubleOrNull()
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
