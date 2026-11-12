package com.pulsehealth.app.data

import com.pulsehealth.app.data.local.HydrationDao
import com.pulsehealth.app.data.local.HydrationEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

class HealthDataRepository(
    private val hydrationDao: HydrationDao,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {
    val todayWaterLiters: Flow<Double>
        get() {
            val (start, end) = todayBounds()
            return hydrationDao.observeTotalMilliliters(start, end).map { it / 1_000.0 }
        }

    suspend fun addWater(amountMilliliters: Int = 250) {
        require(amountMilliliters in 1..5_000) { "Hydration amount must be between 1 and 5000 mL" }
        hydrationDao.upsert(
            HydrationEntryEntity(
                id = UUID.randomUUID().toString(),
                amountMilliliters = amountMilliliters,
                recordedAtEpochMillis = clock.millis(),
                source = "manual",
                syncState = "pending"
            )
        )
    }

    private fun todayBounds(): Pair<Long, Long> {
        val today = Instant.ofEpochMilli(clock.millis()).atZone(zoneId).toLocalDate()
        val start = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return start to end
    }
}
