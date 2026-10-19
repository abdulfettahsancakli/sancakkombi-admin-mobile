package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CatalogItem
import com.example.data.model.Proposal
import com.example.data.model.ProposalItem
import com.example.data.model.ProposalStatus
import com.example.utils.parseLocalizedDouble
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun NewProposalScreen(
    catalogItems: List<CatalogItem> = emptyList(),
    onBackClick: () -> Unit,
    onCreateProposal: (Proposal) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tokens = rememberProposalTokens()

    // Form fields
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerEmail by remember { mutableStateOf("") }
    var customerDistrict by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("") }
    var deviceBrand by remember { mutableStateOf("") }
    var deviceModel by remember { mutableStateOf("") }

    // Line items list
    val items = remember {
        mutableStateListOf(
            ProposalItem("1", "", 1, 0.0)
        )
    }

    var downPayment by remember { mutableStateOf("0") }
    var remainingPaymentType by remember { mutableStateOf("Nakit") }
    var validityDays by remember { mutableStateOf("15 Gün") }
    var validUntilDate by remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 15) }
        mutableStateOf(SimpleDateFormat("dd.MM.yyyy", Locale("tr")).format(cal.time))
    }
    var preparedBy by remember { mutableStateOf("Sancak Kombi Yetkili Servisi") }
    var note by remember { mutableStateOf("Montaj ve işçilik Sancak Kombi güvencesiyle 1 yıl garantilidir.") }
    var discount by remember { mutableStateOf("0") }

    val currentDateStr = remember {
        SimpleDateFormat("d MMMM yyyy", Locale("tr")).format(Date())
    }

    val subtotal = items.sumOf { it.totalPrice }
    val discountVal = parseLocalizedDouble(discount) ?: 0.0
    val grandTotal = (subtotal - discountVal).coerceAtLeast(0.0)
    val validItems = items.filter { it.title.isNotBlank() && it.quantity > 0 && it.unitPrice >= 0.0 }
    val canCreateProposal = customerName.isNotBlank() && validItems.isNotEmpty()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = tokens.bg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.text),
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Geri", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Yeni Fiyat Teklifi",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = tokens.text
            )
            Text(
                text = "Müşteriye verilecek resmi servis, parça veya montaj teklifini hazırlayın.",
                fontSize = 13.sp,
                color = tokens.text2
            )

            Spacer(modifier = Modifier.height(16.dp))

            NewProposalFormContent(
                tokens = tokens,
                customerName = customerName,
                onCustomerNameChange = { customerName = it },
                customerPhone = customerPhone,
                onCustomerPhoneChange = { customerPhone = it },
                customerEmail = customerEmail,
                onCustomerEmailChange = { customerEmail = it },
                customerDistrict = customerDistrict,
                onCustomerDistrictChange = { customerDistrict = it },
                customerAddress = customerAddress,
                onCustomerAddressChange = { customerAddress = it },
                deviceBrand = deviceBrand,
                onDeviceBrandChange = { deviceBrand = it },
                deviceModel = deviceModel,
                onDeviceModelChange = { deviceModel = it },
                items = items,
                catalogItems = catalogItems,
                downPayment = downPayment,
                onDownPaymentChange = { downPayment = it },
                remainingPaymentType = remainingPaymentType,
                onRemainingPaymentTypeChange = { remainingPaymentType = it },
                validityDays = validityDays,
                onValidityDaysChange = { daysLabel ->
                    validityDays = daysLabel
                    val offset = when (daysLabel) {
                        "7 Gün" -> 7
                        "30 Gün" -> 30
                        else -> 15
                    }
                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
                    validUntilDate = SimpleDateFormat("dd.MM.yyyy", Locale("tr")).format(cal.time)
                },
                validUntilDate = validUntilDate,
                onValidUntilDateChange = { validUntilDate = it },
                preparedBy = preparedBy,
                onPreparedByChange = { preparedBy = it },
                note = note,
                onNoteChange = { note = it },
                discount = discount,
                onDiscountChange = { discount = it },
                subtotal = subtotal,
                grandTotal = grandTotal
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (canCreateProposal) {
                        val newProposal = Proposal(
                            id = "TF-" + SimpleDateFormat("yyyyMM", Locale.US).format(Date()) + "-" + UUID.randomUUID().toString().take(6).uppercase(Locale.US),
                            quoteNumber = "TF-" + SimpleDateFormat("yyyyMM", Locale.US).format(Date()) + "-" + UUID.randomUUID().toString().take(6).uppercase(Locale.US),
                            customerName = customerName,
                            customerPhone = customerPhone,
                            customerEmail = customerEmail,
                            customerDistrict = customerDistrict,
                            customerAddress = customerAddress,
                            deviceBrand = deviceBrand,
                            deviceModel = deviceModel,
                            date = currentDateStr,
                            validUntilDate = validUntilDate,
                            preparedBy = preparedBy,
                            note = note,
                            status = ProposalStatus.PENDING,
                            items = validItems,
                            downPayment = parseLocalizedDouble(downPayment) ?: 0.0,
                            remainingPaymentType = remainingPaymentType,
                            discount = discountVal
                        )
                        onCreateProposal(newProposal)
                        Toast.makeText(context, "Fiyat Teklifi Başarıyla Oluşturuldu", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Lütfen müşteri adını ve en az bir geçerli teklif kalemini doldurun", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = canCreateProposal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.primary,
                    contentColor = tokens.onPrimary
                )
            ) {
                Text(
                    text = "Teklifi Oluştur & Paylaş →",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun NewProposalFormContent(
    tokens: ProposalTokens,
    customerName: String,
    onCustomerNameChange: (String) -> Unit,
    customerPhone: String,
    onCustomerPhoneChange: (String) -> Unit,
    customerEmail: String,
    onCustomerEmailChange: (String) -> Unit,
    customerDistrict: String,
    onCustomerDistrictChange: (String) -> Unit,
    customerAddress: String,
    onCustomerAddressChange: (String) -> Unit,
    deviceBrand: String,
    onDeviceBrandChange: (String) -> Unit,
    deviceModel: String,
    onDeviceModelChange: (String) -> Unit,
    items: MutableList<ProposalItem>,
    catalogItems: List<CatalogItem>,
    downPayment: String,
    onDownPaymentChange: (String) -> Unit,
    remainingPaymentType: String,
    onRemainingPaymentTypeChange: (String) -> Unit,
    validityDays: String,
    onValidityDaysChange: (String) -> Unit,
    validUntilDate: String,
    onValidUntilDateChange: (String) -> Unit,
    preparedBy: String,
    onPreparedByChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    discount: String,
    onDiscountChange: (String) -> Unit,
    subtotal: Double,
    grandTotal: Double,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. MÜŞTERİ & KOMBİ BİLGİLERİ
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = tokens.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "1. Müşteri & Kombi Cihaz Bilgileri",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = tokens.text
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(tokens.primaryC)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("İletişim & Donanım", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tokens.primary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = onCustomerNameChange,
                    label = { Text("Müşteri Adı Soyadı / Kurum Ünvanı *") },
                    placeholder = { Text("Örn: Ahmet Yılmaz veya Sancak Ltd. Şti.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = onCustomerPhoneChange,
                        label = { Text("Telefon") },
                        placeholder = { Text("05XX XXX XX XX") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1.1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = customerEmail,
                        onValueChange = onCustomerEmailChange,
                        label = { Text("E-posta") },
                        placeholder = { Text("ornek@mail.com") },
                        modifier = Modifier.weight(0.9f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = customerDistrict,
                    onValueChange = onCustomerDistrictChange,
                    label = { Text("İlçe / Bölge") },
                    placeholder = { Text("Örn: Bayrampaşa") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Hızlı İlçe Çipleri
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QUICK_DISTRICTS.forEach { district ->
                        val isSel = customerDistrict.equals(district, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) tokens.primaryC else tokens.surface2)
                                .border(1.dp, if (isSel) tokens.primary else tokens.border, RoundedCornerShape(8.dp))
                                .clickable { onCustomerDistrictChange(district) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = district,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) tokens.primary else tokens.text2
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = customerAddress,
                    onValueChange = onCustomerAddressChange,
                    label = { Text("Açık Adres / Montaj Mahalli") },
                    placeholder = { Text("Mahalle, Cadde, Sokak, Bina No, Daire...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Kombi Markası Hızlı Seçim
                Text(
                    text = "Kombi Markası Seçin",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = tokens.text2
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PROPOSAL_BRANDS.forEach { brand ->
                        val isSelected = deviceBrand.equals(brand, ignoreCase = true)
                        val mono = getBrandMonogram(brand)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) tokens.primaryC else tokens.surface2)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) tokens.primary else tokens.border,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onDeviceBrandChange(brand) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BrandLogoBadge(
                                    brand = brand,
                                    size = 22.dp,
                                    tokens = tokens
                                )
                                Text(
                                    text = brand,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) tokens.primary else tokens.text
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = deviceBrand,
                        onValueChange = onDeviceBrandChange,
                        label = { Text("Marka (Özel Değer)") },
                        placeholder = { Text("Örn: DemirDöküm") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = deviceModel,
                        onValueChange = onDeviceModelChange,
                        label = { Text("Model / Kapasite") },
                        placeholder = { Text("Örn: Nitromix P24") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Seçilen markaya ait popüler modeller (Hızlı Seçim)
                val popularModels = BRAND_MODELS[deviceBrand] ?: emptyList()
                if (popularModels.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Model:", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = tokens.text2)
                        popularModels.forEach { m ->
                            val isSelModel = deviceModel.equals(m, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelModel) tokens.primaryC else tokens.surface2)
                                    .border(1.dp, if (isSelModel) tokens.primary else tokens.border, RoundedCornerShape(6.dp))
                                    .clickable { onDeviceModelChange(m) }
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = m,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelModel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelModel) tokens.primary else tokens.text2
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. HİZMET & MALZEME KALEMLERİ
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = tokens.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "2. Hizmet & Malzeme Kalemleri",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = tokens.text
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(tokens.greenC)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("${items.size} Kalem", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tokens.green)
                    }
                }

                if (catalogItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Hazır Hizmet / Malzeme Seç:", fontSize = 11.sp, color = tokens.text2)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        catalogItems.filter { it.active }.take(6).forEach { catItem ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(tokens.surface2)
                                    .border(1.dp, tokens.border, RoundedCornerShape(8.dp))
                                    .clickable {
                                        items.add(
                                            ProposalItem(
                                                id = UUID.randomUUID().toString(),
                                                title = catItem.name,
                                                quantity = 1,
                                                unitPrice = catItem.defaultPrice
                                            )
                                        )
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "+ ${catItem.name} (₺${catItem.defaultPrice.toInt()})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = tokens.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Kalem Satırları
                items.forEachIndexed { index, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.dp, tokens.border)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. Kalem",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.text2
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (items.size > 1) {
                                    IconButton(
                                        onClick = { items.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Sil",
                                            tint = tokens.red,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = item.title,
                                onValueChange = { newTitle ->
                                    items[index] = item.copy(title = newTitle)
                                },
                                label = { Text("Ürün / Hizmet Açıklaması *") },
                                placeholder = { Text("Örn: Kombi bakım & petek temizliği") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = if (item.quantity == 0) "" else item.quantity.toString(),
                                    onValueChange = { qtyStr ->
                                        val qty = qtyStr.toIntOrNull() ?: 0
                                        items[index] = item.copy(quantity = qty)
                                    },
                                    label = { Text("Adet") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )

                                OutlinedTextField(
                                    value = if (item.unitPrice == 0.0) "" else item.unitPrice.toString(),
                                    onValueChange = { priceStr ->
                                        val price = parseLocalizedDouble(priceStr) ?: 0.0
                                        items[index] = item.copy(unitPrice = price)
                                    },
                                    label = { Text("Birim Fiyat (TL)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1.5f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Kalem Toplamı: ${formatProposalCurrency(item.totalPrice)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = tokens.primary,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, tokens.primary, RoundedCornerShape(10.dp))
                        .background(tokens.primaryC)
                        .clickable {
                            items.add(ProposalItem(UUID.randomUUID().toString(), "", 1, 0.0))
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(16.dp))
                        Text("+ Yeni Kalem Ekle", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tokens.primary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fiyat Özeti
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.surface2)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ara Toplam:", fontSize = 12.sp, color = tokens.text2)
                        Text(formatProposalCurrency(subtotal), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = tokens.text)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("İskonto / İndirim (TL):", fontSize = 12.sp, color = tokens.text2)
                        OutlinedTextField(
                            value = discount,
                            onValueChange = onDiscountChange,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.width(100.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(tokens.border))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("GENEL TOPLAM (KDV Dahil):", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = tokens.text)
                        Text(
                            formatProposalCurrency(grandTotal),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tokens.green
                        )
                    }
                }
            }
        }

        // 3. ÖDEME KOŞULLARI & TEKLİF NOTLARI
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = tokens.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3. Ödeme Koşulları & Teklif Notları",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = tokens.text
                )
                Text(
                    text = "İskonto, peşinat, kalan bakiye ödeme şekli ve garanti koşullarını belirleyin.",
                    fontSize = 11.sp,
                    color = tokens.text2
                )

                Spacer(modifier = Modifier.height(14.dp))

                // İskonto / İndirim Tutarı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("İskonto / İndirim Tutarı", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tokens.text2)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        QUICK_DISCOUNT_OPTIONS.forEach { (valStr, label) ->
                            val isReset = valStr == "0"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isReset) tokens.redC else tokens.surface2)
                                    .border(1.dp, if (isReset) tokens.red.copy(alpha = 0.5f) else tokens.border, RoundedCornerShape(6.dp))
                                    .clickable {
                                        if (valStr == "250") {
                                            onDiscountChange("250")
                                        } else if (valStr == "0") {
                                            onDiscountChange("0")
                                        } else {
                                            val pct = valStr.toDoubleOrNull() ?: 0.0
                                            val calc = kotlin.math.round((subtotal * pct / 100.0)).toInt()
                                            onDiscountChange(calc.toString())
                                        }
                                    }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isReset) tokens.red else tokens.text
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = discount,
                    onValueChange = onDiscountChange,
                    label = { Text("İndirim Tutarı (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Peşinat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Alınan / Talep Edilen Peşinat", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tokens.text2)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        QUICK_DOWN_PAYMENT_PERCENTAGES.forEach { (pctStr, label) ->
                            val isZero = pctStr == "0"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isZero) tokens.redC else tokens.surface2)
                                    .border(1.dp, if (isZero) tokens.red.copy(alpha = 0.5f) else tokens.border, RoundedCornerShape(6.dp))
                                    .clickable {
                                        val pct = pctStr.toDoubleOrNull() ?: 0.0
                                        val calc = kotlin.math.round((grandTotal * pct / 100.0)).toInt()
                                        onDownPaymentChange(calc.toString())
                                    }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isZero) tokens.red else tokens.text
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = downPayment,
                    onValueChange = onDownPaymentChange,
                    label = { Text("Peşinat Tutarı (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Kalan Bakiye Ödeme Yolu (Müşteri Şartı) - 4 Şık Kart
                Text("Kalan Bakiye Ödeme Yolu (Müşteri Şartı)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tokens.text2)
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val pairs = PAYMENT_METHODS.chunked(2)
                    pairs.forEach { rowMethods ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            rowMethods.forEach { (method, desc) ->
                                val isSel = remainingPaymentType.equals(method, ignoreCase = true)
                                val iconStr = when {
                                    method.contains("Nakit", ignoreCase = true) -> "💵"
                                    method.contains("Tek Çekim", ignoreCase = true) -> "💳"
                                    method.contains("Taksit", ignoreCase = true) -> "📑"
                                    else -> "🏦"
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) tokens.primaryC else tokens.surface2)
                                        .border(
                                            width = if (isSel) 1.5.dp else 1.dp,
                                            color = if (isSel) tokens.primary else tokens.border,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { onRemainingPaymentTypeChange(method) }
                                        .padding(horizontal = 10.dp, vertical = 9.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "$iconStr $method",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isSel) tokens.primary else tokens.text
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 9.sp,
                                            color = tokens.text2,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            if (rowMethods.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Teklif Geçerlilik Süresi
                Text("Teklif Geçerlilik Süresi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tokens.text2)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    VALIDITY_DAYS.forEach { dayStr ->
                        val isSel = validityDays == dayStr
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) tokens.primaryC else tokens.surface2)
                                .border(
                                    width = if (isSel) 1.5.dp else 1.dp,
                                    color = if (isSel) tokens.primary else tokens.border,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onValidityDaysChange(dayStr) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayStr,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) tokens.primary else tokens.text
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = validUntilDate,
                    onValueChange = onValidUntilDateChange,
                    label = { Text("Son Geçerlilik Tarihi") },
                    placeholder = { Text("gg.aa.yyyy") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = preparedBy,
                    onValueChange = onPreparedByChange,
                    label = { Text("Teklifi Hazırlayan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Şartlar ve Hızlı Şablon Butonları
                Text("Not / Şartlar & Garantiler", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tokens.text2)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CONDITION_TEMPLATES.forEach { (chipLabel, templateText) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(tokens.surface2)
                                .border(1.dp, tokens.border, RoundedCornerShape(8.dp))
                                .clickable {
                                    val newNote = if (note.isBlank()) templateText else "$note • $templateText"
                                    onNoteChange(newNote)
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(chipLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = tokens.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = { Text("Teklif Notları & Şartlar") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}
