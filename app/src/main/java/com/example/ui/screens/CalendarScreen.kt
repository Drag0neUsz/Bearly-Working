package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.ui.components.ScheduleItemCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.SavageUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    uiState: SavageUiState,
    onSelectDate: (String) -> Unit,
    onToggleCompleted: (ScheduleItem) -> Unit,
    onDeleteItem: (ScheduleItem) -> Unit,
    onAddTaskForDate: (dateString: String, title: String, isAllDay: Boolean, hour: Int, minute: Int) -> Unit,
    onEditTask: (ScheduleItem, String, Boolean, Int, Int) -> Unit,
    onSyncCalendarForDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayDateString = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ScheduleItem?>(null) }

    // Calendar navigation state (current displayed month/year)
    val displayCalendar = remember {
        val cal = Calendar.getInstance()
        // If a date is currently selected in uiState, initialize to that month
        if (uiState.selectedCalendarDate.isNotBlank()) {
            try {
                val parts = uiState.selectedCalendarDate.split("-")
                if (parts.size == 3) {
                    cal.set(Calendar.YEAR, parts[0].toInt())
                    cal.set(Calendar.MONTH, parts[1].toInt() - 1)
                    cal.set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                }
            } catch (_: Exception) {}
        }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        mutableStateOf(cal)
    }

    val currentYear = displayCalendar.value.get(Calendar.YEAR)
    val currentMonth = displayCalendar.value.get(Calendar.MONTH) // 0-indexed

    val monthYearTitle = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time).uppercase()
    }

    // Days in current month
    val daysInMonth = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    // Day of week of 1st day (Calendar.SUNDAY = 1 .. SATURDAY = 7)
    val firstDayOfWeekOffset = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday, 1 for Monday, etc.
    }

    // Items grouped by date for this month
    val monthPrefix = String.format("%04d-%02d", currentYear, currentMonth + 1)
    val monthItems = remember(uiState.allItems, monthPrefix) {
        uiState.allItems.filter { it.dateString.startsWith(monthPrefix) }
    }
    val monthTotal = monthItems.size
    val monthCompleted = monthItems.count { it.isCompleted }
    val monthPercent = if (monthTotal == 0) 0 else ((monthCompleted.toFloat() / monthTotal) * 100).toInt()

    val selectedDate = if (uiState.selectedCalendarDate.isNotBlank()) uiState.selectedCalendarDate else todayDateString

    val selectedDateFormatted = remember(selectedDate) {
        try {
            val parts = selectedDate.split("-")
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, parts[0].toInt())
                set(Calendar.MONTH, parts[1].toInt() - 1)
                set(Calendar.DAY_OF_MONTH, parts[2].toInt())
            }
            SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(cal.time).uppercase()
        } catch (_: Exception) {
            selectedDate
        }
    }

    val selectedDayItems = remember(uiState.allItems, selectedDate) {
        uiState.allItems.filter { it.dateString == selectedDate }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ComicWhiteBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(ComicInkBlack, shape = RectangleShape)
                    .border(width = 2.dp, color = ComicInkBlack, shape = RectangleShape)
                    .clickable { showAddDialog = true }
                    .testTag("fab_add_task_calendar"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Commitment For Date",
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
            // Header Frame
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ComicSurfaceWhite)
                        .border(width = 2.dp, color = ComicInkBlack)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("header_calendar_page")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CALENDAR & SCHEDULE",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp,
                                    fontSize = 20.sp
                                ),
                                color = ComicInkBlack
                            )
                            Text(
                                text = "BROWSE AND SCHEDULE PAST & FUTURE DATES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                ),
                                color = ComicTextSecondary
                            )
                        }

                        // Jump to today button
                        Box(
                            modifier = Modifier
                                .border(width = 1.5.dp, color = ComicInkBlack)
                                .background(if (selectedDate == todayDateString) ComicInkBlack else ComicSurfaceSubtle)
                                .clickable {
                                    val now = Calendar.getInstance()
                                    displayCalendar.value = (now.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
                                    onSelectDate(todayDateString)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("btn_jump_to_today")
                        ) {
                            Text(
                                text = "TODAY",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = if (selectedDate == todayDateString) Color.White else ComicInkBlack
                            )
                        }
                    }
                }
            }

            // Month Navigation Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(ComicInkBlack)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val cal = displayCalendar.value.clone() as Calendar
                                cal.add(Calendar.MONTH, -1)
                                displayCalendar.value = cal
                            },
                            modifier = Modifier.size(36.dp).testTag("btn_prev_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Month",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = monthYearTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            ),
                            color = Color.White
                        )

                        IconButton(
                            onClick = {
                                val cal = displayCalendar.value.clone() as Calendar
                                cal.add(Calendar.MONTH, 1)
                                displayCalendar.value = cal
                            },
                            modifier = Modifier.size(36.dp).testTag("btn_next_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Month",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Month Grid Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .background(ComicSurfaceWhite)
                        .border(width = 2.dp, color = ComicInkBlack)
                        .padding(10.dp)
                        .testTag("card_calendar_grid")
                ) {
                    Column {
                        // Weekday labels (SUN .. SAT)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val daysOfWeek = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
                            for (dow in daysOfWeek) {
                                Box(
                                    modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dow,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp
                                        ),
                                        color = if (dow == "SUN" || dow == "SAT") ComicAccentRed else ComicInkBlack
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Days grid
                        val totalCells = firstDayOfWeekOffset + daysInMonth
                        val numRows = (totalCells + 6) / 7

                        for (row in 0 until numRows) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (col in 0 until 7) {
                                    val cellIndex = row * 7 + col
                                    val dayNumber = cellIndex - firstDayOfWeekOffset + 1

                                    if (dayNumber in 1..daysInMonth) {
                                        val dateStr = String.format("%04d-%02d-%02d", currentYear, currentMonth + 1, dayNumber)
                                        val isSelected = dateStr == selectedDate
                                        val isToday = dateStr == todayDateString

                                        val dayItems = uiState.allItems.filter { it.dateString == dateStr }
                                        val dayTotal = dayItems.size
                                        val dayCompleted = dayItems.count { it.isCompleted }
                                        val hasTasks = dayTotal > 0
                                        val isAllDone = hasTasks && dayCompleted == dayTotal

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(2.dp)
                                                .background(
                                                    when {
                                                        isSelected -> ComicInkBlack
                                                        isToday -> Color(0xFFFEF3C7)
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .border(
                                                    width = if (isToday) 2.dp else if (isSelected) 2.dp else 1.dp,
                                                    color = when {
                                                        isSelected -> ComicInkBlack
                                                        isToday -> ComicInkBlack
                                                        else -> ComicBorderSubtle
                                                    }
                                                )
                                                .clickable { onSelectDate(dateStr) }
                                                .testTag("day_cell_$dateStr"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = dayNumber.toString(),
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelected || isToday) FontWeight.Black else FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    ),
                                                    color = when {
                                                        isSelected -> Color.White
                                                        else -> ComicInkBlack
                                                    }
                                                )

                                                // Status dot or badge
                                                if (hasTasks) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(
                                                                color = when {
                                                                    isAllDone -> ComicAccentGreen
                                                                    isSelected -> Color(0xFFF87171)
                                                                    else -> ComicAccentRed
                                                                },
                                                                shape = CircleShape
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // Empty cell for offset
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Monthly Scorecard Summary
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .background(ComicSurfaceWhite)
                        .border(width = 2.dp, color = ComicInkBlack)
                        .padding(12.dp)
                        .testTag("card_month_summary")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$monthYearTitle SCORECARD",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp
                                ),
                                color = ComicInkBlack
                            )
                            Text(
                                text = "$monthCompleted / $monthTotal DONE ($monthPercent%)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = if (monthPercent >= 100 && monthTotal > 0) ComicAccentGreen else ComicInkBlack
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(ComicSurfaceSubtle)
                                .border(width = 1.dp, color = ComicInkBlack)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = (monthCompleted.toFloat() / monthTotal.coerceAtLeast(1)).coerceIn(0f, 1f))
                                    .background(if (monthPercent >= 100 && monthTotal > 0) ComicAccentGreen else ComicInkBlack)
                            )
                        }
                    }
                }
            }

            // Selected Date Banner & Bear Commentary
            item {
                val isToday = selectedDate == todayDateString
                val hasItems = selectedDayItems.isNotEmpty()
                val isAllDone = hasItems && selectedDayItems.all { it.isCompleted }

                val bearAssessment = when {
                    isToday && isAllDone -> "🎉 100% DONE TODAY! THE BEAR BEGRUDGINGLY ADMITS YOU WORKED."
                    isToday && hasItems -> "⚠️ TODAY'S SCHEDULE IS ACTIVE. GET OFF THE CALENDAR AND EXECUTE."
                    isToday && !hasItems -> "NOTHING SCHEDULED FOR TODAY. THE BEAR IS WAITING."
                    isAllDone -> "RECORDED VICTORY: YOU ACTUALLY FINISHED EVERY TASK ON THIS DATE."
                    hasItems -> "UNFINISHED PROMISES DOCUMENTED IN THE ARCHIVES FOR THIS DAY."
                    else -> "CLEAN SLATE. NO COMMITMENTS SCHEDULED ON THIS DATE."
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .background(if (isAllDone) Color(0xFFF0FDF4) else ComicSurfaceWhite)
                        .border(width = 2.dp, color = if (isAllDone) ComicAccentGreen else ComicInkBlack)
                        .padding(14.dp)
                        .testTag("banner_selected_date")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isToday) {
                                    Box(
                                        modifier = Modifier
                                            .background(ComicInkBlack)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "TODAY",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = selectedDateFormatted,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = ComicInkBlack
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .border(width = 1.dp, color = ComicInkBlack)
                                    .background(ComicSurfaceSubtle)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${selectedDayItems.count { it.isCompleted }} / ${selectedDayItems.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = ComicInkBlack
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = bearAssessment,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (isAllDone) ComicAccentGreen else ComicTextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons Bar (Sync & Add)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uiState.hasCalendarPermission) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(width = 1.5.dp, color = ComicInkBlack)
                                        .background(ComicSurfaceSubtle)
                                        .clickable { onSyncCalendarForDate(selectedDate) }
                                        .padding(vertical = 8.dp)
                                        .testTag("btn_sync_selected_date"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Sync,
                                            contentDescription = null,
                                            tint = ComicInkBlack,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "SYNC DEVICE CAL",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            color = ComicInkBlack
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(ComicInkBlack)
                                    .clickable { showAddDialog = true }
                                    .padding(vertical = 8.dp)
                                    .testTag("btn_add_task_selected_date"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ADD COMMITMENT",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // List of items for selected date
            if (selectedDayItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .background(ComicSurfaceWhite)
                            .border(width = 2.dp, color = ComicInkBlack)
                            .padding(24.dp)
                            .testTag("empty_state_calendar_date"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = ComicInkBlack,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "NO COMMITMENTS FOR THIS DATE",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = ComicInkBlack
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Plan ahead or log your tasks for this date.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ComicTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(selectedDayItems, key = { it.id }) { item ->
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
    }

    // Add Task Dialog for Selected Date
    if (showAddDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var isAllDay by remember { mutableStateOf(false) }
        var selectedHour by remember { mutableIntStateOf(12) }
        var selectedMinute by remember { mutableIntStateOf(0) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "ADD COMMITMENT ($selectedDate)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = ComicInkBlack
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Add a task scheduled for $selectedDateFormatted.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ComicTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task or Event Title") },
                        placeholder = { Text("e.g. Doctor appointment, Gym") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ComicInkBlack,
                            unfocusedBorderColor = ComicBorderSubtle,
                            focusedTextColor = ComicInkBlack,
                            unfocusedTextColor = ComicInkBlack
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_calendar_task_title")
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
                        Text(
                            "ALL-DAY TASK",
                            color = ComicInkBlack,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    if (!isAllDay) {
                        Spacer(modifier = Modifier.height(8.dp))

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
                                onAddTaskForDate(selectedDate, taskTitle, isAllDay, selectedHour, selectedMinute)
                                showAddDialog = false
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("btn_confirm_add_calendar_task")
                ) {
                    Text("ADD COMMITMENT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
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
                    color = ComicInkBlack
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Task or Event Title") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ComicInkBlack,
                            unfocusedBorderColor = ComicBorderSubtle,
                            focusedTextColor = ComicInkBlack,
                            unfocusedTextColor = ComicInkBlack
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = editIsAllDay,
                            onCheckedChange = { editIsAllDay = it },
                            colors = CheckboxDefaults.colors(checkedColor = ComicInkBlack)
                        )
                        Text(
                            text = "ALL-DAY COMMITMENT",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = ComicInkBlack
                        )
                    }

                    if (!editIsAllDay) {
                        Spacer(modifier = Modifier.height(8.dp))
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
