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
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
    val steps = metrics.steps
    val description = if (steps != null) {
        "Steps: ${String.format(locale, "%,d", steps)} of ${String.format(locale, "%,d", metrics.stepGoal)} steps goal"
    } else {
        "Steps: No steps recorded"
    }
    MetricCard("Steps", Icons.AutoMirrored.Filled.DirectionsRun, WarmOrange, onClick, description) {
        if (steps != null) {
            Text(String.format(locale, "%,d", steps), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("of ${String.format(locale, "%,d", metrics.stepGoal)} steps", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            MiniBars(WarmOrange, listOf(.25f, .4f, .33f, .7f, .5f, .86f, .65f, .48f, .72f, .55f, .9f, .61f, 1f))
        } else {
            Text("—", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Muted)
            Text("No steps recorded", color = Muted, fontSize = 11.sp)
            EmptyMetricPlaceholder()
        }
    }
}

@Composable
fun HeartCard(metrics: DashboardMetrics, onClick: () -> Unit) {
    val hr = metrics.heartRate
    val description = if (hr != null) {
        "Heart rate: $hr bpm resting"
    } else {
        "Heart rate: No reading available"
    }
    MetricCard("Heart rate", Icons.Default.Favorite, PulsePink, onClick, description) {
        if (hr != null) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$hr", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(" bpm", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 3.dp))
            }
            Text("Resting", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            Sparkline(PulsePink, listOf(.25f, .35f, .28f, .52f, .39f, .34f, .66f, .45f, .58f, .4f, .5f, .7f, .61f, .76f))
        } else {
            Text("—", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Muted)
            Text("No reading available", color = Muted, fontSize = 11.sp)
            EmptyMetricPlaceholder()
        }
    }
}

@Composable
fun SleepCard(metrics: DashboardMetrics, onClick: () -> Unit) {
    val sleep = metrics.sleepMinutes
    val description = if (sleep != null) {
        "Sleep: ${sleep / 60} hours ${sleep % 60} minutes"
    } else {
        "Sleep: No sleep recorded"
    }
    MetricCard("Sleep", Icons.Default.NightsStay, SleepPurple, onClick, description) {
        if (sleep != null) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${sleep / 60}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(" hr ", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 3.dp))
                Text("${sleep % 60}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(" min", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 3.dp))
            }
            Text("Sleep duration", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            SleepBlocks()
        } else {
            Text("—", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Muted)
            Text("No sleep recorded", color = Muted, fontSize = 11.sp)
            EmptyMetricPlaceholder()
        }
    }
}

@Composable
fun WaterCard(metrics: DashboardMetrics, onAdd: () -> Unit, onClick: () -> Unit) {
    val locale = LocalLocale.current.platformLocale
    val description = "Water: ${String.format(locale, "%.1f", metrics.waterLiters)} of ${metrics.waterGoalLiters.toInt()} liters daily goal"
    MetricCard("Water", Icons.Default.LocalDrink, WaterBlue, onClick, description) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(String.format(locale, "%.1f", metrics.waterLiters), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(" L", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 3.dp))
        }
        Text("of ${String.format(locale, "%.1f", metrics.waterGoalLiters)} L goal", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val progress by animateFloatAsState(
                (metrics.waterLiters / metrics.waterGoalLiters).toFloat().coerceIn(0f, 1f),
                label = "water"
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(WaterBlue.copy(alpha = 0.15f))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(WaterBlue)
                )
            }
            Spacer(Modifier.width(6.dp))
            IconButton(
                onClick = onAdd,
                modifier = Modifier
                    .size(40.dp)
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .semantics { contentDescription = "Quick add 250 milliliters of water" }
            ) {
                Surface(
                    shape = CircleShape,
                    color = WaterBlue.copy(alpha = 0.12f)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        Modifier.padding(6.dp),
                        tint = WaterBlue
                    )
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
    cardDescription: String = "$title card",
    content: @Composable ColumnScope.() -> Unit
) {
    HealthCard(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 150.dp)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = cardDescription
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(title, color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
fun EmptyMetricPlaceholder() {
    Row(
        Modifier.fillMaxWidth().height(32.dp).padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(7) {
            Box(
                Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Muted.copy(alpha = 0.2f))
            )
        }
    }
}

@Composable
fun MiniBars(color: Color, values: List<Float>) {
    Row(
        Modifier.fillMaxWidth().height(32.dp).padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEachIndexed { i, value ->
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxSize(value)
                    .background(
                        color.copy(alpha = if (i > values.lastIndex - 3) 0.95f else 0.35f),
                        RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                    )
            )
        }
    }
}

@Composable
fun Sparkline(color: Color, values: List<Float>) {
    Canvas(Modifier.fillMaxWidth().height(32.dp).padding(top = 8.dp)) {
        values.zipWithNext().forEachIndexed { index, pair ->
            val x1 = index * size.width / (values.size - 1)
            val x2 = (index + 1) * size.width / (values.size - 1)
            drawLine(color, Offset(x1, size.height * (1 - pair.first)), Offset(x2, size.height * (1 - pair.second)), strokeWidth = 2f)
        }
    }
}

@Composable
fun SleepBlocks() {
    Row(
        Modifier.fillMaxWidth().height(34.dp).padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(0.65f, 0.42f, 0.8f, 0.55f, 0.88f, 0.7f, 0.38f, 0.74f, 0.58f).forEach { value ->
            Box(
                Modifier
                    .weight(1f)
                    .height((24 * value).dp)
                    .background(SleepPurple.copy(alpha = 0.65f), RoundedCornerShape(2.dp))
            )
        }
    }
}
