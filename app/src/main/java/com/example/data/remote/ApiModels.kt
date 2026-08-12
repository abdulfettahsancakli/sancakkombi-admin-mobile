package com.example.data.remote

import com.example.data.model.FinanceRecord
import com.example.data.model.MaintenanceRule
import com.example.data.model.Proposal
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    val password: String
)

@JsonClass(generateAdapter = true)
data class LoginResponseDto(
    val token: String? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class DashboardStatsDto(
    val bugunkuRandevu: Int = 0,
    val bekleyenOnay: Int = 0,
    val buHaftaTamamlanan: Int = 0,
    val acikAlacak: String = "",
    val buAyServis: Int = 0,
    val buAyGelir: String = ""
)

@JsonClass(generateAdapter = true)
data class SuccessResponseDto(
    val success: Boolean = false,
    val warning: String? = null,
    val error: String? = null,
    val status: String? = null,
    val active: Boolean? = null,
    val channel: String? = null
)

@JsonClass(generateAdapter = true)
data class SendBankTransferRequestDto(
    val paymentAccountKey: String,
    val amount: Double? = null,
    val promisedPaymentDate: String? = null
)

@JsonClass(generateAdapter = true)
data class StatusUpdateRequestDto(
    val status: String,
    val cancellationReason: String? = null
)

@JsonClass(generateAdapter = true)
data class CompleteJobRequestDto(
    val jobReport: com.example.data.model.JobReport
)

@JsonClass(generateAdapter = true)
data class FinanceRecordCreateResponseDto(
    val success: Boolean = false,
    val error: String? = null,
    val data: FinanceRecord? = null
)

@JsonClass(generateAdapter = true)
data class ProposalCreateResponseDto(
    val success: Boolean = false,
    val error: String? = null,
    val data: Proposal? = null
)

@JsonClass(generateAdapter = true)
data class MaintenanceRuleCreateResponseDto(
    val success: Boolean = false,
    val error: String? = null,
    val data: MaintenanceRule? = null
)

@JsonClass(generateAdapter = true)
data class UploadResponseDto(
    val url: String? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class ReceiptDetailDto(
    val entryId: String = "",
    val receiptNo: String = "",
    val date: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "",
    val status: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val customerDistrict: String = "",
    val deviceBrand: String = "",
    val deviceModel: String = "",
    val deviceTested: Boolean = false,
    val workDescription: String = "",
    val warrantyMonths: Int? = null,
    val serviceTitle: String = ""
)

@JsonClass(generateAdapter = true)
data class DeviceHistoryPartDto(
    val name: String = "",
    val quantity: Int = 0,
    val unitPrice: Double? = null
)

@JsonClass(generateAdapter = true)
data class DeviceHistoryRecordDto(
    val appointmentId: String = "",
    val date: String = "",
    val serviceTitle: String = "",
    val deviceBrand: String = "",
    val deviceModel: String = "",
    val workDescription: String = "",
    val parts: List<DeviceHistoryPartDto> = emptyList(),
    val warrantyMonths: Int? = null,
    val warrantyUntil: String? = null,
    val isUnderWarranty: Boolean = false
)

@JsonClass(generateAdapter = true)
data class DeviceHistoryDto(
    val deviceBrand: String = "",
    val deviceModel: String = "",
    val deviceNotes: String = "",
    val records: List<DeviceHistoryRecordDto> = emptyList()
)
