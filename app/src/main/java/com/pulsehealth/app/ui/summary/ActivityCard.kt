package com.pulsehealth.app.ui.summary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
    val movePercent = ((metrics.activeCalories?.toFloat()?.div(metrics.calorieGoal) ?: 0f) * 100).toInt()
    val exercisePercent = ((metrics.exerciseMinutes?.toFloat()?.div(metrics.exerciseGoalMinutes) ?: 0f) * 100).toInt()

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
            Column(Modifier.weight(1f)) {
                Text("Activity Overview", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(
                    if (metrics.activeCalories != null || metrics.exerciseMinutes != null || metrics.standHours != null) {
                        "Daily movement, exercise & stand rings"
                    } else {
                        "No activity recorded today"
                    },
                    color = Muted,
                    fontSize = 12.sp
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ChevronRight, "Open activity", tint = Muted, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            ActivityRings(metrics, Modifier.size(118.dp))
            Spacer(Modifier.width(18.dp))
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActivityValueRow(
                    label = "Move",
                    value = metrics.activeCalories?.let { "$it" } ?: "—",
                    goal = "/ ${metrics.calorieGoal} kcal",
                    color = PulsePink,
                    percent = movePercent
                )
                ActivityValueRow(
                    label = "Exercise",
                    value = metrics.exerciseMinutes?.let { "$it" } ?: "—",
                    goal = "/ ${metrics.exerciseGoalMinutes} min",
                    color = ExerciseGreen,
                    percent = exercisePercent
                )
                ActivityValueRow(
                    label = "Stand",
                    value = metrics.standHours?.let { "$it" } ?: "—",
                    goal = "/ ${metrics.standGoalHours} hr",
                    color = StandCyan,
                    percent = ((metrics.standHours?.toFloat()?.div(metrics.standGoalHours) ?: 0f) * 100).toInt()
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
        val stroke = size.minDimension * 0.108f
        progresses.forEachIndexed { index, pair ->
            val inset = stroke / 2 + index * stroke * 1.36f
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
fun ActivityValueRow(
    label: String,
    value: String,
    goal: String,
    color: Color,
    percent: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(3.dp))
                Text(goal, fontSize = 11.sp, color = Muted)
            }
        }
        if (percent > 0) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "$percent%",
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
