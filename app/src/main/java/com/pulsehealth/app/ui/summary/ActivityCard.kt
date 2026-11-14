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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
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
    val description = "Activity: Move ${metrics.activeCalories ?: 0} of ${metrics.calorieGoal} kcal, Exercise ${metrics.exerciseMinutes ?: 0} of ${metrics.exerciseGoalMinutes} minutes, Stand ${metrics.standHours ?: 0} of ${metrics.standGoalHours} hours. Tap for details."

    HealthCard(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = description
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Activity", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, "Open activity", tint = Muted.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
        }

        Spacer(Modifier.height(14.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            ActivityRings(metrics, Modifier.size(108.dp))
            Spacer(Modifier.width(20.dp))
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MinimalActivityRow(
                    label = "Move",
                    value = metrics.activeCalories?.toString() ?: "—",
                    target = "/ ${metrics.calorieGoal} kcal",
                    color = PulsePink
                )
                MinimalActivityRow(
                    label = "Exercise",
                    value = metrics.exerciseMinutes?.toString() ?: "—",
                    target = "/ ${metrics.exerciseGoalMinutes} min",
                    color = ExerciseGreen
                )
                MinimalActivityRow(
                    label = "Stand",
                    value = metrics.standHours?.toString() ?: "—",
                    target = "/ ${metrics.standGoalHours} hr",
                    color = StandCyan
                )
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
            val inset = stroke / 2 + index * stroke * 1.34f
            drawArc(
                pair.second.copy(alpha = 0.14f),
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
private fun MinimalActivityRow(
    label: String,
    value: String,
    target: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .align(Alignment.CenterVertically)
        )
        Column {
            Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(3.dp))
                Text(target, fontSize = 11.sp, color = Muted)
            }
        }
    }
}
