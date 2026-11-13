package com.pulsehealth.app.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
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
    }

    override val hiddenCards: Flow<Set<String>> =
        context.userDataStore.data.map { it[Keys.hiddenCards] ?: emptySet() }

    override val waterGoalLiters: Flow<Double> =
        context.userDataStore.data.map { it[Keys.waterGoalLiters] ?: 2.0 }

    override suspend fun toggleCard(id: String): Unit = withContext(ioDispatcher) {
        context.userDataStore.edit { preferences ->
            val cards = (preferences[Keys.hiddenCards] ?: emptySet()).toMutableSet()
            if (!cards.add(id)) cards.remove(id)
            preferences[Keys.hiddenCards] = cards
        }
    }

    override suspend fun setWaterGoalLiters(goalLiters: Double): Unit = withContext(ioDispatcher) {
        require(goalLiters in 0.5..10.0) { "Water goal must be between 0.5 and 10.0 liters" }
        context.userDataStore.edit { preferences ->
            preferences[Keys.waterGoalLiters] = goalLiters
        }
    }
}

typealias DataStorePreferencesRepository = UserPreferencesRepository
