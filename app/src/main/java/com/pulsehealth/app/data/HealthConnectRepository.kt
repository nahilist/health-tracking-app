package com.pulsehealth.app.data

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Volume
import com.pulsehealth.app.data.local.WorkoutSessionEntity
import com.pulsehealth.app.data.local.SleepSessionEntity
import com.pulsehealth.app.data.local.SleepStageEntity
import com.pulsehealth.app.data.local.SleepSessionWithStages
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

interface HealthConnectRepository {
    val readPermissions: Set<String>
    val writePermissions: Set<String>
    val allPermissions: Set<String>
    val permissions: Set<String>

    fun getAvailability(): HealthConnectAvailability
    fun isAvailable(): Boolean
    fun permissionContract(): ActivityResultContract<Set<String>, Set<String>>
    suspend fun hasPermissions(): Boolean
    suspend fun hasWriteHydrationPermission(): Boolean
    suspend fun read(range: TimeRange, current: DashboardMetrics): DashboardMetrics
    suspend fun writeHydration(amountMilliliters: Int, recordedAtEpochMillis: Long): Boolean
    suspend fun readExerciseSessions(range: TimeRange): List<WorkoutSessionEntity> = emptyList()
    suspend fun readSleepSessions(range: TimeRange): List<SleepSessionWithStages> = emptyList()
}

class DefaultHealthConnectRepository(
    private val context: Context,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : HealthConnectRepository {

    override val readPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    )

    override val writePermissions: Set<String> = setOf(
        HealthPermission.getWritePermission(HydrationRecord::class)
    )

    override val allPermissions: Set<String> = readPermissions + writePermissions

    override val permissions: Set<String> get() = readPermissions

    override fun getAvailability(): HealthConnectAvailability =
        when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectAvailability.UPDATE_REQUIRED
            else -> HealthConnectAvailability.NOT_SUPPORTED
        }

    override fun isAvailable(): Boolean =
        getAvailability() == HealthConnectAvailability.AVAILABLE

    override fun permissionContract(): ActivityResultContract<Set<String>, Set<String>> =
        PermissionController.createRequestPermissionResultContract()

    override suspend fun hasPermissions(): Boolean = withContext(ioDispatcher) {
        if (!isAvailable()) return@withContext false
        runCatching {
            val granted = HealthConnectClient.getOrCreate(context)
                .permissionController.getGrantedPermissions()
            granted.containsAll(readPermissions)
        }.getOrDefault(false)
    }

    override suspend fun hasWriteHydrationPermission(): Boolean = withContext(ioDispatcher) {
        if (!isAvailable()) return@withContext false
        runCatching {
            val granted = HealthConnectClient.getOrCreate(context)
                .permissionController.getGrantedPermissions()
            granted.containsAll(writePermissions)
        }.getOrDefault(false)
    }

    override suspend fun read(range: TimeRange, current: DashboardMetrics): DashboardMetrics = withContext(ioDispatcher) {
        if (!hasPermissions()) {
            return@withContext current.copy(
                source = if (current.waterLiters > 0.0) DataSource.LOCAL else DataSource.NONE
            )
        }
        val client = HealthConnectClient.getOrCreate(context)
        val now = Instant.ofEpochMilli(clock.millis())
        val today = now.atZone(zoneId).toLocalDate()

        val (startInstant, endInstant) = when (range) {
            TimeRange.TODAY -> {
                val start = today.atStartOfDay(zoneId).toInstant()
                val end = today.plusDays(1).atStartOfDay(zoneId).toInstant()
                start to end
            }
            TimeRange.WEEK -> {
                val start = today.minusDays(6).atStartOfDay(zoneId).toInstant()
                val end = today.plusDays(1).atStartOfDay(zoneId).toInstant()
                start to end
            }
            TimeRange.MONTH -> {
                val start = today.minusDays(29).atStartOfDay(zoneId).toInstant()
                val end = today.plusDays(1).atStartOfDay(zoneId).toInstant()
                start to end
            }
        }
        val filter = TimeRangeFilter.between(startInstant, endInstant)

        val totals = runCatching {
            client.aggregate(
                AggregateRequest(
                    metrics = setOf(
                        StepsRecord.COUNT_TOTAL,
                        ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL
                    ),
                    timeRangeFilter = filter
                )
            )
        }.getOrNull()

        val heartRateRecords = runCatching {
            client.readRecords(
                ReadRecordsRequest(HeartRateRecord::class, filter, ascendingOrder = false, pageSize = 50)
            ).records
        }.getOrDefault(emptyList())

        val sleepRecords = runCatching {
            client.readRecords(
                ReadRecordsRequest(SleepSessionRecord::class, filter, pageSize = 100)
            ).records
        }.getOrDefault(emptyList())

        val exerciseRecords = runCatching {
            client.readRecords(
                ReadRecordsRequest(ExerciseSessionRecord::class, filter, pageSize = 100)
            ).records
        }.getOrDefault(emptyList())

        val heartRates = heartRateRecords.flatMap { it.samples }
        val sleepTotalMinutes = sleepRecords.sumOf {
            Duration.between(it.startTime, it.endTime).toMinutes()
        }
        val exerciseTotalMinutes = exerciseRecords.sumOf {
            Duration.between(it.startTime, it.endTime).toMinutes()
        }

        val steps = totals?.get(StepsRecord.COUNT_TOTAL)
        val activeCalories = totals?.get(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL)
            ?.inKilocalories?.toLong()

        val heartRate = when {
            heartRates.isEmpty() -> null
            range == TimeRange.TODAY -> heartRates.first().beatsPerMinute
            else -> heartRates.map { it.beatsPerMinute }.average().toLong()
        }
        val sleepMinutes = sleepTotalMinutes.takeIf { it > 0 }
        val exerciseMinutes = exerciseTotalMinutes.takeIf { it > 0 }

        val apps = mutableSetOf<String>()
        exerciseRecords.forEach { apps.add(formatPackageName(it.metadata.dataOrigin.packageName)) }
        heartRateRecords.forEach { apps.add(formatPackageName(it.metadata.dataOrigin.packageName)) }
        sleepRecords.forEach { apps.add(formatPackageName(it.metadata.dataOrigin.packageName)) }
        if (current.waterLiters > 0.0) {
            apps.add("Pulse Health")
        }

        val hasHcData = steps != null || activeCalories != null || heartRate != null ||
            sleepMinutes != null || exerciseMinutes != null
        val source = if (hasHcData) {
            DataSource.HEALTH_CONNECT
        } else if (current.waterLiters > 0.0) {
            DataSource.LOCAL
        } else {
            DataSource.NONE
        }

        current.copy(
            steps = steps,
            activeCalories = activeCalories,
            heartRate = heartRate,
            sleepMinutes = sleepMinutes,
            exerciseMinutes = exerciseMinutes,
            source = source,
            lastUpdatedEpochMillis = now.toEpochMilli(),
            contributingApps = apps
        )
    }

    override suspend fun writeHydration(
        amountMilliliters: Int,
        recordedAtEpochMillis: Long
    ): Boolean = withContext(ioDispatcher) {
        if (!isAvailable() || !hasWriteHydrationPermission()) return@withContext false
        val client = HealthConnectClient.getOrCreate(context)
        val instant = Instant.ofEpochMilli(recordedAtEpochMillis)
        val offset = zoneId.rules.getOffset(instant)
        val record = HydrationRecord(
            startTime = instant,
            startZoneOffset = offset,
            endTime = instant,
            endZoneOffset = offset,
            volume = Volume.milliliters(amountMilliliters.toDouble()),
            metadata = Metadata.manualEntry()
        )
        runCatching {
            client.insertRecords(listOf(record))
            true
        }.getOrDefault(false)
    }

    override suspend fun readExerciseSessions(range: TimeRange): List<WorkoutSessionEntity> = withContext(ioDispatcher) {
        if (!hasPermissions()) return@withContext emptyList()
        val client = HealthConnectClient.getOrCreate(context)
        val now = Instant.ofEpochMilli(clock.millis())
        val today = now.atZone(zoneId).toLocalDate()

        val (startInstant, endInstant) = when (range) {
            TimeRange.TODAY -> {
                today.atStartOfDay(zoneId).toInstant() to today.plusDays(1).atStartOfDay(zoneId).toInstant()
            }
            TimeRange.WEEK -> {
                today.minusDays(6).atStartOfDay(zoneId).toInstant() to today.plusDays(1).atStartOfDay(zoneId).toInstant()
            }
            TimeRange.MONTH -> {
                today.minusDays(29).atStartOfDay(zoneId).toInstant() to today.plusDays(1).atStartOfDay(zoneId).toInstant()
            }
        }
        val filter = TimeRangeFilter.between(startInstant, endInstant)

        val exerciseRecords = runCatching {
            client.readRecords(
                ReadRecordsRequest(ExerciseSessionRecord::class, filter, pageSize = 100)
            ).records
        }.getOrDefault(emptyList())

        exerciseRecords.map { record ->
            val durationMinutes = Duration.between(record.startTime, record.endTime).toMinutes().coerceAtLeast(1)
            val workoutType = when (record.exerciseType) {
                ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> WorkoutType.RUNNING
                ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> WorkoutType.WALKING
                ExerciseSessionRecord.EXERCISE_TYPE_BIKING -> WorkoutType.CYCLING
                ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL,
                ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER -> WorkoutType.SWIMMING
                ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING,
                ExerciseSessionRecord.EXERCISE_TYPE_WEIGHTLIFTING,
                ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS -> WorkoutType.STRENGTH
                ExerciseSessionRecord.EXERCISE_TYPE_YOGA,
                ExerciseSessionRecord.EXERCISE_TYPE_PILATES -> WorkoutType.YOGA
                ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING -> WorkoutType.HIIT
                else -> WorkoutType.OTHER
            }
            WorkoutSessionEntity(
                id = "hc_${record.metadata.id}",
                workoutType = workoutType.name,
                title = record.title?.takeIf { it.isNotBlank() } ?: workoutType.defaultTitle,
                startTimeEpochMillis = record.startTime.toEpochMilli(),
                endTimeEpochMillis = record.endTime.toEpochMilli(),
                durationMinutes = durationMinutes,
                activeCalories = (durationMinutes * workoutType.defaultCaloriesPerMinute).toInt(),
                distanceMeters = null,
                source = "health_connect",
                notes = record.notes,
                syncState = "synced"
            )
        }
    }

    override suspend fun readSleepSessions(range: TimeRange): List<SleepSessionWithStages> = withContext(ioDispatcher) {
        if (!isAvailable() || !hasPermissions()) return@withContext emptyList()
        val client = HealthConnectClient.getOrCreate(context)
        val now = Instant.ofEpochMilli(clock.millis())
        val today = now.atZone(zoneId).toLocalDate()

        val (startInstant, endInstant) = when (range) {
            TimeRange.TODAY -> {
                // Sleep typically began the night before
                today.minusDays(1).atTime(12, 0).atZone(zoneId).toInstant() to today.plusDays(1).atStartOfDay(zoneId).toInstant()
            }
            TimeRange.WEEK -> {
                today.minusDays(6).atStartOfDay(zoneId).toInstant() to today.plusDays(1).atStartOfDay(zoneId).toInstant()
            }
            TimeRange.MONTH -> {
                today.minusDays(29).atStartOfDay(zoneId).toInstant() to today.plusDays(1).atStartOfDay(zoneId).toInstant()
            }
        }
        val filter = TimeRangeFilter.between(startInstant, endInstant)

        val sleepRecords = runCatching {
            client.readRecords(
                ReadRecordsRequest(SleepSessionRecord::class, filter, pageSize = 100)
            ).records
        }.getOrDefault(emptyList())

        sleepRecords.map { record ->
            val durationMinutes = Duration.between(record.startTime, record.endTime).toMinutes().coerceAtLeast(1)
            val sessionId = "hc_sleep_${record.metadata.id}"
            val stagesList = mutableListOf<SleepStageEntity>()

            if (record.stages.isNotEmpty()) {
                record.stages.forEachIndexed { index, stageRecord ->
                    val stageDuration = Duration.between(stageRecord.startTime, stageRecord.endTime).toMinutes().coerceAtLeast(1)
                    val stageType = when (stageRecord.stage) {
                        SleepSessionRecord.STAGE_TYPE_DEEP -> SleepStageType.DEEP
                        SleepSessionRecord.STAGE_TYPE_LIGHT -> SleepStageType.LIGHT
                        SleepSessionRecord.STAGE_TYPE_REM -> SleepStageType.REM
                        SleepSessionRecord.STAGE_TYPE_AWAKE,
                        SleepSessionRecord.STAGE_TYPE_OUT_OF_BED -> SleepStageType.AWAKE
                        SleepSessionRecord.STAGE_TYPE_SLEEPING -> SleepStageType.LIGHT
                        else -> SleepStageType.UNKNOWN
                    }
                    stagesList.add(
                        SleepStageEntity(
                            id = "${sessionId}_stage_$index",
                            sessionId = sessionId,
                            stage = stageType.name,
                            startTimeEpochMillis = stageRecord.startTime.toEpochMilli(),
                            endTimeEpochMillis = stageRecord.endTime.toEpochMilli(),
                            durationMinutes = stageDuration
                        )
                    )
                }
            } else {
                stagesList.add(
                    SleepStageEntity(
                        id = "${sessionId}_stage_default",
                        sessionId = sessionId,
                        stage = SleepStageType.LIGHT.name,
                        startTimeEpochMillis = record.startTime.toEpochMilli(),
                        endTimeEpochMillis = record.endTime.toEpochMilli(),
                        durationMinutes = durationMinutes
                    )
                )
            }

            val breakdown = SleepBreakdownSummary.fromStages(stagesList, durationMinutes)
            val session = SleepSessionEntity(
                id = sessionId,
                title = record.title?.takeIf { it.isNotBlank() } ?: "Sleep Session",
                startTimeEpochMillis = record.startTime.toEpochMilli(),
                endTimeEpochMillis = record.endTime.toEpochMilli(),
                durationMinutes = durationMinutes,
                deepDurationMinutes = breakdown.deepMinutes,
                lightDurationMinutes = breakdown.lightMinutes,
                remDurationMinutes = breakdown.remMinutes,
                awakeDurationMinutes = breakdown.awakeMinutes,
                sleepEfficiencyPercentage = breakdown.sleepEfficiencyPercentage,
                source = "health_connect",
                notes = record.notes,
                syncState = "synced"
            )

            SleepSessionWithStages(session, stagesList)
        }
    }

    private fun formatPackageName(pkg: String): String = when (pkg) {
        "com.google.android.apps.fitness" -> "Google Fit"
        "com.sec.android.app.shealth" -> "Samsung Health"
        "com.garmin.android.apps.connectmobile" -> "Garmin Connect"
        "com.fitbit.FitbitMobile" -> "Fitbit"
        "com.pulsehealth.app", "com.pulsehealth.app.debug" -> "Pulse Health"
        else -> pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }
    }
}
