package com.pulsehealth.app.data.medication

import com.pulsehealth.app.data.local.MedicationEntity
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class MedicationFrequency(val displayLabel: String, val timesPerDay: Int) {
    DAILY("Once Daily", 1),
    TWICE_DAILY("Twice Daily", 2),
    THREE_TIMES_DAILY("3x Daily", 3),
    WEEKLY("Weekly", 1),
    AS_NEEDED("As Needed (PRN)", 0);

    companion object {
        fun fromString(value: String): MedicationFrequency {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: DAILY
        }
    }
}

enum class DosageUnit(val displayLabel: String) {
    MG("mg"),
    MCG("mcg"),
    G("g"),
    ML("ml"),
    TABLETS("tabs"),
    CAPSULES("caps"),
    DROPS("drops"),
    PUFFS("puffs");

    companion object {
        fun fromString(value: String): DosageUnit {
            return entries.firstOrNull { it.displayLabel.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) } ?: MG
        }
    }
}

enum class DoseStatus {
    PENDING,
    TAKEN,
    SKIPPED
}

data class ScheduledDoseItem(
    val medication: MedicationEntity,
    val scheduledTimeEpochMillis: Long,
    val timeLabel: String,
    val status: DoseStatus,
    val logId: String? = null,
    val loggedAtEpochMillis: Long? = null
)

data class MedicationAdherenceSummary(
    val totalScheduledDoses: Int,
    val takenDoses: Int,
    val skippedDoses: Int,
    val adherencePercentage: Int,
    val currentStreakDays: Int
)
