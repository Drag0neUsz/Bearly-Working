package com.example.data.local

import androidx.room.*
import com.example.data.model.ScheduleItem
import com.example.data.model.UnlockedRoast
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedule_items WHERE dateString = :dateString ORDER BY isAllDay DESC, startTime ASC")
    fun getItemsForDateFlow(dateString: String): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedule_items WHERE dateString = :dateString ORDER BY isAllDay DESC, startTime ASC")
    suspend fun getItemsForDate(dateString: String): List<ScheduleItem>

    @Query("SELECT * FROM schedule_items WHERE calendarEventId = :calendarEventId AND dateString = :dateString LIMIT 1")
    suspend fun findByCalendarEventId(calendarEventId: Long, dateString: String): ScheduleItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ScheduleItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ScheduleItem>)

    @Update
    suspend fun updateItem(item: ScheduleItem)

    @Query("UPDATE schedule_items SET isCompleted = :completed WHERE id = :itemId")
    suspend fun updateCompletion(itemId: Long, completed: Boolean)

    @Delete
    suspend fun deleteItem(item: ScheduleItem)

    @Query("DELETE FROM schedule_items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: Long)

    @Query("SELECT COUNT(*) FROM schedule_items WHERE dateString = :dateString AND isCompleted = 0")
    suspend fun getUncompletedCountForDate(dateString: String): Int

    @Query("SELECT COUNT(*) FROM schedule_items WHERE dateString = :dateString")
    suspend fun getTotalCountForDate(dateString: String): Int

    // Unlocked Roasts (Completion Book)
    @Query("SELECT * FROM unlocked_roasts ORDER BY firstUnlockedAt DESC")
    fun getAllUnlockedRoastsFlow(): Flow<List<UnlockedRoast>>

    @Query("SELECT * FROM unlocked_roasts")
    suspend fun getAllUnlockedRoastsList(): List<UnlockedRoast>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlockRoast(roast: UnlockedRoast)

    @Query("SELECT EXISTS(SELECT 1 FROM unlocked_roasts WHERE quoteText = :quoteText)")
    suspend fun isRoastUnlocked(quoteText: String): Boolean
}
