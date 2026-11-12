package com.pulsehealth.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userDataStore by preferencesDataStore(name = "pulse_user_preferences")

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val hiddenCards = stringSetPreferencesKey("hidden_cards")
    }

    val hiddenCards: Flow<Set<String>> = context.userDataStore.data.map { it[Keys.hiddenCards] ?: emptySet() }

    suspend fun toggleCard(id: String) {
        context.userDataStore.edit { preferences ->
            val cards = (preferences[Keys.hiddenCards] ?: emptySet()).toMutableSet()
            if (!cards.add(id)) cards.remove(id)
            preferences[Keys.hiddenCards] = cards
        }
    }
}
