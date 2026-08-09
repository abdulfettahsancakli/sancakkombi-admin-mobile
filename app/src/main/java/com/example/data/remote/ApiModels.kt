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
