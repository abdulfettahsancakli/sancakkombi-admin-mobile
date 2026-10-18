package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
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
                    Toast.makeText(context, "PDF indirilemedi (Hata kodu: $responseCode)", Toast.LENGTH_LONG).show()
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
    val cloudWhatsApp = LocalCloudWhatsAppActions.current
    val tokens = rememberProposalTokens()
    val coroutineScope = rememberCoroutineScope()
    var isDownloadingPdf by remember { mutableStateOf(false) }

    if (proposal == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Teklif bulunamadı.", color = tokens.text2)
        }
        return
    }

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
            // Header Top Bar
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
                    Text("Teklifler", fontWeight = FontWeight.Bold)
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
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.primary, contentColor = tokens.onPrimary),
                    modifier = Modifier.testTag("share_proposal_pdf_button")
                ) {
                    if (isDownloadingPdf) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = tokens.onPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("İndiriliyor...", fontSize = 12.sp)
                    } else {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PDF Paylaş", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            ProposalDetailContent(
                proposal = proposal,
                tokens = tokens,
                onUpdateStatus = onUpdateStatus,
                onSharePdf = {
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
                isDownloadingPdf = isDownloadingPdf
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ProposalDetailContent(
    proposal: Proposal,
    tokens: ProposalTokens,
    onUpdateStatus: (ProposalStatus) -> Unit,
    onSharePdf: () -> Unit,
    isDownloadingPdf: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cloudWhatsApp = LocalCloudWhatsAppActions.current
    val visual = getProposalStatusVisual(proposal.status, tokens)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Durum Onay Çubuğu
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = tokens.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Müşteri Onayı:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tokens.text2)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(visual.bgColor)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(visual.label, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = visual.textColor)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { onUpdateStatus(ProposalStatus.APPROVED) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (proposal.status == ProposalStatus.APPROVED) tokens.primary else tokens.primaryC,
                            contentColor = if (proposal.status == ProposalStatus.APPROVED) tokens.onPrimary else tokens.primary
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("✓ Kabul Edildi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onUpdateStatus(ProposalStatus.REJECTED) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (proposal.status == ProposalStatus.REJECTED) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.2f),
                            contentColor = if (proposal.status == ProposalStatus.REJECTED) Color.White else Color(0xFFEF4444)
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("✕ Reddedildi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Hızlı İletişim Aksiyon Butonları (WhatsApp, Arama, PDF)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.primaryC)
                    .border(1.dp, tokens.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable { sendProposalWhatsApp(context, proposal, cloudWhatsApp) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "WhatsApp ile İlet",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.primary
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.primaryC)
                    .border(1.dp, tokens.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable { dialProposalPhone(context, proposal.customerPhone) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(14.dp))
                    Text("Ara", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = tokens.primary)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.surface2)
                    .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
                    .clickable { onSharePdf() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = tokens.text, modifier = Modifier.size(14.dp))
                    Text("PDF İndir", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tokens.text)
                }
            }
        }

        // RESMİ A4 PDF GÖRÜNÜMÜ KARTI (Tasarım Prototipindeki gibi A4 Belge Görünümü)
        ProposalDocumentCard(proposal = proposal)
    }
}

@Composable
fun ProposalDocumentCard(
    proposal: Proposal,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Belge Üst Anteti (Logo + Başlık + Teklif No)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(com.example.R.drawable.logo_sancakkombi),
                        contentDescription = "Sancak Kombi Logo",
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = "SANCAK",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "KOMBİ",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFDC2626)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF0F172A))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TEKNİK SERVİS",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FİYAT TEKLİFİ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "GÜVENLİK • KALİTE • HİZMET",
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TEKLİF NO",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = proposal.quoteNumber.ifBlank { proposal.id },
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFDC2626)
                    )
                    Text(
                        text = proposal.date,
                        fontSize = 8.5.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // İletişim Şeridi
            Text(
                text = "Kocatepe, Şehir Parkı Cad. No:113 Bayrampaşa / İstanbul • Tel: 0212 581 75 74 • info@sancakkombi.com.tr • sancakkombi.com.tr",
                fontSize = 7.5.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)

            // 2 Sütunlu MÜŞTERİ BİLGİLERİ ve KURUM BİLGİLERİ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sol Sütun: MÜŞTERİ BİLGİLERİ
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF9FAFB))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "MÜŞTERİ BİLGİLERİ",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Ad Soyad: ${proposal.customerName}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    if (proposal.customerPhone.isNotBlank()) {
                        Text(
                            text = "Telefon: ${proposal.customerPhone}",
                            fontSize = 9.sp,
                            color = Color(0xFF475569)
                        )
                    }
                    if (proposal.customerAddress.isNotBlank()) {
                        Text(
                            text = "Adres: ${proposal.customerAddress}",
                            fontSize = 8.5.sp,
                            color = Color(0xFF475569),
                            maxLines = 2
                        )
                    }
                    val dev = listOf(proposal.deviceBrand, proposal.deviceModel).filter { it.isNotBlank() }.joinToString(" ")
                    if (dev.isNotBlank()) {
                        Text(
                            text = "Cihaz: $dev",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }

                // Sağ Sütun: KURUM BİLGİLERİ
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF9FAFB))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "KURUM BİLGİLERİ",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Firma: Sancak Kombi Isıtma Sistemleri",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Yetkili: ${proposal.preparedBy.ifBlank { "Sancak Kombi Yetkili Servisi" }}",
                        fontSize = 8.5.sp,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "Telefon: 0212 581 75 74",
                        fontSize = 8.5.sp,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "Adres: Bayrampaşa, İstanbul",
                        fontSize = 8.5.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            // Teklif Detayları & Kalemleri
            Text(
                text = "TEKLİF DETAYLARI & HİZMET KALEMLERİ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            if (proposal.items.isEmpty()) {
                Text(
                    text = "Belirtilen kalem bulunamadı.",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    proposal.items.forEachIndexed { i, item ->
                        val bgRow = if (i % 2 == 0) Color(0xFFF9FAFB) else Color(0xFFFFFFFF)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(bgRow)
                                .padding(horizontal = 8.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${i + 1}.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.width(22.dp)
                            )
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                color = Color(0xFF111827),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${item.quantity} Adet",
                                fontSize = 11.sp,
                                color = Color(0xFF4B5563),
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            Text(
                                text = formatProposalCurrency(item.totalPrice),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }
                    }
                }
            }

            // Genel Toplam Kutusu
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF9FAFB))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (proposal.discount > 0.0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ara Toplam:", fontSize = 11.sp, color = Color(0xFF6B7280))
                        Text(formatProposalCurrency(proposal.subtotal), fontSize = 11.sp, color = Color(0xFF374151))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("İskonto:", fontSize = 11.sp, color = Color(0xFFDC2626))
                        Text("-${formatProposalCurrency(proposal.discount)}", fontSize = 11.sp, color = Color(0xFFDC2626))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GENEL TOPLAM (KDV DAHİL)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF374151)
                    )
                    Text(
                        text = formatProposalCurrency(proposal.grandTotal),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0284C7)
                    )
                }

                if (proposal.downPayment > 0.0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Alınan Peşinat:", fontSize = 11.sp, color = Color(0xFF6B7280))
                        Text(formatProposalCurrency(proposal.downPayment), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Kalan Bakiye (${proposal.remainingPaymentType}):", fontSize = 11.sp, color = Color(0xFF6B7280))
                        Text(formatProposalCurrency(proposal.remainingAmount), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    }
                }
            }

            // Teklif Şartları & Notlar
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "TEKLİF ŞARTLARI & GARANTİ",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = "• Teklifimiz ${proposal.validUntilDate.ifBlank { "15 gün" }} geçerlidir.",
                    fontSize = 10.sp,
                    color = Color(0xFF374151)
                )
                Text(
                    text = "• Montaj ve işçilik Sancak Kombi güvencesiyle 1 yıl garantilidir.",
                    fontSize = 10.sp,
                    color = Color(0xFF374151)
                )
                if (proposal.note.isNotBlank()) {
                    Text(
                        text = "• Not: ${proposal.note}",
                        fontSize = 10.sp,
                        color = Color(0xFF374151)
                    )
                }
                if (proposal.preparedBy.isNotBlank()) {
                    Text(
                        text = "Teklifi Hazırlayan: ${proposal.preparedBy}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
