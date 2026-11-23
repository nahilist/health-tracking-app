package com.pulsehealth.app.ui.summary

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.components.SmoothSparklineChart
import com.pulsehealth.app.ui.theme.AthleticCyan
import com.pulsehealth.app.ui.theme.AthleticOrange
import com.pulsehealth.app.ui.theme.AthleticOrangeLight
import com.pulsehealth.app.ui.theme.DeepCharcoal
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.StandCyan
import kotlin.math.roundToInt

private val ActivitySparklinePoints = listOf(0.35f, 0.48f, 0.88f, 0.42f, 0.65f, 0.72f, 0.55f)

@Composable
fun ActivityCard(metrics: DashboardMetrics, onClick: () -> Unit) {
    val calories = metrics.activeCalories ?: 450
    val goal = metrics.calorieGoal
    val percentage = ((calories.toDouble() / goal.coerceAtLeast(1).toDouble()) * 100.0).roundToInt().coerceIn(0, 999)
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
        // Top Header: Flame badge + Title + Percentage Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = AthleticOrangeLight,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = null,
                        tint = AthleticOrange,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = "Activity",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = DeepCharcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // High-Impact Percentage Completion Pill (as in Image 2)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AthleticOrange
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "$percentage%",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Giant Metric Value & Subtitle
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = String.format(java.util.Locale.getDefault(), "%,d", calories),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepCharcoal,
                    letterSpacing = (-1).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Cal",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Muted,
                    modifier = Modifier.padding(bottom = 4.dp),
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(8.dp))
            // Embedded Activity Rings
            ActivityRings(metrics, Modifier.size(48.dp))
        }

        Text(
            text = if (percentage >= 100) "Daily target achieved! Outstanding consistency" else "You improved overall well-being",
            fontSize = 12.sp,
            color = Muted,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(10.dp))

        // Ultra-Modern Smooth Curved Sparkline with Dotted Grid Lines & Glowing Peak
        SmoothSparklineChart(
            values = ActivitySparklinePoints,
            lineColor = AthleticOrange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // 3-Column Breakdown Row (Walking / Running / Workout or Move / Exercise / Stand)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9FAFB), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(AthleticOrange))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "Move",
                        fontSize = 11.sp,
                        color = Muted,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${metrics.activeCalories ?: 0} Cal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DeepCharcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(ExerciseGreen))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "Exercise",
                        fontSize = 11.sp,
                        color = Muted,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${metrics.exerciseMinutes ?: 0} min",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DeepCharcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(AthleticCyan))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "Stand",
                        fontSize = 11.sp,
                        color = Muted,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${metrics.standHours ?: 0} hr",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DeepCharcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ActivityRings(metrics: DashboardMetrics, modifier: Modifier = Modifier) {
    val progresses = remember(
        metrics.activeCalories,
        metrics.calorieGoal,
        metrics.exerciseMinutes,
        metrics.exerciseGoalMinutes,
        metrics.standHours,
        metrics.standGoalHours
    ) {
        listOf(
            (metrics.activeCalories?.toFloat()?.div(metrics.calorieGoal) ?: 0f) to AthleticOrange,
            (metrics.exerciseMinutes?.toFloat()?.div(metrics.exerciseGoalMinutes) ?: 0f) to ExerciseGreen,
            (metrics.standHours?.toFloat()?.div(metrics.standGoalHours) ?: 0f) to AthleticCyan
        )
    }
    Canvas(modifier.semantics { contentDescription = "Activity progress rings" }) {
        val stroke = size.minDimension * 0.105f
        progresses.forEachIndexed { index, pair ->
            val inset = stroke / 2 + index * stroke * 1.34f
            drawArc(
                pair.second.copy(alpha = 0.14f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round),
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2)
            )
            val sweep = (pair.first * 360f).coerceIn(0f, 360f)
            if (sweep > 0f) {
                drawArc(
                    pair.second,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2)
                )
            }
        }
    }
}
