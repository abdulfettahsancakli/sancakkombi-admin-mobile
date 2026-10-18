package com.example.ui.screens

import androidx.compose.runtime.staticCompositionLocalOf

data class CloudWhatsAppActions(
    val sendMessage: (
        phone: String,
        message: String,
        customerId: String?,
        appointmentId: String?,
        onResult: (Result<Unit>) -> Unit
    ) -> Unit,
    val sendProposal: (proposalId: String, onResult: (Result<Unit>) -> Unit) -> Unit,
    val sendFinanceReminder: (recordId: String, message: String, onResult: (Result<Unit>) -> Unit) -> Unit
)

val LocalCloudWhatsAppActions = staticCompositionLocalOf<CloudWhatsAppActions?> { null }
