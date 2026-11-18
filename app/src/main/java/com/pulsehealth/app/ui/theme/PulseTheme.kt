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
val AppBackground = Color(0xFFF4F6F8)
val Ink = Color(0xFF111827)
val Muted = Color(0xFF8E8E93)

// Ultra-Modern Athletic & Health Design Tokens (Granger / Strava / Apple Fitness UX)
val AthleticOrange = Color(0xFFFF5E1E)
val AthleticOrangeDark = Color(0xFFE04A0E)
val AthleticOrangeLight = Color(0xFFFFF0EB)
val AthleticCyan = Color(0xFF00ACC1)
val AthleticCyanLight = Color(0xFFE0F7FA)
val DeepCharcoal = Color(0xFF111827)
val SoftPillBackground = Color(0xFFF0F3F6)
val CanvasBackground = Color(0xFFF4F6F8)

// Executive Forest Emerald & Warm Accent Palette (Design Spec Alignment)
val ForestEmerald = Color(0xFF0D5C58)
val ForestEmeraldDark = Color(0xFF094340)
val ForestEmeraldLight = Color(0xFFE8F3F2)
val WarmAccent = Color(0xFFFF5E1E)
val WarmAccentLight = Color(0xFFFFF0EB)
val SoftCreamBackground = Color(0xFFF4F6F8)

// Minimalist Brushes
val ActivityGradient = Brush.linearGradient(listOf(AthleticOrange, WarmOrange))
val ExerciseGradient = Brush.linearGradient(listOf(ExerciseGreen, AthleticCyan))
val WaterGradient = Brush.linearGradient(listOf(WaterBlue, StandCyan))
val SleepGradient = Brush.linearGradient(listOf(SleepPurple, AthleticOrange))

private val LightColors = lightColorScheme(
    primary = AthleticOrange,
    onPrimary = Color.White,
    secondary = AthleticCyan,
    onSecondary = Color.White,
    tertiary = ForestEmerald,
    onTertiary = Color.White,
    background = CanvasBackground,
    onBackground = DeepCharcoal,
    surface = Color.White,
    onSurface = DeepCharcoal,
    surfaceVariant = SoftPillBackground,
    onSurfaceVariant = Muted,
    outline = Color(0xFFE5E7EB),
    outlineVariant = Color(0xFFEEF1F4),
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
