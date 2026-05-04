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
import com.recall.app.core.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val CHANNEL_ID = "recall_reminders"
const val EXTRA_REMINDER_LABEL = "reminder_label"
const val EXTRA_REMINDER_ID = "reminder_id"

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> restoreAllAlarms(context)
            else -> showNotification(context, intent)
        }
    }

    private fun showNotification(context: Context, intent: Intent) {
        val label = intent.getStringExtra(EXTRA_REMINDER_LABEL) ?: "Reminder"
        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: "0"

        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Recall Reminders", NotificationManager.IMPORTANCE_HIGH
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

    /** Restore all pending alarms after device reboot. */
    private fun restoreAllAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val now = System.currentTimeMillis()
                // Get all future reminders from DB and re-schedule them
                db.noteDao().getAllRemindersOnce()
                    .filter { it.triggerAt > now }
                    .forEach { reminder ->
                        scheduleReminder(context, reminder.id, reminder.triggerAt, reminder.label)
                    }
            } catch (e: Exception) {
                // Silently fail — alarms will be missed but app remains stable
            }
        }
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

    try {
        // Prefer exact alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
        }
    } catch (se: SecurityException) {
        // Exact alarm permission denied — fall back to inexact
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
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
