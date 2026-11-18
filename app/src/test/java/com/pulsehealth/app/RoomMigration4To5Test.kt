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
class RoomMigration4To5Test {

    @Test
    fun migrate4To5PreservesExistingTablesAndCreatesMedicationTables() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name("test-migration-4-5.db")
                .callback(object : SupportSQLiteOpenHelper.Callback(4) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Version 4 schema
                        db.execSQL("CREATE TABLE IF NOT EXISTS `hydration_entries` (`id` TEXT NOT NULL, `amountMilliliters` INTEGER NOT NULL, `recordedAtEpochMillis` INTEGER NOT NULL, `source` TEXT NOT NULL, `syncState` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `daily_health_summaries` (`epochDay` INTEGER NOT NULL, `steps` INTEGER NOT NULL, `restingHeartRate` INTEGER, `sleepMinutes` INTEGER NOT NULL, `activeCalories` INTEGER NOT NULL, `exerciseMinutes` INTEGER NOT NULL, `standHours` INTEGER NOT NULL, `source` TEXT NOT NULL, `updatedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`epochDay`))")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `workout_sessions` (`id` TEXT NOT NULL, `workoutType` TEXT NOT NULL, `title` TEXT NOT NULL, `startTimeEpochMillis` INTEGER NOT NULL, `endTimeEpochMillis` INTEGER NOT NULL, `durationMinutes` INTEGER NOT NULL, `activeCalories` INTEGER, `distanceMeters` REAL, `source` TEXT NOT NULL, `notes` TEXT, `syncState` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `sleep_sessions` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `startTimeEpochMillis` INTEGER NOT NULL, `endTimeEpochMillis` INTEGER NOT NULL, `durationMinutes` INTEGER NOT NULL, `deepDurationMinutes` INTEGER NOT NULL, `lightDurationMinutes` INTEGER NOT NULL, `remDurationMinutes` INTEGER NOT NULL, `awakeDurationMinutes` INTEGER NOT NULL, `sleepEfficiencyPercentage` INTEGER NOT NULL, `source` TEXT NOT NULL, `notes` TEXT, `syncState` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `sleep_stages` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `stage` TEXT NOT NULL, `startTimeEpochMillis` INTEGER NOT NULL, `endTimeEpochMillis` INTEGER NOT NULL, `durationMinutes` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`sessionId`) REFERENCES `sleep_sessions`(`id`) ON DELETE CASCADE)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `vital_records` (`id` TEXT NOT NULL, `vitalType` TEXT NOT NULL, `systolic` INTEGER, `diastolic` INTEGER, `heartRateBpm` INTEGER, `recordedAtEpochMillis` INTEGER NOT NULL, `measurementContext` TEXT NOT NULL, `notes` TEXT, `source` TEXT NOT NULL, `syncState` TEXT NOT NULL, PRIMARY KEY(`id`))")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )

        val db = helper.writableDatabase

        // 1. Insert seed data into version 4 tables
        db.execSQL("INSERT INTO hydration_entries (id, amountMilliliters, recordedAtEpochMillis, source, syncState) VALUES ('hyd_1', 750, 1731600000000, 'manual', 'synced')")
        db.execSQL("INSERT INTO vital_records (id, vitalType, systolic, diastolic, heartRateBpm, recordedAtEpochMillis, measurementContext, notes, source, syncState) VALUES ('vtl_1', 'BLOOD_PRESSURE', 120, 80, 70, 1731600000000, 'RESTING', 'Baseline', 'manual', 'synced')")

        // 2. Execute migration 4 -> 5
        PulseDatabase.MIGRATION_4_5.migrate(db)

        // 3. Verify version 4 data is intact
        val hydCursor = db.query("SELECT * FROM hydration_entries WHERE id = 'hyd_1'")
        assertTrue(hydCursor.moveToFirst())
        assertEquals(750, hydCursor.getInt(hydCursor.getColumnIndexOrThrow("amountMilliliters")))
        hydCursor.close()

        val vtlCursor = db.query("SELECT * FROM vital_records WHERE id = 'vtl_1'")
        assertTrue(vtlCursor.moveToFirst())
        assertEquals("BLOOD_PRESSURE", vtlCursor.getString(vtlCursor.getColumnIndexOrThrow("vitalType")))
        assertEquals(120, vtlCursor.getInt(vtlCursor.getColumnIndexOrThrow("systolic")))
        vtlCursor.close()

        // 4. Verify medications table works
        db.execSQL(
            """
            INSERT INTO medications (id, name, dosage, unit, frequency, scheduledTimes, instructions, colorHex, isActive, createdAtEpochMillis, notes)
            VALUES ('med_1', 'Vitamin D3', 2000.0, 'IU', 'DAILY', '08:00', 'Take with breakfast', '#0D5C58', 1, 1731600000000, 'Immune support')
            """.trimIndent()
        )
        val medCursor = db.query("SELECT * FROM medications WHERE id = 'med_1'")
        assertTrue(medCursor.moveToFirst())
        assertEquals("Vitamin D3", medCursor.getString(medCursor.getColumnIndexOrThrow("name")))
        assertEquals(2000.0, medCursor.getDouble(medCursor.getColumnIndexOrThrow("dosage")), 0.01)
        assertEquals("IU", medCursor.getString(medCursor.getColumnIndexOrThrow("unit")))
        assertEquals("DAILY", medCursor.getString(medCursor.getColumnIndexOrThrow("frequency")))
        assertEquals("08:00", medCursor.getString(medCursor.getColumnIndexOrThrow("scheduledTimes")))
        assertEquals("Take with breakfast", medCursor.getString(medCursor.getColumnIndexOrThrow("instructions")))
        assertEquals(1, medCursor.getInt(medCursor.getColumnIndexOrThrow("isActive")))
        medCursor.close()

        // 5. Verify medication_logs table works
        db.execSQL(
            """
            INSERT INTO medication_logs (id, medicationId, scheduledTimeEpochMillis, loggedAtEpochMillis, status, notes)
            VALUES ('log_1', 'med_1', 1731657600000, 1731657610000, 'TAKEN', 'Taken with oats')
            """.trimIndent()
        )
        val logCursor = db.query("SELECT * FROM medication_logs WHERE id = 'log_1'")
        assertTrue(logCursor.moveToFirst())
        assertEquals("med_1", logCursor.getString(logCursor.getColumnIndexOrThrow("medicationId")))
        assertEquals(1731657600000L, logCursor.getLong(logCursor.getColumnIndexOrThrow("scheduledTimeEpochMillis")))
        assertEquals("TAKEN", logCursor.getString(logCursor.getColumnIndexOrThrow("status")))
        logCursor.close()

        db.close()
    }
}
