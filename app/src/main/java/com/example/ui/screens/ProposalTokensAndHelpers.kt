package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Proposal
import com.example.data.model.ProposalStatus
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToInt

@Immutable
data class ProposalTokens(
    val isDark: Boolean,
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val border: Color,
    val text: Color,
    val text2: Color,
    val primary: Color,
    val primaryC: Color,
    val onPrimary: Color,
    val green: Color,
    val greenC: Color,
    val amber: Color,
    val amberC: Color,
    val red: Color,
    val redC: Color
)

@Composable
fun rememberProposalTokens(): ProposalTokens {
    val bgLuminance = MaterialTheme.colorScheme.background.luminance()
    val isDark = bgLuminance < 0.5f
    return remember(isDark) {
        if (isDark) {
            ProposalTokens(
                isDark = true,
                bg = Color(0xFF090D16),
                surface = Color(0xFF131B2E),
                surface2 = Color(0xFF1E293B),
                border = Color(0xFF334155),
                text = Color(0xFFF8FAFC),
                text2 = Color(0xFF94A3B8),
                primary = Color(0xFF0EA5E9),
                primaryC = Color(0xFF0F2233),
                onPrimary = Color(0xFFFFFFFF),
                green = Color(0xFF4ADE80),
                greenC = Color(0x244ADE80),
                amber = Color(0xFFFBBF24),
                amberC = Color(0x24FBBF24),
                red = Color(0xFFF87171),
                redC = Color(0x24F87171)
            )
        } else {
            ProposalTokens(
                isDark = false,
                bg = Color(0xFFF4F7FA),
                surface = Color(0xFFFFFFFF),
                surface2 = Color(0xFFE7EEF2),
                border = Color(0x14000000),
                text = Color(0xFF1A1A1A),
                text2 = Color(0xFF757575),
                primary = Color(0xFF0284C7),
                primaryC = Color(0xFFE0F2FE),
                onPrimary = Color(0xFFFFFFFF),
                green = Color(0xFF16A34A),
                greenC = Color(0x1A16A34A),
                amber = Color(0xFFB45309),
                amberC = Color(0x1FB45309),
                red = Color(0xFFB91C1C),
                redC = Color(0x1AB91C1C)
            )
        }
    }
}

data class ProposalKpis(
    val totalCount: Int,
    val totalVolume: Double,
    val pendingCount: Int,
    val pendingVolume: Double,
    val approvedCount: Int,
    val approvedRatio: Int,
    val rejectedCount: Int,
    val expiredCount: Int
)

fun calculateProposalKpis(proposals: List<Proposal>): ProposalKpis {
    val totalCount = proposals.size
    val totalVolume = proposals.sumOf { it.grandTotal }
    val pending = proposals.filter { it.status == ProposalStatus.PENDING }
    val pendingCount = pending.size
    val pendingVolume = pending.sumOf { it.grandTotal }
    val approved = proposals.filter { it.status == ProposalStatus.APPROVED }
    val approvedCount = approved.size
    val approvedRatio = if (totalCount > 0) ((approvedCount.toDouble() / totalCount) * 100).roundToInt() else 0
    val rejectedCount = proposals.count { it.status == ProposalStatus.REJECTED }
    val expiredCount = proposals.count { it.status == ProposalStatus.EXPIRED }

    return ProposalKpis(
        totalCount = totalCount,
        totalVolume = totalVolume,
        pendingCount = pendingCount,
        pendingVolume = pendingVolume,
        approvedCount = approvedCount,
        approvedRatio = approvedRatio,
        rejectedCount = rejectedCount,
        expiredCount = expiredCount
    )
}

data class ProposalStatusVisual(
    val textColor: Color,
    val bgColor: Color,
    val accentColor: Color,
    val label: String
)

fun getProposalStatusVisual(status: ProposalStatus, tokens: ProposalTokens): ProposalStatusVisual {
    return when (status) {
        ProposalStatus.PENDING -> ProposalStatusVisual(
            textColor = if (tokens.isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
            bgColor = if (tokens.isDark) Color(0x2EFEF3C7) else Color(0xFFFEF3C7),
            accentColor = Color(0xFFF59E0B),
            label = "Beklemede"
        )
        ProposalStatus.APPROVED -> ProposalStatusVisual(
            textColor = if (tokens.isDark) Color(0xFF4ADE80) else Color(0xFF065F46),
            bgColor = if (tokens.isDark) Color(0x2ED1FAE5) else Color(0xFFD1FAE5),
            accentColor = Color(0xFF22C55E),
            label = "Kabul Edildi"
        )
        ProposalStatus.REJECTED -> ProposalStatusVisual(
            textColor = if (tokens.isDark) Color(0xFFF87171) else Color(0xFF991B1B),
            bgColor = if (tokens.isDark) Color(0x2EFEE2E2) else Color(0xFFFEE2E2),
            accentColor = Color(0xFFEF4444),
            label = "Reddedildi"
        )
        ProposalStatus.EXPIRED, ProposalStatus.DRAFT -> ProposalStatusVisual(
            textColor = if (tokens.isDark) Color(0xFF94A3B8) else Color(0xFF374151),
            bgColor = if (tokens.isDark) Color(0x20334155) else Color(0xFFF3F4F6),
            accentColor = tokens.border,
            label = if (status == ProposalStatus.EXPIRED) "Süresi Doldu" else "Taslak"
        )
    }
}

val PROPOSAL_BRANDS = listOf(
    "DemirDöküm", "E.C.A.", "Vaillant", "Baymak",
    "Bosch", "Buderus", "Viessmann", "Ariston",
    "Airfel", "Ferroli", "Protherm", "Alarko"
)

val BRAND_MODELS: Map<String, List<String>> = mapOf(
    "DemirDöküm" to listOf("Nitromix", "Atromix", "Atron Condense", "Adonis", "Vinto"),
    "E.C.A." to listOf("Proteus Premix", "Confeo Premix", "Citius Premix", "Calora Premix", "Scot"),
    "Vaillant" to listOf("ecoTEC Pure", "ecoTEC Pro", "ecoTEC Plus", "ecoTEC Intro", "turboTEC"),
    "Baymak" to listOf("Duo Tec Compact", "Lunatec", "Ideatec", "Lambert Lpy", "Baxi"),
    "Bosch" to listOf("Condens 2200i W", "Condens 2300i W", "Condens 7000i W", "Classic Plus"),
    "Buderus" to listOf("Logamax plus GB022i", "Logamax plus GB122i", "Logamax plus GB062"),
    "Viessmann" to listOf("Vitodens 050-W", "Vitodens 100-W", "Vitodens Trend"),
    "Ariston" to listOf("Clas One", "Genus One", "Alteas One", "Cares One"),
    "Airfel" to listOf("Digifel Premix", "Maestro Smart", "Premix CP"),
    "Ferroli" to listOf("Bluehelix Prima", "Divacondens", "Diva"),
    "Protherm" to listOf("Lynx Condens", "Gepard Condens", "Leopard"),
    "Alarko" to listOf("Seradens Super", "Seradens Super Plus", "Harmony Flare")
)

val QUICK_DISTRICTS = listOf(
    "Bayrampaşa", "Gaziosmanpaşa", "Eyüpsultan",
    "Fatih", "Sultangazi", "Esenler", "Zeytinburnu"
)

val QUICK_DOWN_PAYMENT_PERCENTAGES = listOf(
    "25" to "%25",
    "50" to "%50",
    "100" to "Tamamı",
    "0" to "0"
)

val QUICK_DISCOUNT_OPTIONS = listOf(
    "5" to "%5",
    "10" to "%10",
    "15" to "%15",
    "250" to "₺250",
    "0" to "Sıfırla"
)

val PAYMENT_METHODS = listOf(
    "Nakit" to "Montaj / servis anında kapıda",
    "Kredi Kartı (Tek Çekim)" to "Mobil POS ile kapıda çekim",
    "Kredi Kartı (Taksitli)" to "Vade farksız veya anlaşmalı taksit",
    "Havale / EFT" to "Resmi şirket banka hesabına"
)

val VALIDITY_DAYS = listOf("7 Gün", "15 Gün", "30 Gün")

val CONDITION_TEMPLATES = listOf(
    "+ 1 Yıl Garanti Notu" to "Montaj ve işçilik Sancak Kombi güvencesiyle 1 yıl garantilidir. Tüm yedek parçalar %100 orijinal ve üretici onaylı ambalajında takılmaktadır.",
    "+ Orijinal Parça Notu" to "Kullanılan tüm parçalar 1 yıl birebir parça değişim garantilidir.",
    "+ Kartla Ödeme Notu" to "Kapıda mobil POS ile tek çekim veya anlaşmalı kartlara taksit imkanı mevcuttur."
)

fun getBrandMonogram(brand: String): String {
    val b = brand.trim()
    return when {
        b.startsWith("DemirDöküm", ignoreCase = true) -> "DD"
        b.startsWith("E.C.A", ignoreCase = true) || b.startsWith("ECA", ignoreCase = true) -> "EC"
        b.startsWith("Vaillant", ignoreCase = true) -> "VA"
        b.startsWith("Baymak", ignoreCase = true) -> "BM"
        b.startsWith("Bosch", ignoreCase = true) -> "BS"
        b.startsWith("Buderus", ignoreCase = true) -> "BD"
        b.startsWith("Viessmann", ignoreCase = true) -> "VS"
        b.startsWith("Ariston", ignoreCase = true) -> "AR"
        b.startsWith("Airfel", ignoreCase = true) -> "AF"
        b.startsWith("Ferroli", ignoreCase = true) -> "FR"
        b.startsWith("Protherm", ignoreCase = true) -> "PR"
        b.startsWith("Alarko", ignoreCase = true) -> "AL"
        b.length >= 2 -> b.take(2).uppercase(TrLocale)
        b.length == 1 -> b.uppercase(TrLocale)
        else -> "SK"
    }
}

data class BrandVisual(
    val monogram: String,
    val brandColor: Color,
    val iconEmoji: String,
    val logoDrawableRes: Int? = null
)

fun getBrandVisual(brand: String): BrandVisual {
    val b = brand.trim().lowercase(TrLocale)
    return when {
        b.contains("demirdöküm") || b.contains("demirdokum") -> BrandVisual(
            monogram = "DD",
            brandColor = Color(0xFFDC2626),
            iconEmoji = "⚡",
            logoDrawableRes = com.example.R.drawable.brand_demirdokum
        )
        b.contains("baymak") -> BrandVisual(
            monogram = "BM",
            brandColor = Color(0xFFD97706),
            iconEmoji = "☀️",
            logoDrawableRes = com.example.R.drawable.brand_baymak
        )
        b.contains("bosch") -> BrandVisual(
            monogram = "BS",
            brandColor = Color(0xFFE11D48),
            iconEmoji = "⚙️",
            logoDrawableRes = com.example.R.drawable.brand_bosch
        )
        b.contains("vaillant") -> BrandVisual(
            monogram = "VA",
            brandColor = Color(0xFF059669),
            iconEmoji = "🐰",
            logoDrawableRes = com.example.R.drawable.brand_vaillant
        )
        b.contains("e.c.a") || b.contains("eca") -> BrandVisual(
            monogram = "EC",
            brandColor = Color(0xFFEA580C),
            iconEmoji = "🔥"
        )
        b.contains("buderus") -> BrandVisual(
            monogram = "BD",
            brandColor = Color(0xFF003580),
            iconEmoji = "💧"
        )
        b.contains("viessmann") -> BrandVisual(
            monogram = "VS",
            brandColor = Color(0xFFF97316),
            iconEmoji = "🔶"
        )
        b.contains("ariston") -> BrandVisual(
            monogram = "AR",
            brandColor = Color(0xFFC026D3),
            iconEmoji = "🌊"
        )
        b.contains("airfel") -> BrandVisual(
            monogram = "AF",
            brandColor = Color(0xFF0891B2),
            iconEmoji = "🌀"
        )
        b.contains("ferroli") -> BrandVisual(
            monogram = "FR",
            brandColor = Color(0xFFB91C1C),
            iconEmoji = "♨️"
        )
        b.contains("protherm") -> BrandVisual(
            monogram = "PR",
            brandColor = Color(0xFF7C3AED),
            iconEmoji = "🐆"
        )
        b.contains("alarko") -> BrandVisual(
            monogram = "AL",
            brandColor = Color(0xFF16A34A),
            iconEmoji = "⭕"
        )
        else -> BrandVisual(
            monogram = getBrandMonogram(brand),
            brandColor = Color(0xFF0284C7),
            iconEmoji = "🔥"
        )
    }
}

@Composable
fun BrandLogoBadge(
    brand: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val visual = getBrandVisual(brand)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (visual.logoDrawableRes != null) {
            Image(
                painter = painterResource(visual.logoDrawableRes),
                contentDescription = brand,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = visual.iconEmoji,
                fontSize = (size.value * 0.42f).sp
            )
        }
    }
}

fun formatProposalCurrency(amount: Double): String {
    val symbols = DecimalFormatSymbols(TrLocale).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val df = DecimalFormat("#,##0.00", symbols)
    return "₺" + df.format(amount)
}

fun formatProposalCurrencyShort(amount: Double): String {
    val symbols = DecimalFormatSymbols(TrLocale).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val df = DecimalFormat("#,##0", symbols)
    return "₺" + df.format(amount)
}

fun sendProposalWhatsApp(context: Context, proposal: Proposal) {
    try {
        var digits = proposal.customerPhone.filter { it.isDigit() }
        if (digits.startsWith("0")) digits = "90" + digits.drop(1)
        else if (!digits.startsWith("90") && digits.length == 10) digits = "90$digits"

        val code = proposal.quoteNumber.ifBlank { proposal.id }
        val deviceDesc = listOf(proposal.deviceBrand, proposal.deviceModel)
            .filter { it.isNotBlank() }
            .joinToString(" ")

        val message = buildString {
            append("Sayın *${proposal.customerName}*,\n\n")
            append("Sancak Kombi olarak hazırladığımız *$code* numaralı resmi fiyat teklifimiz aşağıdadır:\n")
            if (deviceDesc.isNotBlank()) {
                append("• Cihaz: $deviceDesc\n")
            }
            if (proposal.items.isNotEmpty()) {
                append("• Kalemler:\n")
                proposal.items.take(4).forEach { item ->
                    append("  - ${item.quantity}x ${item.title} (₺${formatProposalCurrency(item.totalPrice)})\n")
                }
            }
            append("• *Toplam Tutar:* ${formatProposalCurrency(proposal.grandTotal)}\n\n")
            append("Teklifi incelemek ve onaylamak için bu mesajı yanıtlayabilirsiniz.\n")
            append("Sağlıklı günler dileriz.\n")
            append("*Sancak Kombi Teknik Servis*")
        }

        val encodedMsg = Uri.encode(message)
        val url = "https://api.whatsapp.com/send?phone=$digits&text=$encodedMsg"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}

fun dialProposalPhone(context: Context, phone: String) {
    try {
        val clean = phone.filter { it.isDigit() || it == '+' }
        if (clean.isBlank()) return
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}
