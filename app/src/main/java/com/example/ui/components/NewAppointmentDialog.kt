package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus
import com.example.data.model.IstanbulLocationData
import com.example.data.remote.GeminiVoiceAppointmentParser
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAppointmentDialog(
    onDismiss: () -> Unit,
    onSave: (Appointment) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var customerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("Bayrampaşa") }
    var neighborhood by remember { mutableStateOf("Muratpaşa Mah.") }
    var streetDoorNo by remember { mutableStateOf("Kamil Caddesi No:12") }
    var date by remember { mutableStateOf("05.08.2026") }
    var timeSlot by remember { mutableStateOf("13:00 - 15:00") }
    var serviceType by remember { mutableStateOf("Kombi Bakım & Servis") }
    var status by remember { mutableStateOf(AppointmentStatus.ONAYLANDI) }
    var problemNote by remember { mutableStateOf("") }

    // Voice AI States
    var voiceInputText by remember { mutableStateOf("") }
    var isAiAnalyzing by remember { mutableStateOf(false) }
    var aiStatusMessage by remember { mutableStateOf<String?>(null) }
    var aiSuccessState by remember { mutableStateOf<Boolean?>(null) }

    val sampleVoicePrompts = remember {
        listOf(
            "Yarın 14:00'te Esenler Menderes Mahallesinde Ahmet Yılmaz'a kombi bakımı ekle, tel 05354443322",
            "Pazartesi Bayrampaşa Muratpaşa Mahallesinde Mehmet Demir kombi su sızdırıyor arıza servisi 05321112233",
            "Gaziosmanpaşa Barbaros Hayrettin Paşa Mahallesinde Mustafa Bey petek temizliği"
        )
    }

    val processVoiceWithGemini: (String) -> Unit = { rawPrompt ->
        if (rawPrompt.isNotBlank()) {
            isAiAnalyzing = true
            aiStatusMessage = null
            coroutineScope.launch {
                val parsed = GeminiVoiceAppointmentParser.parseVoiceText(rawPrompt)
                isAiAnalyzing = false

                if (parsed.customerName.isNotBlank()) customerName = parsed.customerName
                if (parsed.phone.isNotBlank()) phone = parsed.phone
                if (parsed.district.isNotBlank()) district = parsed.district
                if (parsed.neighborhood.isNotBlank()) neighborhood = parsed.neighborhood
                if (parsed.streetDoorNo.isNotBlank()) streetDoorNo = parsed.streetDoorNo
                if (parsed.date.isNotBlank()) date = parsed.date
                if (parsed.timeSlot.isNotBlank()) timeSlot = parsed.timeSlot
                if (parsed.serviceType.isNotBlank()) serviceType = parsed.serviceType
                if (parsed.problemNote.isNotBlank()) problemNote = parsed.problemNote

                aiStatusMessage = parsed.aiSummaryMessage
                aiSuccessState = parsed.missingFields.isEmpty()
            }
        }
    }

    val districts = IstanbulLocationData.districts
    val currentNeighborhoods = remember(district) { IstanbulLocationData.getNeighborhoods(district) }
    val currentStreets = remember(district, neighborhood) { IstanbulLocationData.getStreets(district, neighborhood) }

    val timeSlots = listOf("09:00 - 11:00", "11:00 - 13:00", "13:00 - 15:00", "15:00 - 17:00", "17:00 - 19:00")
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
            shape = RoundedCornerShape(20.dp),
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
                            text = "Sesli komutla veya manuel form doldurarak kaydet.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ==================== GEMINI VOICE AI SECTION ====================
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini Sesli / Metin AI Asistanı",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Randevu detaylarını serbest cümlelerle söyleyin veya yazın, AI tüm alanları anında doldursun.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = voiceInputText,
                            onValueChange = { voiceInputText = it },
                            placeholder = { Text("Örn: 'Yarın 14:00 Esenler Ahmet Yılmaz kombi bakımı 05321112233'") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Mikrofon",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (isAiAnalyzing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    IconButton(
                                        onClick = { processVoiceWithGemini(voiceInputText) },
                                        enabled = voiceInputText.isNotBlank()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Gönder",
                                            tint = if (voiceInputText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                                        )
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { processVoiceWithGemini(voiceInputText) }),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Sample Voice Chips
                        Text(
                            text = "Hızlı Doldurma Cümleleri:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(sampleVoicePrompts) { prompt ->
                                Surface(
                                    onClick = {
                                        voiceInputText = prompt
                                        processVoiceWithGemini(prompt)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = "💬 " + prompt.take(32) + "...",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // AI Feedback Banner
                        AnimatedVisibility(visible = aiStatusMessage != null) {
                            val msg = aiStatusMessage ?: ""
                            val isSuccess = aiSuccessState == true
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSuccess) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, if (isSuccess) Color(0xFF2E7D32).copy(alpha = 0.4f) else Color(0xFFF59E0B).copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                Text(
                                    text = msg,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFB45309),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
                // =================================================================

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
                                        val nList = IstanbulLocationData.getNeighborhoods(item)
                                        if (nList.isNotEmpty()) {
                                            neighborhood = nList.first()
                                            val sList = IstanbulLocationData.getStreets(item, nList.first())
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
                                        val sList = IstanbulLocationData.getStreets(district, nItem)
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
                    label = { Text("Sorun / Servis Notu") },
                    placeholder = { Text("Müşterinin belirttiği arıza veya talep notu...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
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

