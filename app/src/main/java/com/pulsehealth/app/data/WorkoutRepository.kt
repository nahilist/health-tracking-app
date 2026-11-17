package com.pulsehealth.app.data

import com.pulsehealth.app.data.local.WorkoutDao
import com.pulsehealth.app.data.local.WorkoutSessionEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

interface WorkoutRepository {
    fun observeWorkoutsInRange(startEpochMillis: Long, endEpochMillis: Long): Flow<List<WorkoutSessionEntity>>
    fun observeAllWorkouts(): Flow<List<WorkoutSessionEntity>>
    fun observeTodayWorkouts(): Flow<List<WorkoutSessionEntity>>

    suspend fun logWorkout(
        workoutType: WorkoutType,
        title: String? = null,
        startTimeEpochMillis: Long? = null,
        durationMinutes: Long,
        activeCalories: Int? = null,
        distanceMeters: Double? = null,
        notes: String? = null
    ): WorkoutSessionEntity

    suspend fun deleteWorkout(id: String): Boolean
    suspend fun deleteAllWorkouts()
    suspend fun getAllWorkouts(): List<WorkoutSessionEntity>
    suspend fun importHealthConnectWorkouts(workouts: List<WorkoutSessionEntity>)
}

class DefaultWorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WorkoutRepository {

    override fun observeAllWorkouts(): Flow<List<WorkoutSessionEntity>> =
        workoutDao.observeAllWorkouts()

    override fun observeWorkoutsInRange(startEpochMillis: Long, endEpochMillis: Long): Flow<List<WorkoutSessionEntity>> =
        workoutDao.observeWorkoutsInRange(startEpochMillis, endEpochMillis)

    override fun observeTodayWorkouts(): Flow<List<WorkoutSessionEntity>> {
        val today = Instant.ofEpochMilli(clock.millis()).atZone(zoneId).toLocalDate()
        val start = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return workoutDao.observeWorkoutsInRange(start, end)
    }

    override suspend fun logWorkout(
        workoutType: WorkoutType,
        title: String?,
        startTimeEpochMillis: Long?,
        durationMinutes: Long,
        activeCalories: Int?,
        distanceMeters: Double?,
        notes: String?
    ): WorkoutSessionEntity = withContext(ioDispatcher) {
        require(durationMinutes in 1..1440) { "Duration must be between 1 and 1440 minutes" }
        val start = startTimeEpochMillis ?: (clock.millis() - (durationMinutes * 60 * 1000L))
        val end = start + (durationMinutes * 60 * 1000L)
        val calculatedCalories = activeCalories ?: (durationMinutes * workoutType.defaultCaloriesPerMinute).toInt()
        val finalTitle = title?.takeIf { it.isNotBlank() } ?: workoutType.defaultTitle

        val entity = WorkoutSessionEntity(
            id = UUID.randomUUID().toString(),
            workoutType = workoutType.name,
            title = finalTitle,
            startTimeEpochMillis = start,
            endTimeEpochMillis = end,
            durationMinutes = durationMinutes,
            activeCalories = calculatedCalories,
            distanceMeters = distanceMeters,
            source = "local",
            notes = notes,
            syncState = "pending"
        )
        workoutDao.upsert(entity)
        entity
    }

    override suspend fun deleteWorkout(id: String): Boolean = withContext(ioDispatcher) {
        workoutDao.deleteById(id) > 0
    }

    override suspend fun deleteAllWorkouts() = withContext(ioDispatcher) {
        workoutDao.deleteAll()
    }

    override suspend fun getAllWorkouts(): List<WorkoutSessionEntity> = withContext(ioDispatcher) {
        workoutDao.getAllWorkouts()
    }

    override suspend fun importHealthConnectWorkouts(workouts: List<WorkoutSessionEntity>) = withContext(ioDispatcher) {
        workoutDao.upsertAll(workouts)
    }
}

class NoOpWorkoutRepository : WorkoutRepository {
    override fun observeWorkoutsInRange(startEpochMillis: Long, endEpochMillis: Long): Flow<List<WorkoutSessionEntity>> = flowOf(emptyList())
    override fun observeAllWorkouts(): Flow<List<WorkoutSessionEntity>> = flowOf(emptyList())
    override fun observeTodayWorkouts(): Flow<List<WorkoutSessionEntity>> = flowOf(emptyList())
    override suspend fun logWorkout(
        workoutType: WorkoutType,
        title: String?,
        startTimeEpochMillis: Long?,
        durationMinutes: Long,
        activeCalories: Int?,
        distanceMeters: Double?,
        notes: String?
    ): WorkoutSessionEntity = WorkoutSessionEntity(
        id = "mock",
        workoutType = workoutType.name,
        title = title ?: workoutType.defaultTitle,
        startTimeEpochMillis = 0L,
        endTimeEpochMillis = 0L,
        durationMinutes = durationMinutes,
        activeCalories = activeCalories,
        distanceMeters = distanceMeters,
        source = "local",
        notes = notes,
        syncState = "synced"
    )
    override suspend fun deleteWorkout(id: String): Boolean = true
    override suspend fun deleteAllWorkouts() {}
    override suspend fun getAllWorkouts(): List<WorkoutSessionEntity> = emptyList()
    override suspend fun importHealthConnectWorkouts(workouts: List<WorkoutSessionEntity>) {}
}
