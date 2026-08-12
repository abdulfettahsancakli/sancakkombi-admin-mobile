package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class AppointmentReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appointmentId = intent.getStringExtra(EXTRA_APPOINTMENT_ID) ?: "0"
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Randevu Hatırlatması"
        val customerName = intent.getStringExtra(EXTRA_CUSTOMER_NAME) ?: ""
        val address = intent.getStringExtra(EXTRA_ADDRESS) ?: ""

        showNotification(context, appointmentId, title, customerName, address)
    }

    private fun showNotification(
        context: Context,
        appointmentId: String,
        title: String,
        customerName: String,
        address: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "appointment_reminders_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Randevu Hatırlatıcıları",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Yaklaşan teknik servis randevuları için bildirimler"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            appointmentId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (customerName.isNotBlank()) {
            "Müşteri: $customerName | $address"
        } else {
            "Yaklaşan randevunuzun zamanı geldi."
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ $title")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("⏰ $title\n$contentText"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(appointmentId.hashCode(), notification)
    }

    companion object {
        const val EXTRA_APPOINTMENT_ID = "extra_appointment_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_CUSTOMER_NAME = "extra_customer_name"
        const val EXTRA_ADDRESS = "extra_address"
    }
}
