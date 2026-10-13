package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

@Composable
fun CustomerDistrictSelectorField(
    label: String?,
    selectedDistrict: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    tokens: AppointmentTokens,
    modifier: Modifier = Modifier,
    height: Dp = 42.dp
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                color = tokens.text2,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.3.sp
            )
        }
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(RoundedCornerShape(11.dp))
                    .background(tokens.surface2)
                    .border(1.dp, tokens.border, RoundedCornerShape(11.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedDistrict.ifBlank { "Bayrampaşa" },
                    color = tokens.text,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "▾",
                    color = tokens.text2,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(tokens.surface)
                    .heightIn(max = 320.dp)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                color = if (option == selectedDistrict) tokens.primary else tokens.text,
                                fontSize = 13.sp,
                                fontWeight = if (option == selectedDistrict) FontWeight.ExtraBold else FontWeight.SemiBold
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewCustomerFormContent(
    isModal: Boolean,
    existingCustomers: List<Customer>,
    tokens: AppointmentTokens,
    onClose: () -> Unit,
    onShowToast: (String) -> Unit,
    onOpenExistingCustomer: (Customer) -> Unit,
    onCustomerCreated: (Customer, CustomerLocalOverride) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("Bayrampaşa") }
    var address by remember { mutableStateOf("") }
    var gateNote by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var demoContactCursor by remember { mutableIntStateOf(0) }
    var pendingContactUri by remember { mutableStateOf<Uri?>(null) }

    fun applyFallbackDemoContact() {
        val sample = FallbackSampleContacts[demoContactCursor % FallbackSampleContacts.size]
        demoContactCursor += 1
        name = sample.first
        phone = sample.second
        onShowToast("📇 ${sample.first} rehberden dolduruldu")
    }

    fun extractContactFromUri(uri: Uri) {
        scope.launch {
            val pair = withContext(Dispatchers.IO) {
                var pickedName = ""
                var pickedPhone = ""
                var contactId = ""
                try {
                    context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                        if (c.moveToFirst()) {
                            val nameIdx = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                            if (nameIdx >= 0) pickedName = c.getString(nameIdx)?.trim().orEmpty()
                            val idIdx = c.getColumnIndex(ContactsContract.Contacts._ID)
                            if (idIdx >= 0) contactId = c.getString(idIdx)?.trim().orEmpty()
                            val numIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (numIdx >= 0) pickedPhone = c.getString(numIdx)?.trim().orEmpty()
                        }
                    }
                    if (pickedPhone.isBlank() && contactId.isNotBlank()) {
                        context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                            arrayOf(contactId),
                            null
                        )?.use { pc ->
                            if (pc.moveToFirst()) {
                                val idx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (idx >= 0) pickedPhone = pc.getString(idx)?.trim().orEmpty()
                            }
                        }
                    }
                } catch (_: Exception) {
                }
                pickedName to pickedPhone
            }
            if (pair.first.isNotBlank() || pair.second.isNotBlank()) {
                if (pair.first.isNotBlank()) name = pair.first
                if (pair.second.isNotBlank()) phone = formatTrPhoneDisplay(pair.second)
                onShowToast("📇 ${pair.first.ifBlank { "Kişi" }} rehberden dolduruldu")
            } else {
                applyFallbackDemoContact()
            }
        }
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri != null) {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPerm) {
                extractContactFromUri(uri)
            } else {
                pendingContactUri = uri
            }
        }
    }

    val readContactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val uri = pendingContactUri
        pendingContactUri = null
        if (granted && uri != null) {
            extractContactFromUri(uri)
        } else if (granted) {
            try {
                contactPickerLauncher.launch(null)
            } catch (_: Exception) {
                applyFallbackDemoContact()
            }
        } else {
            applyFallbackDemoContact()
        }
    }

    fun handlePickFromContacts() {
        val hasPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPerm) {
            readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            try {
                contactPickerLauncher.launch(null)
            } catch (_: Exception) {
                applyFallbackDemoContact()
            }
        }
    }

    fun handleSave() {
        val trimmedName = name.trim()
        val trimmedPhone = phone.trim()
        if (trimmedName.isBlank() || trimmedPhone.isBlank()) {
            onShowToast("⚠️ Ad soyad ve telefon zorunlu")
            return
        }
        val norm = normalizeTrPhoneDigits(trimmedPhone)
        val duplicate = existingCustomers.firstOrNull {
            !it.isArchived && normalizeTrPhoneDigits(it.phone) == norm && norm.isNotBlank()
        }
        if (duplicate != null) {
            onShowToast("ℹ️ Bu telefon numarası zaten kayıtlı (${duplicate.name})")
            onOpenExistingCustomer(duplicate)
            return
        }

        val todayStr = SimpleDateFormat("dd.MM.yyyy", TrLocale).format(Date())
        val newId = "c_${System.currentTimeMillis()}"
        val combinedNotes = buildString {
            if (brand.isNotBlank()) append("Kombi: $brand ${model.trim()}".trim())
            if (gateNote.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append("Kapı Notu: ${gateNote.trim()}")
            }
        }
        val newCustomer = Customer(
            id = newId,
            name = trimmedName,
            phone = formatTrPhoneDisplay(trimmedPhone),
            district = district.ifBlank { "Bayrampaşa" },
            address = address.trim(),
            appointmentCount = 0,
            activeAppointmentCount = 0,
            notes = combinedNotes,
            isArchived = false,
            gateNote = gateNote.trim(),
            serviceNote = "",
            deviceBrand = brand.trim(),
            deviceModel = model.trim(),
            deviceNotes = "",
            autoWhatsApp = true,
            registeredAt = todayStr
        )
        val localOverride = CustomerLocalOverride(
            name = newCustomer.name,
            phone = newCustomer.phone,
            district = newCustomer.district,
            address = newCustomer.address,
            gateNote = newCustomer.gateNote,
            serviceNote = "",
            deviceBrand = newCustomer.deviceBrand,
            deviceModel = newCustomer.deviceModel,
            deviceNotes = "",
            autoWhatsApp = true
        )
        onCustomerCreated(newCustomer, localOverride)
    }

    Column(
        modifier = Modifier
            .then(
                if (isModal) {
                    Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .heightIn(max = 680.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(tokens.bg)
                        .border(1.dp, tokens.border, RoundedCornerShape(24.dp))
                } else {
                    Modifier
                        .fillMaxSize()
                        .background(tokens.bg)
                }
            )
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.surface)
                    .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Text("✕", color = tokens.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Yeni Müşteri Kaydı",
                    color = tokens.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Portföye hızlı müşteri ekle",
                    color = tokens.text2,
                    fontSize = 11.5.sp
                )
            }
        }

        // Scrollable Body
        Column(
            modifier = Modifier
                .weight(1f, fill = !isModal)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Rehberden Seç Dashed Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tokens.primaryC)
                    .dashedBorder(
                        width = 1.5.dp,
                        color = tokens.primary.copy(alpha = 0.45f),
                        cornerRadius = 14.dp
                    )
                    .clickable { handlePickFromContacts() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("📇", fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Rehberden Seç (isim ve telefon otomatik dolar)",
                    color = tokens.primary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Person & Address Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(tokens.surface)
                    .border(1.dp, tokens.border, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                CustomerLabeledInput(
                    label = "AD SOYAD *",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Örn: Mehmet Kaya",
                    tokens = tokens
                )

                CustomerLabeledInput(
                    label = "CEP TELEFONU *",
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "0532 000 00 00",
                    tokens = tokens,
                    isMonospace = true,
                    keyboardType = KeyboardType.Phone
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomerDistrictSelectorField(
                        label = "İLÇE",
                        selectedDistrict = district,
                        options = CustomerPriorityDistricts,
                        onSelect = { district = it },
                        tokens = tokens,
                        modifier = Modifier.weight(1f)
                    )
                    CustomerLabeledInput(
                        label = "BİNA / KAPI NOTU",
                        value = gateNote,
                        onValueChange = { gateNote = it },
                        placeholder = "Kat 3 D:7, zil bozuk",
                        tokens = tokens,
                        modifier = Modifier.weight(1f)
                    )
                }

                CustomerLabeledInput(
                    label = "AÇIK ADRES",
                    value = address,
                    onValueChange = { address = it },
                    placeholder = "Mahalle, sokak, bina no...",
                    tokens = tokens,
                    singleLine = false,
                    minHeight = 64.dp
                )
            }

            // Kombi Card (Optional)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(tokens.surface)
                    .border(1.dp, tokens.border, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔥 KOMBİ",
                        color = tokens.text2,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "İsteğe bağlı",
                        color = tokens.text2,
                        fontSize = 11.sp
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickBoilerBrands.forEach { b ->
                        val active = brand == b
                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (active) tokens.primary else tokens.surface2)
                                .border(
                                    width = 1.dp,
                                    color = if (active) tokens.primary else tokens.border,
                                    shape = RoundedCornerShape(9.dp)
                                )
                                .clickable { brand = if (active) "" else b }
                                .padding(horizontal = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = b,
                                color = if (active) Color.White else tokens.text,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                CustomerLabeledInput(
                    label = "MODEL",
                    value = model,
                    onValueChange = { model = it },
                    placeholder = "Model (örn: Proteus Premix 24 kW)",
                    tokens = tokens
                )
            }
        }

        // Sticky Bottom Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(tokens.surface)
                .border(1.dp, tokens.border)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tokens.surface2)
                    .border(1.dp, tokens.border, RoundedCornerShape(14.dp))
                    .clickable(onClick = onClose)
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Vazgeç",
                    color = tokens.text2,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tokens.primaryGradient)
                    .clickable { handleSave() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🙋 Müşteri Kaydını Oluştur",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun ContactImportFlowContent(
    isModal: Boolean,
    isTablet: Boolean,
    existingCustomers: List<Customer>,
    tokens: AppointmentTokens,
    onClose: () -> Unit,
    onShowToast: (String) -> Unit,
    onImportCustomers: (List<Customer>) -> Unit
) {
    val context = LocalContext.current
    var step by remember { mutableStateOf("perm") } // "perm" | "scanning" | "pick" | "importing" | "done"
    var scannedContacts by remember { mutableStateOf<List<ScannedPhoneContact>>(emptyList()) }
    val selectedMap = remember { mutableStateMapOf<String, Boolean>() }

    var activeTab by remember { mutableStateOf("new") } // "new" | "existing"
    var searchQuery by remember { mutableStateOf("") }
    var defaultDistrict by remember { mutableStateOf("Belirtilmemiş") }
    var autoWaEnabled by remember { mutableStateOf(true) }

    var importProgressTarget by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = importProgressTarget,
        animationSpec = tween(durationMillis = 650),
        label = "import_progress"
    )
    var addedCountResult by remember { mutableIntStateOf(0) }

    val existingPhonesSet = remember(existingCustomers) {
        existingCustomers.filter { !it.isArchived }
            .map { normalizeTrPhoneDigits(it.phone) }
            .filter { it.isNotBlank() }
            .toSet()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            onShowToast("ℹ️ Rehber izni verilmedi, örnek rehber kayıtları yüklendi")
        }
        step = "scanning"
    }

    LaunchedEffect(step) {
        if (step == "scanning") {
            val loaded = scanDeviceContactsOrFallback(context, existingPhonesSet)
            delay(650)
            scannedContacts = loaded
            selectedMap.clear()
            loaded.forEach { c ->
                selectedMap[c.id] = c.defaultSelected
            }
            step = "pick"
        } else if (step == "importing") {
            importProgressTarget = 0.25f
            delay(260)
            importProgressTarget = 0.72f
            delay(280)
            importProgressTarget = 1f
            delay(320)

            val todayStr = SimpleDateFormat("dd.MM.yyyy", TrLocale).format(Date())
            val toImport = scannedContacts.filter { !it.alreadyExists && selectedMap[it.id] == true }
            val createdList = toImport.map { item ->
                Customer(
                    id = "c_imp_${UUID.randomUUID().toString().take(8)}",
                    name = item.name,
                    phone = item.phone,
                    district = defaultDistrict,
                    address = "",
                    appointmentCount = 0,
                    activeAppointmentCount = 0,
                    notes = "",
                    isArchived = false,
                    autoWhatsApp = autoWaEnabled,
                    registeredAt = todayStr
                )
            }
            addedCountResult = createdList.size
            if (createdList.isNotEmpty()) {
                onImportCustomers(createdList)
            }
            step = "done"
        }
    }

    val importableList = remember(scannedContacts) { scannedContacts.filter { !it.alreadyExists } }
    val existingList = remember(scannedContacts) { scannedContacts.filter { it.alreadyExists } }

    val filteredContacts = remember(scannedContacts, activeTab, searchQuery) {
        val base = if (activeTab == "new") importableList else existingList
        val q = searchQuery.trim().lowercase(TrLocale)
        if (q.isEmpty()) base
        else base.filter {
            it.name.lowercase(TrLocale).contains(q) || it.phone.contains(q) || it.normalizedPhone.contains(q)
        }
    }

    val selectedCount = importableList.count { selectedMap[it.id] == true }
    val allImportableSelected = importableList.isNotEmpty() && importableList.all { selectedMap[it.id] == true }

    Column(
        modifier = Modifier
            .then(
                if (isModal) {
                    Modifier
                        .widthIn(max = 680.dp)
                        .fillMaxWidth()
                        .height(640.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(tokens.bg)
                        .border(1.dp, tokens.border, RoundedCornerShape(24.dp))
                } else {
                    Modifier
                        .fillMaxSize()
                        .background(tokens.bg)
                }
            )
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.surface)
                    .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Text("✕", color = tokens.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tüm Rehberi Aktar",
                    color = tokens.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Telefon rehberinden toplu müşteri aktarımı",
                    color = tokens.text2,
                    fontSize = 11.5.sp
                )
            }
        }

        when (step) {
            "perm" -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(tokens.primaryC)
                            .border(1.dp, tokens.primary.copy(alpha = 0.35f), RoundedCornerShape(26.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📇", fontSize = 40.sp)
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Rehbere erişim izni",
                        color = tokens.text,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Telefon rehberindeki kişileri tarayıp kombi müşterilerini tek seferde portföye ekleyelim.",
                        color = tokens.text2,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 320.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .widthIn(max = 360.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(tokens.surface)
                            .border(1.dp, tokens.border, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "🔁" to "Aynı numara iki kez eklenmez",
                            "👪" to "Anne, kardeş gibi kişisel kayıtlar seçilmeden gelir",
                            "☎️" to "Sabit hatlar ayrıca işaretlenir"
                        ).forEach { (ic, txt) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                Text(ic, fontSize = 15.sp)
                                Text(
                                    text = txt,
                                    color = tokens.text2,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Box(
                        modifier = Modifier
                            .widthIn(max = 360.dp)
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(tokens.primaryGradient)
                            .clickable {
                                val hasPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.READ_CONTACTS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPerm) {
                                    step = "scanning"
                                } else {
                                    permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Rehbere Eriş",
                            color = Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            "scanning" -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔍", fontSize = 42.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Rehber taranıyor…",
                        color = tokens.text,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Kayıtlı müşterilerle karşılaştırılıyor",
                        color = tokens.text2,
                        fontSize = 12.5.sp
                    )
                }
            }

            "pick" -> {
                Column(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Segmented Tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(tokens.surface)
                                .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                "new" to "Aktarılabilir (${importableList.size})",
                                "existing" to "Zaten Kayıtlı (${existingList.size})"
                            ).forEach { (id, label) ->
                                val isSelected = activeTab == id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (isSelected) tokens.primary else Color.Transparent)
                                        .clickable { activeTab = id },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.White else tokens.text2,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        // Search bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(tokens.surface)
                                .border(1.dp, tokens.border, RoundedCornerShape(11.dp))
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🔎", fontSize = 13.sp, color = tokens.text2)
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Rehberde ara...",
                                        color = tokens.text2.copy(alpha = 0.65f),
                                        fontSize = 13.sp
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = tokens.text,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    cursorBrush = SolidColor(tokens.primary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                Text(
                                    text = "✕",
                                    color = tokens.text2,
                                    fontSize = 13.sp,
                                    modifier = Modifier.clickable { searchQuery = "" }
                                )
                            }
                        }

                        // Select All row (only on "new" tab)
                        if (activeTab == "new") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.clickable {
                                        val target = !allImportableSelected
                                        importableList.forEach { c -> selectedMap[c.id] = target }
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (allImportableSelected) tokens.primary else tokens.surface2)
                                            .border(
                                                width = 1.5.dp,
                                                color = if (allImportableSelected) tokens.primary else tokens.border,
                                                shape = RoundedCornerShape(6.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (allImportableSelected) {
                                            Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                    Text(
                                        text = "Tümünü seç (kişisel ve sabit hatlar dahil)",
                                        color = tokens.text,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "$selectedCount seçili",
                                    color = tokens.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Contact Grid (1 col on phone, 2 cols on tablet)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(if (isTablet) 2 else 1),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredContacts, key = { it.id }) { contact ->
                            val isSelected = selectedMap[contact.id] == true && !contact.alreadyExists
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(if (contact.alreadyExists) 0.55f else 1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(tokens.surface)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) tokens.primary else tokens.border,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable(enabled = !contact.alreadyExists) {
                                        selectedMap[contact.id] = !(selectedMap[contact.id] ?: false)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Checkbox
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(if (isSelected) tokens.primary else tokens.surface2)
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isSelected) tokens.primary else tokens.border,
                                            shape = RoundedCornerShape(7.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Text("✓", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                    } else if (contact.alreadyExists) {
                                        Text("—", color = tokens.text2, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Avatar
                                CustomerAvatarBox(
                                    name = contact.name,
                                    idOrKey = contact.id,
                                    size = 40.dp,
                                    cornerRadius = 12.dp,
                                    fontSize = 13.sp
                                )

                                // Name & Phone
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contact.name,
                                        color = tokens.text,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = contact.phone,
                                        color = tokens.text2,
                                        fontSize = 11.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Classification Badge
                                when {
                                    contact.alreadyExists -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(tokens.surface2)
                                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "✓ Zaten kayıtlı",
                                                color = tokens.text2,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                    contact.isPersonal -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(tokens.violetC)
                                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "👪 Kişisel olabilir",
                                                color = tokens.violet,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                    contact.isLandline -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(tokens.amberC)
                                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "☎️ Sabit hat",
                                                color = tokens.amber,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(tokens.greenC)
                                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "Yeni",
                                                color = tokens.green,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Options + CTA Footer
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(tokens.surface)
                            .border(1.dp, tokens.border)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📍 Varsayılan ilçe:",
                                color = tokens.text2,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            CustomerDistrictSelectorField(
                                label = null,
                                selectedDistrict = defaultDistrict,
                                options = listOf("Belirtilmemiş") + CustomerPriorityDistricts,
                                onSelect = { defaultDistrict = it },
                                tokens = tokens,
                                modifier = Modifier.weight(1f),
                                height = 34.dp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "💬 Otomatik WhatsApp bildirimleri",
                                color = tokens.text,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            CustomerSwitchToggle(
                                checked = autoWaEnabled,
                                onCheckedChange = { autoWaEnabled = it },
                                tokens = tokens
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .then(
                                    if (selectedCount > 0) {
                                        Modifier.background(tokens.primaryGradient)
                                    } else {
                                        Modifier.background(tokens.surface2)
                                    }
                                )
                                .clickable(enabled = selectedCount > 0) {
                                    step = "importing"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📥 $selectedCount Kişiyi Aktar",
                                color = if (selectedCount > 0) Color.White else tokens.text2,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            "importing" -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📥", fontSize = 40.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "$selectedCount kişi",
                        color = tokens.text,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .width(240.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(tokens.surface2)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                                .background(tokens.primaryGradient)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Portföye ekleniyor, mükerrer kontrolü yapılıyor…",
                        color = tokens.text2,
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            "done" -> {
                val skippedCount = (importableList.size - addedCountResult).coerceAtLeast(0)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(tokens.greenC)
                            .border(1.dp, tokens.green.copy(alpha = 0.35f), RoundedCornerShape(26.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎉", fontSize = 42.sp)
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "$addedCountResult yeni müşteri eklendi",
                        color = tokens.text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Portföyün büyüyor, eline sağlık!",
                        color = tokens.text2,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .widthIn(max = 360.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(tokens.surface)
                            .border(1.dp, tokens.border, RoundedCornerShape(18.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(tokens.greenC)
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$addedCountResult",
                                color = tokens.green,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Eklendi",
                                color = tokens.green,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(tokens.surface2)
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${existingList.size}",
                                color = tokens.text,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Zaten kayıtlı",
                                color = tokens.text2,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(tokens.surface2)
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$skippedCount",
                                color = tokens.text2,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Seçilmedi",
                                color = tokens.text2,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Box(
                        modifier = Modifier
                            .widthIn(max = 360.dp)
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(tokens.primaryGradient)
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Müşterilere Dön",
                            color = Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
