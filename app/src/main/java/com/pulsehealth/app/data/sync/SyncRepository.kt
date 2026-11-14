package com.pulsehealth.app.data.sync

import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface governing offline outbox sync and cloud reconciliation.
 */
interface SyncRepository {
    val isSyncing: StateFlow<Boolean>
    val lastSyncedEpochMillis: StateFlow<Long?>
    val lastSyncError: StateFlow<String?>

    /**
     * Executes an immediate two-way synchronization:
     * 1. Pushes local pending records to Supabase PostgreSQL.
     * 2. Pulls remote user records and reconciles them into Room.
     */
    suspend fun sync(): SyncResult

    /**
     * Schedules periodic background synchronization using WorkManager.
     */
    fun schedulePeriodicSync()

    /**
     * Triggers a one-time immediate synchronization worker.
     */
    fun triggerImmediateSync()
}

/**
 * No-op fake implementation for unit tests and previews.
 */
class NoOpSyncRepository : SyncRepository {
    override val isSyncing: StateFlow<Boolean> = kotlinx.coroutines.flow.MutableStateFlow(false)
    override val lastSyncedEpochMillis: StateFlow<Long?> = kotlinx.coroutines.flow.MutableStateFlow(null)
    override val lastSyncError: StateFlow<String?> = kotlinx.coroutines.flow.MutableStateFlow(null)

    override suspend fun sync(): SyncResult = SyncResult.Success(0, 0)
    override fun schedulePeriodicSync() {}
    override fun triggerImmediateSync() {}
}
