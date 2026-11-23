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
class RoomMigration5To6Test {

    @Test
    fun migrate5To6PreservesExistingTablesAndCreatesMealLogsTable() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name("test-migration-5-6.db")
                .callback(object : SupportSQLiteOpenHelper.Callback(5) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Version 5 schema tables
                        db.execSQL("CREATE TABLE IF NOT EXISTS `hydration_entries` (`id` TEXT NOT NULL, `amountMilliliters` INTEGER NOT NULL, `recordedAtEpochMillis` INTEGER NOT NULL, `source` TEXT NOT NULL, `syncState` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `medications` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `dosage` REAL NOT NULL, `unit` TEXT NOT NULL, `frequency` TEXT NOT NULL, `scheduledTimes` TEXT NOT NULL, `instructions` TEXT, `colorHex` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL, `notes` TEXT, PRIMARY KEY(`id`))")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )

        val db = helper.writableDatabase

        // 1. Insert seed data into version 5 table
        db.execSQL("INSERT INTO hydration_entries (id, amountMilliliters, recordedAtEpochMillis, source, syncState) VALUES ('hyd_1', 500, 1731600000000, 'manual', 'synced')")

        // 2. Execute migration 5 -> 6
        PulseDatabase.MIGRATION_5_6.migrate(db)

        // 3. Verify version 5 data is intact
        val hydCursor = db.query("SELECT * FROM hydration_entries WHERE id = 'hyd_1'")
        assertTrue(hydCursor.moveToFirst())
        assertEquals(500, hydCursor.getInt(hydCursor.getColumnIndexOrThrow("amountMilliliters")))
        hydCursor.close()

        // 4. Verify meal_logs table works with insert and query
        db.execSQL(
            """
            INSERT INTO meal_logs (id, mealType, name, calories, proteinGrams, carbsGrams, fatGrams, loggedAtEpochMillis, notes, syncState)
            VALUES ('meal_1', 'BREAKFAST', 'Oatmeal & Berries', 380, 24.5, 52.0, 8.0, 1731657600000, 'With whey protein', 'synced')
            """.trimIndent()
        )
        val mealCursor = db.query("SELECT * FROM meal_logs WHERE id = 'meal_1'")
        assertTrue(mealCursor.moveToFirst())
        assertEquals("meal_1", mealCursor.getString(mealCursor.getColumnIndexOrThrow("id")))
        assertEquals("BREAKFAST", mealCursor.getString(mealCursor.getColumnIndexOrThrow("mealType")))
        assertEquals("Oatmeal & Berries", mealCursor.getString(mealCursor.getColumnIndexOrThrow("name")))
        assertEquals(380, mealCursor.getInt(mealCursor.getColumnIndexOrThrow("calories")))
        assertEquals(24.5, mealCursor.getDouble(mealCursor.getColumnIndexOrThrow("proteinGrams")), 0.01)
        assertEquals(52.0, mealCursor.getDouble(mealCursor.getColumnIndexOrThrow("carbsGrams")), 0.01)
        assertEquals(8.0, mealCursor.getDouble(mealCursor.getColumnIndexOrThrow("fatGrams")), 0.01)
        assertEquals("With whey protein", mealCursor.getString(mealCursor.getColumnIndexOrThrow("notes")))
        mealCursor.close()

        db.close()
    }
}
