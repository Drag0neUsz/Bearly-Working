package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ComicInkBlack,
    onPrimary = Color.White,
    primaryContainer = ComicSurfaceSubtle,
    onPrimaryContainer = ComicInkBlack,
    secondary = ComicAccentRed,
    onSecondary = Color.White,
    tertiary = ComicAccentBlue,
    onTertiary = Color.White,
    background = ComicWhiteBg,
    onBackground = ComicTextPrimary,
    surface = ComicSurfaceWhite,
    onSurface = ComicTextPrimary,
    surfaceVariant = ComicSurfaceSubtle,
    onSurfaceVariant = ComicTextSecondary,
    outline = ComicInkBorder,
    outlineVariant = ComicBorderSubtle,
    error = ComicAccentRed,
    onError = Color.White
)

@Composable
fun SavageBearTheme(
    content: @Composable () -> Unit
) {
    // Pure crisp light mode with bold comic framing
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
