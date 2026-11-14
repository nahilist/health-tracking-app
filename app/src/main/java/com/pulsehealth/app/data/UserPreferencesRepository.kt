package com.pulsehealth.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.userDataStore by preferencesDataStore(name = "pulse_user_preferences")

class UserPreferencesRepository(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PreferencesRepository {

    private object Keys {
        val hiddenCards = stringSetPreferencesKey("hidden_cards")
        val waterGoalLiters = doublePreferencesKey("water_goal_liters")
        val syncWaterToHealthConnect = booleanPreferencesKey("sync_water_to_health_connect")
        val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val remindersEnabled = booleanPreferencesKey("reminders_enabled")
        val reminderIntervalHours = intPreferencesKey("reminder_interval_hours")
        val quietHoursStartHour = intPreferencesKey("quiet_hours_start_hour")
        val quietHoursEndHour = intPreferencesKey("quiet_hours_end_hour")
    }

    override val hiddenCards: Flow<Set<String>> =
        context.userDataStore.data.map { preferences ->
            val raw = preferences[Keys.hiddenCards] ?: emptySet()
            raw.map { FavoriteCardId.normalize(it) }.toSet()
        }

    override val waterGoalLiters: Flow<Double> =
        context.userDataStore.data.map { it[Keys.waterGoalLiters] ?: 2.0 }

    override val syncWaterToHealthConnect: Flow<Boolean> =
        context.userDataStore.data.map { it[Keys.syncWaterToHealthConnect] ?: false }

    override val onboardingCompleted: Flow<Boolean> =
        context.userDataStore.data.map { it[Keys.onboardingCompleted] ?: false }

    override val remindersEnabled: Flow<Boolean> =
        context.userDataStore.data.map { it[Keys.remindersEnabled] ?: false }

    override val reminderIntervalHours: Flow<Int> =
        context.userDataStore.data.map { it[Keys.reminderIntervalHours] ?: 2 }

    override val quietHoursStartHour: Flow<Int> =
        context.userDataStore.data.map { it[Keys.quietHoursStartHour] ?: 22 }

    override val quietHoursEndHour: Flow<Int> =
        context.userDataStore.data.map { it[Keys.quietHoursEndHour] ?: 8 }

    override suspend fun toggleCard(id: String): Unit = withContext(ioDispatcher) {
        val canonical = FavoriteCardId.normalize(id)
        context.userDataStore.edit { preferences ->
            val cards = (preferences[Keys.hiddenCards] ?: emptySet())
                .map { FavoriteCardId.normalize(it) }
                .toMutableSet()
            if (!cards.add(canonical)) cards.remove(canonical)
            preferences[Keys.hiddenCards] = cards
        }
    }

    override suspend fun setWaterGoalLiters(goalLiters: Double): Unit = withContext(ioDispatcher) {
        require(goalLiters in 0.5..10.0) { "Water goal must be between 0.5 and 10.0 liters" }
        context.userDataStore.edit { preferences ->
            preferences[Keys.waterGoalLiters] = goalLiters
        }
    }

    override suspend fun setSyncWaterToHealthConnect(enabled: Boolean): Unit = withContext(ioDispatcher) {
        context.userDataStore.edit { preferences ->
            preferences[Keys.syncWaterToHealthConnect] = enabled
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean): Unit = withContext(ioDispatcher) {
        context.userDataStore.edit { preferences ->
            preferences[Keys.onboardingCompleted] = completed
        }
    }

    override suspend fun setRemindersEnabled(enabled: Boolean): Unit = withContext(ioDispatcher) {
        context.userDataStore.edit { preferences ->
            preferences[Keys.remindersEnabled] = enabled
        }
    }

    override suspend fun setReminderIntervalHours(hours: Int): Unit = withContext(ioDispatcher) {
        require(hours in 1..12) { "Reminder interval must be between 1 and 12 hours" }
        context.userDataStore.edit { preferences ->
            preferences[Keys.reminderIntervalHours] = hours
        }
    }

    override suspend fun setQuietHours(startHour: Int, endHour: Int): Unit = withContext(ioDispatcher) {
        require(startHour in 0..23 && endHour in 0..23) { "Hours must be between 0 and 23" }
        context.userDataStore.edit { preferences ->
            preferences[Keys.quietHoursStartHour] = startHour
            preferences[Keys.quietHoursEndHour] = endHour
        }
    }

    override suspend fun clearAllPreferences(): Unit = withContext(ioDispatcher) {
        context.userDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

typealias DataStorePreferencesRepository = UserPreferencesRepository
