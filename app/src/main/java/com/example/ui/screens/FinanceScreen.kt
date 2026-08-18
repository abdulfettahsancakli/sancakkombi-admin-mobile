package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BankAccount
import com.example.data.model.FinanceRecord
import com.example.data.model.FinanceSummary
import com.example.data.model.FinanceType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// Adaptive Theme Colors for 2026 Executive Finance Dashboard (Light & Dark Mode)
data class FinanceThemeColors(
    val isDark: Boolean,
    val background: Color,
    val cardSurface: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val tabSelectedBg: Color,
    val tabUnselectedBg: Color,
    val inputBg: Color,
    val successColor: Color,
    val dangerColor: Color,
    val warningColor: Color,
    val bottomSheetBg: Color
)

@Composable
fun rememberFinanceThemeColors(isDark: Boolean = isSystemInDarkTheme()): FinanceThemeColors {
    return remember(isDark) {
        if (isDark) {
            FinanceThemeColors(
                isDark = true,
                background = Color(0xFF090A0F),
                cardSurface = Color(0xFF12151E),
                cardBorder = Color(0x1AFFFFFF),
                textPrimary = Color(0xFFFFFFFF),
                textSecondary = Color(0xFFA0A5B5),
                tabSelectedBg = Color(0xFF1E2332),
                tabUnselectedBg = Color.Transparent,
                inputBg = Color(0xFF0A0D14),
                successColor = Color(0xFF00E676),
                dangerColor = Color(0xFFFF5252),
                warningColor = Color(0xFFFFAB00),
                bottomSheetBg = Color(0xFF0F121C)
            )
        } else {
            FinanceThemeColors(
                isDark = false,
                background = Color(0xFFF8FAFC),
                cardSurface = Color(0xFFFFFFFF),
                cardBorder = Color(0xFFE2E8F0),
                textPrimary = Color(0xFF0F172A),
                textSecondary = Color(0xFF64748B),
                tabSelectedBg = Color(0xFFE2E8F0),
                tabUnselectedBg = Color.Transparent,
                inputBg = Color(0xFFF1F5F9),
                successColor = Color(0xFF059669),
                dangerColor = Color(0xFFDC2626),
                warningColor = Color(0xFFD97706),
                bottomSheetBg = Color(0xFFFFFFFF)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    summary: FinanceSummary,
    financeRecords: List<FinanceRecord>,
    bankAccounts: List<BankAccount>,
    onBackClick: () -> Unit,
    onAddFinanceRecord: (FinanceRecord) -> Unit,
    onDeleteFinanceRecord: (String) -> Unit = {},
    onUpdateBankAccounts: (List<BankAccount>) -> Unit,
    onViewReceipt: (FinanceRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = rememberFinanceThemeColors(isDark = isDarkTheme)
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Özet & Alacaklar, 1: IBAN & Banka, 2: Hızlı Gelir/Gider

    // BottomSheet states
    var showReceivableSheet by remember { mutableStateOf(false) }
    var showIbanEditSheet by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<FinanceRecord?>(null) }

    val currentDateStr = remember {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .testTag("back_button")
                        .clip(CircleShape)
                        .background(colors.cardSurface)
                        .border(1.dp, colors.cardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Finans Komuta Merkezi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = if (colors.isDark) "OLED Executive Dashboard" else "Executive Finance Dashboard",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                // Live status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.successColor.copy(alpha = 0.15f))
                        .border(1.dp, colors.successColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(colors.successColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CANLI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.successColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sekmeli / Tabbed Navigation Bar (3 Tabs)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.cardSurface)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
                    .padding(4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf("📊 Özet & Alacak", "💳 IBAN & Banka", "📝 Hızlı Gelir/Gider", "📈 Analiz & İstatistik")
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) colors.tabSelectedBg else colors.tabUnselectedBg)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) colors.successColor.copy(alpha = 0.5f) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedTab = index }
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) colors.textPrimary else colors.textSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content Area based on Tab
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                when (selectedTab) {
                    0 -> SummaryAndReceivablesTab(
                        colors = colors,
                        summary = summary,
                        financeRecords = financeRecords,
                        onReceivableClick = { showReceivableSheet = true },
                        onViewReceipt = onViewReceipt,
                        onDeleteRecord = { rec -> recordToDelete = rec },
                        onGoToQuickEntry = { selectedTab = 2 }
                    )
                    1 -> IbanManagementTab(
                        colors = colors,
                        bankAccounts = bankAccounts,
                        onOpenEditSheet = { showIbanEditSheet = true }
                    )
                    2 -> QuickEntryAndHistoryTab(
                        colors = colors,
                        currentDateStr = currentDateStr,
                        financeRecords = financeRecords,
                        onAddFinanceRecord = onAddFinanceRecord,
                        onViewReceipt = onViewReceipt,
                        onDeleteRecord = { rec -> recordToDelete = rec },
                        onOpenAnalytics = { selectedTab = 3 }
                    )
                    3 -> FinanceAnalyticsTab(
                        colors = colors,
                        financeRecords = financeRecords
                    )
                }
            }
        }
    }

    // Modal BottomSheet for Receivable Action
    if (showReceivableSheet) {
        ReceivableActionBottomSheet(
            colors = colors,
            context = context,
            bankAccounts = bankAccounts,
            onDismiss = { showReceivableSheet = false },
            onMarkCollected = {
                val rec = FinanceRecord(
                    id = UUID.randomUUID().toString(),
                    date = currentDateStr,
                    type = FinanceType.GELIR,
                    amount = 500.0,
                    status = "Ödendi",
                    source = "Fettah Sancaklı - Alacak Tahsilatı",
                    note = "Açık alacak kapatıldı",
                    receiptNo = "SK-202608-" + UUID.randomUUID().toString().take(6).uppercase()
                )
                onAddFinanceRecord(rec)
                showReceivableSheet = false
                Toast.makeText(context, "500,00 ₺ alacak tahsil edilerek Gelir olarak kaydedildi!", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Delete Record Confirmation Dialog
    if (recordToDelete != null) {
        val rec = recordToDelete!!
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = colors.dangerColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Finans Kaydını Sil",
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Aşağıdaki finans işlemi sistemden kalıcı olarak silinecektir:",
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.tabSelectedBg.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = rec.source,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${rec.date} • ${if (rec.type == FinanceType.GELIR) "+" else "-"}₺%.2f".format(rec.amount).replace(".", ","),
                                fontWeight = FontWeight.SemiBold,
                                color = if (rec.type == FinanceType.GELIR) colors.successColor else colors.dangerColor,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idToDelete = rec.id
                        recordToDelete = null
                        onDeleteFinanceRecord(idToDelete)
                        Toast.makeText(context, "Finans kaydı başarıyla silindi.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.dangerColor)
                ) {
                    Text("Evet, Sil", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { recordToDelete = null }) {
                    Text("Vazgeç", color = colors.textPrimary)
                }
            },
            containerColor = colors.cardSurface
        )
    }

    // Modal BottomSheet for IBAN Management
    if (showIbanEditSheet) {
        IbanEditBottomSheet(
            colors = colors,
            context = context,
            bankAccounts = bankAccounts,
            onDismiss = { showIbanEditSheet = false },
            onSave = { updatedList ->
                onUpdateBankAccounts(updatedList)
                showIbanEditSheet = false
                Toast.makeText(context, "IBAN Bilgileri Güncellendi!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// TAB 0: Özet & Alacaklar
@Composable
private fun SummaryAndReceivablesTab(
    colors: FinanceThemeColors,
    summary: FinanceSummary,
    financeRecords: List<FinanceRecord>,
    onReceivableClick: () -> Unit,
    onViewReceipt: (FinanceRecord) -> Unit,
    onDeleteRecord: (FinanceRecord) -> Unit,
    onGoToQuickEntry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // KPI Bento Cards (3 Cards)
        // Card 1: Toplam Gelir
        OledCard(
            colors = colors,
            borderColor = colors.successColor.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colors.successColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Toplam Gelir", fontSize = 12.sp, color = colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₺%.2f".format(summary.totalIncome).replace(".", ","),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.successColor
                    )
                    Text(
                        text = "${financeRecords.count { it.type == FinanceType.GELIR }} işlem gerçekleşti",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }

                // Micro Analytics Sparkline (Green)
                SparklineGraph(
                    color = colors.successColor,
                    modifier = Modifier
                        .width(90.dp)
                        .height(42.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 2: Toplam Gider
            OledCard(
                colors = colors,
                borderColor = colors.dangerColor.copy(alpha = 0.3f),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(colors.dangerColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Toplam Gider", fontSize = 11.sp, color = colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₺%.2f".format(summary.totalExpense).replace(".", ","),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.dangerColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SparklineGraph(
                        color = colors.dangerColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    )
                }
            }

            // Card 3: Açık Alacak
            OledCard(
                colors = colors,
                borderColor = colors.warningColor.copy(alpha = 0.4f),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(colors.warningColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Açık Alacak", fontSize = 11.sp, color = colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₺%.2f".format(summary.outstandingReceivable).replace(".", ","),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.warningColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.warningColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "1 Bekleyen",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.warningColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Yenilenen Alacak Takip Kartı
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Alacak Takip & Tahsilat",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "1 Bekliyor",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.warningColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.warningColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Modern Glass/Executive Receivable Card
        OledCard(
            colors = colors,
            borderColor = colors.warningColor.copy(alpha = 0.3f),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onReceivableClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
            ) {
                // Vertical Warning Accent Bar on the left
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(84.dp)
                        .background(colors.warningColor)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.warningColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = colors.warningColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fettah Sancaklı",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Vade: 18.05.2026 • GECİKMİŞ • Kısmi",
                            fontSize = 11.sp,
                            color = colors.warningColor
                        )
                        Text(
                            text = "Dokun: Tahsil Et / WhatsApp'tan Hatırlat",
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₺500,00",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.dangerColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.inputBg)
                                .border(1.dp, colors.cardBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Aksiyon Al →", fontSize = 10.sp, color = colors.textPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Son Finans Kayıtları Preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Son Finans Kayıtları",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Tümünü İşle →",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.successColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onGoToQuickEntry() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        financeRecords.take(4).forEach { rec ->
            TransactionRow(colors = colors, record = rec, onViewReceipt = onViewReceipt, onDeleteRecord = onDeleteRecord)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// TAB 1: IBAN & Banka
@Composable
private fun IbanManagementTab(
    colors: FinanceThemeColors,
    bankAccounts: List<BankAccount>,
    onOpenEditSheet: () -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "IBAN & Banka Hesapları",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Müşterilere hızlı kopyala ve WhatsApp ile gönder",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }

            Button(
                onClick = onOpenEditSheet,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.tabSelectedBg)
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = colors.successColor)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Düzenle", fontSize = 12.sp, color = colors.textPrimary)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (bankAccounts.isEmpty()) {
            OledCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Kayıtlı banka hesabı bulunamadı.",
                    color = colors.textSecondary,
                    modifier = Modifier.padding(20.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            bankAccounts.forEach { acc ->
                WalletStyleIbanCard(colors = colors, account = acc, context = context)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

// Wallet Style IBAN Card Component
@Composable
private fun WalletStyleIbanCard(
    colors: FinanceThemeColors,
    account: BankAccount,
    context: Context
) {
    val brandAccentColor = when {
        account.bankName.contains("YAPI", ignoreCase = true) -> Color(0xFF0047BB)
        account.bankName.contains("AKBANK", ignoreCase = true) -> Color(0xFFE20613)
        account.bankName.contains("KUVEYT", ignoreCase = true) -> Color(0xFF008752)
        account.bankName.contains("GARANTİ", ignoreCase = true) -> Color(0xFF00A34E)
        else -> Color(0xFF3B82F6)
    }

    OledCard(
        colors = colors,
        borderColor = brandAccentColor.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Accent Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(brandAccentColor)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(brandAccentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = brandAccentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = account.bankName.ifBlank { "BANKA HESABI" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = brandAccentColor
                        )
                        Text(
                            text = account.accountHolder,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // IBAN Box Monospace
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.inputBg)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = account.iban.ifBlank { "TR00 0000 0000 0000 0000 0000 00" },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.successColor,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Copy Button
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("IBAN", account.iban)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "${account.bankName} IBAN Panoya Kopyalandı", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kopyala", fontSize = 12.sp)
                    }

                    // WhatsApp Share Button
                    Button(
                        onClick = {
                            val shareMessage = """
                                SANCAK KOMBİ TEKNİK SERVİS
                                Banka: ${account.bankName}
                                Alıcı: ${account.accountHolder}
                                IBAN: ${account.iban}
                            """.trimIndent()

                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                            }
                            context.startActivity(Intent.createChooser(intent, "IBAN Bilgisini Paylaş"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// TAB 2: Hızlı Gelir/Gider & İşlem Geçmişi
@Composable
private fun QuickEntryAndHistoryTab(
    colors: FinanceThemeColors,
    currentDateStr: String,
    financeRecords: List<FinanceRecord>,
    onAddFinanceRecord: (FinanceRecord) -> Unit,
    onViewReceipt: (FinanceRecord) -> Unit,
    onDeleteRecord: (FinanceRecord) -> Unit,
    onOpenAnalytics: () -> Unit
) {
    val context = LocalContext.current
    var isGelir by remember { mutableStateOf(true) } // true: Gelir, false: Gider

    var tutarText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Servis Tahsilatı") }

    val categories = if (isGelir) {
        listOf("Servis Tahsilatı", "Yedek Parça", "Kombi Bakımı", "Montaj", "Diğer")
    } else {
        listOf("Google Ads Reklam", "Malzeme Alımı", "Yedek Parça Tedarik", "Yakıt / Ulaşım", "Dükkan Gideri", "Personel / Diğer")
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Analytics Quick Banner
        OledCard(
            colors = colors,
            borderColor = colors.successColor.copy(alpha = 0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenAnalytics() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.successColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Analiz",
                        tint = colors.successColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📈 Gelir / Gider Analiz İstatistikleri",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    val totalInc = financeRecords.filter { it.type == FinanceType.GELIR }.sumOf { it.amount }
                    val totalExp = financeRecords.filter { it.type == FinanceType.GIDER }.sumOf { it.amount }
                    val net = totalInc - totalExp
                    Text(
                        text = "Net Bakiye: ₺%.2f • Kategori Dağılımını İncele".format(net).replace(".", ","),
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = colors.successColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OledCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Hızlı Finans Kaydı",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Segmented Toggle Switch (Gelir vs Gider)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.inputBg)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isGelir) colors.successColor.copy(alpha = 0.2f) else Color.Transparent)
                            .border(if (isGelir) 1.dp else 0.dp, if (isGelir) colors.successColor else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable {
                                isGelir = true
                                selectedCategory = "Servis Tahsilatı"
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, tint = if (isGelir) colors.successColor else colors.textSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ GELİR KAYDI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isGelir) colors.successColor else colors.textSecondary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isGelir) colors.dangerColor.copy(alpha = 0.2f) else Color.Transparent)
                            .border(if (!isGelir) 1.dp else 0.dp, if (!isGelir) colors.dangerColor else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable {
                                isGelir = false
                                selectedCategory = "Malzeme Alımı"
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, tint = if (!isGelir) colors.dangerColor else colors.textSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("- GİDER KAYDI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!isGelir) colors.dangerColor else colors.textSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Numpad Friendly Big Financial Input
                Text("TUTAR (TL)", fontSize = 11.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = tutarText,
                    onValueChange = { tutarText = it },
                    placeholder = { Text("0,00 ₺", fontSize = 28.sp, color = colors.textSecondary.copy(alpha = 0.4f)) },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGelir) colors.successColor else colors.dangerColor
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isGelir) colors.successColor else colors.dangerColor,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedContainerColor = colors.inputBg,
                        unfocusedContainerColor = colors.inputBg
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Chips
                Text("KATEGORİ", fontSize = 11.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isGelir) colors.successColor.copy(alpha = 0.2f) else colors.dangerColor.copy(alpha = 0.2f),
                                selectedLabelColor = if (isGelir) colors.successColor else colors.dangerColor,
                                containerColor = colors.inputBg,
                                labelColor = colors.textSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Customer / Note Input
                Text("MÜŞTERİ / AÇIKLAMA NOTU", fontSize = 11.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("Örn: Ahmet Yılmaz - Parça Değişimi", color = colors.textSecondary.copy(alpha = 0.5f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.cardBorder,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedContainerColor = colors.inputBg,
                        unfocusedContainerColor = colors.inputBg,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        val amt = tutarText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            val record = FinanceRecord(
                                id = UUID.randomUUID().toString(),
                                date = currentDateStr,
                                type = if (isGelir) FinanceType.GELIR else FinanceType.GIDER,
                                amount = amt,
                                status = "Ödendi",
                                source = if (noteText.isNotBlank()) noteText else selectedCategory,
                                note = selectedCategory,
                                receiptNo = "SK-202608-" + UUID.randomUUID().toString().take(6).uppercase()
                            )
                            onAddFinanceRecord(record)
                            tutarText = ""
                            noteText = ""
                            Toast.makeText(context, "${if (isGelir) "Gelir" else "Gider"} kaydı eklendi!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Lütfen geçerli bir tutar giriniz", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isGelir) colors.successColor else colors.dangerColor
                    )
                ) {
                    Text(
                        text = if (isGelir) "GELİRİ KAYDET (₺)" else "GİDERİ KAYDET (₺)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Full Transaction History Section
        Text(
            text = "Finans İşlem Geçmişi (${financeRecords.size})",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (financeRecords.isEmpty()) {
            OledCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Henüz finans kaydı bulunmuyor.",
                    color = colors.textSecondary,
                    modifier = Modifier.padding(20.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            financeRecords.forEach { rec ->
                TransactionRow(colors = colors, record = rec, onViewReceipt = onViewReceipt, onDeleteRecord = onDeleteRecord)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// Transaction Row Component
@Composable
private fun TransactionRow(
    colors: FinanceThemeColors,
    record: FinanceRecord,
    onViewReceipt: (FinanceRecord) -> Unit,
    onDeleteRecord: (FinanceRecord) -> Unit
) {
    val isIncome = record.type == FinanceType.GELIR
    val isAds = record.source.contains("Google Ads", ignoreCase = true) || record.id.startsWith("ads_")

    OledCard(
        colors = colors,
        borderColor = if (isAds) Color(0xFF4285F4).copy(alpha = 0.4f) else colors.cardBorder,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle Icon Indicator
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isAds) Color(0xFF4285F4).copy(alpha = 0.15f)
                        else if (isIncome) colors.successColor.copy(alpha = 0.15f)
                        else colors.dangerColor.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAds) Icons.Default.Analytics else if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (isAds) Color(0xFF4285F4) else if (isIncome) colors.successColor else colors.dangerColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.source,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isAds) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF4285F4).copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "GÜNLÜK ADS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4285F4)
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = record.date, fontSize = 11.sp, color = colors.textSecondary)
                    if (record.note.isNotBlank()) {
                        Text(text = " • ${record.note}", fontSize = 11.sp, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncome) "+" else "-"}₺%.2f".format(record.amount).replace(".", ","),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) colors.successColor else colors.dangerColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colors.tabSelectedBg,
                        modifier = Modifier
                            .height(30.dp)
                            .clickable { onViewReceipt(record) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(13.dp), tint = colors.textSecondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Makbuz", fontSize = 11.sp, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colors.dangerColor.copy(alpha = 0.15f),
                        modifier = Modifier
                            .size(30.dp)
                            .clickable { onDeleteRecord(record) }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Kaydı Sil",
                                tint = colors.dangerColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Sparkline Micro-Analytics Canvas Graph Component
@Composable
private fun SparklineGraph(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val path = Path().apply {
            moveTo(0f, height * 0.7f)
            cubicTo(
                width * 0.25f, height * 0.2f,
                width * 0.5f, height * 0.9f,
                width * 0.75f, height * 0.3f
            )
            lineTo(width, height * 0.1f)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.5.dp.toPx())
        )

        // Gradient fill below curve
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.25f), Color.Transparent)
            )
        )
    }
}

// Reusable Glass/Executive Container Card
@Composable
private fun OledCard(
    colors: FinanceThemeColors,
    borderColor: Color = colors.cardBorder,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardSurface)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        content()
    }
}

// Modal BottomSheet for Receivable Actions (Tahsilat / WhatsApp Hatırlat)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceivableActionBottomSheet(
    colors: FinanceThemeColors,
    context: Context,
    bankAccounts: List<BankAccount>,
    onDismiss: () -> Unit,
    onMarkCollected: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.bottomSheetBg,
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colors.warningColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Alacak Tahsilat İşlemi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OledCard(
                colors = colors,
                borderColor = colors.warningColor.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Müşteri: Fettah Sancaklı", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text("Durum: Vade 18.05.2026 • GECİKMİŞ", fontSize = 12.sp, color = colors.warningColor)
                    Text("Kalan Alacak Tutar: ₺500,00", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.dangerColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action 1: WhatsApp Reminder
            Button(
                onClick = {
                    val defaultIban = bankAccounts.firstOrNull()?.iban ?: "TR33 0006 7010 0000 0012 3456 78"
                    val reminderText = """
                        Sayın Fettah Sancaklı,
                        Sancak Kombi Teknik Servis hizmetinize ait ₺500,00 tutarındaki ödemenizi hatırlatırız.
                        
                        Banka Hesabımız:
                        ${bankAccounts.firstOrNull()?.bankName ?: "Yapı Kredi"} - ${bankAccounts.firstOrNull()?.accountHolder ?: "Fatih Sancaklı"}
                        IBAN: $defaultIban
                        
                        Anlayışınız için teşekkür ederiz.
                    """.trimIndent()

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, reminderText)
                    }
                    context.startActivity(Intent.createChooser(intent, "WhatsApp'tan Ödeme Hatırlatması Gönder"))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("WhatsApp'tan Ödeme Hatırlatması Gönder", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action 2: Mark as Collected
            Button(
                onClick = onMarkCollected,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.successColor)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tahsil Edildi Olarak İşaretle (+500 ₺)", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
            ) {
                Text("Vazgeç")
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// Modal BottomSheet for Editing IBANs
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IbanEditBottomSheet(
    colors: FinanceThemeColors,
    context: Context,
    bankAccounts: List<BankAccount>,
    onDismiss: () -> Unit,
    onSave: (List<BankAccount>) -> Unit
) {
    var iban1 by remember { mutableStateOf(bankAccounts.getOrNull(0)?.iban ?: "") }
    var iban2 by remember { mutableStateOf(bankAccounts.getOrNull(1)?.iban ?: "") }
    var iban3 by remember { mutableStateOf(bankAccounts.getOrNull(2)?.iban ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.bottomSheetBg,
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("IBAN Hesapları Güncelleme", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Text("Müşterilere iletilen banka IBAN bilgilerinizi düzenleyin", fontSize = 12.sp, color = colors.textSecondary)

            Spacer(modifier = Modifier.height(16.dp))

            Text("YAPI KREDİ - Fatih Sancaklı", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.successColor)
            OutlinedTextField(
                value = iban1,
                onValueChange = { iban1 = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.successColor,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = colors.inputBg,
                    unfocusedContainerColor = colors.inputBg
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text("AKBANK - Abdulfettah Sancaklı", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.dangerColor)
            OutlinedTextField(
                value = iban2,
                onValueChange = { iban2 = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.dangerColor,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = colors.inputBg,
                    unfocusedContainerColor = colors.inputBg
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text("KUVEYT TÜRK - Abdullah Sancaklı", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.warningColor)
            OutlinedTextField(
                value = iban3,
                onValueChange = { iban3 = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.warningColor,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = colors.inputBg,
                    unfocusedContainerColor = colors.inputBg
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val updated = listOf(
                        BankAccount("b1", "YAPI KREDİ", "Fatih Sancaklı", "YAPI KREDİ", iban1),
                        BankAccount("b2", "AKBANK", "Abdulfettah Sancaklı", "AKBANK", iban2),
                        BankAccount("b3", "KUVEYT TÜRK", "Abdullah Sancaklı", "KUVEYT TÜRK", iban3)
                    )
                    onSave(updated)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.successColor)
            ) {
                Text("Değişiklikleri Kaydet", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


// TAB 3: Gelir / Gider Analiz & İstatistikler
@Composable
private fun FinanceAnalyticsTab(
    colors: FinanceThemeColors,
    financeRecords: List<FinanceRecord>
) {
    val incomeRecords = financeRecords.filter { it.type == FinanceType.GELIR }
    val expenseRecords = financeRecords.filter { it.type == FinanceType.GIDER }

    val totalIncome = incomeRecords.sumOf { it.amount }
    val totalExpense = expenseRecords.sumOf { it.amount }
    val netBalance = totalIncome - totalExpense
    val totalVolume = totalIncome + totalExpense
    val profitMarginPercent = if (totalIncome > 0) ((netBalance / totalIncome) * 100).coerceAtLeast(0.0) else 0.0

    val incomeCount = incomeRecords.size
    val expenseCount = expenseRecords.size
    val avgIncome = if (incomeCount > 0) totalIncome / incomeCount else 0.0
    val avgExpense = if (expenseCount > 0) totalExpense / expenseCount else 0.0

    val incomeRatio = if (totalVolume > 0) (totalIncome / totalVolume).toFloat() else 0.5f
    val expenseRatio = if (totalVolume > 0) (totalExpense / totalVolume).toFloat() else 0.5f

    // Category grouping
    val incomeByCategory = incomeRecords.groupBy {
        if (it.note.isNotBlank()) it.note else if (it.source.isNotBlank()) it.source else "Diğer Gelir"
    }.mapValues { entry -> entry.value.sumOf { it.amount } }

    val expenseByCategory = expenseRecords.groupBy {
        if (it.note.isNotBlank()) it.note else if (it.source.isNotBlank()) it.source else "Diğer Gider"
    }.mapValues { entry -> entry.value.sumOf { it.amount } }

    var selectedAnalysisCategoryType by remember { mutableStateOf(true) } // true: Gelir, false: Gider

    Column(modifier = Modifier.fillMaxWidth()) {
        // Top KPI Card
        OledCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = colors.successColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Finansal Performans & Analiz",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.successColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "%%%s Kârlılık".format("%.1f".format(profitMarginPercent)),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.successColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3 Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Toplam Gelir Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.inputBg)
                            .border(1.dp, colors.successColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = colors.successColor, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("GELİR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₺%.2f".format(totalIncome).replace(".", ","),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.successColor
                            )
                            Text("$incomeCount İşlem", fontSize = 10.sp, color = colors.textSecondary)
                        }
                    }

                    // Toplam Gider Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.inputBg)
                            .border(1.dp, colors.dangerColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = colors.dangerColor, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("GİDER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₺%.2f".format(totalExpense).replace(".", ","),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.dangerColor
                            )
                            Text("$expenseCount İşlem", fontSize = 10.sp, color = colors.textSecondary)
                        }
                    }

                    // Net Bakiye / Kar Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.inputBg)
                            .border(1.dp, if (netBalance >= 0) colors.successColor.copy(alpha = 0.5f) else colors.dangerColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("NET KAR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₺%.2f".format(netBalance).replace(".", ","),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (netBalance >= 0) colors.successColor else colors.dangerColor
                            )
                            Text(if (netBalance >= 0) "Net Bakiye" else "Zarar", fontSize = 10.sp, color = colors.textSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Income vs Expense Proportion Visual Bar
                Text(
                    text = "GELİR / GİDER DENGESİ ORANI",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                ClipProgressRatioBar(
                    incomeRatio = incomeRatio,
                    expenseRatio = expenseRatio,
                    successColor = colors.successColor,
                    dangerColor = colors.dangerColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Gelir Oranı: %s".format("%%%s".format("%.1f".format(incomeRatio * 100))),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.successColor
                    )
                    Text(
                        text = "Gider Oranı: %s".format("%%%s".format("%.1f".format(expenseRatio * 100))),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.dangerColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Average Transactions Metrics Card
        OledCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "İşlem Hacmi & Ortalamalar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Ort. Gelir", fontSize = 10.sp, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₺%.2f".format(avgIncome).replace(".", ","),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.successColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(colors.cardBorder)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Ort. Gider", fontSize = 10.sp, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₺%.2f".format(avgExpense).replace(".", ","),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.dangerColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(colors.cardBorder)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Toplam Hacim", fontSize = 10.sp, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₺%.2f".format(totalVolume).replace(".", ","),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Category Breakdown Card
        OledCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Kategori Bazlı Finansal Dağılım",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Toggle Gelir / Gider categories
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.inputBg)
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedAnalysisCategoryType) colors.successColor.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { selectedAnalysisCategoryType = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Gelir Kategorileri (${incomeByCategory.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedAnalysisCategoryType) colors.successColor else colors.textSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!selectedAnalysisCategoryType) colors.dangerColor.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { selectedAnalysisCategoryType = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Gider Kategorileri (${expenseByCategory.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!selectedAnalysisCategoryType) colors.dangerColor else colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val activeMap = if (selectedAnalysisCategoryType) incomeByCategory else expenseByCategory
                val activeTotal = if (selectedAnalysisCategoryType) totalIncome else totalExpense
                val themeColor = if (selectedAnalysisCategoryType) colors.successColor else colors.dangerColor

                if (activeMap.isEmpty()) {
                    Text(
                        text = "Henüz bu türde kayıtlı veri bulunmuyor.",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    activeMap.entries.sortedByDescending { it.value }.forEach { (catName, catAmount) ->
                        val percent = if (activeTotal > 0) (catAmount / activeTotal).toFloat() else 0f
                        val pctStr = "%%%s".format("%.1f".format(percent * 100))

                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = catName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "₺%.2f (%s)".format(catAmount, pctStr).replace(".", ","),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColor
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(colors.inputBg)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = percent.coerceIn(0.01f, 1f))
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(themeColor)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClipProgressRatioBar(
    incomeRatio: Float,
    expenseRatio: Float,
    successColor: Color,
    dangerColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = 0.2f))
    ) {
        if (incomeRatio > 0f) {
            Box(
                modifier = Modifier
                    .weight(incomeRatio.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(successColor)
            )
        }
        if (expenseRatio > 0f) {
            Box(
                modifier = Modifier
                    .weight(expenseRatio.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(dangerColor)
            )
        }
    }
}
