package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleItem
import com.example.ui.components.IosTimeWheelPicker
import com.example.ui.components.SavageBearView
import com.example.ui.components.ScheduleItemCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.SavageUiState
import com.example.ui.viewmodel.TaskFilter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: SavageUiState,
    onRequestCalendarPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onToggleCompleted: (ScheduleItem) -> Unit,
    onDeleteItem: (ScheduleItem) -> Unit,
    onPokeBear: () -> Unit,
    onTriggerSecretRapidTap: () -> Unit,
    onSetFilter: (TaskFilter) -> Unit,
    onAddTask: (title: String, isAllDay: Boolean, hour: Int, minute: Int) -> Unit,
    onEditTask: (ScheduleItem, String, Boolean, Int, Int) -> Unit = { _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ScheduleItem?>(null) }
    val todayFormatted = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()).uppercase()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ComicWhiteBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            // Sharp rectangular Action Button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(ComicInkBlack, shape = RectangleShape)
                    .border(width = 2.dp, color = ComicInkBlack, shape = RectangleShape)
                    .clickable { showAddDialog = true }
                    .testTag("fab_add_task"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Schedule Item",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 68.dp)
        ) {
            // Top Bar Frame (Clean, automatic sync in background, NO manual sync button!)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ComicSurfaceWhite)
                        .border(width = 2.dp, color = ComicInkBlack)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BEARLY FINISHED",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp,
                                    fontSize = 20.sp
                                ),
                                color = ComicInkBlack
                            )
                            Text(
                                text = todayFormatted,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = ComicTextSecondary
                            )
                        }

                        // Status pill showing auto-synced status
                        if (uiState.isAutoSyncing) {
                            Text(
                                text = "AUTO-SYNCING...",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    fontSize = 10.sp
                                ),
                                color = ComicAccentRed
                            )
                        } else if (uiState.hasCalendarPermission) {
                            Box(
                                modifier = Modifier
                                    .border(width = 1.dp, color = ComicBorderSubtle)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "CALENDAR CONNECTED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = ComicTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Calendar Permission Warning Frame (Full Width Frame)
            item {
                AnimatedVisibility(visible = !uiState.hasCalendarPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .background(Color(0xFFFEF3C7))
                            .border(width = 2.dp, color = ComicInkBlack)
                            .padding(12.dp)
                            .testTag("banner_calendar_permission")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = ComicInkBlack,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CONNECT DEVICE CALENDAR",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = ComicInkBlack
                                )
                                Text(
                                    text = "Auto-pulls today's events on startup without manual tapping.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ComicInkBlack
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(ComicInkBlack)
                                    .clickable(onClick = onRequestCalendarPermission)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("btn_grant_calendar")
                            ) {
                                Text(
                                    text = "GRANT",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Notification Permission Warning Frame (Full Width Frame)
            item {
                AnimatedVisibility(visible = !uiState.hasNotificationPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .background(Color(0xFFFEE2E2))
                            .border(width = 2.dp, color = ComicAccentRed)
                            .padding(12.dp)
                            .testTag("banner_notification_permission")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = ComicAccentRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "NOTIFICATIONS DISABLED",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = ComicAccentRed
                                )
                                Text(
                                    text = "Required for the bear to poke you when slacking.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ComicInkBlack
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(ComicAccentRed)
                                    .clickable(onClick = onRequestNotificationPermission)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("btn_grant_notifications")
                            ) {
                                Text(
                                    text = "ENABLE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Savage Bear Companion Section (Talking animation synced with letters appearing!)
            item {
                SavageBearView(
                    quote = uiState.bearQuote,
                    completedRatio = uiState.completionRatio,
                    onPokeBear = onPokeBear,
                    onTriggerSecretRapidTap = onTriggerSecretRapidTap
                )
            }

            // Scorecard (Full-Width Sharp Frame)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .background(ComicSurfaceWhite)
                        .border(width = 2.dp, color = ComicInkBlack)
                        .padding(14.dp)
                        .testTag("card_progress_summary")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DAILY SCORECARD",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = ComicInkBlack
                            )
                            Text(
                                text = "${uiState.completedCount} / ${uiState.totalCount} COMPLETED",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = if (uiState.completionRatio >= 1f && uiState.totalCount > 0) ComicAccentGreen else ComicInkBlack
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Sharp Progress Indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .background(ComicSurfaceSubtle)
                                .border(width = 1.5.dp, color = ComicInkBlack)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = uiState.completionRatio.coerceIn(0f, 1f))
                                    .background(if (uiState.completionRatio >= 1f) ComicAccentGreen else ComicInkBlack)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = when {
                                uiState.totalCount == 0 -> "NO TASKS SCHEDULED. THE BEAR IS WATCHING."
                                uiState.completionRatio >= 1f -> "🎉 ALL TASKS COMPLETE! REMINDERS PAUSED FOR TODAY."
                                else -> "⚠️ ${uiState.pendingCount} TASK(S) REMAINING. REMINDER EVERY ${uiState.notificationIntervalMinutes} MIN."
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = if (uiState.completionRatio >= 1f) ComicAccentGreen else ComicTextSecondary
                        )
                    }
                }
            }

            // Full-Width Sharp Segmented Filter Bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .border(width = 2.dp, color = ComicInkBlack)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val isAllSelected = uiState.filter == TaskFilter.ALL
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isAllSelected) ComicInkBlack else ComicSurfaceWhite)
                                .clickable { onSetFilter(TaskFilter.ALL) }
                                .padding(vertical = 10.dp)
                                .testTag("filter_all"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ALL (${uiState.totalCount})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                color = if (isAllSelected) Color.White else ComicInkBlack
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(40.dp)
                                .background(ComicInkBlack)
                        )

                        val isPendingSelected = uiState.filter == TaskFilter.PENDING
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isPendingSelected) ComicInkBlack else ComicSurfaceWhite)
                                .clickable { onSetFilter(TaskFilter.PENDING) }
                                .padding(vertical = 10.dp)
                                .testTag("filter_pending"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "PENDING (${uiState.pendingCount})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                color = if (isPendingSelected) Color.White else ComicInkBlack
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(40.dp)
                                .background(ComicInkBlack)
                        )

                        val isDoneSelected = uiState.filter == TaskFilter.COMPLETED
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isDoneSelected) ComicAccentGreen else ComicSurfaceWhite)
                                .clickable { onSetFilter(TaskFilter.COMPLETED) }
                                .padding(vertical = 10.dp)
                                .testTag("filter_completed"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "DONE (${uiState.completedCount})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                color = if (isDoneSelected) Color.White else ComicInkBlack
                            )
                        }
                    }
                }
            }

            // Empty State (Full-Width Sharp Frame - NO sync button)
            if (uiState.filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .background(ComicSurfaceWhite)
                            .border(width = 2.dp, color = ComicInkBlack)
                            .padding(24.dp)
                            .testTag("empty_state_card"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.EventBusy,
                                contentDescription = null,
                                tint = ComicInkBlack,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (uiState.filter == TaskFilter.ALL) "NO SCHEDULE ITEMS TODAY" else "NO ITEMS IN THIS VIEW",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = ComicInkBlack
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+' below to add your commitments for today.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ComicTextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .background(ComicInkBlack)
                                    .clickable { showAddDialog = true }
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Text("ADD COMMITMENT", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Schedule Items List (Full Width Sharp Frames)
            items(uiState.filteredItems, key = { it.id }) { item ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    ScheduleItemCard(
                        item = item,
                        onToggleCompleted = { onToggleCompleted(item) },
                        onEdit = { editingItem = item },
                        onDelete = { onDeleteItem(item) }
                    )
                }
            }
        }
    }

    // Add Task Dialog (Sharp Light Mode Frame)
    if (showAddDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var isAllDay by remember { mutableStateOf(false) }
        var selectedHour by remember { mutableIntStateOf(14) }
        var selectedMinute by remember { mutableIntStateOf(0) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "ADD SCHEDULE COMMITMENT",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = ComicInkBlack
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Commit to it. The bear will track whether you finish.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ComicTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task or Event Title") },
                        placeholder = { Text("e.g. Finish client report, Workout") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ComicInkBlack,
                            unfocusedBorderColor = ComicBorderSubtle,
                            focusedTextColor = ComicInkBlack,
                            unfocusedTextColor = ComicInkBlack
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_task_title")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isAllDay,
                            onCheckedChange = { isAllDay = it },
                            colors = CheckboxDefaults.colors(checkedColor = ComicInkBlack)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ALL-DAY TASK", color = ComicInkBlack, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    if (!isAllDay) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // Live Time Preview Header
                        val amPm = if (selectedHour < 12) "AM" else "PM"
                        val displayH = if (selectedHour == 0) 12 else if (selectedHour > 12) selectedHour - 12 else selectedHour
                        val liveTimeStr = String.format("%02d:%02d (%d:%02d %s)", selectedHour, selectedMinute, displayH, selectedMinute, amPm)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "COMMITMENT TIME",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                ),
                                color = ComicInkBlack
                            )
                            Box(
                                modifier = Modifier
                                    .background(ComicInkBlack)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = liveTimeStr,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Directly scrollable drum wheel picker (iOS alarm style, with slight single vibration per number)
                        IosTimeWheelPicker(
                            selectedHour = selectedHour,
                            selectedMinute = selectedMinute,
                            onHourChange = { selectedHour = it },
                            onMinuteChange = { selectedMinute = it },
                            modifier = Modifier.fillMaxWidth(),
                            height = 132.dp
                        )
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .background(ComicInkBlack)
                        .clickable {
                            if (taskTitle.isNotBlank()) {
                                onAddTask(taskTitle, isAllDay, selectedHour, selectedMinute)
                                showAddDialog = false
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("btn_confirm_add_task")
                ) {
                    Text("ADD TO SCHEDULE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("CANCEL", color = ComicTextSecondary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = ComicSurfaceWhite,
            shape = RectangleShape
        )
    }

    // Edit Task Dialog (Sharp Light Mode Frame)
    editingItem?.let { currentItem ->
        val initialCal = remember(currentItem) {
            Calendar.getInstance().apply { timeInMillis = currentItem.startTime }
        }
        var editTitle by remember(currentItem) { mutableStateOf(currentItem.title) }
        var editIsAllDay by remember(currentItem) { mutableStateOf(currentItem.isAllDay) }
        var editHour by remember(currentItem) { mutableIntStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) }
        var editMinute by remember(currentItem) { mutableIntStateOf(initialCal.get(Calendar.MINUTE)) }

        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = {
                Text(
                    text = "EDIT SCHEDULE COMMITMENT",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = ComicInkBlack
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Modify commitment details. The bear expects results.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ComicTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Task or Event Title") },
                        placeholder = { Text("e.g. Finish client report, Workout") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ComicInkBlack,
                            unfocusedBorderColor = ComicBorderSubtle,
                            focusedTextColor = ComicInkBlack,
                            unfocusedTextColor = ComicInkBlack
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_edit_task_title")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = editIsAllDay,
                            onCheckedChange = { editIsAllDay = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = ComicInkBlack,
                                uncheckedColor = ComicInkBlack
                            ),
                            modifier = Modifier.testTag("checkbox_edit_all_day")
                        )
                        Text(
                            text = "ALL-DAY COMMITMENT",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = ComicInkBlack,
                            modifier = Modifier.clickable { editIsAllDay = !editIsAllDay }
                        )
                    }

                    if (!editIsAllDay) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // Live Time Preview Header
                        val amPm = if (editHour < 12) "AM" else "PM"
                        val displayHour12 = when (val h = editHour % 12) {
                            0 -> 12
                            else -> h
                        }
                        val liveTimeStr = "${String.format("%02d:%02d", editHour, editMinute)} ($displayHour12:${String.format("%02d", editMinute)} $amPm)"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "COMMITMENT TIME",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = ComicInkBlack
                            )
                            Box(
                                modifier = Modifier
                                    .background(ComicInkBlack)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = liveTimeStr,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Directly scrollable drum wheel picker (iOS alarm style, with slight single vibration per number)
                        IosTimeWheelPicker(
                            selectedHour = editHour,
                            selectedMinute = editMinute,
                            onHourChange = { editHour = it },
                            onMinuteChange = { editMinute = it },
                            modifier = Modifier.fillMaxWidth(),
                            height = 132.dp
                        )
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .background(ComicInkBlack)
                        .clickable {
                            if (editTitle.isNotBlank()) {
                                onEditTask(currentItem, editTitle, editIsAllDay, editHour, editMinute)
                                editingItem = null
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("btn_confirm_edit_task")
                ) {
                    Text("SAVE CHANGES", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) {
                    Text("CANCEL", color = ComicTextSecondary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = ComicSurfaceWhite,
            shape = RectangleShape
        )
    }
}
