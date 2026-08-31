package com.example.data.model

import com.squareup.moshi.JsonClass

enum class ProposalStatus(val label: String) {
    DRAFT("Taslak"),
    PENDING("Beklemede"),
    APPROVED("Kabul Edildi"),
    REJECTED("Reddedildi")
}

@JsonClass(generateAdapter = true)
data class ProposalItem(
    val id: String,
    val title: String,
    val quantity: Int = 1,
    val unitPrice: Double = 0.0
) {
    val totalPrice: Double
        get() = quantity * unitPrice
}

@JsonClass(generateAdapter = true)
data class Proposal(
    val id: String, // e.g. TF-202608-024EFE
    val quoteNumber: String = "", // ör. "TF-202608-024EFE" - id artık gerçek DB id'si, kullanıcı dostu kod burada
    val customerName: String,
    val customerPhone: String = "",
    val customerEmail: String = "",
    val customerDistrict: String = "",
    val customerAddress: String = "",
    val deviceBrand: String = "",
    val deviceModel: String = "",
    val date: String,
    val validUntilDate: String = "",
    val preparedBy: String = "",
    val note: String = "",
    val status: ProposalStatus = ProposalStatus.PENDING,
    val items: List<ProposalItem> = emptyList(),
    val downPayment: Double = 0.0,
    val remainingPaymentType: String = "",
    val discount: Double = 0.0
) {
    val subtotal: Double
        get() = items.sumOf { it.totalPrice }

    val grandTotal: Double
        get() = (subtotal - discount).coerceAtLeast(0.0)

    val remainingAmount: Double
        get() = (grandTotal - downPayment).coerceAtLeast(0.0)
}
