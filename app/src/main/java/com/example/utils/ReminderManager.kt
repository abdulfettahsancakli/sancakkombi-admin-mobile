package com.example.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.Appointment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ReminderManager {

    private const val CHANNEL_ID = "appointment_reminders_channel"
    private const val CHANNEL_NAME = "Randevu Hatırlatıcıları"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Yaklaşan servis randevuları için zamanlanmış bildirimler"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleAppointmentReminder(
        context: Context,
        appointment: Appointment,
        minutesBefore: Int = 30
    ) {
        createNotificationChannel(context)

        val triggerTimeMillis = parseAppointmentTime(appointment.date, appointment.timeSlot, minutesBefore)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AppointmentReminderReceiver::class.java).apply {
            putExtra(AppointmentReminderReceiver.EXTRA_APPOINTMENT_ID, appointment.id)
            putExtra(AppointmentReminderReceiver.EXTRA_TITLE, "${appointment.serviceType} Randevusu (${appointment.timeSlot})")
            putExtra(AppointmentReminderReceiver.EXTRA_CUSTOMER_NAME, appointment.customerName)
            putExtra(
                AppointmentReminderReceiver.EXTRA_ADDRESS,
                "${appointment.district} / ${appointment.neighborhood} ${appointment.streetDoorNo}".trim()
            )
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointment.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } catch (ignored: Exception) {}
        }
    }

    fun scheduleTestReminder(context: Context, delaySeconds: Int = 5) {
        createNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AppointmentReminderReceiver::class.java).apply {
            putExtra(AppointmentReminderReceiver.EXTRA_APPOINTMENT_ID, "test_999")
            putExtra(AppointmentReminderReceiver.EXTRA_TITLE, "⏰ Test Randevu Hatırlatıcı")
            putExtra(AppointmentReminderReceiver.EXTRA_CUSTOMER_NAME, "Ahmet Yılmaz (Test)")
            putExtra(AppointmentReminderReceiver.EXTRA_ADDRESS, "Esenler Menderes Mah. No:12")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            99999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (delaySeconds * 1000L)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Toast.makeText(context, "$delaySeconds saniye sonra test bildirimi gönderilecek!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            showImmediateNotification(context, "⏰ Test Randevusu", "Randevu saatine 30 dakika kaldı! Müşteri: Ahmet Yılmaz")
        }
    }

    fun scheduleDailySummaryReminders(context: Context, todayCount: Int = 0, firstApptSummary: String? = null) {
        createNotificationChannel(context)
        val morningMsg = if (todayCount > 0) {
            "Bugün toplam $todayCount randevunuz var. ${firstApptSummary?.let { "İlk servis: $it" } ?: ""}"
        } else {
            "Bugün için planlanmış randevunuz bulunmuyor. İyi çalışmalar!"
        }
        scheduleDailyAlarm(context, 9, 0, 10001, "☀️ Günaydın Usta (09:00)", morningMsg)

        val noonMsg = "Günün ilk yarısı tamamlandı. Kalan servislerinizi ve günün akışını kontrol edin."
        scheduleDailyAlarm(context, 12, 0, 10002, "🕛 Gün Ortası Durumu (12:00)", noonMsg)
    }

    private fun scheduleDailyAlarm(context: Context, hour: Int, minute: Int, requestCode: Int, title: String, message: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, AppointmentReminderReceiver::class.java).apply {
            putExtra(AppointmentReminderReceiver.EXTRA_APPOINTMENT_ID, "daily_$requestCode")
            putExtra(AppointmentReminderReceiver.EXTRA_TITLE, title)
            putExtra(AppointmentReminderReceiver.EXTRA_CUSTOMER_NAME, message)
            putExtra(AppointmentReminderReceiver.EXTRA_ADDRESS, "Sancak Kombi Mobil Yönetim")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } catch (ignored: Exception) {}
        }
    }

    fun showImmediateNotification(context: Context, title: String, message: String) {
        createNotificationChannel(context)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openAppIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun cancelReminder(context: Context, appointmentId: String) {
        val intent = Intent(context, AppointmentReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointmentId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun parseAppointmentTime(dateStr: String, timeSlot: String, minutesBefore: Int): Long {
        val calendar = Calendar.getInstance()

        try {
            // Check if dateStr is YYYY-MM-DD or DD.MM.YYYY
            val sdf = if (dateStr.contains("-")) {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            } else if (dateStr.contains(".")) {
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            } else {
                null
            }

            if (sdf != null) {
                val parsedDate = sdf.parse(dateStr)
                if (parsedDate != null) {
                    calendar.time = parsedDate
                }
            }

            // Extract hour and minute from timeSlot e.g. "14:00" or "14:00-16:00"
            val startTime = timeSlot.split("-").firstOrNull()?.trim() ?: timeSlot
            val timeParts = startTime.split(":")
            if (timeParts.size >= 2) {
                val hour = timeParts[0].filter { it.isDigit() }.toIntOrNull() ?: 10
                val minute = timeParts[1].filter { it.isDigit() }.toIntOrNull() ?: 0
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Subtract minutesBefore
        calendar.add(Calendar.MINUTE, -minutesBefore)

        val targetMillis = calendar.timeInMillis
        // If targetMillis is in the past, schedule for 10 seconds from now for demo
        return if (targetMillis <= System.currentTimeMillis()) {
            System.currentTimeMillis() + 10000L
        } else {
            targetMillis
        }
    }
}
