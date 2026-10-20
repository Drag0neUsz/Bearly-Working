package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SavageQuotes
import com.example.data.model.ScheduleItem
import com.example.notification.SavageNotificationManager
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Savage Bear", appName)
    }

    @Test
    fun `savage categories and quotes are non empty`() {
        assertTrue(SavageQuotes.ALL_CATEGORIES.isNotEmpty())
        assertTrue(SavageQuotes.BEAR_POKES_ROASTS.isNotEmpty())
        assertTrue(SavageQuotes.SECRETS_ROASTS.isNotEmpty())
        assertTrue(SavageQuotes.BROKEN_PROMISES_ROASTS.isNotEmpty())
        assertTrue(SavageQuotes.ALL_COMPLETED_PRAISE.isNotEmpty())
        assertTrue(SavageQuotes.IGNORED_PRIORITY_ROASTS.isNotEmpty())

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
    fun `schedule item completion defaults to false`() {
        val item = ScheduleItem(
            title = "Test Task",
            dateString = "2026-10-20"
        )
        assertFalse(item.isCompleted)
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
}
