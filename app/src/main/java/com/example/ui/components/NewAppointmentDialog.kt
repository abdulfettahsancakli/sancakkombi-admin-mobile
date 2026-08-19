package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

enum class InterviewStep(
    val stepIndex: Int,
    val title: String,
    val questionPrompt: String,
    val hintText: String,
    val sampleAnswers: List<String>
) {
    NAME(
        stepIndex = 1,
        title = "1. Müşteri Adı",
        questionPrompt = "Müşterinin adı ve soyadı nedir?",
        hintText = "Sözlü yanıt verin (Örn: 'Ahmet Yılmaz')",
        sampleAnswers = listOf("Ahmet Yılmaz", "Mehmet Demir", "Mustafa Kaya")
    ),
    PHONE(
        stepIndex = 2,
        title = "2. Telefon",
        questionPrompt = "Müşterinin cep telefon numarası nedir?",
        hintText = "Örn: '0532 111 22 33'",
        sampleAnswers = listOf("0532 111 22 33", "0535 444 33 22", "0542 000 11 22")
    ),
    ADDRESS(
        stepIndex = 3,
        title = "3. İlçe & Adres",
        questionPrompt = "Servis hangi ilçe ve mahallede verilecek?",
        hintText = "Örn: 'Esenler Menderes Mahallesi Kamil Sokak No 12'",
        sampleAnswers = listOf("Esenler Menderes Mah.", "Bayrampaşa Muratpaşa Mah.", "Gaziosmanpaşa Merkez")
    ),
    SERVICE_DATE(
        stepIndex = 4,
        title = "4. Hizmet & Zaman",
        questionPrompt = "Hangi hizmet verilecek ve randevu hangi gün/saatte olsun?",
        hintText = "Örn: 'Kombi bakımı yarın saat 14:00'te'",
        sampleAnswers = listOf("Kombi bakımı yarın 14:00", "Petek temizliği pazartesi 10:00", "Arıza servisi bugün 16:00")
    ),
    NOTE(
        stepIndex = 5,
        title = "5. Arıza Notu",
        questionPrompt = "Arıza detayları veya eklemek istediğiniz özel bir not var mı?",
        hintText = "Örn: 'Kombi su sızdırıyor' veya 'Ek not yok'",
        sampleAnswers = listOf("Kombi su sızdırıyor", "Petekler ısınmıyor", "Ek not yok")
    )
}

private fun convertToIsoDate(dateStr: String): String {
    val trimmed = dateStr.trim()
    val parts = trimmed.split(".")
    if (parts.size == 3 && parts[0].length <= 2 && parts[1].length <= 2 && parts[2].length == 4) {
        val day = parts[0].padStart(2, '0')
        val month = parts[1].padStart(2, '0')
        val year = parts[2]
        return "$year-$month-$day"
    }
    return trimmed
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAppointmentDialog(
    onDismiss: () -> Unit,
    onSave: (Appointment) -> Unit,
    onGetAvailableSlots: (dateIso: String, onResult: (Result<List<String>>) -> Unit) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var customerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var neighborhood by remember { mutableStateOf("") }
    val todayFormatted = remember {
        java.text.SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")).format(java.util.Date())
    }
    var date by remember { mutableStateOf(todayFormatted) }
    var timeSlot by remember { mutableStateOf("13:00 - 14:00") }
    var serviceType by remember { mutableStateOf("Kombi Bakım & Servis") }
    var status by remember { mutableStateOf(AppointmentStatus.ONAYLANDI) }
    var problemNote by remember { mutableStateOf("") }

    // Voice AI States & Modes
    var voiceMode by remember { mutableStateOf("STEP_BY_STEP") } // "STEP_BY_STEP" or "FREE_FORM"
    var currentStep by remember { mutableStateOf(InterviewStep.NAME) }
    var voiceInputText by remember { mutableStateOf("") }
    var isAiAnalyzing by remember { mutableStateOf(false) }
    var aiStatusMessage by remember { mutableStateOf<String?>(null) }
    var aiSuccessState by remember { mutableStateOf<Boolean?>(null) }
    var isVoiceExpanded by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }

    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        try {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale("tr", "TR")
                    ttsInstance = tts
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        onDispose {
            try {
                tts?.stop()
                tts?.shutdown()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val speakText: (String) -> Unit = { textToSpeak ->
        try {
            ttsInstance?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "gemini_voice_interview")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val sampleVoicePrompts = remember {
        listOf(
            "Yarın 14:00'te Esenler Menderes Mahallesinde Ahmet Yılmaz'a kombi bakımı ekle, tel 05354443322",
            "Pazartesi Bayrampaşa Muratpaşa Mahallesinde Mehmet Demir kombi su sızdırıyor arıza servisi 05321112233",
            "Gaziosmanpaşa Barbaros Hayrettin Paşa Mahallesinde Mustafa Bey petek temizliği"
        )
    }

    val processStepAnswer: (String) -> Unit = { spoken ->
        if (spoken.isNotBlank()) {
            when (currentStep) {
                InterviewStep.NAME -> {
                    customerName = spoken.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("tr", "TR")) else it.toString() }
                    aiStatusMessage = "✅ Müşteri Adı: '$customerName' kaydedildi."
                    aiSuccessState = true
                    currentStep = InterviewStep.PHONE
                    speakText("Müşterinin iletişim telefon numarası nedir?")
                }
                InterviewStep.PHONE -> {
                    val digits = spoken.filter { it.isDigit() }
                    phone = if (digits.length >= 10) digits else spoken
                    aiStatusMessage = "✅ Telefon: '$phone' kaydedildi."
                    aiSuccessState = true
                    currentStep = InterviewStep.ADDRESS
                    speakText("Servis hangi ilçe ve mahallede verilecek?")
                }
                InterviewStep.ADDRESS -> {
                    coroutineScope.launch {
                        val parsed = GeminiVoiceAppointmentParser.parseVoiceText(spoken)
                        if (parsed.district.isNotBlank()) district = parsed.district
                        if (parsed.neighborhood.isNotBlank()) neighborhood = parsed.neighborhood
                        if (parsed.streetDoorNo.isNotBlank()) streetDoorNo = parsed.streetDoorNo
                        aiStatusMessage = "✅ Adres: '$district / $neighborhood $streetDoorNo' kaydedildi."
                        aiSuccessState = true
                        currentStep = InterviewStep.SERVICE_DATE
                        speakText("Hangi hizmet verilecek ve randevu ne zaman yapılmalı?")
                    }
                }
                InterviewStep.SERVICE_DATE -> {
                    coroutineScope.launch {
                        val parsed = GeminiVoiceAppointmentParser.parseVoiceText(spoken)
                        if (parsed.serviceType.isNotBlank()) serviceType = parsed.serviceType
                        if (parsed.date.isNotBlank()) date = parsed.date
                        if (parsed.timeSlot.isNotBlank()) timeSlot = parsed.timeSlot
                        aiStatusMessage = "✅ Hizmet: '$serviceType', Tarih: '$date $timeSlot' kaydedildi."
                        aiSuccessState = true
                        currentStep = InterviewStep.NOTE
                        speakText("Arıza detayları veya eklemek istediğiniz bir not var mı?")
                    }
                }
                InterviewStep.NOTE -> {
                    problemNote = spoken.trim()
                    aiStatusMessage = "🎉 Harika! Tüm 5 adım tamamlandı. Randevunuz kaydedilmeye hazır."
                    aiSuccessState = true
                    speakText("Tüm bilgiler alındı, randevunuzu kaydedebilirsiniz.")
                }
            }
            voiceInputText = ""
        }
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

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                voiceInputText = spokenText
                if (voiceMode == "STEP_BY_STEP") {
                    processStepAnswer(spokenText)
                } else {
                    processVoiceWithGemini(spokenText)
                }
            }
        }
    }

    val startVoiceListening: (String?) -> Unit = { promptOverride ->
        isVoiceExpanded = true
        try {
            val promptText = promptOverride ?: if (voiceMode == "STEP_BY_STEP") {
                currentStep.questionPrompt
            } else {
                "Randevu bilgilerini konuşun (Örn: Yarın 14:00 Esenler Ahmet Yılmaz kombi bakımı)"
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
                putExtra(RecognizerIntent.EXTRA_PROMPT, promptText)
            }
            speechLauncher.launch(intent)
            isListening = true
        } catch (e: Exception) {
            Toast.makeText(context, "Ses tanıma servisi açılamadı.", Toast.LENGTH_SHORT).show()
            isListening = false
        }
    }

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

    var availableSlots by remember { mutableStateOf<List<String>?>(null) }
    var isLoadingSlots by remember { mutableStateOf(false) }

    LaunchedEffect(date) {
        val isoDate = convertToIsoDate(date)
        if (isoDate.isNotBlank()) {
            isLoadingSlots = true
            onGetAvailableSlots(isoDate) { result ->
                isLoadingSlots = false
                result.onSuccess { slots ->
                    availableSlots = slots
                    if (timeSlot.isNotBlank() && !slots.contains(timeSlot)) {
                        timeSlot = ""
                    }
                }.onFailure {
                    // Fail-safe: fallback to allowing all slots if request fails
                    availableSlots = null
                }
            }
        } else {
            availableSlots = null
        }
    }

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
                // Title Header & Sesli Ekle Button
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

                    // "Sesli Ekle" Button that expands microphone area and triggers speech recognition
                    Surface(
                        onClick = {
                            if (!isVoiceExpanded) {
                                startVoiceListening(null)
                            } else {
                                isVoiceExpanded = false
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isVoiceExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("voice_add_toggle_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Sesli Ekle",
                                tint = if (isVoiceExpanded) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sesli Ekle",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isVoiceExpanded) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ==================== EXPANDABLE GEMINI VOICE AI SECTION ====================
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isVoiceExpanded) 0.45f else 0.2f),
                    border = BorderStroke(
                        width = if (isVoiceExpanded) 1.5.dp else 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = if (isVoiceExpanded) 0.8f else 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .clickable { if (!isVoiceExpanded) isVoiceExpanded = true }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Gemini Sesli Asistan",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (isVoiceExpanded) "Mikrofona konuşun veya metin yazın" else "Dokun veya 'Sesli Ekle' butonuna bas",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Icon(
                                imageVector = if (isVoiceExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        AnimatedVisibility(visible = isVoiceExpanded) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                // Mode Toggle Bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        onClick = { voiceMode = "STEP_BY_STEP" },
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (voiceMode == "STEP_BY_STEP") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "🤖 Adım Adım Soru-Cevap",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (voiceMode == "STEP_BY_STEP") Color.White else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                                        )
                                    }

                                    Surface(
                                        onClick = { voiceMode = "FREE_FORM" },
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (voiceMode == "FREE_FORM") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "⚡ Tek Cümle Hızlı",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (voiceMode == "FREE_FORM") Color.White else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                                        )
                                    }
                                }

                                if (voiceMode == "STEP_BY_STEP") {
                                    // Step Indicator Bar
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    ) {
                                        items(InterviewStep.values()) { step ->
                                            val isSelected = step == currentStep
                                            Surface(
                                                onClick = {
                                                    currentStep = step
                                                    speakText("Adım ${step.stepIndex}: ${step.questionPrompt}")
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                            ) {
                                                Text(
                                                    text = step.title,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Gemini Question Card
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.SmartToy,
                                                    contentDescription = "Gemini",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Adım ${currentStep.stepIndex} / 5: ${currentStep.title}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = currentStep.questionPrompt,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            IconButton(
                                                onClick = { speakText("Adım ${currentStep.stepIndex}: ${currentStep.questionPrompt}") },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.VolumeUp,
                                                    contentDescription = "Soruyu Okut",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Primary Voice Button & Input Field for Current Step
                                    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
                                    val pulseScale by infiniteTransition.animateFloat(
                                        initialValue = 0.95f,
                                        targetValue = 1.12f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(700),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "scale"
                                    )

                                    Button(
                                        onClick = { startVoiceListening(currentStep.questionPrompt) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isListening) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Mic,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(if (isListening) (20 * pulseScale).dp else 18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (isListening) "🎙️ Dinleniyor... Konuşun!" else "🎙️ Konuşarak Cevapla",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = voiceInputText,
                                        onValueChange = { voiceInputText = it },
                                        placeholder = { Text(currentStep.hintText, fontSize = 12.sp) },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = { processStepAnswer(voiceInputText) },
                                                enabled = voiceInputText.isNotBlank()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Send,
                                                    contentDescription = "Onayla",
                                                    tint = if (voiceInputText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                                                )
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = { processStepAnswer(voiceInputText) }),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Sample Quick Chips for Current Step
                                    Text(
                                        text = "Dokun-Doldur Örnek Yanıtlar:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(currentStep.sampleAnswers) { sample ->
                                            Surface(
                                                onClick = {
                                                    voiceInputText = sample
                                                    processStepAnswer(sample)
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                            ) {
                                                Text(
                                                    text = "💬 $sample",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Step Controls (Previous / Skip / Next)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                val prevIndex = currentStep.stepIndex - 1
                                                if (prevIndex >= 1) {
                                                    currentStep = InterviewStep.values().first { it.stepIndex == prevIndex }
                                                    speakText("Adım ${currentStep.stepIndex}: ${currentStep.questionPrompt}")
                                                }
                                            },
                                            enabled = currentStep.stepIndex > 1,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Önceki", fontSize = 11.sp)
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        OutlinedButton(
                                            onClick = {
                                                val nextIndex = currentStep.stepIndex + 1
                                                if (nextIndex <= 5) {
                                                    currentStep = InterviewStep.values().first { it.stepIndex == nextIndex }
                                                    speakText("Adım ${currentStep.stepIndex}: ${currentStep.questionPrompt}")
                                                }
                                            },
                                            enabled = currentStep.stepIndex < 5,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Atla", fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(imageVector = Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Button(
                                            onClick = {
                                                if (voiceInputText.isNotBlank()) {
                                                    processStepAnswer(voiceInputText)
                                                } else {
                                                    val nextIndex = currentStep.stepIndex + 1
                                                    if (nextIndex <= 5) {
                                                        currentStep = InterviewStep.values().first { it.stepIndex == nextIndex }
                                                        speakText("Adım ${currentStep.stepIndex}: ${currentStep.questionPrompt}")
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Sonraki", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    }

                                } else {
                                    // FREE FORM MODE UI
                                    // Pulsing Mic Action Banner
                                    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
                                    val pulseScale by infiniteTransition.animateFloat(
                                        initialValue = 0.95f,
                                        targetValue = 1.12f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(700),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "scale"
                                    )

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 10.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isListening) Color(0xFFD32F2F).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, if (isListening) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isListening) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Mic,
                                                        contentDescription = "Dinleniyor",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(if (isListening) (22 * pulseScale).dp else 22.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = if (isListening) "🎙️ Dinleniyor... Konuşun!" else "Sesle Doldurmak İçin Başlatın",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isListening) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = if (isListening) "Cümleniz otomatik Türkçe ses tanımasıyla alınacak." else "Örn: 'Yarın 14:00 Esenler Ahmet Yılmaz kombi bakımı'",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Button(
                                                onClick = { startVoiceListening(null) },
                                                shape = RoundedCornerShape(20.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isListening) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Text(if (isListening) "Yeniden Dinle" else "Başlat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = voiceInputText,
                                        onValueChange = { voiceInputText = it },
                                        placeholder = { Text("Konuşulan ses burada görünecektir...", fontSize = 12.sp) },
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
                                        singleLine = false,
                                        maxLines = 3,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "veya Hazır Cümleye Tıklayın:",
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
                            trailingIcon = {
                                if (isLoadingSlots) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTimeSlot)
                                }
                            },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTimeSlot,
                            onDismissRequest = { expandedTimeSlot = false }
                        ) {
                            timeSlots.forEach { slot ->
                                val isAvailable = availableSlots == null || availableSlots!!.contains(slot)
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = slot,
                                                fontSize = 13.sp,
                                                color = if (isAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                            )
                                            if (!isAvailable) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.errorContainer,
                                                    modifier = Modifier.padding(start = 6.dp)
                                                ) {
                                                    Text(
                                                        text = "Dolu",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    enabled = isAvailable,
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
                                try {
                                    com.example.utils.ReminderManager.scheduleAppointmentReminder(context, newAppt, 30)
                                    Toast.makeText(context, "⏰ Randevu oluşturuldu ve 30 dk öncesine hatırlatıcı kuruldu!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
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

