package com.pulsehealth.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PulsePink = Color(0xFFF2335B)
val ExerciseGreen = Color(0xFF35C76F)
val StandCyan = Color(0xFF16ABC3)
val WaterBlue = Color(0xFF209FE5)
val SleepPurple = Color(0xFF7658D6)
val WarmOrange = Color(0xFFF59C22)
val AppBackground = Color(0xFFF4F4F8)
val Ink = Color(0xFF202126)
val Muted = Color(0xFF777981)

private val LightColors = lightColorScheme(
    primary = PulsePink,
    onPrimary = Color.White,
    background = AppBackground,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE9E9EF),
    onSurfaceVariant = Muted,
    outline = Color(0xFFD8D8DF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF6681),
    secondary = StandCyan,
    background = Color(0xFF111216),
    surface = Color(0xFF1B1C21),
    onSurface = Color(0xFFF5F5F7)
)

@Composable
fun PulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
