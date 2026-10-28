package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
        targetValue = if (item.isCompleted) Color(0xFFF0FDF4) else ComicSurfaceWhite,
        label = "cardBgColor"
    )

    val borderThickness = if (item.isCompleted) 1.5.dp else 2.dp
    val borderColor = if (item.isCompleted) ComicAccentGreen else ComicInkBlack

    val hasCalendarAccount = item.source == ScheduleItem.SOURCE_CALENDAR || !item.calendarName.isNullOrBlank()

    // Full-width sharp rectangular frame
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(cardBg, shape = RectangleShape)
            .border(width = borderThickness, color = borderColor, shape = RectangleShape)
            .testTag("schedule_item_${item.id}")
    ) {
        // Separate top section for the associated email / calendar account
        if (hasCalendarAccount) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (item.isCompleted) ComicAccentGreen.copy(alpha = 0.12f)
                        else ComicSurfaceSubtle
                    )
                    .clickable(onClick = onToggleCompleted)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("schedule_item_account_${item.id}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(if (item.isCompleted) ComicAccentGreen else ComicInkBlack)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = (item.calendarName ?: "CALENDAR").uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp,
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
                thickness = borderThickness,
                color = borderColor
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Square Sharp Checkbox (min 48dp touch target)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = onToggleCompleted)
                    .testTag("checkbox_${item.id}"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(if (item.isCompleted) ComicAccentGreen else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = if (item.isCompleted) ComicAccentGreen else ComicInkBlack,
                            shape = RectangleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Task Content (Time + Title)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onToggleCompleted)
            ) {
                // Time Frame Badge
                Box(
                    modifier = Modifier
                        .border(width = 1.dp, color = ComicInkBlack, shape = RectangleShape)
                        .background(ComicSurfaceSubtle)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = ComicInkBlack,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (item.isCompleted) FontWeight.Medium else FontWeight.Black,
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        fontSize = 16.sp
                    ),
                    color = if (item.isCompleted) ComicTextMuted else ComicTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!item.description.isNullOrBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = ComicTextSecondary,
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
                            tint = ComicTextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = item.location,
                            style = MaterialTheme.typography.labelSmall,
                            color = ComicTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Sharp Action Buttons: Edit & Delete
            Row(
                verticalAlignment = Alignment.CenterVertically
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
                        tint = ComicInkBlack,
                        modifier = Modifier.size(20.dp)
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
                        tint = ComicTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
