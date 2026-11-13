package com.pulsehealth.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pulsehealth.app.PulseHealthApplication
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.data.DashboardUiState
import com.pulsehealth.app.data.DataSource
import com.pulsehealth.app.data.HealthConnectRepository
import com.pulsehealth.app.data.HydrationRepository
import com.pulsehealth.app.data.PreferencesRepository
import com.pulsehealth.app.data.TimeRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val hydrationRepository: HydrationRepository,
    private val preferencesRepository: PreferencesRepository,
    val healthRepository: HealthConnectRepository
) : ViewModel() {

    private val isDemoModeFlow = MutableStateFlow(false)
    private val runtimeState = MutableStateFlow(
        DashboardUiState(healthConnectAvailable = healthRepository.isAvailable())
    )

    val uiState = combine(
        runtimeState,
        hydrationRepository.todayWaterLiters,
        preferencesRepository.hiddenCards,
        isDemoModeFlow
    ) { state, water, hidden, isDemo ->
        val effectiveMetrics = if (isDemo) {
            DashboardMetrics.demo().copy(waterLiters = if (water > 0.0) water else 1.5)
        } else {
            val source = if (state.metrics.source == DataSource.NONE && water > 0.0) {
                DataSource.LOCAL
            } else {
                state.metrics.source
            }
            state.metrics.copy(waterLiters = water, source = source)
        }

        state.copy(
            metrics = effectiveMetrics,
            hiddenCards = hidden,
            isDemoMode = isDemo
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), runtimeState.value)

    init {
        refreshPermissionsAndSync()
    }

    fun selectRange(range: TimeRange) {
        runtimeState.value = runtimeState.value.copy(selectedRange = range)
        sync()
    }

    fun addWater() = viewModelScope.launch {
        hydrationRepository.addWater()
    }

    fun toggleCard(id: String) = viewModelScope.launch {
        preferencesRepository.toggleCard(id)
    }

    fun setDemoMode(enabled: Boolean) {
        isDemoModeFlow.value = enabled
    }

    fun toggleDemoMode() {
        isDemoModeFlow.value = !isDemoModeFlow.value
    }

    fun refreshPermissionsAndSync() = viewModelScope.launch {
        val granted = runCatching { healthRepository.hasPermissions() }.getOrDefault(false)
        runtimeState.value = runtimeState.value.copy(hasHealthPermissions = granted)
        if (granted) sync()
    }

    fun sync() = viewModelScope.launch {
        val current = runtimeState.value
        if (!current.hasHealthPermissions) return@launch
        runtimeState.value = current.copy(isSyncing = true, message = null)
        runCatching { healthRepository.read(current.selectedRange, current.metrics) }
            .onSuccess { metrics ->
                runtimeState.value = runtimeState.value.copy(
                    metrics = metrics,
                    isSyncing = false,
                    lastSyncedEpochMillis = System.currentTimeMillis()
                )
            }
            .onFailure {
                runtimeState.value = runtimeState.value.copy(
                    isSyncing = false,
                    message = "Health data could not be refreshed. Your saved data is still available."
                )
            }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PulseHealthApplication
                DashboardViewModel(
                    hydrationRepository = app.container.hydrationRepository,
                    preferencesRepository = app.container.preferencesRepository,
                    healthRepository = app.container.healthConnectRepository
                )
            }
        }
    }
}
