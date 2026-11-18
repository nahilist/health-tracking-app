package com.pulsehealth.app.data.analytics

import com.pulsehealth.app.data.HydrationRepository
import com.pulsehealth.app.data.HydrationTrends
import com.pulsehealth.app.data.PreferencesRepository
import com.pulsehealth.app.data.SleepRepository
import com.pulsehealth.app.data.VitalsRepository
import com.pulsehealth.app.data.local.DailyHealthSummaryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

interface InsightsRepository {
    fun observeInsights(): Flow<List<HealthInsight>>
    fun observeInsightsForMetric(metricType: String): Flow<List<HealthInsight>>
}

class DefaultInsightsRepository(
    private val dailySummaryDao: DailyHealthSummaryDao,
    private val vitalsRepository: VitalsRepository,
    private val hydrationRepository: HydrationRepository,
    private val sleepRepository: SleepRepository,
    private val preferencesRepository: PreferencesRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : InsightsRepository {

    override fun observeInsights(): Flow<List<HealthInsight>> {
        val now = LocalDate.now(clock)
        val endEpochDay = now.toEpochDay()
        val startEpochDay = now.minusDays(30).toEpochDay()

        val summariesFlow = dailySummaryDao.observeRange(startEpochDay, endEpochDay)
        val vitalsFlow = vitalsRepository.observeRecentVitals(50)
        val hydrationEntriesFlow = hydrationRepository.observePastDaysEntries(14)
        val sleepFlow = sleepRepository.observeAllSleepWithStages()

        val goalsFlow = combine(
            preferencesRepository.stepGoal,
            preferencesRepository.sleepGoalMinutes,
            preferencesRepository.waterGoalLiters
        ) { stepGoal, sleepGoal, waterGoal ->
            Triple(stepGoal, sleepGoal, waterGoal)
        }

        val dataFlowA = combine(summariesFlow, vitalsFlow) { summaries, vitals ->
            summaries to vitals
        }
        val dataFlowB = combine(hydrationEntriesFlow, sleepFlow) { hydrationEntries, sleepSessions ->
            hydrationEntries to sleepSessions
        }

        return combine(
            dataFlowA,
            dataFlowB,
            goalsFlow
        ) { (summaries, vitals), (hydrationEntries, sleepSessions), (stepGoal, sleepGoal, waterGoal) ->
            val (_, hydrationProgress) = HydrationTrends.calculateStreaksAndProgress(
                entries = hydrationEntries,
                goalLiters = waterGoal,
                zoneId = zoneId,
                nowEpochMillis = clock.millis()
            )

            HealthAnomalyDetector.detect(
                summaries = summaries,
                vitalRecords = vitals,
                hydrationProgress = hydrationProgress,
                sleepSessions = sleepSessions,
                stepGoal = stepGoal,
                sleepGoalMinutes = sleepGoal,
                waterGoalLiters = waterGoal
            )
        }
    }

    override fun observeInsightsForMetric(metricType: String): Flow<List<HealthInsight>> {
        return observeInsights().combine(flowOf(metricType)) { insights, type ->
            insights.filter { it.metricType.equals(type, ignoreCase = true) }
        }
    }
}

class NoOpInsightsRepository : InsightsRepository {
    override fun observeInsights(): Flow<List<HealthInsight>> = flowOf(emptyList())
    override fun observeInsightsForMetric(metricType: String): Flow<List<HealthInsight>> = flowOf(emptyList())
}
