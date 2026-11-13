package com.pulsehealth.app.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant

class HealthConnectRepository(private val context: Context) {
    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class)
    )

    fun isAvailable(): Boolean =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    fun permissionContract() = PermissionController.createRequestPermissionResultContract()

    suspend fun hasPermissions(): Boolean {
        if (!isAvailable()) return false
        return HealthConnectClient.getOrCreate(context)
            .permissionController.getGrantedPermissions().containsAll(permissions)
    }

    suspend fun read(range: TimeRange, current: DashboardMetrics): DashboardMetrics {
        if (!hasPermissions()) {
            return current.copy(
                source = if (current.waterLiters > 0.0) DataSource.LOCAL else DataSource.NONE
            )
        }
        val client = HealthConnectClient.getOrCreate(context)
        val end = Instant.now()
        val start = end.minus(Duration.ofDays(range.days))
        val filter = TimeRangeFilter.between(start, end)

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

        val heartRates = runCatching {
            client.readRecords(
                ReadRecordsRequest(HeartRateRecord::class, filter, ascendingOrder = false, pageSize = 20)
            ).records.flatMap { it.samples }
        }.getOrDefault(emptyList())

        val sleep = runCatching {
            client.readRecords(
                ReadRecordsRequest(SleepSessionRecord::class, filter, pageSize = 100)
            ).records.sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }
        }.getOrDefault(0L)

        val steps = totals?.get(StepsRecord.COUNT_TOTAL)
        val activeCalories = totals?.get(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL)
            ?.inKilocalories?.toLong()
        val heartRate = heartRates.firstOrNull()?.beatsPerMinute
        val sleepMinutes = sleep.takeIf { it > 0 }

        val hasHcData = steps != null || activeCalories != null || heartRate != null || sleepMinutes != null
        val source = if (hasHcData) {
            DataSource.HEALTH_CONNECT
        } else if (current.waterLiters > 0.0) {
            DataSource.LOCAL
        } else {
            DataSource.NONE
        }

        return current.copy(
            steps = steps,
            activeCalories = activeCalories,
            heartRate = heartRate,
            sleepMinutes = sleepMinutes,
            source = source,
            lastUpdatedEpochMillis = end.toEpochMilli()
        )
    }
}
