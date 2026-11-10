package com.example

import com.example.data.SavageQuotes
import com.example.data.model.ScheduleItem
import com.example.notification.SavageNotificationManager
import com.example.ui.viewmodel.SavageUiState
import org.junit.Assert.*
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `savage categories and quotes are non empty`() {
        assertTrue(SavageQuotes.ALL_CATEGORIES.isNotEmpty())
        assertTrue(SavageQuotes.BEAR_POKES_ROASTS.isNotEmpty())
        assertTrue(SavageQuotes.FINISHED_DAY_POKES_ROASTS.isNotEmpty())
        assertTrue(SavageQuotes.SECRETS_ROASTS.isNotEmpty())
        assertTrue(SavageQuotes.BROKEN_PROMISES_ROASTS.isNotEmpty())
        assertTrue(SavageQuotes.ALL_COMPLETED_PRAISE.isNotEmpty())
        assertTrue(SavageQuotes.IGNORED_PRIORITY_ROASTS.isNotEmpty())

        val finishedDayCategory = SavageQuotes.ALL_CATEGORIES.find { it.id == SavageQuotes.CAT_FINISHED_DAY_POKES }
        assertNotNull(finishedDayCategory)
        assertEquals("FINISHED DAY POKES", finishedDayCategory?.title)

        val (quote, category) = SavageQuotes.selectContextualPushRoast(emptyList(), 14)
        assertNotNull(quote)
        assertTrue(quote.isNotBlank())
        assertTrue(category.isNotBlank())
    }

    @Test
    fun `bear poke returns dedicated poke quote`() {
        val pokeQuote = SavageQuotes.getRandomBearPokeRoast()
        assertTrue(SavageQuotes.BEAR_POKES_ROASTS.contains(pokeQuote))
        assertEquals("Hey I'm talking stop that you buffoon", SavageQuotes.SECRET_RAPID_TAPS_QUOTE)
    }

    @Test
    fun `finished day poke returns dedicated completed day poke quote`() {
        val finishedQuote = SavageQuotes.getRandomFinishedDayPokeRoast()
        assertTrue(SavageQuotes.FINISHED_DAY_POKES_ROASTS.contains(finishedQuote))
    }

    @Test
    fun `schedule item completion and ignore default to false`() {
        val item = ScheduleItem(
            title = "Test Task",
            dateString = "2026-10-20"
        )
        assertFalse(item.isCompleted)
        assertFalse(item.isIgnored)
        assertEquals("MANUAL", item.source)
    }

    @Test
    fun `vibration pattern has 3 short and 1 long vibration`() {
        val pattern = SavageNotificationManager.SAVAGE_VIBRATION_PATTERN
        assertEquals(8, pattern.size)
        assertTrue(pattern[1] in 50..80)
        assertTrue(pattern[3] in 50..80)
        assertTrue(pattern[5] in 50..80)
        assertTrue(pattern[7] >= 250)
    }

    @Test
    fun `ui state defaults to quiet mode ON, confirm delete ON, and 120m poke interval`() {
        val state = SavageUiState()
        assertEquals(120, state.notificationIntervalMinutes)
        assertTrue(state.quietHoursEnabled)
        assertTrue(state.confirmBeforeDelete)
    }

    @Test
    fun `main destination contains four pages including calendar`() {
        val destinations = MainDestination.values()
        assertEquals(4, destinations.size)
        assertTrue(destinations.contains(MainDestination.SCHEDULE))
        assertTrue(destinations.contains(MainDestination.CALENDAR))
        assertTrue(destinations.contains(MainDestination.ROASTS))
        assertTrue(destinations.contains(MainDestination.SETTINGS))
        assertEquals("CALENDAR", MainDestination.CALENDAR.title)
    }
}
