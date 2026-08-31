package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WhatsAppStatus(
    val isConnected: Boolean = false,
    val phoneNumber: String? = null,
    val businessName: String? = null,
    val wabaAccountId: String? = null,
    val connectedAt: String? = null,
    val qualityRating: String? = null,
    val messagingLimit: String? = null,
    val displayPhoneNumberStatus: String? = null,
    val webhookStatus: String? = null
)
