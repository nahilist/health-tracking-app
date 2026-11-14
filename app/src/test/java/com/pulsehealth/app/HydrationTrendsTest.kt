package com.pulsehealth.app

import com.pulsehealth.app.data.HydrationTrends
import com.pulsehealth.app.data.local.HydrationEntryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class HydrationTrendsTest {

    private val zoneId = ZoneId.of("UTC")
    private val baseDate = LocalDate.of(2026, 11, 14)
    private val nowEpochMillis = baseDate.atStartOfDay(zoneId).plusHours(12).toInstant().toEpochMilli()

    private fun entry(daysAgo: Long, amountMl: Int): HydrationEntryEntity {
        val date = baseDate.minusDays(daysAgo)
        val epochMillis = date.atStartOfDay(zoneId).plusHours(10).toInstant().toEpochMilli()
        return HydrationEntryEntity(
            id = "entry_$daysAgo",
            amountMilliliters = amountMl,
            recordedAtEpochMillis = epochMillis,
            source = "manual",
            syncState = "synced"
        )
    }

    @Test
    fun emptyEntriesReturnsZeroStreakAndSevenDaysWindow() {
        val (streak, progress) = HydrationTrends.calculateStreaksAndProgress(
            entries = emptyList(),
            goalLiters = 2.0,
            zoneId = zoneId,
            nowEpochMillis = nowEpochMillis
        )

        assertEquals(0, streak.currentStreakDays)
        assertEquals(0, streak.bestStreakDays)
        assertEquals(0, streak.daysMetThisWeek)
        assertFalse(streak.isGoalMetToday)
        assertEquals(7, progress.size)
        assertTrue(progress.all { it.waterLiters == 0.0 })
    }

    @Test
    fun goalMetTodayComputesActiveStreakConsecutiveDays() {
        val entries = listOf(
            entry(0, 2500), // Today met
            entry(1, 2000), // Yesterday met
            entry(2, 2200), // 2 days ago met
            entry(3, 1000)  // 3 days ago missed
        )

        val (streak, progress) = HydrationTrends.calculateStreaksAndProgress(
            entries = entries,
            goalLiters = 2.0,
            zoneId = zoneId,
            nowEpochMillis = nowEpochMillis
        )

        assertEquals(3, streak.currentStreakDays)
        assertEquals(3, streak.bestStreakDays)
        assertEquals(3, streak.daysMetThisWeek)
        assertTrue(streak.isGoalMetToday)

        val todayProgress = progress.last()
        assertTrue(todayProgress.isToday)
        assertEquals(2.5, todayProgress.waterLiters, 0.0)
        assertTrue(todayProgress.isGoalAchieved)
    }

    @Test
    fun preservesStreakEndingYesterdayWhileTodayInProgress() {
        val entries = listOf(
            entry(0, 500),  // Today in progress (< 2.0L)
            entry(1, 2100), // Yesterday met
            entry(2, 2400)  // 2 days ago met
        )

        val (streak, progress) = HydrationTrends.calculateStreaksAndProgress(
            entries = entries,
            goalLiters = 2.0,
            zoneId = zoneId,
            nowEpochMillis = nowEpochMillis
        )

        assertEquals(2, streak.currentStreakDays)
        assertFalse(streak.isGoalMetToday)
        assertEquals(2, streak.daysMetThisWeek)
    }

    @Test
    fun resetsStreakWhenYesterdayWasMissedAndTodayNotYetMet() {
        val entries = listOf(
            entry(0, 500),  // Today in progress
            entry(1, 1000), // Yesterday missed
            entry(2, 2400)  // 2 days ago met
        )

        val (streak, _) = HydrationTrends.calculateStreaksAndProgress(
            entries = entries,
            goalLiters = 2.0,
            zoneId = zoneId,
            nowEpochMillis = nowEpochMillis
        )

        assertEquals(0, streak.currentStreakDays)
        assertFalse(streak.isGoalMetToday)
    }

    @Test
    fun computesBestStreakAcrossEntireHistory() {
        val entries = listOf(
            entry(0, 2000), // Today met (streak = 2)
            entry(1, 2000), // Yesterday met
            entry(2, 500),  // Break
            entry(3, 2000), // Past 4-day streak
            entry(4, 2000),
            entry(5, 2000),
            entry(6, 2000),
            entry(7, 500)
        )

        val (streak, _) = HydrationTrends.calculateStreaksAndProgress(
            entries = entries,
            goalLiters = 2.0,
            zoneId = zoneId,
            nowEpochMillis = nowEpochMillis
        )

        assertEquals(2, streak.currentStreakDays)
        assertEquals(4, streak.bestStreakDays)
    }
}
