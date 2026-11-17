package com.pulsehealth.app.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import com.pulsehealth.app.data.local.WorkoutSessionEntity

enum class WorkoutType(
    val label: String,
    val defaultTitle: String,
    val icon: ImageVector,
    val defaultCaloriesPerMinute: Double
) {
    RUNNING("Running", "Outdoor Run", Icons.AutoMirrored.Filled.DirectionsRun, 11.5),
    WALKING("Walking", "Outdoor Walk", Icons.AutoMirrored.Filled.DirectionsWalk, 4.5),
    CYCLING("Cycling", "Cycling Workout", Icons.AutoMirrored.Filled.DirectionsBike, 8.5),
    STRENGTH("Strength", "Strength Training", Icons.Default.FitnessCenter, 6.0),
    SWIMMING("Swimming", "Pool Swimming", Icons.Default.Pool, 10.0),
    YOGA("Yoga", "Yoga & Flexibility", Icons.Default.SelfImprovement, 3.5),
    HIIT("HIIT", "High Intensity Interval", Icons.Default.Timer, 12.0),
    OTHER("Workout", "General Workout", Icons.Default.Sports, 7.0);

    companion object {
        fun fromString(value: String): WorkoutType {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true)
            } ?: OTHER
        }
    }
}

fun WorkoutSessionEntity.toDomain(): WorkoutSession = WorkoutSession(
    id = id,
    workoutType = WorkoutType.fromString(workoutType),
    title = title,
    startTimeEpochMillis = startTimeEpochMillis,
    endTimeEpochMillis = endTimeEpochMillis,
    durationMinutes = durationMinutes,
    activeCalories = activeCalories,
    distanceMeters = distanceMeters,
    source = when (source.lowercase()) {
        "health_connect", "health connect" -> DataSource.HEALTH_CONNECT
        "demo" -> DataSource.DEMO
        else -> DataSource.LOCAL
    },
    notes = notes
)

data class WorkoutSession(
    val id: String,
    val workoutType: WorkoutType,
    val title: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val durationMinutes: Long,
    val activeCalories: Int? = null,
    val distanceMeters: Double? = null,
    val source: DataSource = DataSource.LOCAL,
    val notes: String? = null
) {
    val distanceKilometers: Double?
        get() = distanceMeters?.let { it / 1000.0 }
}
