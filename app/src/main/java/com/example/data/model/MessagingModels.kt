package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessagingStats(
    val gonderildiCount: Int = 0,
    val bekleyenCount: Int = 0,
    val basarisizCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class CustomerMessagingSettings(
    val isNotificationsActive: Boolean = true,
    val selectedChannel: String = "WhatsApp", // "SMS" or "WhatsApp"
    val autoSendOnAppointmentCreated: Boolean = true,
    val autoSendOnAppointmentUpdated: Boolean = false,
    val reminderHoursBefore: Int = 2,
    val autoSendFeedbackAfterAppointment: Boolean = false,
    val feedbackDaysAfter: Int = 2
)

@JsonClass(generateAdapter = true)
data class StaffMessagingSettings(
    val isNotificationsActive: Boolean = true,
    val selectedChannel: String = "WhatsApp", // "WhatsApp" or "SMS"
    val autoSendDailyAppointmentSummary: Boolean = true,
    val dailySummaryTime: String = "12:15",
    val autoSendAppointmentReminder: Boolean = true
)

@JsonClass(generateAdapter = true)
data class MessageTemplate(
    val id: String,
    val title: String,
    val tag: String,
    val category: String, // "MUSTERI" or "USTA"
    val templateText: String,
    val buttons: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MessageJob(
    val id: String,
    val template: String,
    val channel: String,
    val time: String,
    val status: String, // "PENDING", "FAILED", "SUCCESS"
    val error: String? = null,
    val recipient: String,
    val isRetrying: Boolean = false
)

@JsonClass(generateAdapter = true)
data class MessageLog(
    val id: String,
    val time: String,
    val channel: String,
    val template: String,
    val recipient: String,
    val status: String, // "sent", "skipped", "failed"
    val provider: String // "meta_whatsapp_cloud", "verimor_sms"
)
