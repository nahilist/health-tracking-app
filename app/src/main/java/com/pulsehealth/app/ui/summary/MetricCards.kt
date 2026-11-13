package com.pulsehealth.app.ui.summary

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.SleepPurple
import com.pulsehealth.app.ui.theme.WarmOrange
import com.pulsehealth.app.ui.theme.WaterBlue

@Composable
fun StepsCard(metrics: DashboardMetrics, onClick: () -> Unit) {
    val locale = LocalLocale.current.platformLocale
    MetricCard("Steps", Icons.AutoMirrored.Filled.DirectionsRun, WarmOrange, onClick) {
        val steps = metrics.steps
        if (steps != null) {
            Text(String.format(locale, "%,d", steps), fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Text("of ${String.format(locale, "%,d", metrics.stepGoal)} steps", color = Muted, fontSize = 11.sp)
            MiniBars(WarmOrange, listOf(.25f, .4f, .33f, .7f, .5f, .86f, .65f, .48f, .72f, .55f, .9f, .61f, 1f))
        } else {
            Text("—", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Muted)
            Text("No steps recorded", color = Muted, fontSize = 11.sp)
            EmptyMetricPlaceholder()
        }
    }
}

@Composable
fun HeartCard(metrics: DashboardMetrics, onClick: () -> Unit) {
    MetricCard("Heart rate", Icons.Default.Favorite, PulsePink, onClick) {
        val hr = metrics.heartRate
        if (hr != null) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$hr", fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text(" bpm", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 5.dp))
            }
            Text("Resting", color = Muted, fontSize = 11.sp)
            Sparkline(PulsePink, listOf(.25f, .35f, .28f, .52f, .39f, .34f, .66f, .45f, .58f, .4f, .5f, .7f, .61f, .76f))
        } else {
            Text("—", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Muted)
            Text("No reading available", color = Muted, fontSize = 11.sp)
            EmptyMetricPlaceholder()
        }
    }
}

@Composable
fun SleepCard(metrics: DashboardMetrics, onClick: () -> Unit) {
    MetricCard("Sleep", Icons.Default.NightsStay, SleepPurple, onClick) {
        val sleep = metrics.sleepMinutes
        if (sleep != null) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${sleep / 60}", fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text(" hr  ", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 5.dp))
                Text("${sleep % 60}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(" min", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 5.dp))
            }
            Text("Total sleep time", color = Muted, fontSize = 10.sp)
            SleepBlocks()
        } else {
            Text("—", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Muted)
            Text("No sleep recorded", color = Muted, fontSize = 11.sp)
            EmptyMetricPlaceholder()
        }
    }
}

@Composable
fun WaterCard(metrics: DashboardMetrics, onAdd: () -> Unit, onClick: () -> Unit) {
    val locale = LocalLocale.current.platformLocale
    MetricCard("Water", Icons.Default.LocalDrink, WaterBlue, onClick) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(String.format(locale, "%.1f", metrics.waterLiters), fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Text(" L", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 5.dp))
        }
        Text("of ${metrics.waterGoalLiters.toInt()} L daily goal", color = Muted, fontSize = 11.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            val progress by animateFloatAsState(
                (metrics.waterLiters / metrics.waterGoalLiters).toFloat().coerceIn(0f, 1f),
                label = "water"
            )
            Box(Modifier.weight(1f).height(8.dp).clip(CircleShape).background(WaterBlue.copy(alpha = 0.15f))) {
                Box(Modifier.fillMaxWidth(progress).height(8.dp).clip(CircleShape).background(WaterBlue))
            }
            IconButton(onClick = onAdd, modifier = Modifier.size(36.dp)) {
                Surface(shape = CircleShape, color = WaterBlue.copy(alpha = 0.1f)) {
                    Icon(Icons.Default.Add, "Add 250 milliliters", Modifier.padding(6.dp), tint = WaterBlue)
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    HealthCard(Modifier.fillMaxWidth().height(154.dp).clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(5.dp))
            Text(title, color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(7.dp))
        content()
    }
}

@Composable
fun EmptyMetricPlaceholder() {
    Row(
        Modifier.fillMaxWidth().height(36.dp).padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(7) {
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Muted.copy(alpha = 0.25f))
            )
        }
    }
}

@Composable
fun MiniBars(color: Color, values: List<Float>) {
    Row(Modifier.fillMaxWidth().height(36.dp).padding(top = 7.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
        values.forEachIndexed { i, value ->
            Box(Modifier.weight(1f).fillMaxSize(value).background(color.copy(alpha = if (i > values.lastIndex - 3) 1f else 0.45f), RoundedCornerShape(2.dp)))
        }
    }
}

@Composable
fun Sparkline(color: Color, values: List<Float>) {
    Canvas(Modifier.fillMaxWidth().height(36.dp).padding(top = 9.dp)) {
        values.zipWithNext().forEachIndexed { index, pair ->
            val x1 = index * size.width / (values.size - 1)
            val x2 = (index + 1) * size.width / (values.size - 1)
            drawLine(color, Offset(x1, size.height * (1 - pair.first)), Offset(x2, size.height * (1 - pair.second)), strokeWidth = 2.5f)
        }
    }
}

@Composable
fun SleepBlocks() {
    Row(Modifier.fillMaxWidth().height(38.dp).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf(0.65f, 0.42f, 0.8f, 0.55f, 0.88f, 0.7f, 0.38f, 0.74f, 0.58f).forEach { value ->
            Box(Modifier.weight(1f).height((28 * value).dp).background(SleepPurple.copy(alpha = 0.75f), RoundedCornerShape(3.dp)))
        }
    }
}
