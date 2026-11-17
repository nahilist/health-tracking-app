package com.pulsehealth.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pulsehealth.app.data.DefaultWorkoutRepository
import com.pulsehealth.app.data.WorkoutRepository
import com.pulsehealth.app.data.WorkoutType
import com.pulsehealth.app.data.local.PulseDatabase
import com.pulsehealth.app.data.local.WorkoutSessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WorkoutRepositoryTest {

    private lateinit var database: PulseDatabase
    private lateinit var repository: WorkoutRepository
    private val fixedInstant = Instant.parse("2026-11-17T10:00:00Z")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PulseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DefaultWorkoutRepository(
            workoutDao = database.workoutDao(),
            clock = Clock.fixed(fixedInstant, ZoneId.of("UTC")),
            zoneId = ZoneId.of("UTC")
        )
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun loggingWorkoutPersistsAndCalculatesCalories() = runTest {
        val logged = repository.logWorkout(
            workoutType = WorkoutType.RUNNING,
            durationMinutes = 30
        )

        assertNotNull(logged.id)
        assertEquals(WorkoutType.RUNNING.name, logged.workoutType)
        assertEquals(WorkoutType.RUNNING.defaultTitle, logged.title)
        assertEquals(30L, logged.durationMinutes)
        // 30 min * 11.5 cal/min = 345 kcal
        assertEquals(345, logged.activeCalories)
        assertEquals("local", logged.source)

        val all = repository.observeAllWorkouts().first()
        assertEquals(1, all.size)
        assertEquals(logged.id, all.first().id)
    }

    @Test
    fun loggingWorkoutWithCustomCaloriesAndDistance() = runTest {
        val logged = repository.logWorkout(
            workoutType = WorkoutType.CYCLING,
            title = "Morning Ride",
            durationMinutes = 45,
            activeCalories = 420,
            distanceMeters = 15500.0,
            notes = "Interval training"
        )

        assertEquals("Morning Ride", logged.title)
        assertEquals(420, logged.activeCalories)
        assertEquals(15500.0, logged.distanceMeters ?: 0.0, 0.01)
        assertEquals("Interval training", logged.notes)
    }

    @Test(expected = IllegalArgumentException::class)
    fun loggingWorkoutValidatesZeroDuration() = runTest {
        repository.logWorkout(WorkoutType.YOGA, durationMinutes = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun loggingWorkoutValidatesExcessiveDuration() = runTest {
        repository.logWorkout(WorkoutType.YOGA, durationMinutes = 2000)
    }

    @Test
    fun observingTodayWorkoutsReturnsOnlyCurrentDay() = runTest {
        // Today's workout (within 2026-11-17)
        repository.logWorkout(
            workoutType = WorkoutType.WALKING,
            startTimeEpochMillis = fixedInstant.toEpochMilli() - 3600_000L,
            durationMinutes = 20
        )

        // Yesterday's workout
        val yesterdayMillis = fixedInstant.minusSeconds(86400 * 2).toEpochMilli()
        database.workoutDao().upsert(
            WorkoutSessionEntity(
                id = "past_1",
                workoutType = WorkoutType.SWIMMING.name,
                title = "Past Swim",
                startTimeEpochMillis = yesterdayMillis,
                endTimeEpochMillis = yesterdayMillis + 1800_000L,
                durationMinutes = 30,
                activeCalories = 300,
                distanceMeters = 1000.0,
                source = "local",
                notes = null,
                syncState = "synced"
            )
        )

        val todayWorkouts = repository.observeTodayWorkouts().first()
        assertEquals(1, todayWorkouts.size)
        assertEquals(WorkoutType.WALKING.name, todayWorkouts.first().workoutType)

        val allWorkouts = repository.observeAllWorkouts().first()
        assertEquals(2, allWorkouts.size)
    }

    @Test
    fun deletingWorkoutRemovesIt() = runTest {
        val session = repository.logWorkout(
            workoutType = WorkoutType.STRENGTH,
            durationMinutes = 50
        )
        assertEquals(1, repository.getAllWorkouts().size)

        val deleted = repository.deleteWorkout(session.id)
        assertTrue(deleted)
        assertEquals(0, repository.getAllWorkouts().size)
    }

    @Test
    fun deleteAllWorkoutsClearsAllEntries() = runTest {
        repository.logWorkout(WorkoutType.RUNNING, durationMinutes = 25)
        repository.logWorkout(WorkoutType.YOGA, durationMinutes = 40)
        assertEquals(2, repository.getAllWorkouts().size)

        repository.deleteAllWorkouts()
        assertEquals(0, repository.getAllWorkouts().size)
    }

    @Test
    fun importingHealthConnectWorkoutsUpsertsSuccessfully() = runTest {
        val hcSession = WorkoutSessionEntity(
            id = "hc_session_123",
            workoutType = WorkoutType.HIIT.name,
            title = "HIIT Circuit",
            startTimeEpochMillis = fixedInstant.toEpochMilli() - 1800_000L,
            endTimeEpochMillis = fixedInstant.toEpochMilli(),
            durationMinutes = 30,
            activeCalories = 360,
            distanceMeters = null,
            source = "health_connect",
            notes = "Synced from Health Connect",
            syncState = "synced"
        )

        repository.importHealthConnectWorkouts(listOf(hcSession))

        val all = repository.getAllWorkouts()
        assertEquals(1, all.size)
        assertEquals("hc_session_123", all.first().id)
        assertEquals("health_connect", all.first().source)
    }
}
