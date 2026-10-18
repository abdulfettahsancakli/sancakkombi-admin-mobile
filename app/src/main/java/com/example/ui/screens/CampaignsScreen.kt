package com.example.ui.screens

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
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.remote.CampaignCustomerDto
import com.example.data.remote.CampaignDashboardDto
import com.example.data.remote.CampaignDto
import java.time.OffsetDateTime
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CampaignGreen = Color(0xFF16A34A)
private val WhatsAppGreen = Color(0xFF008069)
private val WhatsAppBackground = Color(0xFFEFEAE2)
private val WhatsAppBlue = Color(0xFF0284C7)
private val WarningBackground = Color(0xFFFFF7E6)
private val WarningBorder = Color(0xFFFDE68A)
private val WarningText = Color(0xFFA16207)

private enum class ConsentFilter(val label: String) {
    ALL("Tümü"), GRANTED("İzinli"), UNKNOWN("Belirsiz"), REJECTED("İstemiyor")
}

private enum class PhoneCampaignPage { RECIPIENTS, PREVIEW }

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
    var phonePage by remember { mutableStateOf(PhoneCampaignPage.RECIPIENTS) }
    var campaignName by remember { mutableStateOf("${Year.now().value} Sonbahar Bakım Kampanyası") }
    var confirmNew by remember { mutableStateOf(false) }
    var campaignToResume by remember { mutableStateOf<CampaignDto?>(null) }
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
    val previewCustomer = customers.firstOrNull { it.id == previewId }
        ?: selected.firstOrNull() ?: filtered.firstOrNull() ?: eligible.firstOrNull()
    val messageBody = dashboard.messageBody.ifBlank { dashboard.campaigns.firstOrNull()?.messageBody.orEmpty() }

    LaunchedEffect(customers) {
        selectedIds = selectedIds.intersect(eligible.mapTo(mutableSetOf()) { it.id })
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

    fun toggleCustomer(customer: CampaignCustomerDto) {
        previewId = customer.id
        if (customer.consentStatus == "granted") {
            selectedIds = if (customer.id in selectedIds) selectedIds - customer.id else selectedIds + customer.id
        }
    }

    val shared = CampaignCallbacks(
        onSearchChange = { search = it },
        onFilterChange = { filter = it },
        onSelectAll = { selectedIds = eligible.mapTo(mutableSetOf()) { it.id } },
        onSelectVisible = {
            selectedIds = selectedIds + filtered.filter { it.consentStatus == "granted" }.map { it.id }
        },
        onToggle = ::toggleCustomer,
        onPreview = { previewId = it.id },
        onCampaignNameChange = { campaignName = it },
        onSend = { confirmNew = true },
        onResume = { campaignToResume = it }
    )

    BoxWithConstraints(modifier.fillMaxSize()) {
        if (maxWidth >= 700.dp) {
            TabletCampaignLayout(
                dashboard, customers, eligible, filtered, selectedIds, previewCustomer,
                search, filter, campaignName, messageBody, isLoading, error, busy, progress,
                onBackClick, onRefresh, shared
            )
        } else {
            PhoneCampaignLayout(
                dashboard, customers, eligible, filtered, selectedIds, previewCustomer,
                search, filter, campaignName, messageBody, phonePage, isLoading, error, busy, progress,
                onPageChange = { phonePage = it },
                onBackClick = onBackClick,
                onRefresh = onRefresh,
                callbacks = shared.copy(onPreview = {
                    previewId = it.id
                    phonePage = PhoneCampaignPage.PREVIEW
                })
            )
        }
    }

    if (confirmNew) {
        AlertDialog(
            onDismissRequest = { if (!busy) confirmNew = false },
            title = { Text("Bakım kampanyasını başlat?") },
            text = { Text("${selected.size} izinli müşteriye ${dashboard.templateName} şablonu WhatsApp Cloud API üzerinden gönderilecek.") },
            confirmButton = {
                Button(
                    enabled = !busy && selected.isNotEmpty() && campaignName.isNotBlank(),
                    onClick = {
                        confirmNew = false
                        busy = true
                        onCreateAndSend(campaignName.trim(), selected.map { it.id }, { progress = it }, ::finish)
                    }
                ) { Text("Gönderimi başlat") }
            },
            dismissButton = { TextButton(onClick = { confirmNew = false }) { Text("Vazgeç") } }
        )
    }

    campaignToResume?.let { campaign ->
        AlertDialog(
            onDismissRequest = { if (!busy) campaignToResume = null },
            title = { Text("Gönderime devam et?") },
            text = { Text("${campaign.name} kampanyasının kalan alıcıları yeniden işlenecek.") },
            confirmButton = {
                Button(onClick = {
                    campaignToResume = null
                    busy = true
                    onResumeCampaign(campaign.id, { progress = it }, ::finish)
                }) { Text("Devam et") }
            },
            dismissButton = { TextButton(onClick = { campaignToResume = null }) { Text("Vazgeç") } }
        )
    }
}

private data class CampaignCallbacks(
    val onSearchChange: (String) -> Unit,
    val onFilterChange: (ConsentFilter) -> Unit,
    val onSelectAll: () -> Unit,
    val onSelectVisible: () -> Unit,
    val onToggle: (CampaignCustomerDto) -> Unit,
    val onPreview: (CampaignCustomerDto) -> Unit,
    val onCampaignNameChange: (String) -> Unit,
    val onSend: () -> Unit,
    val onResume: (CampaignDto) -> Unit
)

@Composable
private fun PhoneCampaignLayout(
    dashboard: CampaignDashboardDto,
    customers: List<CampaignCustomerDto>,
    eligible: List<CampaignCustomerDto>,
    filtered: List<CampaignCustomerDto>,
    selectedIds: Set<String>,
    previewCustomer: CampaignCustomerDto?,
    search: String,
    filter: ConsentFilter,
    campaignName: String,
    messageBody: String,
    page: PhoneCampaignPage,
    isLoading: Boolean,
    error: String?,
    busy: Boolean,
    progress: String,
    onPageChange: (PhoneCampaignPage) -> Unit,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    callbacks: CampaignCallbacks
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { CampaignHeader(customers.size, eligible.size, selectedIds.size, busy, progress, onBackClick, onRefresh) }
        item { PhonePageSwitch(page, selectedIds.size, onPageChange) }
        if (dashboard.migrationRequired) item { MigrationWarning() }
        if (error != null) item { ErrorCard(error) }
        if (isLoading && customers.isEmpty()) item { LoadingCard() }

        if (page == PhoneCampaignPage.RECIPIENTS) {
            item { Steps(activeStep = 1, compact = true) }
            item { RecipientToolbar(customers, filtered, selectedIds.size, search, filter, callbacks) }
            if (!isLoading && filtered.isEmpty()) item { EmptyRecipients() }
            items(filtered, key = { it.id }) { customer ->
                CustomerRow(
                    customer, customer.id in selectedIds, customer.id == previewCustomer?.id,
                    onToggle = { callbacks.onToggle(customer) },
                    onPreview = { callbacks.onPreview(customer) }
                )
            }
            item {
                Button(
                    onClick = { onPageChange(PhoneCampaignPage.PREVIEW) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CampaignGreen)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (selectedIds.isNotEmpty()) "Önizle ve gönder (${selectedIds.size})" else "Mesaj önizlemesini aç")
                }
            }
        } else {
            item { Steps(activeStep = if (selectedIds.isEmpty()) 1 else 2, compact = true) }
            item {
                PreviewAndSend(
                    previewCustomer, messageBody, dashboard.templateName, campaignName,
                    selectedIds.size, eligible.size, busy, progress, callbacks
                )
            }
            item { History(dashboard.campaigns, busy, compact = true, onResume = callbacks.onResume) }
        }
    }
}

@Composable
private fun TabletCampaignLayout(
    dashboard: CampaignDashboardDto,
    customers: List<CampaignCustomerDto>,
    eligible: List<CampaignCustomerDto>,
    filtered: List<CampaignCustomerDto>,
    selectedIds: Set<String>,
    previewCustomer: CampaignCustomerDto?,
    search: String,
    filter: ConsentFilter,
    campaignName: String,
    messageBody: String,
    isLoading: Boolean,
    error: String?,
    busy: Boolean,
    progress: String,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    callbacks: CampaignCallbacks
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { CampaignHeader(customers.size, eligible.size, selectedIds.size, busy, progress, onBackClick, onRefresh, tablet = true) }
        if (dashboard.migrationRequired) item { MigrationWarning() }
        if (error != null) item { ErrorCard(error) }
        if (isLoading && customers.isEmpty()) item { LoadingCard() }
        item { Steps(activeStep = if (selectedIds.isEmpty()) 1 else 2, compact = false) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                SectionCard(Modifier.weight(1.3f)) {
                    RecipientToolbar(customers, filtered, selectedIds.size, search, filter, callbacks, insideCard = true)
                    Spacer(Modifier.height(10.dp))
                    if (!isLoading && filtered.isEmpty()) EmptyRecipients()
                    else filtered.forEach { customer ->
                        CustomerRow(
                            customer, customer.id in selectedIds, customer.id == previewCustomer?.id,
                            onToggle = { callbacks.onToggle(customer) },
                            onPreview = { callbacks.onPreview(customer) }
                        )
                        Spacer(Modifier.height(7.dp))
                    }
                }
                Column(Modifier.weight(1f)) {
                    PreviewAndSend(
                        previewCustomer, messageBody, dashboard.templateName, campaignName,
                        selectedIds.size, eligible.size, busy, progress, callbacks
                    )
                }
            }
        }
        item { History(dashboard.campaigns, busy, compact = false, onResume = callbacks.onResume) }
    }
}

@Composable
private fun CampaignHeader(
    customerCount: Int,
    eligibleCount: Int,
    selectedCount: Int,
    busy: Boolean,
    progress: String,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    tablet: Boolean = false
) {
    SectionCard(borderColor = CampaignGreen.copy(alpha = .25f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri") }
            Image(
                painterResource(R.drawable.logo_sancakkombi), "Sancak Kombi",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(if (tablet) 48.dp else 42.dp).clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "0212 581 75 74 · KURUMSAL WHATSAPP SERVİSİ",
                    color = CampaignGreen, style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text("Bakım Kampanyaları", fontSize = if (tablet) 24.sp else 21.sp, fontWeight = FontWeight.ExtraBold)
                if (tablet) Text(
                    "Müşterilerinize kendi isimleriyle kişiselleştirilmiş WhatsApp bakım daveti gönderin.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRefresh, enabled = !busy) { Icon(Icons.Default.Refresh, contentDescription = "Yenile") }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CampaignStat(customerCount.toString(), "Kayıtlı Müşteri", Modifier.weight(1f))
            CampaignStat(eligibleCount.toString(), "Kampanyaya Uygun", Modifier.weight(1f), true)
            CampaignStat(selectedCount.toString(), "Seçili Alıcı", Modifier.weight(1f))
        }
        if (busy) Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = CampaignGreen)
            Spacer(Modifier.width(10.dp))
            Text(progress.ifBlank { "İşleniyor" }, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CampaignStat(value: String, label: String, modifier: Modifier, highlighted: Boolean = false) {
    Surface(
        modifier, shape = RoundedCornerShape(12.dp),
        color = if (highlighted) CampaignGreen.copy(alpha = .09f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f),
        border = BorderStroke(1.dp, if (highlighted) CampaignGreen.copy(alpha = .25f) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(horizontal = 5.dp, vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = CampaignGreen)
            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PhonePageSwitch(page: PhoneCampaignPage, selectedCount: Int, onPageChange: (PhoneCampaignPage) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp), tonalElevation = 1.dp) {
        Row(Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            PhonePageButton("Alıcılar", page == PhoneCampaignPage.RECIPIENTS, Modifier.weight(1f)) { onPageChange(PhoneCampaignPage.RECIPIENTS) }
            PhonePageButton("Önizleme${if (selectedCount > 0) " ($selectedCount)" else ""}", page == PhoneCampaignPage.PREVIEW, Modifier.weight(1f)) { onPageChange(PhoneCampaignPage.PREVIEW) }
        }
    }
}

@Composable
private fun PhonePageButton(text: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier.height(44.dp).clickable(onClick = onClick),
        color = if (active) CampaignGreen else Color.Transparent,
        contentColor = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(10.dp)
    ) { Box(contentAlignment = Alignment.Center) { Text(text, fontWeight = FontWeight.Bold) } }
}

@Composable
private fun Steps(activeStep: Int, compact: Boolean) {
    SectionCard(contentPadding = if (compact) 10.dp else 14.dp) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val labels = listOf(
                "Alıcıları Belirleyin" to "Filtreleyin ve seçin",
                "Önizlemeyi İnceleyin" to "Mesajı kontrol edin",
                "Tek Tıkla Başlatın" to "Cloud API ile iletin"
            )
            labels.forEachIndexed { index, (title, subtitle) ->
                Column(Modifier.weight(1f), horizontalAlignment = if (compact) Alignment.CenterHorizontally else Alignment.Start) {
                    Surface(
                        color = if (index + 1 <= activeStep) CampaignGreen else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (index + 1 <= activeStep) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = CircleShape, modifier = Modifier.size(28.dp)
                    ) { Box(contentAlignment = Alignment.Center) { Text("${index + 1}", fontWeight = FontWeight.ExtraBold) } }
                    Spacer(Modifier.height(5.dp))
                    Text(
                        if (compact) title.substringBefore(" ") else title,
                        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    if (!compact) Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun RecipientToolbar(
    customers: List<CampaignCustomerDto>,
    filtered: List<CampaignCustomerDto>,
    selectedCount: Int,
    search: String,
    filter: ConsentFilter,
    callbacks: CampaignCallbacks,
    insideCard: Boolean = false
) {
    val body: @Composable ColumnScope.() -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Alıcıları Seç", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                Text("Mesaj sadece izinli müşterilere gider.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(color = CampaignGreen.copy(alpha = .12f), shape = CircleShape) {
                Text("$selectedCount seçili", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = CampaignGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(9.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            ConsentFilter.entries.forEach { item ->
                val count = when (item) {
                    ConsentFilter.ALL -> customers.size
                    ConsentFilter.GRANTED -> customers.count { it.consentStatus == "granted" }
                    ConsentFilter.UNKNOWN -> customers.count { it.consentStatus.isBlank() || it.consentStatus == "unknown" }
                    ConsentFilter.REJECTED -> customers.count { it.consentStatus == "rejected" }
                }
                FilterChip(filter == item, { callbacks.onFilterChange(item) }, label = { Text("${item.label} ($count)") })
            }
        }
        OutlinedTextField(
            search, callbacks.onSearchChange,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text("İsim, telefon veya ilçe ile ara...") },
            singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                callbacks.onSelectAll, enabled = customers.any { it.consentStatus == "granted" },
                modifier = Modifier.weight(1f).height(46.dp), contentPadding = PaddingValues(horizontal = 7.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF073368))
            ) { Text("Tüm İzinlileri Seç", maxLines = 1, fontSize = 11.sp) }
            OutlinedButton(
                callbacks.onSelectVisible, enabled = filtered.any { it.consentStatus == "granted" },
                modifier = Modifier.weight(1f).height(46.dp), contentPadding = PaddingValues(horizontal = 7.dp)
            ) { Text("Görünenleri Seç", maxLines = 1, fontSize = 11.sp) }
        }
    }
    if (insideCard) Column(content = body) else SectionCard(content = body)
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
        Modifier.fillMaxWidth().clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = if (previewed) CampaignGreen.copy(alpha = .08f) else MaterialTheme.colorScheme.surface),
        border = BorderStroke(if (previewed) 2.dp else 1.dp, if (previewed) CampaignGreen else MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(selected, { onToggle() }, enabled = eligible)
            Surface(shape = CircleShape, color = CampaignGreen.copy(alpha = .12f), modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(customer.name.trim().firstOrNull()?.uppercase() ?: "?", color = CampaignGreen, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(customer.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
                    if (previewed) Icon(Icons.Default.Visibility, "Önizleniyor", tint = CampaignGreen, modifier = Modifier.padding(start = 5.dp).size(15.dp))
                }
                Text(
                    listOf(customer.phone, customer.district.orEmpty()).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text("Son servis: ${formatDate(customer.lastServiceDate, false)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                ConsentBadge(customer.consentStatus)
                TextButton(onPreview, contentPadding = PaddingValues(horizontal = 7.dp, vertical = 0.dp)) { Text("Önizle", fontSize = 11.sp) }
            }
        }
    }
}

@Composable
private fun ConsentBadge(status: String) {
    val (background, foreground) = when (status) {
        "granted" -> Color(0xFFECFDF3) to Color(0xFF15803D)
        "rejected" -> Color(0xFFFDECEA) to Color(0xFFB42318)
        else -> WarningBackground to WarningText
    }
    Surface(color = background, contentColor = foreground, shape = CircleShape) {
        Text(consentLabel(status), Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun PreviewAndSend(
    customer: CampaignCustomerDto?,
    messageBody: String,
    templateName: String,
    campaignName: String,
    selectedCount: Int,
    eligibleCount: Int,
    busy: Boolean,
    progress: String,
    callbacks: CampaignCallbacks
) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_whatsapp), null, tint = Color(0xFF25D366), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Canlı Mesaj Önizlemesi", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                Text("Müşterinin telefonunda mesaj böyle görünecek.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(10.dp))
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f), shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(Modifier.padding(10.dp)) {
                Text("Önizlenen Müşteri", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(customer?.let { "${it.name} · ${it.phone}" } ?: "Önizlenecek müşteri bulunamadı", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.height(10.dp))
        WhatsAppPreview(customer, messageBody)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            campaignName, callbacks.onCampaignNameChange,
            label = { Text("Kampanya Başlığı (Takip İçin)") }, singleLine = true,
            shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(9.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MiniSummary(selectedCount.toString(), "Hazır Alıcı", Modifier.weight(1f))
            MiniSummary("Kişiye Özel", "Tekil WhatsApp", Modifier.weight(1f))
            MiniSummary("Meta Onaylı", "Resmi Şablon", Modifier.weight(1f))
        }
        Spacer(Modifier.height(9.dp))
        Button(
            callbacks.onSend,
            enabled = !busy && selectedCount > 0 && campaignName.isNotBlank() && messageBody.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CampaignGreen, disabledContainerColor = Color(0xFFA7E8C0))
        ) {
            if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
            else Icon(Icons.AutoMirrored.Filled.Send, null)
            Spacer(Modifier.width(8.dp))
            Text(
                if (busy) progress.ifBlank { "Gönderiliyor" }
                else if (selectedCount > 0) "$selectedCount Müşteriye WhatsApp Gönder"
                else "Gönderim İçin Müşteri Seçin",
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        if (selectedCount == 0) {
            Spacer(Modifier.height(8.dp))
            Surface(color = WarningBackground, contentColor = WarningText, shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, WarningBorder)) {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("En az 1 izinli müşteri seçmelisiniz.", Modifier.weight(1f), fontSize = 12.sp)
                    TextButton(callbacks.onSelectAll, enabled = eligibleCount > 0) { Text("Tümünü Seç ($eligibleCount)", fontSize = 10.sp) }
                }
            }
        }
        Row(Modifier.padding(top = 9.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Lock, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(6.dp))
            Text(
                "Güvenli İletim: Meta onaylı $templateName şablonuyla WhatsApp Cloud API üzerinden iletilir.",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WhatsAppPreview(customer: CampaignCustomerDto?, messageBody: String) {
    val rendered = if (messageBody.isBlank()) "Şablon önizlemesi sunucudan alınamadı."
    else messageBody.replace("{{customer_name}}", customer?.name ?: "[Müşteri adı]")
    Surface(shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), color = WhatsAppBackground) {
        Column {
            Row(
                Modifier.background(WhatsAppGreen).padding(horizontal = 9.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ChevronLeft, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Image(painterResource(R.drawable.logo_sancakkombi), null, contentScale = ContentScale.Crop, modifier = Modifier.size(34.dp).clip(CircleShape))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Sancak Kombi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Icon(Icons.Default.CheckCircle, "Doğrulanmış", tint = Color(0xFF25D366), modifier = Modifier.padding(start = 4.dp).size(14.dp))
                    }
                    Text("Resmi İşletme Hesabı · 0212 581 75 74", color = Color.White.copy(alpha = .84f), fontSize = 9.sp, maxLines = 1)
                }
            }
            Column(Modifier.padding(12.dp)) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Surface(color = Color.White.copy(alpha = .85f), shape = RoundedCornerShape(7.dp)) {
                        Text("BUGÜN", Modifier.padding(horizontal = 9.dp, vertical = 4.dp), color = Color(0xFF54656F), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(9.dp))
                Surface(color = Color.White, contentColor = Color(0xFF111B21), shape = RoundedCornerShape(2.dp, 10.dp, 10.dp, 10.dp), shadowElevation = 1.dp) {
                    Column(Modifier.padding(horizontal = 11.dp, vertical = 9.dp)) {
                        Text(rendered, fontSize = 12.sp, lineHeight = 17.sp)
                        Text("22:20  ✓✓", Modifier.align(Alignment.End), color = Color(0xFF53BDEB), fontSize = 9.sp)
                    }
                }
                listOf("📅 Randevu Al", "📞 Beni Arayın", "🔕 İlgilenmiyorum").forEach { label ->
                    Surface(color = Color.White, contentColor = WhatsAppBlue, shape = RoundedCornerShape(8.dp), shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth().padding(top = 5.dp)) {
                        Text(label, Modifier.padding(9.dp), textAlign = TextAlign.Center, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniSummary(value: String, label: String, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(11.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(horizontal = 3.dp, vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = CampaignGreen, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun History(campaigns: List<CampaignDto>, busy: Boolean, compact: Boolean, onResume: (CampaignDto) -> Unit) {
    SectionCard {
        Text("Kampanya Geçmişi & Raporlar", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        Text("Daha önce gönderilen kampanyalar ve müşteri geri dönüşleri.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        if (campaigns.isEmpty()) Text("Henüz gönderilmiş kampanya yok.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        campaigns.forEach { campaign ->
            Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .4f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                if (compact) Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CampaignHistoryLead(campaign)
                    campaign.recipients.take(3).forEach { recipient ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(recipient.customerName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            StatusBadge(recipientStatus(recipient.status), recipient.status)
                        }
                    }
                    ResumeButton(campaign, busy, onResume)
                } else Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1.6f)) { CampaignHistoryLead(campaign) }
                    Text(campaign.recipients.firstOrNull()?.customerName ?: "—", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    Text(formatDate(campaign.createdAt), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    Column(Modifier.weight(.9f), horizontalAlignment = Alignment.End) {
                        StatusBadge(campaignStatus(campaign.status), campaign.status)
                        ResumeButton(campaign, busy, onResume)
                    }
                }
            }
        }
    }
}

@Composable
private fun CampaignHistoryLead(campaign: CampaignDto) {
    Text(campaign.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    Text(
        "${formatDate(campaign.createdAt, false)} · ${campaign.counts["sent"] ?: campaign.recipientCount}/${campaign.recipientCount} gönderildi · ${campaign.counts["read"] ?: 0} okundu · ${campaign.counts["responded"] ?: 0} dönüş",
        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ResumeButton(campaign: CampaignDto, busy: Boolean, onResume: (CampaignDto) -> Unit) {
    if (campaign.status in setOf("partial", "failed", "sending")) {
        TextButton({ onResume(campaign) }, enabled = !busy) { Text("Devam et", fontSize = 11.sp) }
    }
}

@Composable
private fun StatusBadge(label: String, status: String) {
    val color = when (status) {
        "completed", "read", "responded", "delivered" -> CampaignGreen
        "partial", "failed" -> Color(0xFFD97706)
        "sending", "sent" -> WhatsAppBlue
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(color = color.copy(alpha = .12f), contentColor = color, shape = CircleShape) {
        Text(label, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    contentPadding: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, borderColor), shape = RoundedCornerShape(18.dp)
    ) { Column(Modifier.padding(contentPadding), content = content) }
}

@Composable private fun LoadingCard() = SectionCard {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(Modifier.size(22.dp)); Spacer(Modifier.width(10.dp)); Text("Kampanya verileri yükleniyor…")
    }
}

@Composable private fun EmptyRecipients() = SectionCard {
    Text("Bu filtreye uyan müşteri bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable private fun MigrationWarning() {
    Surface(color = WarningBackground, contentColor = WarningText, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, WarningBorder), modifier = Modifier.fillMaxWidth()) {
        Text("Kampanya tabloları sunucuda hazır değil. Supabase kampanya migration’ı çalıştırılmalı.", Modifier.padding(12.dp))
    }
}

@Composable private fun ErrorCard(message: String) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Text(message, Modifier.padding(12.dp))
    }
}

private fun consentLabel(status: String) = when (status) {
    "granted" -> "İzinli"; "rejected" -> "İstemiyor"; else -> "Belirsiz"
}

private fun campaignStatus(status: String) = when (status) {
    "completed" -> "Tamamlandı"; "partial" -> "Kısmi / Hatalı"; "sending" -> "Gönderiliyor"; "failed" -> "Başarısız"; else -> "Taslak"
}

private fun recipientStatus(status: String) = when (status) {
    "sent" -> "Gönderildi"; "delivered" -> "Teslim edildi"; "read" -> "Okundu"; "responded" -> "Yanıtladı"
    "opted_out" -> "İletişimden ayrıldı"; "failed" -> "Başarısız"; "skipped" -> "Atlandı"; "sending" -> "Gönderiliyor"; else -> "Sırada"
}

private fun formatDate(value: String?, includeTime: Boolean = true): String {
    if (value.isNullOrBlank()) return "—"
    return runCatching {
        val pattern = if (includeTime) "dd MMM yyyy HH:mm" else "dd MMM yyyy"
        OffsetDateTime.parse(value).format(DateTimeFormatter.ofPattern(pattern, Locale("tr", "TR")))
    }.getOrDefault(value.take(10))
}
