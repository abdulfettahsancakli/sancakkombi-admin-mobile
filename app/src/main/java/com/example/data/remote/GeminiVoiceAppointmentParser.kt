package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val turkishLocale = Locale.forLanguageTag("tr-TR")

data class ParsedVoiceAppointment(
    val customerName: String = "",
    val phone: String = "",
    val district: String = "",
    val neighborhood: String = "",
    val streetDoorNo: String = "",
    val date: String = "",
    val timeSlot: String = "",
    val serviceType: String = "",
    val problemNote: String = "",
    val missingFields: List<String> = emptyList(),
    val aiSummaryMessage: String = ""
)

/**
 * Voice parsing goes through the authenticated web API. The Gemini key is
 * deliberately absent from the Android build, because anything in an APK
 * can be extracted. The local parser is only a no-secret offline fallback.
 */
object GeminiVoiceAppointmentParser {
    private const val MAX_INPUT_LENGTH = 4000

    suspend fun parseVoiceText(
        voiceText: String,
        api: AdminApiService,
        authHeader: String
    ): ParsedVoiceAppointment = withContext(Dispatchers.IO) {
        val text = voiceText.trim().take(MAX_INPUT_LENGTH)
        if (text.isBlank()) return@withContext fallbackLocalParser("")

        try {
            val response = api.parseVoiceAppointment(authHeader, VoiceParseRequestDto(text))
            if (response.isSuccessful) {
                response.body()?.toDomain()?.let { return@withContext it }
            }
        } catch (_: Exception) {
            // Offline or temporarily unavailable: continue with the safe local parser.
        }

        fallbackLocalParser(text)
    }

    /** Used by callers that do not have a repository/API instance yet. */
    suspend fun parseVoiceText(voiceText: String): ParsedVoiceAppointment = withContext(Dispatchers.Default) {
        fallbackLocalParser(voiceText.trim().take(MAX_INPUT_LENGTH))
    }

    private fun VoiceParseResponseDto.toDomain() = ParsedVoiceAppointment(
        customerName = customerName,
        phone = phone,
        district = if (missingFields.any { it.equals("\u0130l\u00e7e", ignoreCase = true) }) "" else district,
        neighborhood = neighborhood,
        streetDoorNo = streetDoorNo,
        date = if (missingFields.any { it.equals("Tarih", ignoreCase = true) }) "" else date,
        timeSlot = if (missingFields.any { it.equals("Saat Aral\u0131\u011f\u0131", ignoreCase = true) }) "" else timeSlot,
        serviceType = if (missingFields.any { it.equals("Hizmet T\u00fcr\u00fc", ignoreCase = true) }) "" else serviceType,
        problemNote = problemNote,
        missingFields = missingFields,
        aiSummaryMessage = aiSummaryMessage
    )

    private fun fallbackLocalParser(text: String): ParsedVoiceAppointment {
        val lower = text.lowercase(turkishLocale)
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", turkishLocale)
        var dateStr = ""
        var hasDateSignal = false

        fun setDate() {
            dateStr = dateFormat.format(cal.time)
            hasDateSignal = true
        }

        when {
            lower.contains("bug\u00fcn") -> setDate()
            lower.contains("yar\u0131n") -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                setDate()
            }
            lower.contains("pazartesi") -> {
                while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) cal.add(Calendar.DAY_OF_YEAR, 1)
                setDate()
            }
            lower.contains("sal\u0131") -> {
                while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.TUESDAY) cal.add(Calendar.DAY_OF_YEAR, 1)
                setDate()
            }
            lower.contains("\u00e7ar\u015famba") -> {
                while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) cal.add(Calendar.DAY_OF_YEAR, 1)
                setDate()
            }
            lower.contains("per\u015fembe") -> {
                while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.THURSDAY) cal.add(Calendar.DAY_OF_YEAR, 1)
                setDate()
            }
            lower.contains("cuma") -> {
                while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) cal.add(Calendar.DAY_OF_YEAR, 1)
                setDate()
            }
        }

        val districts = listOf("Bayrampa\u015fa", "Esenler", "Gaziosmanpa\u015fa", "Zeytinburnu", "Fatih", "Ey\u00fcp\u015fsultan")
        val matchedDistrict = districts.firstOrNull { lower.contains(it.lowercase(turkishLocale)) } ?: ""

        val phoneRegex = Regex("""0?5\d{2}\s?\d{3}\s?\d{2}\s?\d{2}|\d{10,11}""")
        val phoneMatch = phoneRegex.find(text)?.value?.replace("\\s".toRegex(), "") ?: ""

        val serviceType = when {
            lower.contains("petek") -> "Petek Temizli\u011fi"
            lower.contains("ar\u0131za") || lower.contains("\u00e7al\u0131\u015fm\u0131yor") || lower.contains("su s\u0131zd\u0131r\u0131yor") -> "Ar\u0131za Onar\u0131m"
            lower.contains("gaz ka\u00e7a\u011f\u0131") -> "Gaz Ka\u00e7a\u011f\u0131 Tespiti"
            lower.contains("kombi") || lower.contains("bak\u0131m") || lower.contains("servis") -> "Kombi Bak\u0131m & Servis"
            else -> ""
        }

        val timeSlot = when {
            lower.contains("sabah") || lower.contains("09:") || lower.contains("10:") -> "09:00 - 11:00"
            lower.contains("\u00f6\u011flen") || lower.contains("11:") || lower.contains("12:") -> "11:00 - 13:00"
            lower.contains("\u00f6\u011fleden sonra") || lower.contains("14:") || lower.contains("15:") -> "13:00 - 15:00"
            lower.contains("ak\u015fam\u00fcst\u00fc") || lower.contains("16:") || lower.contains("17:") -> "15:00 - 17:00"
            else -> ""
        }

        val nameWords = text.split(Regex("\\s+"))
            .map { it.trim(',', '.', ':', ';') }
            .filter { word ->
                val wordLower = word.lowercase(turkishLocale)
                word.isNotBlank() &&
                    !wordLower.contains("randevu") && !wordLower.contains("ekle") && !wordLower.contains("kombi") &&
                    !wordLower.contains("bak\u0131m") && !wordLower.contains("bug\u00fcn") && !wordLower.contains("yar\u0131n") &&
                    !wordLower.contains("saat") && !districts.any { district -> wordLower.contains(district.lowercase(turkishLocale)) } &&
                    !wordLower.contains("telefon") && !wordLower.contains("05")
            }
        val name = when {
            nameWords.size >= 2 -> listOf(nameWords[0].capitalizeTr(), nameWords[1].capitalizeTr()).joinToString(" ")
            nameWords.size == 1 -> nameWords[0].capitalizeTr()
            else -> ""
        }

        val missingList = mutableListOf<String>()
        if (name.isBlank()) missingList.add("M\u00fc\u015fteri Ad\u0131")
        if (phoneMatch.isBlank()) missingList.add("Telefon Numaras\u0131")
        if (matchedDistrict.isBlank()) missingList.add("\u0130l\u00e7e")
        if (!hasDateSignal) missingList.add("Tarih")
        if (timeSlot.isBlank()) missingList.add("Saat Aral\u0131\u011f\u0131")
        if (serviceType.isBlank()) missingList.add("Hizmet T\u00fcr\u00fc")

        val summary = if (missingList.isEmpty()) {
            "T\u00fcm bilgiler alg\u0131land\u0131 ve forma aktar\u0131ld\u0131."
        } else {
            "Alg\u0131lanan bilgiler dolduruldu. Eksik kalanlar: " + missingList.joinToString(", ") + "."
        }

        return ParsedVoiceAppointment(
            customerName = name,
            phone = phoneMatch,
            district = matchedDistrict,
            date = dateStr,
            timeSlot = timeSlot,
            serviceType = serviceType,
            problemNote = text,
            missingFields = missingList,
            aiSummaryMessage = summary
        )
    }

    private fun String.capitalizeTr(): String = replaceFirstChar { first ->
        if (first.isLowerCase()) first.titlecase(turkishLocale) else first.toString()
    }
}
