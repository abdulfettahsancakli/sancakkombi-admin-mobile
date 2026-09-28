package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.data.model.IstanbulLocationData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class AppointmentDialogMode { SCHEDULE, SERVICE_RECEIPT }

private val defaultTimeSlots = listOf(
    "09:00 - 11:00", "11:00 - 13:00", "13:00 - 15:00", "15:00 - 17:00", "17:00 - 19:00"
)

private val serviceOptions = listOf(
    "Genel Servis", "Kombi Bakım & Servis", "Petek Temizliği", "Arıza Onarımı", "Gaz Kaçağı Tespiti"
)

private val leadSources = listOf(
    "google_reklam" to "Google Reklamı",
    "google_arama" to "Google'da Arama",
    "tavsiye" to "Tavsiye / Tanıdık",
    "tabela" to "Tabela / Geçerken Gördü",
    "eski_musteri" to "Eski Müşteri",
    "sosyal_medya" to "Sosyal Medya",
    "bilinmiyor" to "Bilinmiyor / Sormadım"
)

private val faultPresets = listOf(
    "🔥 Sıcak Su Gelmiyor", "❄️ Petekler Isınmıyor", "💧 Su Akıtıyor",
    "📉 Basınç Düşüyor", "⚡ Ateşleme / F1 / E01", "🔊 Ses / Gürültü",
    "🛠️ Yıllık Bakım", "⚠️ Gaz Kokusu (Acil)"
)

private fun toIsoDate(date: String): String {
    val parts = date.split(".")
    return if (parts.size == 3) "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}" else date
}

private fun offsetDate(days: Int): String {
    val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, days) }
    return SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")).format(calendar.time)
}

private fun formatPhone(raw: String): String {
    var digits = raw.filter(Char::isDigit)
    if (digits.startsWith("90") && digits.length > 10) digits = digits.drop(2)
    if (digits.isNotBlank() && !digits.startsWith("0")) digits = "0$digits"
    digits = digits.take(11)
    return listOf(digits.take(4), digits.drop(4).take(3), digits.drop(7).take(2), digits.drop(9).take(2))
        .filter { it.isNotBlank() }
        .joinToString(" ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAppointmentDialog(
    customers: List<Customer> = emptyList(),
    initialDate: String? = null,
    onDismiss: () -> Unit,
    onSave: (Appointment, (Result<Unit>) -> Unit) -> Unit,
    onGetAvailableSlots: (String, (Result<List<String>>) -> Unit) -> Unit = { _, callback -> callback(Result.success(defaultTimeSlots)) },
    mode: AppointmentDialogMode = AppointmentDialogMode.SCHEDULE,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isReceiptMode = mode == AppointmentDialogMode.SERVICE_RECEIPT
    val today = remember { SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")).format(Date()) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerQuery by remember { mutableStateOf("") }
    var customerMenuOpen by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("Bayrampaşa") }
    var neighborhood by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var doorNo by remember { mutableStateOf("") }
    var date by remember(initialDate) { mutableStateOf(initialDate?.takeIf { it.isNotBlank() } ?: today) }
    var timeSlot by remember { mutableStateOf("") }
    var service by remember { mutableStateOf("Genel Servis") }
    var source by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(AppointmentStatus.ONAYLANDI) }
    var problemNote by remember { mutableStateOf("") }
    var slots by remember { mutableStateOf(if (isReceiptMode) defaultTimeSlots else emptyList()) }
    var slotsLoading by remember { mutableStateOf(!isReceiptMode) }
    var isSaving by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val neighborhoods = remember(district) { IstanbulLocationData.getNeighborhoods(district) }
    val streets = remember(district, neighborhood) {
        if (neighborhood.isBlank()) emptyList() else IstanbulLocationData.getStreets(context, district, neighborhood)
    }

    LaunchedEffect(date, mode) {
        if (isReceiptMode) {
            slots = defaultTimeSlots
            if (timeSlot.isBlank()) timeSlot = defaultTimeSlots.first()
            slotsLoading = false
        } else {
            slotsLoading = true
            onGetAvailableSlots(toIsoDate(date)) { result ->
                slots = result.getOrDefault(defaultTimeSlots)
                if (timeSlot !in slots) timeSlot = ""
                slotsLoading = false
            }
        }
    }

    fun chooseCustomer(customer: Customer) {
        selectedCustomer = customer
        customerQuery = customer.name
        name = customer.name
        phone = formatPhone(customer.phone)
        district = customer.district.ifBlank { "Bayrampaşa" }
        val matchedNeighborhood = IstanbulLocationData.getNeighborhoods(district).firstOrNull { option ->
            customer.address.contains(option.removeSuffix(" Mah."), ignoreCase = true)
        }.orEmpty()
        neighborhood = matchedNeighborhood
        street = if (matchedNeighborhood.isBlank()) customer.address else customer.address
            .replace(matchedNeighborhood, "", ignoreCase = true)
            .replace(matchedNeighborhood.removeSuffix(" Mah."), "", ignoreCase = true)
            .trim(' ', ',', '-')
        customerMenuOpen = false
    }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(0.96f).padding(vertical = 10.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Box {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)) {
                                Icon(
                                    imageVector = if (isReceiptMode) Icons.Default.ReceiptLong else Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(10.dp).size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (isReceiptMode) "Servis Fişi Kes" else "Yeni Randevu",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (isReceiptMode) "Randevusuz hızlı iş için müşteri ve hizmet bilgileri"
                                    else "Müşteriyi, adresi ve uygun zamanı planlayın",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = onDismiss, enabled = !isSaving) { Icon(Icons.Default.Close, "Kapat") }
                        }
                    }

                    item {
                        FormSection("Müşteri & İletişim", Icons.Default.Person) {
                            Box {
                                OutlinedTextField(
                                    value = customerQuery,
                                    onValueChange = { customerQuery = it; name = it; customerMenuOpen = true },
                                    label = { Text("Müşteri ara veya yeni isim yaz") },
                                    modifier = Modifier.fillMaxWidth().testTag("appointment_customer_picker"),
                                    singleLine = true
                                )
                                DropdownMenu(
                                    expanded = customerMenuOpen && customers.isNotEmpty(),
                                    onDismissRequest = { customerMenuOpen = false },
                                    modifier = Modifier.fillMaxWidth(.85f)
                                ) {
                                    customers
                                        .filter { !it.isArchived && (it.name.contains(customerQuery, true) || it.phone.contains(customerQuery, true)) }
                                        .take(8)
                                        .forEach { customer ->
                                            DropdownMenuItem(text = { Text("${customer.name} • ${customer.phone}") }, onClick = { chooseCustomer(customer) })
                                        }
                                }
                            }
                            OutlinedTextField(name, { name = it }, label = { Text("Ad Soyad *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(
                                phone,
                                { phone = formatPhone(it) },
                                label = { Text("Telefon *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            SelectionField(
                                "Bizi Nereden Buldu?",
                                leadSources.firstOrNull { it.first == source }?.second.orEmpty(),
                                leadSources.map { it.second }
                            ) { label -> source = leadSources.firstOrNull { it.second == label }?.first.orEmpty() }
                        }
                    }

                    item {
                        FormSection("Adres Bilgileri", Icons.Default.LocationOn) {
                            SelectionField("İlçe *", district, IstanbulLocationData.districts) {
                                district = it; neighborhood = ""; street = ""
                            }
                            SelectionField("Mahalle ${if (isReceiptMode) "" else "*"}", neighborhood, neighborhoods) {
                                neighborhood = it; street = ""
                            }
                            SelectionField(
                                label = "Cadde / Sokak ${if (isReceiptMode) "" else "*"}",
                                value = street,
                                options = streets,
                                onSelect = { street = it },
                                allowTextEntry = true
                            )
                            OutlinedTextField(
                                doorNo,
                                { doorNo = it },
                                label = { Text("Bina / Daire No") },
                                placeholder = { Text("No: 12 D: 4") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }

                    item {
                        FormSection("Tarih, Saat & Hizmet", Icons.Default.CalendarMonth) {
                            Text("HIZLI TARİH", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(listOf("Bugün" to offsetDate(0), "Yarın" to offsetDate(1), "+2 Gün" to offsetDate(2))) { preset ->
                                    FilterChip(
                                        selected = date == preset.second,
                                        onClick = { date = preset.second },
                                        label = { Text(preset.first) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = date,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(if (isReceiptMode) "İşin Yapıldığı Tarih *" else "Randevu Tarihi *") },
                                trailingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                                modifier = Modifier.fillMaxWidth().clickable {
                                    val current = Calendar.getInstance()
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, day -> date = "%02d.%02d.%04d".format(day, month + 1, year) },
                                        current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                            )
                            Text(
                                if (slotsLoading) "Müsait saatler kontrol ediliyor…" else "SAAT ARALIĞI *",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (slotsLoading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                            else if (slots.isEmpty()) Text("Seçilen tarihte müsait saat bulunamadı.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                            else LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(slots) { slot ->
                                    FilterChip(
                                        selected = timeSlot == slot,
                                        onClick = { timeSlot = slot },
                                        label = { Text(slot, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = .16f),
                                            selectedLabelColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                            SelectionField("Hizmet Türü", service, serviceOptions) { service = it }
                            if (!isReceiptMode) {
                                SelectionField("Randevu Durumu", status.label, AppointmentStatus.entries.map { it.label }) { label ->
                                    status = AppointmentStatus.entries.first { it.label == label }
                                }
                            }
                        }
                    }

                    item {
                        FormSection("Arıza & Müşteri Şikayeti", Icons.Default.CheckCircle) {
                            Text("HIZLI ARIZA ŞABLONLARI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(faultPresets) { preset ->
                                    val note = preset.substringAfter(' ')
                                    FilterChip(
                                        selected = problemNote.contains(note),
                                        onClick = {
                                            if (!problemNote.contains(note)) problemNote = listOf(problemNote, note).filter { it.isNotBlank() }.joinToString(", ")
                                        },
                                        label = { Text(preset, fontSize = 11.sp) }
                                    )
                                }
                            }
                            OutlinedTextField(
                                problemNote,
                                { problemNote = it },
                                label = { Text("Sorun / müşteri beyanı") },
                                placeholder = { Text("Örn: Sıcak su açıldığında kombi duruyor…") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )
                        }
                    }

                    item {
                        error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        if (saved) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16A34A))
                                Spacer(Modifier.width(6.dp))
                                Text("Randevu oluşturuldu", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = onDismiss, enabled = !isSaving, modifier = Modifier.weight(1f)) { Text("Vazgeç") }
                            Button(
                                onClick = {
                                    if (name.isBlank() || phone.filter(Char::isDigit).length < 10 || date.isBlank() || timeSlot.isBlank()) {
                                        error = "Ad, geçerli telefon, tarih ve saat zorunludur."
                                        return@Button
                                    }
                                    if (!isReceiptMode && (district.isBlank() || neighborhood.isBlank() || street.isBlank())) {
                                        error = "İlçe, mahalle ve cadde/sokak zorunludur."
                                        return@Button
                                    }
                                    val address = listOf(neighborhood, street, doorNo).filter { it.isNotBlank() }.joinToString(" ")
                                    val appointment = Appointment(
                                        id = UUID.randomUUID().toString(),
                                        customerId = selectedCustomer?.id.orEmpty(),
                                        customerName = name.trim(),
                                        phone = phone.trim(),
                                        district = district.trim(),
                                        neighborhood = neighborhood.trim(),
                                        streetDoorNo = listOf(street, doorNo).filter { it.isNotBlank() }.joinToString(" "),
                                        date = date,
                                        timeSlot = timeSlot,
                                        serviceType = service,
                                        status = if (isReceiptMode) AppointmentStatus.ONAYLANDI else status,
                                        source = source,
                                        addressDetail = address,
                                        problemNote = problemNote.trim()
                                    )
                                    isSaving = true
                                    error = null
                                    onSave(appointment) { result ->
                                        isSaving = false
                                        result.onSuccess { saved = true }.onFailure { error = it.message ?: "İşlem kaydedilemedi." }
                                    }
                                },
                                enabled = !isSaving && !saved && timeSlot.isNotBlank(),
                                modifier = Modifier.weight(1f).testTag(if (isReceiptMode) "continue_service_receipt" else "submit_new_appointment")
                            ) {
                                if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                else Text(if (isReceiptMode) "İş & Tahsilata İlerle" else "Randevuyu Kaydet", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                if (isSaving) Surface(color = MaterialTheme.colorScheme.scrim.copy(alpha = .3f), modifier = Modifier.fillMaxSize()) {}
            }
        }
    }
}

@Composable
private fun FormSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .38f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionField(
    label: String,
    value: String,
    options: List<String>,
    allowTextEntry: Boolean = false,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = value,
            onValueChange = { if (allowTextEntry) onSelect(it) },
            readOnly = !allowTextEntry,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            singleLine = true
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.filter { !allowTextEntry || value.isBlank() || it.contains(value, ignoreCase = true) }
                .take(40)
                .forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { onSelect(option); expanded = false }
                    )
                }
            if (options.isEmpty()) DropdownMenuItem(text = { Text("Seçenek bulunamadı") }, onClick = { expanded = false })
        }
    }
}
