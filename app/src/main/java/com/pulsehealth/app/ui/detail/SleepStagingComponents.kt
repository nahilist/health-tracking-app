package com.pulsehealth.app.ui.detail

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.SleepBreakdownSummary
import com.pulsehealth.app.data.SleepStageType
import com.pulsehealth.app.data.local.SleepSessionEntity
import com.pulsehealth.app.data.local.SleepSessionWithStages
import com.pulsehealth.app.data.local.SleepStageEntity
import androidx.compose.ui.platform.LocalLocale
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.theme.Muted
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SleepHypnogramCard(
    sessionWithStages: SleepSessionWithStages?,
    isDemo: Boolean,
    modifier: Modifier = Modifier
) {
    val activeSession = remember(sessionWithStages, isDemo) {
        sessionWithStages ?: if (isDemo) createDemoSleepSession() else null
    }

    if (activeSession == null) {
        SleepEmptyStateCard(modifier = modifier)
        return
    }

    val locale = LocalLocale.current.platformLocale
    val session = activeSession.session
    val stages = activeSession.stages
    val breakdown = remember(stages, session.durationMinutes) {
        SleepBreakdownSummary.fromStages(stages, session.durationMinutes)
    }

    val timeFormatter = remember(locale) { DateTimeFormatter.ofPattern("h:mm a", locale) }
    val startTimeStr = remember(session.startTimeEpochMillis) {
        Instant.ofEpochMilli(session.startTimeEpochMillis).atZone(ZoneId.systemDefault()).format(timeFormatter)
    }
    val endTimeStr = remember(session.endTimeEpochMillis) {
        Instant.ofEpochMilli(session.endTimeEpochMillis).atZone(ZoneId.systemDefault()).format(timeFormatter)
    }

    val accessibilityDesc = "Sleep session hypnogram from $startTimeStr to $endTimeStr. " +
        "Total sleep: ${breakdown.totalSleepMinutes / 60} hours ${breakdown.totalSleepMinutes % 60} minutes. " +
        "Deep sleep: ${breakdown.deepMinutes} minutes, Light sleep: ${breakdown.lightMinutes} minutes, " +
        "REM sleep: ${breakdown.remMinutes} minutes, Awake: ${breakdown.awakeMinutes} minutes. " +
        "Efficiency: ${breakdown.sleepEfficiencyPercentage} percent."

    HealthCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = accessibilityDesc }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF673AB7).copy(alpha = 0.15f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = Color(0xFF673AB7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "SLEEP HYPNOGRAM",
                        color = Muted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$startTimeStr – $endTimeStr",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when {
                    breakdown.sleepEfficiencyPercentage >= 85 -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                    breakdown.sleepEfficiencyPercentage >= 75 -> Color(0xFF2196F3).copy(alpha = 0.15f)
                    else -> Color(0xFFFF9800).copy(alpha = 0.15f)
                }
            ) {
                Text(
                    text = "${breakdown.sleepEfficiencyPercentage}% ${breakdown.efficiencyRating}",
                    color = when {
                        breakdown.sleepEfficiencyPercentage >= 85 -> Color(0xFF2E7D32)
                        breakdown.sleepEfficiencyPercentage >= 75 -> Color(0xFF1565C0)
                        else -> Color(0xFFE65100)
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Visual Hypnogram Stepped Bars
        if (stages.isNotEmpty()) {
            val totalSpan = (session.endTimeEpochMillis - session.startTimeEpochMillis).coerceAtLeast(1L)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                horizontalArrangement = Arrangement.Start
            ) {
                stages.forEach { stageEntity ->
                    val weight = ((stageEntity.endTimeEpochMillis - stageEntity.startTimeEpochMillis).toFloat() / totalSpan).coerceAtLeast(0.01f)
                    val stageType = SleepStageType.fromString(stageEntity.stage)
                    val stageColor = Color(stageType.colorHex)

                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .height(
                                when (stageType) {
                                    SleepStageType.DEEP -> 36.dp
                                    SleepStageType.LIGHT -> 26.dp
                                    SleepStageType.REM -> 18.dp
                                    SleepStageType.AWAKE -> 8.dp
                                    SleepStageType.UNKNOWN -> 20.dp
                                }
                            )
                            .background(stageColor)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Time Markers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(startTimeStr, color = Muted, fontSize = 11.sp)
            Text("${breakdown.inBedMinutes / 60}h ${breakdown.inBedMinutes % 60}m in bed", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(endTimeStr, color = Muted, fontSize = 11.sp)
        }

        Spacer(Modifier.height(12.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(label = "Deep", color = Color(SleepStageType.DEEP.colorHex))
            LegendItem(label = "Light", color = Color(SleepStageType.LIGHT.colorHex))
            LegendItem(label = "REM", color = Color(SleepStageType.REM.colorHex))
            LegendItem(label = "Awake", color = Color(SleepStageType.AWAKE.colorHex))
        }
    }
}

@Composable
fun SleepStageBreakdownCard(
    sessionWithStages: SleepSessionWithStages?,
    isDemo: Boolean,
    modifier: Modifier = Modifier
) {
    val activeSession = remember(sessionWithStages, isDemo) {
        sessionWithStages ?: if (isDemo) createDemoSleepSession() else null
    } ?: return

    val stages = activeSession.stages
    val breakdown = remember(stages, activeSession.session.durationMinutes) {
        SleepBreakdownSummary.fromStages(stages, activeSession.session.durationMinutes)
    }

    HealthCard(modifier = modifier.fillMaxWidth()) {
        Text("STAGE BREAKDOWN", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        // Proportional Multi-Segment Progress Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            if (breakdown.deepPercentage > 0) {
                Box(
                    Modifier
                        .weight(breakdown.deepPercentage.toFloat())
                        .height(12.dp)
                        .background(Color(SleepStageType.DEEP.colorHex))
                )
            }
            if (breakdown.lightPercentage > 0) {
                Box(
                    Modifier
                        .weight(breakdown.lightPercentage.toFloat())
                        .height(12.dp)
                        .background(Color(SleepStageType.LIGHT.colorHex))
                )
            }
            if (breakdown.remPercentage > 0) {
                Box(
                    Modifier
                        .weight(breakdown.remPercentage.toFloat())
                        .height(12.dp)
                        .background(Color(SleepStageType.REM.colorHex))
                )
            }
            if (breakdown.awakePercentage > 0) {
                Box(
                    Modifier
                        .weight(breakdown.awakePercentage.toFloat())
                        .height(12.dp)
                        .background(Color(SleepStageType.AWAKE.colorHex))
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 4 Stage Detail Rows
        StageDetailRow(
            title = "Deep Sleep",
            durationMinutes = breakdown.deepMinutes,
            percentage = breakdown.deepPercentage,
            color = Color(SleepStageType.DEEP.colorHex),
            typicalNote = "Physical recovery (15–25% typical)"
        )
        Spacer(Modifier.height(10.dp))
        StageDetailRow(
            title = "Light Sleep",
            durationMinutes = breakdown.lightMinutes,
            percentage = breakdown.lightPercentage,
            color = Color(SleepStageType.LIGHT.colorHex),
            typicalNote = "Body maintenance (45–55% typical)"
        )
        Spacer(Modifier.height(10.dp))
        StageDetailRow(
            title = "REM Sleep",
            durationMinutes = breakdown.remMinutes,
            percentage = breakdown.remPercentage,
            color = Color(SleepStageType.REM.colorHex),
            typicalNote = "Cognitive & memory consolidation (20–25% typical)"
        )
        Spacer(Modifier.height(10.dp))
        StageDetailRow(
            title = "Awake",
            durationMinutes = breakdown.awakeMinutes,
            percentage = breakdown.awakePercentage,
            color = Color(SleepStageType.AWAKE.colorHex),
            typicalNote = "Natural brief awakenings (<10% typical)"
        )
    }
}

@Composable
fun SleepEfficiencyCard(
    sessionWithStages: SleepSessionWithStages?,
    isDemo: Boolean,
    modifier: Modifier = Modifier
) {
    val activeSession = remember(sessionWithStages, isDemo) {
        sessionWithStages ?: if (isDemo) createDemoSleepSession() else null
    } ?: return

    val stages = activeSession.stages
    val breakdown = remember(stages, activeSession.session.durationMinutes) {
        SleepBreakdownSummary.fromStages(stages, activeSession.session.durationMinutes)
    }

    HealthCard(modifier = modifier.fillMaxWidth()) {
        Text("RESTFULNESS & EFFICIENCY", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text("Sleep Efficiency", color = Muted, fontSize = 11.sp)
                Text(
                    text = "${breakdown.sleepEfficiencyPercentage}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = when {
                        breakdown.sleepEfficiencyPercentage >= 85 -> Color(0xFF2E7D32)
                        breakdown.sleepEfficiencyPercentage >= 75 -> Color(0xFF1565C0)
                        else -> Color(0xFFE65100)
                    }
                )
                Text(
                    text = breakdown.efficiencyRating,
                    color = Muted,
                    fontSize = 11.sp
                )
            }

            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Time Asleep", color = Muted, fontSize = 11.sp)
                val hours = breakdown.totalSleepMinutes / 60
                val mins = breakdown.totalSleepMinutes % 60
                Text(
                    text = "${hours}h ${mins}m",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text("Restorative sleep", color = Muted, fontSize = 11.sp)
            }

            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("Time in Bed", color = Muted, fontSize = 11.sp)
                val inBedHours = breakdown.inBedMinutes / 60
                val inBedMins = breakdown.inBedMinutes % 60
                Text(
                    text = "${inBedHours}h ${inBedMins}m",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text("${breakdown.awakeMinutes}m awake", color = Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun SleepEmptyStateCard(modifier: Modifier = Modifier) {
    HealthCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF673AB7).copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.NightlightRound,
                        contentDescription = null,
                        tint = Color(0xFF673AB7),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "No Sleep Staging Recorded",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Wear your connected wearable to track sleep stages",
                    color = Muted,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Health Connect automatically syncs Deep, Light, REM, and Awake stages from compatible smartwatches and fitness trackers.",
            color = Muted,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun StageDetailRow(
    title: String,
    durationMinutes: Long,
    percentage: Int,
    color: Color,
    typicalNote: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(typicalNote, color = Muted, fontSize = 10.sp)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            val hours = durationMinutes / 60
            val mins = durationMinutes % 60
            val durationText = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
            Text(durationText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("$percentage%", color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, color = Muted, fontSize = 11.sp)
    }
}

fun createDemoSleepSession(): SleepSessionWithStages {
    val now = System.currentTimeMillis()
    // Session: 11:15 PM last night to 7:15 AM this morning (8 hours = 480 minutes)
    val startTime = now - (8 * 3600_000L)
    val endTime = now

    val sessionId = "demo_sleep_session_1"
    val stages = listOf(
        // 11:15 - 11:30 (15 min): Awake
        SleepStageEntity("demo_s1", sessionId, SleepStageType.AWAKE.name, startTime, startTime + (15 * 60_000L), 15),
        // 11:30 - 1:00 (90 min): Light
        SleepStageEntity("demo_s2", sessionId, SleepStageType.LIGHT.name, startTime + (15 * 60_000L), startTime + (105 * 60_000L), 90),
        // 1:00 - 2:20 (80 min): Deep
        SleepStageEntity("demo_s3", sessionId, SleepStageType.DEEP.name, startTime + (105 * 60_000L), startTime + (185 * 60_000L), 80),
        // 2:20 - 3:40 (80 min): Light
        SleepStageEntity("demo_s4", sessionId, SleepStageType.LIGHT.name, startTime + (185 * 60_000L), startTime + (265 * 60_000L), 80),
        // 3:40 - 5:10 (90 min): REM
        SleepStageEntity("demo_s5", sessionId, SleepStageType.REM.name, startTime + (265 * 60_000L), startTime + (355 * 60_000L), 90),
        // 5:10 - 6:00 (50 min): Deep
        SleepStageEntity("demo_s6", sessionId, SleepStageType.DEEP.name, startTime + (355 * 60_000L), startTime + (405 * 60_000L), 50),
        // 6:00 - 7:00 (60 min): Light
        SleepStageEntity("demo_s7", sessionId, SleepStageType.LIGHT.name, startTime + (405 * 60_000L), startTime + (465 * 60_000L), 60),
        // 7:00 - 7:15 (15 min): Awake
        SleepStageEntity("demo_s8", sessionId, SleepStageType.AWAKE.name, startTime + (465 * 60_000L), endTime, 15)
    )

    val session = SleepSessionEntity(
        id = sessionId,
        title = "Night Sleep",
        startTimeEpochMillis = startTime,
        endTimeEpochMillis = endTime,
        durationMinutes = 480,
        deepDurationMinutes = 130, // 80 + 50 = 130 min (27%)
        lightDurationMinutes = 230, // 90 + 80 + 60 = 230 min (48%)
        remDurationMinutes = 90, // 90 min (19%)
        awakeDurationMinutes = 30, // 15 + 15 = 30 min (6%)
        sleepEfficiencyPercentage = 94,
        source = "Demo",
        notes = "Restorative 8-hour sleep session with deep sleep cycles.",
        syncState = "synced"
    )

    return SleepSessionWithStages(session, stages)
}
