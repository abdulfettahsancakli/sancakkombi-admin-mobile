package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.BankAccount
import com.example.data.model.FinanceRecord
import com.example.data.model.FinanceSummary
import com.example.data.model.FinanceType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun FinanceScreen(
    summary: FinanceSummary,
    financeRecords: List<FinanceRecord>,
    bankAccounts: List<BankAccount>,
    onBackClick: () -> Unit,
    onAddFinanceRecord: (FinanceRecord) -> Unit,
    onUpdateBankAccounts: (List<BankAccount>) -> Unit,
    onViewReceipt: (FinanceRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Form fields for new Gelir/Gider entry
    var gelirTutar by remember { mutableStateOf("") }
    var gelirKategori by remember { mutableStateOf("Servis") }
    var gelirOdemeYontemi by remember { mutableStateOf("Nakit") }
    var gelirNot by remember { mutableStateOf("") }

    var giderTutar by remember { mutableStateOf("") }
    var giderKategori by remember { mutableStateOf("Malzeme Alımı") }
    var giderOdemeYontemi by remember { mutableStateOf("Nakit") }
    var giderTedarikci by remember { mutableStateOf("") }

    // Bank Accounts state for editing
    var acc1Iban by remember(bankAccounts) { mutableStateOf(bankAccounts.getOrNull(0)?.iban ?: "") }
    var acc2Iban by remember(bankAccounts) { mutableStateOf(bankAccounts.getOrNull(1)?.iban ?: "") }
    var acc3Iban by remember(bankAccounts) { mutableStateOf(bankAccounts.getOrNull(2)?.iban ?: "") }

    val currentDateStr = remember {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
    }

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
                text = "Finans",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Randevu bazlı tahsilat, açık alacak ve manuel gider kayıtları.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Cards Row
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Toplam Gelir Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Toplam Gelir",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₺%.2f".format(summary.totalIncome).replace(".", ","),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(text = "10 kayıt", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Toplam Gider Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFEF4444))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Toplam Gider",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₺%.2f".format(summary.totalExpense).replace(".", ","),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(text = "1 kayıt", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Açık Alacak Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFF59E0B))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Açık Alacak",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₺%.2f".format(summary.outstandingReceivable).replace(".", ","),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(text = "1 bekliyor", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alacak Takip Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Alacak Takip",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF92400E)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "1 bekliyor",
                            fontSize = 11.sp,
                            color = Color(0xFFB45309),
                            modifier = Modifier
                                .background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fettah Sancaklı",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF78350F)
                            )
                            Text(
                                text = "Vade: 18.05.2026 • GECİKMİŞ • Kısmi",
                                fontSize = 11.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                        Text(
                            text = "₺500,00",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // IBAN Hesapları Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "IBAN Hesapları",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Account 1
                    Text(text = "Fatih Sancaklı", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = acc1Iban,
                        onValueChange = { acc1Iban = it },
                        label = { Text("YAPI KREDİ - Fatih Sancaklı") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Account 2
                    Text(text = "Abdulfettah Sancaklı", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = acc2Iban,
                        onValueChange = { acc2Iban = it },
                        label = { Text("AKBANK - Abdulfettah Sancaklı") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Account 3
                    Text(text = "Abdullah Sancaklı", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = acc3Iban,
                        onValueChange = { acc3Iban = it },
                        label = { Text("KUVEYT TÜRK - Abdullah Sancaklı") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val updated = listOf(
                                BankAccount("b1", "Fatih Sancaklı", "Fatih Sancaklı", "YAPI KREDİ", acc1Iban),
                                BankAccount("b2", "Kart Başlığı", "Abdulfettah Sancaklı", "AKBANK", acc2Iban),
                                BankAccount("b3", "Kart Başlığı", "Abdullah Sancaklı", "KUVEYT TÜRK", acc3Iban)
                            )
                            onUpdateBankAccounts(updated)
                            Toast.makeText(context, "IBAN Bilgileri Kaydedildi", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("IBAN Bilgilerini Kaydet", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Income / Expense Entry Forms
            // Gelir Kaydet Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Manuel Gelir Kaydı", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = gelirTutar,
                        onValueChange = { gelirTutar = it },
                        label = { Text("Tutar (TL)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = gelirNot,
                        onValueChange = { gelirNot = it },
                        label = { Text("Müşteri / Açıklama Notu") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val valAmt = gelirTutar.toDoubleOrNull() ?: 0.0
                            if (valAmt > 0) {
                                val rec = FinanceRecord(
                                    id = UUID.randomUUID().toString(),
                                    date = currentDateStr,
                                    type = FinanceType.GELIR,
                                    amount = valAmt,
                                    status = "Ödendi",
                                    source = if (gelirNot.isNotBlank()) gelirNot else "Gelir Kaydı",
                                    receiptNo = "SK-202608-" + UUID.randomUUID().toString().take(6).uppercase()
                                )
                                onAddFinanceRecord(rec)
                                gelirTutar = ""
                                gelirNot = ""
                                Toast.makeText(context, "Gelir kaydı eklendi", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Gelir Kaydet", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Gider Kaydet Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.TrendingDown, contentDescription = null, tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Manuel Gider Kaydı", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = giderTutar,
                        onValueChange = { giderTutar = it },
                        label = { Text("Tutar (TL)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = giderTedarikci,
                        onValueChange = { giderTedarikci = it },
                        label = { Text("Tedarikçi / Açıklama (opsiyonel)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val valAmt = giderTutar.toDoubleOrNull() ?: 0.0
                            if (valAmt > 0) {
                                val rec = FinanceRecord(
                                    id = UUID.randomUUID().toString(),
                                    date = currentDateStr,
                                    type = FinanceType.GIDER,
                                    amount = valAmt,
                                    status = "Ödendi",
                                    source = if (giderTedarikci.isNotBlank()) giderTedarikci else "Gider Kaydı",
                                    receiptNo = "SK-202608-" + UUID.randomUUID().toString().take(6).uppercase()
                                )
                                onAddFinanceRecord(rec)
                                giderTutar = ""
                                giderTedarikci = ""
                                Toast.makeText(context, "Gider kaydı eklendi", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Gider Kaydet", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Son Finans Kayıtları Table / List
            Text(
                text = "Son Finans Kayıtları (${financeRecords.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            financeRecords.forEach { rec ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rec.date,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Type badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (rec.type == FinanceType.GELIR) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = rec.type.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (rec.type == FinanceType.GELIR) Color(0xFF065F46) else Color(0xFF991B1B)
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = "${if (rec.type == FinanceType.GELIR) "+" else "-"}₺%.2f".format(rec.amount).replace(".", ","),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (rec.type == FinanceType.GELIR) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rec.source,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedButton(
                                onClick = { onViewReceipt(rec) },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Makbuz", fontSize = 11.sp)
                            }
                        }

                        if (rec.note.isNotBlank()) {
                            Text(
                                text = "Not: ${rec.note}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
