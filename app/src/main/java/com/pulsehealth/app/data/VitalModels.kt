package com.pulsehealth.app.data

import androidx.compose.ui.graphics.Color
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.WarmOrange

enum class VitalType {
    HEART_RATE,
    BLOOD_PRESSURE
}

enum class MeasurementContext(val label: String) {
    RESTING("Resting"),
    POST_EXERCISE("Post-Workout"),
    GENERAL("General")
}

data class BloodPressureCategory(
    val label: String,
    val description: String,
    val color: Color
)

object BloodPressureClassification {

    val NORMAL = BloodPressureCategory(
        label = "Normal Range",
        description = "Systolic less than 120 mmHg and diastolic less than 80 mmHg. Indicates optimal cardiovascular circulation.",
        color = ExerciseGreen
    )

    val ELEVATED = BloodPressureCategory(
        label = "Elevated",
        description = "Systolic 120–129 mmHg and diastolic less than 80 mmHg. Lifestyle and hydration adjustments may help maintain baseline.",
        color = WarmOrange
    )

    val STAGE_1 = BloodPressureCategory(
        label = "Stage 1 Range",
        description = "Systolic 130–139 mmHg or diastolic 80–89 mmHg. Consider tracking over consecutive days at the same time.",
        color = PulsePink
    )

    val STAGE_2 = BloodPressureCategory(
        label = "Stage 2 Range",
        description = "Systolic 140 mmHg or higher, or diastolic 90 mmHg or higher. Share records with your healthcare provider for evaluation.",
        color = PulsePink
    )

    val CRITICAL = BloodPressureCategory(
        label = "Consult Healthcare Provider",
        description = "Systolic higher than 180 mmHg and/or diastolic higher than 120 mmHg. Seek prompt medical advice if confirmed.",
        color = Color(0xFFD32F2F)
    )

    fun classify(systolic: Int, diastolic: Int): BloodPressureCategory {
        return when {
            systolic > 180 || diastolic > 120 -> CRITICAL
            systolic >= 140 || diastolic >= 90 -> STAGE_2
            systolic >= 130 || diastolic >= 80 -> STAGE_1
            systolic in 120..129 && diastolic < 80 -> ELEVATED
            else -> NORMAL
        }
    }
}

object HeartRateClassification {

    fun classifyRestingBpm(bpm: Int): String {
        return when {
            bpm < 50 -> "Athletic / Low"
            bpm in 50..69 -> "Optimal Resting"
            bpm in 70..85 -> "Typical Resting"
            bpm in 86..100 -> "Elevated Resting"
            else -> "High"
        }
    }
}
