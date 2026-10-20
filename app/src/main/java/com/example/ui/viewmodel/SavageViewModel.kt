package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SavageQuotes
import com.example.data.local.SavageSettingsManager
import com.example.data.model.ScheduleItem
import com.example.data.repository.ScheduleRepository
import com.example.notification.SavageNotificationManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TaskFilter {
    ALL,
    PENDING,
    COMPLETED
}

data class SavageUiState(
    val items: List<ScheduleItem> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL,
    val bearQuote: String = "Tap my head. Let's see what promises you've broken today.",
    val bearQuoteCategory: String = "READY TO ROAST",
    val hasCalendarPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val isAutoSyncing: Boolean = false,
    val notificationIntervalMinutes: Int = 120,
    val notificationsEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = true,
    val quietStartHour: Int = 23,
    val quietStartMinute: Int = 0,
    val quietEndHour: Int = 7,
    val quietEndMinute: Int = 0,
    val vibrateEnabled: Boolean = true,
    val unlockedQuotes: Set<String> = emptySet(),
    val statusMessage: String? = null
) {
    val totalCount: Int get() = items.size
    val completedCount: Int get() = items.count { it.isCompleted }
    val pendingCount: Int get() = items.count { !it.isCompleted }
    val completionRatio: Float
        get() = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount

    val totalRoastsCount: Int get() = SavageQuotes.TOTAL_QUOTES_COUNT
    val unlockedRoastsCount: Int get() = unlockedQuotes.size
    val completionBookPercent: Int
        get() = if (totalRoastsCount == 0) 0 else ((unlockedRoastsCount.toFloat() / totalRoastsCount) * 100).toInt()

    val filteredItems: List<ScheduleItem>
        get() = when (filter) {
            TaskFilter.ALL -> items
            TaskFilter.PENDING -> items.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> items.filter { it.isCompleted }
        }
}

class SavageViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScheduleRepository(application)
    private val settings = SavageSettingsManager(application)
    private val notificationManager = SavageNotificationManager(application)

    private val _uiState = MutableStateFlow(
        SavageUiState(
            hasCalendarPermission = repository.hasCalendarPermission(),
            notificationIntervalMinutes = settings.notificationIntervalMinutes,
            notificationsEnabled = settings.notificationsEnabled,
            quietHoursEnabled = settings.quietHoursEnabled,
            quietStartHour = settings.quietStartHour,
            quietStartMinute = settings.quietStartMinute,
            quietEndHour = settings.quietEndHour,
            quietEndMinute = settings.quietEndMinute,
            vibrateEnabled = settings.vibrateEnabled
        )
    )
    val uiState: StateFlow<SavageUiState> = _uiState.asStateFlow()

    init {
        val todayStr = repository.getTodayDateString()

        if (repository.hasCalendarPermission()) {
            autoSyncCalendar()
        }

        viewModelScope.launch {
            repository.getItemsForDateFlow(todayStr).collect { items ->
                _uiState.update { current ->
                    current.copy(items = items)
                }
            }
        }

        viewModelScope.launch {
            repository.getUnlockedRoastsFlow().collect { unlockedList ->
                val unlockedSet = unlockedList.map { it.quoteText }.toSet()
                _uiState.update { it.copy(unlockedQuotes = unlockedSet) }
            }
        }

        if (settings.notificationsEnabled) {
            notificationManager.scheduleNextCheck()
        }
    }

    fun updatePermissions(calendarGranted: Boolean, notificationGranted: Boolean) {
        _uiState.update {
            it.copy(
                hasCalendarPermission = calendarGranted,
                hasNotificationPermission = notificationGranted
            )
        }
        if (calendarGranted) {
            autoSyncCalendar()
        }
    }

    fun setFilter(filter: TaskFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun autoSyncCalendar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAutoSyncing = true) }
            repository.syncCalendarForDate(Calendar.getInstance())
            _uiState.update { it.copy(isAutoSyncing = false) }
        }
    }

    fun toggleCompletion(item: ScheduleItem) {
        viewModelScope.launch {
            repository.toggleCompletion(item)
            val updatedWillBeCompleted = !item.isCompleted
            val currentItems = _uiState.value.items
            val completedNow = currentItems.count { if (it.id == item.id) updatedWillBeCompleted else it.isCompleted }
            val ratio = if (currentItems.isEmpty()) 0f else completedNow.toFloat() / currentItems.size

            if (ratio >= 1.0f && currentItems.isNotEmpty()) {
                val praise = SavageQuotes.ALL_COMPLETED_PRAISE.random()
                repository.unlockRoast(praise, "100% COMPLETION RESPECT")
                _uiState.update {
                    it.copy(
                        bearQuote = praise,
                        bearQuoteCategory = "100% COMPLETION RESPECT"
                    )
                }
            }
        }
    }

    /**
     * Tapping the bear chooses EXCLUSIVELY from the dedicated Bear Pokes category.
     * Custom vibration pattern does NOT fire on in-app tap (only applies to push notifs).
     */
    fun pokeBear() {
        val quote = SavageQuotes.getRandomBearPokeRoast()
        viewModelScope.launch {
            repository.unlockRoast(quote, SavageQuotes.CAT_BEAR_POKES)
        }

        _uiState.update {
            it.copy(
                bearQuote = quote,
                bearQuoteCategory = "BEAR POKES & CALLOUTS"
            )
        }
    }

    /**
     * Triggered when user rapidly taps the bear 10 times!
     * Unlocks the secret roast into the Secrets category.
     */
    fun triggerSecretRapidTap() {
        val secretQuote = SavageQuotes.SECRET_RAPID_TAPS_QUOTE
        viewModelScope.launch {
            repository.unlockRoast(secretQuote, SavageQuotes.CAT_SECRETS)
        }

        _uiState.update {
            it.copy(
                bearQuote = secretQuote,
                bearQuoteCategory = "SECRET UNLOCKED"
            )
        }
    }

    fun addQuickTask(title: String, isAllDay: Boolean = false, hour: Int = 12, minute: Int = 0) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            val startTime = cal.timeInMillis
            val endTime = startTime + 3600000

            val item = ScheduleItem(
                title = title.trim(),
                startTime = startTime,
                endTime = endTime,
                isAllDay = isAllDay,
                source = ScheduleItem.SOURCE_MANUAL,
                dateString = repository.getTodayDateString()
            )
            repository.addItem(item)
        }
    }

    fun deleteItem(item: ScheduleItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun setNotificationInterval(minutes: Int) {
        settings.notificationIntervalMinutes = minutes
        _uiState.update { it.copy(notificationIntervalMinutes = minutes) }
        notificationManager.scheduleNextCheck(minutes)
    }

    fun toggleNotifications(enabled: Boolean) {
        settings.notificationsEnabled = enabled
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        if (enabled) {
            notificationManager.scheduleNextCheck()
        } else {
            notificationManager.cancelScheduledChecks()
        }
    }

    fun toggleQuietHours(enabled: Boolean) {
        settings.quietHoursEnabled = enabled
        _uiState.update { it.copy(quietHoursEnabled = enabled) }
    }

    fun setSleepTime(hour: Int, minute: Int) {
        settings.quietStartHour = hour
        settings.quietStartMinute = minute
        _uiState.update { it.copy(quietStartHour = hour, quietStartMinute = minute) }
    }

    fun setWakeTime(hour: Int, minute: Int) {
        settings.quietEndHour = hour
        settings.quietEndMinute = minute
        _uiState.update { it.copy(quietEndHour = hour, quietEndMinute = minute) }
    }

    fun setQuietHours(startHour: Int, endHour: Int) {
        settings.quietStartHour = startHour
        settings.quietEndHour = endHour
        _uiState.update { it.copy(quietStartHour = startHour, quietEndHour = endHour) }
    }

    fun toggleVibration(enabled: Boolean) {
        settings.vibrateEnabled = enabled
        _uiState.update { it.copy(vibrateEnabled = enabled) }
    }

    /**
     * Test Push Notification: fires push notification with the custom 3-short + 1-long vibration pattern!
     */
    fun testPushNotificationNow() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val items = _uiState.value.items
        val (selectedQuote, category) = SavageQuotes.selectContextualPushRoast(items, hour)

        viewModelScope.launch {
            repository.unlockRoast(selectedQuote, category)
        }

        notificationManager.showSavageNotification(
            roastMessage = selectedQuote,
            uncompletedCount = if (_uiState.value.pendingCount > 0) _uiState.value.pendingCount else null
        )
        _uiState.update { it.copy(statusMessage = "Fired savage push notification!") }
    }

    fun clearRoastsCollectionBook() {
        viewModelScope.launch {
            repository.clearUnlockedRoasts()
            _uiState.update {
                it.copy(
                    unlockedQuotes = emptySet(),
                    statusMessage = "Cleared Roast Completion Book. All roasts locked."
                )
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
