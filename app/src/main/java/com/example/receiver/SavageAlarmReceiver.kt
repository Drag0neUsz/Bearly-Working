package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.notification.SavageNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SavageAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_POKE = "com.example.savagebear.ACTION_POKE"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notificationManager = SavageNotificationManager(context)
                notificationManager.performScheduleCheck()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
