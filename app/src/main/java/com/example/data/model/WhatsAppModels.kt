package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WhatsAppStatus(
    val isConnected: Boolean = true,
    val phoneNumber: String? = "+90 532 123 45 67",
    val businessName: String? = "Sancak Kombi Servisi",
    val wabaAccountId: String? = "WABA_28940182740",
    val connectedAt: String? = "12 Ocak 2025 - 14:30",
    val qualityRating: String? = "Yüksek (Yeşil)",
    val messagingLimit: String? = "1.000 Müşteri / 24 Saat",
    val displayPhoneNumberStatus: String? = "Onaylandı (APPROVED)",
    val webhookStatus: String? = "Etkin (Webhooks Active)"
)
