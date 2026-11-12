package com.pulsehealth.app.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.aggregate.AggregateMetric
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

    suspend fun read(range: TimeRange, fallback: DashboardMetrics): DashboardMetrics {
        if (!hasPermissions()) return fallback
        val client = HealthConnectClient.getOrCreate(context)
        val end = Instant.now()
        val start = end.minus(Duration.ofDays(range.days))
        val filter = TimeRangeFilter.between(start, end)

        val totals = client.aggregate(
            AggregateRequest(
                metrics = setOf(
                    StepsRecord.COUNT_TOTAL,
                    ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL
                ),
                timeRangeFilter = filter
            )
        )
        val heartRates = client.readRecords(
            ReadRecordsRequest(HeartRateRecord::class, filter, ascendingOrder = false, pageSize = 20)
        ).records.flatMap { it.samples }
        val sleep = client.readRecords(
            ReadRecordsRequest(SleepSessionRecord::class, filter, pageSize = 100)
        ).records.sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }

        return fallback.copy(
            steps = totals[StepsRecord.COUNT_TOTAL] ?: fallback.steps,
            activeCalories = totals[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]
                ?.inKilocalories?.toLong() ?: fallback.activeCalories,
            heartRate = heartRates.firstOrNull()?.beatsPerMinute ?: fallback.heartRate,
            sleepMinutes = sleep.takeIf { it > 0 } ?: fallback.sleepMinutes,
            source = DataSource.HEALTH_CONNECT
        )
    }
}
