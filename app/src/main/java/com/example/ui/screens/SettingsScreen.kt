package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SavageUiState

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
        containerColor = SoftCanvasBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sticky Top Header Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoftSurfaceWhite)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "Settings & Frequency",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        ),
                        color = SoftTextPrimary
                    )
                    Text(
                        text = "Configure how often the bear roasts you",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = SoftTextMuted
                    )
                }
                HorizontalDivider(thickness = 2.dp, color = SoftBorder)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // =============================================================
                // CARD 1: RELENTLESS POKES
                // =============================================================
                Tactile3DCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_notification_toggle"),
                    containerColor = SoftSurfaceWhite,
                    borderColor = SoftBorder,
                    bottomLipColor = SoftBorder,
                    cornerRadius = 24.dp,
                    lipHeight = 3.dp,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFFFFFBEB), shape = RoundedCornerShape(14.dp))
                                    .border(width = 2.dp, color = Color(0xFFFDE68A), shape = RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "☒",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "RELENTLESS POKES",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp,
                                        fontSize = 15.sp
                                    ),
                                    color = SoftTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Pokes you continuously until 100% of tasks are completed.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    ),
                                    color = SoftTextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        TactileToggleSwitch(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = {
                                if (it && !uiState.hasNotificationPermission) {
                                    onRequestNotificationPermission()
                                }
                                onToggleNotifications(it)
                            },
                            modifier = Modifier.testTag("switch_notifications")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3D Action Button: Test Savage Notification Now
                    Tactile3DButton(
                        onClick = {
                            if (!uiState.hasNotificationPermission) {
                                onRequestNotificationPermission()
                            }
                            onTestNotification()
                        },
                        containerColor = SoftOrange,
                        bottomLipColor = SoftOrangeShadow,
                        borderColor = SoftOrangeShadow,
                        cornerRadius = 16.dp,
                        lipHeight = 4.5.dp,
                        contentPadding = PaddingValues(vertical = 13.dp, horizontal = 16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_test_notification")
                    ) {
                        Text(
                            text = "☒",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TEST SAVAGE NOTIFICATION NOW",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.6.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                // =============================================================
                // CARD 2: POKE INTERVAL
                // =============================================================
                Tactile3DCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_interval_settings"),
                    containerColor = SoftSurfaceWhite,
                    borderColor = SoftBorder,
                    bottomLipColor = SoftBorder,
                    cornerRadius = 24.dp,
                    lipHeight = 3.dp,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(0xFFEFF6FF), shape = RoundedCornerShape(12.dp))
                                    .border(width = 2.dp, color = Color(0xFFBFDBFE), shape = RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "☒",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3B82F6)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "POKE INTERVAL",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp,
                                        fontSize = 14.sp
                                    ),
                                    color = SoftTextPrimary
                                )
                                Text(
                                    text = "Uncensored callouts timing",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = SoftTextMuted
                                )
                            }
                        }

                        Tactile3DBadge(
                            containerColor = SoftOrangeLight,
                            borderColor = SoftOrangeBorder,
                            bottomLipColor = SoftOrangeBorder,
                            shape = RoundedCornerShape(12.dp),
                            lipHeight = 3.dp,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "EVERY ${uiState.notificationIntervalMinutes} MIN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = SoftOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Sends a fresh roast at this interval until everything is marked done.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = SoftTextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3x2 Grid of Tactile 3D Interval Buttons
                    val intervals = listOf(15, 30, 45, 60, 90, 120)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (rowIntervals in intervals.chunked(3)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (mins in rowIntervals) {
                                    val isSelected = uiState.notificationIntervalMinutes == mins
                                    Tactile3DButton(
                                        onClick = { onSetInterval(mins) },
                                        containerColor = if (isSelected) SoftGreen else SoftSurfaceWhite,
                                        bottomLipColor = if (isSelected) SoftGreenShadow else SoftBorder,
                                        borderColor = if (isSelected) SoftGreenShadow else SoftBorder,
                                        cornerRadius = 16.dp,
                                        lipHeight = 3.5.dp,
                                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("chip_interval_$mins")
                                    ) {
                                        Text(
                                            text = if (isSelected) "${mins}M ✓" else "${mins}M",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp
                                            ),
                                            color = if (isSelected) Color.White else Color(0xFF4B4B4B)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Custom Fine-Tune Slider Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftSurfaceSubtle, shape = RoundedCornerShape(18.dp))
                            .border(width = 2.dp, color = SoftBorder, shape = RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "☒ CUSTOM FINE-TUNE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.6.sp,
                                    fontSize = 11.sp
                                ),
                                color = SoftTextSecondary
                            )

                            Tactile3DBadge(
                                containerColor = Color(0xFFECFDF5),
                                borderColor = Color(0xFF6EE7B7),
                                bottomLipColor = Color(0xFF6EE7B7),
                                shape = RoundedCornerShape(10.dp),
                                lipHeight = 2.5.dp,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.5.dp)
                            ) {
                                Text(
                                    text = "${uiState.notificationIntervalMinutes} Min",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    ),
                                    color = SoftGreen
                                )
                            }
                        }

                        Slider(
                            value = uiState.notificationIntervalMinutes.toFloat(),
                            onValueChange = { onSetInterval(it.toInt()) },
                            valueRange = 5f..180f,
                            steps = 34,
                            colors = SliderDefaults.colors(
                                thumbColor = SoftGreen,
                                activeTrackColor = SoftGreen,
                                inactiveTrackColor = SoftBorder
                            ),
                            modifier = Modifier.testTag("slider_interval")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "5m min",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                ),
                                color = SoftTextMuted,
                                modifier = Modifier.clickable { onSetInterval(5) }
                            )
                            Text(
                                text = "DRAG FOR CUSTOM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.6.sp
                                ),
                                color = SoftTextMuted
                            )
                            Text(
                                text = "180m max",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                ),
                                color = SoftTextMuted,
                                modifier = Modifier.clickable { onSetInterval(180) }
                            )
                        }
                    }
                }

                // =============================================================
                // CARD 3: QUIET HOURS
                // =============================================================
                Tactile3DCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_quiet_hours"),
                    containerColor = SoftSurfaceWhite,
                    borderColor = SoftBorder,
                    bottomLipColor = SoftBorder,
                    cornerRadius = 24.dp,
                    lipHeight = 3.dp,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(SoftIndigoLight, shape = RoundedCornerShape(14.dp))
                                    .border(width = 2.dp, color = SoftIndigoBorder, shape = RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "☒",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftIndigo
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "QUIET HOURS",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp,
                                        fontSize = 15.sp
                                    ),
                                    color = SoftTextPrimary
                                )
                                Text(
                                    text = if (uiState.quietHoursEnabled)
                                        "Sleep mode: Mutes ${String.format("%02d:%02d", uiState.quietStartHour, uiState.quietStartMinute)} to ${String.format("%02d:%02d", uiState.quietEndHour, uiState.quietEndMinute)}"
                                    else "Off (24/7 alerts)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    color = SoftTextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        TactileToggleSwitch(
                            checked = uiState.quietHoursEnabled,
                            onCheckedChange = onToggleQuietHours,
                            modifier = Modifier.testTag("switch_quiet_hours")
                        )
                    }

                    if (uiState.quietHoursEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Sleep Time Card
                            Tactile3DCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_pick_sleep_time"),
                                containerColor = SoftSurfaceSubtle,
                                borderColor = SoftBorder,
                                bottomLipColor = SoftBorder,
                                cornerRadius = 18.dp,
                                lipHeight = 3.5.dp,
                                onClick = { activeTimePickerTarget = QuietTimePickerTarget.SLEEP },
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "☒ SLEEP TIME",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.6.sp,
                                            fontSize = 10.sp
                                        ),
                                        color = SoftTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = String.format("%02d:%02d", uiState.quietStartHour, uiState.quietStartMinute),
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 24.sp
                                        ),
                                        color = SoftTextPrimary
                                    )
                                    Text(
                                        text = formatDisplayTime(uiState.quietStartHour, uiState.quietStartMinute),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = SoftTextMuted
                                    )
                                }
                            }

                            // Wake Time Card
                            Tactile3DCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_pick_wake_time"),
                                containerColor = SoftSurfaceSubtle,
                                borderColor = SoftBorder,
                                bottomLipColor = SoftBorder,
                                cornerRadius = 18.dp,
                                lipHeight = 3.5.dp,
                                onClick = { activeTimePickerTarget = QuietTimePickerTarget.WAKE },
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "☒ WAKE TIME",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.6.sp,
                                            fontSize = 10.sp
                                        ),
                                        color = SoftTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = String.format("%02d:%02d", uiState.quietEndHour, uiState.quietEndMinute),
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 24.sp
                                        ),
                                        color = SoftTextPrimary
                                    )
                                    Text(
                                        text = formatDisplayTime(uiState.quietEndHour, uiState.quietEndMinute),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = SoftTextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                // =============================================================
                // CARD 4: HAPTIC FEEDBACK
                // =============================================================
                Tactile3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = SoftSurfaceWhite,
                    borderColor = SoftBorder,
                    bottomLipColor = SoftBorder,
                    cornerRadius = 24.dp,
                    lipHeight = 3.dp,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFFECFDF5), shape = RoundedCornerShape(14.dp))
                                    .border(width = 2.dp, color = Color(0xFFA7F3D0), shape = RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "☒",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "HAPTIC FEEDBACK",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp,
                                        fontSize = 15.sp
                                    ),
                                    color = SoftTextPrimary
                                )
                                Text(
                                    text = "3-short + 1-long vibration pattern",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = SoftTextMuted
                                )
                            }
                        }

                        TactileToggleSwitch(
                            checked = uiState.vibrateEnabled,
                            onCheckedChange = onToggleVibration
                        )
                    }
                }

                // =============================================================
                // CARD 5: DANGER ZONE
                // =============================================================
                Tactile3DCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_danger_zone"),
                    containerColor = SoftSurfaceWhite,
                    borderColor = SoftRedBorder,
                    bottomLipColor = SoftRedBorder,
                    cornerRadius = 24.dp,
                    lipHeight = 3.dp,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "☒",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = SoftRed
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DANGER ZONE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp,
                                fontSize = 12.sp
                            ),
                            color = SoftRed
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "CLEAR ROAST COMPLETION BOOK",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.4.sp,
                            fontSize = 14.sp
                        ),
                        color = SoftTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Reset all unlocked roasts and discovered secrets back to '???'. This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        color = SoftTextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Tactile3DButton(
                        onClick = { showConfirmClearDialog = true },
                        containerColor = SoftRed,
                        bottomLipColor = SoftRedShadow,
                        borderColor = SoftRedShadow,
                        cornerRadius = 16.dp,
                        lipHeight = 4.5.dp,
                        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_clear_roasts_book")
                    ) {
                        Text(
                            text = "☒",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RESET ROAST COMPLETION BOOK",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
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
                    color = SoftRed
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to clear your collection book? All unlocked roasts will be locked back to '???' and the secrets category will be hidden again.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftTextPrimary
                )
            },
            confirmButton = {
                Tactile3DButton(
                    onClick = {
                        onClearRoastsBook()
                        showConfirmClearDialog = false
                    },
                    containerColor = SoftRed,
                    bottomLipColor = SoftRedShadow,
                    cornerRadius = 14.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("btn_confirm_reset_roasts")
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
                    Text("CANCEL", color = SoftTextSecondary, fontWeight = FontWeight.ExtraBold)
                }
            },
            containerColor = SoftSurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }
}
