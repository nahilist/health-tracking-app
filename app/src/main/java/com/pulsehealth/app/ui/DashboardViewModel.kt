package com.pulsehealth.app.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pulsehealth.app.PulseHealthApplication
import com.pulsehealth.app.data.AuthRepository
import com.pulsehealth.app.data.AuthResult
import com.pulsehealth.app.data.AuthState
import com.pulsehealth.app.data.AuthUser
import com.pulsehealth.app.data.DailySummaryRepository
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.data.DashboardUiState
import com.pulsehealth.app.data.DataSource
import com.pulsehealth.app.data.ExportedHealthPackage
import com.pulsehealth.app.data.HealthConnectAvailability
import com.pulsehealth.app.data.HealthConnectRepository
import com.pulsehealth.app.data.HydrationRepository
import com.pulsehealth.app.data.HydrationTrends
import com.pulsehealth.app.data.NoOpAuthRepository
import com.pulsehealth.app.data.NoOpDailySummaryRepository
import com.pulsehealth.app.data.NoOpSharingRepository
import com.pulsehealth.app.data.PreferencesRepository
import com.pulsehealth.app.data.ShareDuration
import com.pulsehealth.app.data.ShareRelationship
import com.pulsehealth.app.data.SharingRepository
import com.pulsehealth.app.data.TimeRange
import com.pulsehealth.app.data.TrustedContact
import com.pulsehealth.app.data.WeeklyDigestCalculator
import com.pulsehealth.app.data.WeeklyWellnessDigest
import com.pulsehealth.app.data.local.DailyHealthSummaryEntity
import com.pulsehealth.app.data.local.HydrationEntryEntity
import com.pulsehealth.app.data.toDailyHealthSummary
import com.pulsehealth.app.data.toDashboardMetrics
import com.pulsehealth.app.data.sync.NoOpSyncRepository
import com.pulsehealth.app.data.sync.SyncRepository
import com.pulsehealth.app.data.sync.SyncResult
import com.pulsehealth.app.notifications.NoOpReminderScheduler
import com.pulsehealth.app.notifications.NotificationHelper
import com.pulsehealth.app.notifications.ReminderScheduler
import com.pulsehealth.app.notifications.ReminderSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val hydrationRepository: HydrationRepository,
    private val preferencesRepository: PreferencesRepository,
    val healthRepository: HealthConnectRepository,
    val authRepository: AuthRepository = NoOpAuthRepository(),
    val syncRepository: SyncRepository = NoOpSyncRepository(),
    val reminderScheduler: ReminderScheduler = NoOpReminderScheduler(),
    val sharingRepository: SharingRepository = NoOpSharingRepository(),
    val dailySummaryRepository: DailySummaryRepository = NoOpDailySummaryRepository()
) : ViewModel() {

    private val selectedRangeFlow: StateFlow<String> = savedStateHandle.getStateFlow(
        KEY_SELECTED_RANGE,
        TimeRange.TODAY.name
    )

    private val isDemoModeFlow: StateFlow<Boolean> = savedStateHandle.getStateFlow(
        KEY_IS_DEMO_MODE,
        false
    )

    val todayHydrationEntries: StateFlow<List<HydrationEntryEntity>> =
        hydrationRepository.observeTodayEntries()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val weeklyWaterLiters: StateFlow<Double> =
        hydrationRepository.weeklyWaterLiters
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val pastDailySummaries: StateFlow<List<DailyHealthSummaryEntity>> =
        dailySummaryRepository.observeSummaryRange(
            startEpochDay = java.time.LocalDate.now().minusDays(29).toEpochDay(),
            endEpochDay = java.time.LocalDate.now().toEpochDay()
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isCloudSyncing: StateFlow<Boolean> = syncRepository.isSyncing
    val lastCloudSyncedEpochMillis: StateFlow<Long?> = syncRepository.lastSyncedEpochMillis
    val lastCloudSyncError: StateFlow<String?> = syncRepository.lastSyncError

    private data class SyncState(
        val healthConnectAvailability: HealthConnectAvailability,
        val hasHealthPermissions: Boolean,
        val hasWriteHydrationPermission: Boolean,
        val isLoading: Boolean,
        val isSyncing: Boolean,
        val errorMessage: String?,
        val metrics: DashboardMetrics,
        val lastSyncedEpochMillis: Long?
    )

    private val syncStateFlow = MutableStateFlow(
        SyncState(
            healthConnectAvailability = healthRepository.getAvailability(),
            hasHealthPermissions = false,
            hasWriteHydrationPermission = false,
            isLoading = false,
            isSyncing = false,
            errorMessage = null,
            metrics = DashboardMetrics(),
            lastSyncedEpochMillis = null
        )
    )

    val authState: StateFlow<AuthState> = authRepository.authState
    val currentUser: StateFlow<AuthUser?> = authRepository.currentUser

    private data class AccountAndCloudState(
        val authState: AuthState = AuthState.Loading,
        val currentUser: AuthUser? = null,
        val isCloudSyncing: Boolean = false,
        val lastCloudSyncedEpochMillis: Long? = null,
        val cloudSyncError: String? = null
    )

    private data class GoalsState(
        val waterGoalLiters: Double = 2.0,
        val stepGoal: Long = 10_000L,
        val calorieGoal: Long = 500L,
        val sleepGoalMinutes: Long = 480L
    )

    private data class PreferencesState(
        val hiddenCards: Set<String>,
        val goals: GoalsState = GoalsState(),
        val syncWaterToHealthConnect: Boolean,
        val onboardingCompleted: Boolean,
        val isDemoMode: Boolean,
        val authState: AuthState = AuthState.Loading,
        val currentUser: AuthUser? = null,
        val isCloudSyncing: Boolean = false,
        val lastCloudSyncedEpochMillis: Long? = null,
        val cloudSyncError: String? = null,
        val reminderSettings: ReminderSettings = ReminderSettings()
    )

    private val goalsFlow = combine(
        preferencesRepository.waterGoalLiters,
        preferencesRepository.stepGoal,
        preferencesRepository.calorieGoal,
        preferencesRepository.sleepGoalMinutes
    ) { water, steps, calories, sleep ->
        GoalsState(water, steps, calories, sleep)
    }

    private val appFlagsFlow = combine(
        preferencesRepository.onboardingCompleted,
        isDemoModeFlow
    ) { onboarding, isDemo -> onboarding to isDemo }

    private val reminderSettingsFlow = combine(
        preferencesRepository.remindersEnabled,
        preferencesRepository.reminderIntervalHours,
        preferencesRepository.quietHoursStartHour,
        preferencesRepository.quietHoursEndHour
    ) { enabled, interval, quietStart, quietEnd ->
        ReminderSettings(enabled, interval, quietStart, quietEnd)
    }

    private val appAndReminderFlow = combine(
        appFlagsFlow,
        reminderSettingsFlow
    ) { (onboarding, isDemo), reminders ->
        Triple(onboarding, isDemo, reminders)
    }

    private val accountAndCloudFlow = combine(
        authRepository.authState,
        authRepository.currentUser,
        syncRepository.isSyncing,
        syncRepository.lastSyncedEpochMillis,
        syncRepository.lastSyncError
    ) { authState, user, cloudSyncing, lastCloudSync, cloudError ->
        AccountAndCloudState(authState, user, cloudSyncing, lastCloudSync, cloudError)
    }

    private val preferencesFlow = combine(
        preferencesRepository.hiddenCards,
        goalsFlow,
        preferencesRepository.syncWaterToHealthConnect,
        appAndReminderFlow,
        accountAndCloudFlow
    ) { hidden, goals, syncWater, (onboarding, isDemo, reminders), account ->
        PreferencesState(
            hiddenCards = hidden,
            goals = goals,
            syncWaterToHealthConnect = syncWater,
            onboardingCompleted = onboarding,
            isDemoMode = isDemo,
            authState = account.authState,
            currentUser = account.currentUser,
            isCloudSyncing = account.isCloudSyncing,
            lastCloudSyncedEpochMillis = account.lastCloudSyncedEpochMillis,
            cloudSyncError = account.cloudSyncError,
            reminderSettings = reminders
        )
    }

    private val hydrationFlow = combine(
        hydrationRepository.todayWaterLiters,
        hydrationRepository.observePastDaysEntries(30)
    ) { todayWater, pastEntries ->
        todayWater to pastEntries
    }

    private val healthAndSummaryFlow = combine(
        syncStateFlow,
        pastDailySummaries
    ) { sync, summaries ->
        sync to summaries
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        healthAndSummaryFlow,
        hydrationFlow,
        selectedRangeFlow,
        preferencesFlow,
        sharingRepository.contacts
    ) { (sync, pastSummaries), (water, pastEntries), rangeName, pref, contacts ->
        val range = runCatching { TimeRange.valueOf(rangeName) }.getOrDefault(TimeRange.TODAY)
        val (streakInfo, weeklyProgress) = HydrationTrends.calculateStreaksAndProgress(
            entries = pastEntries,
            goalLiters = pref.goals.waterGoalLiters
        )
        val weeklyDigest = if (pref.isDemoMode) {
            WeeklyDigestCalculator.calculateDemo(
                stepGoal = pref.goals.stepGoal,
                waterGoalLiters = pref.goals.waterGoalLiters
            )
        } else {
            WeeklyDigestCalculator.calculate(
                summaries = pastSummaries,
                weeklyHydration = weeklyProgress,
                stepGoal = pref.goals.stepGoal,
                calorieGoal = pref.goals.calorieGoal,
                sleepGoalMinutes = pref.goals.sleepGoalMinutes,
                waterGoalLiters = pref.goals.waterGoalLiters
            )
        }
        val effectiveMetrics = if (pref.isDemoMode) {
            DashboardMetrics.demo().copy(
                waterLiters = if (water > 0.0) water else 1.5,
                waterGoalLiters = pref.goals.waterGoalLiters,
                stepGoal = pref.goals.stepGoal,
                calorieGoal = pref.goals.calorieGoal
            )
        } else {
            val source = if (sync.metrics.source == DataSource.NONE && water > 0.0) {
                DataSource.LOCAL
            } else {
                sync.metrics.source
            }
            sync.metrics.copy(
                waterLiters = water,
                waterGoalLiters = pref.goals.waterGoalLiters,
                stepGoal = pref.goals.stepGoal,
                calorieGoal = pref.goals.calorieGoal,
                source = source
            )
        }

        DashboardUiState(
            metrics = effectiveMetrics,
            selectedRange = range,
            healthConnectAvailability = sync.healthConnectAvailability,
            hasHealthPermissions = sync.hasHealthPermissions,
            hasWriteHydrationPermission = sync.hasWriteHydrationPermission,
            syncWaterToHealthConnect = pref.syncWaterToHealthConnect,
            onboardingCompleted = pref.onboardingCompleted,
            isLoading = sync.isLoading,
            isSyncing = sync.isSyncing,
            errorMessage = sync.errorMessage,
            hiddenCards = pref.hiddenCards,
            isDemoMode = pref.isDemoMode,
            lastSyncedEpochMillis = sync.lastSyncedEpochMillis,
            authState = pref.authState,
            currentUser = pref.currentUser,
            isCloudSyncing = pref.isCloudSyncing,
            lastCloudSyncedEpochMillis = pref.lastCloudSyncedEpochMillis,
            cloudSyncError = pref.cloudSyncError,
            remindersEnabled = pref.reminderSettings.enabled,
            reminderIntervalHours = pref.reminderSettings.intervalHours,
            quietHoursStartHour = pref.reminderSettings.quietHoursStartHour,
            quietHoursEndHour = pref.reminderSettings.quietHoursEndHour,
            streakInfo = streakInfo,
            weeklyHydrationProgress = weeklyProgress,
            weeklyWellnessDigest = weeklyDigest,
            trustedContacts = contacts
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DashboardUiState(
            healthConnectAvailability = healthRepository.getAvailability(),
            selectedRange = runCatching { TimeRange.valueOf(selectedRangeFlow.value) }.getOrDefault(TimeRange.TODAY),
            isDemoMode = isDemoModeFlow.value,
            authState = authRepository.authState.value,
            currentUser = authRepository.currentUser.value,
            isCloudSyncing = syncRepository.isSyncing.value,
            lastCloudSyncedEpochMillis = syncRepository.lastSyncedEpochMillis.value,
            cloudSyncError = syncRepository.lastSyncError.value,
            weeklyWellnessDigest = if (isDemoModeFlow.value) WeeklyDigestCalculator.calculateDemo() else null
        )
    )

    init {
        refreshPermissionsAndSync()
    }

    fun selectRange(range: TimeRange) {
        savedStateHandle[KEY_SELECTED_RANGE] = range.name
        sync()
    }

    fun addWater(amountMilliliters: Int = 250) = viewModelScope.launch {
        hydrationRepository.addWater(amountMilliliters)
        syncRepository.triggerImmediateSync()
        if (uiState.value.syncWaterToHealthConnect && syncStateFlow.value.hasWriteHydrationPermission) {
            runCatching {
                healthRepository.writeHydration(amountMilliliters, System.currentTimeMillis())
            }
        }
    }

    fun editWater(id: String, amountMilliliters: Int) = viewModelScope.launch {
        hydrationRepository.updateWater(id, amountMilliliters)
        syncRepository.triggerImmediateSync()
    }

    fun deleteWater(id: String) = viewModelScope.launch {
        hydrationRepository.deleteWater(id)
        syncRepository.triggerImmediateSync()
    }

    fun setWaterGoal(goalLiters: Double) = viewModelScope.launch {
        preferencesRepository.setWaterGoalLiters(goalLiters)
    }

    fun setStepGoal(steps: Long) = viewModelScope.launch {
        preferencesRepository.setStepGoal(steps)
    }

    fun setCalorieGoal(calories: Long) = viewModelScope.launch {
        preferencesRepository.setCalorieGoal(calories)
    }

    fun setSleepGoalMinutes(minutes: Long) = viewModelScope.launch {
        preferencesRepository.setSleepGoalMinutes(minutes)
    }

    fun setOnboardingCompleted(completed: Boolean) = viewModelScope.launch {
        preferencesRepository.setOnboardingCompleted(completed)
    }

    suspend fun exportDataJson(): String {
        return hydrationRepository.exportDataJson()
    }

    suspend fun exportDataCsv(): String {
        return hydrationRepository.exportDataCsv()
    }

    suspend fun exportClinicalSummary(): String {
        return hydrationRepository.exportClinicalSummary()
    }

    fun deleteAccount(alsoDeleteLocalData: Boolean = true, onCompleted: (Result<Unit>) -> Unit = {}) = viewModelScope.launch {
        val result = authRepository.deleteAccount()
        if (result.isSuccess) {
            syncRepository.cancelPeriodicSync()
            if (alsoDeleteLocalData) {
                hydrationRepository.clearAllData()
                dailySummaryRepository.deleteAllSummaries()
                preferencesRepository.clearAllPreferences()
                syncStateFlow.update {
                    it.copy(
                        metrics = DashboardMetrics(),
                        lastSyncedEpochMillis = null,
                        errorMessage = null
                    )
                }
            }
        }
        onCompleted(result)
    }

    fun deleteAllLocalData(onDeleted: () -> Unit = {}) = viewModelScope.launch {
        hydrationRepository.clearAllData()
        dailySummaryRepository.deleteAllSummaries()
        preferencesRepository.clearAllPreferences()
        syncStateFlow.update {
            it.copy(
                metrics = DashboardMetrics(),
                lastSyncedEpochMillis = null,
                errorMessage = null
            )
        }
        onDeleted()
    }

    fun setSyncWaterToHealthConnect(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setSyncWaterToHealthConnect(enabled)
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
        val availability = healthRepository.getAvailability()
        val readGranted = runCatching { healthRepository.hasPermissions() }.getOrDefault(false)
        val writeGranted = runCatching { healthRepository.hasWriteHydrationPermission() }.getOrDefault(false)

        // Instant local cache load from Room for immediate UI availability
        val todayEpochDay = java.time.LocalDate.now().toEpochDay()
        val cached = dailySummaryRepository.getSummary(todayEpochDay)
        if (cached != null && syncStateFlow.value.metrics.steps == null) {
            syncStateFlow.update {
                it.copy(
                    metrics = cached.toDashboardMetrics(
                        waterLiters = it.metrics.waterLiters,
                        waterGoalLiters = it.metrics.waterGoalLiters
                    ),
                    lastSyncedEpochMillis = cached.updatedAtEpochMillis
                )
            }
        }

        syncStateFlow.update {
            it.copy(
                healthConnectAvailability = availability,
                hasHealthPermissions = readGranted,
                hasWriteHydrationPermission = writeGranted,
                isLoading = false
            )
        }
        if (readGranted) sync()
    }

    fun sync() = viewModelScope.launch {
        val current = syncStateFlow.value
        val todayEpochDay = java.time.LocalDate.now().toEpochDay()

        // 1. Instant cache load if currently empty
        val cached = dailySummaryRepository.getSummary(todayEpochDay)
        if (cached != null && current.metrics.steps == null) {
            syncStateFlow.update {
                it.copy(
                    metrics = cached.toDashboardMetrics(
                        waterLiters = current.metrics.waterLiters,
                        waterGoalLiters = current.metrics.waterGoalLiters
                    ),
                    lastSyncedEpochMillis = cached.updatedAtEpochMillis
                )
            }
        }

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

                // 2. Persist normalized daily summary rollup into Room cache
                if (metrics.steps != null || metrics.heartRate != null || metrics.sleepMinutes != null) {
                    dailySummaryRepository.saveDailySummary(
                        metrics.toDailyHealthSummary(
                            epochDay = todayEpochDay,
                            sourceLabel = metrics.source.label
                        )
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

    fun signInWithEmail(email: String, password: String, onResult: (AuthResult) -> Unit = {}) = viewModelScope.launch {
        val result = authRepository.signInWithEmail(email, password)
        if (result is AuthResult.Success) {
            syncRepository.schedulePeriodicSync()
            syncRepository.sync()
        }
        onResult(result)
    }

    fun signUpWithEmail(email: String, password: String, onResult: (AuthResult) -> Unit = {}) = viewModelScope.launch {
        val result = authRepository.signUpWithEmail(email, password)
        if (result is AuthResult.Success) {
            syncRepository.schedulePeriodicSync()
            syncRepository.sync()
        }
        onResult(result)
    }

    fun syncCloud(onResult: (SyncResult) -> Unit = {}) = viewModelScope.launch {
        val result = syncRepository.sync()
        onResult(result)
    }

    fun signInAsGuest(onResult: (AuthResult) -> Unit = {}) = viewModelScope.launch {
        val result = authRepository.signInAsGuest()
        onResult(result)
    }

    fun signOut(onComplete: () -> Unit = {}) = viewModelScope.launch {
        authRepository.signOut()
        onComplete()
    }

    fun setRemindersEnabled(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setRemindersEnabled(enabled)
        if (enabled) {
            val interval = preferencesRepository.reminderIntervalHours.first()
            reminderScheduler.scheduleReminders(interval)
        } else {
            reminderScheduler.cancelReminders()
        }
    }

    fun setReminderIntervalHours(hours: Int) = viewModelScope.launch {
        preferencesRepository.setReminderIntervalHours(hours)
        if (uiState.value.remindersEnabled) {
            reminderScheduler.scheduleReminders(hours)
        }
    }

    fun setQuietHours(startHour: Int, endHour: Int) = viewModelScope.launch {
        preferencesRepository.setQuietHours(startHour, endHour)
    }

    fun sendTestReminder(context: Context) = viewModelScope.launch {
        val todayWater = hydrationRepository.todayWaterLiters.first()
        val goal = preferencesRepository.waterGoalLiters.first()
        NotificationHelper.showHydrationReminder(context, todayWater, goal)
    }

    fun addTrustedContact(
        name: String,
        relationship: ShareRelationship,
        contactIdentifier: String,
        sharedMetrics: Set<String>,
        duration: ShareDuration
    ) = viewModelScope.launch {
        val contact = TrustedContact.create(
            name = name,
            relationship = relationship,
            contactIdentifier = contactIdentifier,
            sharedMetrics = sharedMetrics,
            duration = duration
        )
        sharingRepository.addContact(contact)
    }

    fun updateContactMetrics(contactId: String, sharedMetrics: Set<String>) = viewModelScope.launch {
        sharingRepository.updateContactMetrics(contactId, sharedMetrics)
    }

    fun revokeContactSharing(contactId: String) = viewModelScope.launch {
        sharingRepository.revokeContact(contactId)
    }

    fun deleteTrustedContact(contactId: String) = viewModelScope.launch {
        sharingRepository.deleteContact(contactId)
    }

    suspend fun createExportHealthPackage(
        recipientName: String?,
        selectedMetrics: Set<String>,
        range: TimeRange = uiState.value.selectedRange
    ): ExportedHealthPackage {
        return sharingRepository.createExportPackage(
            recipientName = recipientName,
            selectedMetrics = selectedMetrics,
            range = range,
            dashboardMetrics = uiState.value.metrics
        )
    }

    fun formatShareText(pkg: ExportedHealthPackage): String {
        return sharingRepository.formatShareText(pkg)
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
                    healthRepository = app.container.healthConnectRepository,
                    authRepository = app.container.authRepository,
                    syncRepository = app.container.syncRepository,
                    reminderScheduler = app.container.reminderScheduler,
                    sharingRepository = app.container.sharingRepository,
                    dailySummaryRepository = app.container.dailySummaryRepository
                )
            }
        }
    }
}
