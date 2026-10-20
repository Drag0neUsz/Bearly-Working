package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SavageUiState
import kotlinx.coroutines.flow.distinctUntilChanged

enum class QuietTimePickerTarget {
    NONE,
    SLEEP,
    WAKE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SavageUiState,
    onSetInterval: (Int) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleQuietHours: (Boolean) -> Unit,
    onSetSleepTime: (Int, Int) -> Unit,
    onSetWakeTime: (Int, Int) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onTestNotification: () -> Unit,
    onClearRoastsBook: () -> Unit,
    onRequestCalendarPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showConfirmClearDialog by remember { mutableStateOf(false) }
    var activeTimePickerTarget by remember { mutableStateOf(QuietTimePickerTarget.NONE) }

    fun formatDisplayTime(hour: Int, minute: Int): String {
        val h = hour % 24
        val amPm = if (h < 12) "AM" else "PM"
        val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
        return String.format("%d:%02d %s", displayH, minute, amPm)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ComicWhiteBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Header Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ComicSurfaceWhite)
                    .border(width = 2.dp, color = ComicInkBlack)
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "SETTINGS & FREQUENCY",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        ),
                        color = ComicInkBlack
                    )
                    Text(
                        text = "Configure how often the bear roasts you until tasks are done.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ComicTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Master Push Notification Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ComicSurfaceWhite)
                    .border(width = 2.dp, color = ComicInkBlack)
                    .padding(14.dp)
                    .testTag("card_notification_toggle")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = ComicInkBlack,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "RELENTLESS NOTIFICATIONS",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                    color = ComicInkBlack
                                )
                                Text(
                                    text = "Pokes you until 100% of tasks are completed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ComicTextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = {
                                if (it && !uiState.hasNotificationPermission) {
                                    onRequestNotificationPermission()
                                }
                                onToggleNotifications(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ComicInkBlack
                            ),
                            modifier = Modifier.testTag("switch_notifications")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Test Notification Button (Custom 3-short + 1-long pattern)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ComicAccentRed)
                            .clickable {
                                if (!uiState.hasNotificationPermission) {
                                    onRequestNotificationPermission()
                                }
                                onTestNotification()
                            }
                            .padding(vertical = 12.dp)
                            .testTag("btn_test_notification"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TEST SAVAGE NOTIFICATION NOW",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Notification Interval Frame (Default 120m)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ComicSurfaceWhite)
                    .border(width = 2.dp, color = ComicInkBlack)
                    .padding(14.dp)
                    .testTag("card_interval_settings")
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = ComicInkBlack,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "POKE INTERVAL",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                            color = ComicInkBlack
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "EVERY ${uiState.notificationIntervalMinutes} MINUTES",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = ComicAccentRed
                    )
                    Text(
                        text = "Sends a new uncensored callout at this interval until everything is marked done.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ComicTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val intervals = listOf(15, 30, 45, 60, 90, 120)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.5.dp, color = ComicInkBlack)
                    ) {
                        intervals.take(3).forEachIndexed { index, mins ->
                            val isSelected = uiState.notificationIntervalMinutes == mins
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelected) ComicInkBlack else ComicSurfaceWhite)
                                    .clickable { onSetInterval(mins) }
                                    .padding(vertical = 10.dp)
                                    .testTag("chip_interval_$mins"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${mins}M",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color.White else ComicInkBlack
                                )
                            }
                            if (index < 2) {
                                Box(
                                    modifier = Modifier
                                        .width(1.5.dp)
                                        .height(38.dp)
                                        .background(ComicInkBlack)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.5.dp, color = ComicInkBlack)
                    ) {
                        intervals.drop(3).forEachIndexed { index, mins ->
                            val isSelected = uiState.notificationIntervalMinutes == mins
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelected) ComicInkBlack else ComicSurfaceWhite)
                                    .clickable { onSetInterval(mins) }
                                    .padding(vertical = 10.dp)
                                    .testTag("chip_interval_$mins"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${mins}M",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color.White else ComicInkBlack
                                )
                            }
                            if (index < 2) {
                                Box(
                                    modifier = Modifier
                                        .width(1.5.dp)
                                        .height(38.dp)
                                        .background(ComicInkBlack)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Slider(
                        value = uiState.notificationIntervalMinutes.toFloat(),
                        onValueChange = { onSetInterval(it.toInt()) },
                        valueRange = 5f..180f,
                        steps = 34,
                        colors = SliderDefaults.colors(
                            thumbColor = ComicInkBlack,
                            activeTrackColor = ComicInkBlack,
                            inactiveTrackColor = ComicBorderSubtle
                        ),
                        modifier = Modifier.testTag("slider_interval")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quiet Hours Frame (Defaults to ON, Clickable Sleep/Wake Time boxes open separate iPhone-style wheel popups)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ComicSurfaceWhite)
                    .border(width = 2.dp, color = ComicInkBlack)
                    .padding(14.dp)
                    .testTag("card_quiet_hours")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = ComicInkBlack,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "QUIET HOURS (SLEEP MODE)",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                    color = ComicInkBlack
                                )
                                Text(
                                    text = if (uiState.quietHoursEnabled)
                                        "Mutes from ${formatDisplayTime(uiState.quietStartHour, uiState.quietStartMinute)} to ${formatDisplayTime(uiState.quietEndHour, uiState.quietEndMinute)}"
                                    else "Off (24/7 alerts)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ComicTextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = uiState.quietHoursEnabled,
                            onCheckedChange = onToggleQuietHours,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ComicInkBlack
                            ),
                            modifier = Modifier.testTag("switch_quiet_hours")
                        )
                    }

                    if (uiState.quietHoursEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Separate Clickable Sleep Time & Wake Time Boxes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Sleep Time Frame (Clickable -> Opens Sleep Time Picker)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(width = 2.dp, color = ComicInkBlack)
                                    .background(ComicSurfaceSubtle)
                                    .clickable { activeTimePickerTarget = QuietTimePickerTarget.SLEEP }
                                    .padding(vertical = 12.dp, horizontal = 10.dp)
                                    .testTag("btn_pick_sleep_time"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Bedtime, contentDescription = null, modifier = Modifier.size(12.dp), tint = ComicTextSecondary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("SLEEP TIME", fontWeight = FontWeight.Black, color = ComicTextSecondary, fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = String.format("%02d:%02d", uiState.quietStartHour, uiState.quietStartMinute),
                                        fontWeight = FontWeight.Black,
                                        color = ComicInkBlack,
                                        fontSize = 18.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = formatDisplayTime(uiState.quietStartHour, uiState.quietStartMinute),
                                        fontWeight = FontWeight.Bold,
                                        color = ComicTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Wake Time Frame (Clickable -> Opens Wake Time Picker)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(width = 2.dp, color = ComicInkBlack)
                                    .background(ComicSurfaceSubtle)
                                    .clickable { activeTimePickerTarget = QuietTimePickerTarget.WAKE }
                                    .padding(vertical = 12.dp, horizontal = 10.dp)
                                    .testTag("btn_pick_wake_time"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(12.dp), tint = ComicTextSecondary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("WAKE TIME", fontWeight = FontWeight.Black, color = ComicTextSecondary, fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = String.format("%02d:%02d", uiState.quietEndHour, uiState.quietEndMinute),
                                        fontWeight = FontWeight.Black,
                                        color = ComicInkBlack,
                                        fontSize = 18.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = formatDisplayTime(uiState.quietEndHour, uiState.quietEndMinute),
                                        fontWeight = FontWeight.Bold,
                                        color = ComicTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Vibration Toggle Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ComicSurfaceWhite)
                    .border(width = 2.dp, color = ComicInkBlack)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = ComicInkBlack,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "VIBRATE ON PUSH ROAST",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                color = ComicInkBlack
                            )
                            Text(
                                text = "3-short + 1-long vibration pattern",
                                style = MaterialTheme.typography.bodySmall,
                                color = ComicTextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = uiState.vibrateEnabled,
                        onCheckedChange = onToggleVibration,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ComicInkBlack
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // DANGER ZONE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF2F2))
                    .border(width = 2.dp, color = ComicAccentRed)
                    .padding(14.dp)
                    .testTag("card_danger_zone")
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Danger Zone",
                            tint = ComicAccentRed,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DANGER ZONE",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = ComicAccentRed
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "CLEAR ROAST COMPLETION BOOK",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = ComicInkBlack
                    )
                    Text(
                        text = "Reset all unlocked roasts and discovered secrets back to '???'. This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ComicTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ComicAccentRed)
                            .clickable { showConfirmClearDialog = true }
                            .padding(vertical = 12.dp)
                            .testTag("btn_clear_roasts_book"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RESET ROAST COMPLETION BOOK",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // =========================================================================
    // IPHONE-STYLE SCROLLABLE WHEEL PICKER POPUPS (SLEEP TIME OR WAKE TIME)
    // =========================================================================
    if (activeTimePickerTarget != QuietTimePickerTarget.NONE) {
        val isSleep = activeTimePickerTarget == QuietTimePickerTarget.SLEEP
        val initialHour = if (isSleep) uiState.quietStartHour else uiState.quietEndHour
        val initialMinute = if (isSleep) uiState.quietStartMinute else uiState.quietEndMinute
        val title = if (isSleep) "SET SLEEP TIME" else "SET WAKE TIME"

        IosAlarmStyleWheelTimeDialog(
            title = title,
            initialHour = initialHour,
            initialMinute = initialMinute,
            onConfirm = { h, m ->
                if (isSleep) {
                    onSetSleepTime(h, m)
                } else {
                    onSetWakeTime(h, m)
                }
                activeTimePickerTarget = QuietTimePickerTarget.NONE
            },
            onDismiss = { activeTimePickerTarget = QuietTimePickerTarget.NONE }
        )
    }

    // Confirmation Dialog for Danger Zone
    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            title = {
                Text(
                    text = "RESET ALL ROASTS?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = ComicAccentRed
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to clear your collection book? All unlocked roasts will be locked back to '???' and the secrets category will be hidden again.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ComicInkBlack
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .background(ComicAccentRed)
                        .clickable {
                            onClearRoastsBook()
                            showConfirmClearDialog = false
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("btn_confirm_reset_roasts")
                ) {
                    Text(
                        text = "YES, RESET EVERYTHING",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text("CANCEL", color = ComicTextSecondary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = ComicSurfaceWhite,
            shape = RectangleShape
        )
    }
}

/**
 * iPhone Alarm Style Scrollable Hour:Minute Wheel Picker Dialog.
 * Vibrates slightly on every single number scrolled!
 */
@Composable
fun IosAlarmStyleWheelTimeDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    val amPm = if (selectedHour < 12) "AM" else "PM"
    val displayH = if (selectedHour == 0) 12 else if (selectedHour > 12) selectedHour - 12 else selectedHour
    val liveDisplayFormatted = String.format("%d:%02d %s", displayH, selectedMinute, amPm)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = ComicInkBlack
                )
                Box(
                    modifier = Modifier
                        .background(ComicInkBlack)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = liveDisplayFormatted,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Scroll wheels to set time. Ticks with subtle vibration.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ComicTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Column Headers: HOUR  :  MINUTE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "HOUR (00-23)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = ComicTextSecondary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(28.dp))
                    Text(
                        text = "MIN (00-59)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = ComicTextSecondary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // The iPhone-style Drum Wheel Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(144.dp) // 3 rows of 48.dp
                        .background(Color(0xFFF1F5F9))
                        .border(width = 2.dp, color = ComicInkBlack),
                    contentAlignment = Alignment.Center
                ) {
                    // Center Selection Highlight Band (Between top and bottom divider lines like iOS picker)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color.White)
                            .border(width = 1.5.dp, color = ComicInkBlack)
                    )

                    // Columns: Hours : Minutes
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Hour Wheel Column
                        WheelColumn(
                            range = 0..23,
                            initialValue = initialHour,
                            onValueChange = { selectedHour = it },
                            modifier = Modifier.weight(1f)
                        )

                        // Center Colon Divider
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 28.sp
                            ),
                            color = ComicInkBlack,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // Minute Wheel Column
                        WheelColumn(
                            range = 0..59,
                            initialValue = initialMinute,
                            onValueChange = { selectedMinute = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .background(ComicInkBlack)
                    .clickable { onConfirm(selectedHour, selectedMinute) }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
                    .testTag("btn_confirm_wheel_time")
            ) {
                Text("DONE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = ComicTextSecondary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = ComicSurfaceWhite,
        shape = RectangleShape
    )
}

/**
 * Single Wheel Column for iPhone style drum picker.
 * Emits a very slight single vibration tick when scrolling past each number!
 */
@Composable
fun WheelColumn(
    range: IntRange,
    initialValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemsList = remember(range) { range.toList() }
    val initialIndex = itemsList.indexOf(initialValue).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val view = LocalView.current
    val context = LocalContext.current
    var isInitialLaunch by remember { mutableStateOf(true) }

    // Trigger very slight single vibration after every number scrolls
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                if (index in itemsList.indices) {
                    onValueChange(itemsList[index])

                    if (isInitialLaunch) {
                        isInitialLaunch = false
                    } else {
                        // Very slight single tick vibration on scroll
                        try {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            @Suppress("DEPRECATION")
                            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator?.vibrate(VibrationEffect.createOneShot(8, 40))
                            } else {
                                vibrator?.vibrate(8)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
    }

    Box(
        modifier = modifier.height(144.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = 48.dp), // Centers the selected 48dp row
            modifier = Modifier.fillMaxSize()
        ) {
            items(itemsList.size) { index ->
                val num = itemsList[index]
                val isSelected = listState.firstVisibleItemIndex == index

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d", num),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            fontSize = if (isSelected) 24.sp else 18.sp
                        ),
                        color = if (isSelected) ComicInkBlack else ComicTextMuted.copy(alpha = 0.45f)
                    )
                }
            }
        }
    }
}
