package com.pulsehealth.app.data

import com.pulsehealth.app.data.local.SleepStageEntity
import kotlin.math.roundToInt

enum class SleepStageType(val displayName: String, val colorHex: Long) {
    DEEP("Deep Sleep", 0xFF673AB7),   // Rich Deep Purple
    LIGHT("Light Sleep", 0xFF2196F3), // Soft Calm Blue
    REM("REM Sleep", 0xFF00BCD4),     // Teal / Cyan
    AWAKE("Awake", 0xFFFF9800),       // Warm Amber / Orange
    UNKNOWN("Unknown", 0xFF9E9E9E);   // Neutral Slate

    companion object {
        fun fromString(value: String): SleepStageType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

data class SleepStageInterval(
    val id: String,
    val sessionId: String,
    val stage: SleepStageType,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val durationMinutes: Long
)

data class SleepBreakdownSummary(
    val totalSleepMinutes: Long,
    val inBedMinutes: Long,
    val deepMinutes: Long,
    val lightMinutes: Long,
    val remMinutes: Long,
    val awakeMinutes: Long,
    val deepPercentage: Int,
    val lightPercentage: Int,
    val remPercentage: Int,
    val awakePercentage: Int,
    val sleepEfficiencyPercentage: Int
) {
    val efficiencyRating: String
        get() = when {
            sleepEfficiencyPercentage >= 85 -> "Optimal"
            sleepEfficiencyPercentage >= 75 -> "Good"
            sleepEfficiencyPercentage >= 65 -> "Fair"
            else -> "Restless"
        }

    companion object {
        fun fromStages(stages: List<SleepStageEntity>, sessionDurationMinutes: Long): SleepBreakdownSummary {
            var deep = 0L
            var light = 0L
            var rem = 0L
            var awake = 0L

            for (stage in stages) {
                when (SleepStageType.fromString(stage.stage)) {
                    SleepStageType.DEEP -> deep += stage.durationMinutes
                    SleepStageType.LIGHT -> light += stage.durationMinutes
                    SleepStageType.REM -> rem += stage.durationMinutes
                    SleepStageType.AWAKE -> awake += stage.durationMinutes
                    SleepStageType.UNKNOWN -> light += stage.durationMinutes
                }
            }

            val totalSleep = deep + light + rem
            val inBed = (totalSleep + awake).coerceAtLeast(sessionDurationMinutes)

            val deepPct = if (inBed > 0) ((deep.toDouble() / inBed) * 100).roundToInt() else 0
            val lightPct = if (inBed > 0) ((light.toDouble() / inBed) * 100).roundToInt() else 0
            val remPct = if (inBed > 0) ((rem.toDouble() / inBed) * 100).roundToInt() else 0
            val awakePct = if (inBed > 0) ((awake.toDouble() / inBed) * 100).roundToInt() else 0

            val efficiency = if (inBed > 0) {
                ((totalSleep.toDouble() / inBed) * 100).roundToInt().coerceIn(0, 100)
            } else 0

            return SleepBreakdownSummary(
                totalSleepMinutes = totalSleep,
                inBedMinutes = inBed,
                deepMinutes = deep,
                lightMinutes = light,
                remMinutes = rem,
                awakeMinutes = awake,
                deepPercentage = deepPct,
                lightPercentage = lightPct,
                remPercentage = remPct,
                awakePercentage = awakePct,
                sleepEfficiencyPercentage = efficiency
            )
        }
    }
}
