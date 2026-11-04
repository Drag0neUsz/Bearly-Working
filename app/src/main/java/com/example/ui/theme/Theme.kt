package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = SoftGreen,
    onPrimary = Color.White,
    primaryContainer = SoftGreenLight,
    onPrimaryContainer = SoftGreenText,
    secondary = SoftBlue,
    onSecondary = Color.White,
    secondaryContainer = SoftBlueLight,
    onSecondaryContainer = SoftBlueText,
    tertiary = SoftOrange,
    onTertiary = Color.White,
    tertiaryContainer = SoftOrangeLight,
    onTertiaryContainer = SoftOrangeText,
    background = SoftCanvasBg,
    onBackground = SoftTextPrimary,
    surface = SoftSurfaceWhite,
    onSurface = SoftTextPrimary,
    surfaceVariant = SoftSurfaceSubtle,
    onSurfaceVariant = SoftTextSecondary,
    outline = SoftBorder,
    outlineVariant = SoftBorderShadow,
    error = SoftRed,
    onError = Color.White
)

val SoftPopShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun SavageBearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = SoftPopShapes,
        content = content
    )
}
