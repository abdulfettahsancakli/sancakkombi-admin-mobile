package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus
import com.example.data.model.Customer
import com.example.data.model.FinanceRecord
import com.example.data.model.FinanceType
import com.example.data.model.MessageLog
import com.example.data.remote.DeviceHistoryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlin.math.abs

val CustomerAvatarPalette = listOf(
    Color(0xFF0EA5E9),
    Color(0xFF14B8A6),
    Color(0xFF8B5CF6),
    Color(0xFFF59E0B),
    Color(0xFF22C55E),
    Color(0xFFEC4899),
    Color(0xFF3B82F6)
)

val PopularBoilerBrands = listOf(
    "E.C.A.",
    "Demirdöküm",
    "Buderus",
    "Vaillant",
    "Baymak",
    "Bosch",
    "Viessmann",
    "Protherm",
    "Alarko"
)

val QuickBoilerBrands = listOf(
    "E.C.A.",
    "Demirdöküm",
    "Buderus",
    "Vaillant",
    "Baymak",
    "Bosch"
)

val CustomerPriorityDistricts = listOf(
    "Bayrampaşa",
    "Gaziosmanpaşa",
    "Eyüpsultan",
    "Esenler",
    "Zeytinburnu",
    "Fatih",
    "Güngören",
    "Bağcılar",
    "Küçükçekmece",
    "Sultangazi",
    "Kağıthane",
    "Şişli",
    "Beşiktaş",
    "Bakırköy",
    "Bahçelievler",
    "Avcılar",
    "Başakşehir"
)

val AppointmentTokens.violetC: Color
    get() = if (isDark) Color(0xFFA78BFA).copy(alpha = 0.14f) else Color(0xFF7C3AED).copy(alpha = 0.10f)

fun customerAvatarColor(idOrName: String, fallbackIndex: Int = 0): Color {
    if (idOrName.isBlank()) return CustomerAvatarPalette[abs(fallbackIndex) % CustomerAvatarPalette.size]
    val knownIndex = when (idOrName) {
        "c1" -> 0
        "c2" -> 1
        "c3" -> 2
        "c4" -> 3
        "c5" -> 4
        "c6" -> 5
        "c7" -> 6
        "c8" -> 0
        else -> null
    }
    if (knownIndex != null) return CustomerAvatarPalette[knownIndex]
    val hash = idOrName.fold(0) { acc, ch -> (acc * 31 + ch.code) and 0x7FFFFFFF }
    return CustomerAvatarPalette[hash % CustomerAvatarPalette.size]
}

fun dialCustomerPhone(context: Context, phone: String) {
    val cleanPhone = phone.filter { it.isDigit() || it == '+' }
    if (cleanPhone.isBlank()) return
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}

fun openCustomerWhatsApp(context: Context, phone: String, message: String = "") {
    val digits = phone.filter { it.isDigit() }
    val normalized = when {
        digits.startsWith("90") && digits.length == 12 -> digits
        digits.startsWith("0") && digits.length == 11 -> "90" + digits.drop(1)
        digits.length == 10 -> "90$digits"
        else -> digits
    }
    if (normalized.isBlank()) return
    val encodedText = if (message.isNotBlank()) "?text=${Uri.encode(message)}" else ""
    try {
        val uri = Uri.parse("https://wa.me/$normalized$encodedText")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}

fun normalizeTrPhoneDigits(raw: String): String {
    val d = raw.filter { it.isDigit() }
    if (d.length == 12 && d.startsWith("90")) return "0" + d.substring(2)
    if (d.length == 10 && (d.startsWith("5") || d.startsWith("2") || d.startsWith("3") || d.startsWith("4"))) {
        return "0$d"
    }
    return d
}

fun formatTrPhoneDisplay(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.matches(Regex("^0\\d{3}\\s\\d{3}\\s\\d{2}\\s\\d{2}$"))) return trimmed
    val norm = normalizeTrPhoneDigits(trimmed)
    if (norm.length == 11 && norm.startsWith("0")) {
        return "${norm.substring(0, 4)} ${norm.substring(4, 7)} ${norm.substring(7, 9)} ${norm.substring(9, 11)}"
    }
    return trimmed.ifBlank { "Belirtilmemiş" }
}

fun isLandlinePhone(raw: String): Boolean {
    val norm = normalizeTrPhoneDigits(raw)
    return norm.startsWith("02") || norm.startsWith("03") || norm.startsWith("04")
}

private val PersonalNameRegex = Regex(
    "(^|\\s)(anne|annem|baba|babam|abi|abim|abla|ablam|kardeş|kardes|eşim|esim|aşkım|askim|canım|canim|ev|iş|is|dayı|dayi|teyze|hala|amca)(\\s|$)",
    RegexOption.IGNORE_CASE
)

fun isPersonalContactName(name: String): Boolean {
    val trimmed = name.trim()
    if (PersonalNameRegex.containsMatchIn(trimmed)) return true
    if (trimmed.contains("❤️") || trimmed.contains("💕") || trimmed.contains("🏠") || trimmed.contains("👪") || trimmed.contains("♥️")) {
        return true
    }
    return false
}

fun formatCustomerCurrency(amount: Double): String {
    val symbols = DecimalFormatSymbols(TrLocale).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val df = DecimalFormat("#,##0", symbols)
    return df.format(amount)
}

fun copyPhoneToClipboard(context: Context, phone: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("Telefon", phone))
}

fun openGoogleMapsDirections(context: Context, address: String, district: String) {
    val query = buildString {
        if (address.isNotBlank()) append(address).append(", ")
        if (district.isNotBlank() && district != "Belirtilmemiş") append(district).append(", ")
        append("İstanbul")
    }
    try {
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {
        }
    }
}

data class CustomerLocalOverride(
    val name: String? = null,
    val phone: String? = null,
    val district: String? = null,
    val address: String? = null,
    val gateNote: String? = null,
    val serviceNote: String? = null,
    val deviceBrand: String? = null,
    val deviceModel: String? = null,
    val deviceNotes: String? = null,
    val autoWhatsApp: Boolean? = null
)

data class CustomerServiceRecordItem(
    val id: String,
    val date: String,
    val title: String,
    val desc: String,
    val partsText: String,
    val priceText: String
)

data class CustomerReceiptItem(
    val id: String,
    val no: String,
    val date: String,
    val amount: Double,
    val status: String, // "Ödendi" | "Bekliyor"
    val method: String,
    val items: String,
    val backingRecord: FinanceRecord? = null
)

data class CustomerMessageItem(
    val id: String,
    val date: String,
    val channel: String,
    val template: String,
    val status: String, // "Okundu" | "İletildi" | "Gönderildi"
    val preview: String
)

data class ResolvedCustomerProfile(
    val customer: Customer,
    val displayPhone: String,
    val normalizedPhone: String,
    val district: String,
    val address: String,
    val gateNote: String,
    val serviceNote: String,
    val brand: String,
    val model: String,
    val boilerNote: String,
    val autoWhatsApp: Boolean,
    val registeredAt: String,
    val appointmentCount: Int,
    val activeAppointmentCount: Int,
    val isActive: Boolean,
    val isLoyal: Boolean,
    val hasBoiler: Boolean,
    val appointments: List<Appointment>,
    val serviceHistory: List<CustomerServiceRecordItem>,
    val receipts: List<CustomerReceiptItem>,
    val messages: List<CustomerMessageItem>,
    val billedAmount: Double,
    val paidAmount: Double,
    val pendingAmount: Double
)

data class WaTemplateDef(
    val id: String,
    val title: String,
    val body: String
)

val DefaultWaTemplates = listOf(
    WaTemplateDef(
        id = "t1",
        title = "Randevu Onayı",
        body = "Sayın {ad}, randevunuz onaylanmıştır. Belirlenen saat aralığında adresinizde olacağız. — Sancak Kombi Servisi"
    ),
    WaTemplateDef(
        id = "t2",
        title = "Yola Çıkıyoruz",
        body = "Sayın {ad}, teknisyenimiz adresinize doğru yola çıkmıştır. Yaklaşık 25 dk içinde kapınızdayız. 🛠️"
    ),
    WaTemplateDef(
        id = "t3",
        title = "Yıllık Bakım Hatırlatması",
        body = "Merhaba {ad}, kombinizin yıllık bakım zamanı geldi. Verimli ve güvenli kullanım için randevu oluşturabilirsiniz. 🔥"
    ),
    WaTemplateDef(
        id = "t4",
        title = "Teşekkür & Memnuniyet",
        body = "Sayın {ad}, servisimizi tercih ettiğiniz için teşekkür ederiz. Herhangi bir sorunuzda bir telefon uzağınızdayız. 🙏"
    )
)

fun resolveCustomerProfile(
    customer: Customer,
    override: CustomerLocalOverride?,
    allAppointments: List<Appointment>,
    allFinanceRecords: List<FinanceRecord>,
    allMessageLogs: List<MessageLog>,
    sessionMessages: List<CustomerMessageItem>,
    paidReceiptIds: Set<String>,
    deviceHistory: DeviceHistoryDto?
): ResolvedCustomerProfile {
    val name = override?.name ?: customer.name
    val phone = override?.phone ?: customer.phone
    val normPhone = normalizeTrPhoneDigits(phone)
    val district = (override?.district ?: customer.district).ifBlank { "Bayrampaşa" }
    val address = override?.address ?: customer.address

    val matchingAppointments = allAppointments.filter { appt ->
        appt.customerId == customer.id ||
            (normPhone.isNotBlank() && normalizeTrPhoneDigits(appt.phone) == normPhone) ||
            appt.customerName.trim().equals(name.trim(), ignoreCase = true)
    }

    val defaultSeedBrand = when (customer.id) {
        "c1" -> "E.C.A."
        "c2" -> "Demirdöküm"
        "c4" -> "Vaillant"
        "c5" -> "Bosch"
        "c6" -> "Baymak"
        else -> ""
    }
    val defaultSeedModel = when (customer.id) {
        "c1" -> "Proteus Premix 24 kW"
        "c2" -> "Nitromix P24"
        "c4" -> "ecoTEC Pro 286"
        "c5" -> "Condens 2200i W"
        "c6" -> "Duo Tec Compact"
        else -> ""
    }
    val defaultSeedBoilerNote = when (customer.id) {
        "c1" -> "2024 montaj, yıllık bakım hatırlatması açık"
        "c2" -> "Garanti dışı, eşanjör temizlendi"
        "c4" -> "Su basıncı sık düşüyor, genleşme tankı kontrol edildi"
        "c5" -> "Yeni montaj 2025"
        "c6" -> "2022 model, düzenli bakım müşterisi"
        else -> ""
    }
    val defaultSeedGateNote = when (customer.id) {
        "c1" -> "Kat 2 Daire 4, zil çalışmıyor telefonla arayın"
        "c2" -> "Giriş kat, sağdaki daire"
        "c3" -> "Site içi B Blok D:12"
        "c6" -> "Asansörsüz bina 4. kat"
        else -> ""
    }
    val defaultSeedRegisteredAt = when (customer.id) {
        "c1" -> "12.01.2025"
        "c2" -> "04.03.2025"
        "c3" -> "19.11.2025"
        "c4" -> "22.02.2026"
        "c5" -> "01.04.2026"
        "c6" -> "08.09.2024"
        else -> SimpleDateFormat("dd.MM.yyyy", TrLocale).format(Date())
    }

    val brandFromNotes = PopularBoilerBrands.firstOrNull { b ->
        customer.notes.contains(b.removeSuffix("."), ignoreCase = true)
    } ?: ""

    val brandFromAppts = matchingAppointments.firstNotNullOfOrNull {
        it.jobReport?.deviceBrand?.takeIf { b -> b.isNotBlank() }
    } ?: ""

    val modelFromAppts = matchingAppointments.firstNotNullOfOrNull {
        it.jobReport?.deviceModel?.takeIf { m -> m.isNotBlank() }
    } ?: ""

    val resolvedBrand = override?.deviceBrand
        ?: customer.deviceBrand.takeIf { it.isNotBlank() }
        ?: deviceHistory?.deviceBrand?.takeIf { it.isNotBlank() }
        ?: brandFromAppts.takeIf { it.isNotBlank() }
        ?: brandFromNotes.takeIf { it.isNotBlank() }
        ?: defaultSeedBrand

    val resolvedModel = override?.deviceModel
        ?: customer.deviceModel.takeIf { it.isNotBlank() }
        ?: deviceHistory?.deviceModel?.takeIf { it.isNotBlank() }
        ?: modelFromAppts.takeIf { it.isNotBlank() }
        ?: defaultSeedModel

    val resolvedBoilerNote = override?.deviceNotes
        ?: customer.deviceNotes.takeIf { it.isNotBlank() }
        ?: deviceHistory?.deviceNotes?.takeIf { it.isNotBlank() }
        ?: defaultSeedBoilerNote

    val resolvedGateNote = override?.gateNote
        ?: customer.gateNote.takeIf { it.isNotBlank() }
        ?: defaultSeedGateNote

    val resolvedServiceNote = override?.serviceNote
        ?: customer.serviceNote.takeIf { it.isNotBlank() }
        ?: customer.notes

    val resolvedAutoWa = override?.autoWhatsApp ?: customer.autoWhatsApp
    val resolvedReg = customer.registeredAt.ifBlank { defaultSeedRegisteredAt }

    // Service History
    val historyFromDto = deviceHistory?.records.orEmpty().mapIndexed { idx, rec ->
        val partsStr = rec.parts.joinToString(" + ") { p ->
            if (p.quantity > 1) "${p.name} x${p.quantity}" else p.name
        }.ifBlank { "Standart Bakım & Kontrol" }
        val total = rec.parts.sumOf { (it.unitPrice ?: 0.0) * it.quantity }
        CustomerServiceRecordItem(
            id = rec.appointmentId.ifBlank { "dh_$idx" },
            date = rec.date,
            title = rec.serviceTitle.ifBlank { "Kombi Bakım & Servis" },
            desc = rec.workDescription.ifBlank { "Servis işlemi tamamlandı" },
            partsText = partsStr,
            priceText = if (total > 0) "₺${formatCustomerCurrency(total)}" else "Servis Kaydı"
        )
    }

    val historyFromAppts = matchingAppointments
        .filter { it.status == AppointmentStatus.TAMAMLANDI || it.jobReport != null }
        .map { appt ->
            val rep = appt.jobReport
            val partsStr = rep?.usedParts?.joinToString(" + ") { p ->
                if (p.quantity > 1) "${p.name} x${p.quantity}" else p.name
            }.orEmpty().ifBlank { "Genel kontrol + filtre temizliği" }
            val priceVal = rep?.collectedAmount?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
            CustomerServiceRecordItem(
                id = appt.id,
                date = appt.date,
                title = appt.serviceType.ifBlank { "Kombi Servisi" },
                desc = rep?.workDoneNote?.ifBlank { appt.problemNote } ?: appt.problemNote.ifBlank { "Servis tamamlandı" },
                partsText = partsStr,
                priceText = if (priceVal > 0) "₺${formatCustomerCurrency(priceVal)}" else "Tamamlandı"
            )
        }

    val mergedServiceHistory = (historyFromDto + historyFromAppts)
        .distinctBy { "${it.date}_${it.title}" }
        .ifEmpty {
            when (customer.id) {
                "c1" -> listOf(
                    CustomerServiceRecordItem(
                        id = "s1",
                        date = "28.05.2026",
                        title = "Ateşleme Arızası Onarımı",
                        desc = "Ateşleyici elektrodu ve O-ring takımı yenilendi, gaz ayarı yapıldı.",
                        partsText = "Ateşleyici Elektrodu + O-Ring Takımı",
                        priceText = "₺1.370"
                    ),
                    CustomerServiceRecordItem(
                        id = "s2",
                        date = "25.05.2026",
                        title = "Yıllık Periyodik Kombi Bakımı",
                        desc = "Yanma odası temizliği, filtre temizliği, genleşme tankı hava basıncı 1.0 bar ayarlandı.",
                        partsText = "Standart Bakım Paketi",
                        priceText = "₺1.000"
                    )
                )
                "c6" -> listOf(
                    CustomerServiceRecordItem(
                        id = "s3",
                        date = "14.11.2025",
                        title = "Petek Temizliği & Kombi Bakımı",
                        desc = "Makine ile 7 petek ilaçlı yıkama + kombi genel bakım.",
                        partsText = "Koruyucu Kimyasal + Bakım",
                        priceText = "₺2.200"
                    )
                )
                else -> emptyList()
            }
        }

    // Finance Receipts
    val apptIds = matchingAppointments.map { it.id }.toSet()
    val matchingFinance = allFinanceRecords.filter { rec ->
        rec.type == FinanceType.GELIR && (
            (rec.appointmentId != null && rec.appointmentId in apptIds) ||
                rec.source.contains(name.trim(), ignoreCase = true)
            )
    }

    val mappedReceipts = matchingFinance.map { rec ->
        val isMarkedPaid = rec.id in paidReceiptIds || rec.status.equals("Ödendi", ignoreCase = true)
        CustomerReceiptItem(
            id = rec.id,
            no = rec.receiptNo.ifBlank { "SK-${rec.id.uppercase()}" },
            date = rec.date,
            amount = rec.totalAmount.takeIf { it > 0 } ?: rec.amount,
            status = if (isMarkedPaid) "Ödendi" else "Bekliyor",
            method = rec.category.ifBlank { "Nakit / Havale" },
            items = rec.note.ifBlank { rec.source },
            backingRecord = rec
        )
    }.ifEmpty {
        when (customer.id) {
            "c1" -> listOf(
                CustomerReceiptItem(
                    id = "f2",
                    no = "SK-202605-28A6F1",
                    date = "28.05.2026",
                    amount = 1370.0,
                    status = "Ödendi",
                    method = "Nakit",
                    items = "Ateşleyici elektrodu + O-Ring takımı + Servis Bedeli"
                ),
                CustomerReceiptItem(
                    id = "f3",
                    no = "SK-202605-25B3C2",
                    date = "25.05.2026",
                    amount = 1000.0,
                    status = "Ödendi",
                    method = "IBAN Havale",
                    items = "Yıllık Periyodik Kombi Bakımı"
                ),
                CustomerReceiptItem(
                    id = "f_pend_c1",
                    no = "SK-202608-03P9D4",
                    date = "03.08.2026",
                    amount = 500.0,
                    status = if ("f_pend_c1" in paidReceiptIds) "Ödendi" else "Bekliyor",
                    method = "Açık Hesap",
                    items = "Ön Arıza Tespit & Servis Kaydı"
                )
            )
            "c6" -> listOf(
                CustomerReceiptItem(
                    id = "f_c6_1",
                    no = "SK-202511-14K8M2",
                    date = "14.11.2025",
                    amount = 2200.0,
                    status = "Ödendi",
                    method = "Kredi Kartı",
                    items = "Petek Temizliği & Kombi Bakımı"
                )
            )
            else -> emptyList()
        }
    }

    val billed = mappedReceipts.sumOf { it.amount }.takeIf { it > 0 } ?: customer.billedAmount
    val paid = mappedReceipts.filter { it.status == "Ödendi" }.sumOf { it.amount }
        .takeIf { mappedReceipts.isNotEmpty() } ?: customer.paidAmount
    val pending = (billed - paid).coerceAtLeast(0.0)

    // Messages
    val repoMessages = allMessageLogs.filter { log ->
        (normPhone.isNotBlank() && normalizeTrPhoneDigits(log.recipient) == normPhone) ||
            log.recipient.contains(name.trim(), ignoreCase = true)
    }.map { log ->
        val st = when (log.status.lowercase(TrLocale)) {
            "read", "okundu" -> "Okundu"
            "delivered", "iletildi" -> "İletildi"
            else -> "Gönderildi"
        }
        CustomerMessageItem(
            id = log.id,
            date = log.time,
            channel = if (log.channel.contains("SMS", ignoreCase = true)) "SMS" else "WhatsApp",
            template = log.template,
            status = st,
            preview = "Sayın $name, ${log.template.lowercase(TrLocale)} bilgilendirme mesajınız iletilmiştir."
        )
    }

    val seedMessages = if (repoMessages.isEmpty() && customer.id == "c1") {
        listOf(
            CustomerMessageItem(
                id = "m_seed_1",
                date = "03.08.2026 · 09:15",
                channel = "WhatsApp",
                template = "Randevu Onayı",
                status = "Okundu",
                preview = "Sayın Fettah Sancaklı, 03.08.2026 13:00-15:00 randevunuz onaylanmıştır."
            ),
            CustomerMessageItem(
                id = "m_seed_2",
                date = "28.05.2026 · 17:42",
                channel = "WhatsApp",
                template = "Dijital Servis Fişi",
                status = "Okundu",
                preview = "SK-202605-28A6F1 numaralı servis fişiniz hazırlandı. Toplam: ₺1.370,00"
            )
        )
    } else emptyList()

    val mergedMessages = (sessionMessages + repoMessages + seedMessages).distinctBy { it.id }

    val activeApptsCount = matchingAppointments.count {
        it.status == AppointmentStatus.BEKLIYOR ||
            it.status == AppointmentStatus.ONAYLANDI
    }.let { count ->
        if (matchingAppointments.isEmpty()) customer.activeAppointmentCount else count
    }

    val totalApptsCount = maxOf(
        customer.appointmentCount,
        matchingAppointments.size,
        mergedServiceHistory.size
    )

    val updatedCustomer = customer.copy(
        name = name,
        phone = formatTrPhoneDisplay(phone),
        district = district,
        address = address,
        gateNote = resolvedGateNote,
        serviceNote = resolvedServiceNote,
        deviceBrand = resolvedBrand,
        deviceModel = resolvedModel,
        deviceNotes = resolvedBoilerNote,
        autoWhatsApp = resolvedAutoWa,
        registeredAt = resolvedReg,
        appointmentCount = totalApptsCount,
        activeAppointmentCount = activeApptsCount
    )

    return ResolvedCustomerProfile(
        customer = updatedCustomer,
        displayPhone = formatTrPhoneDisplay(phone),
        normalizedPhone = normPhone,
        district = district,
        address = address,
        gateNote = resolvedGateNote,
        serviceNote = resolvedServiceNote,
        brand = resolvedBrand,
        model = resolvedModel,
        boilerNote = resolvedBoilerNote,
        autoWhatsApp = resolvedAutoWa,
        registeredAt = resolvedReg,
        appointmentCount = totalApptsCount,
        activeAppointmentCount = activeApptsCount,
        isActive = activeApptsCount > 0,
        isLoyal = totalApptsCount >= 2,
        hasBoiler = resolvedBrand.isNotBlank(),
        appointments = matchingAppointments,
        serviceHistory = mergedServiceHistory,
        receipts = mappedReceipts,
        messages = mergedMessages,
        billedAmount = billed,
        paidAmount = paid,
        pendingAmount = pending
    )
}

data class ScannedPhoneContact(
    val id: String,
    val name: String,
    val phone: String,
    val normalizedPhone: String,
    val alreadyExists: Boolean,
    val isPersonal: Boolean,
    val isLandline: Boolean
) {
    val defaultSelected: Boolean
        get() = !alreadyExists && !isPersonal && !isLandline
}

val FallbackSampleContacts = listOf(
    "Mehmet Kaya" to "0532 418 92 11",
    "Ayşe Demir" to "0544 812 30 45",
    "Mustafa Öztürk" to "0505 671 24 89",
    "Zeynep Çelik" to "0533 904 17 62",
    "Hüseyin Şahin" to "0542 319 88 04",
    "Emine Yıldız" to "0536 745 61 20",
    "İbrahim Aydın" to "0530 228 53 97",
    "Hatice Özdemir" to "0545 603 79 14",
    "Fettah Sancaklı" to "0541 328 06 98",
    "Fatih" to "0535 555 46 26",
    "Annem ❤️" to "0538 111 22 33",
    "Ev Sabit Hat" to "0212 614 22 10"
)

suspend fun scanDeviceContactsOrFallback(
    context: Context,
    existingCustomerPhones: Set<String>
): List<ScannedPhoneContact> = withContext(Dispatchers.IO) {
    val rawContacts = mutableListOf<Pair<String, String>>()
    try {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (cursor.moveToNext()) {
                val n = if (nameIdx >= 0) cursor.getString(nameIdx)?.trim().orEmpty() else ""
                val p = if (numIdx >= 0) cursor.getString(numIdx)?.trim().orEmpty() else ""
                if (n.isNotBlank() && p.isNotBlank()) {
                    rawContacts.add(n to p)
                }
            }
        }
    } catch (_: SecurityException) {
        // Permission not granted; will use fallback sample contacts
    } catch (_: Exception) {
    }

    val sourceList = if (rawContacts.isNotEmpty()) rawContacts else FallbackSampleContacts
    val seenPhones = mutableSetOf<String>()
    val result = mutableListOf<ScannedPhoneContact>()

    sourceList.forEachIndexed { index, (name, rawPhone) ->
        val norm = normalizeTrPhoneDigits(rawPhone)
        if (norm.length >= 7 && seenPhones.add(norm)) {
            val exists = norm in existingCustomerPhones
            val personal = isPersonalContactName(name)
            val landline = isLandlinePhone(rawPhone)
            result.add(
                ScannedPhoneContact(
                    id = "rc_${index}_$norm",
                    name = name,
                    phone = formatTrPhoneDisplay(rawPhone),
                    normalizedPhone = norm,
                    alreadyExists = exists,
                    isPersonal = personal,
                    isLandline = landline
                )
            )
        }
    }
    result
}

@Composable
fun CustomerAvatarBox(
    name: String,
    idOrKey: String,
    size: Dp,
    cornerRadius: Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    val bgColor = remember(idOrKey, name) { customerAvatarColor(idOrKey.ifBlank { name }) }
    val initials = remember(name) { customerInitials(name) }
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.3).sp
        )
    }
}

@Composable
fun CustomerSwitchToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tokens: AppointmentTokens
) {
    Box(
        modifier = Modifier
            .width(48.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (checked) tokens.green else tokens.surface2)
            .border(
                width = 1.dp,
                color = if (checked) Color.Transparent else tokens.border,
                shape = RoundedCornerShape(999.dp)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
fun CustomerToastHost(
    toastMessage: String?,
    tokens: AppointmentTokens,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !toastMessage.isNullOrBlank(),
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier
    ) {
        Surface(
            color = Color(0xF20F172A),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
        ) {
            Text(
                text = toastMessage.orEmpty(),
                color = Color.White,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
            )
        }
    }
}

@Composable
fun WaTemplateDrawerModal(
    customer: ResolvedCustomerProfile,
    tokens: AppointmentTokens,
    onDismiss: () -> Unit,
    onSendInApp: (templateTitle: String, text: String) -> Unit,
    onOpenRealWhatsApp: (templateTitle: String, text: String) -> Unit
) {
    var selectedTemplateId by remember { mutableStateOf(DefaultWaTemplates.first().id) }
    var customText by remember(customer.customer.id) {
        mutableStateOf(
            DefaultWaTemplates.first().body.replace("{ad}", customer.customer.name)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(tokens.surface)
                    .border(
                        width = 1.dp,
                        color = tokens.border,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .clickable(enabled = false) {}
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(tokens.greenC),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💬", fontSize = 19.sp)
                        }
                        Column {
                            Text(
                                text = "WhatsApp Şablonu Gönder",
                                color = tokens.text,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "${customer.customer.name} · ${customer.displayPhone}",
                                color = tokens.text2,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(tokens.surface2)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = tokens.text2, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DefaultWaTemplates.forEach { tpl ->
                        val isSelected = selectedTemplateId == tpl.id
                        val resolvedBody = tpl.body.replace("{ad}", customer.customer.name)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) tokens.primaryC else tokens.surface2)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) tokens.primary else tokens.border,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedTemplateId = tpl.id
                                    customText = resolvedBody
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = tpl.title,
                                color = if (isSelected) tokens.primary else tokens.text,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = resolvedBody,
                                color = tokens.text2,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "MESAJ METNİ (DÜZENLENEBİLİR)",
                        color = tokens.text2,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    BasicTextField(
                        value = customText,
                        onValueChange = { customText = it },
                        textStyle = TextStyle(
                            color = tokens.text,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp
                        ),
                        cursorBrush = SolidColor(tokens.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.surface2)
                            .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    )
                }

                val activeTitle = DefaultWaTemplates.find { it.id == selectedTemplateId }?.title ?: "Özel Mesaj"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(46.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(tokens.surface2)
                            .border(1.dp, tokens.border, RoundedCornerShape(13.dp))
                            .clickable {
                                onOpenRealWhatsApp(activeTitle, customText)
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📱 WhatsApp'ta Aç",
                            color = tokens.text,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(tokens.green)
                            .clickable {
                                onSendInApp(activeTitle, customText)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💬 Gönder",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArchiveConfirmModal(
    customerName: String,
    tokens: AppointmentTokens,
    onDismiss: () -> Unit,
    onConfirmArchive: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(tokens.surface)
                .border(1.dp, tokens.border, RoundedCornerShape(22.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(tokens.redC),
                contentAlignment = Alignment.Center
            ) {
                Text("🗄️", fontSize = 26.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Müşteriyi Arşivle",
                color = tokens.text,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "$customerName listeden gizlenecek. Geçmiş servis ve finans kayıtları korunur.",
                color = tokens.text2,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.surface2)
                        .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Vazgeç",
                        color = tokens.text,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.red)
                        .clickable(onClick = onConfirmArchive),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Arşivle",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditBoilerModal(
    initialBrand: String,
    initialModel: String,
    initialNotes: String,
    tokens: AppointmentTokens,
    onDismiss: () -> Unit,
    onSave: (brand: String, model: String, notes: String) -> Unit
) {
    var brand by remember { mutableStateOf(initialBrand) }
    var model by remember { mutableStateOf(initialModel) }
    var notes by remember { mutableStateOf(initialNotes) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(tokens.surface)
                .border(1.dp, tokens.border, RoundedCornerShape(22.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(tokens.primaryC),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔥", fontSize = 19.sp)
                    }
                    Column {
                        Text(
                            text = "Cihaz Kimlik Kartı",
                            color = tokens.text,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Kombi marka, model ve cihaz notu",
                            color = tokens.text2,
                            fontSize = 11.5.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(tokens.surface2)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = tokens.text2, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "POPÜLER MARKALAR",
                    color = tokens.text2,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PopularBoilerBrands.forEach { b ->
                        val active = brand.equals(b, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (active) tokens.primary else tokens.surface2)
                                .border(
                                    width = 1.dp,
                                    color = if (active) tokens.primary else tokens.border,
                                    shape = RoundedCornerShape(9.dp)
                                )
                                .clickable { brand = if (active) "" else b }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = b,
                                color = if (active) Color.White else tokens.text,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            CustomerLabeledInput(
                label = "KOMBİ MARKASI",
                value = brand,
                onValueChange = { brand = it },
                placeholder = "Örn: E.C.A., Demirdöküm, Vaillant...",
                tokens = tokens
            )

            CustomerLabeledInput(
                label = "MODEL",
                value = model,
                onValueChange = { model = it },
                placeholder = "Örn: Proteus Premix 24 kW",
                tokens = tokens
            )

            CustomerLabeledInput(
                label = "CİHAZ NOTU",
                value = notes,
                onValueChange = { notes = it },
                placeholder = "Garanti durumu, kronik arıza veya montaj yılı...",
                tokens = tokens,
                singleLine = false,
                minHeight = 68.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.surface2)
                        .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Vazgeç", color = tokens.text2, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.primaryGradient)
                        .clickable { onSave(brand.trim(), model.trim(), notes.trim()) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓ Cihazı Kaydet", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun CustomerQuickAppointmentModal(
    customer: ResolvedCustomerProfile,
    tokens: AppointmentTokens,
    onDismiss: () -> Unit,
    onCreateAppointment: (Appointment) -> Unit
) {
    val todayStr = remember {
        SimpleDateFormat("dd.MM.yyyy", TrLocale).format(Date())
    }
    val tomorrowStr = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        SimpleDateFormat("dd.MM.yyyy", TrLocale).format(cal.time)
    }

    var date by remember { mutableStateOf(todayStr) }
    var timeSlot by remember { mutableStateOf("13:00 - 15:00") }
    var serviceType by remember { mutableStateOf("Kombi Bakım & Servis") }
    var problemNote by remember {
        mutableStateOf(
            if (customer.brand.isNotBlank()) "${customer.brand} ${customer.model}".trim() else ""
        )
    }

    val slots = listOf("09:00 - 11:00", "11:00 - 13:00", "13:00 - 15:00", "15:00 - 17:00", "17:00 - 19:00")
    val types = listOf("Kombi Bakım & Servis", "Arıza Tespit & Onarım", "Petek Temizliği", "Montaj / Değişim")

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(tokens.surface)
                .border(1.dp, tokens.border, RoundedCornerShape(22.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📅 Hızlı Randevu Oluştur",
                        color = tokens.text,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${customer.customer.name} · ${customer.district}",
                        color = tokens.text2,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(tokens.surface2)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = tokens.text2, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Bugün ($todayStr)" to todayStr, "Yarın ($tomorrowStr)" to tomorrowStr).forEach { (lbl, dVal) ->
                    val active = date == dVal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) tokens.primary else tokens.surface2)
                            .border(1.dp, if (active) tokens.primary else tokens.border, RoundedCornerShape(10.dp))
                            .clickable { date = dVal },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = lbl,
                            color = if (active) Color.White else tokens.text,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("SAAT ARALIĞI", color = tokens.text2, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    slots.forEach { slot ->
                        val active = timeSlot == slot
                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (active) tokens.primaryC else tokens.surface2)
                                .border(1.dp, if (active) tokens.primary else tokens.border, RoundedCornerShape(9.dp))
                                .clickable { timeSlot = slot }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = slot,
                                color = if (active) tokens.primary else tokens.text2,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("İŞLEM TÜRÜ", color = tokens.text2, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    types.forEach { t ->
                        val active = serviceType == t
                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (active) tokens.primary else tokens.surface2)
                                .border(1.dp, if (active) tokens.primary else tokens.border, RoundedCornerShape(9.dp))
                                .clickable { serviceType = t }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = t,
                                color = if (active) Color.White else tokens.text,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            CustomerLabeledInput(
                label = "ARIZA / SERVİS NOTU",
                value = problemNote,
                onValueChange = { problemNote = it },
                placeholder = "Müşteri talebi veya arıza detayı...",
                tokens = tokens,
                singleLine = false,
                minHeight = 64.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.surface2)
                        .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Vazgeç", color = tokens.text2, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.primaryGradient)
                        .clickable {
                            val appt = Appointment(
                                id = "a_${UUID.randomUUID().toString().take(8)}",
                                customerId = customer.customer.id,
                                customerName = customer.customer.name,
                                phone = customer.displayPhone,
                                district = customer.district,
                                neighborhood = customer.address.substringBefore(",").trim(),
                                streetDoorNo = customer.gateNote.ifBlank { customer.address },
                                date = date,
                                timeSlot = timeSlot,
                                serviceType = serviceType,
                                status = AppointmentStatus.ONAYLANDI,
                                addressDetail = buildString {
                                    append(customer.address)
                                    if (customer.gateNote.isNotBlank()) {
                                        append(" (${customer.gateNote})")
                                    }
                                },
                                problemNote = problemNote.ifBlank { serviceType }
                            )
                            onCreateAppointment(appt)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("📅 Randevuyu Kaydet", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun CustomerLabeledInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    tokens: AppointmentTokens,
    modifier: Modifier = Modifier,
    isMonospace: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minHeight: Dp = 42.dp
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            color = tokens.text2,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.3.sp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(minHeight)
                .clip(RoundedCornerShape(11.dp))
                .background(tokens.surface2)
                .border(1.dp, tokens.border, RoundedCornerShape(11.dp))
                .padding(horizontal = 12.dp, vertical = if (singleLine) 0.dp else 10.dp),
            contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
        ) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(
                    text = placeholder,
                    color = tokens.text2.copy(alpha = 0.65f),
                    fontSize = 13.5.sp,
                    fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
                    maxLines = if (singleLine) 1 else 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = TextStyle(
                    color = tokens.text,
                    fontSize = 13.5.sp,
                    fontWeight = if (isMonospace) FontWeight.Bold else FontWeight.SemiBold,
                    fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
                ),
                cursorBrush = SolidColor(tokens.primary),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
