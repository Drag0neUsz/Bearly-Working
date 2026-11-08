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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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
import kotlinx.coroutines.isActive
import kotlin.random.Random

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
    // Default to not show the text cloud until the user taps the bear
    var hasSpoken by remember { mutableStateOf(false) }
    var displayedText by remember { mutableStateOf(quote) }
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

    // Dynamic talking animation with randomized scaling parameters and subtle organic tilt
    val dynamicTalkingScale = remember { Animatable(1f) }
    val talkingTilt = remember { Animatable(0f) }

    LaunchedEffect(isTyping) {
        if (isTyping) {
            while (isActive) {
                val peakScale = Random.nextFloat() * (1.08f - 1.025f) + 1.025f
                val dipScale = Random.nextFloat() * (0.98f - 0.945f) + 0.945f
                val upDuration = Random.nextInt(70, 130)
                val downDuration = Random.nextInt(70, 130)
                val tilt = (Random.nextFloat() - 0.5f) * 3.6f

                dynamicTalkingScale.animateTo(
                    targetValue = peakScale,
                    animationSpec = tween(
                        durationMillis = upDuration,
                        easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
                    )
                )
                talkingTilt.animateTo(
                    targetValue = tilt,
                    animationSpec = tween(durationMillis = upDuration)
                )
                dynamicTalkingScale.animateTo(
                    targetValue = dipScale,
                    animationSpec = tween(
                        durationMillis = downDuration,
                        easing = FastOutLinearInEasing
                    )
                )
            }
        } else {
            dynamicTalkingScale.animateTo(1f, tween(100))
            talkingTilt.animateTo(0f, tween(100))
        }
    }

    val currentScale = (if (isTyping) dynamicTalkingScale.value else 1f) * tapBounce

    // Fast typewriter effect and post-speech 3s lock if secret triggered
    LaunchedEffect(quote, hasSpoken) {
        if (hasSpoken) {
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

            if (quote == SavageQuotes.SECRET_RAPID_TAPS_QUOTE) {
                isBlockedCooldown = true
                delay(3000)
                isBlockedCooldown = false
            }
        } else {
            displayedText = quote
        }
    }

    val handleBearClick = {
        if (!isBlockedCooldown) {
            val now = System.currentTimeMillis()
            tapTimestamps.add(now)

            // Keep timestamps within the last 3 seconds
            tapTimestamps.removeAll { now - it > 3000 }

            if (tapTimestamps.size >= 10) {
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

    val isSecret = quote == SavageQuotes.SECRET_RAPID_TAPS_QUOTE

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Speech Bubble with Centered Pointer Tail — hidden by default until user taps the bear
        AnimatedVisibility(
            visible = hasSpoken,
            enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.85f),
            exit = fadeOut(tween(100))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 360.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { handleBearClick() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val bubbleShape = RoundedCornerShape(20.dp)
                val bubbleBorderColor = if (isSecret) SoftRed else Color(0xFFE2E8F0)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bubbleBorderColor, shape = bubbleShape)
                        .padding(bottom = 3.dp)
                        .background(SoftSurfaceWhite, shape = bubbleShape)
                        .border(width = 2.dp, color = bubbleBorderColor, shape = bubbleShape)
                        .clip(bubbleShape)
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                        .testTag("bear_speech_bubble"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "“$displayedText”",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            lineHeight = 21.sp
                        ),
                        color = if (isSecret) SoftRed else Color(0xFF1E293B),
                        textAlign = TextAlign.Center
                    )
                }

                // Centered Speech Tail Pointer
                Canvas(
                    modifier = Modifier
                        .width(24.dp)
                        .height(12.dp)
                        .offset(y = (-2).dp)
                ) {
                    val tailPath = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width / 2f, size.height)
                        close()
                    }
                    drawPath(tailPath, color = SoftSurfaceWhite)
                    drawLine(
                        color = bubbleBorderColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width / 2f, size.height),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = bubbleBorderColor,
                        start = Offset(size.width, 0f),
                        end = Offset(size.width / 2f, size.height),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
            }
        }

        // Cartoon Bear Mascot (Clickable, with randomized talking bounce & tilt)
        Box(
            modifier = Modifier
                .size(116.dp)
                .scale(currentScale)
                .rotate(if (isTyping) talkingTilt.value else 0f)
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
                drawCartoonBear(
                    scope = this,
                    mood = if (isSecret) BearMood.FURIOUS else mood,
                    isTalking = isTyping
                )
            }
        }
    }
}

private fun drawCartoonBear(scope: DrawScope, mood: BearMood, isTalking: Boolean) {
    with(scope) {
        val scaleX = size.width / 120f
        val scaleY = size.height / 120f
        val s = minOf(scaleX, scaleY)

        val earOuterColor = Color(0xFF8B4513)
        val earInnerColor = Color(0xFFF4A460)
        val headFillColor = Color(0xFFA0522D)
        val strokeBrown = Color(0xFF5C2D0C)
        val cheekPink = Color(0xFFF87171)
        val browDark = Color(0xFF451A03)
        val eyeDark = Color(0xFF201007)
        val snoutCream = Color(0xFFF5DEB3)
        val noseDark = Color(0xFF2B1408)
        val mouthRed = Color(0xFF991B1B)
        val tonguePink = Color(0xFFFCA5A5)

        // Subtle drop shadow under head
        drawCircle(
            color = Color.Black.copy(alpha = 0.08f),
            radius = 44f * s,
            center = Offset(60f * s, 68f * s)
        )

        // Left Ear Outer & Stroke
        val leftEarCenter = Offset(28f * s, 30f * s)
        drawCircle(color = earOuterColor, radius = 18f * s, center = leftEarCenter)
        drawCircle(
            color = strokeBrown,
            radius = 18f * s,
            center = leftEarCenter,
            style = Stroke(width = 4f * s)
        )
        // Left Ear Inner
        drawCircle(color = earInnerColor, radius = 10f * s, center = leftEarCenter)

        // Right Ear Outer & Stroke
        val rightEarCenter = Offset(92f * s, 30f * s)
        drawCircle(color = earOuterColor, radius = 18f * s, center = rightEarCenter)
        drawCircle(
            color = strokeBrown,
            radius = 18f * s,
            center = rightEarCenter,
            style = Stroke(width = 4f * s)
        )
        // Right Ear Inner
        drawCircle(color = earInnerColor, radius = 10f * s, center = rightEarCenter)

        // Bear Head
        val headCenter = Offset(60f * s, 64f * s)
        drawCircle(color = headFillColor, radius = 44f * s, center = headCenter)
        drawCircle(
            color = strokeBrown,
            radius = 44f * s,
            center = headCenter,
            style = Stroke(width = 5f * s)
        )

        // Rosy Cheeks
        drawCircle(
            color = cheekPink.copy(alpha = 0.6f),
            radius = 8f * s,
            center = Offset(34f * s, 74f * s)
        )
        drawCircle(
            color = cheekPink.copy(alpha = 0.6f),
            radius = 8f * s,
            center = Offset(86f * s, 74f * s)
        )

        // Snout
        val snoutTopLeft = Offset((60f - 19f) * s, (75f - 14f) * s)
        val snoutSize = Size(38f * s, 28f * s)
        drawOval(color = snoutCream, topLeft = snoutTopLeft, size = snoutSize)
        drawOval(
            color = strokeBrown,
            topLeft = snoutTopLeft,
            size = snoutSize,
            style = Stroke(width = 3f * s)
        )

        // Nose
        val noseTopLeft = Offset((60f - 6f) * s, (66f - 4f) * s)
        drawOval(color = noseDark, topLeft = noseTopLeft, size = Size(12f * s, 8f * s))
        val noseTriangle = Path().apply {
            moveTo(60f * s, 70f * s)
            lineTo(54f * s, 65f * s)
            lineTo(66f * s, 65f * s)
            close()
        }
        drawPath(noseTriangle, color = noseDark)

        // Mouth
        if (isTalking) {
            val openMouthPath = Path().apply {
                moveTo(52f * s, 74f * s)
                quadraticTo(60f * s, 87f * s, 68f * s, 74f * s)
                close()
            }
            drawPath(openMouthPath, color = mouthRed)
            drawPath(
                openMouthPath,
                color = strokeBrown,
                style = Stroke(width = 2.2f * s, join = StrokeJoin.Round)
            )
            val tonguePath = Path().apply {
                moveTo(56f * s, 80f * s)
                quadraticTo(60f * s, 83f * s, 64f * s, 80f * s)
            }
            drawPath(
                tonguePath,
                color = tonguePink,
                style = Stroke(width = 2.2f * s, cap = StrokeCap.Round)
            )
        } else {
            val mouthPath = Path().apply {
                moveTo(54f * s, 75f * s)
                quadraticTo(60f * s, 83f * s, 66f * s, 75f * s)
                close()
            }
            drawPath(mouthPath, color = mouthRed)
            drawPath(
                mouthPath,
                color = strokeBrown,
                style = Stroke(width = 2f * s, join = StrokeJoin.Round)
            )
            val tongueCurve = Path().apply {
                moveTo(57f * s, 79f * s)
                quadraticTo(60f * s, 81f * s, 63f * s, 79f * s)
            }
            drawPath(
                tongueCurve,
                color = tonguePink,
                style = Stroke(width = 1.5f * s, cap = StrokeCap.Round)
            )
        }

        // Eyebrows & Eyes
        if (mood == BearMood.PROUD) {
            // Happy raised brows + cool shades for 100% completion
            drawLine(
                color = browDark,
                start = Offset(34f * s, 48f * s),
                end = Offset(48f * s, 46f * s),
                strokeWidth = 4.2f * s,
                cap = StrokeCap.Round
            )
            drawLine(
                color = browDark,
                start = Offset(86f * s, 48f * s),
                end = Offset(72f * s, 46f * s),
                strokeWidth = 4.2f * s,
                cap = StrokeCap.Round
            )
            // Shades bridge
            drawLine(
                color = eyeDark,
                start = Offset(50f * s, 57f * s),
                end = Offset(70f * s, 57f * s),
                strokeWidth = 3.5f * s
            )
            // Left lens
            drawRoundRect(
                color = eyeDark,
                topLeft = Offset(33f * s, 52f * s),
                size = Size(19f * s, 13f * s),
                cornerRadius = CornerRadius(4f * s, 4f * s)
            )
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(36f * s, 62f * s),
                end = Offset(44f * s, 54f * s),
                strokeWidth = 2f * s,
                cap = StrokeCap.Round
            )
            // Right lens
            drawRoundRect(
                color = eyeDark,
                topLeft = Offset(68f * s, 52f * s),
                size = Size(19f * s, 13f * s),
                cornerRadius = CornerRadius(4f * s, 4f * s)
            )
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(71f * s, 62f * s),
                end = Offset(79f * s, 54f * s),
                strokeWidth = 2f * s,
                cap = StrokeCap.Round
            )
        } else {
            // Angry / Sassy Eyebrows matching the SVG design
            drawLine(
                color = browDark,
                start = Offset(34f * s, 50f * s),
                end = Offset(48f * s, 55f * s),
                strokeWidth = 4.5f * s,
                cap = StrokeCap.Round
            )
            drawLine(
                color = browDark,
                start = Offset(86f * s, 50f * s),
                end = Offset(72f * s, 55f * s),
                strokeWidth = 4.5f * s,
                cap = StrokeCap.Round
            )

            // Left Eye + Catchlight
            drawOval(
                color = eyeDark,
                topLeft = Offset((42f - 4.5f) * s, (60f - 5.5f) * s),
                size = Size(9f * s, 11f * s)
            )
            drawCircle(
                color = Color.White,
                radius = 1.8f * s,
                center = Offset(43.5f * s, 58f * s)
            )

            // Right Eye + Catchlight
            drawOval(
                color = eyeDark,
                topLeft = Offset((78f - 4.5f) * s, (60f - 5.5f) * s),
                size = Size(9f * s, 11f * s)
            )
            drawCircle(
                color = Color.White,
                radius = 1.8f * s,
                center = Offset(79.5f * s, 58f * s)
            )
        }
    }
}
