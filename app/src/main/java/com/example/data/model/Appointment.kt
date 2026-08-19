package com.example.data.model

import com.squareup.moshi.JsonClass

enum class AppointmentStatus(val label: String) {
    BEKLIYOR("Bekliyor"),
    ONAYLANDI("Onaylandı"),
    TAMAMLANDI("Tamamlandı"),
    IPTAL("İptal")
}

@JsonClass(generateAdapter = true)
data class UsedPart(
    val id: String,
    val name: String,
    val quantity: Int = 1,
    val price: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class JobReport(
    val technicianName: String = "",
    val notifyCustomerMessage: Boolean = true,
    val sendWhatsappPdf: Boolean = true,
    val addRevenueRecord: Boolean = true,
    val collectedAmount: String = "",
    val paymentStatus: String = "Ödendi", // Ödendi, Bekliyor
    val paymentMethod: String = "Nakit", // Nakit, Kredi Kartı, EFT/Havale
    val revenueNote: String = "",
    val addJobReport: Boolean = true,
    val deviceBrand: String = "",
    val deviceModel: String = "",
    val workDoneNote: String = "",
    val warrantyMonths: String = "",
    val usedParts: List<UsedPart> = emptyList(),
    val serviceFee: String = "0",
    val otherFee: String = "0",
    val deviceTested: Boolean = false,
    val createExpenseRecord: Boolean = false,
    val photoUris: List<String> = emptyList(),
    val customerSignaturePath: String? = null,
    val technicianSignaturePath: String? = null
)

@JsonClass(generateAdapter = true)
data class Appointment(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val phone: String = "",
    val email: String = "",
    val district: String = "Bayrampaşa",
    val neighborhood: String = "",
    val streetDoorNo: String = "",
    val date: String = "", // e.g. "03.08.2026"
    val timeSlot: String = "", // e.g. "13:00 - 15:00"
    val serviceType: String = "Kombi Bakım & Servis", // e.g. "Kombi Bakım & Servis"
    val status: AppointmentStatus = AppointmentStatus.ONAYLANDI,
    val addressDetail: String = "",
    val problemNote: String = "",
    val jobReport: JobReport? = null
)
