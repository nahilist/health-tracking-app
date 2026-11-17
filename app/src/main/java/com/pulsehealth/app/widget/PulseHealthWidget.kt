package com.pulsehealth.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pulsehealth.app.MainActivity
import com.pulsehealth.app.R
import java.util.Locale

class PulseHealthWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = WidgetDataProvider.loadState(context)

        provideContent {
            GlanceTheme {
                WidgetContent(state = state)
            }
        }
    }

    @Composable
    fun WidgetContent(state: WidgetState) {
        val bgModifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF141A24))
            .cornerRadius(20.dp)
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>())

        Column(
            modifier = bgModifier,
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // Top Row: Brand Icon, Name, Date
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_app),
                    contentDescription = "Pulse Health",
                    modifier = GlanceModifier.size(24.dp)
                )
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    text = "Pulse Health",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = state.dateLabel,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF8A9BA8)),
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(GlanceModifier.height(10.dp))

            // Hydration Section
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_notification_water),
                    contentDescription = "Water",
                    modifier = GlanceModifier.size(20.dp)
                )
                Spacer(GlanceModifier.width(6.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                        Text(
                            text = String.format(Locale.US, "%.2f", state.waterLiters),
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = " / ${state.waterGoalLiters} L",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF8A9BA8)),
                                fontSize = 12.sp
                            )
                        )
                        Spacer(GlanceModifier.defaultWeight())
                        Text(
                            text = if (state.isGoalAchieved) "Goal Met! 🔥" else "${state.percentInt}%",
                            style = TextStyle(
                                color = ColorProvider(if (state.isGoalAchieved) Color(0xFF2ECC71) else Color(0xFF38A3F1)),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(GlanceModifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = state.progressRatio,
                        modifier = GlanceModifier.fillMaxWidth().height(6.dp),
                        color = ColorProvider(if (state.isGoalAchieved) Color(0xFF2ECC71) else Color(0xFF38A3F1)),
                        backgroundColor = ColorProvider(Color(0xFF253346))
                    )
                }
            }

            Spacer(GlanceModifier.height(10.dp))

            // Bottom Row: Steps + Calories & Quick Action Button
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "${state.steps} steps",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFF68B1E)),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "${state.activeCalories} kcal active",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFF2335B)),
                            fontSize = 11.sp
                        )
                    )
                }

                Button(
                    text = "+250 mL",
                    onClick = actionRunCallback<QuickAddWaterActionCallback>(),
                    modifier = GlanceModifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ColorProvider(Color(0xFF1E88E5)),
                        contentColor = ColorProvider(Color.White)
                    )
                )
            }
        }
    }
}
