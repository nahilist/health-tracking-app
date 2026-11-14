package com.pulsehealth.app.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

interface ReminderScheduler {
    fun scheduleReminders(intervalHours: Int)
    fun cancelReminders()
}

class WorkManagerReminderScheduler(
    private val context: Context
) : ReminderScheduler {

    override fun scheduleReminders(intervalHours: Int) {
        runCatching {
            val hours = intervalHours.coerceIn(1, 12).toLong()
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(hours, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_REMINDERS,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }

    override fun cancelReminders() {
        runCatching {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_REMINDERS)
        }
    }

    companion object {
        const val WORK_NAME_REMINDERS = "pulse_hydration_reminders_work"
    }
}

class NoOpReminderScheduler : ReminderScheduler {
    var scheduledInterval: Int? = null
    var isCancelled: Boolean = false

    override fun scheduleReminders(intervalHours: Int) {
        scheduledInterval = intervalHours
        isCancelled = false
    }

    override fun cancelReminders() {
        isCancelled = true
        scheduledInterval = null
    }
}
