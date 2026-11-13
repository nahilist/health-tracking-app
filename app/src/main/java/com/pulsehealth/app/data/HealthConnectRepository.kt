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

    private fun formatPackageName(pkg: String): String = when (pkg) {
        "com.google.android.apps.fitness" -> "Google Fit"
        "com.sec.android.app.shealth" -> "Samsung Health"
        "com.garmin.android.apps.connectmobile" -> "Garmin Connect"
        "com.fitbit.FitbitMobile" -> "Fitbit"
        "com.pulsehealth.app", "com.pulsehealth.app.debug" -> "Pulse Health"
        else -> pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }
    }
}
