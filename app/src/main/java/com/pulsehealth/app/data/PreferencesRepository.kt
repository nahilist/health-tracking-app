package com.pulsehealth.app.data

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val hiddenCards: Flow<Set<String>>
    val waterGoalLiters: Flow<Double>
    val syncWaterToHealthConnect: Flow<Boolean>
    val onboardingCompleted: Flow<Boolean>
    val remindersEnabled: Flow<Boolean>
    val reminderIntervalHours: Flow<Int>
    val quietHoursStartHour: Flow<Int>
    val quietHoursEndHour: Flow<Int>

    suspend fun toggleCard(id: String)
    suspend fun setWaterGoalLiters(goalLiters: Double)
    suspend fun setSyncWaterToHealthConnect(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setRemindersEnabled(enabled: Boolean)
    suspend fun setReminderIntervalHours(hours: Int)
    suspend fun setQuietHours(startHour: Int, endHour: Int)
    suspend fun clearAllPreferences()
}
