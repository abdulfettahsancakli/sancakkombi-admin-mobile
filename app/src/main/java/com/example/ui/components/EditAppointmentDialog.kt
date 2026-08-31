package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.example.data.model.Customer
import java.util.Calendar

@Composable
fun EditAppointmentDialog(
    appointment: Appointment,
    customers: List<Customer> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Appointment, (Result<Unit>) -> Unit) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCustomer by remember { mutableStateOf(customers.find { it.id == appointment.customerId }) }
    var query by remember { mutableStateOf(appointment.customerName) }
    var customerMenuOpen by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(appointment.customerName) }
    var phone by remember { mutableStateOf(appointment.phone) }
    var district by remember { mutableStateOf(appointment.district) }
    var address by remember { mutableStateOf(appointment.addressDetail) }
    var date by remember { mutableStateOf(appointment.date) }
    var slot by remember { mutableStateOf(appointment.timeSlot) }
    var service by remember { mutableStateOf(appointment.serviceType) }
    var note by remember { mutableStateOf(appointment.problemNote) }
    var status by remember { mutableStateOf(appointment.status) }
    var statusMenuOpen by remember { mutableStateOf(false) }
    var serviceMenuOpen by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun chooseCustomer(customer: Customer) {
        selectedCustomer = customer
        query = customer.name
        name = customer.name
        phone = customer.phone
        district = customer.district
        address = customer.address
        customerMenuOpen = false
    }

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = modifier.fillMaxWidth(.94f).padding(vertical = 12.dp), shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
            Box {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Randevuyu Düzenle", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Müşteri, zaman ve servis bilgilerini güncelleyin.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = onDismiss, enabled = !isSaving) { Icon(Icons.Default.Close, "Kapat") }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Müşteri", fontWeight = FontWeight.Bold)
                            Box {
                                OutlinedTextField(query, { query = it; name = it; customerMenuOpen = true }, label = { Text("Mevcut müşteriden seçin") }, leadingIcon = { Icon(Icons.Default.Person, null) }, modifier = Modifier.fillMaxWidth().testTag("edit_appointment_customer_picker"), singleLine = true)
                                DropdownMenu(expanded = customerMenuOpen, onDismissRequest = { customerMenuOpen = false }) {
                                    customers.filter { !it.isArchived && (it.name.contains(query, true) || it.phone.contains(query, true)) }.take(8).forEach { customer ->
                                        DropdownMenuItem(text = { Text("${customer.name} • ${customer.phone}") }, onClick = { chooseCustomer(customer) })
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(name, { name = it }, label = { Text("Ad Soyad *") }, modifier = Modifier.weight(1f), singleLine = true)
                                OutlinedTextField(phone, { phone = it }, label = { Text("Telefon *") }, modifier = Modifier.weight(1f), singleLine = true)
                            }
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Randevu Detayları", fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = date, onValueChange = {}, readOnly = true, label = { Text("Tarih *") }, leadingIcon = { Icon(Icons.Default.CalendarMonth, null) }, modifier = Modifier.weight(1f).clickable {
                                    val cal = Calendar.getInstance()
                                    DatePickerDialog(context, { _, year, month, day -> date = "%02d.%02d.%04d".format(day, month + 1, year) }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                                })
                                OutlinedTextField(value = slot, onValueChange = { slot = it }, label = { Text("Saat aralığı *") }, modifier = Modifier.weight(1f), singleLine = true)
                            }
                            Box {
                                OutlinedTextField(value = service, onValueChange = {}, readOnly = true, label = { Text("Hizmet") }, trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) }, modifier = Modifier.fillMaxWidth().clickable { serviceMenuOpen = true })
                                DropdownMenu(expanded = serviceMenuOpen, onDismissRequest = { serviceMenuOpen = false }) {
                                    listOf("Kombi Bakım & Servis", "Genel Servis", "Petek Temizliği", "Arıza Onarımı", "Gaz Kaçağı Tespiti").forEach { item -> DropdownMenuItem(text = { Text(item) }, onClick = { service = item; serviceMenuOpen = false }) }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("İlçe") }, modifier = Modifier.weight(1f), singleLine = true)
                                Box(Modifier.weight(1f)) {
                                    OutlinedTextField(value = status.label, onValueChange = {}, readOnly = true, label = { Text("Durum") }, modifier = Modifier.fillMaxWidth().clickable { statusMenuOpen = true })
                                    DropdownMenu(expanded = statusMenuOpen, onDismissRequest = { statusMenuOpen = false }) { AppointmentStatus.values().forEach { item -> DropdownMenuItem(text = { Text(item.label) }, onClick = { status = item; statusMenuOpen = false }) } }
                                }
                            }
                            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Adres") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Sorun / servis notu") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                        }
                    }
                    if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    if (saved) Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16A34A)); Spacer(Modifier.width(6.dp)); Text("Randevu güncellendi", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { onDelete(appointment.id) }, enabled = !isSaving, modifier = Modifier.weight(1f), colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Arşivle / Sil") }
                        Button(onClick = onDismiss, enabled = !isSaving, modifier = Modifier.weight(1f)) { Text("Vazgeç") }
                        Button(onClick = {
                            if (name.isBlank() || phone.isBlank() || date.isBlank() || slot.isBlank()) { error = "Ad, telefon, tarih ve saat zorunludur."; return@Button }
                            isSaving = true; error = null
                            onSave(appointment.copy(customerId = selectedCustomer?.id ?: appointment.customerId, customerName = name.trim(), phone = phone.trim(), district = district.trim(), addressDetail = address.trim(), date = date, timeSlot = slot.trim(), serviceType = service, status = status, problemNote = note.trim())) { result ->
                                isSaving = false
                                result.onSuccess { saved = true }.onFailure { error = it.message ?: "Randevu güncellenemedi." }
                            }
                        }, enabled = !isSaving && !saved, modifier = Modifier.weight(1f).testTag("submit_edit_appointment")) {
                            if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp) else Text("Kaydet", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (isSaving) Surface(color = MaterialTheme.colorScheme.scrim.copy(alpha = .35f), modifier = Modifier.fillMaxSize()) {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Spacer(Modifier.height(8.dp)); Text("Randevu güncelleniyor…", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
