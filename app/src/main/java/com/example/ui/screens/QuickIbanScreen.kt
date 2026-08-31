package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Appointment
import com.example.data.model.BankAccount
import com.example.data.model.Customer
import com.example.data.model.FinanceRecord
import com.example.data.model.FinanceType
import com.example.utils.parseLocalizedDouble
import java.net.URLEncoder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickIbanScreen(
    appointments: List<Appointment> = emptyList(),
    customers: List<Customer> = emptyList(),
    bankAccounts: List<BankAccount> = emptyList(),
    onBackClick: () -> Unit,
    onAddFinanceRecord: ((FinanceRecord) -> Unit)? = null,
    onSendBankTransfer: ((appointmentId: String, accountKey: String, amount: Double?, date: String?, onResult: (Result<String>) -> Unit) -> Unit)? = null,
    onAddAppointment: ((Appointment) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Only use accounts received from the backend. Never invent or fall back to
    // a hardcoded IBAN on a payment screen.
    val activeBankAccounts = remember(bankAccounts) {
        bankAccounts.filter { account ->
            account.isReady &&
                account.bankName.isNotBlank() &&
                account.accountHolder.isNotBlank() &&
                account.iban.isNotBlank()
        }
    }

    // State
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var serviceTitle by remember { mutableStateOf("Kombi Bakım & Servis") }
    var amountText by remember { mutableStateOf("") }
    var selectedBankIndex by remember { mutableStateOf(0) }
    var showCustomerPicker by remember { mutableStateOf(false) }
    var customerSearchQuery by remember { mutableStateOf("") }
    var isSendingCloudApi by remember { mutableStateOf(false) }
    var selectedAppointmentId by remember { mutableStateOf<String?>(null) }

    val selectedBank = activeBankAccounts.getOrElse(selectedBankIndex) { activeBankAccounts.firstOrNull() }

    // Quick services chips
    val quickServices = listOf(
        "Kombi Bakım & Servis",
        "Arıza Onarım & Tamir",
        "Petek Temizliği",
        "Kombi Kart Tamiri",
        "Parça Değişimi & Montaj",
        "Genel Kontrol & Gaz Ayarı"
    )

    // Quick amount chips
    val quickAmounts = listOf("1000", "1500", "2000", "2500", "3000", "4000", "5000")

    // Format Amount Display
    val formattedAmountDisplay = remember(amountText) {
        val parsed = parseLocalizedDouble(amountText) ?: 0.0
        val nf = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
        nf.format(parsed)
    }

    // Generate WhatsApp Message text exactly in user's requested template
    val generatedMessage = remember(customerName, serviceTitle, formattedAmountDisplay, selectedBank) {
        val namePart = if (customerName.isNotBlank()) customerName.trim() else "Müşterimiz"
        val bankNamePart = selectedBank?.bankName?.ifBlank { selectedBank.cardTitle } ?: "Banka hesabı seçilmedi"
        val holderPart = selectedBank?.accountHolder?.ifBlank { "Belirtilmedi" } ?: "Belirtilmedi"
        val ibanPart = selectedBank?.iban?.ifBlank { "Belirtilmedi" } ?: "Belirtilmedi"

        """
Merhaba $namePart, Sancak Kombi'yi tercih ettiğiniz için teşekkür ederiz.

Servis ödemenizi dilerseniz havale / EFT yöntemiyle aşağıdaki hesabımıza iletebilirsiniz:

🗓️ Hizmet: $serviceTitle
💰 Tutar: $formattedAmountDisplay
👤 Hesap Sahibi: $holderPart
🏦 Banka: $bankNamePart
🔢 IBAN: $ibanPart

Ödemenizi tamamladıktan sonra dekont paylaşmanız durumunda kaydınız hemen güncellenecektir.
Sağlıklı ve sıcak günlerde kullanmanızı dileriz.
        """.trimIndent()
    }

    fun validatePaymentDetails(): Double? {
        val account = selectedBank
        if (account == null) {
            Toast.makeText(context, "Önce backend'den geçerli bir banka hesabı yüklenmelidir.", Toast.LENGTH_LONG).show()
            return null
        }

        if (serviceTitle.isBlank()) {
            Toast.makeText(context, "Lütfen hizmet açıklamasını girin.", Toast.LENGTH_SHORT).show()
            return null
        }

        val amount = parseLocalizedDouble(amountText)
        if (amount == null || !amount.isFinite() || amount <= 0) {
            Toast.makeText(context, "Lütfen 0'dan büyük geçerli bir tutar girin.", Toast.LENGTH_SHORT).show()
            return null
        }

        return amount
    }

    // Send WhatsApp Action
    fun sendWhatsApp() {
        if (validatePaymentDetails() == null) return

        val cleanDigits = customerPhone.filter { it.isDigit() }
        val finalPhone = when {
            cleanDigits.startsWith("90") -> cleanDigits
            cleanDigits.startsWith("0") -> "90${cleanDigits.substring(1)}"
            cleanDigits.length == 10 -> "90$cleanDigits"
            else -> cleanDigits
        }

        if (finalPhone.length < 10) {
            Toast.makeText(context, "Lütfen geçerli bir telefon numarası girin veya yukarıdan bir randevu seçin.", Toast.LENGTH_LONG).show()
            return
        }

        try {
            val encodedMsg = URLEncoder.encode(generatedMessage, "UTF-8")
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$finalPhone&text=$encodedMsg")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                // Fallback to generic share intent
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, generatedMessage)
                }
                context.startActivity(Intent.createChooser(shareIntent, "IBAN Bilgisini Paylaş"))
            } catch (ex: Exception) {
                Toast.makeText(context, "WhatsApp açılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Copy Action
    fun copyToClipboard() {
        if (validatePaymentDetails() == null) return

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Sancak Kombi IBAN", generatedMessage)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "✓ IBAN ve mesaj panoya kopyalandı!", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("quick_iban_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hızlı IBAN Gönder",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Serviste müşteriye tek tıkla WhatsApp'tan IBAN atın",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { copyToClipboard() }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Kopyala",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. USTAYA ÖZEL HIZLI SEÇİM: BUGÜNKÜ / AKTİF RANDEVULAR (TEK DOKUNUŞ)
            if (appointments.isNotEmpty()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ Bugünkü Randevulardan Seç (Tek Dokunuş)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0288D1)
                        )
                        Text(
                            text = "${appointments.size} Randevu",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        appointments.take(8).forEach { appt ->
                            val isSelected = customerPhone == appt.phone || (customerName.isNotBlank() && customerName == appt.customerName)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF0288D1).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF0288D1) else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .clickable {
                                        customerName = appt.customerName
                                        customerPhone = appt.phone
                                        selectedAppointmentId = appt.id
                                        if (appt.serviceType.isNotBlank()) {
                                            serviceTitle = appt.serviceType
                                        }
                                        val collected = appt.jobReport?.collectedAmount?.let(::parseLocalizedDouble)
                                        if (collected != null && collected > 0) {
                                            amountText = collected.toInt().toString()
                                        }
                                        Toast.makeText(context, "Seçildi: ${appt.customerName}", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isSelected) Color(0xFF0288D1) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = appt.customerName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color(0xFF0288D1) else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${appt.phone} • ${appt.serviceType}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. MÜŞTERİ BİLGİSİ GİRİŞİ (KİME GİDECEK?)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1. Kime Gönderilecek?",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (customers.isNotEmpty()) {
                            TextButton(onClick = { showCustomerPicker = !showCustomerPicker }) {
                                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (showCustomerPicker) "Aramayı Kapat" else "Rehberden Seç", fontSize = 12.sp)
                            }
                        }
                    }

                    // Customer search dropdown/filter if opened
                    AnimatedVisibility(visible = showCustomerPicker) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = customerSearchQuery,
                                onValueChange = { customerSearchQuery = it },
                                placeholder = { Text("Müşteri adı veya telefon ara...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val filteredCusts = customers.filter {
                                it.name.contains(customerSearchQuery, ignoreCase = true) || it.phone.contains(customerSearchQuery)
                            }.take(5)

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                filteredCusts.forEach { cust ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                customerName = cust.name
                                                customerPhone = cust.phone
                                                showCustomerPicker = false
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(cust.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(cust.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Müşteri Adı
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Müşteri Adı Soyadı") },
                        placeholder = { Text("Müşterinin adı soyadı") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Telefon Numarası
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("WhatsApp / Telefon Numarası *") },
                        placeholder = { Text("05xx xxx xx xx") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF22C55E))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF22C55E),
                            focusedLabelColor = Color(0xFF22C55E)
                        )
                    )
                }
            }

            // 3. HİZMET & TUTAR
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Hizmet ve Tutar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hizmet Hızlı Seçim
                    Text(
                        text = "Hizmet Türü",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickServices.forEach { srv ->
                            FilterChip(
                                selected = serviceTitle == srv,
                                onClick = { serviceTitle = srv },
                                label = { Text(srv, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = serviceTitle,
                        onValueChange = { serviceTitle = it },
                        label = { Text("Hizmet Açıklaması") },
                        leadingIcon = {
                            Icon(Icons.Default.Handyman, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tutar Hızlı Seçim
                    Text(
                        text = "Ödeme Tutarı (TL)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickAmounts.forEach { amt ->
                            FilterChip(
                                selected = amountText == amt,
                                onClick = { amountText = amt },
                                label = { Text("₺$amt", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF22C55E).copy(alpha = 0.15f),
                                    selectedLabelColor = Color(0xFF15803D)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Tutar (₺)") },
                        placeholder = { Text("Örn: 2000") },
                        leadingIcon = {
                            Text("₺", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E), modifier = Modifier.padding(start = 12.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // 4. BANKA HESABI SEÇİMİ
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Hangi Banka Hesabınız Gönderilsin?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Müşterinin ödeme yapacağı IBAN hesabını seçin",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (activeBankAccounts.isEmpty()) {
                            Text(
                                text = "Kullanılabilir ödeme hesabı bulunamadı. Önce web yönetim panelinden hesapları tanımlayın.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            activeBankAccounts.forEachIndexed { index, bank ->
                            val isSelected = selectedBankIndex == index
                            val bankColor = when {
                                bank.bankName.contains("YAPI", ignoreCase = true) -> Color(0xFF0047BB)
                                bank.bankName.contains("AKBANK", ignoreCase = true) -> Color(0xFFE20613)
                                bank.bankName.contains("KUVEYT", ignoreCase = true) -> Color(0xFF008752)
                                else -> MaterialTheme.colorScheme.primary
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) bankColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) bankColor else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedBankIndex = index }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(bankColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccountBalance,
                                                contentDescription = null,
                                                tint = bankColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = bank.bankName.ifBlank { bank.cardTitle },
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "👤 ${bank.accountHolder}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = bank.iban,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = bankColor
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Seçili",
                                            tint = bankColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                            }
                        }
                    }
                }
            }

            // 5. CANLI WHATSAPP MESAJI ÖNİZLEMESİ
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WhatsApp Mesajı Önizleme",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { copyToClipboard() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Kopyala",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = generatedMessage,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            // ==================== BÜYÜK DEV EYLEM BUTONLARI ====================
            Spacer(modifier = Modifier.height(4.dp))

            // 1. DEV YEŞİL WHATSAPP BUTONU (TEK DOKUNUŞLA CLOUD API İLE GÖNDER)
            Button(
                onClick = {
                    val parsedAmt = validatePaymentDetails() ?: return@Button
                    val cleanDigits = customerPhone.filter { it.isDigit() }
                    if (cleanDigits.length < 10) {
                        Toast.makeText(context, "Lütfen geçerli bir telefon numarası girin veya yukarıdan bir randevu seçin.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    val accountKey = when (selectedBank?.id?.lowercase(Locale.ROOT)) {
                        "fatih" -> "fatih"
                        "fettah" -> "fettah"
                        "abdullah" -> "abdullah"
                        else -> {
                            Toast.makeText(context, "Seçilen banka hesabı web sisteminde tanımlı değil.", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                    }

                    val targetApptId = selectedAppointmentId
                        ?: appointments.find {
                            val aPhone = it.phone.filter { c -> c.isDigit() }
                            aPhone.takeLast(10) == cleanDigits.takeLast(10)
                        }?.id

                    if (targetApptId != null && onSendBankTransfer != null) {
                        isSendingCloudApi = true
                        onSendBankTransfer(targetApptId, accountKey, parsedAmt, null) { result ->
                            isSendingCloudApi = false
                            result.onSuccess {
                                Toast.makeText(context, "✅ WhatsApp IBAN mesajı Meta Cloud API ile müşteriye gönderildi!", Toast.LENGTH_LONG).show()
                            }.onFailure { err ->
                                Toast.makeText(context, "Cloud API Hatası: ${err.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        // Fallback if no appointment found in system
                        sendWhatsApp()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("send_whatsapp_button"),
                enabled = !isSendingCloudApi,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF25D366)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                if (isSendingCloudApi) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Cloud API ile Gönderiliyor...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "WhatsApp ile IBAN Gönder",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // 2. FİNANSA GELİR/ALACAK OLARAK İŞLE (İSTEĞE BAĞLI)
            if (onAddFinanceRecord != null) {
                OutlinedButton(
                    onClick = {
                        val parsedAmt = validatePaymentDetails() ?: return@OutlinedButton
                        val todayStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                        val rec = FinanceRecord(
                            id = "iban_req_${System.currentTimeMillis()}",
                            type = FinanceType.GELIR,
                            source = if (customerName.isNotBlank()) "Havale Talebi: $customerName" else "Havale/EFT Servis Geliri",
                            amount = parsedAmt,
                            totalAmount = parsedAmt,
                            collectedAmount = 0.0,
                            date = todayStr,
                            status = "Bekliyor",
                            note = "$serviceTitle (${selectedBank?.bankName ?: "Banka"}) - IBAN İletildi"
                        )
                        onAddFinanceRecord(rec)
                        Toast.makeText(context, "✓ Finans kaydı olarak işlendi (Bekliyor)", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF10B981)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Finans Bekleyen Alacaklara Ekle", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
