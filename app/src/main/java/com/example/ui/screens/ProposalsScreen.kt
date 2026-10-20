package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CatalogItem
import com.example.data.model.Proposal
import com.example.data.model.ProposalStatus
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ProposalsScreen(
    proposals: List<Proposal>,
    onBackClick: () -> Unit,
    onNewProposalClick: () -> Unit,
    onViewProposalDetail: (Proposal) -> Unit,
    onDeleteProposal: (String, (Result<Unit>) -> Unit) -> Unit,
    catalogItems: List<CatalogItem> = emptyList(),
    onUpdateProposalStatus: ((String, ProposalStatus) -> Unit)? = null,
    onCreateProposal: ((Proposal) -> Unit)? = null,
    onSelectProposal: ((Proposal) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val tokens = rememberProposalTokens()
    var selectedProposalId by remember(proposals) {
        mutableStateOf(proposals.firstOrNull()?.id)
    }
    var showTabletNewProposalDialog by remember { mutableStateOf(false) }
    var proposalPendingDeletion by remember { mutableStateOf<Proposal?>(null) }
    var isDeletingProposal by remember { mutableStateOf(false) }
    val context = LocalContext.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp

        if (isTablet) {
            // TABLET GÖRÜNÜMÜ (SPLIT VIEW & MASTER-DETAIL)
            ProposalsTabletLandscapeSplitView(
                proposals = proposals,
                selectedProposalId = selectedProposalId,
                onSelectProposal = { prop ->
                    selectedProposalId = prop.id
                    onSelectProposal?.invoke(prop)
                },
                onBackClick = onBackClick,
                onOpenNewProposalDialog = { showTabletNewProposalDialog = true },
                onUpdateStatus = { id, st -> onUpdateProposalStatus?.invoke(id, st) },
                onDeleteProposal = { id -> proposalPendingDeletion = proposals.find { it.id == id } },
                tokens = tokens
            )

            if (showTabletNewProposalDialog) {
                NewProposalTabletDialog(
                    catalogItems = catalogItems,
                    onDismissRequest = { showTabletNewProposalDialog = false },
                    onCreateProposal = { newProp ->
                        onCreateProposal?.invoke(newProp)
                        selectedProposalId = newProp.id
                    },
                    tokens = tokens
                )
            }
        } else {
            // TELEFON GÖRÜNÜMÜ (< 600.dp)
            ProposalsPhoneView(
                proposals = proposals,
                onBackClick = onBackClick,
                onNewProposalClick = onNewProposalClick,
                onViewProposalDetail = onViewProposalDetail,
                onDeleteProposal = { id -> proposalPendingDeletion = proposals.find { it.id == id } },
                onUpdateStatus = onUpdateProposalStatus,
                tokens = tokens
            )
        }
    }

    proposalPendingDeletion?.let { proposal ->
        AlertDialog(
            onDismissRequest = { if (!isDeletingProposal) proposalPendingDeletion = null },
            title = { Text("Teklif silinsin mi?") },
            text = {
                Text(
                    "${proposal.quoteNumber.ifBlank { proposal.id }} numaralı ${proposal.customerName} teklifi kalıcı olarak silinecek."
                )
            },
            dismissButton = {
                TextButton(
                    enabled = !isDeletingProposal,
                    onClick = { proposalPendingDeletion = null }
                ) {
                    Text("Vazgeç")
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isDeletingProposal,
                    onClick = {
                        isDeletingProposal = true
                        onDeleteProposal(proposal.id) { result ->
                            isDeletingProposal = false
                            if (result.isSuccess) {
                                proposalPendingDeletion = null
                                android.widget.Toast.makeText(context, "Teklif silindi.", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(
                                    context,
                                    result.exceptionOrNull()?.message ?: "Teklif silinemedi.",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                ) {
                    Text(if (isDeletingProposal) "Siliniyor…" else "Sil", color = tokens.red)
                }
            }
        )
    }
}

@Composable
fun ProposalsPhoneView(
    proposals: List<Proposal>,
    onBackClick: () -> Unit,
    onNewProposalClick: () -> Unit,
    onViewProposalDetail: (Proposal) -> Unit,
    onDeleteProposal: (String) -> Unit,
    tokens: ProposalTokens,
    onUpdateStatus: ((String, ProposalStatus) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cloudWhatsApp = LocalCloudWhatsAppActions.current
    val coroutineScope = rememberCoroutineScope()
    var isDownloadingPdf by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Tümü") }

    val kpis = remember(proposals) { calculateProposalKpis(proposals) }

    val filteredProposals = remember(proposals, searchQuery, selectedTab) {
        val q = searchQuery.trim().lowercase(Locale("tr"))
        proposals.filter { p ->
            val matchQuery = q.isEmpty() ||
                p.customerName.lowercase(Locale("tr")).contains(q) ||
                p.customerPhone.contains(q) ||
                p.quoteNumber.lowercase(Locale("tr")).contains(q) ||
                p.id.lowercase(Locale("tr")).contains(q) ||
                p.deviceBrand.lowercase(Locale("tr")).contains(q) ||
                p.deviceModel.lowercase(Locale("tr")).contains(q)

            val matchStatus = when (selectedTab) {
                "Beklemede" -> p.status == ProposalStatus.PENDING
                "Kabul Edildi" -> p.status == ProposalStatus.APPROVED
                "Reddedildi" -> p.status == ProposalStatus.REJECTED
                "Süresi Doldu" -> p.status == ProposalStatus.EXPIRED
                else -> true
            }

            matchQuery && matchStatus
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = tokens.bg
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Kompakt Üst Bar (Randevular / Müşteriler ile uyumlu)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                onClick = onBackClick,
                                shape = RoundedCornerShape(12.dp),
                                color = tokens.surface,
                                border = BorderStroke(1.dp, tokens.border),
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("back_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Geri",
                                        tint = tokens.text,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Fiyat Teklifleri",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = tokens.text,
                                    letterSpacing = (-0.4).sp
                                )
                                Text(
                                    text = "Teklif takip ve onay sistemi",
                                    fontSize = 11.sp,
                                    color = tokens.text2
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = tokens.primaryC
                        ) {
                            Text(
                                text = "${proposals.size} Kayıt",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = tokens.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 4 Kompakt KPI Kartı (2x2 Grid)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            PhoneKpiCard(
                                title = "TOPLAM TEKLİF HACMİ",
                                value = formatProposalCurrencyShort(kpis.totalVolume),
                                subtitle = "${kpis.totalCount} adet teklif",
                                tokens = tokens,
                                modifier = Modifier.weight(1f)
                            )
                            PhoneKpiCard(
                                title = "BEKLEYEN",
                                value = "${kpis.pendingCount}",
                                subtitle = "${formatProposalCurrencyShort(kpis.pendingVolume)} potansiyel",
                                tokens = tokens,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            PhoneKpiCard(
                                title = "KABUL EDİLEN",
                                value = "${kpis.approvedCount}",
                                subtitle = "%${kpis.approvedRatio} başarı oranı",
                                tokens = tokens,
                                modifier = Modifier.weight(1f)
                            )
                            PhoneKpiCard(
                                title = "RED / SÜRESİ DOLAN",
                                value = "${kpis.rejectedCount} Red",
                                subtitle = "${kpis.expiredCount} süresi doldu",
                                tokens = tokens,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Arama Kutusu
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("🔍 Müşteri, telefon, marka, teklif no…", fontSize = 11.5.sp, color = tokens.text2) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filtre Çipleri
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Tümü", "Beklemede", "Kabul Edildi", "Reddedildi", "Süresi Doldu").forEach { tab ->
                            val isSel = selectedTab == tab
                            val count = when (tab) {
                                "Beklemede" -> kpis.pendingCount
                                "Kabul Edildi" -> kpis.approvedCount
                                "Reddedildi" -> kpis.rejectedCount
                                "Süresi Doldu" -> kpis.expiredCount
                                else -> kpis.totalCount
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) tokens.primary else tokens.surface)
                                    .border(1.dp, if (isSel) tokens.primary else tokens.border, RoundedCornerShape(10.dp))
                                    .clickable { selectedTab = tab }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "$tab $count",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) tokens.onPrimary else tokens.text2
                                )
                            }
                        }
                    }
                }

                // Teklif Kartları Listesi
                if (filteredProposals.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Bu kritere uygun teklif kaydı bulunamadı.",
                                    fontSize = 13.sp,
                                    color = tokens.text2
                                )
                            }
                        }
                    }
                } else {
                    items(filteredProposals, key = { it.id }) { proposal ->
                        val vis = getProposalStatusVisual(proposal.status, tokens)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onViewProposalDetail(proposal) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = tokens.surface),
                            border = BorderStroke(1.dp, tokens.border)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 11.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 1. Satır: Marka Kutusu + Marka/Model + Teklif No + Durum Rozeti
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        BrandLogoBadge(
                                            brand = proposal.deviceBrand,
                                            size = 36.dp,
                                            tokens = tokens
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            val dev = listOf(proposal.deviceBrand, proposal.deviceModel)
                                                .filter { it.isNotBlank() }
                                                .joinToString(" · ")
                                            Text(
                                                text = dev.ifBlank { "Genel Cihaz" },
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = tokens.text,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = proposal.quoteNumber.ifBlank { proposal.id },
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = tokens.primary
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, vis.accentColor, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = vis.label,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = vis.textColor
                                            )
                                        }
                                    }

                                    // 2. Satır: Müşteri Adı, Telefon, Tarih ve TOPLAM
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = proposal.customerName,
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = tokens.text
                                                )
                                                if (proposal.customerDistrict.isNotBlank()) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "📍 ${proposal.customerDistrict}",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = tokens.text2
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "${proposal.customerPhone} · ${proposal.date}",
                                                fontSize = 10.5.sp,
                                                color = tokens.text2
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "TOPLAM",
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = tokens.text2
                                            )
                                            Text(
                                                text = formatProposalCurrency(proposal.grandTotal),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = tokens.primary
                                            )
                                            if (proposal.downPayment > 0.0) {
                                                Text(
                                                    text = "Peşinat: ${formatProposalCurrency(proposal.downPayment)}",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = tokens.text2
                                                )
                                            }
                                        }
                                    }

                                    // 3. Satır: Hızlı Aksiyon Butonları (WhatsApp, Ara, PDF, Sil)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .border(1.dp, tokens.border, RoundedCornerShape(10.dp))
                                                .clickable { sendProposalWhatsApp(context, proposal, cloudWhatsApp) }
                                                .padding(vertical = 7.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "WhatsApp",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = tokens.primary
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .border(1.dp, tokens.border, RoundedCornerShape(10.dp))
                                                .clickable { dialProposalPhone(context, proposal.customerPhone) }
                                                .padding(vertical = 7.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Ara",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = tokens.primary
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .border(1.dp, tokens.border, RoundedCornerShape(10.dp))
                                                .clickable {
                                                    if (!isDownloadingPdf) {
                                                        coroutineScope.launch {
                                                            downloadAndShareQuotePdf(
                                                                context = context,
                                                                quoteId = proposal.id,
                                                                quoteNumber = proposal.quoteNumber.ifBlank { proposal.id },
                                                                onLoadingStateChange = { isDownloadingPdf = it }
                                                            )
                                                        }
                                                    }
                                                }
                                                .padding(vertical = 7.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isDownloadingPdf) "..." else "PDF",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = tokens.text
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(tokens.redC)
                                                .border(1.dp, tokens.red.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                                .clickable { onDeleteProposal(proposal.id) }
                                                .padding(vertical = 7.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = null,
                                                    tint = tokens.red,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = "Sil",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = tokens.red
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Alt boşluk (Floating bar için)
                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }

            // Sabit Alt Aksiyon Butonu
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Button(
                    onClick = onNewProposalClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.primary,
                        contentColor = tokens.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = "+ Yeni Teklif Oluştur",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun PhoneKpiCard(
    title: String,
    value: String,
    subtitle: String,
    tokens: ProposalTokens,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.surface),
        border = BorderStroke(1.dp, tokens.border)
    ) {
        Column(modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp)) {
            Text(text = title, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = tokens.text2, letterSpacing = 0.2.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 13.5.sp, fontWeight = FontWeight.Black, color = tokens.text)
            Text(text = subtitle, fontSize = 8.5.sp, color = tokens.text2)
        }
    }
}
