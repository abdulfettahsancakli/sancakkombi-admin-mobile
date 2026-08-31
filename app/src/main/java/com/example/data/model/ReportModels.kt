package com.example.data.model

import com.squareup.moshi.JsonClass

enum class ReportTimeRange(val label: String) {
    WEEK("Bu Hafta"),
    MONTH("Bu Ay"),
    ALL_TIME("Tüm Zamanlar")
}

@JsonClass(generateAdapter = true)
data class AppointmentReportData(
    val totalAppointments: Int = 0,
    val pendingCount: Int = 0,
    val approvedCount: Int = 0,
    val completedCount: Int = 0,
    val cancelledCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class FinanceReportData(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netProfit: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class PopularServiceItem(
    val serviceName: String,
    val count: Int,
    val percentage: Int,
    val colorHex: Long
)

@JsonClass(generateAdapter = true)
data class TrendBarData(
    val dayOrPeriod: String,
    val count: Int
)

@JsonClass(generateAdapter = true)
data class ReportData(
    val timeRange: ReportTimeRange = ReportTimeRange.WEEK,
    val appointments: AppointmentReportData = AppointmentReportData(),
    val finance: FinanceReportData = FinanceReportData(),
    val popularServices: List<PopularServiceItem> = emptyList(),
    val appointmentTrends: List<TrendBarData> = emptyList()
)
