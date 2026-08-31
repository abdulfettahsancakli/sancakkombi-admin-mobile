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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.data.model.AppointmentStatus
import com.example.data.model.BankAccount
import com.example.data.model.JobReport
import com.example.data.model.StockItem
import com.example.data.model.UsedPart
import com.example.ui.screens.SendBankTransferDialog
import com.example.utils.parseLocalizedDouble
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CompleteJobDialog(
    appointment: Appointment,
    bankAccounts: List<BankAccount> = emptyList(),
    stockItems: List<StockItem> = emptyList(),
    onDismiss: () -> Unit,
    onComplete: (JobReport, (Result<Unit>) -> Unit) -> Unit,
    onSendBankTransfer: (accountKey: String, amount: Double?, date: String?, onResult: (Result<String>) -> Unit) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(1) } // 1: Finans, 2: İş Detayı, 3: Fotoğraf & İmza

    // IBAN Sending Dialog State
    var showIbanDialog by remember { mutableStateOf(false) }

    val isEditMode = appointment.status == AppointmentStatus.TAMAMLANDI
    val existingReport = appointment.jobReport

    // Basic Form State
    var technicianName by remember { mutableStateOf(existingReport?.technicianName?.trim().orEmpty()) }
    var notifyCustomerMessage by remember { mutableStateOf(!isEditMode) }
    var sendWhatsappPdf by remember { mutableStateOf(!isEditMode) }

    // Revenue Section State
    var addRevenueRecord by remember { mutableStateOf(existingReport?.addRevenueRecord ?: true) }
    var collectedAmount by remember { mutableStateOf(existingReport?.collectedAmount ?: "") }
    var paymentStatus by remember { mutableStateOf(existingReport?.paymentStatus?.ifBlank { "Bekliyor" } ?: "Bekliyor") }
    var paymentMethod by remember { mutableStateOf(existingReport?.paymentMethod?.trim().orEmpty()) }
    var paymentPromiseDate by remember { mutableStateOf(existingReport?.paymentPromiseDate?.trim().orEmpty()) }
    var revenueNote by remember { mutableStateOf(existingReport?.revenueNote ?: "") }

    // Job Report Section State
    var addJobReport by remember { mutableStateOf(existingReport?.addJobReport ?: true) }
    var deviceBrand by remember { mutableStateOf(existingReport?.deviceBrand ?: "") }
    var deviceModel by remember { mutableStateOf(existingReport?.deviceModel ?: "") }
    var workDoneNote by remember { mutableStateOf(existingReport?.workDoneNote ?: "") }
    var warrantyMonths by remember { mutableStateOf(existingReport?.warrantyMonths ?: "") }

    val usedParts = remember {
        mutableStateListOf<UsedPart>().apply {
            existingReport?.usedParts?.let { addAll(it) }
        }
    }
    var serviceFee by remember { mutableStateOf(existingReport?.serviceFee ?: "") }
    var otherFee by remember { mutableStateOf(existingReport?.otherFee ?: "") }
    var deviceTested by remember { mutableStateOf(existingReport?.deviceTested ?: false) }
    var createExpenseRecord by remember { mutableStateOf(existingReport?.createExpenseRecord ?: false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var submitSuccess by remember { mutableStateOf(false) }

    // Photo & Signature State
    val context = LocalContext.current
    val photoUris = remember {
        mutableStateListOf<String>().apply {
            existingReport?.photoUris?.let { addAll(it) }
        }
    }
    val customerSignatureLines = remember { mutableStateListOf<Line>() }
    val technicianSignatureLines = remember { mutableStateListOf<Line>() }
    var customerSignatureCanvasSize by remember { mutableStateOf(IntSize.Zero) }
    var technicianSignatureCanvasSize by remember { mutableStateOf(IntSize.Zero) }

    // Dropdown state
    var expandedTech by remember { mutableStateOf(false) }
    val techList = listOf("Fatih Sancaklı", "Abdullah Sancaklı")

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
                            text = if (isEditMode) "Servis Fişini Düzenle" else "İş Kapanış & Dijital İmza",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEditMode) "Servis raporunu ve detayları güncelleyin" else "Servis raporunu ve tahsilatı tamamlayın",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            paymentPromiseDate = paymentPromiseDate,
                            onPaymentPromiseDateChange = { paymentPromiseDate = it },
                            revenueNote = revenueNote,
                            onRevenueNoteChange = { revenueNote = it },
                            onOpenIbanDialog = { showIbanDialog = true }
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
                            stockItems = stockItems,
                            serviceFee = serviceFee,
                            onServiceFeeChange = { serviceFee = it },
                            otherFee = otherFee,
                            onOtherFeeChange = { otherFee = it },
                            createExpenseRecord = createExpenseRecord,
                            onCreateExpenseRecordChange = { createExpenseRecord = it }
                        )

                        3 -> Step3SignAndPhotosView(
                            photoUris = photoUris,
                            customerSignatureLines = customerSignatureLines,
                            technicianSignatureLines = technicianSignatureLines,
                            hasCustomerSignature = !existingReport?.customerSignaturePath.isNullOrBlank(),
                            hasTechnicianSignature = !existingReport?.technicianSignaturePath.isNullOrBlank(),
                            deviceTested = deviceTested,
                            onDeviceTestedChange = { deviceTested = it },
                            sendWhatsappPdf = sendWhatsappPdf,
                            onSendWhatsappPdfChange = { sendWhatsappPdf = it },
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
                                if (isSubmitting) return@Button
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
                                    paymentPromiseDate = paymentPromiseDate,
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
                                isSubmitting = true
                                submitError = null
                                onComplete(report) { result ->
                                    isSubmitting = false
                                    result.onSuccess { submitSuccess = true }
                                        .onFailure { submitError = it.message ?: "İş kapatılamadı." }
                                }
                            },
                            enabled = !isSubmitting && !submitSuccess,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isEditMode) MaterialTheme.colorScheme.primary else Color(0xFF2E7D32)),
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("submit_complete_job")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isEditMode) "Fişi Güncelle & Kaydet" else "İşi Kapat & Kaydet", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (submitSuccess) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tamamlandı", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Servis fişi, finans ve stok bilgileri güncellendi.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onDismiss) { Text("Kapat") }
                }
            }
        }
    }
    if (submitError != null) {
        Dialog(onDismissRequest = { submitError = null }) {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("İşlem tamamlanamadı", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(submitError!!, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { submitError = null }) { Text("Tekrar Dene") }
                }
            }
        }
    }
    if (isSubmitting) {
        Dialog(onDismissRequest = {}) {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("İş kapatılıyor…", fontWeight = FontWeight.Bold)
                    Text("Servis fişi, finans ve stok bilgileri kaydediliyor.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showIbanDialog) {
        SendBankTransferDialog(
            appointment = appointment,
            bankAccounts = bankAccounts,
            initialAmount = collectedAmount,
            onDismiss = { showIbanDialog = false },
            onSend = { accountKey, amount, date, onResult ->
                onSendBankTransfer(accountKey, amount, date, onResult)
            }
        )
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
    paymentPromiseDate: String,
    onPaymentPromiseDateChange: (String) -> Unit,
    revenueNote: String,
    onRevenueNoteChange: (String) -> Unit,
    onOpenIbanDialog: () -> Unit = {}
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
            placeholder = { Text("Örn: 1200") },
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
                    onClick = {
                        onPaymentMethodChange(method)
                        if (method == "EFT / Havale") {
                            onOpenIbanDialog()
                        }
                    },
                    label = { Text(method, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (paymentMethod == "EFT / Havale") {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Müşteriye IBAN / Ödeme Bilgisi Gönder",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "EFT / Havale seçildi. Müşteriye WhatsApp veya SMS ile şirket banka hesap bilgilerini iletebilirsiniz.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onOpenIbanDialog,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .testTag("open_iban_dialog_from_complete_job")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("IBAN Bilgilerini Gönder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tahsilat Durumu Chips
        Text(text = "Tahsilat Durumu:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Ödendi", "Kısmi", "Bekliyor").forEach { st ->
                FilterChip(
                    selected = paymentStatus == st,
                    onClick = { onPaymentStatusChange(st) },
                    label = { Text(st, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (st) {
                            "Ödendi" -> Color(0xFF2E7D32)
                            "Kısmi" -> Color(0xFF2563EB)
                            else -> Color(0xFFF59E0B)
                        },
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (paymentStatus != "Ödendi") {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = paymentPromiseDate,
                onValueChange = onPaymentPromiseDateChange,
                label = { Text("Ödeme Sözü Tarihi (GG.AA.YYYY)") },
                placeholder = { Text("Tahsilat tarihi") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = revenueNote,
            onValueChange = onRevenueNoteChange,
            label = { Text("Servis / Usta Notu (Gelecek İçin Not)") },
            placeholder = { Text("Örn: Gelecek sefere eşanjör temizlenecek, petek vanası gevşek") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
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
    stockItems: List<StockItem>,
    serviceFee: String,
    onServiceFeeChange: (String) -> Unit,
    otherFee: String,
    onOtherFeeChange: (String) -> Unit,
    createExpenseRecord: Boolean,
    onCreateExpenseRecordChange: (Boolean) -> Unit
) {
    val partsTotal = usedParts.sumOf { it.quantity * it.price }
    val serviceTotal = partsTotal + (parseLocalizedDouble(serviceFee) ?: 0.0) + (parseLocalizedDouble(otherFee) ?: 0.0)
    Column {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .35f))) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("Servis fişi toplamı", fontSize = 11.sp); Text("Parça + servis + diğer ücret", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Text("₺%.2f".format(serviceTotal).replace('.', ','), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
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
            placeholder = { Text("Örn: 12") },
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
                    val stock = stockItems.firstOrNull() ?: return@OutlinedButton
                    usedParts.add(
                        UsedPart(
                            id = UUID.randomUUID().toString(),
                            name = stock.name,
                            quantity = 1,
                            price = stock.salePrice,
                            stockItemId = stock.id
                        )
                    )
                },
                enabled = stockItems.isNotEmpty(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Parça Ekle", fontSize = 11.sp)
            }
        }

        if (usedParts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            usedParts.forEachIndexed { index, part ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = part.name,
                            onValueChange = { newName ->
                                usedParts[index] = part.copy(name = newName)
                            },
                            label = { Text("Parça") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(
                            value = if (part.price > 0) part.price.toInt().toString() else "",
                            onValueChange = { newPriceStr ->
                                val p = parseLocalizedDouble(newPriceStr) ?: 0.0
                                usedParts[index] = part.copy(price = p)
                            },
                            label = { Text("₺") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(72.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { usedParts.removeAt(index) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Ücret Dağılımı
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = serviceFee,
                onValueChange = onServiceFeeChange,
                label = { Text("Servis / İşçilik (₺)") },
                placeholder = { Text("Örn: 800") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(10.dp))
            OutlinedTextField(
                value = otherFee,
                onValueChange = onOtherFeeChange,
                label = { Text("Diğer Ücretler (₺)") },
                placeholder = { Text("Örn: 400") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCreateExpenseRecordChange(!createExpenseRecord) }
                .padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = createExpenseRecord,
                onCheckedChange = onCreateExpenseRecordChange,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(text = "Bu iş için malzeme/parça gider kaydı aç", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Kullanılan parçaların maliyeti gider tablosuna işlenir.", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun Step3SignAndPhotosView(
    photoUris: MutableList<String>,
    customerSignatureLines: SnapshotStateList<Line>,
    technicianSignatureLines: SnapshotStateList<Line>,
    deviceTested: Boolean,
    onDeviceTestedChange: (Boolean) -> Unit,
    sendWhatsappPdf: Boolean,
    serviceTotal: Double = 0.0,
    deviceBrand: String = "",
    onDeviceBrandChange: (String) -> Unit = {},
    deviceModel: String = "",
    onDeviceModelChange: (String) -> Unit,
    workDoneNote: String,
    onWorkDoneNoteChange: (String) -> Unit,
    warrantyMonths: String,
    onWarrantyMonthsChange: (String) -> Unit,
    usedParts: MutableList<UsedPart>,
    stockItems: List<StockItem>,
    serviceFee: String,
    onServiceFeeChange: (String) -> Unit,
    otherFee: String,
    onOtherFeeChange: (String) -> Unit,
    createExpenseRecord: Boolean,
    onCreateExpenseRecordChange: (Boolean) -> Unit
) {
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .35f))
        ) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Servis fişi toplamı", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Parça + servis + diğer ücret", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("₺%.2f".format(serviceTotal).replace('.', ','), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
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
            placeholder = { Text("Örn: 12") },
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
                    val stock = stockItems.firstOrNull() ?: return@OutlinedButton
                    usedParts.add(
                        UsedPart(
                            id = UUID.randomUUID().toString(),
                            name = stock.name,
                            quantity = 1,
                            price = stock.salePrice,
                            stockItemId = stock.id
                        )
                    )
                },
                enabled = stockItems.isNotEmpty(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Parça Ekle", fontSize = 11.sp)
            }
        }

        if (usedParts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            usedParts.forEachIndexed { index, part ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = part.name,
                            onValueChange = { newName ->
                                usedParts[index] = part.copy(name = newName)
                            },
                            label = { Text("Parça") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(
                            value = part.quantity.toString(),
                            onValueChange = { quantity -> usedParts[index] = part.copy(quantity = quantity.toIntOrNull()?.coerceAtLeast(1) ?: 1) },
                            label = { Text("Adet") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(58.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(
                            value = if (part.price > 0) part.price.toInt().toString() else "",
                            onValueChange = { newPriceStr ->
                                val p = parseLocalizedDouble(newPriceStr) ?: 0.0
                                usedParts[index] = part.copy(price = p)
                            },
                            label = { Text("₺") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(72.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { usedParts.removeAt(index) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Ücret Dağılımı
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = serviceFee,
                onValueChange = onServiceFeeChange,
                label = { Text("Servis / İşçilik (₺)") },
                placeholder = { Text("Örn: 800") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(10.dp))
            OutlinedTextField(
                value = otherFee,
                onValueChange = onOtherFeeChange,
                label = { Text("Diğer Ücretler (₺)") },
                placeholder = { Text("Örn: 400") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCreateExpenseRecordChange(!createExpenseRecord) }
                .padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = createExpenseRecord,
                onCheckedChange = onCreateExpenseRecordChange,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(text = "Bu iş için malzeme/parça gider kaydı aç", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Kullanılan parçaların maliyeti gider tablosuna işlenir.", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun Step3SignAndPhotosView(
    photoUris: MutableList<String>,
    customerSignatureLines: SnapshotStateList<Line>,
    technicianSignatureLines: SnapshotStateList<Line>,
    hasCustomerSignature: Boolean = false,
    hasTechnicianSignature: Boolean = false,
    deviceTested: Boolean,
    onDeviceTestedChange: (Boolean) -> Unit,
    sendWhatsappPdf: Boolean,
    onSendWhatsappPdfChange: (Boolean) -> Unit,
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
                    hasExistingSignature = hasCustomerSignature,
                    onCanvasSizeChanged = onCustomerSizeChanged
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Cihaz Test Edildi Onay Kutusu (Müşteri İmzasının Altında)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onDeviceTestedChange(!deviceTested) }
                .padding(vertical = 4.dp, horizontal = 4.dp)
        ) {
            Checkbox(
                checked = deviceTested,
                onCheckedChange = onDeviceTestedChange,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "Cihaz test edildi, sorunsuz çalışır teslim alındı",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Müşteri cihazın çalışır durumda olduğunu kontrol edip onayladı.",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    hasExistingSignature = hasTechnicianSignature,
                    onCanvasSizeChanged = onTechnicianSizeChanged
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // WhatsApp PDF Gönderim Seçeneği (Theme-Adaptive)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSendWhatsappPdfChange(!sendWhatsappPdf) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Checkbox(
                    checked = sendWhatsappPdf,
                    onCheckedChange = onSendWhatsappPdfChange,
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Servis fişini WhatsApp'tan PDF at",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "İş kapatıldığında müşteriye resmi A4 PDF onay bağlantısı iletilir.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
