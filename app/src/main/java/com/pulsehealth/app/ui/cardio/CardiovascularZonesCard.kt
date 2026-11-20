package com.pulsehealth.app.ui.cardio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.cardio.CardiovascularZoneBreakdown
import com.pulsehealth.app.data.cardio.HeartRateAnalyticsCalculator
import com.pulsehealth.app.data.cardio.HeartRateZone
import com.pulsehealth.app.data.cardio.RhrStabilityMetrics
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.theme.ForestEmerald
import com.pulsehealth.app.ui.theme.ForestEmeraldDark
import com.pulsehealth.app.ui.theme.ForestEmeraldLight
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.SoftCreamBackground
import com.pulsehealth.app.ui.theme.WarmAccent
import kotlin.math.roundToInt

/**
 * Executive modern cardiovascular analytics component providing real-time Heart Rate Zone
 * calculation, Tanaka Max HR customization, stacked intensity distributions, interactive zone
 * breakdown details, and sinus rhythm ECG trend waveform visualization.
 */
@Composable
fun CardiovascularZonesCard(
    currentRestingBpm: Int? = 68,
    activeMinutesToday: Int = 45,
    recentHeartRateReadings: List<Int> = emptyList(),
    modifier: Modifier = Modifier
) {
    var userAge by remember { mutableIntStateOf(30) }
    var selectedZone by remember { mutableStateOf(HeartRateZone.ZONE_2_FAT_BURN) }
    var showAgeAdjuster by remember { mutableStateOf(false) }

    val maxHr = remember(userAge) { HeartRateAnalyticsCalculator.calculateMaxHeartRate(userAge) }
    val zoneBreakdowns = remember(activeMinutesToday, currentRestingBpm, maxHr) {
        val estimatedAvgBpm = currentRestingBpm?.let { it + 45 } ?: 135
        HeartRateAnalyticsCalculator.computeZoneBreakdowns(
            totalDurationMinutes = activeMinutesToday,
            averageBpm = estimatedAvgBpm,
            maxHr = maxHr
        )
    }

    val stabilityMetrics = remember(recentHeartRateReadings) {
        val samples = if (recentHeartRateReadings.isNotEmpty()) {
            recentHeartRateReadings
        } else {
            listOf(64, 66, 68, 65, 67, 69, 66)
        }
        HeartRateAnalyticsCalculator.computeRhrStability(samples)
    }

    HealthCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Cardiovascular Heart Rate Zones and Pulse Analytics Card"
            }
    ) {
        // Card Title & Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = ForestEmeraldLight,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = ForestEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = "Cardiovascular Zones & Pulse",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = ForestEmeraldDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "5-Tier HR Training & Sinus Cadence",
                    color = Muted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Quick Max HR / Age pill button
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SoftCreamBackground,
                border = BorderStroke(1.dp, ForestEmerald.copy(alpha = 0.3f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { showAgeAdjuster = !showAgeAdjuster }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = ForestEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "$maxHr BPM Max",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = ForestEmeraldDark,
                        maxLines = 1
                    )
                }
            }
        }

        // Expandable Tanaka Age & Max HR Fine-Tuning Drawer
        AnimatedVisibility(visible = showAgeAdjuster) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .background(Color(0xFFF4F8F6), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Tanaka Max HR Formula",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ForestEmeraldDark
                        )
                        Text(
                            text = "HRmax = 208 - (0.7 × Age)",
                            fontSize = 11.sp,
                            color = Muted
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (userAge > 18) userAge-- },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease Age", tint = ForestEmerald)
                        }
                        Text(
                            text = "$userAge yrs",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = ForestEmeraldDark,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { if (userAge < 90) userAge++ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase Age", tint = ForestEmerald)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Multi-Segment Stacked Intensity Bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
            ) {
                zoneBreakdowns.forEach { breakdown ->
                    val weight = breakdown.percentageOfTotal.coerceAtLeast(0.02f)
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .height(14.dp)
                            .background(Color(breakdown.zone.colorHex))
                            .clickable { selectedZone = breakdown.zone }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Zone Legend Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeartRateZone.entries.forEach { zone ->
                    val isSelected = zone == selectedZone
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { selectedZone = zone }
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(Color(zone.colorHex))
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Z${zone.zoneNumber}",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) ForestEmeraldDark else Muted
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Selected Zone Detailed Card
        val selectedBreakdown = zoneBreakdowns.firstOrNull { it.zone == selectedZone }
            ?: zoneBreakdowns[1]

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SoftCreamBackground,
            border = BorderStroke(1.dp, Color(selectedZone.colorHex).copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(selectedZone.colorHex))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = selectedZone.label,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ForestEmeraldDark
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = selectedZone.percentRangeString,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Muted
                        )
                    }

                    // BPM Target Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(selectedZone.colorHex).copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "${selectedBreakdown.minBpm}–${selectedBreakdown.maxBpm} BPM",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color(selectedZone.colorHex),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = selectedZone.shortDescription,
                    fontSize = 12.sp,
                    color = ForestEmeraldDark,
                    lineHeight = 16.sp
                )

                Spacer(Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = ForestEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = selectedZone.physiologicalBenefit,
                            fontSize = 11.sp,
                            color = Muted,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Stats breakdown: Duration & Calories in Zone
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Active Time: ${selectedBreakdown.durationMinutes} mins (${(selectedBreakdown.percentageOfTotal * 100).roundToInt()}%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ForestEmeraldDark
                    )
                    Text(
                        text = "~${selectedBreakdown.estimatedCaloriesBurned} kcal burned",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmAccent
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Sinus Rhythm ECG Waveform Canvas Visualizer
        Text(
            text = "SINUS CADENCE & RESTING PULSE WAVEFORM",
            color = Muted,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
        )

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFF1F6F4),
            border = BorderStroke(1.dp, Color(0xFFD9E6E2)),
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                EcgWaveformCanvas(
                    lineColor = ForestEmerald,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Resting Pulse Stability Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF4F8F6), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = ForestEmerald,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Resting Stability: ${stabilityMetrics.statusLabel}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = ForestEmeraldDark
                )
            }

            Text(
                text = "${stabilityMetrics.stabilityScore}/100 Score",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = ForestEmerald
            )
        }
    }
}

/**
 * Procedural ECG Sinus Rhythm Canvas rendering characteristic P-Q-R-S-T cardiac deflections.
 */
@Composable
private fun EcgWaveformCanvas(
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height * 0.55f

        val path = Path()
        val numBeats = 3
        val beatWidth = width / numBeats

        path.moveTo(0f, centerY)

        for (i in 0 until numBeats) {
            val startX = i * beatWidth

            // Isoelectric line before P wave
            path.lineTo(startX + beatWidth * 0.15f, centerY)

            // P Wave (Atrial Depolarization)
            path.quadraticTo(
                startX + beatWidth * 0.22f, centerY - height * 0.18f,
                startX + beatWidth * 0.28f, centerY
            )

            // PR Segment (AV Node Conduction)
            path.lineTo(startX + beatWidth * 0.36f, centerY)

            // Q Wave (Initial Ventricular Septal Depolarization)
            path.lineTo(startX + beatWidth * 0.39f, centerY + height * 0.12f)

            // R Wave (Main Ventricular Depolarization Spike)
            path.lineTo(startX + beatWidth * 0.44f, centerY - height * 0.48f)

            // S Wave (Late Ventricular Depolarization)
            path.lineTo(startX + beatWidth * 0.48f, centerY + height * 0.22f)

            // ST Segment
            path.lineTo(startX + beatWidth * 0.54f, centerY)

            // T Wave (Ventricular Repolarization)
            path.quadraticTo(
                startX + beatWidth * 0.65f, centerY - height * 0.24f,
                startX + beatWidth * 0.76f, centerY
            )

            // Isoelectric line to end of beat
            path.lineTo(startX + beatWidth, centerY)
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
