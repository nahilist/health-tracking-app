package com.pulsehealth.app.data

import com.pulsehealth.app.data.local.VitalRecordDao
import com.pulsehealth.app.data.local.VitalRecordEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.Clock
import java.util.UUID

interface VitalsRepository {
    fun observeVitals(type: VitalType): Flow<List<VitalRecordEntity>>
    fun observeRecentVitals(limit: Int = 50): Flow<List<VitalRecordEntity>>
    fun observeVitalsInRange(startEpochMillis: Long, endEpochMillis: Long): Flow<List<VitalRecordEntity>>
    suspend fun logVital(
        type: VitalType,
        systolic: Int? = null,
        diastolic: Int? = null,
        heartRateBpm: Int? = null,
        context: MeasurementContext = MeasurementContext.GENERAL,
        notes: String? = null,
        source: String = "manual"
    ): Result<Unit>
    suspend fun deleteVital(id: String): Result<Unit>
}

class DefaultVitalsRepository(
    private val vitalRecordDao: VitalRecordDao,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : VitalsRepository {

    override fun observeVitals(type: VitalType): Flow<List<VitalRecordEntity>> {
        return vitalRecordDao.observeRecordsForType(type.name)
    }

    override fun observeRecentVitals(limit: Int): Flow<List<VitalRecordEntity>> {
        return vitalRecordDao.observeRecentRecords(limit)
    }

    override fun observeVitalsInRange(startEpochMillis: Long, endEpochMillis: Long): Flow<List<VitalRecordEntity>> {
        return vitalRecordDao.observeRecordsInRange(startEpochMillis, endEpochMillis)
    }

    override suspend fun logVital(
        type: VitalType,
        systolic: Int?,
        diastolic: Int?,
        heartRateBpm: Int?,
        context: MeasurementContext,
        notes: String?,
        source: String
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            when (type) {
                VitalType.HEART_RATE -> {
                    val bpm = heartRateBpm ?: return@withContext Result.failure(
                        IllegalArgumentException("Heart rate BPM is required.")
                    )
                    if (bpm !in 30..250) {
                        return@withContext Result.failure(
                            IllegalArgumentException("Heart rate must be between 30 and 250 BPM.")
                        )
                    }
                }
                VitalType.BLOOD_PRESSURE -> {
                    val sys = systolic ?: return@withContext Result.failure(
                        IllegalArgumentException("Systolic pressure is required.")
                    )
                    val dia = diastolic ?: return@withContext Result.failure(
                        IllegalArgumentException("Diastolic pressure is required.")
                    )
                    if (sys !in 50..300) {
                        return@withContext Result.failure(
                            IllegalArgumentException("Systolic value must be between 50 and 300 mmHg.")
                        )
                    }
                    if (dia !in 30..200) {
                        return@withContext Result.failure(
                            IllegalArgumentException("Diastolic value must be between 30 and 200 mmHg.")
                        )
                    }
                    if (sys <= dia) {
                        return@withContext Result.failure(
                            IllegalArgumentException("Systolic pressure must be higher than diastolic pressure.")
                        )
                    }
                }
            }

            val entity = VitalRecordEntity(
                id = UUID.randomUUID().toString(),
                vitalType = type.name,
                systolic = systolic,
                diastolic = diastolic,
                heartRateBpm = heartRateBpm,
                recordedAtEpochMillis = clock.millis(),
                measurementContext = context.name,
                notes = notes?.trim()?.takeIf { it.isNotEmpty() },
                source = source,
                syncState = "pending"
            )
            vitalRecordDao.insertRecord(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteVital(id: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            vitalRecordDao.deleteRecord(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class NoOpVitalsRepository : VitalsRepository {
    override fun observeVitals(type: VitalType): Flow<List<VitalRecordEntity>> = kotlinx.coroutines.flow.flowOf(emptyList())
    override fun observeRecentVitals(limit: Int): Flow<List<VitalRecordEntity>> = kotlinx.coroutines.flow.flowOf(emptyList())
    override fun observeVitalsInRange(startEpochMillis: Long, endEpochMillis: Long): Flow<List<VitalRecordEntity>> = kotlinx.coroutines.flow.flowOf(emptyList())
    override suspend fun logVital(
        type: VitalType,
        systolic: Int?,
        diastolic: Int?,
        heartRateBpm: Int?,
        context: MeasurementContext,
        notes: String?,
        source: String
    ): Result<Unit> = Result.success(Unit)
    override suspend fun deleteVital(id: String): Result<Unit> = Result.success(Unit)
}
