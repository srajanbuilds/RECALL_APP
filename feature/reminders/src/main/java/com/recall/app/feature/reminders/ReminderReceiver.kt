package com.recall.app.feature.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

const val CHANNEL_ID = "recall_reminders"
const val EXTRA_REMINDER_LABEL = "reminder_label"
const val EXTRA_REMINDER_ID = "reminder_id"

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val label = intent.getStringExtra(EXTRA_REMINDER_LABEL) ?: "Reminder"
        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: "0"

        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel (required on Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recall Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminder notifications from Recall"
                enableVibration(true)
            }
            notifManager.createNotificationChannel(channel)
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val openIntent = PendingIntent.getActivity(
            context, 0,
            launchIntent ?: Intent(),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ Recall Reminder")
            .setContentText(label)
            .setStyle(NotificationCompat.BigTextStyle().bigText(label))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .build()

        notifManager.notify(reminderId.hashCode(), notification)
    }
}

fun scheduleReminder(context: Context, reminderId: String, triggerAtMs: Long, label: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra(EXTRA_REMINDER_LABEL, label)
        putExtra(EXTRA_REMINDER_ID, reminderId)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        reminderId.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
    } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
    }
}

fun cancelReminder(context: Context, reminderId: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        reminderId.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
}
