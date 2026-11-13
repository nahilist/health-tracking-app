package com.pulsehealth.app.data

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val hiddenCards: Flow<Set<String>>
    val waterGoalLiters: Flow<Double>

    suspend fun toggleCard(id: String)
    suspend fun setWaterGoalLiters(goalLiters: Double)
}
