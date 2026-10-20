package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_items")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis() + 3600000,
    val isCompleted: Boolean = false,
    val isAllDay: Boolean = false,
    val source: String = SOURCE_MANUAL, // SOURCE_CALENDAR or SOURCE_MANUAL
    val calendarEventId: Long? = null,
    val calendarName: String? = null,
    val location: String? = null,
    val dateString: String // Format: YYYY-MM-DD
) {
    companion object {
        const val SOURCE_CALENDAR = "CALENDAR"
        const val SOURCE_MANUAL = "MANUAL"
    }
}

@Entity(tableName = "unlocked_roasts")
data class UnlockedRoast(
    @PrimaryKey
    val quoteText: String,
    val category: String,
    val firstUnlockedAt: Long = System.currentTimeMillis()
)
