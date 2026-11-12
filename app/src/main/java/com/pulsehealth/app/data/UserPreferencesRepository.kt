package com.pulsehealth.app.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userDataStore by preferencesDataStore(name = "pulse_user_preferences")

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val water = doublePreferencesKey("water_liters")
        val hiddenCards = stringSetPreferencesKey("hidden_cards")
    }

    val waterLiters: Flow<Double> = context.userDataStore.data.map { it[Keys.water] ?: 1.5 }
    val hiddenCards: Flow<Set<String>> = context.userDataStore.data.map { it[Keys.hiddenCards] ?: emptySet() }

    suspend fun addWater(liters: Double) {
        context.userDataStore.edit { preferences ->
            preferences[Keys.water] = ((preferences[Keys.water] ?: 1.5) + liters).coerceAtMost(5.0)
        }
    }

    suspend fun toggleCard(id: String) {
        context.userDataStore.edit { preferences ->
            val cards = (preferences[Keys.hiddenCards] ?: emptySet()).toMutableSet()
            if (!cards.add(id)) cards.remove(id)
            preferences[Keys.hiddenCards] = cards
        }
    }
}
