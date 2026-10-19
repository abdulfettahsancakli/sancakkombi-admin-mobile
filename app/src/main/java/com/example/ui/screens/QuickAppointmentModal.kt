package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus
import com.example.data.model.Customer
import com.example.data.model.IstanbulLocationData
import com.example.ui.components.VoiceDictationButton
import java.util.Calendar
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickAppointmentContent(
    t: AppointmentTokens,
    todayIso: String,
    appointments: List<Appointment>,
    customers: List<Customer>,
    editingAppointment: Appointment? = null,
    onGetAvailableSlots: ((String, (Result<List<String>>) -> Unit) -> Unit)? = null,
    onShowToast: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (Appointment, Boolean, (Result<Unit>) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEditing = editingAppointment != null

    val initialAddressParts = remember(editingAppointment) {
        val mah = editingAppointment?.neighborhood ?: ""
        val rest = editingAppointment?.streetDoorNo?.ifBlank { editingAppointment.addressDetail } ?: ""
        val noMatch = Regex("No:\\s*(.+)", RegexOption.IGNORE_CASE).find(rest)
        val noStr = noMatch?.groupValues?.getOrNull(1)?.trim() ?: ""
        val streetStr = if (noMatch != null) {
            rest.substring(0, noMatch.range.first).trim().removeSuffix(",")
        } else rest
        Triple(mah, streetStr, noStr)
    }

    var selectedCustomerId by remember(editingAppointment) { mutableStateOf(editingAppointment?.customerId ?: "") }
    var name by remember(editingAppointment) { mutableStateOf(editingAppointment?.customerName ?: "") }
    var phone by remember(editingAppointment) { mutableStateOf(editingAppointment?.phone ?: "") }
    var district by remember(editingAppointment) {
        mutableStateOf(editingAppointment?.district?.ifBlank { "Bayrampaşa" } ?: "Bayrampaşa")
    }
    var neighborhood by remember(editingAppointment) { mutableStateOf(initialAddressParts.first) }
    var street by remember(editingAppointment) { mutableStateOf(initialAddressParts.second) }
    var buildingNo by remember(editingAppointment) { mutableStateOf(initialAddressParts.third) }
    var source by remember(editingAppointment) {
        mutableStateOf(editingAppointment?.source?.ifBlank { "Google Arama" } ?: "Google Arama")
    }
    var status by remember(editingAppointment) {
        mutableStateOf(editingAppointment?.status ?: AppointmentStatus.ONAYLANDI)
    }
    var dateIso by remember(editingAppointment, todayIso) {
        mutableStateOf(
            editingAppointment?.date?.let { normalizeToIsoDate(it) }?.ifBlank { todayIso } ?: todayIso
        )
    }
    var serviceType by remember(editingAppointment) {
        mutableStateOf(editingAppointment?.serviceType?.ifBlank { "Genel Servis" } ?: "Genel Servis")
    }
    var selectedSlot by remember(editingAppointment) {
        mutableStateOf(
            editingAppointment?.timeSlot?.takeIf { it.isNotBlank() }?.let { normalizeSlotKey(it) } ?: ""
        )
    }
    var note by remember(editingAppointment) { mutableStateOf(editingAppointment?.problemNote ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    // Customer autocomplete / picker dropdown
    var showCustomerSuggestions by remember { mutableStateOf(false) }
    val matchingCustomers = remember(name, customers) {
        val q = name.trim().lowercase(TrLocale)
        if (q.length < 2) customers.take(8)
        else customers.filter {
            it.name.lowercase(TrLocale).contains(q) || it.phone.contains(q)
        }.take(6)
    }

    // Contact picker launcher (uses ACTION_PICK on CommonDataKinds.Phone so no runtime permission is needed!)
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val contactUri = result.data?.data ?: return@rememberLauncherForActivityResult
        try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val pickedName = if (nameIdx >= 0) cursor.getString(nameIdx) ?: "" else ""
                    val pickedNumber = if (numIdx >= 0) cursor.getString(numIdx) ?: "" else ""
                    if (pickedName.isNotBlank()) name = pickedName
                    if (pickedNumber.isNotBlank()) phone = pickedNumber
                    onShowToast("📇 Rehberden aktarıldı: $pickedName")
                }
            }
        } catch (_: Exception) {
            onShowToast("⚠️ Rehber bilgisi okunamadı")
        }
    }

    // Remote available slots check (optional enhancement alongside local appointment conflict check)
    var remoteAvailableSlots by remember { mutableStateOf<Set<String>?>(null) }
    LaunchedEffect(dateIso) {
        remoteAvailableSlots = null
        onGetAvailableSlots?.invoke(dateIso) { res ->
            res.getOrNull()?.let { list ->
                if (list.isNotEmpty()) {
                    remoteAvailableSlots = list.map { normalizeSlotKey(it) }.toSet()
                }
            }
        }
    }

    val occupiedSlots = remember(appointments, dateIso, editingAppointment) {
        appointments
            .filter {
                normalizeToIsoDate(it.date) == dateIso &&
                    it.status != AppointmentStatus.IPTAL &&
                    it.id != editingAppointment?.id
            }
            .map { normalizeSlotKey(it.timeSlot) }
            .toSet()
    }

    val canSave = name.isNotBlank() && phone.isNotBlank() && selectedSlot.isNotBlank() && !isSaving

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(t.bg)
    ) {
        // Sticky Top Bar
        Surface(
            color = t.surface,
            border = BorderStroke(1.dp, t.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    color = t.surface2,
                    border = BorderStroke(1.dp, t.border),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "✕",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = t.text
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEditing) "Randevu Düzenle" else "Hızlı Randevu Kaydı",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = t.text
                    )
                    Text(
                        text = "Telefonla gelen işi saniyeler içinde kaydet",
                        fontSize = 11.sp,
                        color = t.text2
                    )
                }

                if (customers.isNotEmpty()) {
                    Box {
                        Surface(
                            onClick = { showCustomerSuggestions = !showCustomerSuggestions },
                            shape = RoundedCornerShape(10.dp),
                            color = t.primaryC,
                            border = BorderStroke(1.dp, t.primary.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("appointment_customer_picker")
                        ) {
                            Text(
                                text = "👥 Müşteri Seç",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = t.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showCustomerSuggestions,
                            onDismissRequest = { showCustomerSuggestions = false },
                            modifier = Modifier
                                .background(t.surface)
                                .heightIn(max = 280.dp)
                        ) {
                            matchingCustomers.forEach { cust ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = cust.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = t.text
                                            )
                                            Text(
                                                text = "${cust.phone} · ${cust.district}",
                                                fontSize = 11.sp,
                                                color = t.text2
                                            )
                                        }
                                    },
                                    onClick = {
                                        name = cust.name
                                        phone = cust.phone
                                        if (cust.district.isNotBlank()) district = cust.district
                                        if (cust.address.isNotBlank()) street = cust.address
                                        showCustomerSuggestions = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Scrollable Form Body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // CARD 1: Müşteri & İletişim
            FormSectionCard(t = t, title = "👤 Müşteri & İletişim") {
                FormFieldLabel(t = t, text = "Müşteri Adı Soyadı *")
                FormTextInput(
                    t = t,
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Örn: Ahmet Yılmaz"
                )

                Spacer(modifier = Modifier.height(10.dp))

                FormFieldLabel(t = t, text = "Telefon *")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormTextInput(
                        t = t,
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = "05XX XXX XX XX",
                        keyboardType = KeyboardType.Phone,
                        isMono = true,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        onClick = {
                            try {
                                val intent = Intent(
                                    Intent.ACTION_PICK,
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                                )
                                contactPickerLauncher.launch(intent)
                            } catch (_: Exception) {
                                onShowToast("⚠️ Rehber uygulaması açılamadı")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = t.primaryC,
                        border = BorderStroke(1.dp, t.primary),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📇 Rehber",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = t.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // İlçe & Mahalle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel(t = t, text = "İlçe *")
                        FormDropdownField(
                            t = t,
                            value = district,
                            options = IstanbulLocationData.districts,
                            onSelect = { newDist ->
                                district = newDist
                            }
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        val neighborhoodOptions = remember(district) {
                            IstanbulLocationData.getNeighborhoods(district)
                        }
                        FormFieldLabel(t = t, text = "Mahalle *")
                        FormEditableDropdownField(
                            t = t,
                            value = neighborhood,
                            onValueChange = { neighborhood = it },
                            placeholder = "Yıldırım, Kartaltepe...",
                            options = neighborhoodOptions
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cadde/Sokak (2fr) + No/Daire (1fr)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(2f)) {
                        val streetOptions = remember(district, neighborhood) {
                            IstanbulLocationData.getStreets(context, district, neighborhood)
                        }
                        FormFieldLabel(t = t, text = "Cadde / Sokak *")
                        FormEditableDropdownField(
                            t = t,
                            value = street,
                            onValueChange = { street = it },
                            placeholder = "Cadde veya sokak adı",
                            options = streetOptions
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel(t = t, text = "No / Daire")
                        FormTextInput(
                            t = t,
                            value = buildingNo,
                            onValueChange = { buildingNo = it },
                            placeholder = "12 / 4"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bizi Nereden Buldu & Durum
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel(t = t, text = "Bizi Nereden Buldu?")
                        FormDropdownField(
                            t = t,
                            value = source,
                            options = listOf(
                                "Google Arama",
                                "Tavsiye / Komşu",
                                "Eski Müşteri",
                                "Instagram / Sosyal Medya",
                                "Tabela / Dükkan",
                                "Diğer"
                            ),
                            onSelect = { source = it }
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel(t = t, text = "Durum *")
                        val statusOptions = listOf(
                            "Onaylandı" to AppointmentStatus.ONAYLANDI,
                            "Bekliyor" to AppointmentStatus.BEKLIYOR,
                            "Tamamlandı" to AppointmentStatus.TAMAMLANDI,
                            "İptal" to AppointmentStatus.IPTAL
                        )
                        val currentLabel = statusOptions.find { it.second == status }?.first ?: "Onaylandı"
                        FormDropdownField(
                            t = t,
                            value = currentLabel,
                            options = statusOptions.map { it.first },
                            onSelect = { label ->
                                statusOptions.find { it.first == label }?.second?.let { status = it }
                            }
                        )
                    }
                }
            }

            // CARD 2: Tarih & Saat
            FormSectionCard(t = t, title = "📅 Tarih & Saat") {
                // 3 Quick Date Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickDates = listOf(
                        0 to "Bugün",
                        1 to "Yarın",
                        2 to "+2 Gün"
                    )
                    quickDates.forEach { (offset, label) ->
                        val targetIso = addDaysIso(todayIso, offset)
                        val isSelected = dateIso == targetIso
                        Surface(
                            onClick = { dateIso = targetIso },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) t.primaryC else t.surface2,
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) t.primary else t.border
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) t.primary else t.text
                                )
                                Text(
                                    text = quickDateSub(todayIso, offset),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) t.primary else t.text2
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Date Picker & Service Type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel(t = t, text = "Randevu Tarihi *")
                        Surface(
                            onClick = {
                                val parts = dateIso.split("-")
                                val cal = Calendar.getInstance()
                                val y = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR)
                                val m = (parts.getOrNull(1)?.toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)) - 1
                                val d = parts.getOrNull(2)?.toIntOrNull() ?: cal.get(Calendar.DAY_OF_MONTH)
                                DatePickerDialog(
                                    context,
                                    { _, selY, selM, selD ->
                                        dateIso = "%04d-%02d-%02d".format(selY, selM + 1, selD)
                                    },
                                    y, m, d
                                ).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = t.surface2,
                            border = BorderStroke(1.dp, t.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = isoToDisplayDate(dateIso),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = t.text
                                )
                                Text(text = "📅", fontSize = 13.sp)
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel(t = t, text = "Hizmet Türü")
                        FormDropdownField(
                            t = t,
                            value = serviceType,
                            options = listOf(
                                "Genel Servis",
                                "Kombi Bakım & Servis",
                                "Petek Temizliği",
                                "Doğalgaz Tesisatı",
                                "Gaz Kaçağı Tespiti"
                            ),
                            onSelect = { serviceType = it },
                            height = 46.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Saat Aralığı Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saat Aralığı *",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = t.text2
                    )
                    Text(
                        text = if (selectedSlot.isNotBlank()) "Seçilen: $selectedSlot" else "Seçilmedi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedSlot.isNotBlank()) t.primary else t.amber
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 15 Time Slots in 3-column grid
                val chunkedSlots = remember { AllAppointmentTimeSlots.chunked(3) }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    chunkedSlots.forEach { rowSlots ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowSlots.forEach { slot ->
                                val isOccupied = occupiedSlots.contains(slot) ||
                                    (remoteAvailableSlots != null && !remoteAvailableSlots!!.contains(slot) && slot != normalizeSlotKey(editingAppointment?.timeSlot ?: ""))
                                val isSelected = selectedSlot == slot
                                Surface(
                                    onClick = {
                                        if (!isOccupied) selectedSlot = slot
                                    },
                                    enabled = !isOccupied,
                                    shape = RoundedCornerShape(11.dp),
                                    color = if (isSelected) t.primary else t.surface2,
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) t.primary else t.border
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .alpha(if (isOccupied) 0.4f else 1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = when {
                                                isOccupied -> "${slot.take(5)} dolu"
                                                isSelected -> "✓ ${slot.replace("-", "–")}"
                                                else -> slot.replace("-", "–")
                                            },
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isSelected) Color.White else t.text,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // CARD 3: Arıza & Müşteri Şikâyeti
            FormSectionCard(
                t = t,
                title = "🔧 Arıza & Müşteri Şikâyeti",
                rightHint = "Dokun, nota eklensin"
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickFaultChips.forEach { chip ->
                        val isActive = note.lowercase(TrLocale).contains(chip.value.lowercase(TrLocale))
                        val bg = when {
                            isActive -> t.primary
                            chip.urgent -> t.redC
                            else -> t.surface2
                        }
                        val fg = when {
                            isActive -> Color.White
                            chip.urgent -> t.red
                            else -> t.text
                        }
                        val borderCol = when {
                            isActive -> t.primary
                            chip.urgent -> t.red
                            else -> t.border
                        }
                        Surface(
                            onClick = {
                                val current = note.trim()
                                note = if (current.isEmpty()) chip.value else "$current, ${chip.value}"
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = bg,
                            border = BorderStroke(1.dp, borderCol)
                        ) {
                            Text(
                                text = chip.label,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = fg,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FormTextArea(
                    t = t,
                    value = note,
                    onValueChange = { note = it },
                    placeholder = "Müşteri ne dedi? Cihaz marka/modeli, hata kodu, daire tarifi...",
                    minHeight = 86.dp
                )
                VoiceDictationButton(
                    currentText = note,
                    onTextChange = { note = it },
                    label = "Şikâyeti sesle yaz"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sticky Bottom Action Bar
        Surface(
            color = t.surface,
            border = BorderStroke(1.dp, t.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    color = t.surface2,
                    border = BorderStroke(1.dp, t.border),
                    modifier = Modifier.height(52.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Vazgeç",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = t.text2
                        )
                    }
                }

                Surface(
                    onClick = {
                        if (!canSave) {
                            onShowToast("👀 Ad, telefon ve saat aralığını doldur")
                            return@Surface
                        }
                        isSaving = true
                        val cleanMah = if (neighborhood.isNotBlank()) {
                            val m = neighborhood.trim()
                            if (m.endsWith("Mah.", ignoreCase = true) || m.endsWith("Mahallesi", ignoreCase = true)) m else "$m Mah."
                        } else ""
                        val streetAndDoor = buildList {
                            if (street.isNotBlank()) add(street.trim())
                            if (buildingNo.isNotBlank()) add("No: ${buildingNo.trim()}")
                        }.joinToString(", ")
                        val fullAddress = listOf(cleanMah, streetAndDoor).filter { it.isNotBlank() }.joinToString(", ").ifBlank { "Adres belirtilmedi" }

                        val formattedSlot = selectedSlot.replace("-", " - ")
                        val savedAppt = Appointment(
                            id = editingAppointment?.id ?: UUID.randomUUID().toString(),
                            customerId = selectedCustomerId,
                            customerName = name.trim(),
                            phone = phone.trim(),
                            email = editingAppointment?.email ?: "",
                            district = district.trim().ifBlank { "Bayrampaşa" },
                            neighborhood = cleanMah,
                            streetDoorNo = streetAndDoor,
                            addressDetail = fullAddress,
                            serviceType = serviceType,
                            date = isoToDisplayDate(dateIso),
                            timeSlot = formattedSlot,
                            status = status,
                            source = source,
                            problemNote = note.trim().ifBlank { "Arıza kaydı girilmedi" },
                            jobReport = editingAppointment?.jobReport
                        )
                        onSave(savedAppt, isEditing) {
                            isSaving = false
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = t.primary,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .alpha(if (canSave) 1f else 0.5f)
                        .testTag(if (isEditing) "submit_edit_appointment" else "submit_new_appointment")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isEditing) "✓ Değişiklikleri Kaydet" else "✓ Randevuyu Kaydet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// REUSABLE FORM BUILDING BLOCKS
// ============================================================================

@Composable
fun FormSectionCard(
    t: AppointmentTokens,
    title: String,
    rightHint: String? = null,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = t.surface,
        border = BorderStroke(1.dp, t.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = t.text
                )
                if (rightHint != null) {
                    Text(
                        text = rightHint,
                        fontSize = 10.5.sp,
                        color = t.text2
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun FormFieldLabel(t: AppointmentTokens, text: String) {
    Text(
        text = text,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        color = t.text2,
        modifier = Modifier.padding(bottom = 5.dp)
    )
}

@Composable
fun FormTextInput(
    t: AppointmentTokens,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    keyboardType: KeyboardType = KeyboardType.Text,
    isMono: Boolean = false,
    testTag: String? = null
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = t.surface2,
        border = BorderStroke(1.dp, t.border),
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .let { if (testTag != null) it.testTag(testTag) else it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    fontSize = 13.5.sp,
                    color = t.text2.copy(alpha = 0.7f),
                    fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                cursorBrush = SolidColor(t.primary),
                textStyle = TextStyle(
                    color = t.text,
                    fontSize = if (isMono) 15.sp else 14.sp,
                    fontWeight = if (isMono) FontWeight.Bold else FontWeight.SemiBold,
                    fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun FormTextArea(
    t: AppointmentTokens,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minHeight: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = t.surface2,
        border = BorderStroke(1.dp, t.border),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.TopStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    fontSize = 13.sp,
                    color = t.text2.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                cursorBrush = SolidColor(t.primary),
                textStyle = TextStyle(
                    color = t.text,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight - 24.dp)
            )
        }
    }
}

@Composable
fun FormDropdownField(
    t: AppointmentTokens,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    height: Dp = 48.dp,
    testTag: String? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = t.surface2,
            border = BorderStroke(1.dp, t.border),
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .let { if (testTag != null) it.testTag(testTag) else it }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = value,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = t.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "▾",
                    fontSize = 13.sp,
                    color = t.text2
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(t.surface)
                .heightIn(max = 280.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontSize = 13.5.sp,
                            fontWeight = if (option == value) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (option == value) t.primary else t.text
                        )
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FormEditableDropdownField(
    t: AppointmentTokens,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    options: List<String>,
    height: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = t.surface2,
            border = BorderStroke(1.dp, t.border),
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 13.sp,
                            color = t.text2.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        cursorBrush = SolidColor(t.primary),
                        textStyle = TextStyle(
                            color = t.text,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (options.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { expanded = !expanded },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "▾", fontSize = 13.sp, color = t.text2)
                    }
                }
            }
        }
        if (options.isNotEmpty()) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(t.surface)
                    .heightIn(max = 260.dp)
            ) {
                val filtered = remember(value, options) {
                    val q = value.trim().lowercase(TrLocale)
                    if (q.isEmpty()) options else options.filter { it.lowercase(TrLocale).contains(q) }.ifEmpty { options }
                }
                filtered.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontSize = 13.sp,
                                color = t.text
                            )
                        },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
