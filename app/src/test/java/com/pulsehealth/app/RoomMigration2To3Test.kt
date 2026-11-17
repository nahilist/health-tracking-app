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
class RoomMigration2To3Test {

    @Test
    fun migrate2To3PreservesExistingTablesAndCreatesSleepTables() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name("test-migration-2-3.db")
                .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Version 2 schema: hydration, daily summaries, workouts
                        db.execSQL("CREATE TABLE IF NOT EXISTS `hydration_entries` (`id` TEXT NOT NULL, `amountMilliliters` INTEGER NOT NULL, `recordedAtEpochMillis` INTEGER NOT NULL, `source` TEXT NOT NULL, `syncState` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_hydration_entries_recordedAtEpochMillis` ON `hydration_entries` (`recordedAtEpochMillis`)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `daily_health_summaries` (`epochDay` INTEGER NOT NULL, `steps` INTEGER NOT NULL, `restingHeartRate` INTEGER, `sleepMinutes` INTEGER NOT NULL, `activeCalories` INTEGER NOT NULL, `exerciseMinutes` INTEGER NOT NULL, `standHours` INTEGER NOT NULL, `source` TEXT NOT NULL, `updatedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`epochDay`))")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `workout_sessions` (`id` TEXT NOT NULL, `workoutType` TEXT NOT NULL, `title` TEXT NOT NULL, `startTimeEpochMillis` INTEGER NOT NULL, `endTimeEpochMillis` INTEGER NOT NULL, `durationMinutes` INTEGER NOT NULL, `activeCalories` INTEGER, `distanceMeters` REAL, `source` TEXT NOT NULL, `notes` TEXT, `syncState` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_startTimeEpochMillis` ON `workout_sessions` (`startTimeEpochMillis`)")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )

        val db = helper.writableDatabase

        // 1. Insert seed data into version 2 tables
        db.execSQL("INSERT INTO hydration_entries (id, amountMilliliters, recordedAtEpochMillis, source, syncState) VALUES ('hyd_1', 500, 1731600000000, 'manual', 'synced')")
        db.execSQL("INSERT INTO daily_health_summaries (epochDay, steps, restingHeartRate, sleepMinutes, activeCalories, exerciseMinutes, standHours, source, updatedAtEpochMillis) VALUES (20001, 10200, 62, 480, 520, 45, 12, 'Health Connect', 1731600000000)")
        db.execSQL("INSERT INTO workout_sessions (id, workoutType, title, startTimeEpochMillis, endTimeEpochMillis, durationMinutes, activeCalories, distanceMeters, source, notes, syncState) VALUES ('wk_1', 'RUNNING', 'Morning 5K', 1731600000000, 1731601800000, 30, 300, 5000.0, 'local', 'Nice pace', 'synced')")

        // 2. Execute migration 2 -> 3
        PulseDatabase.MIGRATION_2_3.migrate(db)

        // 3. Verify version 2 data is intact
        val hydCursor = db.query("SELECT * FROM hydration_entries WHERE id = 'hyd_1'")
        assertTrue(hydCursor.moveToFirst())
        assertEquals(500, hydCursor.getInt(hydCursor.getColumnIndexOrThrow("amountMilliliters")))
        hydCursor.close()

        val summaryCursor = db.query("SELECT * FROM daily_health_summaries WHERE epochDay = 20001")
        assertTrue(summaryCursor.moveToFirst())
        assertEquals(10200, summaryCursor.getInt(summaryCursor.getColumnIndexOrThrow("steps")))
        summaryCursor.close()

        val workoutCursor = db.query("SELECT * FROM workout_sessions WHERE id = 'wk_1'")
        assertTrue(workoutCursor.moveToFirst())
        assertEquals("Morning 5K", workoutCursor.getString(workoutCursor.getColumnIndexOrThrow("title")))
        workoutCursor.close()

        // 4. Verify new sleep_sessions table exists and supports queries
        db.execSQL(
            """
            INSERT INTO sleep_sessions (id, title, startTimeEpochMillis, endTimeEpochMillis, durationMinutes, deepDurationMinutes, lightDurationMinutes, remDurationMinutes, awakeDurationMinutes, sleepEfficiencyPercentage, source, notes, syncState)
            VALUES ('slp_1', 'Restful Sleep', 1731620000000, 1731648800000, 480, 100, 240, 100, 40, 92, 'local', 'Felt refreshed', 'synced')
            """.trimIndent()
        )
        val sleepCursor = db.query("SELECT * FROM sleep_sessions WHERE id = 'slp_1'")
        assertTrue(sleepCursor.moveToFirst())
        assertEquals("Restful Sleep", sleepCursor.getString(sleepCursor.getColumnIndexOrThrow("title")))
        assertEquals(480, sleepCursor.getInt(sleepCursor.getColumnIndexOrThrow("durationMinutes")))
        assertEquals(92, sleepCursor.getInt(sleepCursor.getColumnIndexOrThrow("sleepEfficiencyPercentage")))
        sleepCursor.close()

        // 5. Verify new sleep_stages table exists and supports foreign key relation
        db.execSQL(
            """
            INSERT INTO sleep_stages (id, sessionId, stage, startTimeEpochMillis, endTimeEpochMillis, durationMinutes)
            VALUES ('stg_1', 'slp_1', 'DEEP', 1731620000000, 1731626000000, 100)
            """.trimIndent()
        )
        val stageCursor = db.query("SELECT * FROM sleep_stages WHERE id = 'stg_1'")
        assertTrue(stageCursor.moveToFirst())
        assertEquals("DEEP", stageCursor.getString(stageCursor.getColumnIndexOrThrow("stage")))
        assertEquals("slp_1", stageCursor.getString(stageCursor.getColumnIndexOrThrow("sessionId")))
        stageCursor.close()

        db.close()
    }
}
