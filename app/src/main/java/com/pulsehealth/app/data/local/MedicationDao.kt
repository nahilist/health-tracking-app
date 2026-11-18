package com.pulsehealth.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun observeAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY name ASC")
    fun observeActiveMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: String): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMedication(medication: MedicationEntity)

    @Query("UPDATE medications SET isActive = :isActive WHERE id = :id")
    suspend fun updateActiveStatus(id: String, isActive: Boolean)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedication(id: String)

    @Query("SELECT * FROM medication_logs WHERE medicationId = :medicationId ORDER BY scheduledTimeEpochMillis DESC")
    fun observeLogsForMedication(medicationId: String): Flow<List<MedicationLogEntity>>

    @Query("SELECT * FROM medication_logs WHERE scheduledTimeEpochMillis BETWEEN :startEpochMillis AND :endEpochMillis ORDER BY scheduledTimeEpochMillis ASC")
    fun observeLogsBetween(startEpochMillis: Long, endEpochMillis: Long): Flow<List<MedicationLogEntity>>

    @Query("SELECT * FROM medication_logs WHERE scheduledTimeEpochMillis BETWEEN :startEpochMillis AND :endEpochMillis")
    suspend fun getLogsBetween(startEpochMillis: Long, endEpochMillis: Long): List<MedicationLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: MedicationLogEntity)

    @Query("DELETE FROM medication_logs WHERE id = :id")
    suspend fun deleteLog(id: String)

    @Query("DELETE FROM medication_logs WHERE medicationId = :medicationId AND scheduledTimeEpochMillis = :scheduledTimeEpochMillis")
    suspend fun deleteLogForDose(medicationId: String, scheduledTimeEpochMillis: Long)
}
