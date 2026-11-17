package com.pulsehealth.app.data

import com.pulsehealth.app.data.local.SleepDao
import com.pulsehealth.app.data.local.SleepSessionEntity
import com.pulsehealth.app.data.local.SleepSessionWithStages
import com.pulsehealth.app.data.local.SleepStageEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import kotlin.math.roundToInt

interface SleepRepository {
    fun observeSleepWithStagesInRange(startEpochMillis: Long, endEpochMillis: Long): Flow<List<SleepSessionWithStages>>
    fun observeAllSleepWithStages(): Flow<List<SleepSessionWithStages>>
    fun observeTodaySleep(): Flow<SleepSessionWithStages?>
    fun observeLatestSleep(): Flow<SleepSessionWithStages?>

    suspend fun logManualSleep(
        startTimeEpochMillis: Long,
        endTimeEpochMillis: Long,
        deepMinutes: Long? = null,
        lightMinutes: Long? = null,
        remMinutes: Long? = null,
        awakeMinutes: Long? = null,
        notes: String? = null
    ): SleepSessionWithStages

    suspend fun deleteSleepSession(id: String): Boolean
    suspend fun deleteAllSleep()
    suspend fun getAllSessions(): List<SleepSessionEntity>
    suspend fun importHealthConnectSleep(sessions: List<SleepSessionWithStages>)
}

class DefaultSleepRepository(
    private val sleepDao: SleepDao,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SleepRepository {

    override fun observeAllSleepWithStages(): Flow<List<SleepSessionWithStages>> =
        sleepDao.observeAllSleepWithStages()

    override fun observeSleepWithStagesInRange(startEpochMillis: Long, endEpochMillis: Long): Flow<List<SleepSessionWithStages>> =
        sleepDao.observeSleepWithStagesInRange(startEpochMillis, endEpochMillis)

    override fun observeTodaySleep(): Flow<SleepSessionWithStages?> {
        val today = Instant.ofEpochMilli(clock.millis()).atZone(zoneId).toLocalDate()
        // Typical sleep spans previous night: from yesterday 12:00 PM to today 11:59 PM
        val startOfDay = today.minusDays(1).atTime(12, 0).atZone(zoneId).toInstant().toEpochMilli()
        val endOfDay = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return sleepDao.observeSleepWithStagesInRange(startOfDay, endOfDay)
            .map { sessions -> sessions.maxByOrNull { it.session.startTimeEpochMillis } }
    }

    override fun observeLatestSleep(): Flow<SleepSessionWithStages?> =
        sleepDao.observeLatestSleepWithStages()

    override suspend fun logManualSleep(
        startTimeEpochMillis: Long,
        endTimeEpochMillis: Long,
        deepMinutes: Long?,
        lightMinutes: Long?,
        remMinutes: Long?,
        awakeMinutes: Long?,
        notes: String?
    ): SleepSessionWithStages = withContext(ioDispatcher) {
        require(endTimeEpochMillis > startTimeEpochMillis) { "End time must be after start time" }
        val totalDurationMinutes = ((endTimeEpochMillis - startTimeEpochMillis) / 60_000L).coerceAtLeast(1)
        require(totalDurationMinutes in 1..1440) { "Sleep duration must be between 1 minute and 24 hours" }

        val sessionId = UUID.randomUUID().toString()

        // Distribute stages if not explicitly provided (sensible realistic default distribution: 20% Deep, 55% Light, 20% REM, 5% Awake)
        val deep = deepMinutes ?: (totalDurationMinutes * 0.20).roundToInt().toLong()
        val rem = remMinutes ?: (totalDurationMinutes * 0.20).roundToInt().toLong()
        val awake = awakeMinutes ?: (totalDurationMinutes * 0.05).roundToInt().toLong()
        val light = lightMinutes ?: (totalDurationMinutes - deep - rem - awake).coerceAtLeast(0)

        val totalSleep = deep + light + rem
        val efficiency = if (totalDurationMinutes > 0) {
            ((totalSleep.toDouble() / totalDurationMinutes) * 100).roundToInt().coerceIn(0, 100)
        } else 0

        val stages = mutableListOf<SleepStageEntity>()
        var currentStart = startTimeEpochMillis

        // Synthesize sequential intervals for visualization if manual
        if (awake > 0) {
            val awakeEnd = currentStart + (awake * 60_000L)
            stages.add(
                SleepStageEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    stage = SleepStageType.AWAKE.name,
                    startTimeEpochMillis = currentStart,
                    endTimeEpochMillis = awakeEnd,
                    durationMinutes = awake
                )
            )
            currentStart = awakeEnd
        }

        if (light > 0) {
            val lightEnd = currentStart + (light * 60_000L)
            stages.add(
                SleepStageEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    stage = SleepStageType.LIGHT.name,
                    startTimeEpochMillis = currentStart,
                    endTimeEpochMillis = lightEnd,
                    durationMinutes = light
                )
            )
            currentStart = lightEnd
        }

        if (deep > 0) {
            val deepEnd = currentStart + (deep * 60_000L)
            stages.add(
                SleepStageEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    stage = SleepStageType.DEEP.name,
                    startTimeEpochMillis = currentStart,
                    endTimeEpochMillis = deepEnd,
                    durationMinutes = deep
                )
            )
            currentStart = deepEnd
        }

        if (rem > 0) {
            val remEnd = currentStart + (rem * 60_000L)
            stages.add(
                SleepStageEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    stage = SleepStageType.REM.name,
                    startTimeEpochMillis = currentStart,
                    endTimeEpochMillis = remEnd,
                    durationMinutes = rem
                )
            )
        }

        val session = SleepSessionEntity(
            id = sessionId,
            title = "Sleep Session",
            startTimeEpochMillis = startTimeEpochMillis,
            endTimeEpochMillis = endTimeEpochMillis,
            durationMinutes = totalDurationMinutes,
            deepDurationMinutes = deep,
            lightDurationMinutes = light,
            remDurationMinutes = rem,
            awakeDurationMinutes = awake,
            sleepEfficiencyPercentage = efficiency,
            source = "local",
            notes = notes,
            syncState = "pending"
        )

        sleepDao.upsertSessionWithStages(session, stages)
        SleepSessionWithStages(session, stages)
    }

    override suspend fun deleteSleepSession(id: String): Boolean = withContext(ioDispatcher) {
        sleepDao.deleteSessionById(id) > 0
    }

    override suspend fun deleteAllSleep() = withContext(ioDispatcher) {
        sleepDao.deleteAllStages()
        sleepDao.deleteAllSessions()
    }

    override suspend fun getAllSessions(): List<SleepSessionEntity> = withContext(ioDispatcher) {
        sleepDao.getAllSessions()
    }

    override suspend fun importHealthConnectSleep(sessions: List<SleepSessionWithStages>) = withContext(ioDispatcher) {
        for (item in sessions) {
            sleepDao.upsertSessionWithStages(item.session, item.stages)
        }
    }
}

class NoOpSleepRepository : SleepRepository {
    override fun observeSleepWithStagesInRange(startEpochMillis: Long, endEpochMillis: Long): Flow<List<SleepSessionWithStages>> =
        flowOf(emptyList())

    override fun observeAllSleepWithStages(): Flow<List<SleepSessionWithStages>> =
        flowOf(emptyList())

    override fun observeTodaySleep(): Flow<SleepSessionWithStages?> =
        flowOf(null)

    override fun observeLatestSleep(): Flow<SleepSessionWithStages?> =
        flowOf(null)

    override suspend fun logManualSleep(
        startTimeEpochMillis: Long,
        endTimeEpochMillis: Long,
        deepMinutes: Long?,
        lightMinutes: Long?,
        remMinutes: Long?,
        awakeMinutes: Long?,
        notes: String?
    ): SleepSessionWithStages {
        val session = SleepSessionEntity(
            id = "noop_id",
            title = "Sleep",
            startTimeEpochMillis = startTimeEpochMillis,
            endTimeEpochMillis = endTimeEpochMillis,
            durationMinutes = ((endTimeEpochMillis - startTimeEpochMillis) / 60_000L).coerceAtLeast(1),
            deepDurationMinutes = 0L,
            lightDurationMinutes = 0L,
            remDurationMinutes = 0L,
            awakeDurationMinutes = 0L,
            sleepEfficiencyPercentage = 100,
            source = "noop",
            notes = notes,
            syncState = "synced"
        )
        return SleepSessionWithStages(session, emptyList())
    }

    override suspend fun deleteSleepSession(id: String): Boolean = true
    override suspend fun deleteAllSleep() {}
    override suspend fun getAllSessions(): List<SleepSessionEntity> = emptyList()
    override suspend fun importHealthConnectSleep(sessions: List<SleepSessionWithStages>) {}
}
