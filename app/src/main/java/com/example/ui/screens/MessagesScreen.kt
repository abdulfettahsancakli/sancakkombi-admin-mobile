package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
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
    var selectedTab by remember { mutableIntStateOf(2) } // Varsayılan olarak Şablonlar sekmesi veya 0
    var templateCategoryFilter by remember { mutableStateOf("MUSTERI") } // "MUSTERI" or "USTA"

    // Local copy of customer settings for form inputs (Kanal WhatsApp)
    var custActive by remember(customerSettings) { mutableStateOf(customerSettings.isNotificationsActive) }
    var custAutoCreated by remember(customerSettings) { mutableStateOf(customerSettings.autoSendOnAppointmentCreated) }
    var custAutoUpdated by remember(customerSettings) { mutableStateOf(customerSettings.autoSendOnAppointmentUpdated) }
    var custReminderHours by remember(customerSettings) { mutableStateOf(customerSettings.reminderHoursBefore.toString()) }
    var custAutoFeedback by remember(customerSettings) { mutableStateOf(customerSettings.autoSendFeedbackAfterAppointment) }
    var custFeedbackDays by remember(customerSettings) { mutableStateOf(customerSettings.feedbackDaysAfter.toString()) }

    // Local copy of staff settings for form inputs (Kanal WhatsApp)
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

        // Navigation Tabs (Müşteriler / Personel / Mesaj Şablonları)
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
                    // Mesaj Şablonları Tab (Meta Onaylı WhatsApp Şablonları - Salt Okunur / Görüntüleme)
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Meta Onay Bilgilendirme Kartı
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.04f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.3f))
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
                                        text = "Bu şablonlar WhatsApp Business Cloud API tarafından onaylanmıştır. Usta ve müşterilere sistem tarafından otomatik doldurularak iletilir.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        // Kategori Filtresi (Müşteriye Gidecek / Ustaya Gidecek)
                        val musteriCount = templates.count { it.category == "MUSTERI" }
                        val ustaCount = templates.count { it.category == "USTA" }

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

                        // Her şablonu gerçekçi WhatsApp Baloncuğu kartı olarak göster
                        filteredTemplates.forEach { tpl ->
                            WhatsAppTemplateCard(
                                template = tpl,
                                onCopyClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("WhatsApp Şablonu", tpl.templateText)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "${tpl.title} kopyalandı", Toast.LENGTH_SHORT).show()
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
 * Meta Onaylı WhatsApp Şablonunu görselleştiren baloncuk kartı
 */
@Composable
private fun WhatsAppTemplateCard(
    template: MessageTemplate,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Şablon Üst Başlığı ve Rozetler
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF22C55E).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Meta Onaylı",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = template.tag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onCopyClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Metni Kopyala",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // WhatsApp Mesaj Baloncuğu Görünümü
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE5DDD5).copy(alpha = 0.35f))
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                        .background(Color.White)
                        .padding(12.dp)
                ) {
                    // Mesaj Metni
                    Text(
                        text = template.templateText,
                        fontSize = 12.sp,
                        color = Color(0xFF111827),
                        lineHeight = 17.sp
                    )

                    // Zaman Damgası
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "10:00 ✓✓",
                        fontSize = 10.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.align(Alignment.End)
                    )

                    // WhatsApp Şablon Butonları (Varsa)
                    if (template.buttons.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            template.buttons.forEach { btnText ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF0FDF4),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (btnText.contains("Ara") || btnText.contains("Hat")) {
                                            Icon(
                                                imageVector = Icons.Default.Call,
                                                contentDescription = null,
                                                tint = Color(0xFF16A34A),
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        } else if (btnText.contains("Gör") || btnText.contains("Değerlendir") || btnText.contains("Git")) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                contentDescription = null,
                                                tint = Color(0xFF16A34A),
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = btnText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D)
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
