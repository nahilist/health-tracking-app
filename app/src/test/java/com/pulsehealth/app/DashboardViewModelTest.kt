package com.pulsehealth.app

import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.SavedStateHandle
import com.pulsehealth.app.data.DailySummaryRepository
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.data.DataSource
import com.pulsehealth.app.data.HealthConnectAvailability
import com.pulsehealth.app.data.HealthConnectRepository
import com.pulsehealth.app.data.HydrationRepository
import com.pulsehealth.app.data.PreferencesRepository
import com.pulsehealth.app.data.TimeRange
import com.pulsehealth.app.data.local.DailyHealthSummaryEntity
import com.pulsehealth.app.data.local.HydrationEntryEntity
import com.pulsehealth.app.ui.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeHydrationRepository(initialWater: Double = 0.0) : HydrationRepository {
        private val _water = MutableStateFlow(initialWater)
        override val todayWaterLiters: Flow<Double> = _water.asStateFlow()

        private val _weeklyWater = MutableStateFlow(initialWater)
        override val weeklyWaterLiters: Flow<Double> = _weeklyWater.asStateFlow()

        private val _entries = MutableStateFlow<List<HydrationEntryEntity>>(emptyList())
        override fun observeTodayEntries(): Flow<List<HydrationEntryEntity>> = _entries.asStateFlow()

        override fun observeRecentEntries(limit: Int, offset: Int): Flow<List<HydrationEntryEntity>> =
            _entries.asStateFlow()

        override fun observePastDaysEntries(days: Int): Flow<List<HydrationEntryEntity>> =
            _entries.asStateFlow()

        override suspend fun addWater(amountMilliliters: Int, recordedAtEpochMillis: Long?) {
            _water.value += amountMilliliters / 1000.0
            _weeklyWater.value += amountMilliliters / 1000.0
            val newEntry = HydrationEntryEntity(
                id = UUID.randomUUID().toString(),
                amountMilliliters = amountMilliliters,
                recordedAtEpochMillis = recordedAtEpochMillis ?: System.currentTimeMillis(),
                source = "manual",
                syncState = "pending"
            )
            _entries.value = listOf(newEntry) + _entries.value
        }

        override suspend fun updateWater(id: String, amountMilliliters: Int): Boolean {
            val current = _entries.value
            val existing = current.find { it.id == id } ?: return false
            val diff = (amountMilliliters - existing.amountMilliliters) / 1000.0
            _water.value += diff
            _weeklyWater.value += diff
            _entries.value = current.map {
                if (it.id == id) it.copy(amountMilliliters = amountMilliliters) else it
            }
            return true
        }

        override suspend fun deleteWater(id: String): Boolean {
            val current = _entries.value
            val existing = current.find { it.id == id } ?: return false
            _water.value -= existing.amountMilliliters / 1000.0
            _weeklyWater.value -= existing.amountMilliliters / 1000.0
            _entries.value = current.filterNot { it.id == id }
            return true
        }

        override suspend fun getAllEntries(): List<HydrationEntryEntity> = _entries.value

        override suspend fun clearAllData() {
            _water.value = 0.0
            _weeklyWater.value = 0.0
            _entries.value = emptyList()
        }

        override suspend fun exportDataJson(): String =
            "{\"app\":\"Pulse Health\",\"hydrationEntriesCount\":${_entries.value.size}}"

        override suspend fun exportDataCsv(): String =
            "Date,Amount_mL\n2026-11-13,${_entries.value.size}"

        override suspend fun exportClinicalSummary(): String =
            "PULSE HEALTH WELLNESS REPORT - Entries: ${_entries.value.size}"
    }

    private class FakePreferencesRepository(
        initialHidden: Set<String> = emptySet(),
        initialGoal: Double = 2.0,
        initialSyncWater: Boolean = false,
        initialOnboardingCompleted: Boolean = true
    ) : PreferencesRepository {
        private val _hidden = MutableStateFlow(initialHidden)
        override val hiddenCards: Flow<Set<String>> = _hidden.asStateFlow()

        private val _goal = MutableStateFlow(initialGoal)
        override val waterGoalLiters: Flow<Double> = _goal.asStateFlow()

        private val _stepGoal = MutableStateFlow(10_000L)
        override val stepGoal: Flow<Long> = _stepGoal.asStateFlow()

        private val _calorieGoal = MutableStateFlow(500L)
        override val calorieGoal: Flow<Long> = _calorieGoal.asStateFlow()

        private val _sleepGoalMinutes = MutableStateFlow(480L)
        override val sleepGoalMinutes: Flow<Long> = _sleepGoalMinutes.asStateFlow()

        private val _syncWater = MutableStateFlow(initialSyncWater)
        override val syncWaterToHealthConnect: Flow<Boolean> = _syncWater.asStateFlow()

        private val _onboardingCompleted = MutableStateFlow(initialOnboardingCompleted)
        override val onboardingCompleted: Flow<Boolean> = _onboardingCompleted.asStateFlow()

        private val _reminders = MutableStateFlow(false)
        override val remindersEnabled: Flow<Boolean> = _reminders.asStateFlow()

        private val _interval = MutableStateFlow(2)
        override val reminderIntervalHours: Flow<Int> = _interval.asStateFlow()

        private val _quietStart = MutableStateFlow(22)
        override val quietHoursStartHour: Flow<Int> = _quietStart.asStateFlow()

        private val _quietEnd = MutableStateFlow(8)
        override val quietHoursEndHour: Flow<Int> = _quietEnd.asStateFlow()

        private val _biometricLock = MutableStateFlow(false)
        override val biometricLockEnabled: Flow<Boolean> = _biometricLock.asStateFlow()

        private val _biometricTimeout = MutableStateFlow(0L)
        override val biometricLockTimeoutSeconds: Flow<Long> = _biometricTimeout.asStateFlow()

        override suspend fun toggleCard(id: String) {
            val current = _hidden.value.toMutableSet()
            if (!current.add(id)) current.remove(id)
            _hidden.value = current
        }

        override suspend fun setWaterGoalLiters(goalLiters: Double) {
            _goal.value = goalLiters
        }

        override suspend fun setStepGoal(steps: Long) {
            _stepGoal.value = steps
        }

        override suspend fun setCalorieGoal(calories: Long) {
            _calorieGoal.value = calories
        }

        override suspend fun setSleepGoalMinutes(minutes: Long) {
            _sleepGoalMinutes.value = minutes
        }

        override suspend fun setSyncWaterToHealthConnect(enabled: Boolean) {
            _syncWater.value = enabled
        }

        override suspend fun setOnboardingCompleted(completed: Boolean) {
            _onboardingCompleted.value = completed
        }

        override suspend fun setRemindersEnabled(enabled: Boolean) {
            _reminders.value = enabled
        }

        override suspend fun setReminderIntervalHours(hours: Int) {
            _interval.value = hours
        }

        override suspend fun setQuietHours(startHour: Int, endHour: Int) {
            _quietStart.value = startHour
            _quietEnd.value = endHour
        }

        override suspend fun setBiometricLockEnabled(enabled: Boolean) {
            _biometricLock.value = enabled
        }

        override suspend fun setBiometricLockTimeoutSeconds(seconds: Long) {
            _biometricTimeout.value = seconds
        }

        override suspend fun clearAllPreferences() {
            _hidden.value = emptySet()
            _goal.value = 2.0
            _syncWater.value = false
            _onboardingCompleted.value = false
            _reminders.value = false
            _interval.value = 2
            _quietStart.value = 22
            _quietEnd.value = 8
            _biometricLock.value = false
            _biometricTimeout.value = 0L
        }
    }

    private class FakeHealthConnectRepository(
        var currentAvailability: HealthConnectAvailability = HealthConnectAvailability.AVAILABLE,
        var hasPermissions: Boolean = false,
        var hasWriteHydrationPermission: Boolean = false,
        var shouldThrowOnRead: Boolean = false
    ) : HealthConnectRepository {
        override val readPermissions: Set<String> = setOf("READ_TEST")
        override val writePermissions: Set<String> = setOf("WRITE_TEST")
        override val allPermissions: Set<String> = readPermissions + writePermissions
        override val permissions: Set<String> get() = readPermissions

        val writtenHydration = mutableListOf<Pair<Int, Long>>()

        override fun getAvailability(): HealthConnectAvailability = currentAvailability
        override fun isAvailable(): Boolean = currentAvailability == HealthConnectAvailability.AVAILABLE
        override fun permissionContract(): ActivityResultContract<Set<String>, Set<String>> {
            throw UnsupportedOperationException("Not needed in unit test")
        }
        override suspend fun hasPermissions(): Boolean = hasPermissions
        override suspend fun hasWriteHydrationPermission(): Boolean = hasWriteHydrationPermission
        override suspend fun read(range: TimeRange, current: DashboardMetrics): DashboardMetrics {
            if (shouldThrowOnRead) throw IllegalStateException("Health Connect disconnected")
            return current.copy(steps = 5_000, source = DataSource.HEALTH_CONNECT, contributingApps = setOf("Google Fit"))
        }

        override suspend fun writeHydration(amountMilliliters: Int, recordedAtEpochMillis: Long): Boolean {
            writtenHydration.add(amountMilliliters to recordedAtEpochMillis)
            return true
        }
    }

    private class FakeDailySummaryRepository : DailySummaryRepository {
        private val summaries = mutableMapOf<Long, DailyHealthSummaryEntity>()
        private val _flow = MutableStateFlow<List<DailyHealthSummaryEntity>>(emptyList())

        override fun observeDailySummary(epochDay: Long): Flow<DailyHealthSummaryEntity?> =
            MutableStateFlow(summaries[epochDay]).asStateFlow()

        override fun observeSummaryRange(startEpochDay: Long, endEpochDay: Long): Flow<List<DailyHealthSummaryEntity>> =
            _flow.asStateFlow()

        override suspend fun getSummary(epochDay: Long): DailyHealthSummaryEntity? = summaries[epochDay]

        override suspend fun saveDailySummary(summary: DailyHealthSummaryEntity) {
            summaries[summary.epochDay] = summary
            _flow.value = summaries.values.toList()
        }

        override suspend fun saveDailySummaries(summaries: List<DailyHealthSummaryEntity>) {
            summaries.forEach { this.summaries[it.epochDay] = it }
            _flow.value = this.summaries.values.toList()
        }

        override suspend fun getAllSummaries(): List<DailyHealthSummaryEntity> = summaries.values.toList()

        override suspend fun deleteAllSummaries(): Int {
            val count = summaries.size
            summaries.clear()
            _flow.value = emptyList()
            return count
        }
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
    fun initialStateObservesHydrationAndPreferences() = runTest {
        val hydrationRepo = FakeHydrationRepository(initialWater = 1.0)
        val prefRepo = FakePreferencesRepository(initialHidden = setOf("Sleep"), initialGoal = 2.5)
        val healthRepo = FakeHealthConnectRepository()

        val viewModel = DashboardViewModel(SavedStateHandle(), hydrationRepo, prefRepo, healthRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1.0, state.metrics.waterLiters, 0.0)
        assertEquals(2.5, state.metrics.waterGoalLiters, 0.0)
        assertTrue(state.hiddenCards.contains("Sleep"))
        assertFalse(state.isDemoMode)
        assertEquals(TimeRange.TODAY, state.selectedRange)
    }

    @Test
    fun statePreservedAcrossRecreationViaSavedStateHandle() = runTest {
        val savedState = SavedStateHandle(
            mapOf(
                DashboardViewModel.KEY_SELECTED_RANGE to TimeRange.WEEK.name,
                DashboardViewModel.KEY_IS_DEMO_MODE to true
            )
        )
        val hydrationRepo = FakeHydrationRepository()
        val prefRepo = FakePreferencesRepository()
        val healthRepo = FakeHealthConnectRepository()

        val viewModel = DashboardViewModel(savedState, hydrationRepo, prefRepo, healthRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(TimeRange.WEEK, state.selectedRange)
        assertTrue(state.isDemoMode)
        assertEquals(DataSource.DEMO, state.metrics.source)
    }

    @Test
    fun addingWaterInvokesRepositoryAndUpdatesMetrics() = runTest {
        val hydrationRepo = FakeHydrationRepository(initialWater = 0.0)
        val prefRepo = FakePreferencesRepository()
        val healthRepo = FakeHealthConnectRepository()

        val viewModel = DashboardViewModel(SavedStateHandle(), hydrationRepo, prefRepo, healthRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.todayHydrationEntries.collect()
        }

        viewModel.addWater(250)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0.25, state.metrics.waterLiters, 0.001)
        assertEquals(DataSource.LOCAL, state.metrics.source)
        assertEquals(1, viewModel.todayHydrationEntries.value.size)
    }

    @Test
    fun editingAndDeleteWaterUpdatesTotalsAndEntries() = runTest {
        val hydrationRepo = FakeHydrationRepository(initialWater = 0.0)
        val prefRepo = FakePreferencesRepository()
        val healthRepo = FakeHealthConnectRepository()

        val viewModel = DashboardViewModel(SavedStateHandle(), hydrationRepo, prefRepo, healthRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.todayHydrationEntries.collect()
        }

        viewModel.addWater(250)
        advanceUntilIdle()

        val entryId = viewModel.todayHydrationEntries.value.first().id
        viewModel.editWater(entryId, 500)
        advanceUntilIdle()

        assertEquals(0.50, viewModel.uiState.value.metrics.waterLiters, 0.001)
        assertEquals(500, viewModel.todayHydrationEntries.value.first().amountMilliliters)

        viewModel.deleteWater(entryId)
        advanceUntilIdle()

        assertEquals(0.0, viewModel.uiState.value.metrics.waterLiters, 0.001)
        assertTrue(viewModel.todayHydrationEntries.value.isEmpty())
    }

    @Test
    fun updatingWaterGoalReflectsInUiState() = runTest {
        val hydrationRepo = FakeHydrationRepository()
        val prefRepo = FakePreferencesRepository(initialGoal = 2.0)
        val healthRepo = FakeHealthConnectRepository()

        val viewModel = DashboardViewModel(SavedStateHandle(), hydrationRepo, prefRepo, healthRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertEquals(2.0, viewModel.uiState.value.metrics.waterGoalLiters, 0.0)

        viewModel.setWaterGoal(3.2)
        advanceUntilIdle()

        assertEquals(3.2, viewModel.uiState.value.metrics.waterGoalLiters, 0.0)
    }

    @Test
    fun togglingDemoModeSuppliesLabeledSampleData() = runTest {
        val hydrationRepo = FakeHydrationRepository(initialWater = 0.0)
        val prefRepo = FakePreferencesRepository()
        val healthRepo = FakeHealthConnectRepository()

        val viewModel = DashboardViewModel(SavedStateHandle(), hydrationRepo, prefRepo, healthRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.toggleDemoMode()
        advanceUntilIdle()

        val demoState = viewModel.uiState.value
        assertTrue(demoState.isDemoMode)
        assertEquals(DataSource.DEMO, demoState.metrics.source)
        assertEquals(8_432L, demoState.metrics.steps)

        viewModel.toggleDemoMode()
        advanceUntilIdle()

        val regularState = viewModel.uiState.value
        assertFalse(regularState.isDemoMode)
        assertEquals(DataSource.NONE, regularState.metrics.source)
    }

    @Test
    fun syncFailureSurfacesErrorMessageAndAllowsClearing() = runTest {
        val hydrationRepo = FakeHydrationRepository()
        val prefRepo = FakePreferencesRepository()
        val healthRepo = FakeHealthConnectRepository(hasPermissions = true, shouldThrowOnRead = true)

        val viewModel = DashboardViewModel(SavedStateHandle(), hydrationRepo, prefRepo, healthRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        val errorState = viewModel.uiState.value
        assertEquals("Health data could not be refreshed. Your saved data is still available.", errorState.errorMessage)

        viewModel.clearError()
        advanceUntilIdle()

        val clearedState = viewModel.uiState.value
        assertNull(clearedState.errorMessage)
    }

    @Test
    fun syncWaterToHealthConnectWritesToHealthConnectWhenEnabledAndPermitted() = runTest {
        val hydrationRepo = FakeHydrationRepository()
        val prefRepo = FakePreferencesRepository(initialSyncWater = true)
        val healthRepo = FakeHealthConnectRepository(hasPermissions = true, hasWriteHydrationPermission = true)

        val viewModel = DashboardViewModel(SavedStateHandle(), hydrationRepo, prefRepo, healthRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        viewModel.addWater(350)
        advanceUntilIdle()

        assertEquals(1, healthRepo.writtenHydration.size)
        assertEquals(350, healthRepo.writtenHydration.first().first)
    }

    @Test
    fun syncWaterToHealthConnectDoesNotWriteWhenNotPermitted() = runTest {
        val hydrationRepo = FakeHydrationRepository()
        val prefRepo = FakePreferencesRepository(initialSyncWater = true)
        val healthRepo = FakeHealthConnectRepository(hasPermissions = true, hasWriteHydrationPermission = false)

        val viewModel = DashboardViewModel(SavedStateHandle(), hydrationRepo, prefRepo, healthRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        viewModel.addWater(350)
        advanceUntilIdle()

        assertTrue(healthRepo.writtenHydration.isEmpty())
    }

    @Test
    fun healthConnectAvailabilityStatesReflectedInUiState() = runTest {
        val hydrationRepo = FakeHydrationRepository()
        val prefRepo = FakePreferencesRepository()
        val healthRepo = FakeHealthConnectRepository(currentAvailability = HealthConnectAvailability.UPDATE_REQUIRED)

        val viewModel = DashboardViewModel(SavedStateHandle(), hydrationRepo, prefRepo, healthRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertEquals(HealthConnectAvailability.UPDATE_REQUIRED, viewModel.uiState.value.healthConnectAvailability)
        assertFalse(viewModel.uiState.value.healthConnectAvailable)
    }

    @Test
    fun togglingSyncWaterPreferencesSavesToRepository() = runTest {
        val hydrationRepo = FakeHydrationRepository()
        val prefRepo = FakePreferencesRepository(initialSyncWater = false)
        val healthRepo = FakeHealthConnectRepository()

        val viewModel = DashboardViewModel(SavedStateHandle(), hydrationRepo, prefRepo, healthRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.syncWaterToHealthConnect)

        viewModel.setSyncWaterToHealthConnect(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.syncWaterToHealthConnect)
    }

    @Test
    fun onboardingFlowSavesStateAndCompletes() = runTest {
        val hydrationRepo = FakeHydrationRepository()
        val prefRepo = FakePreferencesRepository(initialOnboardingCompleted = false)
        val healthRepo = FakeHealthConnectRepository()

        val viewModel = DashboardViewModel(SavedStateHandle(), hydrationRepo, prefRepo, healthRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.onboardingCompleted)

        viewModel.setOnboardingCompleted(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.onboardingCompleted)
    }

    @Test
    fun deleteAllLocalDataClearsRepositoryAndState() = runTest {
        val hydrationRepo = FakeHydrationRepository(initialWater = 1.5)
        val prefRepo = FakePreferencesRepository(initialHidden = setOf("Sleep"), initialSyncWater = true)
        val healthRepo = FakeHealthConnectRepository()

        val viewModel = DashboardViewModel(SavedStateHandle(), hydrationRepo, prefRepo, healthRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertEquals(1.5, viewModel.uiState.value.metrics.waterLiters, 0.001)

        var deletedCallbackCalled = false
        viewModel.deleteAllLocalData { deletedCallbackCalled = true }
        advanceUntilIdle()

        assertTrue(deletedCallbackCalled)
        assertEquals(0.0, viewModel.uiState.value.metrics.waterLiters, 0.001)
        assertTrue(viewModel.uiState.value.hiddenCards.isEmpty())
        assertFalse(viewModel.uiState.value.syncWaterToHealthConnect)
        assertEquals(0, hydrationRepo.getAllEntries().size)
    }

    @Test
    fun exportDataJsonProducesValidJsonFromRepository() = runTest {
        val hydrationRepo = FakeHydrationRepository()
        val prefRepo = FakePreferencesRepository()
        val healthRepo = FakeHealthConnectRepository()

        val viewModel = DashboardViewModel(SavedStateHandle(), hydrationRepo, prefRepo, healthRepo)
        val json = viewModel.exportDataJson()

        assertTrue(json.contains("\"app\":\"Pulse Health\""))
    }

    @Test
    fun signInAsGuestUpdatesUiStateToGuest() = runTest {
        val hydrationRepo = FakeHydrationRepository()
        val prefRepo = FakePreferencesRepository()
        val healthRepo = FakeHealthConnectRepository()
        val authRepo = com.pulsehealth.app.data.NoOpAuthRepository(com.pulsehealth.app.data.AuthState.Unauthenticated)

        val viewModel = DashboardViewModel(SavedStateHandle(), hydrationRepo, prefRepo, healthRepo, authRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertEquals(com.pulsehealth.app.data.AuthState.Unauthenticated, viewModel.uiState.value.authState)

        viewModel.signInAsGuest()
        advanceUntilIdle()

        assertEquals(com.pulsehealth.app.data.AuthState.Guest, viewModel.uiState.value.authState)
        assertTrue(viewModel.uiState.value.currentUser?.isAnonymous == true)
    }

    @Test
    fun signInWithEmailUpdatesUiStateToAuthenticated() = runTest {
        val hydrationRepo = FakeHydrationRepository()
        val prefRepo = FakePreferencesRepository()
        val healthRepo = FakeHealthConnectRepository()
        val authRepo = com.pulsehealth.app.data.NoOpAuthRepository(com.pulsehealth.app.data.AuthState.Unauthenticated)

        val viewModel = DashboardViewModel(SavedStateHandle(), hydrationRepo, prefRepo, healthRepo, authRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        viewModel.signInWithEmail("tester@pulsehealth.app", "pass12345")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.authState is com.pulsehealth.app.data.AuthState.Authenticated)
        assertEquals("tester@pulsehealth.app", viewModel.uiState.value.currentUser?.email)
    }

    @Test
    fun signOutRevertsUiStateToUnauthenticated() = runTest {
        val hydrationRepo = FakeHydrationRepository()
        val prefRepo = FakePreferencesRepository()
        val healthRepo = FakeHealthConnectRepository()
        val authRepo = com.pulsehealth.app.data.NoOpAuthRepository(
            com.pulsehealth.app.data.AuthState.Authenticated(
                com.pulsehealth.app.data.AuthUser(id = "user_1", email = "bob@example.com")
            )
        )

        val viewModel = DashboardViewModel(SavedStateHandle(), hydrationRepo, prefRepo, healthRepo, authRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.authState is com.pulsehealth.app.data.AuthState.Authenticated)

        viewModel.signOut()
        advanceUntilIdle()

        assertEquals(com.pulsehealth.app.data.AuthState.Unauthenticated, viewModel.uiState.value.authState)
        assertNull(viewModel.uiState.value.currentUser)
    }

    @Test
    fun syncCloudInvokesSyncRepository() = runTest {
        val hydrationRepo = FakeHydrationRepository()
        val prefRepo = FakePreferencesRepository()
        val healthRepo = FakeHealthConnectRepository()
        var syncInvoked = false
        val syncRepo = object : com.pulsehealth.app.data.sync.SyncRepository {
            override val isSyncing = MutableStateFlow(false)
            override val lastSyncedEpochMillis = MutableStateFlow<Long?>(null)
            override val lastSyncError = MutableStateFlow<String?>(null)
            override suspend fun sync(): com.pulsehealth.app.data.sync.SyncResult {
                syncInvoked = true
                return com.pulsehealth.app.data.sync.SyncResult.Success(1, 1)
            }
            override fun schedulePeriodicSync() {}
            override fun triggerImmediateSync() {}
            override fun cancelPeriodicSync() {}
        }

        val viewModel = DashboardViewModel(
            savedStateHandle = SavedStateHandle(),
            hydrationRepository = hydrationRepo,
            preferencesRepository = prefRepo,
            healthRepository = healthRepo,
            syncRepository = syncRepo
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        viewModel.syncCloud()
        advanceUntilIdle()

        assertTrue(syncInvoked)
    }

    @Test
    fun togglingRemindersUpdatesStateAndScheduler() = runTest {
        val hydrationRepo = FakeHydrationRepository()
        val prefRepo = FakePreferencesRepository()
        val healthRepo = FakeHealthConnectRepository()
        val reminderScheduler = com.pulsehealth.app.notifications.NoOpReminderScheduler()

        val viewModel = DashboardViewModel(
            savedStateHandle = SavedStateHandle(),
            hydrationRepository = hydrationRepo,
            preferencesRepository = prefRepo,
            healthRepository = healthRepo,
            reminderScheduler = reminderScheduler
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.remindersEnabled)

        viewModel.setRemindersEnabled(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.remindersEnabled)
        assertEquals(2, reminderScheduler.scheduledInterval)

        viewModel.setReminderIntervalHours(3)
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.reminderIntervalHours)
        assertEquals(3, reminderScheduler.scheduledInterval)

        viewModel.setRemindersEnabled(false)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.remindersEnabled)
        assertTrue(reminderScheduler.isCancelled)
    }

    @Test
    fun cachedDailySummaryLoadsImmediatelyIntoState() = runTest {
        val todayEpochDay = java.time.LocalDate.now().toEpochDay()
        val dailyRepo = FakeDailySummaryRepository()
        dailyRepo.saveDailySummary(
            DailyHealthSummaryEntity(
                epochDay = todayEpochDay,
                steps = 6_800L,
                restingHeartRate = 62,
                sleepMinutes = 440L,
                activeCalories = 380L,
                exerciseMinutes = 28L,
                standHours = 9L,
                source = "Health Connect",
                updatedAtEpochMillis = 1731500000000L
            )
        )

        val viewModel = DashboardViewModel(
            savedStateHandle = SavedStateHandle(),
            hydrationRepository = FakeHydrationRepository(),
            preferencesRepository = FakePreferencesRepository(),
            healthRepository = FakeHealthConnectRepository(hasPermissions = false),
            dailySummaryRepository = dailyRepo
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(6_800L, state.metrics.steps)
        assertEquals(62L, state.metrics.heartRate)
        assertEquals(440L, state.metrics.sleepMinutes)
    }

    @Test
    fun successfulHealthConnectSyncPersistsDailySummary() = runTest {
        val dailyRepo = FakeDailySummaryRepository()
        val healthRepo = FakeHealthConnectRepository(
            hasPermissions = true
        )

        val viewModel = DashboardViewModel(
            savedStateHandle = SavedStateHandle(),
            hydrationRepository = FakeHydrationRepository(),
            preferencesRepository = FakePreferencesRepository(),
            healthRepository = healthRepo,
            dailySummaryRepository = dailyRepo
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        val allSummaries = dailyRepo.getAllSummaries()
        assertEquals(1, allSummaries.size)
        assertEquals(5_000L, allSummaries.first().steps)
    }

    @Test
    fun deleteAllLocalDataClearsDailySummaries() = runTest {
        val todayEpochDay = java.time.LocalDate.now().toEpochDay()
        val dailyRepo = FakeDailySummaryRepository()
        dailyRepo.saveDailySummary(
            DailyHealthSummaryEntity(
                epochDay = todayEpochDay,
                steps = 5_000L,
                restingHeartRate = 60,
                sleepMinutes = 400L,
                activeCalories = 300L,
                exerciseMinutes = 20L,
                standHours = 6L,
                source = "Health Connect",
                updatedAtEpochMillis = 1000L
            )
        )

        val viewModel = DashboardViewModel(
            savedStateHandle = SavedStateHandle(),
            hydrationRepository = FakeHydrationRepository(),
            preferencesRepository = FakePreferencesRepository(),
            healthRepository = FakeHealthConnectRepository(),
            dailySummaryRepository = dailyRepo
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertEquals(1, dailyRepo.getAllSummaries().size)

        var deletedCalled = false
        viewModel.deleteAllLocalData { deletedCalled = true }
        advanceUntilIdle()

        assertTrue(deletedCalled)
        assertEquals(0, dailyRepo.getAllSummaries().size)
    }

    @Test
    fun deleteAccountPurgesAuthAndWipesLocalDataWhenRequested() = runTest {
        val todayEpochDay = java.time.LocalDate.now().toEpochDay()
        val dailyRepo = FakeDailySummaryRepository()
        dailyRepo.saveDailySummary(
            DailyHealthSummaryEntity(
                epochDay = todayEpochDay,
                steps = 5_000L,
                restingHeartRate = 60L,
                sleepMinutes = 400L,
                activeCalories = 300L,
                exerciseMinutes = 20L,
                standHours = 6L,
                source = "Health Connect",
                updatedAtEpochMillis = 1000L
            )
        )
        val hydrationRepo = FakeHydrationRepository(initialWater = 1.5)
        val authRepo = com.pulsehealth.app.data.NoOpAuthRepository(
            com.pulsehealth.app.data.AuthState.Authenticated(
                com.pulsehealth.app.data.AuthUser(id = "user_del", email = "delete@pulsehealth.app")
            )
        )

        val viewModel = DashboardViewModel(
            savedStateHandle = SavedStateHandle(),
            hydrationRepository = hydrationRepo,
            preferencesRepository = FakePreferencesRepository(),
            healthRepository = FakeHealthConnectRepository(),
            authRepository = authRepo,
            dailySummaryRepository = dailyRepo
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.authState is com.pulsehealth.app.data.AuthState.Authenticated)
        assertEquals(1, dailyRepo.getAllSummaries().size)

        var completedCalled = false
        viewModel.deleteAccount(alsoDeleteLocalData = true) {
            completedCalled = true
        }
        advanceUntilIdle()

        assertTrue(completedCalled)
        assertEquals(com.pulsehealth.app.data.AuthState.Unauthenticated, viewModel.uiState.value.authState)
        assertNull(viewModel.uiState.value.currentUser)
        assertEquals(0, dailyRepo.getAllSummaries().size)
        assertEquals(0, hydrationRepo.getAllEntries().size)
    }

    @Test
    fun deleteAccountKeepsLocalDataWhenRequested() = runTest {
        val todayEpochDay = java.time.LocalDate.now().toEpochDay()
        val dailyRepo = FakeDailySummaryRepository()
        dailyRepo.saveDailySummary(
            DailyHealthSummaryEntity(
                epochDay = todayEpochDay,
                steps = 5_000L,
                restingHeartRate = 60L,
                sleepMinutes = 400L,
                activeCalories = 300L,
                exerciseMinutes = 20L,
                standHours = 6L,
                source = "Health Connect",
                updatedAtEpochMillis = 1000L
            )
        )
        val hydrationRepo = FakeHydrationRepository(initialWater = 1.0)
        val authRepo = com.pulsehealth.app.data.NoOpAuthRepository(
            com.pulsehealth.app.data.AuthState.Authenticated(
                com.pulsehealth.app.data.AuthUser(id = "user_keep", email = "keep@pulsehealth.app")
            )
        )

        val viewModel = DashboardViewModel(
            savedStateHandle = SavedStateHandle(),
            hydrationRepository = hydrationRepo,
            preferencesRepository = FakePreferencesRepository(),
            healthRepository = FakeHealthConnectRepository(),
            authRepository = authRepo,
            dailySummaryRepository = dailyRepo
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        var completedCalled = false
        viewModel.deleteAccount(alsoDeleteLocalData = false) {
            completedCalled = true
        }
        advanceUntilIdle()

        assertTrue(completedCalled)
        assertEquals(com.pulsehealth.app.data.AuthState.Unauthenticated, viewModel.uiState.value.authState)
        assertNull(viewModel.uiState.value.currentUser)
        assertEquals(1, dailyRepo.getAllSummaries().size)
    }

    @Test
    fun exportDataCsvAndClinicalSummaryProduceExpectedContent() = runTest {
        val hydrationRepo = FakeHydrationRepository()
        hydrationRepo.addWater(250)
        val viewModel = DashboardViewModel(
            savedStateHandle = SavedStateHandle(),
            hydrationRepository = hydrationRepo,
            preferencesRepository = FakePreferencesRepository(),
            healthRepository = FakeHealthConnectRepository()
        )

        val csv = viewModel.exportDataCsv()
        val report = viewModel.exportClinicalSummary()

        assertTrue(csv.contains("Date,Amount_mL"))
        assertTrue(report.contains("PULSE HEALTH WELLNESS REPORT"))
    }

    @Test
    fun weeklyWellnessDigestPopulatedInDemoMode() = runTest {
        val viewModel = DashboardViewModel(
            savedStateHandle = SavedStateHandle(),
            hydrationRepository = FakeHydrationRepository(),
            preferencesRepository = FakePreferencesRepository(),
            healthRepository = FakeHealthConnectRepository()
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        viewModel.setDemoMode(true)
        advanceUntilIdle()

        val digest = viewModel.uiState.value.weeklyWellnessDigest
        assertTrue(digest != null)
        assertEquals(88, digest?.consistencyScore)
        assertTrue(digest?.totalSteps ?: 0L > 50_000L)
    }

    @Test
    fun biometricLockDisabledByDefault_appIsUnlocked() = runTest {
        val viewModel = DashboardViewModel(
            savedStateHandle = SavedStateHandle(),
            hydrationRepository = FakeHydrationRepository(),
            preferencesRepository = FakePreferencesRepository(),
            healthRepository = FakeHealthConnectRepository()
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.biometricLockEnabled)
        assertTrue(viewModel.uiState.value.isAppUnlocked)
    }

    @Test
    fun biometricLockEnabled_lockAppLocksApp_unlockAppUnlocksApp() = runTest {
        val prefs = FakePreferencesRepository()
        val viewModel = DashboardViewModel(
            savedStateHandle = SavedStateHandle(),
            hydrationRepository = FakeHydrationRepository(),
            preferencesRepository = prefs,
            healthRepository = FakeHealthConnectRepository()
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        viewModel.setBiometricLockEnabled(true)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.biometricLockEnabled)
        assertTrue(viewModel.uiState.value.isAppUnlocked)

        // Calling lockApp when enabled locks the app
        viewModel.lockApp()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isAppUnlocked)

        // Calling unlockApp restores unlocked state
        viewModel.unlockApp()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isAppUnlocked)

        // Disabling biometric lock unlocks app automatically
        viewModel.lockApp()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isAppUnlocked)

        viewModel.setBiometricLockEnabled(false)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.biometricLockEnabled)
        assertTrue(viewModel.uiState.value.isAppUnlocked)
    }
}
