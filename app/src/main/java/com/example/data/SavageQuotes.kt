package com.example.data

import com.example.data.model.ScheduleItem

data class RoastCategory(
    val id: String,
    val title: String,
    val description: String,
    val quotes: List<String>,
    val isSecret: Boolean = false
)

object SavageQuotes {

    const val CAT_IGNORED_PRIORITY = "IGNORED_PRIORITY"
    const val CAT_TIME_RUNNING_OUT = "TIME_RUNNING_OUT"
    const val CAT_BROKEN_PROMISES = "BROKEN_PROMISES"
    const val CAT_MIDDAY_SLUMP = "MIDDAY_SLUMP"
    const val CAT_MORNING_SLACKING = "MORNING_SLACKING"
    const val CAT_ALL_COMPLETED = "ALL_COMPLETED"
    const val CAT_BEAR_POKES = "BEAR_POKES"
    const val CAT_SECRETS = "SECRETS"

    const val SECRET_RAPID_TAPS_QUOTE = "Hey I'm talking stop that you buffoon"

    // --- Category 1: Direct Bear Pokes (EXCLUSIVELY chosen when the bear is tapped) ---
    val BEAR_POKES_ROASTS = listOf(
        "Why are you poking me? Go poke your to-do list instead.",
        "Tapping my head won't magically finish your tasks, you know.",
        "Look at you, procrastinating by bothering a cartoon bear. Truly rock bottom.",
        "I'm a bear, not your personal life coach. Get off your ass and work.",
        "Every time you poke me, a deadline somewhere dies a painful death.",
        "Did you really open this app just to tap me instead of doing what you promised?",
        "If you spent as much energy working as you do clicking me, you'd be a billionaire by now.",
        "Stop tapping and start doing. Nobody is impressed by your finger cardio.",
        "I swear, watching you avoid your responsibilities is like watching a slow-motion train wreck.",
        "Are you hoping I'll tell you it's okay to slack off? It's not. Move."
    )

    // --- Category 2: Secrets (Only revealed in tiles after discovering) ---
    val SECRETS_ROASTS = listOf(
        SECRET_RAPID_TAPS_QUOTE,
        "You really thought spamming taps would unlock a cheat code for life?",
        "Secret uncovered: You are still avoiding your actual work right now."
    )

    // --- Category 3: Ignored Deadlines & Priority (For Push Notifications) ---
    val IGNORED_PRIORITY_ROASTS = listOf(
        "You had a scheduled deadline and you blew right past it, you lazy bastard.",
        "Your calendar event literally started and finished without you lifting a finger.",
        "Look at your overdue tasks glaring at you. Do you enjoy failing deadlines?",
        "That task wasn't optional. It was scheduled, and you ignored it completely.",
        "You're treating your highest priority commitments like spam email.",
        "That task has been sitting there ignored for hours. What's your excuse now?",
        "If ignoring high-priority work was an Olympic sport, you'd have gold."
    )

    // --- Category 4: Time Running Out (For Push Notifications) ---
    val TIME_RUNNING_OUT_ROASTS = listOf(
        "The clock is running out on today. You've got hours left and a mountain of excuses.",
        "It's getting late. Are you seriously going to sleep with this unfinished mess?",
        "Midnight is creeping up. Panic mode won't save you, but getting off your ass will.",
        "Another day slipping down the drain while you stare blankly at your phone.",
        "Look at the time. You swore today wouldn't end like yesterday. Fix it now."
    )

    // --- Category 5: Broken Promises to Oneself (For Push Notifications) ---
    val BROKEN_PROMISES_ROASTS = listOf(
        "You promised yourself you'd finish this by now. Why do you treat your own word like trash?",
        "If anyone else lied to you as much as you lie to yourself about your schedule, you'd hate them.",
        "You lazy sonuvabih! That schedule isn't going to complete itself.",
        "Time to finally do something, lazy bastard.",
        "Remember that motivational surge you had? Where did it go? Into the couch cushions?",
        "Breaking promises to yourself is your #1 cardio exercise, isn't it?",
        "Stop negotiating with yourself. You already decided to do it, now execute."
    )

    // --- Category 6: Midday Slump (For Push Notifications) ---
    val MIDDAY_SLUMP_ROASTS = listOf(
        "Half the day is dead and gone. What do you have to show for it? Jack shit.",
        "It's afternoon already and you're still dragging your feet like an amateur.",
        "Doomscrolling through social media isn't finishing your schedule.",
        "You're in the danger zone of giving up for the day. Snap out of it and move.",
        "Still laying there? Your dreams are laughing at your lazy ass right now."
    )

    // --- Category 7: Morning Slacking (For Push Notifications) ---
    val MORNING_SLACKING_ROASTS = listOf(
        "Good morning sleeping beauty. The world has been awake for hours, you haven't.",
        "Coffee in hand, zero work done. Peak slacker aesthetic.",
        "The sun is up, your tasks are waiting, and your ambition is nowhere to be found.",
        "Don't start today with the exact same lazy routine as yesterday. Move!"
    )

    // --- Category 8: 100% Completion Respect ---
    val ALL_COMPLETED_PRAISE = listOf(
        "Wait... you actually finished everything?! Who are you and where is the lazy bum?",
        "I'm genuinely in shock. You didn't fold today. Fine, I admit it: well done.",
        "All tasks complete? Don't let it go to your head, but damn, good job.",
        "No notifications needed anymore today. You actually kept your word for once!",
        "100% finished. I guess you're not completely hopeless after all. Respect."
    )

    val ALL_CATEGORIES = listOf(
        RoastCategory(
            id = CAT_BEAR_POKES,
            title = "BEAR POKES & CALLOUTS",
            description = "Special roasts triggered exclusively when you tap the bear companion.",
            quotes = BEAR_POKES_ROASTS
        ),
        RoastCategory(
            id = CAT_IGNORED_PRIORITY,
            title = "IGNORED DEADLINES & PRIORITY",
            description = "Triggered in notifications when scheduled events are overdue and untouched.",
            quotes = IGNORED_PRIORITY_ROASTS
        ),
        RoastCategory(
            id = CAT_TIME_RUNNING_OUT,
            title = "TIME RUNNING OUT",
            description = "Triggered in late afternoon / night as hours slip away with tasks remaining.",
            quotes = TIME_RUNNING_OUT_ROASTS
        ),
        RoastCategory(
            id = CAT_BROKEN_PROMISES,
            title = "BROKEN PROMISES TO ONESELF",
            description = "Triggered when personal commitments and promises are broken.",
            quotes = BROKEN_PROMISES_ROASTS
        ),
        RoastCategory(
            id = CAT_MIDDAY_SLUMP,
            title = "MIDDAY DOOMSCROLLING",
            description = "Triggered between 12:00 PM and 5:00 PM when procrastination peaks.",
            quotes = MIDDAY_SLUMP_ROASTS
        ),
        RoastCategory(
            id = CAT_MORNING_SLACKING,
            title = "MORNING LAZINESS",
            description = "Triggered in morning hours when sitting on zero progress.",
            quotes = MORNING_SLACKING_ROASTS
        ),
        RoastCategory(
            id = CAT_ALL_COMPLETED,
            title = "100% COMPLETION RESPECT",
            description = "Rare stunned praise when every single task on the schedule is finished.",
            quotes = ALL_COMPLETED_PRAISE
        ),
        RoastCategory(
            id = CAT_SECRETS,
            title = "SECRET ROASTS",
            description = "Hidden easter eggs unlocked through secret interactions with the bear.",
            quotes = SECRETS_ROASTS,
            isSecret = true
        )
    )

    val TOTAL_QUOTES_COUNT = ALL_CATEGORIES.sumOf { it.quotes.size }

    /**
     * Pick a random direct poke roast when the bear is clicked.
     */
    fun getRandomBearPokeRoast(): String {
        return BEAR_POKES_ROASTS.random()
    }

    /**
     * Contextual roast selection for background PUSH NOTIFICATIONS.
     */
    fun selectContextualPushRoast(
        items: List<ScheduleItem>,
        hourOfDay: Int,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Pair<String, String> {
        val total = items.size
        val completed = items.count { it.isCompleted }
        val pending = total - completed

        if (total > 0 && pending == 0) {
            return Pair(ALL_COMPLETED_PRAISE.random(), "100% COMPLETION RESPECT")
        }

        val hasOverdue = items.any { !it.isCompleted && !it.isAllDay && it.endTime < currentTimeMillis }
        if (hasOverdue) {
            return Pair(IGNORED_PRIORITY_ROASTS.random(), "IGNORED DEADLINES & PRIORITY")
        }

        if (hourOfDay >= 18) {
            return Pair(TIME_RUNNING_OUT_ROASTS.random(), "TIME RUNNING OUT")
        }

        if (hourOfDay >= 12) {
            return Pair(MIDDAY_SLUMP_ROASTS.random(), "MIDDAY DOOMSCROLLING")
        }

        if (hourOfDay < 12 && completed == 0) {
            return Pair(MORNING_SLACKING_ROASTS.random(), "MORNING LAZINESS")
        }

        return Pair(BROKEN_PROMISES_ROASTS.random(), "BROKEN PROMISES TO ONESELF")
    }
}
