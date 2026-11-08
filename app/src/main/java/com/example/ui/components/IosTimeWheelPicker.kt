package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
                    color = SoftTextSecondary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(28.dp))
                Text(
                    text = "MIN (00-59)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = SoftTextSecondary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        val containerShape = RoundedCornerShape(18.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(Color(0xFFF8FAFC), shape = containerShape)
                .border(width = 2.dp, color = SoftBorder, shape = containerShape)
                .clip(containerShape),
            contentAlignment = Alignment.Center
        ) {
            // Center Selection Highlight Band
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .height(48.dp)
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                    .border(width = 2.dp, color = SoftGreenBorder, shape = RoundedCornerShape(12.dp))
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                WheelColumn(
                    range = 0..23,
                    initialValue = selectedHour,
                    onValueChange = onHourChange,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = ":",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp
                    ),
                    color = SoftTextPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

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

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                if (index in itemsList.indices) {
                    onValueChange(itemsList[index])

                    if (isInitialLaunch) {
                        isInitialLaunch = false
                    } else {
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
            contentPadding = PaddingValues(vertical = 48.dp),
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
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.ExtraBold,
                            fontSize = if (isSelected) 24.sp else 18.sp
                        ),
                        color = if (isSelected) SoftGreenText else SoftTextMuted.copy(alpha = 0.55f)
                    )
                }
            }
        }
    }
}

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
                        letterSpacing = 0.6.sp
                    ),
                    color = SoftTextPrimary
                )
                Tactile3DBadge(
                    containerColor = SoftGreenLight,
                    borderColor = SoftGreenBorder,
                    bottomLipColor = SoftGreenBorder,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = liveDisplayFormatted,
                        color = SoftGreenText,
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
                    color = SoftTextSecondary,
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
            Tactile3DButton(
                onClick = { onConfirm(selectedHour, selectedMinute) },
                containerColor = SoftGreen,
                bottomLipColor = SoftGreenShadow,
                cornerRadius = 14.dp,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                modifier = Modifier.testTag("btn_confirm_wheel_time")
            ) {
                Text("DONE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = SoftTextSecondary, fontWeight = FontWeight.Black)
            }
        },
        containerColor = SoftSurfaceWhite,
        shape = RoundedCornerShape(24.dp)
    )
}
