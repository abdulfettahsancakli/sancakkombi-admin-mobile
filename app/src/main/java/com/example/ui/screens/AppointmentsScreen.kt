package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus
import com.example.data.model.JobReport
import com.example.ui.components.CompleteJobDialog
import com.example.ui.components.EditAppointmentDialog
import com.example.ui.components.NewAppointmentDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    appointments: List<Appointment>,
    onBackClick: () -> Unit,
    onAddAppointment: (Appointment) -> Unit,
    onUpdateAppointment: (Appointment) -> Unit,
    onUpdateStatus: (String, AppointmentStatus) -> Unit,
    onCompleteJob: (String, JobReport) -> Unit,
    onDeleteAppointment: (String) -> Unit,
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

            // Filter Bar Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expandedFilter,
                            onExpandedChange = { expandedFilter = !expandedFilter },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            OutlinedTextField(
                                value = selectedStatusFilter?.label ?: "Tüm Durumlar",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("DURUM", fontSize = 10.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFilter) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedFilter,
                                onDismissRequest = { expandedFilter = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Tüm Durumlar") },
                                    onClick = {
                                        selectedStatusFilter = null
                                        expandedFilter = false
                                    }
                                )
                                AppointmentStatus.values().forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text(st.label) },
                                        onClick = {
                                            selectedStatusFilter = st
                                            expandedFilter = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("BAŞLANGIÇ", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("BİTİŞ", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
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
            }
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
            onDismiss = { completingAppointment = null },
            onComplete = { report ->
                onCompleteJob(appt.id, report)
                completingAppointment = null
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
    onCancel: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (appointment.status) {
        AppointmentStatus.BEKLIYOR -> Color(0xFFF59E0B)
        AppointmentStatus.ONAYLANDI -> Color(0xFF3B82F6)
        AppointmentStatus.TAMAMLANDI -> Color(0xFF10B981)
        AppointmentStatus.IPTAL -> Color(0xFFEF4444)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Date / Time + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.date,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = appointment.timeSlot,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "• ${appointment.status.label}",
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Customer Info & Phone Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = appointment.district,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick Action Chips: Ara, WhatsApp, Rota
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = onCall,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Ara", fontSize = 10.sp)
                    }

                    OutlinedButton(
                        onClick = onWhatsapp,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("WhatsApp", fontSize = 10.sp)
                    }

                    OutlinedButton(
                        onClick = onRoute,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Rota", fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Service & Address
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Text(
                    text = appointment.serviceType,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            if (appointment.addressDetail.isNotBlank()) {
                Text(
                    text = "Adres: ${appointment.addressDetail}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (appointment.problemNote.isNotBlank()) {
                Text(
                    text = "Not: ${appointment.problemNote}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (appointment.status == AppointmentStatus.BEKLIYOR) {
                    OutlinedButton(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Onayla", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (appointment.status != AppointmentStatus.TAMAMLANDI && appointment.status != AppointmentStatus.IPTAL) {
                    Button(
                        onClick = onComplete,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("complete_job_button")
                    ) {
                        Text("Tamamla", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        ),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("İptal", fontSize = 11.sp, color = Color(0xFFEF4444))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Düzenle", modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
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
