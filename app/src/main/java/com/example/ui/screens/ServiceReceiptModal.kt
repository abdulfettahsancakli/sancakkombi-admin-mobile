package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus
import com.example.data.model.BankAccount
import com.example.data.model.Customer
import com.example.data.model.IstanbulLocationData
import com.example.data.model.JobReport
import com.example.data.model.StockItem
import com.example.data.model.UsedPart
import com.example.ui.components.Line
import com.example.ui.components.renderSignatureToBitmap
import com.example.ui.components.saveBitmapToCache
import com.example.ui.components.VoiceDictationButton
import com.example.utils.calculateReceiptPayment
import com.example.utils.parseLocalizedDouble
import java.text.SimpleDateFormat
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class DrawnStroke(
    val points: List<Offset>
)

private fun saveDrawnStrokesToCache(
    context: android.content.Context,
    strokes: List<DrawnStroke>,
    size: IntSize,
    prefix: String
): String? {
    if (strokes.isEmpty()) return null
    val lines = strokes.mapNotNull { s ->
        if (s.points.size < 2) null
        else {
            val p = Path().apply {
                moveTo(s.points.first().x, s.points.first().y)
                s.points.drop(1).forEach { pt -> lineTo(pt.x, pt.y) }
            }
            Line(path = p, strokeWidth = 5.5f)
        }
    }
    if (lines.isEmpty()) return null
    val bmp = renderSignatureToBitmap(
        lines = lines,
        size = IntSize(size.width.coerceAtLeast(300), size.height.coerceAtLeast(120))
    ) ?: return null
    return saveBitmapToCache(context, bmp, prefix)
}

private fun createServicePhotoUri(context: android.content.Context): Uri? = runCatching {
    val directory = File(context.cacheDir, "service-photos").apply { mkdirs() }
    val photo = File.createTempFile("service_${System.currentTimeMillis()}_", ".jpg", directory)
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photo)
}.getOrNull()

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceReceiptFlowContent(
    t: AppointmentTokens,
    todayIso: String,
    targetAppointment: Appointment? = null,
    customers: List<Customer> = emptyList(),
    stockItems: List<StockItem> = emptyList(),
    bankAccounts: List<BankAccount> = emptyList(),
    onShowToast: (String) -> Unit,
    onDismiss: () -> Unit,
    onOpenIbanDialog: ((Appointment) -> Unit)? = null,
    onSubmitReceipt: (Appointment, JobReport, Boolean, (Result<Unit>) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val existingReport = targetAppointment?.jobReport

    // Parse existing address if targetAppointment provided
    val parsedAddr = remember(targetAppointment) {
        val mah = targetAppointment?.neighborhood ?: ""
        val rest = targetAppointment?.streetDoorNo?.ifBlank { targetAppointment.addressDetail } ?: ""
        val noMatch = Regex("No:\\s*(.+)", RegexOption.IGNORE_CASE).find(rest)
        val noStr = noMatch?.groupValues?.getOrNull(1)?.trim() ?: ""
        val streetStr = if (noMatch != null) {
            rest.substring(0, noMatch.range.first).trim().removeSuffix(",")
        } else rest
        Triple(mah, streetStr, noStr)
    }

    // Step state (1..4)
    var step by remember(targetAppointment) { mutableIntStateOf(1) }

    // Step 1 state
    var customerId by remember(targetAppointment) { mutableStateOf(targetAppointment?.customerId ?: "") }
    var customerName by remember(targetAppointment) { mutableStateOf(targetAppointment?.customerName ?: "") }
    var customerPhone by remember(targetAppointment) { mutableStateOf(targetAppointment?.phone ?: "") }
    var district by remember(targetAppointment) {
        mutableStateOf(targetAppointment?.district?.ifBlank { "Bayrampaşa" } ?: "Bayrampaşa")
    }
    var neighborhood by remember(targetAppointment) { mutableStateOf(parsedAddr.first) }
    var street by remember(targetAppointment) { mutableStateOf(parsedAddr.second) }
    var buildingNo by remember(targetAppointment) { mutableStateOf(parsedAddr.third) }
    var dateIso by remember(targetAppointment, todayIso) {
        mutableStateOf(
            targetAppointment?.date?.let { normalizeToIsoDate(it) }?.ifBlank { todayIso } ?: todayIso
        )
    }
    val currentClockStr = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
    var timeOption by remember(targetAppointment) {
        mutableStateOf(
            targetAppointment?.timeSlot?.ifBlank { null } ?: "Şu an ($currentClockStr)"
        )
    }
    var complaint by remember(targetAppointment) {
        mutableStateOf(targetAppointment?.problemNote ?: "")
    }
    var showCustomerPicker by remember { mutableStateOf(false) }

    // Contact picker for Step 1
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val pIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val pName = if (nIdx >= 0) cursor.getString(nIdx) ?: "" else ""
                    val pNum = if (pIdx >= 0) cursor.getString(pIdx) ?: "" else ""
                    if (pName.isNotBlank()) customerName = pName
                    if (pNum.isNotBlank()) customerPhone = pNum
                    onShowToast("📇 Rehberden aktarıldı: $pName")
                }
            }
        } catch (_: Exception) {
        }
    }

    // Step 2 state
    var selectedBrand by remember(existingReport) {
        mutableStateOf(existingReport?.deviceBrand?.ifBlank { "DemirDöküm" } ?: "DemirDöküm")
    }
    var deviceModel by remember(existingReport) {
        mutableStateOf(existingReport?.deviceModel ?: "")
    }
    var workDone by remember(existingReport) {
        mutableStateOf(existingReport?.workDoneNote ?: "")
    }
    var partSourceFilter by remember { mutableStateOf("Tümü") }
    var partCategoryFilter by remember { mutableStateOf("Tümü") }
    var partSearchQuery by remember { mutableStateOf("") }
    val expandedPartCategories = remember {
        mutableStateMapOf<String, Boolean>().apply {
            put("🔥 Sensör & Ateşleme", true)
        }
    }

    // Build combined part options from real stockItems + prototype defaults
    val customParts = remember { mutableStateListOf<ReceiptPartOption>() }
    val allPartOptions = remember(stockItems, customParts.size) {
        val list = mutableListOf<ReceiptPartOption>()
        stockItems.forEach { st ->
            val srcLabel = normalizePartSupplier(st.location, st.brand)
            val catLabel = inferPartCategory(st.name, st.category)
            list.add(
                ReceiptPartOption(
                    id = "stock_${st.id}",
                    name = st.name,
                    price = if (st.salePrice > 0) st.salePrice else st.purchasePrice,
                    source = srcLabel,
                    category = catLabel,
                    stockId = st.id
                )
            )
        }
        DefaultReceiptParts.forEach { defPart ->
            if (list.none { it.name.equals(defPart.name, ignoreCase = true) }) {
                list.add(defPart)
            }
        }
        list.addAll(customParts)
        list
    }

    // Map of selected part ID -> quantity
    val selectedPartQtys = remember(existingReport) {
        mutableStateMapOf<String, Int>().apply {
            existingReport?.usedParts?.forEach { used ->
                val match = allPartOptions.find {
                    it.stockId == used.stockItemId || it.name.equals(used.name, ignoreCase = true)
                }
                if (match != null) {
                    put(match.id, used.quantity.coerceAtLeast(1))
                }
            }
        }
    }

    var showCustomPartForm by remember { mutableStateOf(false) }
    var customPartName by remember { mutableStateOf("") }
    var customPartPrice by remember { mutableStateOf("") }
    var customPartSupplier by remember { mutableStateOf("🏢 Dükkan Depo") }
    var customPartCategory by remember { mutableStateOf("🔧 Bakım & Diğer") }

    var laborStr by remember(existingReport) {
        mutableStateOf(existingReport?.serviceFee?.ifBlank { "800" } ?: "800")
    }
    var otherFeeStr by remember(existingReport) {
        mutableStateOf(existingReport?.otherFee?.ifBlank { "0" } ?: "0")
    }

    var warrantyKey by remember(existingReport) {
        val mInt = existingReport?.warrantyMonths?.toIntOrNull()
        val found = WarrantyOptions.find { it.months == mInt }?.key ?: "1y"
        mutableStateOf(found)
    }
    var deviceTested by remember(existingReport) {
        mutableStateOf(existingReport?.deviceTested ?: true)
    }
    val technicianOptions = remember {
        listOf("Abdullah Sancaklı", "Fatih Sancaklı")
    }
    var technician by remember(existingReport) {
        val existingTech = existingReport?.technicianName?.trim().orEmpty()
        mutableStateOf(
            if (existingTech in technicianOptions) existingTech else "Abdullah Sancaklı"
        )
    }
    var sendWhatsApp by remember(existingReport) {
        mutableStateOf(existingReport?.sendWhatsappPdf ?: true)
    }

    // Step 3 state
    var paymentMethod by remember(existingReport) {
        val m = existingReport?.paymentMethod?.lowercase(TrLocale) ?: "cash"
        val norm = when {
            m.contains("kart") || m == "card" -> "card"
            m.contains("havale") || m.contains("eft") || m == "transfer" -> "transfer"
            else -> "cash"
        }
        mutableStateOf(norm)
    }
    var payStatus by remember(existingReport) {
        val st = existingReport?.paymentStatus?.lowercase(TrLocale) ?: "paid"
        val norm = when {
            st.contains("partial") || st.contains("kısmi") -> "partial"
            st.contains("unpaid") || st.contains("veresiye") || st.contains("bekliyor") -> "unpaid"
            else -> "paid"
        }
        mutableStateOf(norm)
    }
    var partialCollectedAmountStr by remember(existingReport) {
        val status = existingReport?.paymentStatus?.lowercase(TrLocale).orEmpty()
        val isPartial = status.contains("partial") || status.contains("kısmi")
        mutableStateOf(if (isPartial) existingReport?.collectedAmount.orEmpty() else "")
    }
    var showPartialAmountError by remember(existingReport) { mutableStateOf(false) }
    var cashNote by remember(existingReport) {
        mutableStateOf(existingReport?.revenueNote ?: "")
    }
    var promisedDate by remember(existingReport) {
        mutableStateOf(existingReport?.paymentPromiseDate ?: "")
    }
    var hasExpense by remember(existingReport) {
        mutableStateOf(existingReport?.createExpenseRecord ?: false)
    }

    // Step 4 state: Signatures & Photos
    val customerSigStrokes = remember { mutableStateListOf<DrawnStroke>() }
    var customerSigSize by remember { mutableStateOf(IntSize.Zero) }
    val companySigStrokes = remember { mutableStateListOf<DrawnStroke>() }
    var companySigSize by remember { mutableStateOf(IntSize.Zero) }

    val photoUris = remember(existingReport) {
        mutableStateListOf<String>().apply {
            existingReport?.photoUris?.take(5)?.let { addAll(it) }
        }
    }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var previewPhotoUrl by remember { mutableStateOf<String?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            val str = uri.toString()
            if (photoUris.size < 5 && !photoUris.contains(str)) {
                photoUris.add(str)
            }
        }
        if (uris.isNotEmpty()) {
            onShowToast("📸 ${uris.size} fotoğraf eklendi")
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { saved ->
        val uri = pendingCameraUri
        if (saved && uri != null && photoUris.size < 5) {
            val value = uri.toString()
            if (!photoUris.contains(value)) photoUris.add(value)
            onShowToast("📸 Fotoğraf çekildi")
        } else if (!saved) {
            onShowToast("Kamera işlemi iptal edildi")
        }
        pendingCameraUri = null
    }

    // Live Totals
    val partsSum = allPartOptions.sumOf { part ->
        val qty = selectedPartQtys[part.id] ?: 0
        part.price * qty
    }
    val laborVal = parseLocalizedDouble(laborStr) ?: 0.0
    val otherVal = parseLocalizedDouble(otherFeeStr) ?: 0.0
    val grandTotal = partsSum + laborVal + otherVal
    val paymentBreakdown = calculateReceiptPayment(
        totalAmount = grandTotal,
        paymentStatus = payStatus,
        partialAmountText = partialCollectedAmountStr
    )

    var isSubmitting by remember { mutableStateOf(false) }

    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            containerColor = t.surface,
            title = {
                Text("Fotoğraf Ekle", fontWeight = FontWeight.ExtraBold, color = t.text)
            },
            text = {
                Text("Kamerayla yeni bir fotoğraf çekin veya galeriden en fazla 5 fotoğraf seçin.", color = t.text2)
            },
            confirmButton = {
                Surface(
                    onClick = {
                        showPhotoSourceDialog = false
                        val uri = createServicePhotoUri(context)
                        if (uri == null) {
                            onShowToast("⚠️ Kamera dosyası oluşturulamadı")
                        } else {
                            pendingCameraUri = uri
                            cameraLauncher.launch(uri)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = t.primary
                ) {
                    Text("📷 Kamera", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp))
                }
            },
            dismissButton = {
                Surface(
                    onClick = {
                        showPhotoSourceDialog = false
                        photoPickerLauncher.launch("image/*")
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = t.surface2,
                    border = BorderStroke(1.dp, t.border)
                ) {
                    Text("🖼️ Galeri", color = t.text, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(t.bg)
    ) {
        // Top Header + 4-Step Progress Bar
        Surface(
            color = t.surface,
            border = BorderStroke(1.dp, t.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Servis Fişi Kes",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = t.text
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = t.amberC
                            ) {
                                Text(
                                    text = if (targetAppointment == null) "YENİ İŞ" else "RANDEVU FİŞİ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = t.amber,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (targetAppointment == null) {
                                "Randevusuz acil / yerinde iş · fiş + gelir kaydı"
                            } else {
                                "${targetAppointment.customerName} · fiş + gelir kaydı"
                            },
                            fontSize = 11.sp,
                            color = t.text2,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4-Step Progress Bar
                val stepItems = listOf(
                    1 to "👤 Müşteri",
                    2 to "🛠️ İş & Cihaz",
                    3 to "💰 Tahsilat",
                    4 to "✍️ İmza"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    stepItems.forEach { (idx, label) ->
                        val isDoneOrCurrent = step >= idx
                        val isCurrent = step == idx
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { step = idx }
                                .padding(vertical = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isDoneOrCurrent) t.amber else t.surface2)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    isCurrent -> t.amber
                                    step > idx -> t.text
                                    else -> t.text2
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Step Content Body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (step) {
                // ============================================================
                // STEP 1: MÜŞTERİ & ZAMAN
                // ============================================================
                1 -> {
                    FormSectionCard(
                        t = t,
                        title = "👤 Müşteri & Adres",
                        rightHint = if (customers.isNotEmpty()) "Kayıtlı veya yeni" else null
                    ) {
                        if (customers.isNotEmpty()) {
                            Box(modifier = Modifier.padding(bottom = 8.dp)) {
                                Surface(
                                    onClick = { showCustomerPicker = !showCustomerPicker },
                                    shape = RoundedCornerShape(10.dp),
                                    color = t.primaryC,
                                    border = BorderStroke(1.dp, t.primary.copy(alpha = 0.4f)),
                                    modifier = Modifier.testTag("appointment_customer_picker")
                                ) {
                                    Text(
                                        text = "👥 Kayıtlı Müşteriden Doldur",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = t.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = showCustomerPicker,
                                    onDismissRequest = { showCustomerPicker = false },
                                    modifier = Modifier
                                        .background(t.surface)
                                        .heightIn(max = 260.dp)
                                ) {
                                    customers.take(15).forEach { cust ->
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
                                                customerId = cust.id
                                                customerName = cust.name
                                                customerPhone = cust.phone
                                                if (cust.district.isNotBlank()) district = cust.district
                                                if (cust.address.isNotBlank()) street = cust.address
                                                showCustomerPicker = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        FormFieldLabel(t = t, text = "Müşteri Adı Soyadı *")
                        FormTextInput(
                            t = t,
                            value = customerName,
                            onValueChange = { customerName = it },
                            placeholder = "Örn: Mehmet Demir"
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
                                value = customerPhone,
                                onValueChange = { customerPhone = it },
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
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = t.primaryC,
                                border = BorderStroke(1.dp, t.primary),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "📇 Rehber",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = t.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

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
                                    onSelect = { district = it }
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormFieldLabel(t = t, text = "Mahalle")
                                FormEditableDropdownField(
                                    t = t,
                                    value = neighborhood,
                                    onValueChange = { neighborhood = it },
                                    placeholder = "Mahalle adı",
                                    options = IstanbulLocationData.getNeighborhoods(district)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(2f)) {
                                val streetOptions = remember(district, neighborhood) {
                                    IstanbulLocationData.getStreets(context, district, neighborhood)
                                }
                                FormFieldLabel(t = t, text = "Cadde / Sokak")
                                FormEditableDropdownField(
                                    t = t,
                                    value = street,
                                    onValueChange = { street = it },
                                    placeholder = "Sokak / Cadde",
                                    options = streetOptions
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormFieldLabel(t = t, text = "Kapı / Daire")
                                FormTextInput(
                                    t = t,
                                    value = buildingNo,
                                    onValueChange = { buildingNo = it },
                                    placeholder = "12 / 4"
                                )
                            }
                        }
                    }

                    FormSectionCard(t = t, title = "🗓️ Hizmet & Zaman") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormFieldLabel(t = t, text = "İş Tarihi *")
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
                                        .height(48.dp)
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
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = t.text
                                        )
                                        Text(text = "📅", fontSize = 13.sp)
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                FormFieldLabel(t = t, text = "Saat *")
                                FormDropdownField(
                                    t = t,
                                    value = timeOption,
                                    options = listOf(
                                        "Şu an ($currentClockStr)",
                                        "09:00 - 10:00",
                                        "11:00 - 12:00",
                                        "14:00 - 15:00",
                                        "16:00 - 17:00",
                                        "19:00 - 20:00"
                                    ),
                                    onSelect = { timeOption = it }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        FormFieldLabel(t = t, text = "Müşteri Beyanı / Arıza Şikâyeti")
                        FormTextArea(
                            t = t,
                            value = complaint,
                            onValueChange = { complaint = it },
                            placeholder = "Örn: Sıcak su hiç gelmiyor, F28 hatası...",
                            minHeight = 68.dp
                        )
                        VoiceDictationButton(
                            currentText = complaint,
                            onTextChange = { complaint = it },
                            label = "Şikâyeti sesle yaz"
                        )
                    }
                }

                // ============================================================
                // STEP 2: İŞ & CİHAZ
                // ============================================================
                2 -> {
                    // Cihaz Markası & Modeli
                    FormSectionCard(t = t, title = "🔥 Cihaz Markası & Modeli") {
                        val brandRows = remember { BoilerBrands.chunked(3) }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            brandRows.forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    rowItems.forEach { brand ->
                                        val isSel = selectedBrand.equals(brand.name, ignoreCase = true)
                                        Surface(
                                            onClick = { selectedBrand = brand.name },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSel) brand.color else Color.White,
                                            border = BorderStroke(
                                                width = if (isSel) 2.dp else 1.dp,
                                                color = if (isSel) brand.color else t.border
                                            ),
                                            shadowElevation = if (isSel) 4.dp else 0.dp,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            ) {
                                                Text(
                                                    text = brand.name,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isSel) Color.White else brand.color,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                    repeat(3 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        FormFieldLabel(t = t, text = "Model (opsiyonel)")
                        FormTextInput(
                            t = t,
                            value = deviceModel,
                            onValueChange = { deviceModel = it },
                            placeholder = "Örn: Nitromix, Duo Tec, Proteus Premix..."
                        )
                    }

                    // Yapılan İş
                    FormSectionCard(t = t, title = "🛠️ Yapılan İş *") {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            JobTemplateChips.forEach { tpl ->
                                val isActive = workDone.lowercase(TrLocale).contains(tpl.text.lowercase(TrLocale))
                                Surface(
                                    onClick = {
                                        val cur = workDone.trim()
                                        workDone = if (cur.isEmpty()) tpl.text else "$cur. ${tpl.text}"
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isActive) t.primary else t.surface2,
                                    border = BorderStroke(1.dp, if (isActive) t.primary else t.border)
                                ) {
                                    Text(
                                        text = tpl.label,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) Color.White else t.text,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        FormTextArea(
                            t = t,
                            value = workDone,
                            onValueChange = { workDone = it },
                            placeholder = "Yapılan işlem detayı, değişen parça, ölçüm değerleri...",
                            minHeight = 76.dp
                        )
                        VoiceDictationButton(
                            currentText = workDone,
                            onTextChange = { workDone = it },
                            label = "Yapılan işi sesle yaz"
                        )
                    }

                    // Parçalar & Kalemler (Tedarikçi + Parça Kategorisi Boyutlu)
                    FormSectionCard(
                        t = t,
                        title = "🔩 Parçalar & Kalemler",
                        rightHint = if (selectedPartQtys.isNotEmpty()) {
                            "${selectedPartQtys.size} seçili · ₺${formatTLInt(partsSum)}"
                        } else {
                            "Stoktan otomatik düşer"
                        }
                    ) {
                        // Seçili Parçalar Özeti (Sayfayı kaydırmadan seçilenleri üstte görme & düzenleme)
                        if (selectedPartQtys.isNotEmpty()) {
                            val selectedPartsList = allPartOptions.filter { (selectedPartQtys[it.id] ?: 0) > 0 }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = t.greenC,
                                border = BorderStroke(1.dp, t.green.copy(alpha = 0.55f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "✅ Seçilen Parçalar (${selectedPartQtys.size} kalem · ₺${formatTLInt(partsSum)})",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = t.green
                                        )
                                        Surface(
                                            onClick = { selectedPartQtys.clear() },
                                            shape = RoundedCornerShape(6.dp),
                                            color = t.surface,
                                            border = BorderStroke(1.dp, t.border)
                                        ) {
                                            Text(
                                                text = "Temizle",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = t.text2,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    selectedPartsList.forEach { selPart ->
                                        val sQty = selectedPartQtys[selPart.id] ?: 1
                                        Surface(
                                            shape = RoundedCornerShape(9.dp),
                                            color = t.surface,
                                            border = BorderStroke(1.dp, t.border),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 9.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = selPart.name,
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = t.text,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "${selPart.source} · ${selPart.category}",
                                                        fontSize = 9.5.sp,
                                                        color = t.text2,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Surface(
                                                        onClick = {
                                                            if (sQty > 1) selectedPartQtys[selPart.id] = sQty - 1
                                                            else selectedPartQtys.remove(selPart.id)
                                                        },
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = t.surface2,
                                                        border = BorderStroke(1.dp, t.border),
                                                        modifier = Modifier.size(22.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text("-", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = t.text)
                                                        }
                                                    }
                                                    Text(
                                                        text = "${sQty}x",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = t.primary
                                                    )
                                                    Surface(
                                                        onClick = { selectedPartQtys[selPart.id] = sQty + 1 },
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = t.surface2,
                                                        border = BorderStroke(1.dp, t.border),
                                                        modifier = Modifier.size(22.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text("+", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = t.text)
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = "₺${formatTLInt(selPart.price * sQty)}",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = t.green
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // 1. BOYUT: TEDARİKÇİ / KAYNAK KATEGORİSİ
                        Text(
                            text = "📦 1. Boyut: Tedarikçi / Depo Seçimi",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = t.text2
                        )
                        Spacer(modifier = Modifier.height(5.dp))

                        val dynamicSuppliers = remember(allPartOptions) {
                            val extra = allPartOptions.map { it.source }.distinct().filter { it !in PartSupplierOptions }
                            PartSupplierOptions + extra
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            dynamicSuppliers.forEach { src ->
                                val isSel = partSourceFilter == src
                                val countInSrc = if (src == "Tümü") {
                                    allPartOptions.size
                                } else {
                                    allPartOptions.count { it.source == src }
                                }
                                val selectedInSrc = allPartOptions.count {
                                    (src == "Tümü" || it.source == src) && (selectedPartQtys[it.id] ?: 0) > 0
                                }
                                Surface(
                                    onClick = { partSourceFilter = src },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSel) t.primary else t.surface2,
                                    border = BorderStroke(1.dp, if (isSel) t.primary else t.border)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(
                                            text = "$src ($countInSrc)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else t.text
                                        )
                                        if (selectedInSrc > 0 && src != "Tümü") {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isSel) Color.White else t.green
                                            ) {
                                                Text(
                                                    text = "$selectedInSrc",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isSel) t.primary else Color.White,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. BOYUT: PARÇA KATEGORİSİ
                        Text(
                            text = "🧩 2. Boyut: Parça Kategorisi",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = t.text2
                        )
                        Spacer(modifier = Modifier.height(5.dp))

                        val supplierFilteredParts = remember(allPartOptions, partSourceFilter) {
                            if (partSourceFilter == "Tümü") allPartOptions
                            else allPartOptions.filter { it.source == partSourceFilter }
                        }

                        val dynamicCategories = remember(allPartOptions) {
                            val extra = allPartOptions.map { it.category }.distinct().filter { it !in PartCategoryOptions }
                            PartCategoryOptions + extra
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            dynamicCategories.forEach { cat ->
                                val isSel = partCategoryFilter == cat
                                val countInCat = if (cat == "Tümü") {
                                    supplierFilteredParts.size
                                } else {
                                    supplierFilteredParts.count { it.category == cat }
                                }
                                val selectedInCat = supplierFilteredParts.count {
                                    (cat == "Tümü" || it.category == cat) && (selectedPartQtys[it.id] ?: 0) > 0
                                }
                                if (cat == "Tümü" || countInCat > 0) {
                                    Surface(
                                        onClick = { partCategoryFilter = cat },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSel) t.amber else t.surface2,
                                        border = BorderStroke(1.dp, if (isSel) t.amber else t.border)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Text(
                                                text = "$cat ($countInCat)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.White else t.text
                                            )
                                            if (selectedInCat > 0 && cat != "Tümü") {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (isSel) Color.White else t.green
                                                ) {
                                                    Text(
                                                        text = "$selectedInCat",
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = if (isSel) t.amber else Color.White,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Hızlı Parça Arama Çubuğu
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FormTextInput(
                                t = t,
                                value = partSearchQuery,
                                onValueChange = { partSearchQuery = it },
                                placeholder = "🔍 Parça adı veya tedarikçi ara...",
                                height = 40.dp,
                                modifier = Modifier.weight(1f)
                            )
                            if (partSearchQuery.isNotBlank() || partSourceFilter != "Tümü" || partCategoryFilter != "Tümü") {
                                Surface(
                                    onClick = {
                                        partSearchQuery = ""
                                        partSourceFilter = "Tümü"
                                        partCategoryFilter = "Tümü"
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = t.surface2,
                                    border = BorderStroke(1.dp, t.border),
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Sıfırla",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = t.text2
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val visibleParts = remember(supplierFilteredParts, partCategoryFilter, partSearchQuery) {
                            val q = partSearchQuery.trim().lowercase(TrLocale)
                            supplierFilteredParts.filter { p ->
                                (partCategoryFilter == "Tümü" || p.category == partCategoryFilter) &&
                                    (q.isEmpty() ||
                                        p.name.lowercase(TrLocale).contains(q) ||
                                        p.source.lowercase(TrLocale).contains(q) ||
                                        p.category.lowercase(TrLocale).contains(q))
                            }
                        }

                        // Sınırlı Yükseklikli & Kategori Akordiyonlu Parça Alanı (Upuzun kaydırmayı önler)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = t.bg.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, t.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (visibleParts.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 18.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Bu tedarikçi / kategori filtresinde parça bulunamadı.",
                                            fontSize = 11.5.sp,
                                            color = t.text2
                                        )
                                    }
                                } else if (partCategoryFilter == "Tümü" && partSearchQuery.isBlank()) {
                                    // Kategori başlıklarına göre açılır/kapanır (Accordion) görünüm
                                    val groupedByCat = visibleParts.groupBy { it.category }
                                    groupedByCat.entries.forEach { (catName, catParts) ->
                                        val selectedCountInGroup = catParts.count { (selectedPartQtys[it.id] ?: 0) > 0 }
                                        val isExpanded = expandedPartCategories[catName] ?: (selectedCountInGroup > 0)

                                        Surface(
                                            onClick = { expandedPartCategories[catName] = !isExpanded },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isExpanded) t.surface else t.surface2,
                                            border = BorderStroke(
                                                1.dp,
                                                if (selectedCountInGroup > 0) t.green else t.border
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = catName,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = t.text
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = t.surface2
                                                    ) {
                                                        Text(
                                                            text = "${catParts.size} kalem",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = t.text2,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    if (selectedCountInGroup > 0) {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = t.green
                                                        ) {
                                                            Text(
                                                                text = "✓ $selectedCountInGroup seçili",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = Color.White,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = if (isExpanded) "▲" else "▼",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = t.text2
                                                )
                                            }
                                        }

                                        if (isExpanded) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 6.dp, top = 2.dp, bottom = 4.dp),
                                                verticalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                catParts.forEach { part ->
                                                    ReceiptPartRowItem(
                                                        t = t,
                                                        part = part,
                                                        qty = selectedPartQtys[part.id] ?: 0,
                                                        onToggle = {
                                                            val cur = selectedPartQtys[part.id] ?: 0
                                                            if (cur > 0) selectedPartQtys.remove(part.id)
                                                            else selectedPartQtys[part.id] = 1
                                                        },
                                                        onDecrement = {
                                                            val cur = selectedPartQtys[part.id] ?: 0
                                                            if (cur > 1) selectedPartQtys[part.id] = cur - 1
                                                            else selectedPartQtys.remove(part.id)
                                                        },
                                                        onIncrement = {
                                                            val cur = selectedPartQtys[part.id] ?: 0
                                                            selectedPartQtys[part.id] = cur + 1
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Seçili kategori veya arama sonuçları doğrudan listelenir
                                    visibleParts.forEach { part ->
                                        ReceiptPartRowItem(
                                            t = t,
                                            part = part,
                                            qty = selectedPartQtys[part.id] ?: 0,
                                            onToggle = {
                                                val cur = selectedPartQtys[part.id] ?: 0
                                                if (cur > 0) selectedPartQtys.remove(part.id)
                                                else selectedPartQtys[part.id] = 1
                                            },
                                            onDecrement = {
                                                val cur = selectedPartQtys[part.id] ?: 0
                                                if (cur > 1) selectedPartQtys[part.id] = cur - 1
                                                else selectedPartQtys.remove(part.id)
                                            },
                                            onIncrement = {
                                                val cur = selectedPartQtys[part.id] ?: 0
                                                selectedPartQtys[part.id] = cur + 1
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (showCustomPartForm) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = t.surface2,
                                border = BorderStroke(1.dp, t.primary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            FormFieldLabel(t = t, text = "Tedarikçi")
                                            FormDropdownField(
                                                t = t,
                                                value = customPartSupplier,
                                                options = PartSupplierOptions.filter { it != "Tümü" },
                                                onSelect = { customPartSupplier = it }
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            FormFieldLabel(t = t, text = "Parça Kategorisi")
                                            FormDropdownField(
                                                t = t,
                                                value = customPartCategory,
                                                options = PartCategoryOptions.filter { it != "Tümü" },
                                                onSelect = { customPartCategory = it }
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        FormTextInput(
                                            t = t,
                                            value = customPartName,
                                            onValueChange = { customPartName = it },
                                            placeholder = "Parça adı",
                                            height = 42.dp,
                                            modifier = Modifier.weight(1.5f)
                                        )
                                        FormTextInput(
                                            t = t,
                                            value = customPartPrice,
                                            onValueChange = { customPartPrice = it },
                                            placeholder = "Fiyat ₺",
                                            keyboardType = KeyboardType.Number,
                                            isMono = true,
                                            height = 42.dp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            onClick = {
                                                val pName = customPartName.trim()
                                                val pPrice = parseLocalizedDouble(customPartPrice) ?: 0.0
                                                if (pName.isNotBlank()) {
                                                    val newId = "custom_${System.currentTimeMillis()}"
                                                    val chosenSup = if (partSourceFilter != "Tümü") partSourceFilter else customPartSupplier
                                                    val chosenCat = if (partCategoryFilter != "Tümü") partCategoryFilter else customPartCategory
                                                    customParts.add(
                                                        ReceiptPartOption(
                                                            id = newId,
                                                            name = pName,
                                                            price = pPrice,
                                                            source = chosenSup,
                                                            category = chosenCat
                                                        )
                                                    )
                                                    selectedPartQtys[newId] = 1
                                                    expandedPartCategories[chosenCat] = true
                                                    customPartName = ""
                                                    customPartPrice = ""
                                                    showCustomPartForm = false
                                                    onShowToast("🔩 Özel parça eklendi: $pName")
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            color = t.primary,
                                            modifier = Modifier.height(42.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(horizontal = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Ekle",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .dashedBorder(1.dp, t.border, 10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (partSourceFilter != "Tümü") customPartSupplier = partSourceFilter
                                        if (partCategoryFilter != "Tümü") customPartCategory = partCategoryFilter
                                        showCustomPartForm = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "＋ Özel parça satırı ekle",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = t.text2
                                )
                            }
                        }
                    }

                    // Garanti & Test
                    FormSectionCard(t = t, title = "🛡️ Garanti & Test") {
                        FormFieldLabel(t = t, text = "Garanti Süresi")
                        val wRows = remember { WarrantyOptions.chunked(3) }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            wRows.forEach { r ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    r.forEach { w ->
                                        val isSel = warrantyKey == w.key
                                        Surface(
                                            onClick = { warrantyKey = w.key },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSel) t.green else t.surface2,
                                            border = BorderStroke(
                                                width = if (isSel) 2.dp else 1.dp,
                                                color = if (isSel) t.green else t.border
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = w.label,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isSel) Color.White else t.text
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            onClick = { deviceTested = !deviceTested },
                            shape = RoundedCornerShape(12.dp),
                            color = t.surface2,
                            border = BorderStroke(1.dp, t.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (deviceTested) t.green else Color.Transparent,
                                    border = BorderStroke(2.dp, if (deviceTested) t.green else t.border),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (deviceTested) {
                                            Text("✓", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        }
                                    }
                                }
                                Text(
                                    text = "Cihaz test edildi, çalışır durumda teslim edildi",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = t.text
                                )
                            }
                        }
                    }

                    // İşi Yapan Usta (Abdullah Sancaklı veya Fatih Sancaklı)
                    FormSectionCard(t = t, title = "👷 İşi Yapan Usta") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            technicianOptions.forEach { ustaName ->
                                val isSel = technician == ustaName
                                Surface(
                                    onClick = { technician = ustaName },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSel) t.primaryC else t.surface2,
                                    border = BorderStroke(
                                        width = if (isSel) 2.dp else 1.dp,
                                        color = if (isSel) t.primary else t.border
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSel) t.primary else t.surface,
                                            border = BorderStroke(1.dp, if (isSel) t.primary else t.border),
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = if (isSel) "✓" else "👷",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isSel) Color.White else t.text2
                                                )
                                            }
                                        }
                                        Text(
                                            text = ustaName,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isSel) t.primary else t.text,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ============================================================
                // STEP 3: TAHSİLAT (İşçilik & Diğer ₺ buraya aktarıldı)
                // ============================================================
                3 -> {
                    // Live Collection Breakdown Card (Blue Gradient)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(t.primaryGradient)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "CANLI TAHSİLAT DÖKÜMÜ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White.copy(alpha = 0.85f),
                                letterSpacing = 0.6.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₺${formatTL(grandTotal)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.18f))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Parça (${selectedPartQtys.size})", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                    Text(
                                        text = "₺${formatTLInt(partsSum)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("İşçilik", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                    Text(
                                        text = "₺${formatTLInt(laborVal)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Diğer", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                    Text(
                                        text = "₺${formatTLInt(otherVal)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🔒 Parça, işçilik ve diğer ücretlerden otomatik hesaplanır",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    // İşçilik ve Diğer ₺ Ücret Kalemleri (Tahsilat bölümüne aktarıldı)
                    FormSectionCard(
                        t = t,
                        title = "🛠️ İşçilik & Diğer Ücretler (₺)",
                        rightHint = "Toplama anında yansır"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormFieldLabel(t = t, text = "İşçilik / Servis (₺)")
                                FormTextInput(
                                    t = t,
                                    value = laborStr,
                                    onValueChange = { laborStr = it },
                                    placeholder = "800",
                                    keyboardType = KeyboardType.Number,
                                    isMono = true
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormFieldLabel(t = t, text = "Diğer (₺)")
                                FormTextInput(
                                    t = t,
                                    value = otherFeeStr,
                                    onValueChange = { otherFeeStr = it },
                                    placeholder = "0",
                                    keyboardType = KeyboardType.Number,
                                    isMono = true
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Hızlı İşçilik Seçimi",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = t.text2
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        val laborPresets = listOf("0", "500", "800", "1000", "1200", "1500", "2000")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            laborPresets.forEach { preset ->
                                val isSel = laborStr.trim() == preset
                                Surface(
                                    onClick = { laborStr = preset },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) t.primary else t.surface2,
                                    border = BorderStroke(1.dp, if (isSel) t.primary else t.border)
                                ) {
                                    Text(
                                        text = "₺$preset",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isSel) Color.White else t.text,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Ödeme Yöntemi
                    FormSectionCard(t = t, title = "💳 Ödeme Yöntemi") {
                        val methods = listOf(
                            Triple("cash", "💵", "Nakit"),
                            Triple("card", "💳", "Kart / POS"),
                            Triple("transfer", "🏦", "Havale")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            methods.forEach { (key, icon, label) ->
                                val isSel = paymentMethod == key
                                Surface(
                                    onClick = { paymentMethod = key },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSel) t.primaryC else t.surface2,
                                    border = BorderStroke(
                                        width = if (isSel) 2.dp else 1.dp,
                                        color = if (isSel) t.primary else t.border
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(66.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(text = icon, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = label,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isSel) t.primary else t.text
                                        )
                                    }
                                }
                            }
                        }

                        if (paymentMethod == "transfer" && targetAppointment != null && bankAccounts.isNotEmpty() && onOpenIbanDialog != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                onClick = { onOpenIbanDialog(targetAppointment) },
                                shape = RoundedCornerShape(12.dp),
                                color = t.primaryC,
                                border = BorderStroke(1.dp, t.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("open_iban_dialog_from_complete_job")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "📲 Müşteriye IBAN Gönder",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = t.primary
                                    )
                                }
                            }
                        }
                    }

                    // Tahsilat Durumu
                    FormSectionCard(t = t, title = "💵 Tahsilat Durumu") {
                        val statusRows = listOf(
                            Triple("paid", "✅ Tahsil Edildi (Ödendi)", t.green),
                            Triple("partial", "⏳ Kısmi Tahsilat", t.amber),
                            Triple("unpaid", "❌ Ödeme Alınmadı (Veresiye)", t.red)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            statusRows.forEach { (key, label, accent) ->
                                val isSel = payStatus == key
                                Surface(
                                    onClick = {
                                        payStatus = key
                                        showPartialAmountError = false
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSel) t.surface else t.surface2,
                                    border = BorderStroke(
                                        width = if (isSel) 2.dp else 1.dp,
                                        color = if (isSel) accent else t.border
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = t.text
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(if (isSel) accent else Color.Transparent)
                                                .let {
                                                    if (!isSel) {
                                                        it.dashedBorder(2.dp, t.border, 10.dp, 20.dp, 0.dp)
                                                    } else it
                                                }
                                        )
                                    }
                                }
                            }
                        }

                        if (payStatus == "partial") {
                            Spacer(modifier = Modifier.height(12.dp))
                            FormFieldLabel(t = t, text = "Tahsil Edilen Tutar (₺) *")
                            FormTextInput(
                                t = t,
                                value = partialCollectedAmountStr,
                                onValueChange = { partialCollectedAmountStr = it },
                                placeholder = "Örn: 3.000",
                                keyboardType = KeyboardType.Decimal,
                                isMono = true,
                                testTag = "partial_collection_amount_input",
                                isError = showPartialAmountError && !paymentBreakdown.isValid
                            )
                            Text(
                                text = if (showPartialAmountError && !paymentBreakdown.isValid) {
                                    "⚠️ ${paymentBreakdown.errorMessage}"
                                } else {
                                    "Bu fişte şimdi aldığınız gerçek tutarı girin."
                                },
                                fontSize = 11.sp,
                                fontWeight = if (showPartialAmountError && !paymentBreakdown.isValid) FontWeight.Bold else FontWeight.Medium,
                                color = if (showPartialAmountError && !paymentBreakdown.isValid) t.red else t.text2,
                                modifier = Modifier.padding(top = 5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = t.surface2,
                            border = BorderStroke(1.dp, t.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Triple("İş Toplamı", grandTotal, t.text),
                                    Triple("Tahsil Edilen", paymentBreakdown.collectedAmount, t.green),
                                    Triple("Kalan Alacak", paymentBreakdown.remainingAmount, if (paymentBreakdown.remainingAmount > 0.0) t.red else t.green)
                                ).forEach { (label, amount, color) ->
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = label,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = t.text2,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "₺${formatTL(amount)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = color,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        if (payStatus != "paid") {
                            Spacer(modifier = Modifier.height(10.dp))
                            FormFieldLabel(t = t, text = "Ödeme Sözü Tarihi (opsiyonel)")
                            FormTextInput(
                                t = t,
                                value = promisedDate,
                                onValueChange = { promisedDate = it },
                                placeholder = "GG.AA.YYYY"
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        FormFieldLabel(t = t, text = "Kasa Notu (opsiyonel)")
                        FormTextInput(
                            t = t,
                            value = cashNote,
                            onValueChange = { cashNote = it },
                            placeholder = "Örn: 500 ₺ kapora alındı, kalanı cuma havale..."
                        )
                    }

                    // Expense Toggle Card
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = t.surface,
                        border = BorderStroke(1.dp, t.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { hasExpense = !hasExpense },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (hasExpense) t.amber else Color.Transparent,
                                    border = BorderStroke(2.dp, if (hasExpense) t.amber else t.border),
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (hasExpense) {
                                            Text("✓", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        }
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Bu iş için gider kaydı ekle",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = t.text
                                    )
                                    Text(
                                        text = "Parça alımı, yakıt, taşeron ödemesi vb.",
                                        fontSize = 11.sp,
                                        color = t.text2
                                    )
                                }
                            }
                        }
                    }
                }

                // ============================================================
                // STEP 4: İMZA & FOTO (+ WhatsApp Gönderimi Servis Fotoğraflarının Altında)
                // ============================================================
                4 -> {
                    FormSectionCard(t = t, title = "✍️ Dijital İmza") {
                        // Müşteri İmzası
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Müşteri İmzası",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = t.text2
                            )
                            Surface(
                                onClick = { customerSigStrokes.clear() },
                                shape = RoundedCornerShape(8.dp),
                                color = t.surface2,
                                border = BorderStroke(1.dp, t.border)
                            ) {
                                Text(
                                    text = "🧽 Temizle",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = t.text2,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        InteractiveSignaturePadBox(
                            t = t,
                            strokes = customerSigStrokes,
                            onSizeChanged = { customerSigSize = it },
                            height = 140.dp,
                            testTag = "signature_pad_canvas"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Firma Yetkilisi İmzası
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Firma Yetkilisi İmzası ($technician)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = t.text2
                            )
                            Surface(
                                onClick = { companySigStrokes.clear() },
                                shape = RoundedCornerShape(8.dp),
                                color = t.surface2,
                                border = BorderStroke(1.dp, t.border)
                            ) {
                                Text(
                                    text = "🧽 Temizle",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = t.text2,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        InteractiveSignaturePadBox(
                            t = t,
                            strokes = companySigStrokes,
                            onSizeChanged = { companySigSize = it },
                            height = 110.dp
                        )
                    }

                    // Servis Fotoğrafları
                    FormSectionCard(
                        t = t,
                        title = "📸 Servis Fotoğrafları",
                        rightHint = "${photoUris.size} / 5"
                    ) {
                        Text(
                            text = "Cihaz etiketi, eski parça veya yapılan işin fotoğrafı",
                            fontSize = 11.5.sp,
                            color = t.text2
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val totalTiles = (photoUris.size + if (photoUris.size < 5) 1 else 0).coerceAtLeast(1)
                        val rows = (0 until totalTiles).chunked(3)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            rows.forEach { rowIndices ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowIndices.forEach { idx ->
                                        if (idx < photoUris.size) {
                                            val uriStr = photoUris[idx]
                                            Surface(
                                                onClick = { previewPhotoUrl = uriStr },
                                                shape = RoundedCornerShape(12.dp),
                                                color = t.surface2,
                                                border = BorderStroke(1.dp, t.border),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                            ) {
                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    AsyncImage(
                                                        model = uriStr,
                                                        contentDescription = "Fotoğraf #${idx + 1}",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomStart)
                                                            .padding(4.dp)
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.Black.copy(alpha = 0.6f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("🔍", fontSize = 10.sp)
                                                    }
                                                    Surface(
                                                        onClick = { photoUris.removeAt(idx) },
                                                        shape = CircleShape,
                                                        color = Color.Black.copy(alpha = 0.65f),
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(4.dp)
                                                            .size(24.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = "✕",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .dashedBorder(1.5.dp, t.primary, 12.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(t.primaryC)
                                                    .clickable { showPhotoSourceDialog = true }
                                                    .testTag("pick_photos_button"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(text = "📷", fontSize = 22.sp)
                                                    Spacer(modifier = Modifier.height(3.dp))
                                                    Text(
                                                        text = "Çek / Yükle",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = t.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    repeat(3 - rowIndices.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    // Fişi WhatsApp ile Gönder (Servis Fotoğraflarının Altında)
                    Surface(
                        onClick = { sendWhatsApp = !sendWhatsApp },
                        shape = RoundedCornerShape(18.dp),
                        color = if (sendWhatsApp) t.greenC else t.surface,
                        border = BorderStroke(
                            width = if (sendWhatsApp) 1.5.dp else 1.dp,
                            color = if (sendWhatsApp) t.green else t.border
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (sendWhatsApp) t.green else t.surface2,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "📲",
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Fişi WhatsApp ile gönder",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = t.text
                                    )
                                    Text(
                                        text = "Fişi kestiğinizde müşteriye PDF servis fişi gider",
                                        fontSize = 11.sp,
                                        color = t.text2
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (sendWhatsApp) t.green else t.border)
                                    .padding(2.dp),
                                contentAlignment = if (sendWhatsApp) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sticky Bottom Bar (All Steps)
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GENEL TOPLAM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = t.text2
                    )
                    Text(
                        text = "₺${formatTL(grandTotal)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = t.green
                    )
                }

                if (step > 1) {
                    Surface(
                        onClick = { step -= 1 },
                        shape = RoundedCornerShape(12.dp),
                        color = t.surface2,
                        border = BorderStroke(1.dp, t.border),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "←",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = t.text
                            )
                        }
                    }
                }

                if (step < 4) {
                    val nextLabel = when (step) {
                        1 -> "İş & Cihaz →"
                        2 -> "Tahsilata İlerle →"
                        else -> "İmza & Foto →"
                    }
                    Surface(
                        onClick = {
                            if (step == 1) {
                                if (customerName.isBlank() || customerPhone.isBlank()) {
                                    onShowToast("👀 Müşteri adı ve telefon zorunlu")
                                    return@Surface
                                }
                                if (district.isBlank()) {
                                    onShowToast("📍 İlçe bilgisi zorunlu")
                                    return@Surface
                                }
                            }
                            if (step == 2 && workDone.isBlank()) {
                                onShowToast("🛠️ Yapılan iş açıklaması zorunlu")
                                return@Surface
                            }
                            if (step == 3 && !paymentBreakdown.isValid) {
                                showPartialAmountError = true
                                onShowToast(paymentBreakdown.errorMessage ?: "Tahsilat tutarını kontrol edin")
                                return@Surface
                            }
                            step += 1
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = t.primary,
                        modifier = Modifier
                            .height(48.dp)
                            .let {
                                if (step == 1) it.testTag("continue_service_receipt") else it
                            }
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = nextLabel,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Surface(
                        onClick = {
                            if (isSubmitting) return@Surface
                            if (customerName.isBlank() || customerPhone.isBlank()) {
                                onShowToast("👀 Müşteri adı ve telefon zorunlu")
                                step = 1
                                return@Surface
                            }
                            if (district.isBlank()) {
                                onShowToast("📍 İlçe bilgisi zorunlu")
                                step = 1
                                return@Surface
                            }
                            if (workDone.isBlank()) {
                                onShowToast("🛠️ Yapılan iş açıklaması zorunlu")
                                step = 2
                                return@Surface
                            }
                            if (!paymentBreakdown.isValid) {
                                showPartialAmountError = true
                                onShowToast(paymentBreakdown.errorMessage ?: "Tahsilat tutarını kontrol edin")
                                step = 3
                                return@Surface
                            }
                            isSubmitting = true

                            val cleanMah = if (neighborhood.isNotBlank()) {
                                val m = neighborhood.trim()
                                if (m.endsWith("Mah.", ignoreCase = true) || m.endsWith("Mahallesi", ignoreCase = true)) m else "$m Mah."
                            } else ""
                            val streetAndDoor = buildList {
                                if (street.isNotBlank()) add(street.trim())
                                if (buildingNo.isNotBlank()) add("No: ${buildingNo.trim()}")
                            }.joinToString(", ")
                            val fullAddress = listOf(cleanMah, streetAndDoor)
                                .filter { it.isNotBlank() }
                                .joinToString(", ")
                                .ifBlank { targetAppointment?.fullAddressDisplay ?: "Adres belirtilmedi" }

                            val usedPartsList = allPartOptions.mapNotNull { part ->
                                val qty = selectedPartQtys[part.id] ?: 0
                                if (qty > 0) {
                                    UsedPart(
                                        id = part.id,
                                        name = part.name,
                                        quantity = qty,
                                        price = part.price,
                                        stockItemId = part.stockId
                                    )
                                } else null
                            }

                            val warrantyMonthsStr = WarrantyOptions.find { it.key == warrantyKey }?.months?.toString() ?: "0"
                            val custSigPath = saveDrawnStrokesToCache(
                                context = context,
                                strokes = customerSigStrokes,
                                size = customerSigSize,
                                prefix = "customer_sig"
                            ) ?: existingReport?.customerSignaturePath

                            val compSigPath = saveDrawnStrokesToCache(
                                context = context,
                                strokes = companySigStrokes,
                                size = companySigSize,
                                prefix = "technician_sig"
                            ) ?: existingReport?.technicianSignaturePath

                            val paymentStatusTr = when (payStatus) {
                                "partial" -> "Kısmi"
                                "unpaid" -> "Bekliyor"
                                else -> "Ödendi"
                            }
                            val paymentMethodTr = when (paymentMethod) {
                                "card" -> "Kredi Kartı"
                                "transfer" -> "EFT/Havale"
                                else -> "Nakit"
                            }

                            val jobReport = JobReport(
                                technicianName = technician,
                                notifyCustomerMessage = true,
                                sendWhatsappPdf = sendWhatsApp,
                                addRevenueRecord = true,
                                collectedAmount = paymentBreakdown.collectedAmount.toString(),
                                paymentStatus = paymentStatusTr,
                                paymentMethod = paymentMethodTr,
                                paymentPromiseDate = promisedDate.trim(),
                                revenueNote = cashNote.trim(),
                                addJobReport = true,
                                deviceBrand = selectedBrand,
                                deviceModel = deviceModel.trim(),
                                workDoneNote = workDone.trim(),
                                warrantyMonths = warrantyMonthsStr,
                                usedParts = usedPartsList,
                                serviceFee = laborVal.toString(),
                                otherFee = otherVal.toString(),
                                deviceTested = deviceTested,
                                createExpenseRecord = hasExpense,
                                photoUris = photoUris.toList(),
                                customerSignaturePath = custSigPath,
                                technicianSignaturePath = compSigPath
                            )

                            val normSlot = if (timeOption.startsWith("Şu an")) {
                                val h = currentClockStr.take(2).toIntOrNull() ?: 14
                                "%02d:00 - %02d:00".format(h, (h + 1) % 24)
                            } else {
                                timeOption
                            }

                            val appointmentObj = Appointment(
                                id = targetAppointment?.id ?: UUID.randomUUID().toString(),
                                customerId = customerId,
                                customerName = customerName.trim(),
                                phone = customerPhone.trim(),
                                email = targetAppointment?.email ?: "",
                                district = district.trim().ifBlank { "Bayrampaşa" },
                                neighborhood = cleanMah,
                                streetDoorNo = streetAndDoor,
                                addressDetail = fullAddress,
                                serviceType = targetAppointment?.serviceType ?: "Kombi Bakım & Servis",
                                date = isoToDisplayDate(dateIso),
                                timeSlot = normSlot,
                                status = AppointmentStatus.TAMAMLANDI,
                                source = targetAppointment?.source ?: "Servis Fişi",
                                problemNote = complaint.trim().ifBlank { "Yerinde servis" },
                                jobReport = jobReport
                            )

                            onSubmitReceipt(appointmentObj, jobReport, sendWhatsApp) {
                                isSubmitting = false
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = t.green,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("submit_complete_job")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓ Fişi Kes",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        if (previewPhotoUrl != null) {
            PhotoViewerDialog(
                photoUrl = previewPhotoUrl!!,
                onDismiss = { previewPhotoUrl = null }
            )
        }
    }
}

@Composable
private fun ReceiptPartRowItem(
    t: AppointmentTokens,
    part: ReceiptPartOption,
    qty: Int,
    onToggle: () -> Unit,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    val isSel = qty > 0
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(10.dp),
        color = if (isSel) t.primaryC else t.surface,
        border = BorderStroke(1.dp, if (isSel) t.primary else t.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSel) t.primary else Color.Transparent,
                border = BorderStroke(2.dp, if (isSel) t.primary else t.border),
                modifier = Modifier.size(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isSel) {
                        Text(
                            text = "✓",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = part.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = t.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${part.source} · ${part.category}",
                    fontSize = 10.sp,
                    color = t.text2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isSel) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        onClick = onDecrement,
                        shape = RoundedCornerShape(6.dp),
                        color = t.surface,
                        border = BorderStroke(1.dp, t.border),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("-", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = t.text)
                        }
                    }
                    Text(
                        text = "${qty}x",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = t.primary
                    )
                    Surface(
                        onClick = onIncrement,
                        shape = RoundedCornerShape(6.dp),
                        color = t.surface,
                        border = BorderStroke(1.dp, t.border),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("+", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = t.text)
                        }
                    }
                }
            }

            Text(
                text = "₺${formatTLInt(part.price * (if (qty > 0) qty else 1))}",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = t.green
            )
        }
    }
}

@Composable
private fun InteractiveSignaturePadBox(
    t: AppointmentTokens,
    strokes: MutableList<DrawnStroke>,
    onSizeChanged: (IntSize) -> Unit,
    height: Dp,
    testTag: String? = null
) {
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .dashedBorder(1.5.dp, t.border, 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(t.surface2)
            .onSizeChanged(onSizeChanged)
            .let { if (testTag != null) it.testTag(testTag) else it }
    ) {
        if (strokes.isEmpty() && currentPoints.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👆 Parmağınla imzala",
                    fontSize = 12.sp,
                    color = t.text2
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPoints = listOf(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPoints = currentPoints + change.position
                        },
                        onDragEnd = {
                            if (currentPoints.size > 1) {
                                strokes.add(DrawnStroke(currentPoints))
                            }
                            currentPoints = emptyList()
                        },
                        onDragCancel = {
                            currentPoints = emptyList()
                        }
                    )
                }
        ) {
            val strokeStyle = Stroke(
                width = 2.4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
            strokes.forEach { stroke ->
                if (stroke.points.size > 1) {
                    val p = Path().apply {
                        moveTo(stroke.points.first().x, stroke.points.first().y)
                        stroke.points.drop(1).forEach { pt -> lineTo(pt.x, pt.y) }
                    }
                    drawPath(path = p, color = t.text, style = strokeStyle)
                }
            }
            if (currentPoints.size > 1) {
                val p = Path().apply {
                    moveTo(currentPoints.first().x, currentPoints.first().y)
                    currentPoints.drop(1).forEach { pt -> lineTo(pt.x, pt.y) }
                }
                drawPath(path = p, color = t.text, style = strokeStyle)
            }
        }
    }
}

// ============================================================================
// IBAN GÖNDERME DİYALOĞU (BANK TRANSFER MODAL & LEGACY COMPATIBLE DIALOG)
// ============================================================================

@Composable
fun SendBankTransferDialog(
    appointment: Appointment,
    bankAccounts: List<BankAccount>,
    initialAmount: String = "",
    onDismiss: () -> Unit,
    onSend: (accountKey: String, amount: Double?, promisedDate: String?, onResult: (Result<String>) -> Unit) -> Unit
) {
    val t = rememberAppointmentTokens()
    BankTransferModalDialog(
        t = t,
        appointment = appointment,
        bankAccounts = bankAccounts,
        initialAmount = initialAmount,
        onDismiss = onDismiss,
        onSend = { accountKey, amount, promised ->
            onSend(accountKey, amount, promised) { _ -> }
        }
    )
}

@Composable
fun BankTransferModalDialog(
    t: AppointmentTokens,
    appointment: Appointment,
    bankAccounts: List<BankAccount>,
    initialAmount: String = "",
    onDismiss: () -> Unit,
    onSend: (String, Double?, String?) -> Unit
) {
    var selectedAccountId by remember(bankAccounts) {
        mutableStateOf(bankAccounts.firstOrNull()?.id ?: "")
    }
    var amountStr by remember(appointment, initialAmount) {
        val raw = initialAmount.ifBlank { appointment.jobReport?.collectedAmount ?: "" }
        mutableStateOf(raw)
    }
    var promisedDate by remember(appointment) {
        mutableStateOf(appointment.jobReport?.paymentPromiseDate ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = t.surface,
        titleContentColor = t.text,
        textContentColor = t.text2,
        title = {
            Text(
                text = "🏦 Müşteriye IBAN Gönder",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = t.text
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${appointment.customerName} (${appointment.phone}) numarasına seçilen banka IBAN bilgisi gönderilecek.",
                    fontSize = 12.sp,
                    color = t.text2
                )

                FormFieldLabel(t = t, text = "Banka Hesabı Seçin")
                val accountLabels = bankAccounts.map { "${it.bankName} · ${it.accountHolder}" }
                val selectedIdx = bankAccounts.indexOfFirst { it.id == selectedAccountId }.coerceAtLeast(0)
                FormDropdownField(
                    t = t,
                    value = accountLabels.getOrNull(selectedIdx) ?: "Hesap Seçin",
                    options = accountLabels,
                    onSelect = { label ->
                        val idx = accountLabels.indexOf(label)
                        if (idx >= 0) selectedAccountId = bankAccounts[idx].id
                    },
                    testTag = "bank_account_dropdown"
                )

                FormFieldLabel(t = t, text = "Ödeme Tutarı (₺, Opsiyonel)")
                FormTextInput(
                    t = t,
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    placeholder = "Örn: 1500",
                    keyboardType = KeyboardType.Number,
                    isMono = true,
                    testTag = "payment_amount_input"
                )

                FormFieldLabel(t = t, text = "Ödeme Sözü Tarihi (Opsiyonel)")
                FormTextInput(
                    t = t,
                    value = promisedDate,
                    onValueChange = { promisedDate = it },
                    placeholder = "GG.AA.YYYY",
                    testTag = "promised_date_input"
                )
            }
        },
        confirmButton = {
            Surface(
                onClick = {
                    if (selectedAccountId.isNotBlank()) {
                        onSend(
                            selectedAccountId,
                            parseLocalizedDouble(amountStr),
                            promisedDate.trim().ifBlank { null }
                        )
                    }
                },
                shape = RoundedCornerShape(10.dp),
                color = t.primary,
                modifier = Modifier.testTag("send_bank_transfer_button")
            ) {
                Text(
                    text = "📲 IBAN Gönder",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        },
        dismissButton = {
            Surface(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                color = t.surface2,
                border = BorderStroke(1.dp, t.border)
            ) {
                Text(
                    text = "Vazgeç",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = t.text2,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    )
}
