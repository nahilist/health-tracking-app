package com.pulsehealth.app

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.pulsehealth.app.data.local.PulseDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomMigration3To4Test {

    @Test
    fun migrate3To4PreservesExistingTablesAndCreatesVitalRecordsTable() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name("test-migration-3-4.db")
                .callback(object : SupportSQLiteOpenHelper.Callback(3) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Version 3 schema: hydration, daily summaries, workouts, sleep
                        db.execSQL("CREATE TABLE IF NOT EXISTS `hydration_entries` (`id` TEXT NOT NULL, `amountMilliliters` INTEGER NOT NULL, `recordedAtEpochMillis` INTEGER NOT NULL, `source` TEXT NOT NULL, `syncState` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_hydration_entries_recordedAtEpochMillis` ON `hydration_entries` (`recordedAtEpochMillis`)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `daily_health_summaries` (`epochDay` INTEGER NOT NULL, `steps` INTEGER NOT NULL, `restingHeartRate` INTEGER, `sleepMinutes` INTEGER NOT NULL, `activeCalories` INTEGER NOT NULL, `exerciseMinutes` INTEGER NOT NULL, `standHours` INTEGER NOT NULL, `source` TEXT NOT NULL, `updatedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`epochDay`))")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `workout_sessions` (`id` TEXT NOT NULL, `workoutType` TEXT NOT NULL, `title` TEXT NOT NULL, `startTimeEpochMillis` INTEGER NOT NULL, `endTimeEpochMillis` INTEGER NOT NULL, `durationMinutes` INTEGER NOT NULL, `activeCalories` INTEGER, `distanceMeters` REAL, `source` TEXT NOT NULL, `notes` TEXT, `syncState` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_startTimeEpochMillis` ON `workout_sessions` (`startTimeEpochMillis`)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `sleep_sessions` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `startTimeEpochMillis` INTEGER NOT NULL, `endTimeEpochMillis` INTEGER NOT NULL, `durationMinutes` INTEGER NOT NULL, `deepDurationMinutes` INTEGER NOT NULL, `lightDurationMinutes` INTEGER NOT NULL, `remDurationMinutes` INTEGER NOT NULL, `awakeDurationMinutes` INTEGER NOT NULL, `sleepEfficiencyPercentage` INTEGER NOT NULL, `source` TEXT NOT NULL, `notes` TEXT, `syncState` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sleep_sessions_startTimeEpochMillis` ON `sleep_sessions` (`startTimeEpochMillis`)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `sleep_stages` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `stage` TEXT NOT NULL, `startTimeEpochMillis` INTEGER NOT NULL, `endTimeEpochMillis` INTEGER NOT NULL, `durationMinutes` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`sessionId`) REFERENCES `sleep_sessions`(`id`) ON DELETE CASCADE)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sleep_stages_sessionId` ON `sleep_stages` (`sessionId`)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sleep_stages_startTimeEpochMillis` ON `sleep_stages` (`startTimeEpochMillis`)")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )

        val db = helper.writableDatabase

        // 1. Insert seed data into version 3 tables
        db.execSQL("INSERT INTO hydration_entries (id, amountMilliliters, recordedAtEpochMillis, source, syncState) VALUES ('hyd_1', 750, 1731600000000, 'manual', 'synced')")
        db.execSQL("INSERT INTO daily_health_summaries (epochDay, steps, restingHeartRate, sleepMinutes, activeCalories, exerciseMinutes, standHours, source, updatedAtEpochMillis) VALUES (20002, 8500, 65, 450, 480, 40, 11, 'Health Connect', 1731600000000)")
        db.execSQL("INSERT INTO workout_sessions (id, workoutType, title, startTimeEpochMillis, endTimeEpochMillis, durationMinutes, activeCalories, distanceMeters, source, notes, syncState) VALUES ('wk_1', 'CYCLING', 'Evening Ride', 1731600000000, 1731602400000, 40, 280, 12000.0, 'local', 'Outdoor ride', 'synced')")
        db.execSQL("INSERT INTO sleep_sessions (id, title, startTimeEpochMillis, endTimeEpochMillis, durationMinutes, deepDurationMinutes, lightDurationMinutes, remDurationMinutes, awakeDurationMinutes, sleepEfficiencyPercentage, source, notes, syncState) VALUES ('slp_1', 'Night Sleep', 1731620000000, 1731648800000, 480, 90, 250, 100, 40, 90, 'local', 'Good rest', 'synced')")

        // 2. Execute migration 3 -> 4
        PulseDatabase.MIGRATION_3_4.migrate(db)

        // 3. Verify version 3 data is intact
        val hydCursor = db.query("SELECT * FROM hydration_entries WHERE id = 'hyd_1'")
        assertTrue(hydCursor.moveToFirst())
        assertEquals(750, hydCursor.getInt(hydCursor.getColumnIndexOrThrow("amountMilliliters")))
        hydCursor.close()

        val summaryCursor = db.query("SELECT * FROM daily_health_summaries WHERE epochDay = 20002")
        assertTrue(summaryCursor.moveToFirst())
        assertEquals(8500, summaryCursor.getInt(summaryCursor.getColumnIndexOrThrow("steps")))
        summaryCursor.close()

        val workoutCursor = db.query("SELECT * FROM workout_sessions WHERE id = 'wk_1'")
        assertTrue(workoutCursor.moveToFirst())
        assertEquals("Evening Ride", workoutCursor.getString(workoutCursor.getColumnIndexOrThrow("title")))
        workoutCursor.close()

        val sleepCursor = db.query("SELECT * FROM sleep_sessions WHERE id = 'slp_1'")
        assertTrue(sleepCursor.moveToFirst())
        assertEquals("Night Sleep", sleepCursor.getString(sleepCursor.getColumnIndexOrThrow("title")))
        sleepCursor.close()

        // 4. Verify vital_records table exists and supports Blood Pressure and Heart Rate entries
        db.execSQL(
            """
            INSERT INTO vital_records (id, vitalType, systolic, diastolic, heartRateBpm, recordedAtEpochMillis, measurementContext, notes, source, syncState)
            VALUES ('vtl_bp_1', 'BLOOD_PRESSURE', 118, 78, 68, 1731625000000, 'RESTING', 'Morning baseline', 'manual', 'pending')
            """.trimIndent()
        )
        val bpCursor = db.query("SELECT * FROM vital_records WHERE id = 'vtl_bp_1'")
        assertTrue(bpCursor.moveToFirst())
        assertEquals("BLOOD_PRESSURE", bpCursor.getString(bpCursor.getColumnIndexOrThrow("vitalType")))
        assertEquals(118, bpCursor.getInt(bpCursor.getColumnIndexOrThrow("systolic")))
        assertEquals(78, bpCursor.getInt(bpCursor.getColumnIndexOrThrow("diastolic")))
        assertEquals(68, bpCursor.getInt(bpCursor.getColumnIndexOrThrow("heartRateBpm")))
        assertEquals("RESTING", bpCursor.getString(bpCursor.getColumnIndexOrThrow("measurementContext")))
        assertEquals("Morning baseline", bpCursor.getString(bpCursor.getColumnIndexOrThrow("notes")))
        bpCursor.close()

        db.execSQL(
            """
            INSERT INTO vital_records (id, vitalType, systolic, diastolic, heartRateBpm, recordedAtEpochMillis, measurementContext, notes, source, syncState)
            VALUES ('vtl_hr_1', 'HEART_RATE', NULL, NULL, 72, 1731626000000, 'GENERAL', 'After work', 'manual', 'pending')
            """.trimIndent()
        )
        val hrCursor = db.query("SELECT * FROM vital_records WHERE id = 'vtl_hr_1'")
        assertTrue(hrCursor.moveToFirst())
        assertEquals("HEART_RATE", hrCursor.getString(hrCursor.getColumnIndexOrThrow("vitalType")))
        assertEquals(72, hrCursor.getInt(hrCursor.getColumnIndexOrThrow("heartRateBpm")))
        assertTrue(hrCursor.isNull(hrCursor.getColumnIndexOrThrow("systolic")))
        hrCursor.close()

        db.close()
    }
}
