package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Customer
import com.example.data.model.MaintenanceRule
import com.example.data.model.MaintenanceStats
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID

private val MaintenanceBlue = Color(0xFF073368)
private val ActionBlue = Color(0xFF006ECE)
private val MaintenanceGreen = Color(0xFF16A34A)
private val WhatsAppGreen = Color(0xFF15803D)
private val DangerRed = Color(0xFFB91C1C)

private enum class MaintenanceFilter(val label: String) {
    ALL("Tümü"), OVERDUE("Acil / Gecikmiş"), SOON("30 Gün İçinde"), FUTURE("İleri Tarihli"), INACTIVE("Pasif")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    stats: MaintenanceStats,
    rules: List<MaintenanceRule>,
    customers: List<Customer>,
    onBackClick: () -> Unit,
    onAddRule: (MaintenanceRule) -> Unit,
    onUpdateRule: (MaintenanceRule) -> Unit,
    onDeleteRule: (String) -> Unit,
    onToggleRuleStatus: (String) -> Unit,
    onSendReminder: (String, (Result<Unit>) -> Unit) -> Unit,
    onCreateAppointment: (MaintenanceRule) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(MaintenanceFilter.ALL) }
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteRule by remember { mutableStateOf<MaintenanceRule?>(null) }
    var sendingRuleId by remember { mutableStateOf<String?>(null) }

    val filteredRules = remember(rules, search, filter) {
        val query = search.trim().lowercase(Locale.forLanguageTag("tr-TR"))
        rules.filter { rule ->
            val days = daysUntil(rule.nextReminderDate)
            val statusMatch = when (filter) {
                MaintenanceFilter.ALL -> true
                MaintenanceFilter.OVERDUE -> rule.status == "Aktif" && days < 0
                MaintenanceFilter.SOON -> rule.status == "Aktif" && days in 0..30
                MaintenanceFilter.FUTURE -> rule.status == "Aktif" && days > 30
                MaintenanceFilter.INACTIVE -> rule.status != "Aktif"
            }
            val searchMatch = query.isBlank() || listOf(
                rule.customerName, rule.customerPhone, rule.customerDistrict,
                rule.deviceBrand, rule.deviceModel, serviceLabel(rule.serviceType)
            ).joinToString(" ").lowercase(Locale.forLanguageTag("tr-TR")).contains(query)
            statusMatch && searchMatch
        }.sortedBy { parseMaintenanceDate(it.nextReminderDate) }
    }
    val futureCount = rules.count { it.status == "Aktif" && daysUntil(it.nextReminderDate) > 30 }
    val inactiveCount = rules.count { it.status != "Aktif" }

    BoxWithConstraints(
        modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).testTag("maintenance_screen")
    ) {
        val tablet = maxWidth >= 700.dp
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = if (tablet) 20.dp else 12.dp,
                top = 10.dp,
                end = if (tablet) 20.dp else 12.dp,
                bottom = 104.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                MaintenanceHeader(
                    count = rules.size,
                    tablet = tablet,
                    onBackClick = onBackClick,
                    onAddClick = { showAddDialog = true }
                )
            }
            item {
                SummaryCards(
                    active = stats.activeRulesCount,
                    soon = stats.within30DaysCount,
                    overdue = stats.overdueCount,
                    future = futureCount,
                    inactive = inactiveCount,
                    tablet = tablet
                )
            }
            item {
                MaintenanceToolbar(
                    search = search,
                    filter = filter,
                    rules = rules,
                    onSearchChange = { search = it },
                    onFilterChange = { filter = it }
                )
            }
            if (filteredRules.isEmpty()) {
                item { EmptyMaintenanceState(hasRules = rules.isNotEmpty(), onClear = { search = ""; filter = MaintenanceFilter.ALL }) }
            } else if (tablet) {
                items(filteredRules.chunked(2), key = { row -> row.joinToString("-") { it.id } }) { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                        row.forEach { rule ->
                            MaintenanceCalendarCard(
                                rule = rule,
                                sending = sendingRuleId == rule.id,
                                onToggle = { onToggleRuleStatus(rule.id) },
                                onSend = {
                                    sendingRuleId = rule.id
                                    onSendReminder(rule.id) { result ->
                                        sendingRuleId = null
                                        result.onSuccess { Toast.makeText(context, "Bakım hatırlatması WhatsApp Cloud API ile gönderildi.", Toast.LENGTH_SHORT).show() }
                                            .onFailure { Toast.makeText(context, it.message ?: "Hatırlatma gönderilemedi.", Toast.LENGTH_LONG).show() }
                                    }
                                },
                                onAppointment = { onCreateAppointment(rule) },
                                onDelete = { deleteRule = rule },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            } else {
                items(filteredRules, key = { it.id }) { rule ->
                    MaintenanceCalendarCard(
                        rule = rule,
                        sending = sendingRuleId == rule.id,
                        onToggle = { onToggleRuleStatus(rule.id) },
                        onSend = {
                            sendingRuleId = rule.id
                            onSendReminder(rule.id) { result ->
                                sendingRuleId = null
                                result.onSuccess { Toast.makeText(context, "Bakım hatırlatması WhatsApp Cloud API ile gönderildi.", Toast.LENGTH_SHORT).show() }
                                    .onFailure { Toast.makeText(context, it.message ?: "Hatırlatma gönderilemedi.", Toast.LENGTH_LONG).show() }
                            }
                        },
                        onAppointment = { onCreateAppointment(rule) },
                        onDelete = { deleteRule = rule }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddMaintenanceRuleDialog(
            customers = customers,
            onDismiss = { showAddDialog = false },
            onCreate = {
                onAddRule(it)
                showAddDialog = false
                Toast.makeText(context, "Yeni bakım takvimi oluşturuldu.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    deleteRule?.let { rule ->
        AlertDialog(
            onDismissRequest = { deleteRule = null },
            title = { Text("Bakım Takvimini Sil") },
            text = { Text("${rule.customerName} adına tanımlı ${serviceLabel(rule.serviceType)} hatırlatması kalıcı olarak silinecek.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteRule(rule.id)
                        deleteRule = null
                        Toast.makeText(context, "Bakım takvimi silindi.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) { Text("Sil") }
            },
            dismissButton = { TextButton(onClick = { deleteRule = null }) { Text("Vazgeç") } }
        )
    }
}

@Composable
private fun MaintenanceHeader(count: Int, tablet: Boolean, onBackClick: () -> Unit, onAddClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(Modifier.padding(horizontal = 10.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick, modifier = Modifier.testTag("btn_back_maintenance")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                }
                Image(
                    painterResource(R.drawable.logo_sancakkombi), "Sancak Kombi",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(if (tablet) 46.dp else 40.dp).clip(RoundedCornerShape(11.dp))
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (tablet) "Periyodik Bakım Takvimleri" else "Bakım Takvimleri",
                            fontSize = if (tablet) 24.sp else 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(7.dp))
                        Surface(color = ActionBlue.copy(alpha = .12f), contentColor = ActionBlue, shape = CircleShape) {
                            Text("$count Takvim", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Text("Yıllık bakım hatırlatmalarını yönetin.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (tablet) {
                    Button(onClick = onAddClick, colors = ButtonDefaults.buttonColors(containerColor = MaintenanceGreen), modifier = Modifier.height(46.dp)) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Yeni Bakım Takvimi", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        if (!tablet) {
            Button(onClick = onAddClick, colors = ButtonDefaults.buttonColors(containerColor = MaintenanceGreen), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(7.dp))
                Text("Yeni Bakım Takvimi", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SummaryCards(active: Int, soon: Int, overdue: Int, future: Int, inactive: Int, tablet: Boolean) {
    val data = listOf(
        Triple("Aktif Takvimler", active, ActionBlue),
        Triple("30 Gün İçinde", soon, Color(0xFF0E7490)),
        Triple("Gecikmiş / Acil", overdue, DangerRed),
        Triple("İleri Tarih / Pasif", future + inactive, MaterialTheme.colorScheme.onSurfaceVariant)
    )
    if (tablet) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            data.forEach { (label, value, color) -> SummaryCard(label, value, color, Modifier.weight(1f)) }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            data.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    row.forEach { (label, value, color) -> SummaryCard(label, value, color, Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: Int, color: Color, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(12.dp)) {
            Text(label.uppercase(Locale.forLanguageTag("tr-TR")), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text(value.toString(), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
private fun MaintenanceToolbar(
    search: String,
    filter: MaintenanceFilter,
    rules: List<MaintenanceRule>,
    onSearchChange: (String) -> Unit,
    onFilterChange: (MaintenanceFilter) -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = search,
                onValueChange = onSearchChange,
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = { Text("Müşteri, telefon, cihaz veya ilçe ara...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                MaintenanceFilter.entries.forEach { item ->
                    val count = rules.count { rule ->
                        val days = daysUntil(rule.nextReminderDate)
                        when (item) {
                            MaintenanceFilter.ALL -> true
                            MaintenanceFilter.OVERDUE -> rule.status == "Aktif" && days < 0
                            MaintenanceFilter.SOON -> rule.status == "Aktif" && days in 0..30
                            MaintenanceFilter.FUTURE -> rule.status == "Aktif" && days > 30
                            MaintenanceFilter.INACTIVE -> rule.status != "Aktif"
                        }
                    }
                    FilterChip(selected = filter == item, onClick = { onFilterChange(item) }, label = { Text("${item.label} ($count)") })
                }
            }
        }
    }
}

@Composable
private fun MaintenanceCalendarCard(
    rule: MaintenanceRule,
    sending: Boolean,
    onToggle: () -> Unit,
    onSend: () -> Unit,
    onAppointment: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val days = daysUntil(rule.nextReminderDate)
    val overdue = rule.status == "Aktif" && days < 0
    Card(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(if (overdue) 2.dp else 1.dp, if (overdue) Color(0xFFFECACA) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(rule.customerName, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = if (overdue) DangerRed else MaintenanceBlue, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (rule.customerPhone.isNotBlank()) {
                            Text(
                                rule.customerPhone,
                                style = MaterialTheme.typography.bodySmall,
                                color = ActionBlue,
                                modifier = Modifier.clickable {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${rule.customerPhone.filter { it.isDigit() || it == '+' }}")))
                                }
                            )
                        }
                        if (rule.customerDistrict.isNotBlank()) {
                            Spacer(Modifier.width(6.dp))
                            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape) {
                                Text(rule.customerDistrict, Modifier.padding(horizontal = 7.dp, vertical = 3.dp), fontSize = 10.sp)
                            }
                        }
                    }
                }
                Surface(
                    color = if (rule.status == "Aktif") ActionBlue.copy(alpha = .12f) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (rule.status == "Aktif") ActionBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = CircleShape,
                    modifier = Modifier.clickable(onClick = onToggle)
                ) {
                    Text(if (rule.status == "Aktif") "Aktif Takipte" else "Pasif", Modifier.padding(horizontal = 9.dp, vertical = 6.dp), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoBox("CİHAZ", listOf(rule.deviceBrand, rule.deviceModel).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Cihaz bilgisi yok" }, Modifier.weight(1f))
                InfoBox("SERVİS", "${serviceLabel(rule.serviceType)} · Her ${rule.intervalDays} gün", Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, tint = ActionBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Column(Modifier.weight(1f)) {
                    Text(rule.nextReminderDate, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Hatırlatma tarihi", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    color = if (overdue) Color(0xFFFEE2E2) else Color(0xFFE0F2FE),
                    contentColor = if (overdue) DangerRed else Color(0xFF0E7490),
                    shape = CircleShape
                ) {
                    Text(dayLabel(days, rule.status), Modifier.padding(horizontal = 9.dp, vertical = 5.dp), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onSend,
                    enabled = !sending && rule.customerPhone.isNotBlank(),
                    modifier = Modifier.weight(1f).height(44.dp),
                    contentPadding = PaddingValues(horizontal = 7.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WhatsAppGreen),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                ) {
                    if (sending) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = WhatsAppGreen)
                    else Icon(painterResource(R.drawable.ic_whatsapp), null, modifier = Modifier.size(17.dp), tint = Color(0xFF25D366))
                    Spacer(Modifier.width(5.dp))
                    Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onAppointment,
                    modifier = Modifier.weight(1f).height(44.dp),
                    contentPadding = PaddingValues(horizontal = 7.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ActionBlue),
                    border = BorderStroke(1.dp, Color(0xFFCFE0F5))
                ) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Randevu", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.DeleteOutline, "Bakım takvimini sil", tint = Color(0xFFDC2626))
                }
            }
        }
    }
}

@Composable
private fun InfoBox(label: String, value: String, modifier: Modifier) {
    Surface(modifier, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f), shape = RoundedCornerShape(10.dp)) {
        Column(Modifier.padding(9.dp)) {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun EmptyMaintenanceState(hasRules: Boolean, onClear: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text(if (hasRules) "Bu filtreye uyan bakım takvimi bulunamadı." else "Henüz bakım takvimi oluşturulmadı.", fontWeight = FontWeight.Bold)
            if (hasRules) TextButton(onClick = onClear) { Text("Filtreleri Temizle") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMaintenanceRuleDialog(customers: List<Customer>, onDismiss: () -> Unit, onCreate: (MaintenanceRule) -> Unit) {
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerExpanded by remember { mutableStateOf(false) }
    var serviceType by remember { mutableStateOf("kombi-bakimi") }
    var serviceExpanded by remember { mutableStateOf(false) }
    var dateText by remember { mutableStateOf(LocalDate.now(ZoneId.of("Europe/Istanbul")).plusYears(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))) }
    val services = listOf(
        "kombi-bakimi" to "Yıllık Kombi Bakımı",
        "petek-temizligi" to "Petek & Tesisat Temizliği",
        "kombi-montaji" to "Kombi Montajı & Kontrol",
        "kombi-tamiri" to "Kombi Arıza & Revizyon",
        "genel" to "Genel Periyodik Servis"
    )
    val valid = selectedCustomer != null && parseMaintenanceDate(dateText) != LocalDate.MAX

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Bakım Takvimi", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(customerExpanded, { customerExpanded = !customerExpanded }) {
                    OutlinedTextField(
                        value = selectedCustomer?.name.orEmpty(), onValueChange = {}, readOnly = true,
                        label = { Text("Müşteri") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(customerExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(customerExpanded, { customerExpanded = false }) {
                        customers.forEach { customer ->
                            DropdownMenuItem({ Text(customer.name) }, { selectedCustomer = customer; customerExpanded = false })
                        }
                    }
                }
                ExposedDropdownMenuBox(serviceExpanded, { serviceExpanded = !serviceExpanded }) {
                    OutlinedTextField(
                        value = services.first { it.first == serviceType }.second, onValueChange = {}, readOnly = true,
                        label = { Text("Servis") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(serviceExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(serviceExpanded, { serviceExpanded = false }) {
                        services.forEach { (key, label) -> DropdownMenuItem({ Text(label) }, { serviceType = key; serviceExpanded = false }) }
                    }
                }
                OutlinedTextField(
                    dateText, { dateText = it }, label = { Text("Sonraki Hatırlatma (GG.AA.YYYY)") },
                    singleLine = true, isError = dateText.isNotBlank() && parseMaintenanceDate(dateText) == LocalDate.MAX,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val customer = selectedCustomer ?: return@Button
                    onCreate(
                        MaintenanceRule(
                            id = "m_${UUID.randomUUID().toString().take(8)}",
                            customerId = customer.id,
                            customerName = customer.name,
                            customerPhone = customer.phone,
                            customerDistrict = customer.district,
                            deviceBrand = customer.deviceBrand,
                            deviceModel = customer.deviceModel,
                            serviceType = serviceType,
                            nextReminderDate = dateText,
                            intervalMonths = 12,
                            intervalDays = 365,
                            channel = "WhatsApp"
                        )
                    )
                },
                enabled = valid,
                colors = ButtonDefaults.buttonColors(containerColor = MaintenanceGreen)
            ) { Text("Oluştur") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } }
    )
}

private fun serviceLabel(slug: String): String = when (slug.lowercase()) {
    "kombi-bakimi", "kombi bakımı" -> "Yıllık Kombi Bakımı"
    "petek-temizligi", "petek temizliği" -> "Petek & Tesisat Temizliği"
    "kombi-montaji" -> "Kombi Montajı & Kontrol"
    "kombi-tamiri" -> "Kombi Arıza & Revizyon"
    "dogalgaz-donusumu" -> "Doğalgaz Dönüşümü"
    "genel", "genel servis" -> "Genel Periyodik Servis"
    else -> slug.replace('-', ' ').replaceFirstChar { it.uppercase() }
}

private fun parseMaintenanceDate(value: String): LocalDate = runCatching {
    LocalDate.parse(value, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
}.getOrDefault(LocalDate.MAX)

private fun daysUntil(value: String): Int {
    val date = parseMaintenanceDate(value)
    if (date == LocalDate.MAX) return Int.MAX_VALUE
    return ChronoUnit.DAYS.between(LocalDate.now(ZoneId.of("Europe/Istanbul")), date).toInt()
}

private fun dayLabel(days: Int, status: String): String = when {
    status != "Aktif" -> "Takip pasif"
    days == Int.MAX_VALUE -> "Tarih belirsiz"
    days < 0 -> "${-days} gün gecikti"
    days == 0 -> "Bugün"
    else -> "$days gün sonra"
}
