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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.FinanceRecord
import com.example.data.model.FinanceType
import com.example.data.remote.ReceiptDetailDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

suspend fun downloadAndShareReceiptOrGuaranteePdf(
    context: Context,
    pdfUrl: String,
    fileNamePrefix: String,
    documentId: String,
    title: String,
    onLoadingStateChange: (Boolean) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            withContext(Dispatchers.Main) { onLoadingStateChange(true) }

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
                val safeDocId = documentId.ifBlank { "dokuman" }.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                val pdfFile = File(pdfDir, "${fileNamePrefix}${safeDocId}.pdf")

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

                val chooserIntent = Intent.createChooser(shareIntent, "$title Paylaş")
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
fun ServiceReceiptScreen(
    record: FinanceRecord?,
    onBackClick: () -> Unit,
    onFetchReceiptDetail: (suspend (String) -> Result<ReceiptDetailDto>)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var receiptDetail by remember { mutableStateOf<ReceiptDetailDto?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isPdfDownloading by remember { mutableStateOf(false) }
    var activePdfType by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(record?.id) {
        val entryId = record?.id ?: ""
        if (entryId.isNotBlank() && onFetchReceiptDetail != null) {
            isLoading = true
            val res = onFetchReceiptDetail(entryId)
            if (res.isSuccess) {
                receiptDetail = res.getOrNull()
            }
            isLoading = false
        }
    }

    val isExpense = record?.type == FinanceType.GIDER || record?.source?.contains("Google Ads", ignoreCase = true) == true || receiptDetail?.serviceTitle?.contains("GİDER", ignoreCase = true) == true
    val isGoogleAds = record?.source?.contains("Google Ads", ignoreCase = true) == true || receiptDetail?.customerName?.contains("Google", ignoreCase = true) == true
    val hasDevice = (!receiptDetail?.deviceBrand.isNullOrBlank() || !receiptDetail?.deviceModel.isNullOrBlank()) && !isExpense

    val entryId = record?.id ?: receiptDetail?.entryId ?: ""
    val receiptNo = receiptDetail?.receiptNo?.ifBlank { null } ?: record?.receiptNo?.ifBlank { null } ?: (if (isGoogleAds) "ADS-${java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())}" else "SK-202608-6A6F7A")
    
    val customerName = if (isGoogleAds) {
        "Google Ads (Google Ireland Ltd.)"
    } else {
        receiptDetail?.customerName?.ifBlank { null } ?: record?.source?.ifBlank { null } ?: if (isExpense) "Tedarikçi / Kurum" else "Müşteri"
    }
    val customerPhone = if (isGoogleAds) "0850 390 20 60" else (receiptDetail?.customerPhone?.ifBlank { null } ?: if (isExpense) "-" else "0537 691 73 61")

    val district = receiptDetail?.customerDistrict?.trim() ?: ""
    val address = receiptDetail?.customerAddress?.trim() ?: ""
    val customerAddressStr = when {
        isGoogleAds -> "Gordon House, Barrow St, Dublin 4, İrlanda"
        district.isNotBlank() && address.isNotBlank() -> "$district / $address"
        district.isNotBlank() -> district
        address.isNotBlank() -> address
        isExpense -> "İşletme Gideri"
        else -> "Bayrampaşa / İstanbul"
    }

    val dateStr = receiptDetail?.date?.ifBlank { null } ?: record?.date?.ifBlank { null } ?: "17.08.2026"
    val amountVal = if ((receiptDetail?.amount ?: 0.0) > 0.0) receiptDetail!!.amount else (record?.amount ?: 0.0)
    val amountStr = "₺%.2f".format(amountVal).replace(".", ",")

    val deviceBrand = receiptDetail?.deviceBrand?.ifBlank { null } ?: ""
    val deviceModel = receiptDetail?.deviceModel?.ifBlank { null } ?: ""
    val workDescription = receiptDetail?.workDescription?.ifBlank { null }
        ?: if (record?.note?.isNotBlank() == true) record.note
        else if (isGoogleAds) "Google Ads arama ağı ve harita reklam harcaması (Günlük Senkronize Gider)"
        else if (isExpense) "İşletme gider ödemesi kaydı."
        else "Teknik servis ve tahsilat işlemi."

    val paymentMethod = if (isGoogleAds) "Otomatik Çekim (Kredi Kartı)" else (receiptDetail?.paymentMethod?.ifBlank { null } ?: "Banka / Kasa")
    val statusRaw = receiptDetail?.status?.ifBlank { null } ?: record?.status ?: "Ödendi"
    val statusText = when (statusRaw.lowercase()) {
        "paid", "ödendi" -> if (isExpense) "Muhasebeleştirildi (Ödendi)" else "İşlem Tamamlandı (Ödendi)"
        "partial", "kısmi" -> "Kısmi Ödendi"
        else -> "Ödeme Bekliyor"
    }
    val warrantyMonths = receiptDetail?.warrantyMonths ?: 12
    val serviceTitle = if (isGoogleAds) {
        "GOOGLE ADS REKLAM GİDER DEKONTU"
    } else if (isExpense) {
        "FİNANSAL GİDER / HARCAMA DEKONTU"
    } else {
        receiptDetail?.serviceTitle?.ifBlank { null } ?: "SERVİS & TAHSİLAT MAKBUZU"
    }

    fun sharePdf(type: String) { // "receipt" or "guarantee"
        val targetId = if (entryId.isNotBlank()) entryId else "default"
        val url = if (type == "guarantee") {
            "https://www.sancakkombi.com/api/guarantee/$targetId"
        } else {
            "https://www.sancakkombi.com/api/receipt/$targetId"
        }
        val title = if (type == "guarantee") "Garanti Belgesi PDF" else "Servis Fişi PDF"
        val prefix = if (type == "guarantee") "GarantiBelgesi_" else "ServisFisi_"

        activePdfType = type
        scope.launch {
            downloadAndShareReceiptOrGuaranteePdf(
                context = context,
                pdfUrl = url,
                fileNamePrefix = prefix,
                documentId = targetId,
                title = title,
                onLoadingStateChange = { isPdfDownloading = it }
            )
        }
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
            // Top Bar Header
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

                Text(
                    text = if (isGoogleAds) "Google Ads Gider Dekontu" else if (isExpense) "Gider / Harcama Dekontu" else "Makbuz / Servis Fişi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PDF Share Actions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "PDF DOKÜMAN PAYLAŞIMI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { sharePdf("receipt") },
                            enabled = !isPdfDownloading,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_receipt_pdf_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isGoogleAds) Color(0xFF2563EB) else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (isPdfDownloading && activePdfType == "receipt") {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isGoogleAds) "Google Ads Dekontu PDF" else if (isExpense) "Gider Dekontu PDF" else "Servis Fişi PDF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!isExpense && hasDevice) {
                            Button(
                                onClick = { sharePdf("guarantee") },
                                enabled = !isPdfDownloading,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("share_guarantee_pdf_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                            ) {
                                if (isPdfDownloading && activePdfType == "guarantee") {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Garanti Belgesi PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Loading Indicator
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Detaylar yükleniyor...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Main Paper Document Card
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
                    // Company Header & Document Title
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
                                text = if (isExpense) "FİNANS & MUHASEBE BİRİMİ" else "TEKNİK SERVİS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "GÜVENLİK • KALİTE • HİZMET",
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = serviceTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isGoogleAds) Color(0xFF2563EB) else Color.Black
                            )
                            Text(
                                text = if (isExpense) "DEKONT / REF NO" else "SERVİS FİŞİ NO",
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = receiptNo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isGoogleAds) Color(0xFF2563EB) else Color(0xFFDC2626)
                            )
                            Text(
                                text = dateStr,
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray)

                    // Details Section
                    if (isExpense) {
                        // Expense Details Block
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Payee info
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("GİDER KALEMİ / ALACAKLI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(customerName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(district.ifBlank { "İşletme Gideri" }, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (isGoogleAds) Color(0xFF2563EB) else Color.DarkGray)
                                    Text(customerAddressStr, fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            // Payment method & reference
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("ÖDEME DETAYI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(paymentMethod, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text("Tür: İşletme Gideri", fontSize = 11.sp, color = Color.DarkGray)
                                    Text("Tarih: $dateStr", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    } else if (hasDevice) {
                        // Service + Device Info Block
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Customer info block
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("MÜŞTERİ BİLGİLERİ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(customerName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(customerPhone, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2563EB))
                                    Text(customerAddressStr, fontSize = 11.sp, color = Color.Gray)
                                }
                            }

                            // Device info block
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("CİHAZ BİLGİLERİ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$deviceBrand $deviceModel", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text("Kombi Bakım & Onarım", fontSize = 11.sp, color = Color.DarkGray)
                                    Text("Garanti Süresi: $warrantyMonths Ay", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                }
                            }
                        }
                    } else {
                        // General Income / Customer block (Full Width)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("MÜŞTERİ & İŞLEM BİLGİLERİ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(customerName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                if (customerPhone != "-") {
                                    Text(customerPhone, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2563EB))
                                }
                                Text(customerAddressStr, fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Durum Badge
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isExpense) Color(0xFFEFF6FF) else Color(0xFFECFDF5)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isExpense) Color(0xFFBFDBFE) else Color(0xFFA7F3D0)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isExpense) Color(0xFF2563EB) else Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isExpense) "MUHASEBE DURUMU: $statusText" else "SERVİS DURUMU: $statusText",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isExpense) Color(0xFF1E40AF) else Color(0xFF065F46)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Açıklama / Rapor
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isExpense) "GİDER AÇIKLAMASI & MUHASEBE NOTU" else "YAPILAN İŞLEM / TEKNİSYEN RAPORU",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = workDescription,
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Ödeme / Harcama Bilgileri
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isExpense) "HARCAMA VE ÖDEME ÖZETİ" else "ÖDEME BİLGİLERİ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(if (isExpense) "Toplam Gider Tutarı" else "Genel Toplam", fontSize = 12.sp, color = Color.Black)
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = amountStr,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExpense) Color(0xFFDC2626) else Color(0xFF059669)
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Ödeme Yöntemi", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.weight(1f))
                                Text(paymentMethod, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Kayıt Durumu", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = statusText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExpense) Color(0xFF2563EB) else Color(0xFF10B981)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Code & Verification Block
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "QR Code",
                            modifier = Modifier.size(50.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isExpense) "GİDER DEKONTU DOĞRULAMA" else "SERVİS FİŞİ DOĞRULAMA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "Doğrulama Kodu: $receiptNo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGoogleAds) Color(0xFF2563EB) else Color(0xFFDC2626)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Sancak Kombi Isıtma Sistemleri • Bayrampaşa, İstanbul • 0212 581 75 74 • www.sancakkombi.com.tr\nBu belge elektronik ortamda düzenlenmiştir.",
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
