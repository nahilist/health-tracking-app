package com.pulsehealth.app.widget

import android.content.Context
import com.pulsehealth.app.data.local.PulseDatabase
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class WidgetState(
    val waterLiters: Double = 0.0,
    val waterGoalLiters: Double = 2.0,
    val steps: Long = 0L,
    val activeCalories: Long = 0L,
    val isGoalAchieved: Boolean = false,
    val dateLabel: String = ""
) {
    val progressRatio: Float
        get() = if (waterGoalLiters > 0.0) (waterLiters / waterGoalLiters).coerceIn(0.0, 1.0).toFloat() else 0f

    val percentInt: Int
        get() = (progressRatio * 100).toInt()
}

object WidgetDataProvider {

    suspend fun loadState(context: Context): WidgetState {
        val db = PulseDatabase.get(context)
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val startOfDay = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val totalMl = runCatching {
            db.hydrationDao().observeTotalMilliliters(startOfDay, endOfDay).first()
        }.getOrDefault(0)

        val waterLiters = totalMl / 1000.0

        val summary = runCatching {
            db.dailyHealthSummaryDao().getByDay(today.toEpochDay())
        }.getOrNull()

        val steps = summary?.steps ?: 0L
        val activeCalories = summary?.activeCalories ?: 0L

        val dateLabel = today.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))

        return WidgetState(
            waterLiters = waterLiters,
            waterGoalLiters = 2.0,
            steps = steps,
            activeCalories = activeCalories,
            isGoalAchieved = waterLiters >= 2.0,
            dateLabel = dateLabel
        )
    }
}
