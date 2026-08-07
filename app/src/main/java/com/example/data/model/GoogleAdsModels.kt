package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GoogleAdsStats(
    val totalSpend: Double = 4250.00,
    val totalClicks: Int = 1840,
    val totalConversions: Int = 126,
    val avgCpa: Double = 33.73,
    val conversionRate: Double = 6.85
)

@JsonClass(generateAdapter = true)
data class GoogleAdsCampaign(
    val id: String,
    val name: String,
    val status: String, // "ACTIVE" or "PAUSED"
    val dailyBudget: Double,
    val spend: Double,
    val clicks: Int,
    val conversions: Int,
    val cpa: Double
)
