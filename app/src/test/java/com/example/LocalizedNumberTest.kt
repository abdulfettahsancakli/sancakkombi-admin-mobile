package com.example

import com.example.utils.parseLocalizedDouble
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocalizedNumberTest {
    @Test
    fun parsesTurkishMoneyFormat() {
        assertEquals(1200.50, parseLocalizedDouble("1.200,50")!!, 0.001)
        assertEquals(1200.50, parseLocalizedDouble("1,200.50")!!, 0.001)
        assertEquals(1200.50, parseLocalizedDouble("1200,50")!!, 0.001)
    }

    @Test
    fun preservesPlainDecimalAndRejectsInvalidInput() {
        assertEquals(1200.50, parseLocalizedDouble("1200.50")!!, 0.001)
        assertNull(parseLocalizedDouble("not-a-number"))
    }
}
