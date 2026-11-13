package com.pulsehealth.app.ui.summary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.StandCyan

@Composable
fun ActivityCard(metrics: DashboardMetrics, onClick: () -> Unit) {
    HealthCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text("Activity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    if (metrics.activeCalories != null || metrics.exerciseMinutes != null || metrics.standHours != null) {
                        "Daily movement and exercise"
                    } else {
                        "No activity recorded today"
                    },
                    color = Muted,
                    fontSize = 12.sp
                )
            }
            Icon(Icons.Default.ChevronRight, "Open activity", tint = Muted)
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ActivityRings(metrics, Modifier.size(112.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                ActivityValue(
                    "Move",
                    metrics.activeCalories?.let { "$it / ${metrics.calorieGoal} kcal" } ?: "— / ${metrics.calorieGoal} kcal",
                    PulsePink
                )
                Row {
                    Box(Modifier.weight(1f)) {
                        ActivityValue(
                            "Exercise",
                            metrics.exerciseMinutes?.let { "$it / ${metrics.exerciseGoalMinutes} min" } ?: "— / ${metrics.exerciseGoalMinutes} min",
                            ExerciseGreen
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        ActivityValue(
                            "Stand",
                            metrics.standHours?.let { "$it / ${metrics.standGoalHours} hr" } ?: "— / ${metrics.standGoalHours} hr",
                            StandCyan
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityRings(metrics: DashboardMetrics, modifier: Modifier = Modifier) {
    val progresses = listOf(
        (metrics.activeCalories?.toFloat()?.div(metrics.calorieGoal) ?: 0f) to PulsePink,
        (metrics.exerciseMinutes?.toFloat()?.div(metrics.exerciseGoalMinutes) ?: 0f) to ExerciseGreen,
        (metrics.standHours?.toFloat()?.div(metrics.standGoalHours) ?: 0f) to StandCyan
    )
    Canvas(modifier.semantics { contentDescription = "Activity progress rings" }) {
        val stroke = size.minDimension * 0.105f
        progresses.forEachIndexed { index, pair ->
            val inset = stroke / 2 + index * stroke * 1.35f
            drawArc(
                pair.second.copy(alpha = 0.16f),
                -90f,
                360f,
                false,
                Offset(inset, inset),
                Size(size.width - inset * 2, size.height - inset * 2),
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
            if (pair.first > 0f) {
                drawArc(
                    pair.second,
                    -90f,
                    pair.first.coerceIn(0f, 1f) * 360f,
                    false,
                    Offset(inset, inset),
                    Size(size.width - inset * 2, size.height - inset * 2),
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
fun ActivityValue(label: String, value: String, color: Color) {
    Column {
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
