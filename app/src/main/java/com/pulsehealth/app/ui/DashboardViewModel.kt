package com.pulsehealth.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.data.DashboardUiState
import com.pulsehealth.app.data.HealthDataRepository
import com.pulsehealth.app.data.HealthConnectRepository
import com.pulsehealth.app.data.TimeRange
import com.pulsehealth.app.data.UserPreferencesRepository
import com.pulsehealth.app.data.local.PulseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = UserPreferencesRepository(application)
    private val healthData = HealthDataRepository(PulseDatabase.get(application).hydrationDao())
    val healthRepository = HealthConnectRepository(application)
    private val runtimeState = MutableStateFlow(
        DashboardUiState(healthConnectAvailable = healthRepository.isAvailable())
    )

    val uiState = combine(
        runtimeState,
        healthData.todayWaterLiters,
        preferences.hiddenCards
    ) { state, water, hidden ->
        state.copy(
            metrics = state.metrics.copy(waterLiters = water),
            hiddenCards = hidden
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), runtimeState.value)

    init { refreshPermissionsAndSync() }

    fun selectRange(range: TimeRange) {
        runtimeState.value = runtimeState.value.copy(selectedRange = range)
        sync()
    }

    fun addWater() = viewModelScope.launch { healthData.addWater() }

    fun toggleCard(id: String) = viewModelScope.launch { preferences.toggleCard(id) }

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
                runtimeState.value = runtimeState.value.copy(metrics = metrics, isSyncing = false)
            }
            .onFailure {
                runtimeState.value = runtimeState.value.copy(
                    isSyncing = false,
                    message = "Health data could not be refreshed. Your saved data is still available."
                )
            }
    }
}
