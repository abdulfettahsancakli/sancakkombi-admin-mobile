package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAppointmentDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onSave: (Appointment) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var customerName by remember { mutableStateOf(appointment.customerName) }
    var phone by remember { mutableStateOf(appointment.phone) }
    var email by remember { mutableStateOf(appointment.email) }
    var district by remember { mutableStateOf(appointment.district) }
    var date by remember { mutableStateOf(appointment.date) }
    var timeSlot by remember { mutableStateOf(appointment.timeSlot) }
    var serviceType by remember { mutableStateOf(appointment.serviceType) }
    var status by remember { mutableStateOf(appointment.status) }
    var addressDetail by remember { mutableStateOf(appointment.addressDetail) }
    var problemNote by remember { mutableStateOf(appointment.problemNote) }

    val districts = listOf("Bayrampaşa", "Esenler", "Gaziosmanpaşa", "Zeytinburnu", "Fatih", "Eyüpsultan")
    val timeSlots = listOf("09:00 - 11:00", "11:00 - 13:00", "13:00 - 15:00", "15:00 - 17:00", "17:00 - 19:00")
    val services = listOf("Kombi Bakım & Servis", "Genel Servis", "Petek Temizliği", "Arıza Onarım", "Gaz Kaçağı Tespiti")

    var expandedDistrict by remember { mutableStateOf(false) }
    var expandedTimeSlot by remember { mutableStateOf(false) }
    var expandedService by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Randevu Düzenle",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = appointment.customerName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Müşteri Adı & Telefon
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Müşteri Adı *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Telefon *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // E-posta & İlçe
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-posta") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedDistrict,
                        onExpandedChange = { expandedDistrict = !expandedDistrict },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = district,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("İlçe *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDistrict) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDistrict,
                            onDismissRequest = { expandedDistrict = false }
                        ) {
                            districts.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        district = item
                                        expandedDistrict = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tarih & Saat Aralığı
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Tarih *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedTimeSlot,
                        onExpandedChange = { expandedTimeSlot = !expandedTimeSlot },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = timeSlot,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Saat Aralığı *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTimeSlot) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTimeSlot,
                            onDismissRequest = { expandedTimeSlot = false }
                        ) {
                            timeSlots.forEach { slot ->
                                DropdownMenuItem(
                                    text = { Text(slot) },
                                    onClick = {
                                        timeSlot = slot
                                        expandedTimeSlot = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hizmet & Durum
                Row(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedService,
                        onExpandedChange = { expandedService = !expandedService },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = serviceType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hizmet") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedService) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedService,
                            onDismissRequest = { expandedService = false }
                        ) {
                            services.forEach { service ->
                                DropdownMenuItem(
                                    text = { Text(service) },
                                    onClick = {
                                        serviceType = service
                                        expandedService = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedStatus,
                        onExpandedChange = { expandedStatus = !expandedStatus },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = status.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Durum") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false }
                        ) {
                            AppointmentStatus.values().forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st.label) },
                                    onClick = {
                                        status = st
                                        expandedStatus = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = addressDetail,
                    onValueChange = { addressDetail = it },
                    label = { Text("Adres Detayı") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = problemNote,
                    onValueChange = { problemNote = it },
                    label = { Text("Sorun Notu") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Sil (Left), Vazgeç, Kaydet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onDelete(appointment.id) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        ),
                        modifier = Modifier.testTag("delete_appointment_button")
                    ) {
                        Text("Sil", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Vazgeç")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val updated = appointment.copy(
                                customerName = customerName,
                                phone = phone,
                                email = email,
                                district = district,
                                date = date,
                                timeSlot = timeSlot,
                                serviceType = serviceType,
                                status = status,
                                addressDetail = addressDetail,
                                problemNote = problemNote
                            )
                            onSave(updated)
                        },
                        enabled = customerName.isNotBlank() && phone.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("submit_edit_appointment")
                    ) {
                        Text("Kaydet", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
