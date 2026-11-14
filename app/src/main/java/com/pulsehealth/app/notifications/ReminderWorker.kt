package com.pulsehealth.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pulsehealth.app.PulseHealthApplication
import kotlinx.coroutines.flow.first
import java.time.LocalTime

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? PulseHealthApplication ?: return Result.success()
        val container = app.container

        val enabled = container.preferencesRepository.remindersEnabled.first()
        if (!enabled) {
            return Result.success()
        }

        val quietStart = container.preferencesRepository.quietHoursStartHour.first()
        val quietEnd = container.preferencesRepository.quietHoursEndHour.first()
        val settings = ReminderSettings(
            enabled = true,
            quietHoursStartHour = quietStart,
            quietHoursEndHour = quietEnd
        )

        val currentHour = LocalTime.now().hour
        if (settings.isQuietTime(currentHour)) {
            return Result.success()
        }

        val todayWater = container.hydrationRepository.todayWaterLiters.first()
        val goalWater = container.preferencesRepository.waterGoalLiters.first()

        NotificationHelper.showHydrationReminder(applicationContext, todayWater, goalWater)
        return Result.success()
    }
}
