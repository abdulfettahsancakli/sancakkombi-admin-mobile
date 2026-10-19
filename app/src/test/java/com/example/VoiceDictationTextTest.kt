package com.example

import com.example.ui.components.appendDictationText
import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceDictationTextTest {
    @Test
    fun `first transcript becomes field value`() {
        assertEquals("Petekler ısınmıyor", appendDictationText("", " Petekler ısınmıyor "))
    }

    @Test
    fun `transcript is appended without deleting typed note`() {
        assertEquals(
            "Pompa kontrol edildi. Filtre temizlendi",
            appendDictationText("Pompa kontrol edildi", "Filtre temizlendi")
        )
    }

    @Test
    fun `existing punctuation is preserved`() {
        assertEquals(
            "Brülör temizlendi. Gaz ayarı yapıldı",
            appendDictationText("Brülör temizlendi.", "Gaz ayarı yapıldı")
        )
    }
}
