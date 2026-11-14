package com.pulsehealth.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val PulsePink = Color(0xFFFF2D55)
val ExerciseGreen = Color(0xFF30D158)
val StandCyan = Color(0xFF00C7BE)
val WaterBlue = Color(0xFF0A84FF)
val SleepPurple = Color(0xFF5E5CE6)
val WarmOrange = Color(0xFFFF9500)
val AppBackground = Color(0xFFF8F9FA)
val Ink = Color(0xFF111216)
val Muted = Color(0xFF8E8E93)

// Minimalist Brushes
val ActivityGradient = Brush.linearGradient(listOf(PulsePink, WarmOrange))
val ExerciseGradient = Brush.linearGradient(listOf(ExerciseGreen, StandCyan))
val WaterGradient = Brush.linearGradient(listOf(WaterBlue, StandCyan))
val SleepGradient = Brush.linearGradient(listOf(SleepPurple, PulsePink))

private val LightColors = lightColorScheme(
    primary = PulsePink,
    onPrimary = Color.White,
    secondary = StandCyan,
    onSecondary = Color.White,
    tertiary = ExerciseGreen,
    onTertiary = Color.White,
    background = AppBackground,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF2F2F7),
    onSurfaceVariant = Muted,
    outline = Color(0xFFE5E5EA),
    outlineVariant = Color(0xFFEBEBF0),
    error = Color(0xFFFF3B30),
    onError = Color.White,
    errorContainer = Color(0xFFFFEEEE),
    onErrorContainer = Color(0xFFD32F2F)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF375F),
    onPrimary = Color.White,
    secondary = StandCyan,
    onSecondary = Color.Black,
    tertiary = ExerciseGreen,
    onTertiary = Color.Black,
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF121214),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFF8E8E93),
    outline = Color(0xFF2C2C2E),
    outlineVariant = Color(0xFF242426),
    error = Color(0xFFFF453A),
    onError = Color.Black,
    errorContainer = Color(0xFF2C1518),
    onErrorContainer = Color(0xFFFF8B80)
)

@Composable
fun PulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
