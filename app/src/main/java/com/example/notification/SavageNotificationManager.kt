package com.example.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.SavageQuotes
import com.example.data.local.AppDatabase
import com.example.data.local.SavageSettingsManager
import com.example.data.repository.ScheduleRepository
import com.example.receiver.SavageAlarmReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SavageNotificationManager(private val context: Context) {

    private val settings = SavageSettingsManager(context)

    companion object {
        const val CHANNEL_ID = "savage_bear_reminders"
        const val CHANNEL_NAME = "Bearly Finished Wakeup Calls"
        const val NOTIFICATION_ID = 1001
        const val ALARM_REQUEST_CODE = 2002

        // 3 very short vibrations and 1 longer in quick succession:
        // Wait 0ms, Vibrate 60ms, Pause 70ms, Vibrate 60ms, Pause 70ms, Vibrate 60ms, Pause 90ms, Vibrate 320ms
        val SAVAGE_VIBRATION_PATTERN = longArrayOf(0, 60, 70, 60, 70, 60, 90, 320)
    }

    init {
        createNotificationChannel()
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Unfiltered push notifications calling you out on being lazy until your schedule is finished."
                enableVibration(true)
                vibrationPattern = SAVAGE_VIBRATION_PATTERN
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Vibrate phone with the custom pattern: 3 very short + 1 longer.
     */
    fun triggerCustomVibration() {
        if (!settings.vibrateEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(SAVAGE_VIBRATION_PATTERN, -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(SAVAGE_VIBRATION_PATTERN, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(SAVAGE_VIBRATION_PATTERN, -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleNextCheck(delayMinutes: Int? = null) {
        if (!settings.notificationsEnabled) {
            cancelScheduledChecks()
            return
        }

        val minutes = delayMinutes ?: settings.notificationIntervalMinutes
        val triggerAtMillis = System.currentTimeMillis() + (minutes * 60 * 1000L)

        val intent = Intent(context, SavageAlarmReceiver::class.java).apply {
            action = SavageAlarmReceiver.ACTION_POKE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelScheduledChecks() {
        val intent = Intent(context, SavageAlarmReceiver::class.java).apply {
            action = SavageAlarmReceiver.ACTION_POKE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }

    fun dismissActiveNotification() {
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Send an unhinged push notification calling out the user.
     */
    fun showSavageNotification(
        roastMessage: String,
        uncompletedCount: Int? = null
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val title = if (uncompletedCount != null && uncompletedCount > 0) {
            "🐻 BEARLY FINISHED ($uncompletedCount TASKS SIT UNFINISHED!)"
        } else {
            "🐻 BEARLY FINISHED"
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(roastMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(roastMessage))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (settings.vibrateEnabled) {
            builder.setVibrate(SAVAGE_VIBRATION_PATTERN)
            triggerCustomVibration()
        }

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
            settings.lastRoastTime = System.currentTimeMillis()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * Check schedule and fire contextual roast only when there are uncompleted tasks.
     */
    suspend fun performScheduleCheck() = withContext(Dispatchers.IO) {
        if (!settings.notificationsEnabled) return@withContext

        // Check quiet hours
        if (settings.quietHoursEnabled) {
            val cal = Calendar.getInstance()
            val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            val startMinutes = settings.quietStartHour * 60 + settings.quietStartMinute
            val endMinutes = settings.quietEndHour * 60 + settings.quietEndMinute
            val isQuiet = if (startMinutes > endMinutes) {
                currentMinutes >= startMinutes || currentMinutes < endMinutes
            } else {
                currentMinutes in startMinutes until endMinutes
            }
            if (isQuiet) {
                scheduleNextCheck()
                return@withContext
            }
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())
        val db = AppDatabase.getDatabase(context)
        val items = db.scheduleDao().getItemsForDate(todayStr)
        val uncompleted = items.count { !it.isCompleted }

        if (uncompleted == 0) {
            // 0 unfinished tasks remaining (either all completed or all deleted).
            // Clear any active notification and schedule next check silently.
            dismissActiveNotification()
            scheduleNextCheck()
            return@withContext
        }

        // Pull roast based on factors (ignored priority / overdue / time left)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val (selectedQuote, category) = SavageQuotes.selectContextualPushRoast(items, hour)

        // Unlock in completion book!
        ScheduleRepository(context).unlockRoast(selectedQuote, category)

        // Show push notification with custom vibration
        showSavageNotification(
            roastMessage = selectedQuote,
            uncompletedCount = uncompleted
        )

        // Reschedule next check
        scheduleNextCheck()
    }
}
