package com.pulsehealth.app.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val hydrationRepository: HydrationRepository,
    private val preferencesRepository: PreferencesRepository,
    val healthRepository: HealthConnectRepository
) : ViewModel() {

    private val selectedRangeFlow: StateFlow<String> = savedStateHandle.getStateFlow(
        KEY_SELECTED_RANGE,
        TimeRange.TODAY.name
    )

    private val isDemoModeFlow: StateFlow<Boolean> = savedStateHandle.getStateFlow(
        KEY_IS_DEMO_MODE,
        false
    )

    private data class SyncState(
        val healthConnectAvailable: Boolean,
        val hasHealthPermissions: Boolean,
        val isLoading: Boolean,
        val isSyncing: Boolean,
        val errorMessage: String?,
        val metrics: DashboardMetrics,
        val lastSyncedEpochMillis: Long?
    )

    private val syncStateFlow = MutableStateFlow(
        SyncState(
            healthConnectAvailable = healthRepository.isAvailable(),
            hasHealthPermissions = false,
            isLoading = false,
            isSyncing = false,
            errorMessage = null,
            metrics = DashboardMetrics(),
            lastSyncedEpochMillis = null
        )
    )

    val uiState: StateFlow<DashboardUiState> = combine(
        syncStateFlow,
        hydrationRepository.todayWaterLiters,
        preferencesRepository.hiddenCards,
        selectedRangeFlow,
        isDemoModeFlow
    ) { sync, water, hidden, rangeName, isDemo ->
        val range = runCatching { TimeRange.valueOf(rangeName) }.getOrDefault(TimeRange.TODAY)
        val effectiveMetrics = if (isDemo) {
            DashboardMetrics.demo().copy(waterLiters = if (water > 0.0) water else 1.5)
        } else {
            val source = if (sync.metrics.source == DataSource.NONE && water > 0.0) {
                DataSource.LOCAL
            } else {
                sync.metrics.source
            }
            sync.metrics.copy(waterLiters = water, source = source)
        }

        DashboardUiState(
            metrics = effectiveMetrics,
            selectedRange = range,
            healthConnectAvailable = sync.healthConnectAvailable,
            hasHealthPermissions = sync.hasHealthPermissions,
            isLoading = sync.isLoading,
            isSyncing = sync.isSyncing,
            errorMessage = sync.errorMessage,
            hiddenCards = hidden,
            isDemoMode = isDemo,
            lastSyncedEpochMillis = sync.lastSyncedEpochMillis
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DashboardUiState(
            healthConnectAvailable = healthRepository.isAvailable(),
            selectedRange = runCatching { TimeRange.valueOf(selectedRangeFlow.value) }.getOrDefault(TimeRange.TODAY),
            isDemoMode = isDemoModeFlow.value
        )
    )

    init {
        refreshPermissionsAndSync()
    }

    fun selectRange(range: TimeRange) {
        savedStateHandle[KEY_SELECTED_RANGE] = range.name
        sync()
    }

    fun addWater() = viewModelScope.launch {
        hydrationRepository.addWater()
    }

    fun toggleCard(id: String) = viewModelScope.launch {
        preferencesRepository.toggleCard(id)
    }

    fun setDemoMode(enabled: Boolean) {
        savedStateHandle[KEY_IS_DEMO_MODE] = enabled
    }

    fun toggleDemoMode() {
        savedStateHandle[KEY_IS_DEMO_MODE] = !isDemoModeFlow.value
    }

    fun clearError() {
        syncStateFlow.update { it.copy(errorMessage = null) }
    }

    fun refreshPermissionsAndSync() = viewModelScope.launch {
        syncStateFlow.update { it.copy(isLoading = true) }
        val granted = runCatching { healthRepository.hasPermissions() }.getOrDefault(false)
        syncStateFlow.update {
            it.copy(
                hasHealthPermissions = granted,
                isLoading = false
            )
        }
        if (granted) sync()
    }

    fun sync() = viewModelScope.launch {
        val current = syncStateFlow.value
        if (!current.hasHealthPermissions) return@launch
        syncStateFlow.update { it.copy(isSyncing = true, errorMessage = null) }
        val range = runCatching { TimeRange.valueOf(selectedRangeFlow.value) }.getOrDefault(TimeRange.TODAY)
        runCatching { healthRepository.read(range, current.metrics) }
            .onSuccess { metrics ->
                syncStateFlow.update {
                    it.copy(
                        metrics = metrics,
                        isSyncing = false,
                        lastSyncedEpochMillis = System.currentTimeMillis()
                    )
                }
            }
            .onFailure {
                syncStateFlow.update {
                    it.copy(
                        isSyncing = false,
                        errorMessage = "Health data could not be refreshed. Your saved data is still available."
                    )
                }
            }
    }

    companion object {
        const val KEY_SELECTED_RANGE = "selected_range"
        const val KEY_IS_DEMO_MODE = "is_demo_mode"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PulseHealthApplication
                val savedStateHandle = createSavedStateHandle()
                DashboardViewModel(
                    savedStateHandle = savedStateHandle,
                    hydrationRepository = app.container.hydrationRepository,
                    preferencesRepository = app.container.preferencesRepository,
                    healthRepository = app.container.healthConnectRepository
                )
            }
        }
    }
}
