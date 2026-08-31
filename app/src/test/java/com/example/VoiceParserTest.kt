package com.example

import com.example.data.remote.GeminiVoiceAppointmentParser
import com.example.data.remote.VoiceParseResponseDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceParserTest {
    @Test
    fun localParserRecognizesTurkishDistrictAndService() = runBlocking {
        val result = GeminiVoiceAppointmentParser.parseVoiceText(
            "Ay\u015fe Y\u0131lmaz i\u00e7in Bayrampa\u015fa'da 0532 123 45 67 numaral\u0131 petek temizli\u011fi randevusu bug\u00fcn sabah"
        )

        assertEquals("Bayrampa\u015fa", result.district)
        assertEquals("Petek Temizli\u011fi", result.serviceType)
        assertEquals("09:00 - 11:00", result.timeSlot)
        assertTrue(result.phone.endsWith("1234567"))
        assertTrue(result.date.isNotBlank())
    }

    @Test
    fun missingVoiceFieldsStayEmpty() = runBlocking {
        val result = GeminiVoiceAppointmentParser.parseVoiceText("randevu olu\u015ftur")

        assertEquals("", result.district)
        assertEquals("", result.timeSlot)
        assertEquals("", result.serviceType)
        assertTrue(result.missingFields.contains("\u0130l\u00e7e"))
        assertTrue(result.missingFields.contains("Saat Aral\u0131\u011f\u0131"))
        assertTrue(result.missingFields.contains("Hizmet T\u00fcr\u00fc"))
    }

    @Test
    fun networkDtoHasNoInventedAppointmentDefaults() {
        val result = VoiceParseResponseDto()

        assertEquals("", result.district)
        assertEquals("", result.date)
        assertEquals("", result.timeSlot)
        assertEquals("", result.serviceType)
    }
}
