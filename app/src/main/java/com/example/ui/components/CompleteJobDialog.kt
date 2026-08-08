package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Appointment
import com.example.data.model.JobReport
import com.example.data.model.UsedPart
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CompleteJobDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onComplete: (JobReport) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(1) } // 1: Finans, 2: İş Detayı, 3: Fotoğraf & İmza

    // Basic Form State
    var technicianName by remember { mutableStateOf("Ahmet Usta") }
    var notifyCustomerMessage by remember { mutableStateOf(true) }
    var sendWhatsappPdf by remember { mutableStateOf(true) }

    // Revenue Section State
    var addRevenueRecord by remember { mutableStateOf(true) }
    var collectedAmount by remember { mutableStateOf("1200") }
    var paymentStatus by remember { mutableStateOf("Ödendi") }
    var paymentMethod by remember { mutableStateOf("Nakit") }
    var revenueNote by remember { mutableStateOf("Bakım ücreti, işçilik") }

    // Job Report Section State
    var addJobReport by remember { mutableStateOf(true) }
    var deviceBrand by remember { mutableStateOf("Demirdöküm") }
    var deviceModel by remember { mutableStateOf("Nitron Plus") }
    var workDoneNote by remember { mutableStateOf("Pilot ateşleyici temizlendi, genleşme tankı havası basıldı, sistem sızdırmazlık testi yapıldı.") }
    var warrantyMonths by remember { mutableStateOf("12") }

    val usedParts = remember { mutableStateListOf<UsedPart>() }
    var serviceFee by remember { mutableStateOf("800") }
    var otherFee by remember { mutableStateOf("400") }
    var deviceTested by remember { mutableStateOf(true) }
    var createExpenseRecord by remember { mutableStateOf(false) }

    // Photo & Signature State
    val context = LocalContext.current
    val photoUris = remember { mutableStateListOf<String>() }
    val customerSignatureLines = remember { mutableStateListOf<Line>() }
    val technicianSignatureLines = remember { mutableStateListOf<Line>() }
    var customerSignatureCanvasSize by remember { mutableStateOf(IntSize.Zero) }
    var technicianSignatureCanvasSize by remember { mutableStateOf(IntSize.Zero) }

    // Dropdown state
    var expandedTech by remember { mutableStateOf(false) }
    val techList = listOf("Ahmet Usta", "Mehmet Usta", "Ali Usta", "Caner Usta")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "İş Kapanış & Dijital İmza",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${appointment.customerName} • ${appointment.district}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3-Step Wizard Progress Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StepTab(
                        stepNumber = 1,
                        title = "1. Finans",
                        icon = Icons.Default.AttachMoney,
                        isActive = currentStep == 1,
                        isDone = currentStep > 1,
                        onClick = { currentStep = 1 },
                        modifier = Modifier.weight(1f)
                    )
                    StepTab(
                        stepNumber = 2,
                        title = "2. İş Detayı",
                        icon = Icons.Default.Build,
                        isActive = currentStep == 2,
                        isDone = currentStep > 2,
                        onClick = { currentStep = 2 },
                        modifier = Modifier.weight(1f)
                    )
                    StepTab(
                        stepNumber = 3,
                        title = "3. Foto/İmza",
                        icon = Icons.Default.Draw,
                        isActive = currentStep == 3,
                        isDone = false,
                        onClick = { currentStep = 3 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Step Content Area
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() with fadeOut() }
                ) { step ->
                    when (step) {
                        1 -> Step1FinanceView(
                            appointment = appointment,
                            technicianName = technicianName,
                            onTechnicianNameChange = { technicianName = it },
                            expandedTech = expandedTech,
                            onExpandedTechChange = { expandedTech = it },
                            techList = techList,
                            collectedAmount = collectedAmount,
                            onCollectedAmountChange = { collectedAmount = it },
                            paymentMethod = paymentMethod,
                            onPaymentMethodChange = { paymentMethod = it },
                            paymentStatus = paymentStatus,
                            onPaymentStatusChange = { paymentStatus = it },
                            revenueNote = revenueNote,
                            onRevenueNoteChange = { revenueNote = it },
                            notifyCustomerMessage = notifyCustomerMessage,
                            onNotifyCustomerChange = { notifyCustomerMessage = it },
                            sendWhatsappPdf = sendWhatsappPdf,
                            onSendWhatsappPdfChange = { sendWhatsappPdf = it }
                        )

                        2 -> Step2JobDetailsView(
                            deviceBrand = deviceBrand,
                            onDeviceBrandChange = { deviceBrand = it },
                            deviceModel = deviceModel,
                            onDeviceModelChange = { deviceModel = it },
                            workDoneNote = workDoneNote,
                            onWorkDoneNoteChange = { workDoneNote = it },
                            warrantyMonths = warrantyMonths,
                            onWarrantyMonthsChange = { warrantyMonths = it },
                            usedParts = usedParts,
                            serviceFee = serviceFee,
                            onServiceFeeChange = { serviceFee = it },
                            otherFee = otherFee,
                            onOtherFeeChange = { otherFee = it },
                            deviceTested = deviceTested,
                            onDeviceTestedChange = { deviceTested = it },
                            createExpenseRecord = createExpenseRecord,
                            onCreateExpenseRecordChange = { createExpenseRecord = it }
                        )

                        3 -> Step3SignAndPhotosView(
                            photoUris = photoUris,
                            customerSignatureLines = customerSignatureLines,
                            technicianSignatureLines = technicianSignatureLines,
                            onCustomerSizeChanged = { customerSignatureCanvasSize = it },
                            onTechnicianSizeChanged = { technicianSignatureCanvasSize = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Action Buttons Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Geri")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("Vazgeç")
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (currentStep < 3) {
                        Button(
                            onClick = { currentStep++ },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("İleri", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                val customerSignatureBitmap = renderSignatureToBitmap(customerSignatureLines, customerSignatureCanvasSize)
                                val technicianSignatureBitmap = renderSignatureToBitmap(technicianSignatureLines, technicianSignatureCanvasSize)
                                val customerSignaturePath = customerSignatureBitmap?.let {
                                    saveBitmapToCache(context, it, "customer_signature")
                                }
                                val technicianSignaturePath = technicianSignatureBitmap?.let {
                                    saveBitmapToCache(context, it, "technician_signature")
                                }

                                val report = JobReport(
                                    technicianName = technicianName,
                                    notifyCustomerMessage = notifyCustomerMessage,
                                    sendWhatsappPdf = sendWhatsappPdf,
                                    addRevenueRecord = addRevenueRecord,
                                    collectedAmount = collectedAmount,
                                    paymentStatus = paymentStatus,
                                    paymentMethod = paymentMethod,
                                    revenueNote = revenueNote,
                                    addJobReport = addJobReport,
                                    deviceBrand = deviceBrand,
                                    deviceModel = deviceModel,
                                    workDoneNote = workDoneNote,
                                    warrantyMonths = warrantyMonths,
                                    usedParts = usedParts.toList(),
                                    serviceFee = serviceFee,
                                    otherFee = otherFee,
                                    deviceTested = deviceTested,
                                    createExpenseRecord = createExpenseRecord,
                                    photoUris = photoUris.toList(),
                                    customerSignaturePath = customerSignaturePath,
                                    technicianSignaturePath = technicianSignaturePath
                                )
                                onComplete(report)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("submit_complete_job")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("İşi Kapat & Kaydet", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepTab(
    stepNumber: Int,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    isDone: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isActive -> MaterialTheme.colorScheme.primary
        isDone -> Color(0xFF2E7D32).copy(alpha = 0.15f)
        else -> Color.Transparent
    }
    val textColor = when {
        isActive -> Color.White
        isDone -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isDone) Icons.Default.Check else icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step1FinanceView(
    appointment: Appointment,
    technicianName: String,
    onTechnicianNameChange: (String) -> Unit,
    expandedTech: Boolean,
    onExpandedTechChange: (Boolean) -> Unit,
    techList: List<String>,
    collectedAmount: String,
    onCollectedAmountChange: (String) -> Unit,
    paymentMethod: String,
    onPaymentMethodChange: (String) -> Unit,
    paymentStatus: String,
    onPaymentStatusChange: (String) -> Unit,
    revenueNote: String,
    onRevenueNoteChange: (String) -> Unit,
    notifyCustomerMessage: Boolean,
    onNotifyCustomerChange: (Boolean) -> Unit,
    sendWhatsappPdf: Boolean,
    onSendWhatsappPdfChange: (Boolean) -> Unit
) {
    Column {
        // Compact Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = appointment.serviceType, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Tarih: ${appointment.date} • ${appointment.timeSlot}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Usta Seçimi
        ExposedDropdownMenuBox(
            expanded = expandedTech,
            onExpandedChange = { onExpandedTechChange(!expandedTech) },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = technicianName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Servisi Yapan Usta *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTech) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                singleLine = true
            )
            ExposedDropdownMenu(
                expanded = expandedTech,
                onDismissRequest = { onExpandedTechChange(false) }
            ) {
                techList.forEach { tech ->
                    DropdownMenuItem(
                        text = { Text(tech) },
                        onClick = {
                            onTechnicianNameChange(tech)
                            onExpandedTechChange(false)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tahsil Edilen Tutar
        Text(
            text = "TAHSİLAT TUTARI (₺)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = collectedAmount,
            onValueChange = onCollectedAmountChange,
            label = { Text("Tahsil Edilen Tutar") },
            prefix = { Text("₺ ", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2E7D32)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Ödeme Yöntemi Selection Chips
        Text(text = "Ödeme Yöntemi:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Nakit", "Kredi Kartı", "EFT / Havale").forEach { method ->
                FilterChip(
                    selected = paymentMethod == method,
                    onClick = { onPaymentMethodChange(method) },
                    label = { Text(method, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tahsilat Durumu Chips
        Text(text = "Tahsilat Durumu:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Ödendi", "Bekliyor").forEach { st ->
                FilterChip(
                    selected = paymentStatus == st,
                    onClick = { onPaymentStatusChange(st) },
                    label = { Text(st, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (st == "Ödendi") Color(0xFF2E7D32) else Color(0xFFF59E0B),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = revenueNote,
            onValueChange = onRevenueNoteChange,
            label = { Text("Gelir / Ödeme Notu") },
            placeholder = { Text("Örn: Nakit tahsil edildi, fiş kesildi") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Checkboxes
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = notifyCustomerMessage,
                onCheckedChange = onNotifyCustomerChange,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Text(text = "Müşteriye SMS/Bildirim gönder", fontSize = 12.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = sendWhatsappPdf,
                onCheckedChange = onSendWhatsappPdfChange,
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF25D366))
            )
            Text(text = "Servis fişini WhatsApp'tan PDF at", fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun Step2JobDetailsView(
    deviceBrand: String,
    onDeviceBrandChange: (String) -> Unit,
    deviceModel: String,
    onDeviceModelChange: (String) -> Unit,
    workDoneNote: String,
    onWorkDoneNoteChange: (String) -> Unit,
    warrantyMonths: String,
    onWarrantyMonthsChange: (String) -> Unit,
    usedParts: MutableList<UsedPart>,
    serviceFee: String,
    onServiceFeeChange: (String) -> Unit,
    otherFee: String,
    onOtherFeeChange: (String) -> Unit,
    deviceTested: Boolean,
    onDeviceTestedChange: (Boolean) -> Unit,
    createExpenseRecord: Boolean,
    onCreateExpenseRecordChange: (Boolean) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = deviceBrand,
                onValueChange = onDeviceBrandChange,
                label = { Text("Cihaz Markası") },
                placeholder = { Text("Demirdöküm, Vaillant...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(10.dp))
            OutlinedTextField(
                value = deviceModel,
                onValueChange = onDeviceModelChange,
                label = { Text("Cihaz Modeli") },
                placeholder = { Text("Örn: Nitron Plus") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = workDoneNote,
            onValueChange = onWorkDoneNoteChange,
            label = { Text("Yapılan İş / Servis Açıklaması *") },
            placeholder = { Text("Yapılan işlemleri ve kontrolleri yazın...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = warrantyMonths,
            onValueChange = onWarrantyMonthsChange,
            label = { Text("Verilen Garanti Süresi (Ay)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Kullanılan Parçalar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kullanılan Parçalar (${usedParts.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(
                onClick = {
                    usedParts.add(
                        UsedPart(
                            id = UUID.randomUUID().toString(),
                            name = "Yedek Parça / Malzeme",
                            quantity = 1,
                            price = 350.0
                        )
                    )
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Parça Ekle", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        usedParts.forEachIndexed { idx, part ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${idx + 1}. ${part.name} - ₺${part.price}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { usedParts.removeAt(idx) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = serviceFee,
                onValueChange = onServiceFeeChange,
                label = { Text("Servis İşçilik (₺)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(10.dp))
            OutlinedTextField(
                value = otherFee,
                onValueChange = onOtherFeeChange,
                label = { Text("Diğer Ücretler (₺)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = deviceTested,
                onCheckedChange = onDeviceTestedChange,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Text(text = "Cihaz test edildi, sorunsuz çalışıyor", fontSize = 12.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = createExpenseRecord,
                onCheckedChange = onCreateExpenseRecordChange,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Text(text = "Bu iş için malzeme/parça gider kaydı aç", fontSize = 12.sp)
        }
    }
}

@Composable
private fun Step3SignAndPhotosView(
    photoUris: MutableList<String>,
    customerSignatureLines: SnapshotStateList<Line>,
    technicianSignatureLines: SnapshotStateList<Line>,
    onCustomerSizeChanged: (IntSize) -> Unit,
    onTechnicianSizeChanged: (IntSize) -> Unit
) {
    Column {
        // Fotoğraflar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                PhotoPickerSection(
                    photoUris = photoUris,
                    onPhotosChanged = { updated ->
                        photoUris.clear()
                        photoUris.addAll(updated)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Müşteri İmzası
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                SignaturePad(
                    title = "Müşteri Dijital İmzası",
                    lines = customerSignatureLines,
                    onCanvasSizeChanged = onCustomerSizeChanged
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Firma Yetkilisi İmzası
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                SignaturePad(
                    title = "Firma Yetkilisi / Usta İmzası",
                    lines = technicianSignatureLines,
                    onCanvasSizeChanged = onTechnicianSizeChanged
                )
            }
        }
    }
}
