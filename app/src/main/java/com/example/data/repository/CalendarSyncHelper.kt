package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.example.data.model.ScheduleItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarSyncHelper(private val context: Context) {

    fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Fetch all calendar instances for the given date.
     */
    fun fetchEventsForDate(calendarDate: Calendar): List<ScheduleItem> {
        if (!hasCalendarPermission()) {
            return emptyList()
        }

        val items = mutableListOf<ScheduleItem>()
        val startOfDay = (calendarDate.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = (calendarDate.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(Date(startOfDay))

        // Query CalendarContract.Instances
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startOfDay)
        ContentUris.appendId(builder, endOfDay)
        val uri: Uri = builder.build()

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.CALENDAR_DISPLAY_NAME
        )

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )

            cursor?.let {
                val idIdx = it.getColumnIndex(CalendarContract.Instances.EVENT_ID)
                val titleIdx = it.getColumnIndex(CalendarContract.Instances.TITLE)
                val descIdx = it.getColumnIndex(CalendarContract.Instances.DESCRIPTION)
                val beginIdx = it.getColumnIndex(CalendarContract.Instances.BEGIN)
                val endIdx = it.getColumnIndex(CalendarContract.Instances.END)
                val allDayIdx = it.getColumnIndex(CalendarContract.Instances.ALL_DAY)
                val locIdx = it.getColumnIndex(CalendarContract.Instances.EVENT_LOCATION)
                val calNameIdx = it.getColumnIndex(CalendarContract.Instances.CALENDAR_DISPLAY_NAME)

                while (it.moveToNext()) {
                    val eventId = if (idIdx != -1) it.getLong(idIdx) else 0L
                    val rawTitle = if (titleIdx != -1) it.getString(titleIdx) else null
                    val title = if (rawTitle.isNullOrBlank()) "Untitled Event" else rawTitle
                    val description = if (descIdx != -1) it.getString(descIdx) else null
                    val begin = if (beginIdx != -1) it.getLong(beginIdx) else startOfDay
                    val end = if (endIdx != -1) it.getLong(endIdx) else begin + 3600000
                    val isAllDay = if (allDayIdx != -1) it.getInt(allDayIdx) == 1 else false
                    val location = if (locIdx != -1) it.getString(locIdx) else null
                    val calName = if (calNameIdx != -1) it.getString(calNameIdx) else null

                    items.add(
                        ScheduleItem(
                            id = 0, // Assigned by Room or matched by calendarEventId
                            title = title,
                            description = description,
                            startTime = begin,
                            endTime = end,
                            isCompleted = false,
                            isAllDay = isAllDay,
                            source = ScheduleItem.SOURCE_CALENDAR,
                            calendarEventId = eventId,
                            calendarName = calName,
                            location = location,
                            dateString = dateString
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return items
    }
}
