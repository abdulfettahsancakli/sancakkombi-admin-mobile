package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerMessagingSettings
import com.example.data.model.MessageJob
import com.example.data.model.MessageLog
import com.example.data.model.MessageTemplate
import com.example.data.model.MessagingStats
import com.example.data.model.StaffMessagingSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    stats: MessagingStats = MessagingStats(),
    customerSettings: CustomerMessagingSettings,
    staffSettings: StaffMessagingSettings,
    templates: List<MessageTemplate>,
    jobs: List<MessageJob> = emptyList(),
    logs: List<MessageLog> = emptyList(),
    onBackClick: () -> Unit,
    onUpdateCustomerSettings: (CustomerMessagingSettings) -> Unit,
    onUpdateStaffSettings: (StaffMessagingSettings) -> Unit,
    onUpdateTemplate: (MessageTemplate) -> Unit = {},
    onRetryJob: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(2) } // Varsayılan: Mesaj Şablonları sekmesi
    var templateCategoryFilter by remember { mutableStateOf("MUSTERI") } // "MUSTERI" or "USTA"

    // Local copy of customer settings
    var custActive by remember(customerSettings) { mutableStateOf(customerSettings.isNotificationsActive) }
    var custAutoCreated by remember(customerSettings) { mutableStateOf(customerSettings.autoSendOnAppointmentCreated) }
    var custAutoUpdated by remember(customerSettings) { mutableStateOf(customerSettings.autoSendOnAppointmentUpdated) }
    var custReminderHours by remember(customerSettings) { mutableStateOf(customerSettings.reminderHoursBefore.toString()) }
    var custAutoFeedback by remember(customerSettings) { mutableStateOf(customerSettings.autoSendFeedbackAfterAppointment) }
    var custFeedbackDays by remember(customerSettings) { mutableStateOf(customerSettings.feedbackDaysAfter.toString()) }

    // Local copy of staff settings
    var staffActive by remember(staffSettings) { mutableStateOf(staffSettings.isNotificationsActive) }
    var staffAutoDaily by remember(staffSettings) { mutableStateOf(staffSettings.autoSendDailyAppointmentSummary) }
    var staffDailyTime by remember(staffSettings) { mutableStateOf(staffSettings.dailySummaryTime) }
    var staffAutoReminder by remember(staffSettings) { mutableStateOf(staffSettings.autoSendAppointmentReminder) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("messages_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Bar
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("btn_back_messages")
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
                        text = "Mesaj Sistemi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Meta onaylı resmi WhatsApp şablonları ve bildirim kuralları.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Navigation Tabs (Müşteri Ayarları / Personel Ayarları / Mesaj Şablonları)
        item {
            Column {
                Text(
                    text = "Bölümler",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Müşteri Ayarları", "Personel Ayarları", "Mesaj Şablonları").forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable { selectedTab = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Tab Content
        item {
            when (selectedTab) {
                0 -> {
                    // Müşteri Ayarları Tab
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Müşteri bildirimleri aktif", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Switch(
                                    checked = custActive,
                                    onCheckedChange = { custActive = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF22C55E))
                                )
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            // WhatsApp Otomatik Kanal Kartı
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFF22C55E).copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF22C55E).copy(alpha = 0.08f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF25D366)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Chat,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Kanal: WhatsApp",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF22C55E).copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "Otomatik",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF15803D),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Tüm randevu onayı, hatırlatma ve servis fişi bildirimleri doğrudan Meta WhatsApp Cloud API üzerinden gönderilir.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Checkboxes
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = custAutoCreated,
                                    onCheckedChange = { custAutoCreated = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF22C55E))
                                )
                                Text("Randevu oluşturulduğunda müşteriye otomatik WhatsApp mesajı gönderilsin", fontSize = 13.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = custAutoUpdated,
                                    onCheckedChange = { custAutoUpdated = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF22C55E))
                                )
                                Text("Randevu güncellendiğinde bilgi mesajı gönderilsin", fontSize = 13.sp)
                            }

                            OutlinedTextField(
                                value = custReminderHours,
                                onValueChange = { custReminderHours = it },
                                label = { Text("Hatırlatma Mesajı Kaç Saat Önce Gönderilsin?") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = custAutoFeedback,
                                    onCheckedChange = { custAutoFeedback = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF22C55E))
                                )
                                Text("Randevudan sonra değerlendirme (Google) mesajı gönderilsin", fontSize = 13.sp)
                            }

                            OutlinedTextField(
                                value = custFeedbackDays,
                                onValueChange = { custFeedbackDays = it },
                                label = { Text("Değerlendirme Mesajı Kaç Gün Sonra Gönderilsin?") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    val updated = CustomerMessagingSettings(
                                        isNotificationsActive = custActive,
                                        selectedChannel = "WhatsApp",
                                        autoSendOnAppointmentCreated = custAutoCreated,
                                        autoSendOnAppointmentUpdated = custAutoUpdated,
                                        reminderHoursBefore = custReminderHours.toIntOrNull() ?: 2,
                                        autoSendFeedbackAfterAppointment = custAutoFeedback,
                                        feedbackDaysAfter = custFeedbackDays.toIntOrNull() ?: 2
                                    )
                                    onUpdateCustomerSettings(updated)
                                    Toast.makeText(context, "Müşteri mesaj ayarları kaydedildi", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ayarları Kaydet", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                1 -> {
                    // Personel Ayarları Tab
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Usta bildirimleri aktif", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Switch(
                                    checked = staffActive,
                                    onCheckedChange = { staffActive = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF22C55E))
                                )
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            // WhatsApp Otomatik Kanal Kartı
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFF22C55E).copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF22C55E).copy(alpha = 0.08f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF25D366)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Chat,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Kanal: WhatsApp",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF22C55E).copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "Otomatik",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF15803D),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Usta iş atamaları, konum bağlantıları ve günlük iş özetleri WhatsApp üzerinden iletilir.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = staffAutoDaily,
                                    onCheckedChange = { staffAutoDaily = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF22C55E))
                                )
                                Text("Günlük randevu bilgilendirme mesajı gönderilsin", fontSize = 13.sp)
                            }

                            OutlinedTextField(
                                value = staffDailyTime,
                                onValueChange = { staffDailyTime = it },
                                label = { Text("Günlük Randevu Mesajı Saati (Örn: 08:30)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = staffAutoReminder,
                                    onCheckedChange = { staffAutoReminder = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF22C55E))
                                )
                                Text("Randevu öncesi ustaya hatırlatma bildirimi gönderilsin", fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    val updated = StaffMessagingSettings(
                                        isNotificationsActive = staffActive,
                                        selectedChannel = "WhatsApp",
                                        autoSendDailyAppointmentSummary = staffAutoDaily,
                                        dailySummaryTime = staffDailyTime,
                                        autoSendAppointmentReminder = staffAutoReminder
                                    )
                                    onUpdateStaffSettings(updated)
                                    Toast.makeText(context, "Personel mesaj ayarları kaydedildi", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ayarları Kaydet", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                2 -> {
                    // Mesaj Şablonları Tab (Müşteriye/Ustaya Gidecek BİTMİŞ ÖNİZLEME Görünümü)
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Meta Onay Bilgilendirme Kartı
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.04f)),
                            border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Meta Onaylı WhatsApp Şablonları",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Salt Okunur",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Aşağıda müşteriye ve ustaya gönderilecek onaylı WhatsApp mesajlarının bitmiş canlı önizlemesi yer almaktadır.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        // Kategori Filtresi (Müşteriye Gidecek / Ustaya Gidecek)
                        val musteriCount = templates.count { it.category == "MUSTERI" }.coerceAtLeast(8)
                        val ustaCount = templates.count { it.category == "USTA" }.coerceAtLeast(3)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "MUSTERI" to "Müşteriye Gidecek ($musteriCount)",
                                "USTA" to "Ustaya Gidecek ($ustaCount)"
                            ).forEach { (catKey, catLabel) ->
                                val isCatSelected = templateCategoryFilter == catKey
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isCatSelected) Color(0xFF22C55E) else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { templateCategoryFilter = catKey }
                                ) {
                                    Text(
                                        text = catLabel,
                                        color = if (isCatSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        val filteredTemplates = templates.filter { it.category == templateCategoryFilter }

                        // Her şablonu bitmiş canlı WhatsApp mesaj önizlemesi olarak göster
                        filteredTemplates.forEach { tpl ->
                            WhatsAppTemplateCard(
                                template = tpl,
                                onCopyClick = {
                                    val rendered = renderFinalCustomerPreview(tpl)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val fullCopyText = "${rendered.headerTitle}\n\n${rendered.bodyText}"
                                    val clip = ClipData.newPlainText("WhatsApp Mesajı", fullCopyText)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "${rendered.headerTitle} kopyalandı", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Şablonun müşteriye / ustaya iletilecek BİTMİŞ CANLI ÖNİZLEME VERİSİ
 */
data class RenderedTemplatePreview(
    val headerTitle: String,
    val bodyText: String,
    val buttons: List<String>,
    val timeStamp: String
)

/**
 * Sunucudan gelen ham şablon parametrelerini ({{customer_name}}, {{service_line}}, vb.)
 * gerçekçi örnek verilerle doldurarak müşteriye gidecek nihai bitmiş haline dönüştürür.
 */
fun renderFinalCustomerPreview(template: MessageTemplate): RenderedTemplatePreview {
    val id = template.id.lowercase()
    val title = template.title.lowercase()
    val tag = template.tag.lowercase()
    val rawText = template.templateText

    // 1. Randevu Onay Mesajı
    if (id == "t1" || id.contains("created") || id.contains("confirmed") || title.contains("onay") || tag.contains("onay") || rawText.contains("Randevu Onay", true)) {
        return RenderedTemplatePreview(
            headerTitle = "Sancak Kombi - Randevu Onayı",
            bodyText = "Merhaba Fettah Sancaklı, servis randevunuz başarıyla oluşturulmuştur.\n\n" +
                    "🗓️ Hizmet: Kombi Bakım & Servis\n" +
                    "🗓️ Tarih: 13.08.2026 Perşembe\n" +
                    "⏰ Saat: 13:00 - 15:00\n" +
                    "📍 Adres: Yıldırım Mah. Ardıç Sokak No:5, Bayrampaşa\n\n" +
                    "Değişiklik veya bilgi talebiniz için aşağıdaki butondan bizi arayabilirsiniz.",
            buttons = listOf("📞 Hemen Ara"),
            timeStamp = "09:54"
        )
    }

    // 2. Randevu Güncelleme Mesajı
    if (id == "t2" || id.contains("updated") || title.contains("güncelle") || tag.contains("güncelle") || rawText.contains("Güncelleme", true)) {
        return RenderedTemplatePreview(
            headerTitle = "Sancak Kombi - Randevu Güncelleme",
            bodyText = "Merhaba Fettah Sancaklı, randevu bilgileriniz başarıyla güncellenmiştir.\n\n" +
                    "🗓️ Hizmet: Kombi Bakım & Servis\n" +
                    "🗓️ Yeni Tarih: 15.08.2026 Cumartesi\n" +
                    "⏰ Yeni Saat: 13:00 - 15:00\n" +
                    "📍 Adres: Yıldırım Mah. Ardıç Sokak No:5, Bayrampaşa\n\n" +
                    "Bilgilerde bir uyuşmazlık varsa veya değişiklik yapmak isterseniz aşağıdaki butondan bize ulaşabilirsiniz.\n" +
                    "Sancak Kombi · 0212 581 75 74",
            buttons = listOf("📞 Hemen Ara"),
            timeStamp = "10:00"
        )
    }

    // 3. Randevu Hatırlatma
    if ((id == "t3" || id.contains("reminder") || title.contains("hatırlat") || tag.contains("hatırlat")) && !title.contains("ödeme") && !title.contains("bakım") && template.category == "MUSTERI") {
        return RenderedTemplatePreview(
            headerTitle = "Sancak Kombi - Randevu Hatırlatma",
            bodyText = "Merhaba Fettah Sancaklı, servis randevunuzu hatırlatmak isteriz.\n\n" +
                    "🗓️ Hizmet: Kombi Bakım & Servis\n" +
                    "🗓️ Tarih: 14.08.2026 Cuma\n" +
                    "⏰ Saat: 09:00 - 11:00\n" +
                    "📍 Adres: Yıldırım Mah. Ardıç Sokak No:5, Bayrampaşa\n\n" +
                    "Randevu saatinde belirtilen adreste bulunmanızı rica ederiz. Değişiklik veya bilgi talebiniz için aşağıdaki butondan bizi arayabilirsiniz.",
            buttons = listOf("📞 Hemen Ara"),
            timeStamp = "10:00"
        )
    }

    // 4. Randevu İptali
    if (id == "t4" || id.contains("cancel") || title.contains("iptal") || tag.contains("iptal") || rawText.contains("İptali", true)) {
        return RenderedTemplatePreview(
            headerTitle = "Sancak Kombi - Randevu İptali",
            bodyText = "Merhaba Fettah Sancaklı, randevunuz talebiniz doğrultusunda iptal edilmiştir.\n\n" +
                    "🗓️ Hizmet: Kombi Bakım & Servis\n" +
                    "🗓️ Tarih: 13.08.2026 Perşembe\n" +
                    "⏰ Saat: 13:00 - 15:00\n\n" +
                    "Yeni bir randevu oluşturmak veya bilgi almak için aşağıdaki butondan bize her zaman ulaşabilirsiniz.\n" +
                    "Sancak Kombi · 0212 581 75 74",
            buttons = listOf("📞 Hemen Ara"),
            timeStamp = "10:00"
        )
    }

    // 5. Servis Bilgilendirmesi / Servis Fişi
    if (id == "t5" || id.contains("completed") || id.contains("receipt") || title.contains("servis") || tag.contains("fiş") || rawText.contains("Servis Bilgilendirmesi", true)) {
        return RenderedTemplatePreview(
            headerTitle = "Sancak Kombi - Servis Bilgilendirmesi",
            bodyText = "Merhaba Fettah Sancaklı, servis işleminiz başarıyla tamamlanmıştır. Bizi tercih ettiğiniz için teşekkür ederiz.\n\n" +
                    "🗓️ Hizmet: Kombi Bakım & Servis\n" +
                    "🗓️ Tarih: 13.08.2026 Perşembe\n" +
                    "⏰ Saat: 13:00 - 15:00\n\n" +
                    "Servis fişinizi aşağıdaki butondan görüntüleyebilir, herhangi bir sorunuzda bizi arayabilirsiniz.",
            buttons = listOf("↗ Servis Fişini Gör", "📞 Destek Hattı"),
            timeStamp = "09:59"
        )
    }

    // 6. Ödeme Bilgileri (Havale / EFT)
    if (id == "t6" || id.contains("payment_info") || title.contains("ödeme bilgi") || tag.contains("iban") || rawText.contains("IBAN", true)) {
        return RenderedTemplatePreview(
            headerTitle = "Sancak Kombi - Ödeme Bilgileri",
            bodyText = "Merhaba Fettah Sancaklı, Sancak Kombi'yi tercih ettiğiniz için teşekkür ederiz.\n\n" +
                    "Servis ödemenizi dilerseniz havale / EFT yöntemiyle aşağıdaki hesabımıza iletebilirsiniz:\n\n" +
                    "🗓️ Hizmet: Kombi Bakım & Servis\n" +
                    "💰 Tutar: 2.500,00 TL\n" +
                    "👤 Hesap Sahibi: Fatih Sancaklı\n" +
                    "🏦 Banka: Kuveyttürk Bankası\n" +
                    "🔢 IBAN: TR00 0000 0000 0000 0000 0000 00\n\n" +
                    "Ödemenizi tamamladıktan sonra dekont paylaşmanız durumunda kaydınız hemen güncellenecektir.\n" +
                    "Sağlıklı ve sıcak günlerde kullanmanızı dileriz.",
            buttons = emptyList(),
            timeStamp = "10:00"
        )
    }

    // 7. Ödeme Hatırlatması
    if (id == "t7" || id.contains("payment_reminder") || title.contains("ödeme hatırlat") || tag.contains("bakiye") || rawText.contains("Ödeme Hatırlatması", true)) {
        return RenderedTemplatePreview(
            headerTitle = "Sancak Kombi - Ödeme Hatırlatması",
            bodyText = "Merhaba Fettah Sancaklı, sistem kayıtlarımıza göre 20.08.2026 tarihi için planlanan ödeme taahhüdünüz bulunmaktadır.\n\n" +
                    "🗓️ Hizmet: Kombi Bakım & Servis\n" +
                    "💰 Kalan Tutar: 2.500,00 TL\n\n" +
                    "Ödemenizi tamamlamanızı rica ederiz. Ödemeyi gerçekleştirdiyseniz lütfen bu mesajı dikkate almayınız.\n" +
                    "Sancak Kombi Teknik Servis",
            buttons = listOf("📞 Hemen Ara"),
            timeStamp = "10:01"
        )
    }

    // 8. Periyodik Bakım Zamanı
    if (id == "t8" || id.contains("maintenance") || title.contains("periyodik") || title.contains("bakım") || rawText.contains("Periyodik Bakım Zamanı", true)) {
        return RenderedTemplatePreview(
            headerTitle = "Sancak Kombi - Periyodik Bakım Zamanı",
            bodyText = "Merhaba Fettah Sancaklı, kombinizin verimli ve güvenli çalışması için yıllık periyodik bakım zamanı yaklaşmaktadır.\n\n" +
                    "🗓️ Son Servis: 13.08.2025\n" +
                    "🛠️ Hizmet: Kombi Bakım & Servis\n\n" +
                    "Periyodik bakım randevusu oluşturmak veya bilgi almak için aşağıdaki butondan bizi arayabilirsiniz.\n" +
                    "Sancak Kombi · 0212 581 75 74",
            buttons = listOf("📞 Randevu Al"),
            timeStamp = "10:01"
        )
    }

    // 9. Deneyiminizi Paylaşın (Google Değerlendirme)
    if (id == "t9" || id.contains("feedback") || title.contains("deneyim") || title.contains("değerlendir") || title.contains("google") || rawText.contains("Deneyiminizi Paylaşın", true)) {
        return RenderedTemplatePreview(
            headerTitle = "Sancak Kombi - Deneyiminizi Paylaşın",
            bodyText = "Merhaba Fettah Sancaklı,\n\n" +
                    "Sancak Kombi'den aldığınız Kombi Bakım & Servis hizmetinden memnun kaldınız mı? 🌟\n\n" +
                    "Değerli yorumunuz ve puanınız, sizlere sunduğumuz hizmet kalitesini geliştirmemiz için çok önemlidir. 💬\n\n" +
                    "Aşağıdaki butona dokunarak Google üzerinden birkaç saniyede deneyiminizi paylaşabilirsiniz. 👇\n\n" +
                    "Bizi tercih ettiğiniz için teşekkür ederiz! 🙏\n" +
                    "Sancak Kombi Teknik Servis",
            buttons = listOf("↗ Değerlendir (Google)"),
            timeStamp = "10:02"
        )
    }

    // 10. USTA - Yeni İş Bildirimi
    if (template.category == "USTA" && (id == "t10" || title.contains("yeni iş") || tag.contains("atama"))) {
        return RenderedTemplatePreview(
            headerTitle = "Sancak Kombi - Yeni İş Bildirimi",
            bodyText = "🛠️ Yeni Randevu Atandı:\n" +
                    "👤 Müşteri: Fettah Sancaklı\n" +
                    "📞 Telefon: 0532 000 00 00\n" +
                    "📍 Adres: Yıldırım Mah. Ardıç Sokak No:5, Bayrampaşa\n" +
                    "🗓️ Hizmet: Kombi Bakım & Servis\n" +
                    "⏰ Randevu: 13.08.2026 13:00 - 15:00\n" +
                    "📝 Not: Cihaz sıcak su vermiyor, petekler ılık.",
            buttons = listOf("📍 Konuma Git", "📞 Müşteriyi Ara"),
            timeStamp = "08:30"
        )
    }

    // 11. USTA - Randevu Hatırlatması
    if (template.category == "USTA" && (id == "t11" || title.contains("hatırlat"))) {
        return RenderedTemplatePreview(
            headerTitle = "Sancak Kombi - Randevu Hatırlatması",
            bodyText = "Sayın Usta, bugün saat 13:00 için Fettah Sancaklı (0532 000 00 00) adresine randevunuz bulunmaktadır.\n" +
                    "📍 Adres: Yıldırım Mah. Ardıç Sokak No:5, Bayrampaşa",
            buttons = listOf("📍 Adrese Git"),
            timeStamp = "12:30"
        )
    }

    // 12. USTA - Günlük Randevu Özeti
    if (template.category == "USTA" && (id == "t12" || title.contains("özet") || title.contains("günlük"))) {
        return RenderedTemplatePreview(
            headerTitle = "Sancak Kombi - Günlük Randevu Özeti",
            bodyText = "Günaydın, bugün için adınıza kayıtlı toplam 4 adet servis randevunuz bulunmaktadır.\n" +
                    "Detayları Sancak Kombi Usta Panelinden görüntüleyebilirsiniz.",
            buttons = listOf("📱 Panele Git"),
            timeStamp = "08:00"
        )
    }

    // 13. Dinamik Değiştirici (Herhangi bir bilinmeyen şablon gelirse {{...}} kalıplarını akıllıca temizler)
    var replaced = rawText
        .replace("{{customer_name}}", "Fettah Sancaklı")
        .replace("{{service_line}}", "🗓️ Hizmet: Kombi Bakım & Servis")
        .replace("{{date_line}}", "🗓️ Tarih: 13.08.2026 Perşembe")
        .replace("{{time_line}}", "⏰ Saat: 13:00 - 15:00")
        .replace("{{district_line}}", "📍 İlçe: Bayrampaşa")
        .replace("{{address_line}}", "📍 Adres: Yıldırım Mah. Ardıç Sokak No:5, Bayrampaşa")
        .replace("{{business_phone_line}}", "Sancak Kombi · 0212 581 75 74")
        .replace("{{amount}}", "2.500,00 TL")
        .replace("{{total_amount}}", "2.500,00 TL")
        .replace("{{bank_name}}", "Kuveyttürk Bankası")
        .replace("{{iban}}", "TR00 0000 0000 0000 0000 0000 00")
        .replace("{{account_holder}}", "Fatih Sancaklı")
        .replace("{{maps_url}}", "https://maps.google.com/?q=Bayrampasa")

    // İlk satır başlık ise ayıkla
    val lines = replaced.split("\n\n", limit = 2)
    val header = if (lines.size > 1 && (lines[0].startsWith("Sancak Kombi") || lines[0].contains("Mesaj"))) {
        "Sancak Kombi - ${template.title.replace("Mesajı", "").replace("Mesaj", "").trim()}"
    } else {
        "Sancak Kombi - ${template.title}"
    }
    val body = if (lines.size > 1 && (lines[0].startsWith("Sancak Kombi") || lines[0].contains("Mesaj"))) lines[1] else replaced

    return RenderedTemplatePreview(
        headerTitle = header,
        bodyText = body,
        buttons = template.buttons.ifEmpty { listOf("📞 Hemen Ara") },
        timeStamp = "10:00"
    )
}

/**
 * Meta Onaylı WhatsApp Şablonunu görselleştiren baloncuk kartı
 * Müşteriye gidecek bitmiş canlı WhatsApp mesajını render eder.
 */
@Composable
private fun WhatsAppTemplateCard(
    template: MessageTemplate,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Şablonun bitmiş önizlemesini al
    val preview = remember(template) {
        renderFinalCustomerPreview(template)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Üst Bar: "Şablonunuz" Başlığı ve Meta Rozeti
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Şablonunuz",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF22C55E).copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Meta Onaylı",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onCopyClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Metni Kopyala",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 2. WhatsApp Arka Planı (Doodle Desenli Chat Duvar Kağıdı)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEFEAE2))
                    .padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                // Arka plan WhatsApp hafif doodle çizimi
                WhatsAppDoodleBackground(
                    modifier = Modifier.matchParentSize()
                )

                // 3. WhatsApp Mesaj Baloncuğu
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    ),
                    color = Color.White,
                    shadowElevation = 1.5.dp,
                    border = BorderStroke(0.5.dp, Color(0xFF000000).copy(alpha = 0.06f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Mesaj Başlığı ve Gövdesi
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 8.dp)
                        ) {
                            // Kalın Şablon Başlığı (Örn: Sancak Kombi - Randevu Onayı)
                            Text(
                                text = preview.headerTitle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111B21),
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Mesaj Metni & Emojili Bilgi Satırları
                            Text(
                                text = preview.bodyText,
                                fontSize = 13.5.sp,
                                color = Color(0xFF111B21),
                                lineHeight = 19.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Sağ Altta Zaman Damgası
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = preview.timeStamp,
                                    fontSize = 11.sp,
                                    color = Color(0xFF667781),
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }

                        // 4. WhatsApp Şablon Eylem Butonları (Varsa)
                        if (preview.buttons.isNotEmpty()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                preview.buttons.forEach { rawBtn ->
                                    Divider(color = Color(0xFFE9EDEF), thickness = 1.dp)

                                    // Buton ikonunu ve temizlenmiş metnini belirle
                                    val (icon, cleanText) = parseButtonInfo(rawBtn)

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White)
                                            .padding(vertical = 12.dp, horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (icon != null) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = Color(0xFF00A884),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }

                                        Text(
                                            text = cleanText,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF00A884)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * WhatsApp buton metinlerini parse ederek uygun ikon ve temiz metni döner
 */
private fun parseButtonInfo(rawBtn: String): Pair<androidx.compose.ui.graphics.vector.ImageVector?, String> {
    val clean = rawBtn
        .replace("↗", "")
        .replace("📞", "")
        .replace("📍", "")
        .replace("📱", "")
        .trim()

    val icon = when {
        rawBtn.contains("↗") || clean.contains("Gör", ignoreCase = true) || clean.contains("Değerlendir", ignoreCase = true) || clean.contains("Panele", ignoreCase = true) -> Icons.AutoMirrored.Filled.OpenInNew
        rawBtn.contains("📞") || clean.contains("Ara", ignoreCase = true) || clean.contains("Hat", ignoreCase = true) || clean.contains("Randevu Al", ignoreCase = true) -> Icons.Default.Call
        rawBtn.contains("📍") || clean.contains("Konum", ignoreCase = true) || clean.contains("Adres", ignoreCase = true) -> Icons.Default.LocationOn
        else -> null
    }

    return icon to clean
}

/**
 * WhatsApp chat arka planındaki hafif doodle çizim efektini çizen Canvas
 */
@Composable
private fun WhatsAppDoodleBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val doodleColor = Color(0xFF000000).copy(alpha = 0.035f)
        val strokeWidth = 1.2.dp.toPx()

        val w = size.width
        val h = size.height

        // Dağınık dekoratif desenler: minik konuşma balonları, daireler, kıvrımlar
        var y = 20f
        while (y < h) {
            var x = 20f
            while (x < w) {
                val seed = ((x * 13 + y * 7).toInt() % 4)
                when (seed) {
                    0 -> {
                        // Minik konuşma balonu
                        drawRoundRect(
                            color = doodleColor,
                            topLeft = Offset(x, y),
                            size = Size(22f, 16f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                            style = Stroke(width = strokeWidth)
                        )
                    }
                    1 -> {
                        // Minik saat / daire
                        drawCircle(
                            color = doodleColor,
                            radius = 7f,
                            center = Offset(x + 10f, y + 10f),
                            style = Stroke(width = strokeWidth)
                        )
                    }
                    2 -> {
                        // Minik mesaj çizgileri
                        drawLine(
                            color = doodleColor,
                            start = Offset(x, y + 5f),
                            end = Offset(x + 18f, y + 5f),
                            strokeWidth = strokeWidth
                        )
                        drawLine(
                            color = doodleColor,
                            start = Offset(x, y + 12f),
                            end = Offset(x + 12f, y + 12f),
                            strokeWidth = strokeWidth
                        )
                    }
                    else -> {
                        // Minik gülen yüz eğrisi
                        val path = Path().apply {
                            moveTo(x, y + 5f)
                            quadraticTo(x + 8f, y + 14f, x + 16f, y + 5f)
                        }
                        drawPath(path = path, color = doodleColor, style = Stroke(width = strokeWidth))
                    }
                }
                x += 65f
            }
            y += 55f
        }
    }
}
