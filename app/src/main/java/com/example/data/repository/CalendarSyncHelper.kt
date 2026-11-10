package com.example.data.repository

import android.content.ContentUris
import android.content.ContentValues
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
import java.util.TimeZone

class CalendarSyncHelper(private val context: Context) {

    fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasWriteCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Finds the user's primary or first writable device calendar, or provisions a local
     * device calendar if no calendar account exists yet (e.g., on an emulator without Google Sign-In).
     */
    private fun findOrCreateWritableCalendar(): Pair<Long, String>? {
        if (!hasCalendarPermission() || !hasWriteCalendarPermission()) return null

        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.IS_PRIMARY
        )
        val selection = "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ?"
        val selectionArgs = arrayOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR.toString())

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CalendarContract.Calendars.IS_PRIMARY} DESC, ${CalendarContract.Calendars._ID} ASC"
            )
            cursor?.let {
                if (it.moveToFirst()) {
                    val idIdx = it.getColumnIndex(CalendarContract.Calendars._ID)
                    val nameIdx = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                    val accIdx = it.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
                    val calId = if (idIdx != -1) it.getLong(idIdx) else -1L
                    val displayName = if (nameIdx != -1) it.getString(nameIdx) else null
                    val accountName = if (accIdx != -1) it.getString(accIdx) else null
                    if (calId != -1L) {
                        val label = displayName?.takeIf { s -> s.isNotBlank() }
                            ?: accountName?.takeIf { s -> s.isNotBlank() }
                            ?: "Phone Calendar"
                        return Pair(calId, label)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        // Fallback: create a local device calendar if the device has no calendars configured
        return try {
            val accountName = "Phone Calendar"
            val values = ContentValues().apply {
                put(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
                put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                put(CalendarContract.Calendars.NAME, "Bearly Finished Schedule")
                put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "Phone Calendar")
                put(CalendarContract.Calendars.CALENDAR_COLOR, 0xFF58CC02.toInt())
                put(
                    CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                    CalendarContract.Calendars.CAL_ACCESS_OWNER
                )
                put(CalendarContract.Calendars.OWNER_ACCOUNT, accountName)
                put(CalendarContract.Calendars.VISIBLE, 1)
                put(CalendarContract.Calendars.SYNC_EVENTS, 1)
                put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().id)
            }
            val syncUri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
                .appendQueryParameter(
                    CalendarContract.Calendars.ACCOUNT_TYPE,
                    CalendarContract.ACCOUNT_TYPE_LOCAL
                )
                .build()
            val insertedUri = context.contentResolver.insert(syncUri, values)
            val newCalId = insertedUri?.let { ContentUris.parseId(it) } ?: -1L
            if (newCalId != -1L) Pair(newCalId, "Phone Calendar") else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Inserts a new commitment into the phone's calendar and returns Pair(eventId, calendarName).
     */
    fun insertEventToDeviceCalendar(
        title: String,
        description: String? = null,
        startTime: Long,
        endTime: Long,
        isAllDay: Boolean
    ): Pair<Long, String>? {
        if (!hasWriteCalendarPermission()) return null
        val targetCalendar = findOrCreateWritableCalendar() ?: return null
        val (calendarId, calendarName) = targetCalendar

        return try {
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, title)
                put(
                    CalendarContract.Events.DESCRIPTION,
                    description ?: "Scheduled via Bearly Finished"
                )
                put(CalendarContract.Events.DTSTART, startTime)
                put(CalendarContract.Events.DTEND, endTime.coerceAtLeast(startTime + 1800000L))
                put(CalendarContract.Events.ALL_DAY, if (isAllDay) 1 else 0)
                put(
                    CalendarContract.Events.EVENT_TIMEZONE,
                    if (isAllDay) "UTC" else TimeZone.getDefault().id
                )
            }
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.let { ContentUris.parseId(it) } ?: -1L
            if (eventId > 0L) Pair(eventId, calendarName) else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Updates an existing event on the phone's calendar if permission is granted.
     */
    fun updateDeviceCalendarEvent(
        eventId: Long,
        title: String,
        startTime: Long,
        endTime: Long,
        isAllDay: Boolean
    ): Boolean {
        if (!hasWriteCalendarPermission() || eventId <= 0L) return false
        return try {
            val values = ContentValues().apply {
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DTSTART, startTime)
                put(CalendarContract.Events.DTEND, endTime.coerceAtLeast(startTime + 1800000L))
                put(CalendarContract.Events.ALL_DAY, if (isAllDay) 1 else 0)
                put(
                    CalendarContract.Events.EVENT_TIMEZONE,
                    if (isAllDay) "UTC" else TimeZone.getDefault().id
                )
            }
            val updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            context.contentResolver.update(updateUri, values, null, null) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Deletes an event from the phone's calendar if permission is granted.
     */
    fun deleteDeviceCalendarEvent(eventId: Long): Boolean {
        if (!hasWriteCalendarPermission() || eventId <= 0L) return false
        return try {
            val deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            context.contentResolver.delete(deleteUri, null, null) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
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
