package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.FinanceRecord

@Composable
fun ServiceReceiptScreen(
    record: FinanceRecord?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val receiptNo = record?.receiptNo ?: "SK-202606-6A6F7A"
    val customerName = record?.source ?: "Müşteri"
    val dateStr = record?.date ?: "5 Haziran 2026 | 18:44"
    val amountStr = "₺%.2f".format(record?.amount ?: 1000.0).replace(".", ",")

    fun shareReceiptPdf() {
        val shareText = """
            SANCAK KOMBİ TEKNİK SERVİS - SERVİS FİŞİ
            Fiş No: $receiptNo
            Tarih: $dateStr
            Müşteri: $customerName
            Tutar: $amountStr
            Durum: Ödendi / İşlem Tamamlandı
            
            Doğrulama Kodu: $receiptNo
            İletişim: 0212 581 75 74 • www.sancakkombi.com.tr
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Servis Fişini Paylaş (PDF/Metin)")
        context.startActivity(shareIntent)
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
            // Header Top Bar with PDF Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    onClick = { shareReceiptPdf() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Servis Fişi PDF", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                                text = "TEKNİK SERVİS",
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
                                text = "SERVİS FİŞİ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                            Text(
                                text = "SERVİS FİŞİ NO",
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = receiptNo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFDC2626)
                            )
                            Text(
                                text = dateStr,
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray)

                    // Customer & Device Info
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
                                Text("0541 328 06 98", fontSize = 11.sp, color = Color.DarkGray)
                                Text("Bayrampaşa / İstanbul", fontSize = 11.sp, color = Color.Gray)
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
                                Text("Demirdöküm Nitromix", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Hermetik Yoğuşmalı", fontSize = 11.sp, color = Color.DarkGray)
                                Text("Kombi Bakım & Onarım", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Servis Durumu Badge
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SERVİS DURUMU: İşlem Tamamlandı", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Yapılan İşlem / Technician Report
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("YAPILAN İŞLEM / TEKNİSYEN RAPORU", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Kombi genleşme tankı hava basıncı kontrol edildi. Ateşleyici elektrot temizliği ve O-ring conta değişimi yapıldı. Sızdırmazlık testi başarıyla tamamlandı.",
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Ödeme Bilgileri
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("ÖDEME BİLGİLERİ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Genel Toplam", fontSize = 12.sp, color = Color.Black)
                                Spacer(modifier = Modifier.weight(1f))
                                Text(amountStr, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("KDV Dahil", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.weight(1f))
                                Text(amountStr, fontSize = 11.sp, color = Color.Gray)
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Ödeme Yöntemi", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("Nakit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Ödeme Durumu", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("Ödendi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Code & Validation
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
                            modifier = Modifier.size(54.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("SERVİS FİŞİ DOĞRULAMA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("QR Kodu okutarak servisi doğrulayabilirsiniz", fontSize = 10.sp, color = Color.Gray)
                            Text("KOD: $receiptNo", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Sancak Kombi Isıtma Sistemleri • Bayrampaşa, İstanbul • 0212 581 75 74 • www.sancakkombi.com.tr\nBu servis fişi elektronik ortamda düzenlenmiştir.",
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
