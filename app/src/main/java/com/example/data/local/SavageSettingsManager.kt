package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class SavageSettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("savage_bear_settings", Context.MODE_PRIVATE)

    var notificationIntervalMinutes: Int
        get() = prefs.getInt(KEY_INTERVAL, 30)
        set(value) = prefs.edit().putInt(KEY_INTERVAL, value.coerceAtLeast(1)).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    var quietHoursEnabled: Boolean
        get() = prefs.getBoolean(KEY_QUIET_HOURS, false)
        set(value) = prefs.edit().putBoolean(KEY_QUIET_HOURS, value).apply()

    var quietStartHour: Int
        get() = prefs.getInt(KEY_QUIET_START, 23) // 11 PM
        set(value) = prefs.edit().putInt(KEY_QUIET_START, value).apply()

    var quietEndHour: Int
        get() = prefs.getInt(KEY_QUIET_END, 7) // 7 AM
        set(value) = prefs.edit().putInt(KEY_QUIET_END, value).apply()

    var vibrateEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATE, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATE, value).apply()

    var lastRoastTime: Long
        get() = prefs.getLong(KEY_LAST_ROAST, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_ROAST, value).apply()

    companion object {
        private const val KEY_INTERVAL = "notification_interval_minutes"
        private const val KEY_ENABLED = "notifications_enabled"
        private const val KEY_QUIET_HOURS = "quiet_hours_enabled"
        private const val KEY_QUIET_START = "quiet_start_hour"
        private const val KEY_QUIET_END = "quiet_end_hour"
        private const val KEY_VIBRATE = "vibrate_enabled"
        private const val KEY_LAST_ROAST = "last_roast_timestamp"
    }
}
