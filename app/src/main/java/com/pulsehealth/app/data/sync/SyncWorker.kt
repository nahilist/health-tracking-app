package com.pulsehealth.app.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pulsehealth.app.PulseHealthApplication

/**
 * Background worker that performs periodic or event-driven cloud synchronization.
 */
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? PulseHealthApplication ?: return Result.failure()
        return when (val syncResult = app.container.syncRepository.sync()) {
            is SyncResult.Success -> Result.success()
            is SyncResult.SkippedUnauthenticated -> Result.success()
            is SyncResult.Error -> {
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        }
    }
}
