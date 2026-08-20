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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus
import com.example.data.model.IstanbulLocationData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAppointmentDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onSave: (Appointment) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val initialDistrict = appointment.district.ifBlank { "Bayrampaşa" }
    val allNeighborhoods = remember(initialDistrict) { IstanbulLocationData.getNeighborhoods(initialDistrict) }

    // Smart neighborhood detection
    val initialNeighborhood = remember(appointment) {
        if (appointment.neighborhood.isNotBlank() && allNeighborhoods.contains(appointment.neighborhood)) {
            appointment.neighborhood
        } else {
            allNeighborhoods.firstOrNull { nh ->
                val cleanNh = nh.replace(" Mah.", "").replace(" Mahallesi", "").trim()
                appointment.addressDetail.contains(cleanNh, ignoreCase = true)
            } ?: appointment.neighborhood.ifBlank { allNeighborhoods.firstOrNull() ?: "" }
        }
    }

    // Smart street extraction: strip neighborhood prefix if already included in addressDetail
    val initialStreet = remember(appointment, initialNeighborhood) {
        if (appointment.streetDoorNo.isNotBlank()) {
            appointment.streetDoorNo
        } else if (initialNeighborhood.isNotBlank()) {
            val cleanNh = initialNeighborhood.replace(" Mah.", "").replace(" Mahallesi", "").trim()
            val raw = appointment.addressDetail
            val regex = Regex("""^(${Regex.escape(initialNeighborhood)}|${Regex.escape(cleanNh)}\s*(Mah\.|Mahallesi|Mah\.?|Mh\.?))\s*[,.-]?\s*""", RegexOption.IGNORE_CASE)
            val stripped = raw.replace(regex, "").trim()
            stripped.ifBlank { raw }
        } else {
            appointment.addressDetail
        }
    }

    var customerName by remember { mutableStateOf(appointment.customerName) }
    var phone by remember { mutableStateOf(appointment.phone) }
    var email by remember { mutableStateOf(appointment.email) }
    var district by remember { mutableStateOf(initialDistrict) }
    var neighborhood by remember { mutableStateOf(initialNeighborhood) }
    var streetDoorNo by remember { mutableStateOf(initialStreet) }
    var date by remember { mutableStateOf(appointment.date) }
    var timeSlot by remember { mutableStateOf(appointment.timeSlot) }
    var serviceType by remember { mutableStateOf(appointment.serviceType) }
    var status by remember { mutableStateOf(appointment.status) }
    var problemNote by remember { mutableStateOf(appointment.problemNote) }

    val districts = IstanbulLocationData.districts
    val currentNeighborhoods = remember(district) { IstanbulLocationData.getNeighborhoods(district) }
    val currentStreets = remember(district, neighborhood) { IstanbulLocationData.getStreets(context, district, neighborhood) }

    val timeSlots = listOf(
        "09:00 - 10:00",
        "10:00 - 11:00",
        "11:00 - 12:00",
        "12:00 - 13:00",
        "13:00 - 14:00",
        "14:00 - 15:00",
        "15:00 - 16:00",
        "16:00 - 17:00",
        "17:00 - 18:00",
        "18:00 - 19:00",
        "19:00 - 20:00",
        "20:00 - 21:00",
        "21:00 - 22:00",
        "22:00 - 23:00",
        "23:00 - 00:00"
    )
    val services = listOf("Kombi Bakım & Servis", "Genel Servis", "Petek Temizliği", "Arıza Onarım", "Gaz Kaçağı Tespiti")

    var expandedDistrict by remember { mutableStateOf(false) }
    var expandedNeighborhood by remember { mutableStateOf(false) }
    var expandedStreet by remember { mutableStateOf(false) }
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

                // İlçe & Mahalle Dropdowns
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
                                        val nList = IstanbulLocationData.getNeighborhoods(item)
                                        if (nList.isNotEmpty()) {
                                            neighborhood = nList.first()
                                            val sList = IstanbulLocationData.getStreets(context, item, nList.first())
                                            streetDoorNo = if (sList.isNotEmpty()) "${sList.first()} No:12" else ""
                                        } else {
                                            neighborhood = ""
                                            streetDoorNo = ""
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedNeighborhood,
                        onExpandedChange = { expandedNeighborhood = !expandedNeighborhood },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = neighborhood,
                            onValueChange = {
                                neighborhood = it
                                expandedNeighborhood = true
                            },
                            label = { Text("Mahalle *") },
                            placeholder = { Text("Mahalle seçin") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNeighborhood) },
                            modifier = Modifier.menuAnchor(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = expandedNeighborhood,
                            onDismissRequest = { expandedNeighborhood = false }
                        ) {
                            currentNeighborhoods.forEach { nItem ->
                                DropdownMenuItem(
                                    text = { Text(nItem) },
                                    onClick = {
                                        neighborhood = nItem
                                        expandedNeighborhood = false
                                        val sList = IstanbulLocationData.getStreets(context, district, nItem)
                                        if (sList.isNotEmpty()) {
                                            streetDoorNo = "${sList.first()} No:12"
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cadde / Sokak / Kapı No
                ExposedDropdownMenuBox(
                    expanded = expandedStreet,
                    onExpandedChange = { expandedStreet = !expandedStreet },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = streetDoorNo,
                        onValueChange = {
                            streetDoorNo = it
                            expandedStreet = true
                        },
                        label = { Text("Cadde / Sokak / Kapı No") },
                        placeholder = { Text("Sokak seçin veya yazın") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStreet) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = expandedStreet,
                        onDismissRequest = { expandedStreet = false }
                    ) {
                        currentStreets.forEach { sItem ->
                            DropdownMenuItem(
                                text = { Text(sItem) },
                                onClick = {
                                    streetDoorNo = "$sItem No:12"
                                    expandedStreet = false
                                }
                            )
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
                    value = problemNote,
                    onValueChange = { problemNote = it },
                    label = { Text("Sorun Notu / Ek Açıklama") },
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
                                val fullAddress = if (neighborhood.isNotBlank()) {
                                    if (streetDoorNo.contains(neighborhood, ignoreCase = true)) streetDoorNo else "$neighborhood, $streetDoorNo".trim()
                                } else {
                                    streetDoorNo
                                }
                                val updated = appointment.copy(
                                    customerName = customerName.trim(),
                                    phone = phone.trim(),
                                    email = email.trim(),
                                    district = district,
                                    neighborhood = neighborhood,
                                    streetDoorNo = streetDoorNo.trim(),
                                    date = date,
                                    timeSlot = timeSlot,
                                    serviceType = serviceType,
                                    status = status,
                                    addressDetail = fullAddress,
                                    problemNote = problemNote.trim()
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
