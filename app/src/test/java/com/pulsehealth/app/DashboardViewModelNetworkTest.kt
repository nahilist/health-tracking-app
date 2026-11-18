package com.pulsehealth.app

import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.SavedStateHandle
import com.pulsehealth.app.data.AuthState
import com.pulsehealth.app.data.AuthUser
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.data.HealthConnectAvailability
import com.pulsehealth.app.data.HealthConnectRepository
import com.pulsehealth.app.data.HydrationRepository
import com.pulsehealth.app.data.NoOpAuthRepository
import com.pulsehealth.app.data.PreferencesRepository
import com.pulsehealth.app.data.TimeRange
import com.pulsehealth.app.data.local.HydrationEntryEntity
import com.pulsehealth.app.data.sync.NoOpSyncRepository
import com.pulsehealth.app.data.sync.SyncRepository
import com.pulsehealth.app.data.sync.SyncResult
import com.pulsehealth.app.ui.DashboardViewModel
import com.pulsehealth.app.util.TestNetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelNetworkTest {

    private val testDispatcher = StandardTestDispatcher()

    private class TestHydrationRepo : HydrationRepository {
        private val _water = MutableStateFlow(0.0)
        override val todayWaterLiters: Flow<Double> = _water.asStateFlow()
        override val weeklyWaterLiters: Flow<Double> = _water.asStateFlow()
        override fun observeTodayEntries(): Flow<List<HydrationEntryEntity>> = MutableStateFlow(emptyList())
        override fun observeRecentEntries(limit: Int, offset: Int): Flow<List<HydrationEntryEntity>> = MutableStateFlow(emptyList())
        override fun observePastDaysEntries(days: Int): Flow<List<HydrationEntryEntity>> = MutableStateFlow(emptyList())
        override suspend fun addWater(amountMilliliters: Int, recordedAtEpochMillis: Long?) {}
        override suspend fun updateWater(id: String, amountMilliliters: Int): Boolean = true
        override suspend fun deleteWater(id: String): Boolean = true
        override suspend fun getAllEntries(): List<HydrationEntryEntity> = emptyList()
        override suspend fun clearAllData() {}
        override suspend fun exportDataJson(): String = "{}"
        override suspend fun exportDataCsv(): String = ""
        override suspend fun exportClinicalSummary(): String = ""
    }

    private class TestPreferencesRepo : PreferencesRepository {
        override val hiddenCards: Flow<Set<String>> = MutableStateFlow(emptySet())
        override val waterGoalLiters: Flow<Double> = MutableStateFlow(2.0)
        override val stepGoal: Flow<Long> = MutableStateFlow(10_000L)
        override val calorieGoal: Flow<Long> = MutableStateFlow(500L)
        override val sleepGoalMinutes: Flow<Long> = MutableStateFlow(480L)
        override val syncWaterToHealthConnect: Flow<Boolean> = MutableStateFlow(false)
        override val onboardingCompleted: Flow<Boolean> = MutableStateFlow(true)
        override val remindersEnabled: Flow<Boolean> = MutableStateFlow(false)
        override val reminderIntervalHours: Flow<Int> = MutableStateFlow(2)
        override val quietHoursStartHour: Flow<Int> = MutableStateFlow(22)
        override val quietHoursEndHour: Flow<Int> = MutableStateFlow(8)
        override val biometricLockEnabled: Flow<Boolean> = MutableStateFlow(false)
        override val biometricLockTimeoutSeconds: Flow<Long> = MutableStateFlow(0L)

        override suspend fun toggleCard(id: String) {}
        override suspend fun setWaterGoalLiters(goalLiters: Double) {}
        override suspend fun setStepGoal(steps: Long) {}
        override suspend fun setCalorieGoal(calories: Long) {}
        override suspend fun setSleepGoalMinutes(minutes: Long) {}
        override suspend fun setSyncWaterToHealthConnect(enabled: Boolean) {}
        override suspend fun setOnboardingCompleted(completed: Boolean) {}
        override suspend fun setRemindersEnabled(enabled: Boolean) {}
        override suspend fun setReminderIntervalHours(hours: Int) {}
        override suspend fun setQuietHours(startHour: Int, endHour: Int) {}
        override suspend fun setBiometricLockEnabled(enabled: Boolean) {}
        override suspend fun setBiometricLockTimeoutSeconds(seconds: Long) {}
        override suspend fun clearAllPreferences() {}
    }

    private class TestHealthConnectRepo : HealthConnectRepository {
        override val readPermissions: Set<String> = emptySet()
        override val writePermissions: Set<String> = emptySet()
        override val allPermissions: Set<String> = emptySet()
        override val permissions: Set<String> = emptySet()
        override fun getAvailability(): HealthConnectAvailability = HealthConnectAvailability.AVAILABLE
        override fun isAvailable(): Boolean = true
        override fun permissionContract(): ActivityResultContract<Set<String>, Set<String>> {
            throw UnsupportedOperationException()
        }
        override suspend fun hasPermissions(): Boolean = true
        override suspend fun hasWriteHydrationPermission(): Boolean = false
        override suspend fun read(range: TimeRange, current: DashboardMetrics): DashboardMetrics = current
        override suspend fun writeHydration(amountMilliliters: Int, recordedAtEpochMillis: Long): Boolean = true
    }

    private class TestSyncRepo : SyncRepository {
        var syncCount = 0
        override val isSyncing: StateFlow<Boolean> = MutableStateFlow(false)
        override val lastSyncedEpochMillis: StateFlow<Long?> = MutableStateFlow(null)
        override val lastSyncError: StateFlow<String?> = MutableStateFlow(null)

        override suspend fun sync(): SyncResult {
            syncCount++
            return SyncResult.Success(uploadedCount = 1, downloadedCount = 0)
        }

        override fun schedulePeriodicSync() {}
        override fun triggerImmediateSync() {}
        override fun cancelPeriodicSync() {}
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialNetworkState_reflectedInUiState() = runTest {
        val networkMonitor = TestNetworkMonitor(initialOnline = true)
        val viewModel = DashboardViewModel(
            savedStateHandle = SavedStateHandle(),
            hydrationRepository = TestHydrationRepo(),
            preferencesRepository = TestPreferencesRepo(),
            healthRepository = TestHealthConnectRepo(),
            networkMonitor = networkMonitor
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isOnline)
    }

    @Test
    fun networkStateChange_dynamicallyUpdatesUiState() = runTest {
        val networkMonitor = TestNetworkMonitor(initialOnline = true)
        val viewModel = DashboardViewModel(
            savedStateHandle = SavedStateHandle(),
            hydrationRepository = TestHydrationRepo(),
            preferencesRepository = TestPreferencesRepo(),
            healthRepository = TestHealthConnectRepo(),
            networkMonitor = networkMonitor
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isOnline)

        networkMonitor.setOnline(false)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isOnline)

        networkMonitor.setOnline(true)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isOnline)
    }

    @Test
    fun offlineTransitionToOnline_autoSyncsForAuthenticatedUser() = runTest {
        val networkMonitor = TestNetworkMonitor(initialOnline = false)
        val authRepo = NoOpAuthRepository(
            initialState = AuthState.Authenticated(
                AuthUser(id = "user-1", email = "test@pulse.local", displayName = "Test User", isAnonymous = false)
            )
        )
        val syncRepo = TestSyncRepo()

        val viewModel = DashboardViewModel(
            savedStateHandle = SavedStateHandle(),
            hydrationRepository = TestHydrationRepo(),
            preferencesRepository = TestPreferencesRepo(),
            healthRepository = TestHealthConnectRepo(),
            authRepository = authRepo,
            syncRepository = syncRepo,
            networkMonitor = networkMonitor
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertEquals(0, syncRepo.syncCount)

        // When reconnecting from offline to online
        networkMonitor.setOnline(true)
        advanceUntilIdle()

        // Auto-sync should be triggered immediately
        assertEquals(1, syncRepo.syncCount)
    }

    @Test
    fun offlineTransitionToOnline_doesNotAutoSyncForGuestUser() = runTest {
        val networkMonitor = TestNetworkMonitor(initialOnline = false)
        val authRepo = NoOpAuthRepository(initialState = AuthState.Guest)
        val syncRepo = TestSyncRepo()

        val viewModel = DashboardViewModel(
            savedStateHandle = SavedStateHandle(),
            hydrationRepository = TestHydrationRepo(),
            preferencesRepository = TestPreferencesRepo(),
            healthRepository = TestHealthConnectRepo(),
            authRepository = authRepo,
            syncRepository = syncRepo,
            networkMonitor = networkMonitor
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertEquals(0, syncRepo.syncCount)

        networkMonitor.setOnline(true)
        advanceUntilIdle()

        // Guest user records are local-only, should not attempt cloud sync
        assertEquals(0, syncRepo.syncCount)
    }

    @Test
    fun manualSync_whenOffline_returnsGracefulOfflineErrorWithoutHanging() = runTest {
        val networkMonitor = TestNetworkMonitor(initialOnline = false)
        val syncRepo = TestSyncRepo()

        val viewModel = DashboardViewModel(
            savedStateHandle = SavedStateHandle(),
            hydrationRepository = TestHydrationRepo(),
            preferencesRepository = TestPreferencesRepo(),
            healthRepository = TestHealthConnectRepo(),
            syncRepository = syncRepo,
            networkMonitor = networkMonitor
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        var syncResult: SyncResult? = null
        viewModel.syncCloud { result ->
            syncResult = result
        }
        advanceUntilIdle()

        assertTrue(syncResult is SyncResult.Error)
        val error = syncResult as SyncResult.Error
        assertTrue(error.message.contains("offline", ignoreCase = true))
        assertEquals(0, syncRepo.syncCount)
    }
}
