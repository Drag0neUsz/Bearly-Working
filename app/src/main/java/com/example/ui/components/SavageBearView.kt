package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavageQuotes
import com.example.ui.theme.*
import kotlinx.coroutines.delay

enum class BearMood {
    SAVAGE,
    FURIOUS,
    SUSPICIOUS,
    PROUD
}

@Composable
fun SavageBearView(
    quote: String,
    completedRatio: Float,
    mood: BearMood = when {
        completedRatio >= 1.0f -> BearMood.PROUD
        completedRatio >= 0.5f -> BearMood.SUSPICIOUS
        completedRatio > 0f -> BearMood.SAVAGE
        else -> BearMood.FURIOUS
    },
    onPokeBear: () -> Unit,
    onTriggerSecretRapidTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var hasSpoken by remember { mutableStateOf(false) }
    var displayedText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var isBlockedCooldown by remember { mutableStateOf(false) }

    // Tap tracking for rapid 10 taps secret
    val tapTimestamps = remember { mutableStateListOf<Long>() }

    var isPoked by remember { mutableStateOf(false) }
    val tapBounce by animateFloatAsState(
        targetValue = if (isPoked) 1.12f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        finishedListener = { isPoked = false },
        label = "bearTapBounce"
    )

    // Cubic interpolation talking bounce that syncs with letters appearing
    val infiniteTransition = rememberInfiniteTransition(label = "talkingTransition")
    val talkingScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 110,
                easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "talkingScaleAnim"
    )

    val currentScale = if (isTyping) talkingScale * tapBounce else tapBounce

    // Fast typewriter effect and post-speech 3s lock if secret triggered
    LaunchedEffect(quote, hasSpoken) {
        if (hasSpoken) {
            // Disable immediately if secret roast so it cannot be skipped or interrupted
            if (quote == SavageQuotes.SECRET_RAPID_TAPS_QUOTE) {
                isBlockedCooldown = true
            }

            isTyping = true
            displayedText = ""
            for (i in 1..quote.length) {
                displayedText = quote.substring(0, i)
                delay(22)
            }
            isTyping = false

            // After speech finishes, keep clicking disabled for exactly 3 seconds
            if (quote == SavageQuotes.SECRET_RAPID_TAPS_QUOTE) {
                isBlockedCooldown = true
                delay(3000)
                isBlockedCooldown = false
            }
        }
    }

    val handleBearClick = {
        if (!isBlockedCooldown) {
            val now = System.currentTimeMillis()
            tapTimestamps.add(now)

            // Keep timestamps within the last 3 seconds
            tapTimestamps.removeAll { now - it > 3000 }

            if (tapTimestamps.size >= 10) {
                // Secret triggered! 10 rapid taps: disable clicking IMMEDIATELY!
                isBlockedCooldown = true
                tapTimestamps.clear()
                hasSpoken = true
                isPoked = true
                onTriggerSecretRapidTap()
            } else {
                hasSpoken = true
                isPoked = true
                onPokeBear()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Comic Book Speech Cloud (Only visible after bear is clicked!)
        AnimatedVisibility(
            visible = hasSpoken,
            enter = fadeIn(animationSpec = tween(150)) + scaleIn(initialScale = 0.85f),
            exit = fadeOut(animationSpec = tween(100))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Speech Cloud Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ComicSurfaceWhite, shape = RoundedCornerShape(12.dp))
                        .border(
                            width = 2.5.dp,
                            color = if (quote == SavageQuotes.SECRET_RAPID_TAPS_QUOTE) ComicAccentRed else ComicInkBlack,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("bear_speech_bubble")
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = when {
                                    quote == SavageQuotes.SECRET_RAPID_TAPS_QUOTE -> "BEAR [🔥 SECRET TRIGGERED: ENRAGED]"
                                    mood == BearMood.PROUD -> "BEAR [SHOCKED RESPECT]"
                                    mood == BearMood.FURIOUS -> "BEAR [UNFILTERED RAGE]"
                                    mood == BearMood.SUSPICIOUS -> "BEAR [EXTREMELY SKEPTICAL]"
                                    else -> "BEAR [DIRECT POKE CALLOUT]"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    fontSize = 11.sp
                                ),
                                color = if (quote == SavageQuotes.SECRET_RAPID_TAPS_QUOTE) ComicAccentRed else ComicInkBlack
                            )

                            if (isTyping) {
                                Text(
                                    text = "TALKING...",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = ComicAccentRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Typewriter text
                        Text(
                            text = "\"$displayedText\"",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 22.sp,
                                fontSize = 16.sp
                            ),
                            color = if (quote == SavageQuotes.SECRET_RAPID_TAPS_QUOTE) ComicAccentRed else ComicInkBlack
                        )
                    }
                }

                // Comic speech bubble tail pointing down toward bear
                Canvas(
                    modifier = Modifier
                        .width(28.dp)
                        .height(14.dp)
                        .offset(y = (-1).dp)
                ) {
                    val tailPath = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width * 0.4f, size.height)
                        close()
                    }
                    drawPath(tailPath, color = ComicSurfaceWhite)
                    val outlineColor = if (quote == SavageQuotes.SECRET_RAPID_TAPS_QUOTE) ComicAccentRed else ComicInkBlack
                    drawLine(
                        color = outlineColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width * 0.4f, size.height),
                        strokeWidth = 2.5.dp.toPx()
                    )
                    drawLine(
                        color = outlineColor,
                        start = Offset(size.width, 0f),
                        end = Offset(size.width * 0.4f, size.height),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
            }
        }

        if (!hasSpoken) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .background(ComicSurfaceWhite)
                    .border(width = 2.dp, color = ComicInkBlack)
                    .padding(vertical = 8.dp, horizontal = 12.dp)
                    .clickable { handleBearClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👉 TAP THE BEAR TO GET HIS UNFILTERED THOUGHTS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    ),
                    color = ComicInkBlack,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Bear Character (Clickable, with talking bounce in scale)
        Box(
            modifier = Modifier
                .size(130.dp)
                .scale(currentScale)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    handleBearClick()
                }
                .testTag("bear_avatar_clickable"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawComicBearCharacter(
                    scope = this,
                    mood = if (quote == SavageQuotes.SECRET_RAPID_TAPS_QUOTE) BearMood.FURIOUS else mood,
                    isTalking = isTyping
                )
            }
        }
    }
}

private fun drawComicBearCharacter(scope: DrawScope, mood: BearMood, isTalking: Boolean) {
    with(scope) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val headRadius = size.width * 0.32f

        val furColor = Color(0xFF9E572E)
        val snoutColor = Color(0xFFFBE4C6)
        val inkBlack = Color(0xFF0F172A)
        val outlineWidth = 6f

        // Left Ear
        drawCircle(
            color = furColor,
            radius = headRadius * 0.42f,
            center = Offset(centerX - headRadius * 0.82f, centerY - headRadius * 0.72f)
        )
        drawCircle(
            color = inkBlack,
            radius = headRadius * 0.42f,
            center = Offset(centerX - headRadius * 0.82f, centerY - headRadius * 0.72f),
            style = Stroke(width = outlineWidth)
        )
        drawCircle(
            color = snoutColor,
            radius = headRadius * 0.22f,
            center = Offset(centerX - headRadius * 0.82f, centerY - headRadius * 0.72f)
        )

        // Right Ear
        drawCircle(
            color = furColor,
            radius = headRadius * 0.42f,
            center = Offset(centerX + headRadius * 0.82f, centerY - headRadius * 0.72f)
        )
        drawCircle(
            color = inkBlack,
            radius = headRadius * 0.42f,
            center = Offset(centerX + headRadius * 0.82f, centerY - headRadius * 0.72f),
            style = Stroke(width = outlineWidth)
        )
        drawCircle(
            color = snoutColor,
            radius = headRadius * 0.22f,
            center = Offset(centerX + headRadius * 0.82f, centerY - headRadius * 0.72f)
        )

        // Head Base
        drawCircle(
            color = furColor,
            radius = headRadius,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = inkBlack,
            radius = headRadius,
            center = Offset(centerX, centerY),
            style = Stroke(width = outlineWidth)
        )

        // Snout
        drawRoundRect(
            color = snoutColor,
            topLeft = Offset(centerX - headRadius * 0.48f, centerY - headRadius * 0.08f),
            size = Size(headRadius * 0.96f, headRadius * 0.72f),
            cornerRadius = CornerRadius(headRadius * 0.36f, headRadius * 0.36f)
        )
        drawRoundRect(
            color = inkBlack,
            topLeft = Offset(centerX - headRadius * 0.48f, centerY - headRadius * 0.08f),
            size = Size(headRadius * 0.96f, headRadius * 0.72f),
            cornerRadius = CornerRadius(headRadius * 0.36f, headRadius * 0.36f),
            style = Stroke(width = outlineWidth * 0.8f)
        )

        // Nose
        drawRoundRect(
            color = inkBlack,
            topLeft = Offset(centerX - headRadius * 0.2f, centerY - headRadius * 0.02f),
            size = Size(headRadius * 0.4f, headRadius * 0.24f),
            cornerRadius = CornerRadius(12f, 12f)
        )

        // Mouth
        val mouthY = centerY + headRadius * 0.36f
        if (isTalking) {
            drawRoundRect(
                color = Color(0xFF7F1D1D),
                topLeft = Offset(centerX - headRadius * 0.2f, mouthY - 4f),
                size = Size(headRadius * 0.4f, headRadius * 0.24f),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = inkBlack,
                topLeft = Offset(centerX - headRadius * 0.2f, mouthY - 4f),
                size = Size(headRadius * 0.4f, headRadius * 0.24f),
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(width = 5f)
            )
            drawCircle(
                color = Color(0xFFEF4444),
                radius = headRadius * 0.1f,
                center = Offset(centerX, mouthY + headRadius * 0.12f)
            )
        } else {
            when (mood) {
                BearMood.PROUD -> {
                    val path = Path().apply {
                        moveTo(centerX - headRadius * 0.26f, mouthY - 4f)
                        quadraticTo(centerX, mouthY + 14f, centerX + headRadius * 0.32f, mouthY - 8f)
                    }
                    drawPath(path, color = inkBlack, style = Stroke(width = 6f, cap = StrokeCap.Round))
                }
                BearMood.FURIOUS -> {
                    val path = Path().apply {
                        moveTo(centerX - headRadius * 0.26f, mouthY + 8f)
                        quadraticTo(centerX, mouthY - 6f, centerX + headRadius * 0.26f, mouthY + 8f)
                    }
                    drawPath(path, color = inkBlack, style = Stroke(width = 7f, cap = StrokeCap.Round))
                }
                BearMood.SUSPICIOUS -> {
                    val path = Path().apply {
                        moveTo(centerX - headRadius * 0.22f, mouthY + 2f)
                        quadraticTo(centerX - 8f, mouthY - 6f, centerX, mouthY)
                        quadraticTo(centerX + 8f, mouthY + 6f, centerX + headRadius * 0.24f, mouthY - 2f)
                    }
                    drawPath(path, color = inkBlack, style = Stroke(width = 6f, cap = StrokeCap.Round))
                }
                BearMood.SAVAGE -> {
                    val path = Path().apply {
                        moveTo(centerX - headRadius * 0.2f, mouthY + 4f)
                        quadraticTo(centerX, mouthY + 8f, centerX + headRadius * 0.3f, mouthY - 8f)
                    }
                    drawPath(path, color = inkBlack, style = Stroke(width = 6f, cap = StrokeCap.Round))
                }
            }
        }

        // Eyes / Brows
        if (mood == BearMood.PROUD) {
            val glassWidth = headRadius * 0.44f
            val glassHeight = headRadius * 0.36f
            val leftGlassX = centerX - headRadius * 0.52f
            val rightGlassX = centerX + headRadius * 0.08f
            val glassY = centerY - headRadius * 0.44f

            drawLine(
                color = inkBlack,
                start = Offset(leftGlassX + glassWidth, glassY + glassHeight * 0.3f),
                end = Offset(rightGlassX, glassY + glassHeight * 0.3f),
                strokeWidth = 7f
            )

            drawRoundRect(
                color = inkBlack,
                topLeft = Offset(leftGlassX, glassY),
                size = Size(glassWidth, glassHeight),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawLine(
                color = Color.White,
                start = Offset(leftGlassX + 8f, glassY + glassHeight - 8f),
                end = Offset(leftGlassX + glassWidth - 12f, glassY + 8f),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )

            drawRoundRect(
                color = inkBlack,
                topLeft = Offset(rightGlassX, glassY),
                size = Size(glassWidth, glassHeight),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawLine(
                color = Color.White,
                start = Offset(rightGlassX + 8f, glassY + glassHeight - 8f),
                end = Offset(rightGlassX + glassWidth - 12f, glassY + 8f),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
        } else {
            val eyeRadius = headRadius * 0.12f
            val leftEyeCenter = Offset(centerX - headRadius * 0.35f, centerY - headRadius * 0.26f)
            val rightEyeCenter = Offset(centerX + headRadius * 0.35f, centerY - headRadius * 0.26f)

            when (mood) {
                BearMood.FURIOUS -> {
                    drawLine(
                        color = inkBlack,
                        start = Offset(leftEyeCenter.x - 18f, leftEyeCenter.y - 28f),
                        end = Offset(leftEyeCenter.x + 18f, leftEyeCenter.y - 12f),
                        strokeWidth = 7f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = inkBlack,
                        start = Offset(rightEyeCenter.x + 18f, rightEyeCenter.y - 28f),
                        end = Offset(rightEyeCenter.x - 18f, rightEyeCenter.y - 12f),
                        strokeWidth = 7f,
                        cap = StrokeCap.Round
                    )
                }
                BearMood.SUSPICIOUS -> {
                    drawLine(
                        color = inkBlack,
                        start = Offset(leftEyeCenter.x - 18f, leftEyeCenter.y - 30f),
                        end = Offset(leftEyeCenter.x + 18f, leftEyeCenter.y - 24f),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = inkBlack,
                        start = Offset(rightEyeCenter.x - 18f, rightEyeCenter.y - 16f),
                        end = Offset(rightEyeCenter.x + 18f, rightEyeCenter.y - 16f),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }
                else -> {
                    drawLine(
                        color = inkBlack,
                        start = Offset(leftEyeCenter.x - 18f, leftEyeCenter.y - 20f),
                        end = Offset(leftEyeCenter.x + 18f, leftEyeCenter.y - 24f),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = inkBlack,
                        start = Offset(rightEyeCenter.x - 18f, rightEyeCenter.y - 26f),
                        end = Offset(rightEyeCenter.x + 18f, rightEyeCenter.y - 18f),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }
            }

            drawCircle(color = inkBlack, radius = eyeRadius, center = leftEyeCenter)
            drawCircle(color = inkBlack, radius = eyeRadius, center = rightEyeCenter)
            drawCircle(color = Color.White, radius = eyeRadius * 0.35f, center = Offset(leftEyeCenter.x - 3f, leftEyeCenter.y - 3f))
            drawCircle(color = Color.White, radius = eyeRadius * 0.35f, center = Offset(rightEyeCenter.x - 3f, rightEyeCenter.y - 3f))
        }
    }
}
