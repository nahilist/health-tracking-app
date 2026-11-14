package com.pulsehealth.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val PulsePink = Color(0xFFFF2A55)
val ExerciseGreen = Color(0xFF00E676)
val StandCyan = Color(0xFF00E5FF)
val WaterBlue = Color(0xFF0091FF)
val SleepPurple = Color(0xFF8A53FF)
val WarmOrange = Color(0xFFFF7A00)
val AppBackground = Color(0xFFF7F8FC)
val Ink = Color(0xFF12131A)
val Muted = Color(0xFF75788A)

// Top App Brushes & Gradients
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
    surfaceVariant = Color(0xFFEDF0F7),
    onSurfaceVariant = Muted,
    outline = Color(0xFFD6DBE8),
    outlineVariant = Color(0xFFE6EAF2),
    error = Color(0xFFE53935),
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF4D71),
    onPrimary = Color.White,
    secondary = StandCyan,
    onSecondary = Color.Black,
    tertiary = ExerciseGreen,
    onTertiary = Color.Black,
    background = Color(0xFF0C0D11),
    onBackground = Color(0xFFF5F6FA),
    surface = Color(0xFF161820),
    onSurface = Color(0xFFF5F6FA),
    surfaceVariant = Color(0xFF222530),
    onSurfaceVariant = Color(0xFF9EA3B5),
    outline = Color(0xFF323646),
    outlineVariant = Color(0xFF232733),
    error = Color(0xFFFF5252),
    onError = Color.Black,
    errorContainer = Color(0xFF381519),
    onErrorContainer = Color(0xFFFFB4AB)
)

@Composable
fun PulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
