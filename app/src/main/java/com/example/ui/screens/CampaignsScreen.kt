package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.remote.CampaignCustomerDto
import com.example.data.remote.CampaignDashboardDto
import com.example.data.remote.CampaignDto
import java.time.OffsetDateTime
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class ConsentFilter(val label: String) {
    ALL("Tümü"), GRANTED("İzinli"), UNKNOWN("Belirsiz"), REJECTED("İstemiyor")
}

@Composable
fun CampaignsScreen(
    dashboard: CampaignDashboardDto,
    isLoading: Boolean,
    error: String?,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onCreateAndSend: (String, List<String>, (String) -> Unit, (Result<Unit>) -> Unit) -> Unit,
    onResumeCampaign: (String, (String) -> Unit, (Result<Unit>) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(ConsentFilter.ALL) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var previewId by remember { mutableStateOf<String?>(null) }
    var campaignName by remember { mutableStateOf(Year.now().value.toString() + " Sonbahar Bakım Kampanyası") }
    var confirmNew by remember { mutableStateOf(false) }
    var resumeCampaign by remember { mutableStateOf<CampaignDto?>(null) }
    var busy by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf("") }

    val customers = dashboard.customers
    val eligible = remember(customers) { customers.filter { it.consentStatus == "granted" } }
    val filtered = remember(customers, search, filter) {
        val query = search.trim().lowercase(Locale("tr", "TR"))
        customers.filter { customer ->
            val queryMatch = query.isBlank() ||
                (customer.name + " " + customer.phone + " " + customer.district.orEmpty())
                    .lowercase(Locale("tr", "TR")).contains(query)
            val filterMatch = when (filter) {
                ConsentFilter.ALL -> true
                ConsentFilter.GRANTED -> customer.consentStatus == "granted"
                ConsentFilter.UNKNOWN -> customer.consentStatus.isBlank() || customer.consentStatus == "unknown"
                ConsentFilter.REJECTED -> customer.consentStatus == "rejected"
            }
            queryMatch && filterMatch
        }
    }
    val selected = customers.filter { it.id in selectedIds && it.consentStatus == "granted" }
    val previewCustomer = customers.firstOrNull { it.id == previewId } ?: selected.firstOrNull() ?: eligible.firstOrNull()
    val messageBody = dashboard.messageBody.ifBlank { dashboard.campaigns.firstOrNull()?.messageBody.orEmpty() }

    LaunchedEffect(customers) {
        val eligibleIds = eligible.mapTo(mutableSetOf()) { it.id }
        selectedIds = selectedIds.intersect(eligibleIds)
        if (customers.none { it.id == previewId }) previewId = null
    }

    fun finish(result: Result<Unit>) {
        busy = false
        progress = ""
        result.onSuccess {
            selectedIds = emptySet()
            Toast.makeText(context, "Kampanya gönderimi tamamlandı", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(context, it.message ?: "Kampanya gönderilemedi", Toast.LENGTH_LONG).show()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Header(
                customerCount = customers.size,
                eligibleCount = eligible.size,
                selectedCount = selected.size,
                busy = busy,
                progress = progress,
                onBackClick = onBackClick,
                onRefresh = onRefresh
            )
        }
        if (dashboard.migrationRequired) {
            item {
                Text(
                    "Kampanya tabloları sunucuda hazır değil. Supabase kampanya migrationı çalıştırılmalı.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        if (error != null) item { Text(error, color = MaterialTheme.colorScheme.error) }
        if (isLoading && customers.isEmpty()) item { CircularProgressIndicator(Modifier.padding(24.dp)) }
        item { Steps() }
        item {
            RecipientToolbar(
                search = search,
                onSearchChange = { search = it },
                filter = filter,
                onFilterChange = { filter = it },
                eligibleCount = eligible.size,
                onSelectAll = { selectedIds = eligible.mapTo(mutableSetOf()) { it.id } }
            )
        }
        if (!isLoading && filtered.isEmpty()) {
            item { Text("Bu filtreye uyan müşteri bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        items(filtered, key = { it.id }) { customer ->
            CustomerRow(
                customer = customer,
                selected = customer.id in selectedIds,
                previewed = customer.id == previewCustomer?.id,
                onToggle = {
                    previewId = customer.id
                    if (customer.consentStatus == "granted") {
                        selectedIds = if (customer.id in selectedIds) selectedIds - customer.id else selectedIds + customer.id
                    }
                },
                onPreview = { previewId = customer.id }
            )
        }
        item {
            PreviewCard(
                customer = previewCustomer,
                messageBody = messageBody,
                templateName = dashboard.templateName,
                campaignName = campaignName,
                onCampaignNameChange = { campaignName = it },
                selectedCount = selected.size,
                busy = busy,
                progress = progress,
                onSend = { confirmNew = true }
            )
        }
        item {
            History(dashboard.campaigns, busy) { resumeCampaign = it }
        }
    }

    if (confirmNew) {
        AlertDialog(
            onDismissRequest = { if (!busy) confirmNew = false },
            title = { Text("Bakım kampanyasını başlat?") },
            text = {
                Text(
                    selected.size.toString() + " izinli müşteriye " + dashboard.templateName +
                        " şablonu WhatsApp Cloud API üzerinden gönderilecek."
                )
            },
            confirmButton = {
                Button(
                    enabled = !busy && selected.isNotEmpty() && campaignName.isNotBlank(),
                    onClick = {
                        confirmNew = false
                        busy = true
                        onCreateAndSend(
                            campaignName.trim(),
                            selected.map { it.id },
                            { progress = it },
                            { finish(it) }
                        )
                    }
                ) { Text("Gönderimi başlat") }
            },
            dismissButton = { TextButton(onClick = { confirmNew = false }) { Text("Vazgeç") } }
        )
    }

    resumeCampaign?.let { campaign ->
        AlertDialog(
            onDismissRequest = { if (!busy) resumeCampaign = null },
            title = { Text("Gönderime devam et?") },
            text = { Text(campaign.name + " kampanyasının kalan alıcıları yeniden işlenecek.") },
            confirmButton = {
                Button(onClick = {
                    resumeCampaign = null
                    busy = true
                    onResumeCampaign(campaign.id, { progress = it }, { finish(it) })
                }) { Text("Devam et") }
            },
            dismissButton = { TextButton(onClick = { resumeCampaign = null }) { Text("Vazgeç") } }
        )
    }
}

@Composable
private fun Header(
    customerCount: Int,
    eligibleCount: Int,
    selectedCount: Int,
    busy: Boolean,
    progress: String,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri") }
            Column(Modifier.weight(1f)) {
                Text("Bakım Kampanyaları", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Meta onaylı WhatsApp bakım daveti", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onRefresh, enabled = !busy) { Icon(Icons.Default.Refresh, contentDescription = "Yenile") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat(customerCount.toString(), "Kayıtlı", Modifier.weight(1f))
            Stat(eligibleCount.toString(), "Uygun", Modifier.weight(1f))
            Stat(selectedCount.toString(), "Seçili", Modifier.weight(1f))
        }
        if (busy) {
            Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text(progress.ifBlank { "İşleniyor" })
            }
        }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun Steps() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("1  Alıcı seç", "2  Önizle", "3  Başlat").forEach { label ->
            Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(label, Modifier.padding(9.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RecipientToolbar(
    search: String,
    onSearchChange: (String) -> Unit,
    filter: ConsentFilter,
    onFilterChange: (ConsentFilter) -> Unit,
    eligibleCount: Int,
    onSelectAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Alıcıları seç", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Mesaj yalnızca kampanya izni bulunan müşterilere gider.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = search,
            onValueChange = onSearchChange,
            label = { Text("İsim, telefon veya ilçe ara") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ConsentFilter.entries.forEach { item ->
                FilterChip(selected = filter == item, onClick = { onFilterChange(item) }, label = { Text(item.label) })
            }
        }
        OutlinedButton(onClick = onSelectAll, enabled = eligibleCount > 0, modifier = Modifier.fillMaxWidth()) {
            Text("Tüm izinlileri seç (" + eligibleCount + ")")
        }
    }
}

@Composable
private fun CustomerRow(
    customer: CampaignCustomerDto,
    selected: Boolean,
    previewed: Boolean,
    onToggle: () -> Unit,
    onPreview: () -> Unit
) {
    val eligible = customer.consentStatus == "granted"
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onPreview),
        colors = CardDefaults.cardColors(
            containerColor = if (previewed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = selected, onCheckedChange = { onToggle() }, enabled = eligible)
            Column(Modifier.weight(1f)) {
                Text(customer.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    listOf(customer.phone, customer.district.orEmpty()).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Son servis: " + formatDate(customer.lastServiceDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(consentLabel(customer.consentStatus), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun PreviewCard(
    customer: CampaignCustomerDto?,
    messageBody: String,
    templateName: String,
    campaignName: String,
    onCampaignNameChange: (String) -> Unit,
    selectedCount: Int,
    busy: Boolean,
    progress: String,
    onSend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Canlı mesaj önizlemesi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                customer?.let { it.name + " · " + it.phone } ?: "Önizlenecek müşteri bulunamadı",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(
                    if (messageBody.isBlank()) "Şablon önizlemesi sunucudan alınamadı."
                    else messageBody.replace("{{customer_name}}", customer?.name ?: "[Müşteri adı]"),
                    Modifier.padding(14.dp)
                )
            }
            listOf("Randevu Al", "Beni Arayın", "İlgilenmiyorum").forEach {
                OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) { Text(it) }
            }
            OutlinedTextField(
                value = campaignName,
                onValueChange = onCampaignNameChange,
                label = { Text("Kampanya başlığı") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Stat(selectedCount.toString(), "Hazır alıcı", Modifier.weight(1f))
                Stat("Kişiye özel", "Tekil", Modifier.weight(1f))
                Stat("Meta", "Onaylı", Modifier.weight(1f))
            }
            Button(
                onClick = onSend,
                enabled = !busy && selectedCount > 0 && campaignName.isNotBlank() && messageBody.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (busy) progress.ifBlank { "Gönderiliyor" } else selectedCount.toString() + " müşteriye gönder")
            }
            if (selectedCount == 0) {
                Text("Gönderim için en az bir izinli müşteri seçin.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "Güvenli iletim: " + templateName + " şablonu WhatsApp Cloud API üzerinden gönderilir.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun History(campaigns: List<CampaignDto>, busy: Boolean, onResume: (CampaignDto) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Kampanya geçmişi ve raporlar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (campaigns.isEmpty()) Text("Henüz gönderilmiş kampanya yok.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        campaigns.forEach { campaign ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(campaign.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(campaignStatus(campaign.status), style = MaterialTheme.typography.labelMedium)
                    }
                    Text(
                        formatDate(campaign.createdAt) + " · " + campaign.recipientCount + " alıcı · " +
                            (campaign.counts["read"] ?: 0) + " okundu · " +
                            (campaign.counts["responded"] ?: 0) + " dönüş",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    campaign.recipients.take(5).forEach { recipient ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(recipient.customerName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Text(recipientStatus(recipient.status), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    if (campaign.status in setOf("partial", "failed", "sending")) {
                        OutlinedButton(onClick = { onResume(campaign) }, enabled = !busy, modifier = Modifier.align(Alignment.End)) {
                            Text("Gönderime devam et")
                        }
                    }
                }
            }
        }
    }
}

private fun consentLabel(status: String) = when (status) {
    "granted" -> "İzinli"
    "rejected" -> "İstemiyor"
    else -> "Belirsiz"
}

private fun campaignStatus(status: String) = when (status) {
    "completed" -> "Tamamlandı"
    "partial" -> "Kısmi / Hatalı"
    "sending" -> "Gönderiliyor"
    "failed" -> "Başarısız"
    else -> "Taslak"
}

private fun recipientStatus(status: String) = when (status) {
    "sent" -> "Gönderildi"
    "delivered" -> "Teslim edildi"
    "read" -> "Okundu"
    "responded" -> "Yanıtladı"
    "opted_out" -> "İletişimden ayrıldı"
    "failed" -> "Başarısız"
    "skipped" -> "Atlandı"
    "sending" -> "Gönderiliyor"
    else -> "Sırada"
}

private fun formatDate(value: String?): String {
    if (value.isNullOrBlank()) return "—"
    return runCatching {
        OffsetDateTime.parse(value).format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale("tr", "TR")))
    }.getOrDefault(value.take(10))
}
