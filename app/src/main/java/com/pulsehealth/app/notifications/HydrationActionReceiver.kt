package com.pulsehealth.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pulsehealth.app.PulseHealthApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HydrationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationHelper.ACTION_QUICK_ADD_WATER) return

        val amountMl = intent.getIntExtra(NotificationHelper.EXTRA_AMOUNT_ML, 250)
        NotificationHelper.cancelReminder(context)

        val pendingResult = goAsync()
        val app = context.applicationContext as? PulseHealthApplication ?: run {
            pendingResult.finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val container = app.container
                container.hydrationRepository.addWater(amountMl)
                container.syncRepository.triggerImmediateSync()

                // Optional Health Connect write-back if enabled
                val syncToHc = container.preferencesRepository.syncWaterToHealthConnect.first()
                if (syncToHc && container.healthConnectRepository.hasWriteHydrationPermission()) {
                    runCatching {
                        container.healthConnectRepository.writeHydration(amountMl, System.currentTimeMillis())
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
