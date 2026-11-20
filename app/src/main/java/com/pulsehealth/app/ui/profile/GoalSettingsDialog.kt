package com.pulsehealth.app.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.SleepPurple
import com.pulsehealth.app.ui.theme.WarmOrange
import com.pulsehealth.app.ui.theme.WaterBlue
import java.util.Locale

@Composable
fun GoalSettingsDialog(
    initialWaterGoalLiters: Double,
    initialStepGoal: Long,
    initialCalorieGoal: Long,
    initialSleepGoalMinutes: Long,
    onSaveGoals: (waterLiters: Double, steps: Long, calories: Long, sleepMinutes: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var waterGoal by remember { mutableFloatStateOf(initialWaterGoalLiters.toFloat()) }
    var stepGoal by remember { mutableLongStateOf(initialStepGoal) }
    var calorieGoal by remember { mutableLongStateOf(initialCalorieGoal) }
    var sleepMinutes by remember { mutableLongStateOf(initialSleepGoalMinutes) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 500.dp)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PulsePink.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Flag,
                                contentDescription = null,
                                tint = PulsePink,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Personal Wellness Targets",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Customize your daily fitness & health goals",
                            fontSize = 11.sp,
                            color = Muted
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp).defaultMinSize(48.dp, 48.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close goal editor", tint = Muted)
                    }
                }

                // 1. Water Target
                GoalSliderCard(
                    title = "Daily Hydration Target",
                    valueLabel = "${String.format(Locale.getDefault(), "%.1f", waterGoal)} L",
                    icon = Icons.Default.LocalDrink,
                    accentColor = WaterBlue,
                    value = waterGoal,
                    range = 1.0f..5.0f,
                    steps = 39,
                    onValueChange = { waterGoal = Math.round(it * 10f) / 10f }
                )

                // 2. Steps Target
                GoalSliderCard(
                    title = "Daily Steps Target",
                    valueLabel = "${String.format(Locale.getDefault(), "%,d", stepGoal)} steps",
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    accentColor = PulsePink,
                    value = stepGoal.toFloat(),
                    range = 2_000f..25_000f,
                    steps = 45,
                    onValueChange = {
                        val rounded = Math.round(it / 500f) * 500
                        stepGoal = rounded.toLong()
                    }
                )

                // 3. Active Calories Target
                GoalSliderCard(
                    title = "Daily Active Calories",
                    valueLabel = "${String.format(Locale.getDefault(), "%,d", calorieGoal)} kcal",
                    icon = Icons.Default.Whatshot,
                    accentColor = WarmOrange,
                    value = calorieGoal.toFloat(),
                    range = 100f..1_500f,
                    steps = 27,
                    onValueChange = {
                        val rounded = Math.round(it / 50f) * 50
                        calorieGoal = rounded.toLong()
                    }
                )

                // 4. Sleep Target
                val sleepHours = sleepMinutes / 60.0
                GoalSliderCard(
                    title = "Daily Sleep Target",
                    valueLabel = "${String.format(Locale.getDefault(), "%.1f", sleepHours)} hours",
                    icon = Icons.Default.Bedtime,
                    accentColor = SleepPurple,
                    value = sleepMinutes.toFloat(),
                    range = 300f..660f,
                    steps = 11,
                    onValueChange = {
                        val rounded = Math.round(it / 30f) * 30
                        sleepMinutes = rounded.toLong()
                    }
                )

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            waterGoal = 2.0f
                            stepGoal = 10_000L
                            calorieGoal = 500L
                            sleepMinutes = 480L
                        },
                        modifier = Modifier.weight(1f).height(46.dp).defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Reset", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            onSaveGoals(
                                waterGoal.toDouble(),
                                stepGoal,
                                calorieGoal,
                                sleepMinutes
                            )
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.5f).height(46.dp).defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PulsePink)
                    ) {
                        Text("Save Targets", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalSliderCard(
    title: String,
    valueLabel: String,
    icon: ImageVector,
    accentColor: Color,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = valueLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                steps = steps,
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = accentColor.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth().semantics {
                    contentDescription = "$title, $valueLabel"
                }
            )
        }
    }
}
