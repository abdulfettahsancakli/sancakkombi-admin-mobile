package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MaintenanceStats(
    val activeRulesCount: Int = 3,
    val within30DaysCount: Int = 0,
    val overdueCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class MaintenanceRule(
    val id: String,
    val customerName: String,
    val serviceType: String,
    val status: String = "Aktif", // "Aktif" or "Pasif"
    val nextReminderDate: String, // e.g. "13.04.2027"
    val intervalMonths: Int = 12,
    val channel: String = "WhatsApp"
)
