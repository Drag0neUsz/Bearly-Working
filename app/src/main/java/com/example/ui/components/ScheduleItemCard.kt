package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleItem
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleItemCard(
    item: ScheduleItem,
    onToggleCompleted: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timeText = if (item.isAllDay) {
        "ALL DAY"
    } else {
        "${timeFormat.format(Date(item.startTime))} - ${timeFormat.format(Date(item.endTime))}".uppercase()
    }

    val cardBg by animateColorAsState(
        targetValue = if (item.isCompleted) SoftGreenLight else SoftSurfaceWhite,
        label = "cardBgColor"
    )

    val borderColor = if (item.isCompleted) SoftGreenBorder else SoftBorder
    val bottomLipColor = if (item.isCompleted) Color(0xFF86EFAC) else Color(0xFFD5D7DA)
    val hasCalendarAccount = item.source == ScheduleItem.SOURCE_CALENDAR || !item.calendarName.isNullOrBlank()
    val cardShape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bottomLipColor, shape = cardShape)
            .padding(bottom = 3.dp)
            .background(cardBg, shape = cardShape)
            .border(width = 2.dp, color = borderColor, shape = cardShape)
            .clip(cardShape)
            .testTag("schedule_item_${item.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Separate top section for the associated email / calendar account
            if (hasCalendarAccount) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (item.isCompleted) SoftGreen.copy(alpha = 0.08f)
                            else SoftBlueLight.copy(alpha = 0.7f)
                        )
                        .clickable(onClick = onToggleCompleted)
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("schedule_item_account_${item.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (item.isCompleted) SoftGreen else SoftBlue,
                                shape = CircleShape
                            )
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = (item.calendarName ?: "CALENDAR").uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 1.5.dp,
                    color = borderColor
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rounded 3D Checkbox (min 48dp touch target)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onToggleCompleted)
                        .testTag("checkbox_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    val checkShape = RoundedCornerShape(9.dp)
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                color = if (item.isCompleted) SoftGreenShadow else SoftBorder,
                                shape = checkShape
                            )
                            .padding(bottom = 2.dp)
                            .background(
                                color = if (item.isCompleted) SoftGreen else SoftSurfaceWhite,
                                shape = checkShape
                            )
                            .border(
                                width = 2.dp,
                                color = if (item.isCompleted) SoftGreen else Color(0xFFCBD5E1),
                                shape = checkShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Task Content (Time Badge + Title)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onToggleCompleted)
                ) {
                    // Time Pill Badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (item.isCompleted) Color.White.copy(alpha = 0.8f) else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (item.isCompleted) SoftGreenBorder else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (item.isCompleted) SoftGreenText else SoftTextSecondary,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = timeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.4.sp
                                ),
                                color = if (item.isCompleted) SoftGreenText else SoftTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (item.isCompleted) FontWeight.Bold else FontWeight.ExtraBold,
                            textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            fontSize = 16.sp
                        ),
                        color = if (item.isCompleted) SoftTextMuted else SoftTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!item.description.isNullOrBlank()) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!item.location.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = SoftTextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = item.location,
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Soft Tactile Action Buttons: Edit & Delete
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("edit_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit item",
                            tint = SoftTextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("delete_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete item",
                            tint = SoftTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
