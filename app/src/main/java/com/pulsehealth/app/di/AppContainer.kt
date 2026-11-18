package com.pulsehealth.app.di

import android.content.Context
import com.pulsehealth.app.data.AuthRepository
import com.pulsehealth.app.data.DailySummaryRepository
import com.pulsehealth.app.data.DataStoreSharingRepository
import com.pulsehealth.app.data.DefaultHealthConnectRepository
import com.pulsehealth.app.data.HealthConnectRepository
import com.pulsehealth.app.data.HealthDataRepository
import com.pulsehealth.app.data.HydrationRepository
import com.pulsehealth.app.data.PreferencesRepository
import com.pulsehealth.app.data.SharingRepository
import com.pulsehealth.app.data.SupabaseAuthRepository
import com.pulsehealth.app.data.UserPreferencesRepository
import com.pulsehealth.app.data.WorkoutRepository
import com.pulsehealth.app.data.DefaultWorkoutRepository
import com.pulsehealth.app.data.SleepRepository
import com.pulsehealth.app.data.DefaultSleepRepository
import com.pulsehealth.app.data.VitalsRepository
import com.pulsehealth.app.data.DefaultVitalsRepository
import com.pulsehealth.app.data.analytics.InsightsRepository
import com.pulsehealth.app.data.analytics.DefaultInsightsRepository
import com.pulsehealth.app.data.local.PulseDatabase
import com.pulsehealth.app.data.sync.SupabaseSyncRepository
import com.pulsehealth.app.data.sync.SyncRepository
import com.pulsehealth.app.notifications.ReminderScheduler
import com.pulsehealth.app.notifications.WorkManagerReminderScheduler
import com.pulsehealth.app.util.LiveNetworkMonitor
import com.pulsehealth.app.util.NetworkMonitor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.time.Clock
import java.time.ZoneId

interface AppContainer {
    val hydrationRepository: HydrationRepository
    val dailySummaryRepository: DailySummaryRepository
    val preferencesRepository: PreferencesRepository
    val healthConnectRepository: HealthConnectRepository
    val authRepository: AuthRepository
    val syncRepository: SyncRepository
    val reminderScheduler: ReminderScheduler
    val sharingRepository: SharingRepository
    val workoutRepository: WorkoutRepository
    val sleepRepository: SleepRepository
    val vitalsRepository: VitalsRepository
    val insightsRepository: InsightsRepository
    val networkMonitor: NetworkMonitor
    val clock: Clock
    val zoneId: ZoneId
    val ioDispatcher: CoroutineDispatcher
}

class DefaultAppContainer(private val applicationContext: Context) : AppContainer {
    override val clock: Clock = Clock.systemDefaultZone()
    override val zoneId: ZoneId = ZoneId.systemDefault()
    override val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    override val reminderScheduler: ReminderScheduler by lazy {
        WorkManagerReminderScheduler(applicationContext)
    }

    override val networkMonitor: NetworkMonitor by lazy {
        LiveNetworkMonitor(applicationContext)
    }

    private val database: PulseDatabase by lazy {
        PulseDatabase.get(applicationContext)
    }

    override val authRepository: AuthRepository by lazy {
        SupabaseAuthRepository(
            context = applicationContext,
            supabaseUrl = com.pulsehealth.app.BuildConfig.SUPABASE_URL,
            supabaseKey = com.pulsehealth.app.BuildConfig.SUPABASE_KEY,
            ioDispatcher = ioDispatcher
        )
    }

    override val syncRepository: SyncRepository by lazy {
        SupabaseSyncRepository(
            context = applicationContext,
            hydrationDao = database.hydrationDao(),
            authRepository = authRepository,
            supabaseClientProvider = {
                (authRepository as? SupabaseAuthRepository)?.client
            },
            ioDispatcher = ioDispatcher
        )
    }

    private val healthDataRepo: HealthDataRepository by lazy {
        HealthDataRepository(
            hydrationDao = database.hydrationDao(),
            dailyHealthSummaryDao = database.dailyHealthSummaryDao(),
            workoutDao = database.workoutDao(),
            clock = clock,
            zoneId = zoneId,
            ioDispatcher = ioDispatcher
        )
    }

    override val workoutRepository: WorkoutRepository by lazy {
        DefaultWorkoutRepository(
            workoutDao = database.workoutDao(),
            clock = clock,
            zoneId = zoneId,
            ioDispatcher = ioDispatcher
        )
    }

    override val sleepRepository: SleepRepository by lazy {
        DefaultSleepRepository(
            sleepDao = database.sleepDao(),
            clock = clock,
            zoneId = zoneId,
            ioDispatcher = ioDispatcher
        )
    }

    override val vitalsRepository: VitalsRepository by lazy {
        DefaultVitalsRepository(
            vitalRecordDao = database.vitalRecordDao(),
            clock = clock,
            ioDispatcher = ioDispatcher
        )
    }

    override val insightsRepository: InsightsRepository by lazy {
        DefaultInsightsRepository(
            dailySummaryDao = database.dailyHealthSummaryDao(),
            vitalsRepository = vitalsRepository,
            hydrationRepository = hydrationRepository,
            sleepRepository = sleepRepository,
            preferencesRepository = preferencesRepository,
            clock = clock,
            zoneId = zoneId
        )
    }

    override val hydrationRepository: HydrationRepository get() = healthDataRepo
    override val dailySummaryRepository: DailySummaryRepository get() = healthDataRepo

    override val preferencesRepository: PreferencesRepository by lazy {
        UserPreferencesRepository(
            context = applicationContext,
            ioDispatcher = ioDispatcher
        )
    }

    override val healthConnectRepository: HealthConnectRepository by lazy {
        DefaultHealthConnectRepository(
            context = applicationContext,
            clock = clock,
            zoneId = zoneId,
            ioDispatcher = ioDispatcher
        )
    }

    override val sharingRepository: SharingRepository by lazy {
        DataStoreSharingRepository(
            context = applicationContext,
            ioDispatcher = ioDispatcher
        )
    }
}
