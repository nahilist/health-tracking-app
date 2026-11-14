package com.pulsehealth.app.data

import com.pulsehealth.app.data.local.HydrationEntryEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object HydrationTrends {

    /**
     * Calculates hydration streak information and 7-day progress bars.
     *
     * @param entries Past entries up to 14-30 days
     * @param goalLiters Daily water goal in liters
     * @param zoneId User's active timezone
     * @param nowEpochMillis Current wall-clock millis
     */
    fun calculateStreaksAndProgress(
        entries: List<HydrationEntryEntity>,
        goalLiters: Double,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowEpochMillis: Long = System.currentTimeMillis()
    ): Pair<HydrationStreakInfo, List<DailyHydrationProgress>> {
        val today = Instant.ofEpochMilli(nowEpochMillis).atZone(zoneId).toLocalDate()

        val dailyLiters = entries.groupBy { entry ->
            Instant.ofEpochMilli(entry.recordedAtEpochMillis).atZone(zoneId).toLocalDate()
        }.mapValues { (_, dayEntries) ->
            dayEntries.sumOf { it.amountMilliliters } / 1000.0
        }

        // Weekly progress: 7 days window (from 6 days ago up to today)
        val weeklyProgress = (6 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val water = dailyLiters[date] ?: 0.0
            val dayLabel = when (daysAgo) {
                0 -> "Today"
                else -> date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
            }
            DailyHydrationProgress(
                date = date,
                dayLabel = dayLabel,
                waterLiters = water,
                goalLiters = goalLiters,
                isToday = (daysAgo == 0)
            )
        }

        val isGoalMetToday = (dailyLiters[today] ?: 0.0) >= goalLiters && goalLiters > 0.0

        // Current streak: if met today count from today, otherwise preserve streak ending yesterday
        var currentStreak = 0
        var dateCursor = if (isGoalMetToday) today else today.minusDays(1)
        while (true) {
            val water = dailyLiters[dateCursor] ?: 0.0
            if (water >= goalLiters && goalLiters > 0.0) {
                currentStreak++
                dateCursor = dateCursor.minusDays(1)
            } else {
                break
            }
        }

        // Best streak calculation across all recorded history
        val achievedDates = dailyLiters.filter { it.value >= goalLiters && goalLiters > 0.0 }
            .keys.sorted()

        var bestStreak = 0
        var tempStreak = 0
        var prevDate: LocalDate? = null

        for (d in achievedDates) {
            if (prevDate != null && d == prevDate.plusDays(1)) {
                tempStreak++
            } else {
                tempStreak = 1
            }
            if (tempStreak > bestStreak) {
                bestStreak = tempStreak
            }
            prevDate = d
        }
        if (currentStreak > bestStreak) {
            bestStreak = currentStreak
        }

        val daysMetThisWeek = weeklyProgress.count { it.isGoalAchieved }

        val streakInfo = HydrationStreakInfo(
            currentStreakDays = currentStreak,
            bestStreakDays = bestStreak,
            daysMetThisWeek = daysMetThisWeek,
            isGoalMetToday = isGoalMetToday
        )

        return streakInfo to weeklyProgress
    }
}
