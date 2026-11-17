package com.pulsehealth.app.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll

class PulseHealthWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = PulseHealthWidget()

    companion object {
        suspend fun updateAllWidgets(context: Context) {
            runCatching {
                PulseHealthWidget().updateAll(context)
            }
        }
    }
}
