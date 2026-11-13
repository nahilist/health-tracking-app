package com.pulsehealth.app.di

import android.content.Context
import com.pulsehealth.app.data.AuthRepository
import com.pulsehealth.app.data.DefaultHealthConnectRepository
import com.pulsehealth.app.data.HealthConnectRepository
import com.pulsehealth.app.data.HealthDataRepository
import com.pulsehealth.app.data.HydrationRepository
import com.pulsehealth.app.data.PreferencesRepository
import com.pulsehealth.app.data.SupabaseAuthRepository
import com.pulsehealth.app.data.UserPreferencesRepository
import com.pulsehealth.app.data.local.PulseDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.time.Clock
import java.time.ZoneId

interface AppContainer {
    val hydrationRepository: HydrationRepository
    val preferencesRepository: PreferencesRepository
    val healthConnectRepository: HealthConnectRepository
    val authRepository: AuthRepository
    val clock: Clock
    val zoneId: ZoneId
    val ioDispatcher: CoroutineDispatcher
}

class DefaultAppContainer(private val applicationContext: Context) : AppContainer {
    override val clock: Clock = Clock.systemDefaultZone()
    override val zoneId: ZoneId = ZoneId.systemDefault()
    override val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val database: PulseDatabase by lazy {
        PulseDatabase.get(applicationContext)
    }

    override val authRepository: AuthRepository by lazy {
        SupabaseAuthRepository(
            context = applicationContext,
            ioDispatcher = ioDispatcher
        )
    }

    override val hydrationRepository: HydrationRepository by lazy {
        HealthDataRepository(
            hydrationDao = database.hydrationDao(),
            dailyHealthSummaryDao = database.dailyHealthSummaryDao(),
            clock = clock,
            zoneId = zoneId,
            ioDispatcher = ioDispatcher
        )
    }

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
}
