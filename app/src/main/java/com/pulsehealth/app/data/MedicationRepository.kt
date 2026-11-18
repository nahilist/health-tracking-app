package com.pulsehealth.app.data

import com.pulsehealth.app.data.local.MedicationDao
import com.pulsehealth.app.data.local.MedicationEntity
import com.pulsehealth.app.data.local.MedicationLogEntity
import com.pulsehealth.app.data.medication.DoseStatus
import com.pulsehealth.app.data.medication.MedicationAdherenceSummary
import com.pulsehealth.app.data.medication.ScheduledDoseItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

interface MedicationRepository {
    fun observeAllMedications(): Flow<List<MedicationEntity>>
    fun observeActiveMedications(): Flow<List<MedicationEntity>>
    suspend fun getMedicationById(id: String): MedicationEntity?

    suspend fun saveMedication(
        id: String? = null,
        name: String,
        dosage: Double,
        unit: String,
        frequency: String,
        scheduledTimes: List<String>,
        instructions: String? = null,
        colorHex: String = "#0D5C58",
        notes: String? = null
    ): String

    suspend fun setMedicationActive(id: String, isActive: Boolean)
    suspend fun deleteMedication(id: String)

    fun observeDailySchedule(date: LocalDate = LocalDate.now()): Flow<List<ScheduledDoseItem>>
    suspend fun logDose(
        medicationId: String,
        scheduledTimeEpochMillis: Long,
        status: DoseStatus,
        notes: String? = null
    )
    suspend fun removeDoseLog(medicationId: String, scheduledTimeEpochMillis: Long)
    fun observeAdherenceSummary(days: Int = 30): Flow<MedicationAdherenceSummary>
}

class DefaultMedicationRepository(
    private val medicationDao: MedicationDao,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : MedicationRepository {

    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())

    override fun observeAllMedications(): Flow<List<MedicationEntity>> =
        medicationDao.observeAllMedications()

    override fun observeActiveMedications(): Flow<List<MedicationEntity>> =
        medicationDao.observeActiveMedications()

    override suspend fun getMedicationById(id: String): MedicationEntity? =
        medicationDao.getMedicationById(id)

    override suspend fun saveMedication(
        id: String?,
        name: String,
        dosage: Double,
        unit: String,
        frequency: String,
        scheduledTimes: List<String>,
        instructions: String?,
        colorHex: String,
        notes: String?
    ): String {
        val medicationId = id ?: UUID.randomUUID().toString()
        val timesString = if (scheduledTimes.isEmpty()) "08:00" else scheduledTimes.joinToString(",")
        val entity = MedicationEntity(
            id = medicationId,
            name = name.trim(),
            dosage = dosage,
            unit = unit.trim(),
            frequency = frequency,
            scheduledTimes = timesString,
            instructions = instructions?.trim()?.ifBlank { null },
            colorHex = colorHex,
            isActive = true,
            createdAtEpochMillis = clock.millis(),
            notes = notes?.trim()?.ifBlank { null }
        )
        medicationDao.upsertMedication(entity)
        return medicationId
    }

    override suspend fun setMedicationActive(id: String, isActive: Boolean) {
        medicationDao.updateActiveStatus(id, isActive)
    }

    override suspend fun deleteMedication(id: String) {
        medicationDao.deleteMedication(id)
    }

    override fun observeDailySchedule(date: LocalDate): Flow<List<ScheduledDoseItem>> {
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        val activeMedsFlow = medicationDao.observeActiveMedications()
        val logsFlow = medicationDao.observeLogsBetween(startOfDay, endOfDay)

        return combine(activeMedsFlow, logsFlow) { meds, logs ->
            val logsMap = logs.associateBy { "${it.medicationId}_${it.scheduledTimeEpochMillis}" }
            val schedule = mutableListOf<ScheduledDoseItem>()

            for (med in meds) {
                if (med.frequency == "AS_NEEDED") {
                    // For PRN, find any logged doses today
                    val medLogs = logs.filter { it.medicationId == med.id }
                    if (medLogs.isEmpty()) {
                        schedule.add(
                            ScheduledDoseItem(
                                medication = med,
                                scheduledTimeEpochMillis = startOfDay,
                                timeLabel = "As Needed",
                                status = DoseStatus.PENDING
                            )
                        )
                    } else {
                        medLogs.forEach { log ->
                            val timeStr = Instant.ofEpochMilli(log.loggedAtEpochMillis)
                                .atZone(zoneId)
                                .toLocalTime()
                                .format(timeFormatter)
                            schedule.add(
                                ScheduledDoseItem(
                                    medication = med,
                                    scheduledTimeEpochMillis = log.scheduledTimeEpochMillis,
                                    timeLabel = timeStr,
                                    status = DoseStatus.valueOf(log.status),
                                    logId = log.id,
                                    loggedAtEpochMillis = log.loggedAtEpochMillis
                                )
                            )
                        }
                    }
                } else {
                    val times = med.scheduledTimes.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    for (timeStr in times) {
                        try {
                            val localTime = LocalTime.parse(timeStr)
                            val doseEpochMillis = date.atTime(localTime).atZone(zoneId).toInstant().toEpochMilli()
                            val key = "${med.id}_${doseEpochMillis}"
                            val existingLog = logsMap[key]

                            val status = when (existingLog?.status) {
                                "TAKEN" -> DoseStatus.TAKEN
                                "SKIPPED" -> DoseStatus.SKIPPED
                                else -> DoseStatus.PENDING
                            }

                            schedule.add(
                                ScheduledDoseItem(
                                    medication = med,
                                    scheduledTimeEpochMillis = doseEpochMillis,
                                    timeLabel = localTime.format(timeFormatter),
                                    status = status,
                                    logId = existingLog?.id,
                                    loggedAtEpochMillis = existingLog?.loggedAtEpochMillis
                                )
                            )
                        } catch (_: Exception) {
                            // ignore invalid format
                        }
                    }
                }
            }
            schedule.sortedBy { it.scheduledTimeEpochMillis }
        }
    }

    override suspend fun logDose(
        medicationId: String,
        scheduledTimeEpochMillis: Long,
        status: DoseStatus,
        notes: String?
    ) {
        val log = MedicationLogEntity(
            id = UUID.randomUUID().toString(),
            medicationId = medicationId,
            scheduledTimeEpochMillis = scheduledTimeEpochMillis,
            loggedAtEpochMillis = clock.millis(),
            status = status.name,
            notes = notes
        )
        medicationDao.upsertLog(log)
    }

    override suspend fun removeDoseLog(medicationId: String, scheduledTimeEpochMillis: Long) {
        medicationDao.deleteLogForDose(medicationId, scheduledTimeEpochMillis)
    }

    override fun observeAdherenceSummary(days: Int): Flow<MedicationAdherenceSummary> {
        val now = LocalDate.now(clock)
        val startDate = now.minusDays(days.toLong())
        val startEpoch = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endEpoch = clock.millis()

        val activeMedsFlow = medicationDao.observeActiveMedications()
        val logsFlow = medicationDao.observeLogsBetween(startEpoch, endEpoch)

        return combine(activeMedsFlow, logsFlow) { meds, logs ->
            val nonPrnMeds = meds.filter { it.frequency != "AS_NEEDED" }
            var totalScheduled = 0
            for (med in nonPrnMeds) {
                val timesCount = med.scheduledTimes.split(",").count { it.isNotBlank() }
                totalScheduled += timesCount * days
            }

            val takenDoses = logs.count { it.status == "TAKEN" }
            val skippedDoses = logs.count { it.status == "SKIPPED" }

            val totalConsidered = maxOf(totalScheduled, takenDoses + skippedDoses)
            val adherencePct = if (totalConsidered > 0) {
                ((takenDoses.toDouble() / totalConsidered) * 100.0).roundToInt().coerceIn(0, 100)
            } else 100

            // Compute current streak (consecutive days with at least one dose taken and zero missed)
            var streak = 0
            for (d in 0 until days) {
                val checkDate = now.minusDays(d.toLong())
                val dayStart = checkDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val dayEnd = checkDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
                val dayLogs = logs.filter { it.scheduledTimeEpochMillis in dayStart..dayEnd }
                if (dayLogs.isNotEmpty() && dayLogs.any { it.status == "TAKEN" } && dayLogs.none { it.status == "SKIPPED" }) {
                    streak++
                } else if (d > 0) {
                    break
                }
            }

            MedicationAdherenceSummary(
                totalScheduledDoses = totalConsidered,
                takenDoses = takenDoses,
                skippedDoses = skippedDoses,
                adherencePercentage = adherencePct,
                currentStreakDays = streak
            )
        }
    }
}

class NoOpMedicationRepository : MedicationRepository {
    override fun observeAllMedications(): Flow<List<MedicationEntity>> = emptyFlow()
    override fun observeActiveMedications(): Flow<List<MedicationEntity>> = emptyFlow()
    override suspend fun getMedicationById(id: String): MedicationEntity? = null
    override suspend fun saveMedication(
        id: String?,
        name: String,
        dosage: Double,
        unit: String,
        frequency: String,
        scheduledTimes: List<String>,
        instructions: String?,
        colorHex: String,
        notes: String?
    ): String = ""

    override suspend fun setMedicationActive(id: String, isActive: Boolean) {}
    override suspend fun deleteMedication(id: String) {}
    override fun observeDailySchedule(date: LocalDate): Flow<List<ScheduledDoseItem>> = emptyFlow()
    override suspend fun logDose(
        medicationId: String,
        scheduledTimeEpochMillis: Long,
        status: DoseStatus,
        notes: String?
    ) {}
    override suspend fun removeDoseLog(medicationId: String, scheduledTimeEpochMillis: Long) {}
    override fun observeAdherenceSummary(days: Int): Flow<MedicationAdherenceSummary> = flowOf(
        MedicationAdherenceSummary(0, 0, 0, 100, 0)
    )
}

