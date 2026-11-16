package com.pulsehealth.app.ui.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.WeeklyWellnessDigest
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.SleepPurple
import com.pulsehealth.app.ui.theme.WarmOrange
import com.pulsehealth.app.ui.theme.WaterBlue
import java.util.Locale

@Composable
fun WeeklyDigestCard(
    digest: WeeklyWellnessDigest,
    modifier: Modifier = Modifier
) {
    HealthCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Card Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF0A84FF).copy(alpha = 0.12f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF0A84FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Weekly Wellness Digest",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        "7-day performance rollup",
                        color = Muted,
                        fontSize = 11.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = when {
                    digest.consistencyScore >= 80 -> ExerciseGreen.copy(alpha = 0.12f)
                    digest.consistencyScore >= 50 -> WarmOrange.copy(alpha = 0.12f)
                    else -> PulsePink.copy(alpha = 0.12f)
                }
            ) {
                Text(
                    text = "${digest.consistencyScore}% Score",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = when {
                        digest.consistencyScore >= 80 -> ExerciseGreen
                        digest.consistencyScore >= 50 -> WarmOrange
                        else -> PulsePink
                    },
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        Spacer(Modifier.height(14.dp))

        // 2x2 Metric Pillars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DigestPillar(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                iconTint = WarmOrange,
                label = "STEPS TOTAL",
                value = String.format(Locale.US, "%,d", digest.totalSteps),
                subtext = "${digest.stepGoalHitDays}/${digest.recordedDaysCount} goals met"
            )

            DigestPillar(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.MonitorHeart,
                iconTint = PulsePink,
                label = "RESTING HR",
                value = if (digest.averageRestingHeartRate != null) "${digest.averageRestingHeartRate} bpm" else "—",
                subtext = digest.restingHeartRateStatus
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val sleepHours = digest.averageNightlySleepMinutes / 60
            val sleepRemMinutes = digest.averageNightlySleepMinutes % 60
            DigestPillar(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.NightsStay,
                iconTint = SleepPurple,
                label = "NIGHTLY SLEEP",
                value = "${sleepHours}h ${sleepRemMinutes}m",
                subtext = "Average rest"
            )

            DigestPillar(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.LocalDrink,
                iconTint = WaterBlue,
                label = "WATER TOTAL",
                value = String.format(Locale.US, "%.1f L", digest.totalWaterLiters),
                subtext = "${digest.hydrationGoalHitDays}/${digest.recordedDaysCount} targets met"
            )
        }

        Spacer(Modifier.height(14.dp))

        // Bottom Milestone Banner
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = digest.milestoneMessage,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun DigestPillar(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    subtext: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Muted,
                    letterSpacing = 0.4.sp
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtext,
                fontSize = 10.sp,
                color = Muted
            )
        }
    }
}
