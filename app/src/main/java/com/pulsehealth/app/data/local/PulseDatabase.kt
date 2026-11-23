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
        WorkoutSessionEntity::class,
        SleepSessionEntity::class,
        SleepStageEntity::class,
        VitalRecordEntity::class,
        MedicationEntity::class,
        MedicationLogEntity::class,
        MealLogEntity::class
    ],
    version = 6,
    exportSchema = true
)
abstract class PulseDatabase : RoomDatabase() {
    abstract fun hydrationDao(): HydrationDao
    abstract fun dailyHealthSummaryDao(): DailyHealthSummaryDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun sleepDao(): SleepDao
    abstract fun vitalRecordDao(): VitalRecordDao
    abstract fun medicationDao(): MedicationDao
    abstract fun mealDao(): MealDao

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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sleep_sessions` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `startTimeEpochMillis` INTEGER NOT NULL,
                        `endTimeEpochMillis` INTEGER NOT NULL,
                        `durationMinutes` INTEGER NOT NULL,
                        `deepDurationMinutes` INTEGER NOT NULL,
                        `lightDurationMinutes` INTEGER NOT NULL,
                        `remDurationMinutes` INTEGER NOT NULL,
                        `awakeDurationMinutes` INTEGER NOT NULL,
                        `sleepEfficiencyPercentage` INTEGER NOT NULL,
                        `source` TEXT NOT NULL,
                        `notes` TEXT,
                        `syncState` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_sleep_sessions_startTimeEpochMillis` ON `sleep_sessions` (`startTimeEpochMillis`)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sleep_stages` (
                        `id` TEXT NOT NULL,
                        `sessionId` TEXT NOT NULL,
                        `stage` TEXT NOT NULL,
                        `startTimeEpochMillis` INTEGER NOT NULL,
                        `endTimeEpochMillis` INTEGER NOT NULL,
                        `durationMinutes` INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`sessionId`) REFERENCES `sleep_sessions`(`id`) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_sleep_stages_sessionId` ON `sleep_stages` (`sessionId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_sleep_stages_startTimeEpochMillis` ON `sleep_stages` (`startTimeEpochMillis`)"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `vital_records` (
                        `id` TEXT NOT NULL,
                        `vitalType` TEXT NOT NULL,
                        `systolic` INTEGER,
                        `diastolic` INTEGER,
                        `heartRateBpm` INTEGER,
                        `recordedAtEpochMillis` INTEGER NOT NULL,
                        `measurementContext` TEXT NOT NULL,
                        `notes` TEXT,
                        `source` TEXT NOT NULL,
                        `syncState` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_vital_records_recordedAtEpochMillis` ON `vital_records` (`recordedAtEpochMillis`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_vital_records_vitalType` ON `vital_records` (`vitalType`)"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `medications` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `dosage` REAL NOT NULL,
                        `unit` TEXT NOT NULL,
                        `frequency` TEXT NOT NULL,
                        `scheduledTimes` TEXT NOT NULL,
                        `instructions` TEXT,
                        `colorHex` TEXT NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `createdAtEpochMillis` INTEGER NOT NULL,
                        `notes` TEXT,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `medication_logs` (
                        `id` TEXT NOT NULL,
                        `medicationId` TEXT NOT NULL,
                        `scheduledTimeEpochMillis` INTEGER NOT NULL,
                        `loggedAtEpochMillis` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `notes` TEXT,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`medicationId`) REFERENCES `medications`(`id`) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_medication_logs_medicationId` ON `medication_logs` (`medicationId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_medication_logs_scheduledTimeEpochMillis` ON `medication_logs` (`scheduledTimeEpochMillis`)"
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `meal_logs` (
                        `id` TEXT NOT NULL,
                        `mealType` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `calories` INTEGER NOT NULL,
                        `proteinGrams` REAL NOT NULL,
                        `carbsGrams` REAL NOT NULL,
                        `fatGrams` REAL NOT NULL,
                        `loggedAtEpochMillis` INTEGER NOT NULL,
                        `notes` TEXT,
                        `syncState` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_meal_logs_loggedAtEpochMillis` ON `meal_logs` (`loggedAtEpochMillis`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_meal_logs_mealType` ON `meal_logs` (`mealType`)"
                )
            }
        }

        fun get(context: Context): PulseDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                PulseDatabase::class.java,
                "pulse_health.db"
            )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
            .build()
            .also { instance = it }
        }
    }
}
