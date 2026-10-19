package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CatalogItem
import com.example.data.model.Proposal
import com.example.data.model.ProposalItem
import com.example.data.model.ProposalStatus
import com.example.utils.parseLocalizedDouble
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun ProposalsTabletLandscapeSplitView(
    proposals: List<Proposal>,
    selectedProposalId: String?,
    onSelectProposal: (Proposal) -> Unit,
    onBackClick: () -> Unit,
    onOpenNewProposalDialog: () -> Unit,
    onUpdateStatus: (String, ProposalStatus) -> Unit,
    onDeleteProposal: (String) -> Unit,
    tokens: ProposalTokens,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cloudWhatsApp = LocalCloudWhatsAppActions.current
    val coroutineScope = rememberCoroutineScope()
    var isDownloadingPdf by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Tümü") }
    var selectedBrand by remember { mutableStateOf("Tüm Markalar") }

    val kpis = remember(proposals) { calculateProposalKpis(proposals) }

    val filteredProposals = remember(proposals, searchQuery, selectedFilter, selectedBrand) {
        val q = searchQuery.trim().lowercase(Locale("tr"))
        proposals.filter { p ->
            val matchQuery = q.isEmpty() ||
                p.customerName.lowercase(Locale("tr")).contains(q) ||
                p.customerPhone.contains(q) ||
                p.quoteNumber.lowercase(Locale("tr")).contains(q) ||
                p.id.lowercase(Locale("tr")).contains(q)

            val matchFilter = when (selectedFilter) {
                "Beklemede" -> p.status == ProposalStatus.PENDING
                "Kabul Edildi" -> p.status == ProposalStatus.APPROVED
                "Reddedildi" -> p.status == ProposalStatus.REJECTED
                "Süresi Doldu" -> p.status == ProposalStatus.EXPIRED
                else -> true
            }

            val matchBrand = if (selectedBrand == "Tüm Markalar") true else p.deviceBrand.equals(selectedBrand, ignoreCase = true)
            matchQuery && matchFilter && matchBrand
        }
    }

    val activeProposal = remember(proposals, selectedProposalId) {
        proposals.find { it.id == selectedProposalId } ?: proposals.firstOrNull()
    }

    Row(modifier = modifier.fillMaxSize()) {
        // SOL MASTER PANEL (380dp)
        Column(
            modifier = Modifier
                .width(380.dp)
                .fillMaxHeight()
                .background(tokens.surface)
                .border(width = 1.dp, color = tokens.border)
                .padding(14.dp)
        ) {
            // Başlık & Geri & Yeni Teklif
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.text)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Geri", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Fiyat Teklifleri",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.text
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(tokens.primaryC)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${proposals.size} kayıt",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = tokens.primary
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onOpenNewProposalDialog,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.primary, contentColor = tokens.onPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Yeni", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4'lü KPI Gridi
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    TabletKpiCard(
                        title = "TOPLAM HACİM",
                        value = formatProposalCurrencyShort(kpis.totalVolume),
                        subtitle = "${kpis.totalCount} adet",
                        tokens = tokens,
                        modifier = Modifier.weight(1f)
                    )
                    TabletKpiCard(
                        title = "BEKLEYEN",
                        value = "${kpis.pendingCount}",
                        subtitle = formatProposalCurrencyShort(kpis.pendingVolume),
                        tokens = tokens,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    TabletKpiCard(
                        title = "KABUL EDİLEN",
                        value = "${kpis.approvedCount}",
                        subtitle = "%${kpis.approvedRatio} başarı",
                        tokens = tokens,
                        modifier = Modifier.weight(1f)
                    )
                    TabletKpiCard(
                        title = "RED / DOLAN",
                        value = "${kpis.rejectedCount} Red",
                        subtitle = "${kpis.expiredCount} süre doldu",
                        tokens = tokens,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Arama Çubuğu
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Müşteri, telefon, teklif no ara…", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = tokens.text2, modifier = Modifier.size(16.dp))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filtre Çipleri
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filterList = listOf("Tümü", "Beklemede", "Kabul Edildi", "Reddedildi", "Süresi Doldu")
                filterList.forEach { flt ->
                    val isSel = selectedFilter == flt
                    val count = when (flt) {
                        "Beklemede" -> kpis.pendingCount
                        "Kabul Edildi" -> kpis.approvedCount
                        "Reddedildi" -> kpis.rejectedCount
                        "Süresi Doldu" -> kpis.expiredCount
                        else -> kpis.totalCount
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) tokens.primary else tokens.surface2)
                            .border(1.dp, if (isSel) tokens.primary else tokens.border, RoundedCornerShape(8.dp))
                            .clickable { selectedFilter = flt }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "$flt $count",
                            fontSize = 10.5.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) tokens.onPrimary else tokens.text2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Marka Filtresi (MARKA:)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MARKA:",
                    fontSize = 9.5.sp,
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
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) tokens.primaryC else tokens.surface2)
                                .border(1.dp, if (isSel) tokens.primary else tokens.border, RoundedCornerShape(6.dp))
                                .clickable { selectedBrand = brand }
                                .padding(horizontal = 7.dp, vertical = 3.5.dp)
                        ) {
                            Text(
                                text = if (brand == "Tüm Markalar") "Tümü ($brandCount)" else "$brand ($brandCount)",
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) tokens.primary else tokens.text
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Teklif Listesi
            if (filteredProposals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Kayıt bulunamadı", fontSize = 12.sp, color = tokens.text2)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProposals, key = { it.id }) { proposal ->
                        val isSelected = activeProposal?.id == proposal.id
                        val vis = getProposalStatusVisual(proposal.status, tokens)
                        val mono = getBrandMonogram(proposal.deviceBrand)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectProposal(proposal) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) tokens.primary else tokens.border
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    BrandLogoBadge(
                                        brand = proposal.deviceBrand,
                                        size = 36.dp,
                                        tokens = tokens
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = proposal.customerName,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = tokens.text,
                                                maxLines = 1
                                            )
                                            if (proposal.customerDistrict.isNotBlank()) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = proposal.customerDistrict,
                                                        fontSize = 8.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = tokens.text2
                                                    )
                                                }
                                            }
                                        }
                                        val dev = listOf(proposal.deviceBrand, proposal.deviceModel).filter { it.isNotBlank() }.joinToString(" ")
                                        Text(
                                            text = if (dev.isNotBlank()) "${proposal.quoteNumber.ifBlank { proposal.id }} · $dev" else proposal.quoteNumber.ifBlank { proposal.id },
                                            fontSize = 9.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = tokens.primary,
                                            maxLines = 1
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = formatProposalCurrency(proposal.grandTotal),
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = tokens.text
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .border(1.dp, vis.accentColor, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = vis.label,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = vis.textColor
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

        // SAĞ DETAY & ÖNİZLEME PANELİ
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(tokens.bg)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (activeProposal == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Görüntülemek için sol listeden bir teklif seçin.", color = tokens.text2)
                }
            } else {
                val vis = getProposalStatusVisual(activeProposal.status, tokens)

                // Üst Başlık & Müşteri / Tutar Özet Barı
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = activeProposal.quoteNumber.ifBlank { activeProposal.id },
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = tokens.primary
                            )
                            Text(
                                text = activeProposal.customerName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = tokens.text
                            )
                            val dev = listOf(activeProposal.deviceBrand, activeProposal.deviceModel).filter { it.isNotBlank() }.joinToString(" ")
                            Text(
                                text = "${activeProposal.customerPhone} · ${activeProposal.date}${if (dev.isNotBlank()) " · $dev" else ""}",
                                fontSize = 12.sp,
                                color = tokens.text2
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(vis.bgColor)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = vis.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = vis.textColor
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatProposalCurrency(activeProposal.grandTotal),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = tokens.primary
                            )
                        }
                    }
                }

                // 1. İŞ KAPSAMI KARTI (Tasarım Prototipi)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "İŞ KAPSAMI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tokens.text2,
                            letterSpacing = 0.4.sp
                        )

                        if (activeProposal.items.isEmpty()) {
                            Text(
                                text = "Kalem bilgisi bulunmamaktadır.",
                                fontSize = 12.sp,
                                color = tokens.text2
                            )
                        } else {
                            activeProposal.items.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, tokens.border, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${item.quantity}x ${item.title}",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = tokens.text,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = formatProposalCurrency(item.totalPrice),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = tokens.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. DURUM DEĞİŞTİRME BAR (Beklemede, Kabul, Red)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("DURUM", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = tokens.text2)
                        Spacer(modifier = Modifier.weight(1f))

                        listOf(
                            ProposalStatus.PENDING to "Beklemede",
                            ProposalStatus.APPROVED to "Kabul",
                            ProposalStatus.REJECTED to "Red"
                        ).forEach { (st, label) ->
                            val isCurrent = activeProposal.status == st
                            val stVis = getProposalStatusVisual(st, tokens)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCurrent) stVis.bgColor else tokens.surface2)
                                    .border(1.dp, if (isCurrent) stVis.accentColor else tokens.border, RoundedCornerShape(8.dp))
                                    .clickable { onUpdateStatus(activeProposal.id, st) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) stVis.textColor else tokens.text2
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                }

                // 3. HIZLI AKSİYONLAR (WhatsApp, Ara, PDF)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.primaryC)
                            .border(1.dp, tokens.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { sendProposalWhatsApp(context, activeProposal, cloudWhatsApp) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("WhatsApp Teklifi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tokens.primary)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.primaryC)
                            .border(1.dp, tokens.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { dialProposalPhone(context, activeProposal.customerPhone) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(14.dp))
                            Text("Ara", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tokens.primary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.surface2)
                            .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
                            .clickable {
                                if (!isDownloadingPdf) {
                                    coroutineScope.launch {
                                        downloadAndShareQuotePdf(
                                            context = context,
                                            quoteId = activeProposal.id,
                                            quoteNumber = activeProposal.quoteNumber.ifBlank { activeProposal.id },
                                            onLoadingStateChange = { isDownloadingPdf = it }
                                        )
                                    }
                                }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = tokens.text, modifier = Modifier.size(14.dp))
                            Text("PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tokens.text)
                        }
                    }
                }

                // 4. ALT AKSİYONLAR (Düzenle, Sil)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.surface)
                            .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
                            .clickable {
                                android.widget.Toast.makeText(context, "Teklif düzenleme formu açılıyor...", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = tokens.text, modifier = Modifier.size(14.dp))
                            Text("✏️ Düzenle", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tokens.text)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.redC)
                            .border(1.dp, tokens.red.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { onDeleteProposal(activeProposal.id) }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = tokens.red, modifier = Modifier.size(14.dp))
                            Text("🗑️ Sil", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tokens.red)
                        }
                    }
                }

                // 5. RESMİ A4 TEKLİF BELGESİ (Belge Önizlemesi)
                ProposalDocumentCard(proposal = activeProposal)
            }
        }
    }
}

@Composable
fun TabletKpiCard(
    title: String,
    value: String,
    subtitle: String,
    tokens: ProposalTokens,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.surface2),
        border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = title, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = tokens.text2)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = tokens.text)
            Text(text = subtitle, fontSize = 9.sp, color = tokens.text2)
        }
    }
}

@Composable
fun NewProposalTabletDialog(
    catalogItems: List<CatalogItem>,
    onDismissRequest: () -> Unit,
    onCreateProposal: (Proposal) -> Unit,
    tokens: ProposalTokens
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerEmail by remember { mutableStateOf("") }
    var customerDistrict by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("") }
    var deviceBrand by remember { mutableStateOf("") }
    var deviceModel by remember { mutableStateOf("") }

    val items = remember {
        mutableStateListOf(ProposalItem("1", "", 1, 0.0))
    }

    var downPayment by remember { mutableStateOf("0") }
    var remainingPaymentType by remember { mutableStateOf("Nakit") }
    var validityDays by remember { mutableStateOf("15 Gün") }
    var validUntilDate by remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 15) }
        mutableStateOf(SimpleDateFormat("dd.MM.yyyy", Locale("tr")).format(cal.time))
    }
    var preparedBy by remember { mutableStateOf("Sancak Kombi Yetkili Servisi") }
    var note by remember { mutableStateOf("Montaj ve işçilik Sancak Kombi güvencesiyle 1 yıl garantilidir.") }
    var discount by remember { mutableStateOf("0") }

    val subtotal = items.sumOf { it.totalPrice }
    val discountVal = parseLocalizedDouble(discount) ?: 0.0
    val grandTotal = (subtotal - discountVal).coerceAtLeast(0.0)
    val validItems = items.filter { it.title.isNotBlank() && it.quantity > 0 && it.unitPrice >= 0.0 }
    val canCreateProposal = customerName.isNotBlank() && validItems.isNotEmpty()

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .width(620.dp)
                .fillMaxHeight(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = tokens.bg),
            border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Modal Başlığı
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Yeni Fiyat Teklifi", fontSize = 18.sp, fontWeight = FontWeight.Black, color = tokens.text)
                        Text("Müşteriye verilecek fiyat teklifini oluşturun.", fontSize = 11.sp, color = tokens.text2)
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat", tint = tokens.text2)
                    }
                }

                // Form Scroll Alanı
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    NewProposalFormContent(
                        tokens = tokens,
                        customerName = customerName,
                        onCustomerNameChange = { customerName = it },
                        customerPhone = customerPhone,
                        onCustomerPhoneChange = { customerPhone = it },
                        customerEmail = customerEmail,
                        onCustomerEmailChange = { customerEmail = it },
                        customerDistrict = customerDistrict,
                        onCustomerDistrictChange = { customerDistrict = it },
                        customerAddress = customerAddress,
                        onCustomerAddressChange = { customerAddress = it },
                        deviceBrand = deviceBrand,
                        onDeviceBrandChange = { deviceBrand = it },
                        deviceModel = deviceModel,
                        onDeviceModelChange = { deviceModel = it },
                        items = items,
                        catalogItems = catalogItems,
                        downPayment = downPayment,
                        onDownPaymentChange = { downPayment = it },
                        remainingPaymentType = remainingPaymentType,
                        onRemainingPaymentTypeChange = { remainingPaymentType = it },
                        validityDays = validityDays,
                        onValidityDaysChange = { daysLabel ->
                            validityDays = daysLabel
                            val offset = when (daysLabel) {
                                "7 Gün" -> 7
                                "30 Gün" -> 30
                                else -> 15
                            }
                            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
                            validUntilDate = SimpleDateFormat("dd.MM.yyyy", Locale("tr")).format(cal.time)
                        },
                        validUntilDate = validUntilDate,
                        onValidUntilDateChange = { validUntilDate = it },
                        preparedBy = preparedBy,
                        onPreparedByChange = { preparedBy = it },
                        note = note,
                        onNoteChange = { note = it },
                        discount = discount,
                        onDiscountChange = { discount = it },
                        subtotal = subtotal,
                        grandTotal = grandTotal
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Alt Aksiyon Butonları
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tokens.surface)
                        .border(1.dp, tokens.border)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.text)
                    ) {
                        Text("İptal", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (canCreateProposal) {
                                val newProposal = Proposal(
                                    id = "TF-" + SimpleDateFormat("yyyyMM", Locale.US).format(Date()) + "-" + UUID.randomUUID().toString().take(6).uppercase(Locale.US),
                                    quoteNumber = "TF-" + SimpleDateFormat("yyyyMM", Locale.US).format(Date()) + "-" + UUID.randomUUID().toString().take(6).uppercase(Locale.US),
                                    customerName = customerName,
                                    customerPhone = customerPhone,
                                    customerEmail = customerEmail,
                                    customerDistrict = customerDistrict,
                                    customerAddress = customerAddress,
                                    deviceBrand = deviceBrand,
                                    deviceModel = deviceModel,
                                    date = SimpleDateFormat("d MMMM yyyy", Locale("tr")).format(Date()),
                                    validUntilDate = validUntilDate,
                                    preparedBy = preparedBy,
                                    note = note,
                                    status = ProposalStatus.PENDING,
                                    items = validItems,
                                    downPayment = parseLocalizedDouble(downPayment) ?: 0.0,
                                    remainingPaymentType = remainingPaymentType,
                                    discount = discountVal
                                )
                                onCreateProposal(newProposal)
                                onDismissRequest()
                            }
                        },
                        enabled = canCreateProposal,
                        modifier = Modifier.weight(1.4f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.primary, contentColor = tokens.onPrimary)
                    ) {
                        Text("Teklifi Oluştur", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
