package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.ScheduleDao
import com.example.data.model.ScheduleItem
import com.example.data.model.UnlockedRoast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ScheduleRepository(
    private val context: Context,
    private val scheduleDao: ScheduleDao = AppDatabase.getDatabase(context).scheduleDao(),
    private val calendarHelper: CalendarSyncHelper = CalendarSyncHelper(context)
) {

    fun getTodayDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun getItemsForDateFlow(dateString: String): Flow<List<ScheduleItem>> {
        return scheduleDao.getItemsForDateFlow(dateString)
    }

    fun getAllItemsFlow(): Flow<List<ScheduleItem>> {
        return scheduleDao.getAllItemsFlow()
    }

    suspend fun syncCalendarForDateString(dateString: String): Int = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.parse(dateString) ?: Date()
        val cal = Calendar.getInstance().apply { time = date }
        syncCalendarForDate(cal)
    }

    suspend fun getItemsForDate(dateString: String): List<ScheduleItem> = withContext(Dispatchers.IO) {
        scheduleDao.getItemsForDate(dateString)
    }

    suspend fun toggleCompletion(item: ScheduleItem) = withContext(Dispatchers.IO) {
        val newStatus = !item.isCompleted
        scheduleDao.updateCompletion(item.id, newStatus)
    }

    suspend fun addItem(item: ScheduleItem, saveToCalendar: Boolean = false): ScheduleItem = withContext(Dispatchers.IO) {
        val itemToInsert = if (saveToCalendar) {
            val calendarResult = calendarHelper.insertEventToDeviceCalendar(
                title = item.title,
                description = item.description,
                startTime = item.startTime,
                endTime = item.endTime,
                isAllDay = item.isAllDay
            )
            if (calendarResult != null) {
                item.copy(
                    calendarEventId = calendarResult.first,
                    calendarName = calendarResult.second
                )
            } else {
                item
            }
        } else {
            item
        }
        val rowId = scheduleDao.insertItem(itemToInsert)
        itemToInsert.copy(id = rowId)
    }

    suspend fun updateItem(item: ScheduleItem) = withContext(Dispatchers.IO) {
        item.calendarEventId?.let { eventId ->
            calendarHelper.updateDeviceCalendarEvent(
                eventId = eventId,
                title = item.title,
                startTime = item.startTime,
                endTime = item.endTime,
                isAllDay = item.isAllDay
            )
        }
        scheduleDao.updateItem(item)
    }

    suspend fun deleteItem(item: ScheduleItem) = withContext(Dispatchers.IO) {
        if (item.source == ScheduleItem.SOURCE_CALENDAR) {
            // Keep a tombstone in Room so the event won't re-appear on next calendar sync
            scheduleDao.updateItem(item.copy(isIgnored = true))
        } else {
            // If this manual task was pushed to the phone's calendar, remove it there too
            item.calendarEventId?.let { eventId ->
                calendarHelper.deleteDeviceCalendarEvent(eventId)
            }
            scheduleDao.deleteItem(item)
        }
    }

    /**
     * Pull events from user's Android calendar and merge with local database.
     * Preserves completed and ignored states if an event was already synced.
     */
    suspend fun syncCalendarForDate(calendar: Calendar): Int = withContext(Dispatchers.IO) {
        val events = calendarHelper.fetchEventsForDate(calendar)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(calendar.time)

        var newSyncedCount = 0
        for (event in events) {
            val eventId = event.calendarEventId ?: continue
            val existing = scheduleDao.findByCalendarEventId(eventId, dateString)
            if (existing == null) {
                scheduleDao.insertItem(event)
                newSyncedCount++
            } else if (!existing.isIgnored) {
                val updated = existing.copy(
                    title = event.title,
                    description = event.description,
                    startTime = event.startTime,
                    endTime = event.endTime,
                    location = event.location,
                    calendarName = event.calendarName
                )
                scheduleDao.updateItem(updated)
            }
        }
        newSyncedCount
    }

    suspend fun getUncompletedCountForToday(): Int = withContext(Dispatchers.IO) {
        scheduleDao.getUncompletedCountForDate(getTodayDateString())
    }

    suspend fun getTotalCountForToday(): Int = withContext(Dispatchers.IO) {
        scheduleDao.getTotalCountForDate(getTodayDateString())
    }

    fun hasCalendarPermission(): Boolean = calendarHelper.hasCalendarPermission()

    fun hasWriteCalendarPermission(): Boolean = calendarHelper.hasWriteCalendarPermission()

    // Completion Book / Unlocked Roasts
    fun getUnlockedRoastsFlow(): Flow<List<UnlockedRoast>> = scheduleDao.getAllUnlockedRoastsFlow()

    suspend fun unlockRoast(quoteText: String, category: String) = withContext(Dispatchers.IO) {
        scheduleDao.unlockRoast(
            UnlockedRoast(
                quoteText = quoteText,
                category = category,
                firstUnlockedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearUnlockedRoasts() = withContext(Dispatchers.IO) {
        scheduleDao.clearAllUnlockedRoasts()
    }
}
