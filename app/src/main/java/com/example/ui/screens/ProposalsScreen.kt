package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    onDeleteProposal: (String) -> Unit,
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
                onDeleteProposal = onDeleteProposal,
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
                onUpdateStatus = onUpdateProposalStatus,
                tokens = tokens
            )
        }
    }
}

@Composable
fun ProposalsPhoneView(
    proposals: List<Proposal>,
    onBackClick: () -> Unit,
    onNewProposalClick: () -> Unit,
    onViewProposalDetail: (Proposal) -> Unit,
    tokens: ProposalTokens,
    onUpdateStatus: ((String, ProposalStatus) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isDownloadingPdf by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Tümü") }
    var selectedBrand by remember { mutableStateOf("Tüm Markalar") }

    val kpis = remember(proposals) { calculateProposalKpis(proposals) }

    val filteredProposals = remember(proposals, searchQuery, selectedTab, selectedBrand) {
        val q = searchQuery.trim().lowercase(Locale("tr"))
        proposals.filter { p ->
            val matchQuery = q.isEmpty() ||
                p.customerName.lowercase(Locale("tr")).contains(q) ||
                p.customerPhone.contains(q) ||
                p.quoteNumber.lowercase(Locale("tr")).contains(q) ||
                p.id.lowercase(Locale("tr")).contains(q)

            val matchStatus = when (selectedTab) {
                "Beklemede" -> p.status == ProposalStatus.PENDING
                "Kabul Edildi" -> p.status == ProposalStatus.APPROVED
                "Reddedildi" -> p.status == ProposalStatus.REJECTED
                "Süresi Doldu" -> p.status == ProposalStatus.EXPIRED
                else -> true
            }

            val matchBrand = if (selectedBrand == "Tüm Markalar") true else p.deviceBrand.equals(selectedBrand, ignoreCase = true)
            matchQuery && matchStatus && matchBrand
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
                    Spacer(modifier = Modifier.height(10.dp))

                    // Geri Barı
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onBackClick,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.text),
                            modifier = Modifier.testTag("back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Geri", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Fiyat Teklifleri",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = tokens.text
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(tokens.primaryC)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${proposals.size} kayıt",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = tokens.primary
                            )
                        }
                    }
                    Text(
                        text = "Arıza, montaj, bakım ve kurumsal servis fiyat tekliflerini oluşturun ve durumlarını takip edin.",
                        fontSize = 12.sp,
                        color = tokens.text2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4 KPI KARTI (2x2 GRID)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
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
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Arama Kutusu
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("🔍 Müşteri, telefon, teklif no ara…", fontSize = 12.sp, color = tokens.text2) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

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
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Marka Filtresi (MARKA FİLTRESİ:)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MARKA:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tokens.text2
                        )
                        val brandList = listOf("Tüm Markalar") + PROPOSAL_BRANDS
                        brandList.forEach { brand ->
                            val isSel = selectedBrand == brand
                            val brandCount = if (brand == "Tüm Markalar") proposals.size else proposals.count { it.deviceBrand.equals(brand, ignoreCase = true) }
                            if (brand == "Tüm Markalar" || brandCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) tokens.primaryC else tokens.surface2)
                                        .border(1.dp, if (isSel) tokens.primary else tokens.border, RoundedCornerShape(8.dp))
                                        .clickable { selectedBrand = brand }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (brand == "Tüm Markalar") "Tümü ($brandCount)" else "$brand ($brandCount)",
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) tokens.primary else tokens.text
                                    )
                                }
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
                            colors = CardDefaults.cardColors(containerColor = tokens.surface),
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
                        val mono = getBrandMonogram(proposal.deviceBrand)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onViewProposalDetail(proposal) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = tokens.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                // Sol Kenarlık Durum Vurgu Çizgisi (3.5dp border-left accent)
                                Box(
                                    modifier = Modifier
                                        .width(3.5.dp)
                                        .matchParentSize()
                                        .background(vis.accentColor)
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 14.dp, top = 12.dp, end = 12.dp, bottom = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                // 1. Satır: Monogram + Marka/Model + Teklif No + Durum Rozeti
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    BrandLogoBadge(
                                        brand = proposal.deviceBrand,
                                        size = 38.dp
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
                                            .background(vis.bgColor)
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

                                // 2. Satır: Müşteri Adı, İlçe, Telefon, Tarih ve TOPLAM
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = proposal.customerName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = tokens.text
                                            )
                                            if (proposal.customerDistrict.isNotBlank()) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(tokens.surface2)
                                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "📍 ${proposal.customerDistrict}",
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = tokens.text2
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${proposal.customerPhone} · ${proposal.date}",
                                            fontSize = 11.sp,
                                            color = tokens.text2
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "TOPLAM",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = tokens.text2
                                        )
                                        Text(
                                            text = formatProposalCurrency(proposal.grandTotal),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = tokens.primary
                                        )
                                        if (proposal.downPayment > 0.0) {
                                            Text(
                                                text = "Peşinat: ${formatProposalCurrency(proposal.downPayment)}",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = tokens.amber
                                            )
                                        }
                                    }
                                }

                                // 3. Satır: İŞ KAPSAMI Kapsülleri (Web Tasarımı ile Uyumlu)
                                if (proposal.items.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "İŞ KAPSAMI (${proposal.items.size} KALEM):",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = tokens.text2
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            proposal.items.take(4).forEach { item ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(tokens.surface2)
                                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = "${item.quantity}x ${item.title}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = tokens.text,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // 4. Satır: Varsa Teklif Notu
                                if (proposal.note.isNotBlank()) {
                                    Text(
                                        text = "📝 ${proposal.note}",
                                        fontSize = 10.sp,
                                        color = tokens.text2,
                                        maxLines = 2
                                    )
                                }

                                // 5. Satır: Durum Değiştirme Butonları (Web Kartı ile Uyumlu)
                                if (onUpdateStatus != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Durum:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tokens.text2)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        listOf(
                                            ProposalStatus.PENDING to "Bekliyor",
                                            ProposalStatus.APPROVED to "✓ Kabul",
                                            ProposalStatus.REJECTED to "✕ Red"
                                        ).forEach { (st, lbl) ->
                                            val isCur = proposal.status == st
                                            val stVisual = getProposalStatusVisual(st, tokens)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isCur) stVisual.bgColor else tokens.surface2)
                                                    .border(0.5.dp, if (isCur) stVisual.accentColor else tokens.border, RoundedCornerShape(6.dp))
                                                    .clickable { onUpdateStatus(proposal.id, st) }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = lbl,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isCur) FontWeight.ExtraBold else FontWeight.Medium,
                                                    color = if (isCur) stVisual.textColor else tokens.text2
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                    }
                                }

                                // 6. Satır: Hızlı Aksiyon Butonları (WhatsApp, Ara, PDF)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF25D366).copy(alpha = 0.12f))
                                            .clickable { sendProposalWhatsApp(context, proposal) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "WhatsApp",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF25D366)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(tokens.primaryC)
                                            .clickable { dialProposalPhone(context, proposal.customerPhone) }
                                            .padding(vertical = 8.dp),
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
                                            .background(tokens.surface2)
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
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "PDF",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = tokens.text
                                        )
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold, color = tokens.text2)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = tokens.text)
            Text(text = subtitle, fontSize = 9.5.sp, color = tokens.text2)
        }
    }
}
