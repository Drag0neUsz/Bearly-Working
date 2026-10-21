package com.example.ui.components

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Reusable iPhone Alarm-style drum wheel time picker.
 * Can be embedded directly inside any layout or dialog.
 * Emits a very slight single vibration tick on every single number scrolled!
 */
@Composable
fun IosTimeWheelPicker(
    selectedHour: Int,
    selectedMinute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 144.dp,
    showLabels: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showLabels) {
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
        }

        // iPhone-style Drum Wheel Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
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
                    initialValue = selectedHour,
                    onValueChange = onHourChange,
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
                    initialValue = selectedMinute,
                    onValueChange = onMinuteChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
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

                IosTimeWheelPicker(
                    selectedHour = selectedHour,
                    selectedMinute = selectedMinute,
                    onHourChange = { selectedHour = it },
                    onMinuteChange = { selectedMinute = it },
                    modifier = Modifier.fillMaxWidth()
                )
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
