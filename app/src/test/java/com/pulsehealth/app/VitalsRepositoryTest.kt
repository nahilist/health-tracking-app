package com.pulsehealth.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pulsehealth.app.data.BloodPressureClassification
import com.pulsehealth.app.data.DefaultVitalsRepository
import com.pulsehealth.app.data.HeartRateClassification
import com.pulsehealth.app.data.MeasurementContext
import com.pulsehealth.app.data.VitalType
import com.pulsehealth.app.data.local.PulseDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
class VitalsRepositoryTest {

    private lateinit var database: PulseDatabase
    private lateinit var repository: DefaultVitalsRepository
    private val fixedClock = Clock.fixed(Instant.parse("2026-11-18T10:00:00Z"), ZoneId.of("UTC"))

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PulseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DefaultVitalsRepository(
            vitalRecordDao = database.vitalRecordDao(),
            clock = fixedClock
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun logBloodPressureSucceedsAndCanBeObserved() = runTest {
        val result = repository.logVital(
            type = VitalType.BLOOD_PRESSURE,
            systolic = 118,
            diastolic = 78,
            heartRateBpm = 68,
            context = MeasurementContext.RESTING,
            notes = "Morning coffee test"
        )
        assertTrue(result.isSuccess)

        val records = repository.observeVitals(VitalType.BLOOD_PRESSURE).first()
        assertEquals(1, records.size)
        val record = records.first()
        assertEquals("BLOOD_PRESSURE", record.vitalType)
        assertEquals(118, record.systolic)
        assertEquals(78, record.diastolic)
        assertEquals(68, record.heartRateBpm)
        assertEquals("RESTING", record.measurementContext)
        assertEquals("Morning coffee test", record.notes)
        assertEquals(fixedClock.millis(), record.recordedAtEpochMillis)
    }

    @Test
    fun logBloodPressureWithInvalidValuesFailsValidation() = runTest {
        // Systolic <= Diastolic
        val invertedResult = repository.logVital(
            type = VitalType.BLOOD_PRESSURE,
            systolic = 70,
            diastolic = 85
        )
        assertTrue(invertedResult.isFailure)

        // Missing systolic
        val missingSys = repository.logVital(
            type = VitalType.BLOOD_PRESSURE,
            diastolic = 80
        )
        assertTrue(missingSys.isFailure)

        // Extreme impossible value
        val extreme = repository.logVital(
            type = VitalType.BLOOD_PRESSURE,
            systolic = 400,
            diastolic = 80
        )
        assertTrue(extreme.isFailure)

        val records = repository.observeVitals(VitalType.BLOOD_PRESSURE).first()
        assertTrue(records.isEmpty())
    }

    @Test
    fun logHeartRateSucceedsAndCanBeObserved() = runTest {
        val result = repository.logVital(
            type = VitalType.HEART_RATE,
            heartRateBpm = 72,
            context = MeasurementContext.GENERAL,
            notes = "Midday check"
        )
        assertTrue(result.isSuccess)

        val hrRecords = repository.observeVitals(VitalType.HEART_RATE).first()
        assertEquals(1, hrRecords.size)
        val record = hrRecords.first()
        assertEquals("HEART_RATE", record.vitalType)
        assertEquals(72, record.heartRateBpm)
        assertEquals("GENERAL", record.measurementContext)

        // Ensure BP filter does not return HR record
        val bpRecords = repository.observeVitals(VitalType.BLOOD_PRESSURE).first()
        assertTrue(bpRecords.isEmpty())
    }

    @Test
    fun deleteVitalRemovesRecord() = runTest {
        repository.logVital(
            type = VitalType.HEART_RATE,
            heartRateBpm = 80
        )
        val initial = repository.observeRecentVitals().first()
        assertEquals(1, initial.size)
        val id = initial.first().id

        val deleteResult = repository.deleteVital(id)
        assertTrue(deleteResult.isSuccess)

        val after = repository.observeRecentVitals().first()
        assertTrue(after.isEmpty())
    }

    @Test
    fun bloodPressureClassificationCategorizesAccurately() {
        assertEquals("Normal Range", BloodPressureClassification.classify(115, 75).label)
        assertEquals("Elevated", BloodPressureClassification.classify(125, 78).label)
        assertEquals("Stage 1 Range", BloodPressureClassification.classify(134, 82).label)
        assertEquals("Stage 2 Range", BloodPressureClassification.classify(142, 92).label)
        assertEquals("Consult Healthcare Provider", BloodPressureClassification.classify(185, 122).label)
    }

    @Test
    fun heartRateClassificationCategorizesAccurately() {
        assertEquals("Optimal Resting", HeartRateClassification.classifyRestingBpm(62))
        assertEquals("Typical Resting", HeartRateClassification.classifyRestingBpm(75))
        assertEquals("Elevated Resting", HeartRateClassification.classifyRestingBpm(90))
        assertEquals("Athletic / Low", HeartRateClassification.classifyRestingBpm(46))
    }
}
