package com.pulsehealth.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HydrationEntryEntity::class, DailyHealthSummaryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class PulseDatabase : RoomDatabase() {
    abstract fun hydrationDao(): HydrationDao
    abstract fun dailyHealthSummaryDao(): DailyHealthSummaryDao

    companion object {
        @Volatile private var instance: PulseDatabase? = null

        fun get(context: Context): PulseDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                PulseDatabase::class.java,
                "pulse_health.db"
            ).build().also { instance = it }
        }
    }
}
