package com.example

import com.example.utils.calculateReceiptPayment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptPaymentTest {
    @Test
    fun partialPaymentKeepsCollectedAndRemainingAmountsSeparate() {
        val result = calculateReceiptPayment(10_000.0, "partial", "3.000")

        assertTrue(result.isValid)
        assertEquals(3_000.0, result.collectedAmount, 0.001)
        assertEquals(7_000.0, result.remainingAmount, 0.001)
    }

    @Test
    fun partialPaymentRequiresAmountBelowTotal() {
        assertFalse(calculateReceiptPayment(10_000.0, "partial", "").isValid)
        assertFalse(calculateReceiptPayment(10_000.0, "partial", "0").isValid)
        assertFalse(calculateReceiptPayment(10_000.0, "partial", "10.000").isValid)
    }

    @Test
    fun paidAndUnpaidStatusesDeriveAmountsFromTotal() {
        val paid = calculateReceiptPayment(10_000.0, "paid", "3.000")
        val unpaid = calculateReceiptPayment(10_000.0, "unpaid", "3.000")

        assertEquals(10_000.0, paid.collectedAmount, 0.001)
        assertEquals(0.0, paid.remainingAmount, 0.001)
        assertEquals(0.0, unpaid.collectedAmount, 0.001)
        assertEquals(10_000.0, unpaid.remainingAmount, 0.001)
    }
}
