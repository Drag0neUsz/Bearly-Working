package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleItem
import com.example.ui.theme.*

/**
 * Tactile 3D Card with a solid bottom rim extrusion.
 */
@Composable
fun Tactile3DCard(
    modifier: Modifier = Modifier,
    containerColor: Color = SoftSurfaceWhite,
    borderColor: Color = SoftBorder,
    bottomLipColor: Color = borderColor,
    cornerRadius: Dp = 24.dp,
    lipHeight: Dp = 3.dp,
    borderWidth: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressOffset by animateDpAsState(
        targetValue = if (onClick != null && isPressed) (lipHeight - 1.dp).coerceAtLeast(0.dp) else 0.dp,
        animationSpec = tween(70),
        label = "cardPressOffset"
    )
    val currentLip by animateDpAsState(
        targetValue = if (onClick != null && isPressed) 1.dp else lipHeight,
        animationSpec = tween(70),
        label = "cardLipHeight"
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .padding(top = pressOffset)
            .background(bottomLipColor, shape = shape)
            .padding(bottom = currentLip)
            .background(containerColor, shape = shape)
            .border(width = borderWidth, color = borderColor, shape = shape)
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * Tactile 3D Push Button that physically depresses on tap.
 */
@Composable
fun Tactile3DButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = SoftGreen,
    bottomLipColor: Color = SoftGreenShadow,
    borderColor: Color = bottomLipColor,
    cornerRadius: Dp = 16.dp,
    lipHeight: Dp = 4.dp,
    borderWidth: Dp = 2.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed) (lipHeight - 1.dp).coerceAtLeast(0.dp) else 0.dp,
        animationSpec = tween(60),
        label = "btnPressOffset"
    )
    val currentLip by animateDpAsState(
        targetValue = if (isPressed) 1.dp else lipHeight,
        animationSpec = tween(60),
        label = "btnLipHeight"
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .padding(top = pressOffset)
            .background(bottomLipColor, shape = shape)
            .padding(bottom = currentLip)
            .background(containerColor, shape = shape)
            .border(width = borderWidth, color = borderColor, shape = shape)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

/**
 * Tactile 3D Pill / Badge (`border-2 border-b-4`).
 */
@Composable
fun Tactile3DBadge(
    modifier: Modifier = Modifier,
    containerColor: Color = SoftOrangeLight,
    borderColor: Color = SoftOrangeBorder,
    bottomLipColor: Color = borderColor,
    shape: Shape = CircleShape,
    lipHeight: Dp = 2.5.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(bottomLipColor, shape = shape)
            .padding(bottom = lipHeight)
            .background(containerColor, shape = shape)
            .border(width = 1.5.dp, color = borderColor, shape = shape)
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

/**
 * Chunky Glossy Capsule Progress Bar.
 */
@Composable
fun TactileGlossyProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    trackColor: Color = Color(0xFFF1F5F9),
    trackBorderColor: Color = SoftBorder,
    fillBrush: Brush = Brush.horizontalGradient(listOf(SoftGreen, Color(0xFF7BE329))),
    minVisualFraction: Float = 0.08f
) {
    val visualFraction = if (progress <= 0f) {
        minVisualFraction
    } else {
        progress.coerceIn(minVisualFraction, 1f)
    }

    Box(
        modifier = modifier
            .height(height)
            .background(trackColor, shape = CircleShape)
            .border(width = 1.5.dp, color = trackBorderColor, shape = CircleShape)
            .padding(2.dp)
            .clip(CircleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = visualFraction)
                .background(brush = fillBrush, shape = CircleShape)
                .clip(CircleShape)
        ) {
            // Glossy white top highlight reflection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.42f)
                    .padding(horizontal = 4.dp, vertical = 1.5.dp)
                    .background(Color.White.copy(alpha = 0.35f), shape = CircleShape)
            )
        }
    }
}

/**
 * Chunky 3D Tactile Toggle Switch.
 */
@Composable
fun TactileToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) SoftGreen else Color(0xFFCBD5E1),
        animationSpec = tween(180),
        label = "switchTrackColor"
    )
    val trackBorderColor by animateColorAsState(
        targetValue = if (checked) SoftGreenShadow else Color(0xFF94A3B8),
        animationSpec = tween(180),
        label = "switchBorderColor"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 0.dp,
        animationSpec = tween(180),
        label = "switchThumbOffset"
    )

    Box(
        modifier = modifier
            .width(56.dp)
            .height(32.dp)
            .clip(CircleShape)
            .background(trackColor, shape = CircleShape)
            .border(width = 2.dp, color = trackBorderColor, shape = CircleShape)
            .clickable(
                role = Role.Switch,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(22.dp)
                .background(Color(0xFFD0D5DD), shape = CircleShape)
                .padding(bottom = 2.dp)
                .background(Color.White, shape = CircleShape)
        )
    }
}

/**
 * Tactile toggle row inside the task creation dialog for also saving the task to the phone's calendar.
 */
@Composable
fun SaveToPhoneCalendarToggleRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    switchTestTag: String = "switch_save_to_calendar",
    modifier: Modifier = Modifier
) {
    val containerBg = if (checked) SoftBlueLight else Color(0xFFF8FAFC)
    val borderColor = if (checked) SoftBlueBorder else SoftBorder

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(containerBg, shape = RoundedCornerShape(14.dp))
            .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = if (checked) SoftBlue else Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = if (checked) Color.White else SoftTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "SAVE TO PHONE CALENDAR",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    ),
                    color = SoftTextPrimary
                )
                Text(
                    text = "Also adds this event to your device calendar",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = SoftTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        TactileToggleSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(switchTestTag)
        )
    }
}

/**
 * Confirmation popup before deleting a task.
 */
@Composable
fun DeleteTaskConfirmationDialog(
    item: ScheduleItem,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_confirm_delete_task"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(SoftRedLight, shape = RoundedCornerShape(10.dp))
                        .border(width = 1.5.dp, color = SoftRedBorder, shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = SoftRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "DELETE COMMITMENT?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = SoftRed
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Are you sure you want to delete \"${item.title}\"?",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = SoftTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (item.source == ScheduleItem.SOURCE_CALENDAR) {
                        "This calendar event will be hidden from your schedule and won't reappear on future syncs."
                    } else if (item.calendarEventId != null) {
                        "This commitment will be removed from your schedule and your phone's calendar."
                    } else {
                        "The bear knows when you back out of a commitment. You can disable this prompt in Settings."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTextSecondary
                )
            }
        },
        confirmButton = {
            Tactile3DButton(
                onClick = onConfirmDelete,
                containerColor = SoftRed,
                bottomLipColor = SoftRedShadow,
                borderColor = SoftRedShadow,
                cornerRadius = 14.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                modifier = Modifier.testTag("btn_confirm_delete_task")
            ) {
                Text(
                    text = "DELETE TASK",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_delete_task")
            ) {
                Text("CANCEL", color = SoftTextSecondary, fontWeight = FontWeight.Black)
            }
        },
        containerColor = SoftSurfaceWhite,
        shape = RoundedCornerShape(24.dp)
    )
}
