package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FinanceRecord(
    val id: String,
    val date: String,
    val type: FinanceType, // GELIR or GIDER
    val amount: Double,
    val status: String, // "Ödendi", "Kısmi", "Bekliyor"
    val source: String, // Customer name or supplier/category
    val totalAmount: Double = amount,
    val collectedAmount: Double = amount,
    val note: String = "",
    val receiptNo: String = "SK-202606-6A6F7A"
)

enum class FinanceType(val label: String) {
    GELIR("Gelir"),
    GIDER("Gider")
}

@JsonClass(generateAdapter = true)
data class BankAccount(
    val id: String,
    val cardTitle: String,
    val accountHolder: String,
    val bankName: String,
    val iban: String,
    val isReady: Boolean = true
)

@JsonClass(generateAdapter = true)
data class FinanceSummary(
    val totalIncome: Double = 8870.00,
    val totalExpense: Double = 500.00,
    val outstandingReceivable: Double = 500.00
)
