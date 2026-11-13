package com.pulsehealth.app.data

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val hiddenCards: Flow<Set<String>>
    suspend fun toggleCard(id: String)
}
