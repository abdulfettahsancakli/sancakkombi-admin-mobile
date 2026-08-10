package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.Proposal
import com.example.data.model.ProposalStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

suspend fun downloadAndShareQuotePdf(
    context: Context,
    quoteId: String,
    quoteNumber: String,
    onLoadingStateChange: (Boolean) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            withContext(Dispatchers.Main) { onLoadingStateChange(true) }

            val pdfUrl = "https://www.sancakkombi.com/api/quote/$quoteId"
            val url = URL(pdfUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val pdfDir = File(context.cacheDir, "pdf")
                if (!pdfDir.exists()) {
                    pdfDir.mkdirs()
                }
                val safeQuoteNumber = quoteNumber.ifBlank { quoteId }.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                val pdfFile = File(pdfDir, "Teklif_${safeQuoteNumber}.pdf")

                connection.inputStream.use { input ->
                    pdfFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    pdfFile
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooserIntent = Intent.createChooser(shareIntent, "Fiyat Teklifi PDF'ini Paylaş")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                withContext(Dispatchers.Main) {
                    onLoadingStateChange(false)
                    context.startActivity(chooserIntent)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onLoadingStateChange(false)
                    Toast.makeText(context, "PDF indirilemedi (Hata koda: $responseCode)", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onLoadingStateChange(false)
                Toast.makeText(context, "PDF indirilirken hata oluştu: ${e.localizedMessage ?: "Bağlantı hatası"}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Composable
fun ProposalDetailScreen(
    proposal: Proposal?,
    onBackClick: () -> Unit,
    onUpdateStatus: (ProposalStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isDownloadingPdf by remember { mutableStateOf(false) }

    if (proposal == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Teklif bulunamadı.")
        }
        return
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
            // Header Top Bar
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

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (!isDownloadingPdf) {
                            coroutineScope.launch {
                                downloadAndShareQuotePdf(
                                    context = context,
                                    quoteId = proposal.id,
                                    quoteNumber = proposal.quoteNumber.ifEmpty { proposal.id },
                                    onLoadingStateChange = { isDownloadingPdf = it }
                                )
                            }
                        }
                    },
                    enabled = !isDownloadingPdf,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("share_proposal_pdf_button")
                ) {
                    if (isDownloadingPdf) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("İndiriliyor...", fontSize = 12.sp)
                    } else {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Fiyat Teklifi PDF", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Changer Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Müşteri Onayı:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { onUpdateStatus(ProposalStatus.APPROVED) },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kabul Edildi", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = { onUpdateStatus(ProposalStatus.REJECTED) },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reddedildi", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Paper Document Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header Area
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SANCAK KOMBİ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFFDC2626)
                            )
                            Text(
                                text = "TEKNİK SERVİS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                            Text("GÜVENLİK • KALİTE • HİZMET", fontSize = 9.sp, color = Color.Gray)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "FİYAT TEKLİFİ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                            Text("Teklif No: ${proposal.quoteNumber.ifEmpty { proposal.id }}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                            Text("Tarih: ${proposal.date}", fontSize = 10.sp, color = Color.Gray)
                            Text("Hazırlayan: ${proposal.preparedBy}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray)

                    // Customer Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("TEKLİF VERİLEN MÜŞTERİ BİLGİLERİ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Sayın ${proposal.customerName}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            if (proposal.customerPhone.isNotBlank()) Text("Tel: ${proposal.customerPhone}", fontSize = 11.sp, color = Color.DarkGray)
                            if (proposal.customerAddress.isNotBlank()) Text("Adres: ${proposal.customerAddress}", fontSize = 11.sp, color = Color.Gray)
                            if (proposal.deviceBrand.isNotBlank()) Text("Cihaz: ${proposal.deviceBrand} ${proposal.deviceModel}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("TEKLİF DETAYLARI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Line Items Table
                    proposal.items.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (index % 2 == 0) Color(0xFFF9FAFB) else Color.White)
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${index + 1}.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.width(24.dp))
                            Text(item.title, fontSize = 12.sp, color = Color.Black, modifier = Modifier.weight(1f))
                            Text("₺%.2f".format(item.totalPrice).replace(".", ","), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Totals & Payment Summary Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("ÖDEME VE TEKLİF ÖZETİ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Ara Toplam", fontSize = 12.sp, color = Color.Black)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("₺%.2f".format(proposal.subtotal).replace(".", ","), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            if (proposal.discount > 0) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text("İskonto", fontSize = 12.sp, color = Color(0xFFEF4444))
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("-₺%.2f".format(proposal.discount).replace(".", ","), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Peşinat (Nakit)", fontSize = 12.sp, color = Color.DarkGray)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("₺%.2f".format(proposal.downPayment).replace(".", ","), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Kalan Tutar", fontSize = 12.sp, color = Color.DarkGray)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("₺%.2f".format(proposal.remainingAmount).replace(".", ","), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Kalan Ödeme Şekli", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.weight(1f))
                                Text(proposal.remainingPaymentType, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.LightGray)

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("GENEL TOPLAM (KDV Dahil)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("₺%.2f".format(proposal.grandTotal).replace(".", ","), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Terms & Notes
                    Text("TEKLİF ŞARTLARI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("• Teklifimiz ${proposal.validUntilDate} tarihine kadar geçerlidir.", fontSize = 10.sp, color = Color.DarkGray)
                    Text("• Montaj ve işçilik Sancak Kombi güvencesiyle 1 yıl garantilidir.", fontSize = 10.sp, color = Color.DarkGray)
                    if (proposal.note.isNotBlank()) {
                        Text("• ${proposal.note}", fontSize = 10.sp, color = Color.DarkGray)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Sancak Kombi Isıtma Sistemleri • Bayrampaşa, İstanbul • 0212 581 75 74 • www.sancakkombi.com.tr",
                        fontSize = 9.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
