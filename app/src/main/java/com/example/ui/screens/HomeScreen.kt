package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleItem
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SavageUiState
import com.example.ui.viewmodel.TaskFilter
import java.util.Calendar

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
    onAddTask: (title: String, isAllDay: Boolean, hour: Int, minute: Int, saveToCalendar: Boolean) -> Unit,
    onEditTask: (ScheduleItem, String, Boolean, Int, Int) -> Unit = { _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ScheduleItem?>(null) }
    var itemPendingDelete by remember { mutableStateOf<ScheduleItem?>(null) }

    // Streak counter derived from real progress
    val completedDaysStreak = remember(uiState.allItems, uiState.completedCount) {
        val completedDatesCount = uiState.allItems
            .groupBy { it.dateString }
            .count { (_, dayList) -> dayList.isNotEmpty() && dayList.all { it.isCompleted } }
        (completedDatesCount + if (uiState.completedCount > 0) 1 else 0).coerceAtLeast(1)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SoftCanvasBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            // Chunky 3D Electric Sky Blue Floating Action Button
            Tactile3DButton(
                onClick = { showAddDialog = true },
                containerColor = SoftBlue,
                bottomLipColor = SoftBlueShadow,
                cornerRadius = 18.dp,
                lipHeight = 4.dp,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .size(56.dp)
                    .testTag("fab_add_task")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Schedule Item",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 84.dp)
        ) {
            // Top Stats Bar (Streak Badge + Calendar Sync Status Pill — Honey badge removed)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftSurfaceWhite)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Streak Pill Badge with Unicode Ballot Box
                        Tactile3DBadge(
                            containerColor = SoftOrangeLight,
                            borderColor = SoftOrangeBorder,
                            bottomLipColor = SoftOrangeBorder,
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "☒",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = SoftOrangeText
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "$completedDaysStreak",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                ),
                                color = SoftOrangeText
                            )
                        }

                        // Sync Active Status Pill
                        if (uiState.isAutoSyncing) {
                            Tactile3DBadge(
                                containerColor = SoftBlueLight,
                                borderColor = SoftBlueBorder,
                                bottomLipColor = SoftBlueBorder,
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 11.dp, vertical = 5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(SoftBlue, shape = CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SYNCING...",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    ),
                                    color = SoftBlueText
                                )
                            }
                        } else if (uiState.hasCalendarPermission) {
                            Tactile3DBadge(
                                containerColor = SoftGreenLight,
                                borderColor = SoftGreenBorder,
                                bottomLipColor = SoftGreenBorder,
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 11.dp, vertical = 5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(SoftGreen, shape = CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SYNC ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.6.sp
                                    ),
                                    color = SoftGreenText
                                )
                            }
                        } else {
                            Tactile3DBadge(
                                containerColor = Color(0xFFF1F5F9),
                                borderColor = SoftBorder,
                                bottomLipColor = SoftBorder,
                                shape = CircleShape,
                                onClick = onRequestCalendarPermission,
                                contentPadding = PaddingValues(horizontal = 11.dp, vertical = 5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(SoftOrange, shape = CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SYNC OFF",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    ),
                                    color = SoftTextSecondary
                                )
                            }
                        }
                    }

                    HorizontalDivider(thickness = 2.dp, color = Color(0xFFF1F5F9))
                }
            }

            // Calendar Permission Warning Banner
            item {
                AnimatedVisibility(visible = !uiState.hasCalendarPermission) {
                    Tactile3DCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("banner_calendar_permission"),
                        containerColor = SoftYellowLight,
                        borderColor = SoftYellowBorder,
                        bottomLipColor = SoftYellowBorder,
                        cornerRadius = 20.dp,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = SoftYellowText,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CONNECT DEVICE CALENDAR",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = SoftTextPrimary
                                )
                                Text(
                                    text = "Auto-pulls today's events on startup.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Tactile3DButton(
                                onClick = onRequestCalendarPermission,
                                containerColor = SoftOrange,
                                bottomLipColor = SoftOrangeShadow,
                                cornerRadius = 12.dp,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("btn_grant_calendar")
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

            // Notification Permission Warning Banner
            item {
                AnimatedVisibility(visible = !uiState.hasNotificationPermission) {
                    Tactile3DCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .testTag("banner_notification_permission"),
                        containerColor = SoftRedLight,
                        borderColor = SoftRedBorder,
                        bottomLipColor = SoftRedBorder,
                        cornerRadius = 20.dp,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = SoftRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "NOTIFICATIONS DISABLED",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = SoftRed
                                )
                                Text(
                                    text = "Required for the bear to poke you when slacking.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Tactile3DButton(
                                onClick = onRequestNotificationPermission,
                                containerColor = SoftRed,
                                bottomLipColor = SoftRedShadow,
                                cornerRadius = 12.dp,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("btn_grant_notifications")
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

            // Mascot & Speech Bubble Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                SavageBearView(
                    quote = uiState.bearQuote,
                    completedRatio = uiState.completionRatio,
                    onPokeBear = onPokeBear,
                    onTriggerSecretRapidTap = onTriggerSecretRapidTap
                )
            }

            // Daily Scorecard Card
            item {
                Tactile3DCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("card_progress_summary"),
                    containerColor = SoftSurfaceWhite,
                    borderColor = Color(0xFFE2E8F0),
                    bottomLipColor = Color(0xFFE2E8F0),
                    cornerRadius = 24.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DAILY SCORECARD",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.6.sp,
                                fontSize = 14.sp
                            ),
                            color = Color(0xFF1E293B)
                        )

                        Tactile3DBadge(
                            containerColor = if (uiState.completionRatio >= 1f && uiState.totalCount > 0) SoftGreenLight else Color(0xFFF1F5F9),
                            borderColor = if (uiState.completionRatio >= 1f && uiState.totalCount > 0) SoftGreenBorder else Color(0xFFE2E8F0),
                            bottomLipColor = if (uiState.completionRatio >= 1f && uiState.totalCount > 0) SoftGreenBorder else Color(0xFFE2E8F0),
                            shape = CircleShape,
                            lipHeight = 2.dp,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${uiState.completedCount} / ${uiState.totalCount} Completed",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                ),
                                color = if (uiState.completionRatio >= 1f && uiState.totalCount > 0) SoftGreenText else Color(0xFF475569)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Chunky Glossy Progress Bar + Milestone Ballot Box Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TactileGlossyProgressBar(
                            progress = uiState.completionRatio,
                            modifier = Modifier.weight(1f),
                            height = 16.dp
                        )

                        // Milestone Reward Badge with Ballot Box
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = if (uiState.completionRatio >= 1f && uiState.totalCount > 0) SoftYellow else Color(0xFFFEF3C7),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    color = Color(0xFFFCD34D),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "☒",
                                color = if (uiState.completionRatio >= 1f && uiState.totalCount > 0) Color.White else Color(0xFFF59E0B),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = when {
                            uiState.totalCount == 0 -> "☒ No tasks scheduled. The bear is watching!"
                            uiState.completionRatio >= 1f -> "☒ All tasks complete! Reminders paused for today."
                            else -> "☒ ${uiState.pendingCount} task(s) remaining. Reminder every ${uiState.notificationIntervalMinutes} min."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        ),
                        color = if (uiState.completionRatio >= 1f && uiState.totalCount > 0) SoftGreenText else Color(0xFF94A3B8),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Segmented Filter Tabs (All / Pending / Done)
            item {
                val navShape = RoundedCornerShape(18.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .background(Color(0xFFF1F5F9), shape = navShape)
                        .border(width = 2.dp, color = Color(0xFFE2E8F0), shape = navShape)
                        .padding(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterTabButton(
                            label = "All (${uiState.totalCount})",
                            isSelected = uiState.filter == TaskFilter.ALL,
                            onClick = { onSetFilter(TaskFilter.ALL) },
                            testTag = "filter_all",
                            modifier = Modifier.weight(1f)
                        )

                        FilterTabButton(
                            label = "Pending (${uiState.pendingCount})",
                            isSelected = uiState.filter == TaskFilter.PENDING,
                            onClick = { onSetFilter(TaskFilter.PENDING) },
                            testTag = "filter_pending",
                            modifier = Modifier.weight(1f)
                        )

                        FilterTabButton(
                            label = "Done (${uiState.completedCount})",
                            isSelected = uiState.filter == TaskFilter.COMPLETED,
                            onClick = { onSetFilter(TaskFilter.COMPLETED) },
                            testTag = "filter_completed",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Empty State Card
            if (uiState.filteredItems.isEmpty()) {
                item {
                    Tactile3DCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("empty_state_card"),
                        containerColor = SoftSurfaceWhite,
                        borderColor = Color(0xFFE2E8F0),
                        bottomLipColor = Color(0xFFE2E8F0),
                        cornerRadius = 24.dp,
                        contentPadding = PaddingValues(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(SoftGreenLight, shape = CircleShape)
                                    .border(width = 2.dp, color = Color(0xFFDCFCE7), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = SoftGreen,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = if (uiState.filter == TaskFilter.ALL) "NO SCHEDULE ITEMS TODAY" else "NO ITEMS IN THIS VIEW",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    fontSize = 16.sp
                                ),
                                color = Color(0xFF1E293B),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Tap below to lock in commitments before the bear gets grumpy.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                ),
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.widthIn(max = 230.dp)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Tactile3DButton(
                                onClick = { showAddDialog = true },
                                containerColor = SoftGreen,
                                bottomLipColor = SoftGreenShadow,
                                cornerRadius = 16.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "+  ADD COMMITMENT",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Schedule Items List
            items(uiState.filteredItems, key = { it.id }) { item ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    ScheduleItemCard(
                        item = item,
                        onToggleCompleted = { onToggleCompleted(item) },
                        onEdit = { editingItem = item },
                        onDelete = {
                            if (uiState.confirmBeforeDelete) {
                                itemPendingDelete = item
                            } else {
                                onDeleteItem(item)
                            }
                        }
                    )
                }
            }
        }
    }

    // Delete Task Confirmation Popup
    itemPendingDelete?.let { itemToDelete ->
        DeleteTaskConfirmationDialog(
            item = itemToDelete,
            onConfirmDelete = {
                onDeleteItem(itemToDelete)
                itemPendingDelete = null
            },
            onDismiss = { itemPendingDelete = null }
        )
    }

    // Add Task Dialog
    if (showAddDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var isAllDay by remember { mutableStateOf(false) }
        var saveToCalendar by remember { mutableStateOf(false) }
        var selectedHour by remember { mutableIntStateOf(14) }
        var selectedMinute by remember { mutableIntStateOf(0) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "ADD SCHEDULE COMMITMENT",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = SoftTextPrimary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Commit to it. The bear will track whether you finish.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task or Event Title") },
                        placeholder = { Text("e.g. Finish client report, Workout") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftGreen,
                            unfocusedBorderColor = SoftBorder,
                            focusedTextColor = SoftTextPrimary,
                            unfocusedTextColor = SoftTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_task_title")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SaveToPhoneCalendarToggleRow(
                        checked = saveToCalendar,
                        onCheckedChange = { enabled ->
                            saveToCalendar = enabled
                            if (enabled && (!uiState.hasCalendarPermission || !uiState.hasWriteCalendarPermission)) {
                                onRequestCalendarPermission()
                            }
                        },
                        switchTestTag = "switch_save_to_calendar"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isAllDay,
                            onCheckedChange = { isAllDay = it },
                            colors = CheckboxDefaults.colors(checkedColor = SoftGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ALL-DAY TASK",
                            color = SoftTextPrimary,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }

                    if (!isAllDay) {
                        Spacer(modifier = Modifier.height(10.dp))

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
                                color = SoftTextPrimary
                            )
                            Tactile3DBadge(
                                containerColor = SoftGreenLight,
                                borderColor = SoftGreenBorder,
                                bottomLipColor = SoftGreenBorder,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = liveTimeStr,
                                    color = SoftGreenText,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

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
                Tactile3DButton(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            onAddTask(taskTitle, isAllDay, selectedHour, selectedMinute, saveToCalendar)
                            showAddDialog = false
                        }
                    },
                    containerColor = SoftGreen,
                    bottomLipColor = SoftGreenShadow,
                    cornerRadius = 14.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("btn_confirm_add_task")
                ) {
                    Text("ADD TO SCHEDULE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("CANCEL", color = SoftTextSecondary, fontWeight = FontWeight.Black)
                }
            },
            containerColor = SoftSurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Edit Task Dialog
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
                    color = SoftTextPrimary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Modify commitment details. The bear expects results.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Task or Event Title") },
                        placeholder = { Text("e.g. Finish client report, Workout") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftGreen,
                            unfocusedBorderColor = SoftBorder,
                            focusedTextColor = SoftTextPrimary,
                            unfocusedTextColor = SoftTextPrimary
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
                            colors = CheckboxDefaults.colors(checkedColor = SoftGreen),
                            modifier = Modifier.testTag("checkbox_edit_all_day")
                        )
                        Text(
                            text = "ALL-DAY COMMITMENT",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = SoftTextPrimary,
                            modifier = Modifier.clickable { editIsAllDay = !editIsAllDay }
                        )
                    }

                    if (!editIsAllDay) {
                        Spacer(modifier = Modifier.height(10.dp))

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
                                color = SoftTextPrimary
                            )
                            Tactile3DBadge(
                                containerColor = SoftGreenLight,
                                borderColor = SoftGreenBorder,
                                bottomLipColor = SoftGreenBorder,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = liveTimeStr,
                                    color = SoftGreenText,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

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
                Tactile3DButton(
                    onClick = {
                        if (editTitle.isNotBlank()) {
                            onEditTask(currentItem, editTitle, editIsAllDay, editHour, editMinute)
                            editingItem = null
                        }
                    },
                    containerColor = SoftGreen,
                    bottomLipColor = SoftGreenShadow,
                    cornerRadius = 14.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("btn_confirm_edit_task")
                ) {
                    Text("SAVE CHANGES", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) {
                    Text("CANCEL", color = SoftTextSecondary, fontWeight = FontWeight.Black)
                }
            },
            containerColor = SoftSurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun FilterTabButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val tabShape = RoundedCornerShape(13.dp)
    if (isSelected) {
        Box(
            modifier = modifier
                .background(Color(0xFFE2E8F0), shape = tabShape)
                .padding(bottom = 2.5.dp)
                .background(SoftSurfaceWhite, shape = tabShape)
                .border(width = 1.5.dp, color = Color(0xFFE2E8F0), shape = tabShape)
                .clip(tabShape)
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp)
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                ),
                color = SoftGreenShadow
            )
        }
    } else {
        Box(
            modifier = modifier
                .clip(tabShape)
                .clickable(onClick = onClick)
                .padding(vertical = 9.dp)
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                ),
                color = Color(0xFF64748B)
            )
        }
    }
}
