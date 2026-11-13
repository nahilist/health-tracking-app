package com.pulsehealth.app.data

import kotlinx.coroutines.flow.Flow

interface HydrationRepository {
    val todayWaterLiters: Flow<Double>
    suspend fun addWater(amountMilliliters: Int = 250)
}
