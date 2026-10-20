package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SavageUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SavageUiState,
    onSetInterval: (Int) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleQuietHours: (Boolean) -> Unit,
    onSetQuietHours: (Int, Int) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onTestNotification: () -> Unit,
    onRequestCalendarPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

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

            // Master Push Notification Frame (Full-Width Sharp Frame)
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

                    // Test Notification Button (Sharp Frame)
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

            // Notification Interval Frame (Full-Width Sharp Frame)
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

                    // Full-width Sharp Segmented Grid (No rounded pills!)
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

            // Quiet Hours Frame (Full-Width Sharp Frame)
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
                                    text = if (uiState.quietHoursEnabled) "Mutes between ${uiState.quietStartHour}:00 and ${uiState.quietEndHour}:00" else "Off (24/7 alerts)",
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
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(width = 1.5.dp, color = ComicInkBlack)
                                    .background(ComicSurfaceSubtle)
                                    .clickable {
                                        val next = (uiState.quietStartHour + 1) % 24
                                        onSetQuietHours(next, uiState.quietEndHour)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("SLEEP: ${uiState.quietStartHour}:00", fontWeight = FontWeight.Black, color = ComicInkBlack, fontSize = 12.sp)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(width = 1.5.dp, color = ComicInkBlack)
                                    .background(ComicSurfaceSubtle)
                                    .clickable {
                                        val next = (uiState.quietEndHour + 1) % 24
                                        onSetQuietHours(uiState.quietStartHour, next)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("WAKE: ${uiState.quietEndHour}:00", fontWeight = FontWeight.Black, color = ComicInkBlack, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Vibration Toggle Frame (Full-Width Sharp Frame)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ComicSurfaceWhite)
                    .border(width = 2.dp, color = ComicInkBlack)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                text = "VIBRATE ON ROAST",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                color = ComicInkBlack
                            )
                            Text(
                                text = "Buzz pattern when you're called out",
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

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
