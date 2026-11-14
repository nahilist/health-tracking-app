package com.pulsehealth.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.TrendPoint
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink

@Composable
fun MetricTrendChart(
    points: List<TrendPoint>,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    chartHeightDp: Int = 80,
    targetLabel: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Trend breakdown chart: ${points.joinToString { "${it.label}: ${it.displayValue}" }}"
            }
    ) {
        if (targetLabel != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = targetLabel,
                    fontSize = 12.sp,
                    color = Muted,
                    fontWeight = FontWeight.Medium
                )
                val metCount = points.count { it.isGoalMet }
                if (metCount > 0) {
                    Text(
                        text = "$metCount/${points.size} days met",
                        fontSize = 11.sp,
                        color = ExerciseGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height((chartHeightDp + 40).dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            points.forEach { point ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = point.displayValue,
                        fontSize = 10.sp,
                        fontWeight = if (point.isHighlighted) FontWeight.Bold else FontWeight.Normal,
                        color = if (point.isHighlighted) MaterialTheme.colorScheme.onSurface else Muted,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )

                    Spacer(Modifier.height(4.dp))

                    val barHeight = (point.value.coerceIn(0.06f, 1.0f) * chartHeightDp).dp

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(chartHeightDp.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // Background track
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(chartHeightDp.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        )
                        // Active bar
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when {
                                        point.isGoalMet -> ExerciseGreen
                                        point.isHighlighted -> primaryColor
                                        else -> primaryColor.copy(alpha = 0.7f)
                                    }
                                )
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = point.label,
                        fontSize = 11.sp,
                        fontWeight = if (point.isHighlighted) FontWeight.Bold else FontWeight.Normal,
                        color = if (point.isHighlighted) PulsePink else Muted
                    )

                    if (point.isHighlighted) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .width(4.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(PulsePink)
                        )
                    } else {
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}
