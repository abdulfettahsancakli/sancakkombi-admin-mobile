package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WhatsAppStatus

@Composable
fun WhatsAppStatusScreen(
    status: WhatsAppStatus,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = status.isConnected

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("whatsapp_status_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("btn_back_whatsapp")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "WhatsApp Bağlantı Durumu",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Meta Embedded Signup WhatsApp Business API entegrasyonu.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Main Status Card Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isConnected) Color(0xFF22C55E).copy(alpha = 0.08f) else Color(0xFFEF4444).copy(alpha = 0.08f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isConnected) Color(0xFF22C55E) else Color(0xFFEF4444),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isConnected) Icons.Default.Verified else Icons.Default.LinkOff,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isConnected) "WhatsApp Bağlantısı Aktif" else "WhatsApp Bağlantısı Bulunmuyor",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isConnected) Color(0xFF16A34A) else Color(0xFFDC2626)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isConnected) status.phoneNumber ?: "" else "Sistemde bağlı bir WhatsApp Business hesabı bulunmuyor.",
                                fontSize = 13.sp,
                                fontWeight = if (isConnected) FontWeight.SemiBold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (isConnected && !status.businessName.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color(0xFF22C55E).copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("İşletme Adı:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(status.businessName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Details Section (If Connected)
        if (isConnected) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PhonelinkSetup, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WABA Entegrasyon Detayları", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        DetailRow(label = "WABA Hesap ID", value = status.wabaAccountId ?: "-")
                        DetailRow(label = "Bağlantı Tarihi", value = status.connectedAt ?: "-")
                        DetailRow(label = "Numara Onay Durumu", value = status.displayPhoneNumberStatus ?: "-", valueColor = Color(0xFF22C55E))
                        DetailRow(label = "Hesap Kalite Skoru", value = status.qualityRating ?: "-", icon = Icons.Default.Shield)
                        DetailRow(label = "24 Saatlik Mesaj Limiti", value = status.messagingLimit ?: "-", icon = Icons.Default.Speed)
                        DetailRow(label = "Webhook Durumu", value = status.webhookStatus ?: "-", isLast = true)
                    }
                }
            }
        }

        // Informational Note Card (Always shown - highlights web-only setup)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "WhatsApp Bağlantı Bilgilendirmesi",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Meta Embedded Signup (Facebook JS SDK pop-up) akışı güvenlik standartları gereği yalnızca Web Yönetim Panelinde desteklenmektedir.\n\nYeni numara bağlama, numara değiştirme veya entegrasyon ayarlarını güncellemek için lütfen masaüstü/web tarayıcınızdan Web Admin Paneline giriş yapınız.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isLast: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
        if (!isLast) {
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        }
    }
}
