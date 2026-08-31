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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus
import com.example.data.model.Customer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

private fun toIsoDate(date: String): String {
    val parts = date.split(".")
    return if (parts.size == 3) "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}" else date
}

@Composable
fun NewAppointmentDialog(
    customers: List<Customer> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Appointment, (Result<Unit>) -> Unit) -> Unit,
    onGetAvailableSlots: (String, (Result<List<String>>) -> Unit) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val today = remember { SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")).format(Date()) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerQuery by remember { mutableStateOf("") }
    var customerMenuOpen by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today) }
    var timeSlot by remember { mutableStateOf("") }
    var service by remember { mutableStateOf("Kombi Bakım & Servis") }
    var problemNote by remember { mutableStateOf("") }
    var serviceMenuOpen by remember { mutableStateOf(false) }
    var slotMenuOpen by remember { mutableStateOf(false) }
    var slots by remember { mutableStateOf<List<String>>(emptyList()) }
    var slotsLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(date) {
        slotsLoading = true
        onGetAvailableSlots(toIsoDate(date)) { result ->
            slots = result.getOrDefault(emptyList())
            if (timeSlot.isBlank()) timeSlot = slots.firstOrNull().orEmpty()
            slotsLoading = false
        }
    }

    fun chooseCustomer(customer: Customer) {
        selectedCustomer = customer
        customerQuery = customer.name
        name = customer.name
        phone = customer.phone
        district = customer.district
        address = customer.address
        customerMenuOpen = false
    }

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = modifier.fillMaxWidth(0.94f).padding(vertical = 12.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Box {
                LazyColumn(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Yeni Randevu Oluştur", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("Müşteriyi seçin, zamanı belirleyin ve işi planlayın.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = onDismiss, enabled = !isSaving) { Icon(Icons.Default.Close, "Kapat") }
                        }
                    }
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)), shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Müşteri", fontWeight = FontWeight.Bold)
                                Box {
                                    OutlinedTextField(
                                        value = customerQuery,
                                        onValueChange = { customerQuery = it; name = it; customerMenuOpen = true },
                                        label = { Text("Mevcut müşteriden seçin veya yeni kişi yazın") },
                                        leadingIcon = { Icon(Icons.Default.Person, null) },
                                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                                        modifier = Modifier.fillMaxWidth().testTag("appointment_customer_picker"),
                                        singleLine = true
                                    )
                                    DropdownMenu(expanded = customerMenuOpen && customers.isNotEmpty(), onDismissRequest = { customerMenuOpen = false }) {
                                        customers.filter { !it.isArchived && (it.name.contains(customerQuery, true) || it.phone.contains(customerQuery, true)) }.take(8).forEach { customer ->
                                            DropdownMenuItem(text = { Text("${customer.name} • ${customer.phone}") }, onClick = { chooseCustomer(customer) })
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(name, { name = it }, label = { Text("Ad Soyad *") }, modifier = Modifier.weight(1f), singleLine = true)
                                    OutlinedTextField(phone, { phone = it }, label = { Text("Telefon *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.weight(1f), singleLine = true)
                                }
                            }
                        }
                    }
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)), shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Randevu Detayları", fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = date,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Tarih *") },
                                        leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                                        modifier = Modifier.weight(1f).clickable {
                                            val cal = Calendar.getInstance()
                                            DatePickerDialog(context, { _, year, month, day -> date = "%02d.%02d.%04d".format(day, month + 1, year) }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                                        }
                                    )
                                    Box(Modifier.weight(1f)) {
                                        OutlinedTextField(value = timeSlot, onValueChange = {}, readOnly = true, label = { Text("Saat aralığı *") }, trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) }, modifier = Modifier.fillMaxWidth().clickable { slotMenuOpen = true })
                                        DropdownMenu(expanded = slotMenuOpen, onDismissRequest = { slotMenuOpen = false }) {
                                            if (slotsLoading) DropdownMenuItem(text = { Text("Saatler yükleniyor…") }, onClick = {})
                                            else if (slots.isEmpty()) DropdownMenuItem(text = { Text("Uygun saat bulunamadı") }, onClick = {})
                                            else slots.forEach { slot -> DropdownMenuItem(text = { Text(slot) }, onClick = { timeSlot = slot; slotMenuOpen = false }) }
                                        }
                                    }
                                }
                                Box {
                                    OutlinedTextField(value = service, onValueChange = {}, readOnly = true, label = { Text("Hizmet") }, trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) }, modifier = Modifier.fillMaxWidth().clickable { serviceMenuOpen = true })
                                    DropdownMenu(expanded = serviceMenuOpen, onDismissRequest = { serviceMenuOpen = false }) {
                                        listOf("Kombi Bakım & Servis", "Genel Servis", "Petek Temizliği", "Arıza Onarımı", "Gaz Kaçağı Tespiti").forEach { item -> DropdownMenuItem(text = { Text(item) }, onClick = { service = item; serviceMenuOpen = false }) }
                                    }
                                }
                                OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("İlçe") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Adres") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                                OutlinedTextField(value = problemNote, onValueChange = { problemNote = it }, label = { Text("Sorun / servis notu") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                            }
                        }
                    }
                    item {
                        if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        if (saved) {
                            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16A34A)); Spacer(Modifier.width(6.dp)); Text("Randevu oluşturuldu", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = onDismiss, enabled = !isSaving, modifier = Modifier.weight(1f)) { Text("Vazgeç") }
                            Button(
                                onClick = {
                                    if (name.isBlank() || phone.isBlank() || date.isBlank() || timeSlot.isBlank()) { error = "Ad, telefon, tarih ve saat zorunludur."; return@Button }
                                    isSaving = true; error = null
                                    onSave(Appointment(UUID.randomUUID().toString(), selectedCustomer?.id.orEmpty(), name.trim(), phone.trim(), district = district.trim(), date = date, timeSlot = timeSlot, serviceType = service, status = AppointmentStatus.ONAYLANDI, addressDetail = address.trim(), problemNote = problemNote.trim())) { result ->
                                        isSaving = false
                                        result.onSuccess { saved = true }.onFailure { error = it.message ?: "Randevu oluşturulamadı." }
                                    }
                                },
                                enabled = !isSaving && !saved,
                                modifier = Modifier.weight(1f).testTag("submit_new_appointment")
                            ) {
                                if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp) else Text("Randevuyu Oluştur", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                if (isSaving) {
                    Surface(color = MaterialTheme.colorScheme.scrim.copy(alpha = .35f), modifier = Modifier.fillMaxSize()) {
                        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(8.dp))
                            Text("Randevu oluşturuluyor…", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
