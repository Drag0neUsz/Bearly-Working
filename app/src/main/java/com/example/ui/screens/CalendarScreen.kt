package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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

    val displayCalendar = remember {
        val cal = Calendar.getInstance()
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
    val currentMonth = displayCalendar.value.get(Calendar.MONTH)

    val monthYearTitle = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time).uppercase()
    }

    val daysInMonth = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeekOffset = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        cal.get(Calendar.DAY_OF_WEEK) - 1
    }

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
            SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(cal.time)
        } catch (_: Exception) {
            selectedDate
        }
    }

    val selectedDayItems = remember(uiState.allItems, selectedDate) {
        uiState.allItems.filter { it.dateString == selectedDate }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SoftCanvasBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            Tactile3DButton(
                onClick = { showAddDialog = true },
                containerColor = SoftNavy,
                bottomLipColor = SoftNavyShadow,
                cornerRadius = 16.dp,
                lipHeight = 4.dp,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .size(50.dp)
                    .testTag("fab_add_task_calendar")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Commitment For Date",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Header Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("header_calendar_page"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Calendar & Schedule",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            ),
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Browse & schedule past & future dates",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            ),
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Chunky 3D TODAY shortcut button
                    Tactile3DButton(
                        onClick = {
                            val now = Calendar.getInstance()
                            displayCalendar.value = (now.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
                            onSelectDate(todayDateString)
                        },
                        containerColor = SoftBlue,
                        bottomLipColor = SoftBlueShadow,
                        cornerRadius = 14.dp,
                        lipHeight = 3.5.dp,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("btn_jump_to_today")
                    ) {
                        Text(
                            text = "TODAY",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 0.6.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Month Navigator Card
            item {
                Tactile3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = SoftSurfaceWhite,
                    borderColor = SoftBorder,
                    bottomLipColor = SoftBorder,
                    cornerRadius = 20.dp,
                    lipHeight = 2.5.dp,
                    contentPadding = PaddingValues(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Month Button
                        Tactile3DButton(
                            onClick = {
                                val cal = displayCalendar.value.clone() as Calendar
                                cal.add(Calendar.MONTH, -1)
                                displayCalendar.value = cal
                            },
                            containerColor = SoftSurfaceSubtle,
                            bottomLipColor = SoftBorder,
                            borderColor = SoftBorder,
                            cornerRadius = 12.dp,
                            lipHeight = 3.dp,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("btn_prev_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Month",
                                tint = SoftTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Current Month Pill Badge
                        Tactile3DBadge(
                            containerColor = SoftBlueLight,
                            borderColor = SoftBlueBorder,
                            bottomLipColor = SoftBlueBorder,
                            shape = CircleShape,
                            lipHeight = 3.dp,
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = monthYearTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp,
                                    fontSize = 15.sp
                                ),
                                color = SoftBlueText
                            )
                        }

                        // Next Month Button
                        Tactile3DButton(
                            onClick = {
                                val cal = displayCalendar.value.clone() as Calendar
                                cal.add(Calendar.MONTH, 1)
                                displayCalendar.value = cal
                            },
                            containerColor = SoftSurfaceSubtle,
                            bottomLipColor = SoftBorder,
                            borderColor = SoftBorder,
                            cornerRadius = 12.dp,
                            lipHeight = 3.dp,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("btn_next_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next Month",
                                tint = SoftTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Calendar Grid Card
            item {
                Tactile3DCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_calendar_grid"),
                    containerColor = SoftSurfaceWhite,
                    borderColor = SoftBorder,
                    bottomLipColor = SoftBorder,
                    cornerRadius = 22.dp,
                    lipHeight = 2.5.dp,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    // Weekday Headers (SUN .. SAT)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        val daysOfWeek = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
                        for (dow in daysOfWeek) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dow,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    ),
                                    color = when (dow) {
                                        "SUN" -> SoftRed
                                        "SAT" -> SoftOrange
                                        else -> SoftTextMuted
                                    }
                                )
                            }
                        }
                    }

                    // Days Grid
                    val totalCells = firstDayOfWeekOffset + daysInMonth
                    val numRows = (totalCells + 6) / 7

                    for (row in 0 until numRows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
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

                                    val cellShape = RoundedCornerShape(12.dp)

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .padding(vertical = 2.dp)
                                            .then(
                                                when {
                                                    isSelected -> Modifier
                                                        .background(SoftNavyShadow, shape = cellShape)
                                                        .padding(bottom = 3.dp)
                                                        .background(SoftNavy, shape = cellShape)
                                                    isAllDone -> Modifier
                                                        .background(SoftGreenLight, shape = cellShape)
                                                        .border(width = 1.dp, color = SoftGreenBorder, shape = cellShape)
                                                    isToday -> Modifier
                                                        .background(SoftBlueLight, shape = cellShape)
                                                        .border(width = 1.5.dp, color = SoftBlueBorder, shape = cellShape)
                                                    else -> Modifier
                                                }
                                            )
                                            .clip(cellShape)
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
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 13.sp
                                                ),
                                                color = when {
                                                    isSelected -> Color.White
                                                    isAllDone -> SoftGreenText
                                                    isToday -> SoftBlueText
                                                    else -> SoftTextPrimary
                                                }
                                            )

                                            if (hasTasks) {
                                                Spacer(modifier = Modifier.height(1.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .background(
                                                            color = if (isAllDone) SoftGreen else SoftOrange,
                                                            shape = CircleShape
                                                        )
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Monthly Scorecard Card
            item {
                Tactile3DCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_month_summary"),
                    containerColor = SoftSurfaceWhite,
                    borderColor = SoftBorder,
                    bottomLipColor = SoftBorder,
                    cornerRadius = 20.dp,
                    lipHeight = 2.5.dp,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "☒",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = SoftOrange
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$monthYearTitle SCORECARD",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.6.sp,
                                    fontSize = 12.sp
                                ),
                                color = SoftTextPrimary
                            )
                        }

                        Tactile3DBadge(
                            containerColor = SoftGreenLight,
                            borderColor = SoftGreenBorder,
                            bottomLipColor = SoftGreenBorder,
                            shape = CircleShape,
                            lipHeight = 2.dp,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.5.dp)
                        ) {
                            Text(
                                text = "$monthCompleted / $monthTotal Done ($monthPercent%)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                ),
                                color = SoftGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TactileGlossyProgressBar(
                        progress = monthCompleted.toFloat() / monthTotal.coerceAtLeast(1),
                        modifier = Modifier.fillMaxWidth(),
                        height = 16.dp,
                        minVisualFraction = 0.14f
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Selected Date Action Card
            item {
                val isToday = selectedDate == todayDateString
                val hasItems = selectedDayItems.isNotEmpty()
                val isAllDone = hasItems && selectedDayItems.all { it.isCompleted }

                val bearAssessment = when {
                    isToday && isAllDone -> "100% done today! The bear begrudgingly admits you worked."
                    isToday && hasItems -> "Today's schedule is active. Time to execute your promises!"
                    isToday && !hasItems -> "Nothing scheduled for today. The bear is resting peacefully!"
                    isAllDone -> "Recorded victory! You finished every task on this date."
                    hasItems -> "Unfinished commitments logged in the schedule for this day."
                    else -> "Clean slate. No commitments scheduled on this date yet!"
                }

                Tactile3DCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("banner_selected_date"),
                    containerColor = SoftSurfaceWhite,
                    borderColor = SoftBorder,
                    bottomLipColor = SoftBorder,
                    cornerRadius = 22.dp,
                    lipHeight = 2.5.dp,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    // Date Header & Counter Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isToday) {
                                Box(
                                    modifier = Modifier
                                        .background(SoftNavyShadow, shape = RoundedCornerShape(7.dp))
                                        .padding(bottom = 2.dp)
                                        .background(SoftNavy, shape = RoundedCornerShape(7.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "TODAY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = selectedDateFormatted,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                ),
                                color = Color(0xFF334155)
                            )
                        }

                        Tactile3DBadge(
                            containerColor = SoftSurfaceSubtle,
                            borderColor = SoftBorder,
                            bottomLipColor = SoftBorder,
                            shape = CircleShape,
                            lipHeight = 2.5.dp,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${selectedDayItems.count { it.isCompleted }} / ${selectedDayItems.size}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                ),
                                color = SoftTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Mascot Status Message Box with Ballot Box Icon
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(14.dp))
                            .border(width = 1.dp, color = Color(0xFFE2E8F0), shape = RoundedCornerShape(14.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFFFEF3C7), shape = CircleShape)
                                .border(width = 1.dp, color = Color(0xFFFDE68A), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "☒",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = SoftOrange
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = bearAssessment,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                            color = Color(0xFF64748B),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dual Chunky Action Buttons (Sync Cal + Add Task)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (uiState.hasCalendarPermission) {
                            Tactile3DButton(
                                onClick = { onSyncCalendarForDate(selectedDate) },
                                containerColor = SoftSurfaceWhite,
                                bottomLipColor = Color(0xFF94A3B8),
                                borderColor = Color(0xFFCBD5E1),
                                cornerRadius = 14.dp,
                                lipHeight = 3.5.dp,
                                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_sync_selected_date")
                            ) {
                                Text(
                                    text = "☒",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sync Cal",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    ),
                                    color = Color(0xFF475569)
                                )
                            }
                        }

                        Tactile3DButton(
                            onClick = { showAddDialog = true },
                            containerColor = SoftGreen,
                            bottomLipColor = Color(0xFF388501),
                            borderColor = SoftGreenShadow,
                            cornerRadius = 14.dp,
                            lipHeight = 3.5.dp,
                            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_add_task_selected_date")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Add Task",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Commitments List or Empty State for Selected Date
            if (selectedDayItems.isEmpty()) {
                item {
                    Tactile3DCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("empty_state_calendar_date"),
                        containerColor = SoftSurfaceWhite,
                        borderColor = SoftBorder,
                        bottomLipColor = SoftBorder,
                        cornerRadius = 22.dp,
                        lipHeight = 2.5.dp,
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(SoftBlueLight, shape = RoundedCornerShape(16.dp))
                                    .border(width = 2.dp, color = SoftBlueBorder, shape = RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "☒",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SoftBlueText
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "NO COMMITMENTS FOR THIS DATE",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    letterSpacing = 0.4.sp
                                ),
                                color = Color(0xFF334155),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Plan ahead or log your friendly micro-goals for this date!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                ),
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.widthIn(max = 220.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(70.dp))
                }
            } else {
                items(selectedDayItems, key = { it.id }) { item ->
                    Box(modifier = Modifier.padding(vertical = 5.dp)) {
                        ScheduleItemCard(
                            item = item,
                            onToggleCompleted = { onToggleCompleted(item) },
                            onEdit = { editingItem = item },
                            onDelete = { onDeleteItem(item) }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(70.dp))
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
                    color = SoftTextPrimary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Add a task scheduled for $selectedDateFormatted.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task or Event Title") },
                        placeholder = { Text("e.g. Doctor appointment, Gym") },
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
                            .testTag("input_calendar_task_title")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                            "ALL-DAY TASK",
                            color = SoftTextPrimary,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
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
                            onAddTaskForDate(selectedDate, taskTitle, isAllDay, selectedHour, selectedMinute)
                            showAddDialog = false
                        }
                    },
                    containerColor = SoftGreen,
                    bottomLipColor = SoftGreenShadow,
                    cornerRadius = 14.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("btn_confirm_add_calendar_task")
                ) {
                    Text("ADD COMMITMENT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
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
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Task or Event Title") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftGreen,
                            unfocusedBorderColor = SoftBorder,
                            focusedTextColor = SoftTextPrimary,
                            unfocusedTextColor = SoftTextPrimary
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
                            colors = CheckboxDefaults.colors(checkedColor = SoftGreen)
                        )
                        Text(
                            text = "ALL-DAY COMMITMENT",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = SoftTextPrimary
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
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
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
