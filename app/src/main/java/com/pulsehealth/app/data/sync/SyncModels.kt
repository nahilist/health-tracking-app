package com.pulsehealth.app.data.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object matching the Supabase PostgreSQL `hydration_entries` table.
 */
@Serializable
data class SupabaseHydrationDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("amount_milliliters") val amountMilliliters: Int,
    @SerialName("recorded_at_epoch_millis") val recordedAtEpochMillis: Long,
    val source: String = "manual",
    @SerialName("updated_at_epoch_millis") val updatedAtEpochMillis: Long = System.currentTimeMillis()
)

/**
 * Outcome of a sync cycle.
 */
sealed interface SyncResult {
    data class Success(val uploadedCount: Int, val downloadedCount: Int) : SyncResult
    data object SkippedUnauthenticated : SyncResult
    data class Error(val message: String, val cause: Throwable? = null) : SyncResult
}
