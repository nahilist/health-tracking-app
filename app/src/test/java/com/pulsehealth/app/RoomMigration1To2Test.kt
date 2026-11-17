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
class RoomMigration1To2Test {

    @Test
    fun migrate1To2PreservesHydrationAndSummaries() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name("test-migration-1-2.db")
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE IF NOT EXISTS `hydration_entries` (`id` TEXT NOT NULL, `amountMilliliters` INTEGER NOT NULL, `recordedAtEpochMillis` INTEGER NOT NULL, `source` TEXT NOT NULL, `syncState` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_hydration_entries_recordedAtEpochMillis` ON `hydration_entries` (`recordedAtEpochMillis`)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `daily_health_summaries` (`epochDay` INTEGER NOT NULL, `steps` INTEGER NOT NULL, `restingHeartRate` INTEGER, `sleepMinutes` INTEGER NOT NULL, `activeCalories` INTEGER NOT NULL, `exerciseMinutes` INTEGER NOT NULL, `standHours` INTEGER NOT NULL, `source` TEXT NOT NULL, `updatedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`epochDay`))")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )

        val db = helper.writableDatabase

        // 1. Insert seed data into version 1 tables
        db.execSQL("INSERT INTO hydration_entries (id, amountMilliliters, recordedAtEpochMillis, source, syncState) VALUES ('test_id_1', 350, 1731600000000, 'manual', 'synced')")
        db.execSQL("INSERT INTO daily_health_summaries (epochDay, steps, restingHeartRate, sleepMinutes, activeCalories, exerciseMinutes, standHours, source, updatedAtEpochMillis) VALUES (20000, 8500, 65, 450, 400, 35, 10, 'Health Connect', 1731600000000)")

        // 2. Execute migration
        PulseDatabase.MIGRATION_1_2.migrate(db)

        // 3. Verify existing data preserved
        val hydrationCursor = db.query("SELECT * FROM hydration_entries WHERE id = 'test_id_1'")
        assertTrue(hydrationCursor.moveToFirst())
        assertEquals(350, hydrationCursor.getInt(hydrationCursor.getColumnIndexOrThrow("amountMilliliters")))
        hydrationCursor.close()

        val summaryCursor = db.query("SELECT * FROM daily_health_summaries WHERE epochDay = 20000")
        assertTrue(summaryCursor.moveToFirst())
        assertEquals(8500, summaryCursor.getInt(summaryCursor.getColumnIndexOrThrow("steps")))
        summaryCursor.close()

        // 4. Verify new workout_sessions table exists and functions
        db.execSQL("INSERT INTO workout_sessions (id, workoutType, title, startTimeEpochMillis, endTimeEpochMillis, durationMinutes, activeCalories, distanceMeters, source, notes, syncState) VALUES ('w1', 'RUNNING', 'Morning Run', 1731600000000, 1731601800000, 30, 320, 5000.0, 'local', 'Great run', 'pending')")
        val workoutCursor = db.query("SELECT * FROM workout_sessions WHERE id = 'w1'")
        assertTrue(workoutCursor.moveToFirst())
        assertEquals("Morning Run", workoutCursor.getString(workoutCursor.getColumnIndexOrThrow("title")))
        assertEquals(30, workoutCursor.getInt(workoutCursor.getColumnIndexOrThrow("durationMinutes")))
        workoutCursor.close()

        db.close()
    }
}
