package com.example.utils

data class ReceiptPaymentBreakdown(
    val collectedAmount: Double,
    val remainingAmount: Double,
    val errorMessage: String? = null
) {
    val isValid: Boolean get() = errorMessage == null
}

fun calculateReceiptPayment(
    totalAmount: Double,
    paymentStatus: String,
    partialAmountText: String
): ReceiptPaymentBreakdown {
    val safeTotal = totalAmount.coerceAtLeast(0.0)

    return when (paymentStatus) {
        "paid" -> ReceiptPaymentBreakdown(
            collectedAmount = safeTotal,
            remainingAmount = 0.0
        )

        "unpaid" -> ReceiptPaymentBreakdown(
            collectedAmount = 0.0,
            remainingAmount = safeTotal
        )

        "partial" -> {
            val compactAmount = partialAmountText
                .trim()
                .replace("\u00A0", "")
                .replace(" ", "")
                .replace("₺", "")
            val collected = if (compactAmount.matches(Regex("[+-]?\\d{1,3}(\\.\\d{3})+"))) {
                parseLocalizedDouble(compactAmount.replace(".", ""))
            } else {
                parseLocalizedDouble(compactAmount)
            }
            val error = when {
                safeTotal <= 0.0 -> "Kısmi tahsilat için iş toplamı 0 ₺'den büyük olmalı."
                collected == null || collected <= 0.0 -> "Tahsil edilen tutarı 0 ₺'den büyük girin."
                collected >= safeTotal -> "Kısmi tahsilat toplamdan düşük olmalı. Tamamı alındıysa 'Tahsil Edildi'yi seçin."
                else -> null
            }
            val safeCollected = collected?.coerceAtLeast(0.0) ?: 0.0
            ReceiptPaymentBreakdown(
                collectedAmount = safeCollected,
                remainingAmount = (safeTotal - safeCollected).coerceAtLeast(0.0),
                errorMessage = error
            )
        }

        else -> ReceiptPaymentBreakdown(
            collectedAmount = 0.0,
            remainingAmount = safeTotal,
            errorMessage = "Geçerli bir tahsilat durumu seçin."
        )
    }
}
