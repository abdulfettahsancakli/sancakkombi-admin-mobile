package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteJobDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onComplete: (JobReport) -> Unit,
    modifier: Modifier = Modifier
) {
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
    var expandedPaymentStatus by remember { mutableStateOf(false) }
    var expandedPaymentMethod by remember { mutableStateOf(false) }

    val techList = listOf("Usta seç (opsiyonel)", "Ahmet Usta", "Mehmet Usta", "Ali Usta", "Caner Usta")
    val paymentStatusList = listOf("Ödendi", "Bekliyor")
    val paymentMethodList = listOf("Nakit", "Kredi Kartı", "EFT / Havale")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
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
                            text = "Randevuyu Tamamla",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = appointment.customerName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Kapanış Özeti Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "KAPANİŞ ÖZETİ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${appointment.date} | ${appointment.timeSlot} | ${appointment.serviceType}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Telefon: ${appointment.phone}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Adres: ${appointment.addressDetail.ifEmpty { "${appointment.district} - ${appointment.neighborhood}" }}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Bu işi yapan usta
                ExposedDropdownMenuBox(
                    expanded = expandedTech,
                    onExpandedChange = { expandedTech = !expandedTech },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = technicianName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Bu işi yapan usta") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTech) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTech,
                        onDismissRequest = { expandedTech = false }
                    ) {
                        techList.forEach { tech ->
                            DropdownMenuItem(
                                text = { Text(tech) },
                                onClick = {
                                    technicianName = tech
                                    expandedTech = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Checkboxes
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = notifyCustomerMessage,
                        onCheckedChange = { notifyCustomerMessage = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text(text = "Müşteriye tamamlandı mesajı gönder", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = sendWhatsappPdf,
                        onCheckedChange = { sendWhatsappPdf = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text(text = "Servis fişini WhatsApp'tan PDF olarak gönder", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Bu iş için gelir kaydı ekle Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = addRevenueRecord,
                                onCheckedChange = { addRevenueRecord = it },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "Bu iş için gelir kaydı ekle",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (addRevenueRecord) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = collectedAmount,
                                    onValueChange = { collectedAmount = it },
                                    label = { Text("Tahsil Edilen Tutar *") },
                                    placeholder = { Text("0.00") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                ExposedDropdownMenuBox(
                                    expanded = expandedPaymentStatus,
                                    onExpandedChange = { expandedPaymentStatus = !expandedPaymentStatus },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = paymentStatus,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Tahsilat Durumu") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPaymentStatus) },
                                        modifier = Modifier.menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedPaymentStatus,
                                        onDismissRequest = { expandedPaymentStatus = false }
                                    ) {
                                        paymentStatusList.forEach { st ->
                                            DropdownMenuItem(
                                                text = { Text(st) },
                                                onClick = {
                                                    paymentStatus = st
                                                    expandedPaymentStatus = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                ExposedDropdownMenuBox(
                                    expanded = expandedPaymentMethod,
                                    onExpandedChange = { expandedPaymentMethod = !expandedPaymentMethod },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = paymentMethod,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Ödeme Yöntemi") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPaymentMethod) },
                                        modifier = Modifier.menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedPaymentMethod,
                                        onDismissRequest = { expandedPaymentMethod = false }
                                    ) {
                                        paymentMethodList.forEach { m ->
                                            DropdownMenuItem(
                                                text = { Text(m) },
                                                onClick = {
                                                    paymentMethod = m
                                                    expandedPaymentMethod = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                OutlinedTextField(
                                    value = revenueNote,
                                    onValueChange = { revenueNote = it },
                                    label = { Text("Gelir Notu") },
                                    placeholder = { Text("Bakım ücreti...") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. İş Raporu Ekle Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = addJobReport,
                                onCheckedChange = { addJobReport = it },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "İş raporu ekle (yapılan iş, parçalar, garanti, gider)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        if (addJobReport) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = deviceBrand,
                                    onValueChange = { deviceBrand = it },
                                    label = { Text("Cihaz Markası") },
                                    placeholder = { Text("Demirdöküm, Baymak...") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                OutlinedTextField(
                                    value = deviceModel,
                                    onValueChange = { deviceModel = it },
                                    label = { Text("Cihaz Modeli") },
                                    placeholder = { Text("Örn: Thema F25") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = workDoneNote,
                                onValueChange = { workDoneNote = it },
                                label = { Text("Yapılan İş / Servis Notu") },
                                placeholder = { Text("Örn: Pilot ateşleyici değiştirildi...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = warrantyMonths,
                                onValueChange = { warrantyMonths = it },
                                label = { Text("Garanti Süresi (ay, boş bırakabilirsiniz)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Kullanılan Parçalar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            usedParts.forEachIndexed { idx, part ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${idx + 1}. ${part.name} - ₺${part.price}",
                                        fontSize = 12.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { usedParts.removeAt(idx) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Sil",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    usedParts.add(
                                        UsedPart(
                                            id = UUID.randomUUID().toString(),
                                            name = "Ateşleme Elektrodu / Filtre Seti",
                                            quantity = 1,
                                            price = 250.0
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Parça Ekle", fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = serviceFee,
                                    onValueChange = { serviceFee = it },
                                    label = { Text("Servis Ücreti (TL)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                OutlinedTextField(
                                    value = otherFee,
                                    onValueChange = { otherFee = it },
                                    label = { Text("Diğer Ücretler (TL)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = deviceTested,
                                    onCheckedChange = { deviceTested = it },
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                )
                                Text(text = "Cihaz test edildi, sistem normal çalışıyor", fontSize = 12.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = createExpenseRecord,
                                    onCheckedChange = { createExpenseRecord = it },
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                )
                                Text(text = "Bu iş için gider kaydı oluştur (parça alımı, usta gideri vb.)", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Servis fotoğrafları ekle Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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

                // 6. Dijital İmza Al Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Dijital imza al (müşteri + firma yetkilisi, opsiyonel)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SignaturePad(
                            title = "Müşteri İmzası",
                            lines = customerSignatureLines,
                            onCanvasSizeChanged = { customerSignatureCanvasSize = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SignaturePad(
                            title = "Firma Yetkilisi İmzası",
                            lines = technicianSignatureLines,
                            onCanvasSizeChanged = { technicianSignatureCanvasSize = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Vazgeç")
                    }

                    Spacer(modifier = Modifier.weight(1f))

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
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("submit_complete_job")
                    ) {
                        Text("Tamamla ve Kaydet", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
