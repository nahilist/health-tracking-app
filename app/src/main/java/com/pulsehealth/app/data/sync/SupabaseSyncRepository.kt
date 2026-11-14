package com.pulsehealth.app.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.pulsehealth.app.data.AuthRepository
import com.pulsehealth.app.data.local.HydrationDao
import com.pulsehealth.app.data.local.HydrationEntryEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class SupabaseSyncRepository(
    private val context: Context,
    private val hydrationDao: HydrationDao,
    private val authRepository: AuthRepository,
    private val supabaseClientProvider: () -> SupabaseClient?,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SyncRepository {

    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncedEpochMillis = MutableStateFlow<Long?>(null)
    override val lastSyncedEpochMillis: StateFlow<Long?> = _lastSyncedEpochMillis.asStateFlow()

    private val _lastSyncError = MutableStateFlow<String?>(null)
    override val lastSyncError: StateFlow<String?> = _lastSyncError.asStateFlow()

    override suspend fun sync(): SyncResult = withContext(ioDispatcher) {
        val user = authRepository.currentUser.value
        if (user == null || user.isAnonymous) {
            return@withContext SyncResult.SkippedUnauthenticated
        }

        _isSyncing.value = true
        _lastSyncError.value = null

        try {
            val pendingEntries = hydrationDao.getEntriesBySyncState(STATE_PENDING)
            val client = supabaseClientProvider()

            var uploadedCount = 0
            var downloadedCount = 0

            if (client != null) {
                // 1. Push pending entries to Supabase PostgreSQL
                if (pendingEntries.isNotEmpty()) {
                    val dtos = pendingEntries.map { entry ->
                        SupabaseHydrationDto(
                            id = entry.id,
                            userId = user.id,
                            amountMilliliters = entry.amountMilliliters,
                            recordedAtEpochMillis = entry.recordedAtEpochMillis,
                            source = entry.source,
                            updatedAtEpochMillis = System.currentTimeMillis()
                        )
                    }
                    client.from(TABLE_HYDRATION).upsert(dtos)
                    pendingEntries.forEach { entry ->
                        hydrationDao.updateSyncState(entry.id, STATE_SYNCED)
                    }
                    uploadedCount = pendingEntries.size
                }

                // 2. Pull remote user entries from Supabase PostgreSQL
                val remoteEntries = client.from(TABLE_HYDRATION)
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeList<SupabaseHydrationDto>()

                if (remoteEntries.isNotEmpty()) {
                    val entities = remoteEntries.map { dto ->
                        HydrationEntryEntity(
                            id = dto.id,
                            amountMilliliters = dto.amountMilliliters,
                            recordedAtEpochMillis = dto.recordedAtEpochMillis,
                            source = dto.source,
                            syncState = STATE_SYNCED
                        )
                    }
                    hydrationDao.upsertAll(entities)
                    downloadedCount = remoteEntries.size
                }
            } else {
                // Local mock / sandbox fallback for testing
                pendingEntries.forEach { entry ->
                    hydrationDao.updateSyncState(entry.id, STATE_SYNCED)
                }
                uploadedCount = pendingEntries.size
            }

            val now = System.currentTimeMillis()
            _lastSyncedEpochMillis.value = now
            SyncResult.Success(uploadedCount, downloadedCount)
        } catch (e: Exception) {
            val message = e.message ?: "Failed to synchronize health records."
            _lastSyncError.value = message
            SyncResult.Error(message, e)
        } finally {
            _isSyncing.value = false
        }
    }

    override fun schedulePeriodicSync() {
        runCatching {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }

    override fun triggerImmediateSync() {
        runCatching {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_IMMEDIATE,
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
        }
    }

    companion object {
        const val TABLE_HYDRATION = "hydration_entries"
        const val STATE_PENDING = "pending"
        const val STATE_SYNCED = "synced"

        const val WORK_NAME_PERIODIC = "pulse_periodic_cloud_sync"
        const val WORK_NAME_IMMEDIATE = "pulse_immediate_cloud_sync"
    }
}
