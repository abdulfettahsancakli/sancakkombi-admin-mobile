package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Proposal
import com.example.data.model.ProposalItem
import com.example.data.model.ProposalStatus
import com.example.data.model.CatalogItem
import com.example.utils.parseLocalizedDouble
import java.text.SimpleDateFormat
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

    var downPayment by remember { mutableStateOf("") }
    var remainingPaymentType by remember { mutableStateOf("") }
    var validUntilDate by remember { mutableStateOf("") }
    var preparedBy by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
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
        color = MaterialTheme.colorScheme.background
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
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Geri")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Yeni Teklif",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Müşteriye verilecek fiyat teklifini oluşturun.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Müşteri Bilgileri Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Müşteri Bilgileri", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Ad Soyad *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("Telefon") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = customerEmail,
                            onValueChange = { customerEmail = it },
                            label = { Text("E-posta") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customerDistrict,
                        onValueChange = { customerDistrict = it },
                        label = { Text("İlçe") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customerAddress,
                        onValueChange = { customerAddress = it },
                        label = { Text("Adres") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = deviceBrand,
                            onValueChange = { deviceBrand = it },
                            label = { Text("Cihaz Marka") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = deviceModel,
                            onValueChange = { deviceModel = it },
                            label = { Text("Cihaz Model") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Teklif Kalemleri Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Teklif Kalemleri", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    if (catalogItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Hazır hizmet / ürün fiyatları", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            catalogItems.filter { it.active }.take(4).forEach { catalogItem ->
                                OutlinedButton(
                                    onClick = { items.add(ProposalItem(UUID.randomUUID().toString(), catalogItem.name, 1, catalogItem.defaultPrice)) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("${catalogItem.name}\n₺%.0f".format(catalogItem.defaultPrice), fontSize = 10.sp, maxLines = 2)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    items.forEachIndexed { index, item ->
                        var title by remember(item) { mutableStateOf(item.title) }
                        var qtyStr by remember(item) { mutableStateOf(item.quantity.toString()) }
                        var priceStr by remember(item) { mutableStateOf(item.unitPrice.toString()) }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = {
                                        title = it
                                        items[index] = items[index].copy(title = it)
                                    },
                                    label = { Text("Ürün / Hizmet") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )

                                if (items.size > 1) {
                                    IconButton(
                                        onClick = { items.removeAt(index) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Sil", tint = Color(0xFFEF4444))
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                OutlinedTextField(
                                    value = qtyStr,
                                    onValueChange = {
                                        qtyStr = it
                                        val q = it.toIntOrNull() ?: 1
                                        items[index] = items[index].copy(quantity = q)
                                    },
                                    label = { Text("Adet") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(0.4f),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = priceStr,
                                    onValueChange = {
                                        priceStr = it
                                        val p = parseLocalizedDouble(it) ?: 0.0
                                        items[index] = items[index].copy(unitPrice = p)
                                    },
                                    label = { Text("Birim Fiyat") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(0.6f),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            items.add(ProposalItem(UUID.randomUUID().toString(), "", 1, 0.0))
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Kalem Ekle")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Ara Toplam", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("₺%.2f".format(subtotal).replace(".", ","), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("İskonto", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.weight(1f))
                        OutlinedTextField(
                            value = discount,
                            onValueChange = { discount = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(100.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Genel Toplam", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("₺%.2f".format(grandTotal).replace(".", ","), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ödeme Planı Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Ödeme Planı", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = downPayment,
                        onValueChange = { downPayment = it },
                        label = { Text("Peşinat Tutarı") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = remainingPaymentType,
                        onValueChange = { remainingPaymentType = it },
                        label = { Text("Kalan Ödeme Sekli") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ek Bilgiler Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Ek Bilgiler", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = validUntilDate,
                        onValueChange = { validUntilDate = it },
                        label = { Text("Geçerlilik Tarihi (gg.aa.yyyy)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = preparedBy,
                        onValueChange = { preparedBy = it },
                        label = { Text("Teklifi Hazırlayan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Not / Şartlar") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (canCreateProposal) {
                        val newProposal = Proposal(
                            id = "TF-202608-" + UUID.randomUUID().toString().take(6).uppercase(),
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
                        Toast.makeText(context, "Yeni Teklif Oluşturuldu", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Müşteri ve en az bir geçerli ürün/hizmet girin", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = canCreateProposal,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("Teklifi Oluştur", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
