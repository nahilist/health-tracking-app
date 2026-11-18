package com.pulsehealth.app.ui.goals

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.SleepPurple
import com.pulsehealth.app.ui.theme.WaterBlue

/**
 * Health target categories supported in the multi-step goal wizard.
 */
enum class GoalCategory(
    val title: String,
    val unit: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    STEPS("Daily Steps", "steps", Icons.AutoMirrored.Filled.DirectionsWalk, ExerciseGreen),
    CALORIES("Active Energy", "kcal", Icons.Default.Whatshot, PulsePink),
    WATER("Hydration", "L", Icons.Default.LocalDrink, WaterBlue),
    SLEEP("Sleep Duration", "hrs", Icons.Default.Bedtime, SleepPurple)
}

/**
 * Preset data classes for physiological guideline recommendations.
 */
data class StepPreset(val label: String, val description: String, val steps: Long)
data class CaloriePreset(val label: String, val description: String, val calories: Long)
data class WaterPreset(val label: String, val description: String, val liters: Double)
data class SleepPreset(val label: String, val description: String, val minutes: Long)

object GoalPresets {
    val stepPresets = listOf(
        StepPreset("Light", "Gentle daily movement", 6_000L),
        StepPreset("Active", "WHO / CDC standard", 10_000L),
        StepPreset("Conditioning", "High daily energy", 14_000L),
        StepPreset("Athlete", "Endurance & training", 18_000L)
    )

    val caloriePresets = listOf(
        CaloriePreset("Light", "Low-impact activity", 300L),
        CaloriePreset("Fitness", "Standard daily burn", 500L),
        CaloriePreset("Conditioning", "Vigorous workouts", 750L),
        CaloriePreset("Peak", "High-performance training", 1_000L)
    )

    val waterPresets = listOf(
        WaterPreset("Gentle", "~6 cups / day", 1.5),
        WaterPreset("Standard", "Recommended baseline (~8 cups)", 2.0),
        WaterPreset("Hydrated", "Active lifestyle (~10 cups)", 2.5),
        WaterPreset("Peak", "Hot climate / sports (~12 cups)", 3.0)
    )

    val sleepPresets = listOf(
        SleepPreset("Minimum", "7h 00m rest", 420L),
        SleepPreset("Optimal", "8h 00m recommended", 480L),
        SleepPreset("Extended", "9h 00m recovery", 540L)
    )
}

/**
 * Value container for the user's custom goals.
 */
data class CustomGoals(
    val waterLiters: Double = 2.0,
    val steps: Long = 10_000L,
    val calories: Long = 500L,
    val sleepMinutes: Long = 480L
) {
    val estimatedDistanceKm: Double
        get() = (steps * 0.00075)

    val estimatedWalkingMinutes: Long
        get() = (steps / 100L)

    val estimatedWaterGlasses: Int
        get() = (waterLiters * 4.0).toInt()

    val sleepHoursFormatted: String
        get() {
            val hrs = sleepMinutes / 60
            val mins = sleepMinutes % 60
            return if (mins == 0L) "${hrs}h" else "${hrs}h ${mins}m"
        }

    val sleepCyclesCount: Int
        get() = (sleepMinutes / 90L).toInt()
}
