package com.pulsehealth.app

import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.SavedStateHandle
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.data.DataSource
import com.pulsehealth.app.data.HealthConnectRepository
import com.pulsehealth.app.data.HydrationRepository
import com.pulsehealth.app.data.PreferencesRepository
import com.pulsehealth.app.data.TimeRange
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

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeHydrationRepository(initialWater: Double = 0.0) : HydrationRepository {
        private val _water = MutableStateFlow(initialWater)
        override val todayWaterLiters: Flow<Double> = _water.asStateFlow()

        override suspend fun addWater(amountMilliliters: Int) {
            _water.value += amountMilliliters / 1000.0
        }
    }

    private class FakePreferencesRepository(initialHidden: Set<String> = emptySet()) : PreferencesRepository {
        private val _hidden = MutableStateFlow(initialHidden)
        override val hiddenCards: Flow<Set<String>> = _hidden.asStateFlow()

        override suspend fun toggleCard(id: String) {
            val current = _hidden.value.toMutableSet()
            if (!current.add(id)) current.remove(id)
            _hidden.value = current
        }
    }

    private class FakeHealthConnectRepository(
        var available: Boolean = true,
        var hasPermissions: Boolean = false,
        var shouldThrowOnRead: Boolean = false
    ) : HealthConnectRepository {
        override val permissions: Set<String> = emptySet()
        override fun isAvailable(): Boolean = available
        override fun permissionContract(): ActivityResultContract<Set<String>, Set<String>> {
            throw UnsupportedOperationException("Not needed in unit test")
        }
        override suspend fun hasPermissions(): Boolean = hasPermissions
        override suspend fun read(range: TimeRange, current: DashboardMetrics): DashboardMetrics {
            if (shouldThrowOnRead) throw IllegalStateException("Health Connect disconnected")
            return current.copy(steps = 5_000, source = DataSource.HEALTH_CONNECT)
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
        val prefRepo = FakePreferencesRepository(initialHidden = setOf("Sleep"))
        val healthRepo = FakeHealthConnectRepository()

        val viewModel = DashboardViewModel(SavedStateHandle(), hydrationRepo, prefRepo, healthRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1.0, state.metrics.waterLiters, 0.0)
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

        viewModel.addWater()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0.25, state.metrics.waterLiters, 0.001)
        assertEquals(DataSource.LOCAL, state.metrics.source)
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
}
