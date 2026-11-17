package com.pulsehealth.app.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.pulsehealth.app.PulseHealthApplication
import com.pulsehealth.app.data.local.HydrationEntryEntity
import com.pulsehealth.app.data.local.PulseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class QuickAddWaterActionCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        withContext(Dispatchers.IO) {
            val db = PulseDatabase.get(context)
            val now = System.currentTimeMillis()
            val entry = HydrationEntryEntity(
                id = UUID.randomUUID().toString(),
                amountMilliliters = 250,
                recordedAtEpochMillis = now,
                source = "widget",
                syncState = "pending"
            )
            db.hydrationDao().upsert(entry)

            val app = context.applicationContext as? PulseHealthApplication
            app?.container?.syncRepository?.triggerImmediateSync()

            PulseHealthWidget().update(context, glanceId)
        }
    }
}
