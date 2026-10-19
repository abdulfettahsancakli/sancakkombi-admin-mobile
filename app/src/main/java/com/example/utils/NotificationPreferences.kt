package com.example.utils

import android.content.Context

data class NotificationPreferences(
    val appointmentReminders: Boolean = true,
    val dailySummary: Boolean = true,
    val proposalAlerts: Boolean = true,
    val financeAlerts: Boolean = true,
    val reminderMinutes: Int = 30
)

object NotificationPreferenceStore {
    private const val FILE_NAME = "notification_preferences"
    private const val KEY_APPOINTMENTS = "appointment_reminders"
    private const val KEY_DAILY = "daily_summary"
    private const val KEY_PROPOSALS = "proposal_alerts"
    private const val KEY_FINANCE = "finance_alerts"
    private const val KEY_MINUTES = "reminder_minutes"

    fun load(context: Context): NotificationPreferences {
        val prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        return NotificationPreferences(
            appointmentReminders = prefs.getBoolean(KEY_APPOINTMENTS, true),
            dailySummary = prefs.getBoolean(KEY_DAILY, true),
            proposalAlerts = prefs.getBoolean(KEY_PROPOSALS, true),
            financeAlerts = prefs.getBoolean(KEY_FINANCE, true),
            reminderMinutes = prefs.getInt(KEY_MINUTES, 30).takeIf { it in setOf(15, 30, 60) } ?: 30
        )
    }

    fun save(context: Context, value: NotificationPreferences) {
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_APPOINTMENTS, value.appointmentReminders)
            .putBoolean(KEY_DAILY, value.dailySummary)
            .putBoolean(KEY_PROPOSALS, value.proposalAlerts)
            .putBoolean(KEY_FINANCE, value.financeAlerts)
            .putInt(KEY_MINUTES, value.reminderMinutes)
            .apply()
    }
}
