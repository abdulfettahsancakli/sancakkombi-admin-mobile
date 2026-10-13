package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

val TrLocale: Locale = Locale("tr", "TR")

@Immutable
data class AppointmentTokens(
    val isDark: Boolean,
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val border: Color,
    val text: Color,
    val text2: Color,
    val primary: Color,
    val primaryC: Color,
    val green: Color,
    val greenC: Color,
    val amber: Color,
    val amberC: Color,
    val red: Color,
    val redC: Color,
    val violet: Color,
    val primaryGradient: Brush,
    val amberGradient: Brush
)

@Composable
fun rememberAppointmentTokens(): AppointmentTokens {
    val bgLuminance = MaterialTheme.colorScheme.background.luminance()
    val isDark = bgLuminance < 0.5f
    return remember(isDark) {
        if (isDark) {
            AppointmentTokens(
                isDark = true,
                bg = Color(0xFF090D16),
                surface = Color(0xFF131B2E),
                surface2 = Color(0xFF1E293B),
                border = Color(0xFF334155),
                text = Color(0xFFF8FAFC),
                text2 = Color(0xFF94A3B8),
                primary = Color(0xFF0EA5E9),
                primaryC = Color(0xFF0EA5E9).copy(alpha = 0.16f),
                green = Color(0xFF22C55E),
                greenC = Color(0xFF22C55E).copy(alpha = 0.14f),
                amber = Color(0xFFF59E0B),
                amberC = Color(0xFFF59E0B).copy(alpha = 0.14f),
                red = Color(0xFFF87171),
                redC = Color(0xFFF87171).copy(alpha = 0.14f),
                violet = Color(0xFFA78BFA),
                primaryGradient = Brush.linearGradient(
                    colors = listOf(Color(0xFF0EA5E9), Color(0xFF0369A1))
                ),
                amberGradient = Brush.linearGradient(
                    colors = listOf(Color(0xFFF59E0B), Color(0xFFB45309))
                )
            )
        } else {
            AppointmentTokens(
                isDark = false,
                bg = Color(0xFFF4F7FA),
                surface = Color(0xFFFFFFFF),
                surface2 = Color(0xFFEEF2F6),
                border = Color(0xFF0F172A).copy(alpha = 0.10f),
                text = Color(0xFF0F172A),
                text2 = Color(0xFF64748B),
                primary = Color(0xFF0284C7),
                primaryC = Color(0xFF0284C7).copy(alpha = 0.10f),
                green = Color(0xFF16A34A),
                greenC = Color(0xFF16A34A).copy(alpha = 0.10f),
                amber = Color(0xFFB45309),
                amberC = Color(0xFFB45309).copy(alpha = 0.12f),
                red = Color(0xFFDC2626),
                redC = Color(0xFFDC2626).copy(alpha = 0.10f),
                violet = Color(0xFF7C3AED),
                primaryGradient = Brush.linearGradient(
                    colors = listOf(Color(0xFF0EA5E9), Color(0xFF0369A1))
                ),
                amberGradient = Brush.linearGradient(
                    colors = listOf(Color(0xFFF59E0B), Color(0xFFB45309))
                )
            )
        }
    }
}

data class StatusMeta(
    val label: String,
    val color: Color,
    val bg: Color
)

fun statusMeta(status: AppointmentStatus, t: AppointmentTokens): StatusMeta = when (status) {
    AppointmentStatus.BEKLIYOR -> StatusMeta("Bekliyor", t.amber, t.amberC)
    AppointmentStatus.ONAYLANDI -> StatusMeta("Onaylandı", t.primary, t.primaryC)
    AppointmentStatus.TAMAMLANDI -> StatusMeta("Tamamlandı", t.green, t.greenC)
    AppointmentStatus.IPTAL -> StatusMeta("İptal", t.red, t.redC)
}

fun serviceColor(serviceType: String, t: AppointmentTokens): Color = when {
    serviceType.contains("Petek", ignoreCase = true) -> t.primary
    serviceType.contains("Doğalgaz", ignoreCase = true) -> t.violet
    serviceType.contains("Gaz Kaçağı", ignoreCase = true) -> t.red
    else -> t.text2
}

private val AvatarPalette = listOf(
    Color(0xFFF59E0B),
    Color(0xFF14B8A6),
    Color(0xFF3B82F6),
    Color(0xFF8B5CF6),
    Color(0xFF22C55E),
    Color(0xFFEC4899),
    Color(0xFF0EA5E9)
)

fun avatarColors(name: String): Pair<Color, Color> {
    var h = 0
    for (ch in name) {
        h = ch.code + ((h shl 5) - h)
    }
    val fg = AvatarPalette[abs(h) % AvatarPalette.size]
    val bg = fg.copy(alpha = 0.18f)
    return bg to fg
}

fun customerInitials(name: String): String {
    return name.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.take(1) }
        .uppercase(TrLocale)
        .ifEmpty { "SK" }
}

@Composable
fun CustomerAvatarCircle(
    name: String,
    size: Dp,
    fontSize: TextUnit = (size.value * 0.36f).sp,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = remember(name) { avatarColors(name) }
    val ini = remember(name) { customerInitials(name) }
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = bg
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = ini,
                color = fg,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

fun Modifier.dashedBorder(
    width: Dp,
    color: Color,
    cornerRadius: Dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 4.dp
): Modifier = this.drawWithContent {
    drawContent()
    val stroke = Stroke(
        width = width.toPx(),
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashLength.toPx(), gapLength.toPx()),
            0f
        )
    )
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = stroke
    )
}

// ============================================================================
// DATE & TIME HELPERS
// ============================================================================

fun todayIsoString(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}

fun addDaysIso(baseIso: String, offsetDays: Int): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val parsed = sdf.parse(baseIso) ?: Date()
        val cal = Calendar.getInstance().apply {
            time = parsed
            add(Calendar.DAY_OF_YEAR, offsetDays)
        }
        sdf.format(cal.time)
    } catch (_: Exception) {
        baseIso
    }
}

/**
 * Normalizes either "dd.MM.yyyy" or "yyyy-MM-dd" (or timestamps starting with yyyy-MM-dd)
 * into canonical "yyyy-MM-dd" for sorting, grouping, and filtering.
 */
fun normalizeToIsoDate(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.length >= 10 && trimmed[4] == '-' && trimmed[7] == '-') {
        return trimmed.substring(0, 10)
    }
    val dotParts = trimmed.split(".")
    if (dotParts.size == 3) {
        val d = dotParts[0].padStart(2, '0')
        val m = dotParts[1].padStart(2, '0')
        val y = dotParts[2].take(4)
        if (y.length == 4) return "$y-$m-$d"
    }
    val slashParts = trimmed.split("/")
    if (slashParts.size == 3) {
        val d = slashParts[0].padStart(2, '0')
        val m = slashParts[1].padStart(2, '0')
        val y = slashParts[2].take(4)
        if (y.length == 4) return "$y-$m-$d"
    }
    return trimmed
}

/**
 * Converts canonical "yyyy-MM-dd" to "dd.MM.yyyy" used by backend Appointment model.
 */
fun isoToDisplayDate(iso: String): String {
    val norm = normalizeToIsoDate(iso)
    val parts = norm.split("-")
    return if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}" else iso
}

fun daysBetweenIso(isoA: String, isoB: String): Int {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val a = sdf.parse(normalizeToIsoDate(isoA)) ?: return Int.MAX_VALUE
        val b = sdf.parse(normalizeToIsoDate(isoB)) ?: return Int.MAX_VALUE
        val diffMs = a.time - b.time
        (diffMs / (1000L * 60 * 60 * 24)).toInt()
    } catch (_: Exception) {
        Int.MAX_VALUE
    }
}

private val TrMonthsUpper = arrayOf(
    "OCAK", "ŞUBAT", "MART", "NİSAN", "MAYIS", "HAZİRAN",
    "TEMMUZ", "AĞUSTOS", "EYLÜL", "EKİM", "KASIM", "ARALIK"
)

private val TrMonthsShort = arrayOf(
    "Oca", "Şub", "Mar", "Nis", "May", "Haz",
    "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara"
)

private val TrDaysUpper = arrayOf(
    "PAZAR", "PAZARTESİ", "SALI", "ÇARŞAMBA", "PERŞEMBE", "CUMA", "CUMARTESİ"
)

private val TrDaysShort = arrayOf(
    "Paz", "Pzt", "Sal", "Çar", "Per", "Cum", "Cmt"
)

fun formatDateHeader(isoDate: String, todayIso: String): String {
    val norm = normalizeToIsoDate(isoDate)
    val tomorrowIso = addDaysIso(todayIso, 1)
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dt = sdf.parse(norm) ?: return isoDate
        val cal = Calendar.getInstance().apply { time = dt }
        val dayNum = cal.get(Calendar.DAY_OF_MONTH)
        val monthName = TrMonthsUpper[cal.get(Calendar.MONTH)]
        val dayName = TrDaysUpper[cal.get(Calendar.DAY_OF_WEEK) - 1]
        val base = "$dayNum $monthName $dayName"
        when (norm) {
            todayIso -> "BUGÜN · $base"
            tomorrowIso -> "YARIN · $base"
            else -> base
        }
    } catch (_: Exception) {
        isoDate
    }
}

fun formatDateShort(rawDate: String): String {
    val norm = normalizeToIsoDate(rawDate)
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dt = sdf.parse(norm) ?: return rawDate
        val cal = Calendar.getInstance().apply { time = dt }
        "${cal.get(Calendar.DAY_OF_MONTH)} ${TrMonthsShort[cal.get(Calendar.MONTH)]}"
    } catch (_: Exception) {
        rawDate
    }
}

fun quickDateSub(baseTodayIso: String, offsetDays: Int): String {
    val targetIso = addDaysIso(baseTodayIso, offsetDays)
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dt = sdf.parse(targetIso) ?: return targetIso
        val cal = Calendar.getInstance().apply { time = dt }
        "${cal.get(Calendar.DAY_OF_MONTH)} ${TrMonthsShort[cal.get(Calendar.MONTH)]} ${TrDaysShort[cal.get(Calendar.DAY_OF_WEEK) - 1]}"
    } catch (_: Exception) {
        targetIso
    }
}

fun parseSlotTimes(timeSlot: String): Pair<String, String> {
    val cleaned = timeSlot.replace("–", "-").replace("—", "-")
    val parts = cleaned.split("-").map { it.trim() }.filter { it.isNotEmpty() }
    val start = parts.getOrNull(0)?.take(5) ?: "09:00"
    val end = parts.getOrNull(1)?.take(5) ?: run {
        val h = start.take(2).toIntOrNull() ?: 9
        val nextH = (h + 1) % 24
        "%02d:00".format(nextH)
    }
    return start to end
}

fun normalizeSlotKey(timeSlot: String): String {
    val (s, e) = parseSlotTimes(timeSlot)
    return "$s-$e"
}

fun formatTL(amount: Double): String {
    val symbols = DecimalFormatSymbols(TrLocale).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val df = DecimalFormat("#,##0.00", symbols)
    return df.format(amount)
}

fun formatTLInt(amount: Double): String {
    val symbols = DecimalFormatSymbols(TrLocale).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val df = DecimalFormat("#,##0", symbols)
    return df.format(amount)
}

// ============================================================================
// INTENTS (CALL, WHATSAPP, NAVIGATION)
// ============================================================================

fun launchDialer(context: Context, phone: String) {
    try {
        val cleanPhone = phone.filter { it.isDigit() || it == '+' }
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}

fun launchWhatsApp(context: Context, phone: String, message: String = "") {
    try {
        var digits = phone.filter { it.isDigit() }
        if (digits.startsWith("0")) digits = "90" + digits.drop(1)
        else if (!digits.startsWith("90") && digits.length == 10) digits = "90$digits"
        val encodedMsg = if (message.isNotBlank()) "&text=${Uri.encode(message)}" else ""
        val url = "https://api.whatsapp.com/send?phone=$digits$encodedMsg"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}

fun launchNavigation(context: Context, address: String, district: String) {
    try {
        val fullQuery = listOf(address, district, "İstanbul")
            .filter { it.isNotBlank() }
            .joinToString(", ")
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(fullQuery)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}

val Appointment.fullAddressDisplay: String
    get() = buildList {
        if (neighborhood.isNotBlank()) add(neighborhood.trim())
        if (streetDoorNo.isNotBlank()) add(streetDoorNo.trim())
        if (addressDetail.isNotBlank() && addressDetail.trim() != streetDoorNo.trim()) add(addressDetail.trim())
    }.joinToString(", ").ifBlank { district.ifBlank { "Adres belirtilmedi" } }

val Appointment.noteDisplay: String
    get() = problemNote.trim()

fun launchFullRouteInGoogleMaps(context: Context, stops: List<Appointment>) {
    if (stops.isEmpty()) return
    try {
        val ordered = stops.map {
            listOf(it.fullAddressDisplay, it.district, "İstanbul").filter { s -> s.isNotBlank() }.joinToString(", ")
        }
        val dest = Uri.encode(ordered.last())
        val waypoints = if (ordered.size > 1) {
            "&waypoints=" + Uri.encode(ordered.dropLast(1).joinToString("|"))
        } else ""
        val url = "https://www.google.com/maps/dir/?api=1&travelmode=driving&destination=$dest$waypoints"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}

// ============================================================================
// DOMAIN CONSTANTS FROM PROTOTYPE
// ============================================================================

val AllAppointmentTimeSlots = listOf(
    "09:00-10:00", "10:00-11:00", "11:00-12:00",
    "12:00-13:00", "13:00-14:00", "14:00-15:00",
    "15:00-16:00", "16:00-17:00", "17:00-18:00",
    "18:00-19:00", "19:00-20:00", "20:00-21:00",
    "21:00-22:00", "22:00-23:00", "23:00-00:00"
)

data class FaultChipItem(
    val label: String,
    val value: String,
    val urgent: Boolean = false
)

val QuickFaultChips = listOf(
    FaultChipItem("🔥 Sıcak Su Gelmiyor", "Sıcak su gelmiyor"),
    FaultChipItem("❄️ Petekler Isınmıyor", "Petekler ısınmıyor"),
    FaultChipItem("💧 Su Akıtıyor", "Cihaz su akıtıyor"),
    FaultChipItem("📉 Basınç Düşüyor", "Su basıncı sürekli düşüyor"),
    FaultChipItem("⚡ Ateşleme / F1 / E01", "Ateşleme hatası veriyor"),
    FaultChipItem("🔊 Ses / Gürültü", "Cihazdan ses geliyor"),
    FaultChipItem("🛠️ Yıllık Bakım", "Yıllık periyodik kombi bakımı"),
    FaultChipItem("⚠️ Gaz Kokusu (Acil)", "ACİL: Gaz kokusu var", urgent = true)
)

data class BoilerBrandItem(
    val name: String,
    val color: Color
)

val BoilerBrands = listOf(
    BoilerBrandItem("DemirDöküm", Color(0xFF1D4ED8)),
    BoilerBrandItem("Baymak", Color(0xFF0F766E)),
    BoilerBrandItem("E.C.A.", Color(0xFF0369A1)),
    BoilerBrandItem("Vaillant", Color(0xFF15803D)),
    BoilerBrandItem("BOSCH", Color(0xFFDC2626)),
    BoilerBrandItem("Buderus", Color(0xFF1E3A8A)),
    BoilerBrandItem("ARISTON", Color(0xFFBE123C)),
    BoilerBrandItem("Viessmann", Color(0xFFEA580C)),
    BoilerBrandItem("Protherm", Color(0xFFF97316)),
    BoilerBrandItem("Airfel", Color(0xFF0284C7)),
    BoilerBrandItem("Ferroli", Color(0xFFB91C1C))
)

data class JobTemplateChip(
    val label: String,
    val text: String
)

val JobTemplateChips = listOf(
    JobTemplateChip("🔥 Yıllık Periyodik Bakım", "Yıllık periyodik kombi bakımı yapıldı"),
    JobTemplateChip("🌡️ NTC Sensör Değişti", "NTC sıcaklık sensörü yenilendi"),
    JobTemplateChip("🎈 Genleşme Tankı Basıldı (1 bar)", "Genleşme tankına 1 bar azot/hava basıldı"),
    JobTemplateChip("🧽 Eşanjör Kireç Temizliği", "Plakalı eşanjör sökülerek kimyasalla temizlendi"),
    JobTemplateChip("🔄 Doldurma Musluğu Yenilendi", "Su doldurma musluğu değiştirildi"),
    JobTemplateChip("🌀 Fan Motoru Temizlendi", "Fan motoru sökülüp temizlendi ve yağlandı"),
    JobTemplateChip("🔁 Sirkülasyon Pompası Revize", "Sirkülasyon pompası sıkışması giderildi"),
    JobTemplateChip("⚙️ 3 Yollu Vana Motoru Değişti", "Üç yollu vana tamir takımı / motoru değiştirildi"),
    JobTemplateChip("⚡ Ateşleme / İyonizasyon", "Ateşleme ve iyonizasyon elektrodu temizlendi")
)

data class ReceiptPartOption(
    val id: String,
    val name: String,
    val price: Double,
    val source: String,
    val category: String = "🔧 Bakım & Diğer",
    val stockId: String? = null
)

val PartSupplierOptions = listOf(
    "Tümü",
    "🏢 Dükkan Depo",
    "🏷️ İlhan Isı",
    "🛒 Paşa Market"
)

val PartCategoryOptions = listOf(
    "Tümü",
    "🔥 Sensör & Ateşleme",
    "💧 Hidrolik & Vana",
    "🔄 Pompa & Fan",
    "♨️ Eşanjör & Tank",
    "🧠 Kart & Elektronik",
    "🔧 Bakım & Diğer"
)

fun normalizePartSupplier(rawLocation: String, rawBrand: String = ""): String {
    val combined = "$rawLocation $rawBrand".trim()
    return when {
        combined.contains("İlhan", ignoreCase = true) || combined.contains("Ilhan", ignoreCase = true) -> "🏷️ İlhan Isı"
        combined.contains("Paşa", ignoreCase = true) || combined.contains("Pasa", ignoreCase = true) -> "🛒 Paşa Market"
        combined.contains("Dükkan", ignoreCase = true) || combined.contains("Depo", ignoreCase = true) || combined.isBlank() || combined.equals("Konumsuz", ignoreCase = true) -> "🏢 Dükkan Depo"
        else -> "🏷️ ${rawLocation.trim()}"
    }
}

fun inferPartCategory(name: String, rawCategory: String = ""): String {
    val text = "$rawCategory $name".lowercase(TrLocale)
    return when {
        text.contains("ntc") || text.contains("sensör") || text.contains("sensor") ||
            text.contains("ateşleme") || text.contains("atesleme") || text.contains("iyonizasyon") ||
            text.contains("elektrot") || text.contains("prosestat") || text.contains("gaz valf") -> "🔥 Sensör & Ateşleme"
        text.contains("vana") || text.contains("musluk") || text.contains("ventil") ||
            text.contains("türbin") || text.contains("turbin") || text.contains("şalter") ||
            text.contains("salter") || text.contains("hidrolik") || text.contains("manometre") ||
            text.contains("basınç") || text.contains("basinc") -> "💧 Hidrolik & Vana"
        text.contains("pompa") || text.contains("fan") || text.contains("motor") ||
            text.contains("sirkülasyon") || text.contains("sirkulasyon") -> "🔄 Pompa & Fan"
        text.contains("eşanjör") || text.contains("esanjor") || text.contains("tank") ||
            text.contains("genleşme") || text.contains("genlesme") || text.contains("boyler") -> "♨️ Eşanjör & Tank"
        text.contains("kart") || text.contains("anakart") || text.contains("elektronik") ||
            text.contains("ekran") || text.contains("termostat") || text.contains("röle") -> "🧠 Kart & Elektronik"
        else -> "🔧 Bakım & Diğer"
    }
}

val DefaultReceiptParts = listOf(
    ReceiptPartOption("p1", "NTC Sıcaklık Sensörü", 350.0, "🏢 Dükkan Depo", "🔥 Sensör & Ateşleme"),
    ReceiptPartOption("p7", "Ateşleme / İyonizasyon Elektrodu", 450.0, "🏢 Dükkan Depo", "🔥 Sensör & Ateşleme"),
    ReceiptPartOption("p8", "Hava Prosestatı", 750.0, "🏷️ İlhan Isı", "🔥 Sensör & Ateşleme"),
    ReceiptPartOption("p9", "Gaz Valfi", 2100.0, "🏷️ İlhan Isı", "🔥 Sensör & Ateşleme"),
    ReceiptPartOption("p2", "Doldurma Musluğu", 650.0, "🏢 Dükkan Depo", "💧 Hidrolik & Vana"),
    ReceiptPartOption("p3", "3 Yollu Vana Motoru", 1450.0, "🏷️ İlhan Isı", "💧 Hidrolik & Vana"),
    ReceiptPartOption("p10", "3 Yollu Vana Tamir Takımı", 850.0, "🏢 Dükkan Depo", "💧 Hidrolik & Vana"),
    ReceiptPartOption("p11", "Emniyet Ventili (3 Bar)", 550.0, "🛒 Paşa Market", "💧 Hidrolik & Vana"),
    ReceiptPartOption("p12", "Akış Türbini / Şalteri", 750.0, "🛒 Paşa Market", "💧 Hidrolik & Vana"),
    ReceiptPartOption("p6", "Sirkülasyon Pompası", 3200.0, "🏷️ İlhan Isı", "🔄 Pompa & Fan"),
    ReceiptPartOption("p13", "Fan Motoru", 2600.0, "🏷️ İlhan Isı", "🔄 Pompa & Fan"),
    ReceiptPartOption("p4", "Genleşme Tankı (8 Lt)", 1850.0, "🏷️ İlhan Isı", "♨️ Eşanjör & Tank"),
    ReceiptPartOption("p5", "Plakalı Eşanjör", 2400.0, "🛒 Paşa Market", "♨️ Eşanjör & Tank"),
    ReceiptPartOption("p14", "Anakart Tamiri / Revizyon", 2200.0, "🏢 Dükkan Depo", "🧠 Kart & Elektronik"),
    ReceiptPartOption("p15", "Tesisat Yıkama Kimyasalı & Filtre", 600.0, "🛒 Paşa Market", "🔧 Bakım & Diğer")
)

data class WarrantyOption(
    val key: String,
    val label: String,
    val months: Int?
)

val WarrantyOptions = listOf(
    WarrantyOption("none", "Yok", null),
    WarrantyOption("1m", "1 Ay", 1),
    WarrantyOption("3m", "3 Ay", 3),
    WarrantyOption("6m", "6 Ay", 6),
    WarrantyOption("1y", "1 Yıl", 12),
    WarrantyOption("2y", "2 Yıl", 24)
)

// ============================================================================
// SERVİS FOTOĞRAF GÖRÜNTÜLEYİCİ & GALERİ BİLEŞENLERİ
// ============================================================================

@Composable
fun PhotoViewerDialog(
    photoUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Servis Fotoğrafı",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 70.dp, bottom = 24.dp, start = 12.dp, end = 12.dp)
            )

            // Üst Kontrol Barı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.65f)
                ) {
                    Text(
                        text = "📸 Servis Fotoğrafı",
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }

                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "✕",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentPhotoGallery(
    photoUris: List<String>,
    t: AppointmentTokens,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (photoUris.isEmpty()) return

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = t.surface2,
        border = BorderStroke(1.dp, t.border),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📸 SERVİS FOTOĞRAFLARI",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = t.text2
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = t.primaryC
                ) {
                    Text(
                        text = "${photoUris.size} Adet",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = t.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(photoUris) { uriStr ->
                    Surface(
                        onClick = { onPhotoClick(uriStr) },
                        shape = RoundedCornerShape(12.dp),
                        color = t.surface,
                        border = BorderStroke(1.dp, t.border),
                        modifier = Modifier.size(86.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = uriStr,
                                contentDescription = "Fotoğraf",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Büyüteç simgesi
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔍", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

