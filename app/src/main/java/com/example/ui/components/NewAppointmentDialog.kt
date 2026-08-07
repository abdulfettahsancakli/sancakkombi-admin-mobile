package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAppointmentDialog(
    onDismiss: () -> Unit,
    onSave: (Appointment) -> Unit,
    modifier: Modifier = Modifier
) {
    var customerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("Bayrampaşa") }
    var neighborhood by remember { mutableStateOf("") }
    var streetDoorNo by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("05.08.2026") }
    var timeSlot by remember { mutableStateOf("13:00 - 15:00") }
    var serviceType by remember { mutableStateOf("Kombi Bakım & Servis") }
    var status by remember { mutableStateOf(AppointmentStatus.ONAYLANDI) }
    var problemNote by remember { mutableStateOf("") }

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
                // Title Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Yeni Randevu Oluştur",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Telefonla gelen randevuyu manuel olarak kaydet.",
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
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Müşteri Adı *") },
                    placeholder = { Text("Müşteri ad soyad...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_customer_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefon *") },
                    placeholder = { Text("05xx xxx xx xx") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_phone"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // İlçe & Mahalle
                Row(modifier = Modifier.fillMaxWidth()) {
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

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedTextField(
                        value = neighborhood,
                        onValueChange = { neighborhood = it },
                        label = { Text("Mahalle *") },
                        placeholder = { Text("Mahalle seçin") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cadde / Sokak & Daire No
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = streetDoorNo,
                        onValueChange = { streetDoorNo = it },
                        label = { Text("Cadde / Sokak *") },
                        placeholder = { Text("Cadde veya sokak yazın") },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Daire No") },
                        placeholder = { Text("No:5/3") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
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
                            label = { Text("Durum *") },
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

                // Sorun Notu
                OutlinedTextField(
                    value = problemNote,
                    onValueChange = { problemNote = it },
                    label = { Text("Sorun Notu") },
                    placeholder = { Text("Müşterinin belirttiği arıza veya talep notu...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            if (customerName.isNotBlank() && phone.isNotBlank()) {
                                val newAppt = Appointment(
                                    id = "a_" + UUID.randomUUID().toString().take(8),
                                    customerId = "c_" + UUID.randomUUID().toString().take(8),
                                    customerName = customerName,
                                    phone = phone,
                                    district = district,
                                    neighborhood = neighborhood,
                                    streetDoorNo = streetDoorNo,
                                    date = date,
                                    timeSlot = timeSlot,
                                    serviceType = serviceType,
                                    status = status,
                                    addressDetail = if (neighborhood.isNotBlank()) "$district - $neighborhood $streetDoorNo" else "$district $streetDoorNo",
                                    problemNote = problemNote
                                )
                                onSave(newAppt)
                            }
                        },
                        enabled = customerName.isNotBlank() && phone.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("submit_create_appointment")
                    ) {
                        Text("Randevu Oluştur", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
