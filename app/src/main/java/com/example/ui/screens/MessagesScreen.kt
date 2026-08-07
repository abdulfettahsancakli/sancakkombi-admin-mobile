package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    stats: MessagingStats,
    customerSettings: CustomerMessagingSettings,
    staffSettings: StaffMessagingSettings,
    templates: List<MessageTemplate>,
    jobs: List<MessageJob>,
    logs: List<MessageLog>,
    onBackClick: () -> Unit,
    onUpdateCustomerSettings: (CustomerMessagingSettings) -> Unit,
    onUpdateStaffSettings: (StaffMessagingSettings) -> Unit,
    onUpdateTemplate: (MessageTemplate) -> Unit,
    onRetryJob: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Müşteriler, 1: Personel, 2: Mesaj Şablonları
    var templateCategoryFilter by remember { mutableStateOf("MUSTERI") } // "MUSTERI" or "USTA"
    var editingTemplate by remember { mutableStateOf<MessageTemplate?>(null) }

    // Local copy of customer settings for form inputs
    var custActive by remember(customerSettings) { mutableStateOf(customerSettings.isNotificationsActive) }
    var custChannel by remember(customerSettings) { mutableStateOf(customerSettings.selectedChannel) }
    var custAutoCreated by remember(customerSettings) { mutableStateOf(customerSettings.autoSendOnAppointmentCreated) }
    var custAutoUpdated by remember(customerSettings) { mutableStateOf(customerSettings.autoSendOnAppointmentUpdated) }
    var custReminderHours by remember(customerSettings) { mutableStateOf(customerSettings.reminderHoursBefore.toString()) }
    var custAutoFeedback by remember(customerSettings) { mutableStateOf(customerSettings.autoSendFeedbackAfterAppointment) }
    var custFeedbackDays by remember(customerSettings) { mutableStateOf(customerSettings.feedbackDaysAfter.toString()) }

    // Local copy of staff settings for form inputs
    var staffActive by remember(staffSettings) { mutableStateOf(staffSettings.isNotificationsActive) }
    var staffChannel by remember(staffSettings) { mutableStateOf(staffSettings.selectedChannel) }
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
                        text = "Mesajlar",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Müşteri ayarları, personel bildirimleri, şablonlar ve gönderim kuyruğu.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Summary Cards (3 Cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Gönderildi Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("GÖNDERİLDİ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(stats.gonderildiCount.toString(), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF22C55E))
                    }
                }

                // Bekleyen Job Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("BEKLEYEN JOB", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(stats.bekleyenCount.toString(), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEAB308))
                    }
                }

                // Başarısız Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("BAŞARISIZ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(stats.basarisizCount.toString(), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF4444))
                    }
                }
            }
        }

        // Navigation Tabs (Müşteriler / Personel / Mesaj Şablonları)
        item {
            Column {
                Text(
                    text = "Mesaj Ayarları",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Müşteriler", "Personel", "Mesaj Şablonları").forEachIndexed { index, title ->
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
                                fontSize = 13.sp,
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
                    // Müşteriler Tab
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

                            Text("Mesajlar Hangi Kanal Üzerinden Gönderilsin?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            
                            // Channel options
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // SMS Card
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = if (custChannel == "SMS") 2.dp else 1.dp,
                                            color = if (custChannel == "SMS") Color(0xFF22C55E) else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { custChannel = "SMS" },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = custChannel == "SMS",
                                            onClick = { custChannel = "SMS" },
                                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF22C55E))
                                        )
                                        Column {
                                            Text("SMS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Kısa hatırlatma ve durum mesajları SMS olarak gitsin.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }

                                // WhatsApp Card
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = if (custChannel == "WhatsApp") 2.dp else 1.dp,
                                            color = if (custChannel == "WhatsApp") Color(0xFF22C55E) else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { custChannel = "WhatsApp" },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = custChannel == "WhatsApp",
                                            onClick = { custChannel = "WhatsApp" },
                                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF22C55E))
                                        )
                                        Column {
                                            Text("WhatsApp", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Müşteriyle yazışma ve hatırlatmalar WhatsApp üzerinden gitsin.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
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
                                Text("Randevu oluşturulduğunda müşteriye otomatik mesaj gönderilsin mi?", fontSize = 13.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = custAutoUpdated,
                                    onCheckedChange = { custAutoUpdated = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF22C55E))
                                )
                                Text("Randevu güncellendiğinde bilgi mesajı gönderilsin mi?", fontSize = 13.sp)
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
                                Text("Randevudan sonra değerlendirme mesajı gönderilsin mi?", fontSize = 13.sp)
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
                                        selectedChannel = custChannel,
                                        autoSendOnAppointmentCreated = custAutoCreated,
                                        autoSendOnAppointmentUpdated = custAutoUpdated,
                                        reminderHoursBefore = custReminderHours.toIntOrNull() ?: 2,
                                        autoSendFeedbackAfterAppointment = custAutoFeedback,
                                        feedbackDaysAfter = custFeedbackDays.toIntOrNull() ?: 2
                                    )
                                    onUpdateCustomerSettings(updated)
                                    Toast.makeText(context, "Müşteri mesaj ayarları güncellendi", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ayarları Güncelle", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                1 -> {
                    // Personel Tab
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

                            Text("Mesajlar Hangi Kanal Üzerinden Gönderilsin?", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = if (staffChannel == "WhatsApp") 2.dp else 1.dp,
                                            color = if (staffChannel == "WhatsApp") Color(0xFF22C55E) else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { staffChannel = "WhatsApp" },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = staffChannel == "WhatsApp",
                                            onClick = { staffChannel = "WhatsApp" },
                                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF22C55E))
                                        )
                                        Column {
                                            Text("WhatsApp", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Usta bildirimleri WhatsApp üzerinden gitsin.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = if (staffChannel == "SMS") 2.dp else 1.dp,
                                            color = if (staffChannel == "SMS") Color(0xFF22C55E) else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { staffChannel = "SMS" },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = staffChannel == "SMS",
                                            onClick = { staffChannel = "SMS" },
                                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF22C55E))
                                        )
                                        Column {
                                            Text("SMS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Usta bildirimleri SMS olarak gitsin.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
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
                                Text("Günlük randevu bilgilendirme mesajı gönderilsin mi?", fontSize = 13.sp)
                            }

                            OutlinedTextField(
                                value = staffDailyTime,
                                onValueChange = { staffDailyTime = it },
                                label = { Text("Günlük Randevu Mesajı Kaç Saatte Gönderilsin? (Saat)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = staffAutoReminder,
                                    onCheckedChange = { staffAutoReminder = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF22C55E))
                                )
                                Text("Randevu hatırlatma mesajı gönderilsin mi?", fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    val updated = StaffMessagingSettings(
                                        isNotificationsActive = staffActive,
                                        selectedChannel = staffChannel,
                                        autoSendDailyAppointmentSummary = staffAutoDaily,
                                        dailySummaryTime = staffDailyTime,
                                        autoSendAppointmentReminder = staffAutoReminder
                                    )
                                    onUpdateStaffSettings(updated)
                                    Toast.makeText(context, "Personel mesaj ayarları güncellendi", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ayarları Güncelle", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                2 -> {
                    // Mesaj Şablonları Tab
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Sub-filter pills (Müşteriye Gidecek / Ustaya Gidecek)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("MUSTERI" to "Müşteriye Gidecek", "USTA" to "Ustaya Gidecek").forEach { (catKey, catLabel) ->
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

                        filteredTemplates.forEach { tpl ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(tpl.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = tpl.tag,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        TextButton(
                                            onClick = { editingTemplate = tpl }
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Düzenle", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Düzenle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Template Text Preview Box
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = tpl.templateText,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Bekleyen ve Hata Veren Joblar
        item {
            Column {
                Text(
                    text = "Bekleyen ve Hata Veren Joblar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )

                if (jobs.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                            Text("Bekleyen veya hatalı job bulunmamaktadır.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    jobs.forEach { job ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(job.template, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (job.status == "FAILED") Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFFEAB308).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (job.status == "FAILED") "Başarısız" else "Bekliyor",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (job.status == "FAILED") Color(0xFFEF4444) else Color(0xFFEAB308),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Kanal: ${job.channel} | Alıcı: ${job.recipient} | Zaman: ${job.time}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                job.error?.let { err ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Hata: $err", fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Medium)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        onRetryJob(job.id)
                                        Toast.makeText(context, "Yeniden gönderim başlatıldı", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Yeniden Dene", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Müşteriye/Ustaya Gönderilen Son Mesaj Logları
        item {
            Column {
                Text(
                    text = "Müşteriye/Ustaya Gönderilen Son Mesaj Logları",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        logs.forEachIndexed { index, log ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(log.template, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${log.recipient} • ${log.channel} • ${log.time}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                val (statusText, statusBg, statusColor) = when (log.status) {
                                    "sent" -> Triple("Gönderildi", Color(0xFF22C55E).copy(alpha = 0.15f), Color(0xFF22C55E))
                                    "skipped" -> Triple("Atlandı", Color(0xFF6B7280).copy(alpha = 0.15f), Color(0xFF6B7280))
                                    else -> Triple("Başarısız", Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFEF4444))
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = statusBg
                                ) {
                                    Text(
                                        text = statusText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (index < logs.size - 1) {
                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Edit Template Dialog
    editingTemplate?.let { tpl ->
        var tempText by remember { mutableStateOf(tpl.templateText) }

        AlertDialog(
            onDismissRequest = { editingTemplate = null },
            title = { Text("Şablonu Düzenle - ${tpl.title}", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text("Şablon Metni:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = tempText,
                        onValueChange = { tempText = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 6,
                        maxLines = 10
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateTemplate(tpl.copy(templateText = tempText))
                        editingTemplate = null
                        Toast.makeText(context, "Şablon güncellendi", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingTemplate = null }) {
                    Text("İptal")
                }
            }
        )
    }
}
