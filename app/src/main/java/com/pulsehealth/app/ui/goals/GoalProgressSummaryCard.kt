package com.pulsehealth.app.ui.goals

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.pulsehealth.app.ui.theme.ForestEmerald
import com.pulsehealth.app.ui.theme.ForestEmeraldDark
import com.pulsehealth.app.ui.theme.ForestEmeraldLight
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.SleepPurple
import com.pulsehealth.app.ui.theme.WaterBlue
import java.util.Locale

/**
 * Executive dashboard card displaying daily target achievement across the 4 key
 * physiological pillars (Steps, Active Burn, Hydration, Sleep), with a 1-tap
 * action to trigger the Goal Setting Wizard.
 */
@Composable
fun GoalProgressSummaryCard(
    metrics: DashboardMetrics,
    onOpenWizard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = metrics.steps ?: 0L
    val stepGoal = metrics.stepGoal
    val stepProgress = if (stepGoal > 0) (steps.toFloat() / stepGoal).coerceIn(0f, 1f) else 0f

    val calories = metrics.activeCalories ?: 0L
    val calorieGoal = metrics.calorieGoal
    val calorieProgress = if (calorieGoal > 0) (calories.toFloat() / calorieGoal).coerceIn(0f, 1f) else 0f

    val water = metrics.waterLiters
    val waterGoal = metrics.waterGoalLiters
    val waterProgress = if (waterGoal > 0) (water.toFloat() / waterGoal.toFloat()).coerceIn(0f, 1f) else 0f

    val sleepMin = metrics.sleepMinutes ?: 0L
    val sleepGoal = 480L // Standard 8 hours
    val sleepProgress = (sleepMin.toFloat() / sleepGoal).coerceIn(0f, 1f)

    HealthCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Daily health targets and goal progress"
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = ForestEmeraldLight,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = ForestEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Daily Wellness Targets",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ForestEmeraldDark
                        )
                        Text(
                            text = "Personalized physiological baselines",
                            fontSize = 11.sp,
                            color = Muted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0F4F2),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenWizard() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Customize Targets",
                            tint = ForestEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Adjust",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = ForestEmerald
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // 4-Pillar Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TargetMetricMiniPill(
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    label = "Steps",
                    current = String.format(Locale.getDefault(), "%,d", steps),
                    target = String.format(Locale.getDefault(), "%,d", stepGoal),
                    progress = stepProgress,
                    accentColor = ExerciseGreen,
                    modifier = Modifier.weight(1f)
                )
                TargetMetricMiniPill(
                    icon = Icons.Default.Whatshot,
                    label = "Burn",
                    current = "$calories",
                    target = "$calorieGoal",
                    progress = calorieProgress,
                    accentColor = PulsePink,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TargetMetricMiniPill(
                    icon = Icons.Default.LocalDrink,
                    label = "Water",
                    current = String.format(Locale.getDefault(), "%.1fL", water),
                    target = String.format(Locale.getDefault(), "%.1fL", waterGoal),
                    progress = waterProgress,
                    accentColor = WaterBlue,
                    modifier = Modifier.weight(1f)
                )
                val hrs = sleepMin / 60
                val mins = sleepMin % 60
                val sleepText = if (mins == 0L) "${hrs}h" else "${hrs}h ${mins}m"
                TargetMetricMiniPill(
                    icon = Icons.Default.Bedtime,
                    label = "Sleep",
                    current = sleepText,
                    target = "8h",
                    progress = sleepProgress,
                    accentColor = SleepPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TargetMetricMiniPill(
    icon: ImageVector,
    label: String,
    current: String,
    target: String,
    progress: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF7FAF9),
        border = BorderStroke(1.dp, Color(0xFFE4EDE9)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = ForestEmeraldDark
                    )
                }

                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = accentColor
                )
            }

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                color = accentColor,
                trackColor = Color(0xFFE2E8E6),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.5.dp))
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = current,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = ForestEmeraldDark
                )
                Text(
                    text = "/ $target",
                    fontSize = 11.sp,
                    color = Muted
                )
            }
        }
    }
}
