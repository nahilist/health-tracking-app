package com.pulsehealth.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        HydrationEntryEntity::class,
        DailyHealthSummaryEntity::class,
        WorkoutSessionEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class PulseDatabase : RoomDatabase() {
    abstract fun hydrationDao(): HydrationDao
    abstract fun dailyHealthSummaryDao(): DailyHealthSummaryDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile private var instance: PulseDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `workout_sessions` (
                        `id` TEXT NOT NULL,
                        `workoutType` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `startTimeEpochMillis` INTEGER NOT NULL,
                        `endTimeEpochMillis` INTEGER NOT NULL,
                        `durationMinutes` INTEGER NOT NULL,
                        `activeCalories` INTEGER,
                        `distanceMeters` REAL,
                        `source` TEXT NOT NULL,
                        `notes` TEXT,
                        `syncState` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_workout_sessions_startTimeEpochMillis` ON `workout_sessions` (`startTimeEpochMillis`)"
                )
            }
        }

        fun get(context: Context): PulseDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                PulseDatabase::class.java,
                "pulse_health.db"
            )
            .addMigrations(MIGRATION_1_2)
            .build()
            .also { instance = it }
        }
    }
}
