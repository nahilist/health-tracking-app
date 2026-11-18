package com.pulsehealth.app.ui.goals

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.ForestEmerald
import com.pulsehealth.app.ui.theme.ForestEmeraldDark
import com.pulsehealth.app.ui.theme.ForestEmeraldLight
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.SleepPurple
import com.pulsehealth.app.ui.theme.WarmAccent
import com.pulsehealth.app.ui.theme.WaterBlue
import java.util.Locale

/**
 * Interactive 5-step modal wizard for personalizing health targets:
 * Step 1: Daily Movement & Steps
 * Step 2: Active Calories & Energy Burn
 * Step 3: Daily Hydration Volume
 * Step 4: Sleep Duration & Sleep Cycles
 * Step 5: Review & Save Summary
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalWizardBottomSheet(
    initialWaterGoalLiters: Double,
    initialStepGoal: Long,
    initialCalorieGoal: Long,
    initialSleepGoalMinutes: Long,
    onSaveGoals: (waterLiters: Double, steps: Long, calories: Long, sleepMinutes: Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentStep by remember { mutableIntStateOf(1) }
    var steps by remember { mutableLongStateOf(initialStepGoal.coerceIn(1_000L, 100_000L)) }
    var calories by remember { mutableLongStateOf(initialCalorieGoal.coerceIn(50L, 10_000L)) }
    var waterLiters by remember { mutableDoubleStateOf(initialWaterGoalLiters.coerceIn(0.5, 10.0)) }
    var sleepMinutes by remember { mutableLongStateOf(initialSleepGoalMinutes.coerceIn(60L, 1_440L)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 1) {
                    IconButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous step",
                            tint = ForestEmeraldDark
                        )
                    }
                } else {
                    Spacer(Modifier.size(36.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Customize Targets",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = ForestEmeraldDark
                    )
                    Text(
                        text = "Step $currentStep of 5",
                        fontSize = 12.sp,
                        color = Muted,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close wizard",
                        tint = Muted
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Step Progress Bar
            LinearProgressIndicator(
                progress = { currentStep / 5f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when (currentStep) {
                    1 -> ExerciseGreen
                    2 -> PulsePink
                    3 -> WaterBlue
                    4 -> SleepPurple
                    else -> ForestEmerald
                },
                trackColor = Color(0xFFE4EDE9)
            )

            Spacer(Modifier.height(24.dp))

            // Animated Step Pages
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "GoalWizardStepTransition"
            ) { step ->
                when (step) {
                    1 -> StepGoalPage(
                        currentSteps = steps,
                        onStepsChange = { steps = it }
                    )
                    2 -> CalorieGoalPage(
                        currentCalories = calories,
                        onCaloriesChange = { calories = it }
                    )
                    3 -> WaterGoalPage(
                        currentWaterLiters = waterLiters,
                        onWaterChange = { waterLiters = it }
                    )
                    4 -> SleepGoalPage(
                        currentSleepMinutes = sleepMinutes,
                        onSleepChange = { sleepMinutes = it }
                    )
                    5 -> ReviewGoalsPage(
                        waterLiters = waterLiters,
                        steps = steps,
                        calories = calories,
                        sleepMinutes = sleepMinutes,
                        initialWaterLiters = initialWaterGoalLiters,
                        initialSteps = initialStepGoal,
                        initialCalories = initialCalorieGoal,
                        initialSleepMinutes = initialSleepGoalMinutes
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Bottom Navigation Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep < 5) {
                    Button(
                        onClick = { currentStep++ },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Proceed to next target step"
                            }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Next Step", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            onSaveGoals(waterLiters, steps, calories, sleepMinutes)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Save and apply all health targets"
                            }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Save & Apply Targets", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Step 1: Daily Movement & Steps
// ---------------------------------------------------------------------------
@Composable
private fun StepGoalPage(
    currentSteps: Long,
    onStepsChange: (Long) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = ExerciseGreen.copy(alpha = 0.12f),
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    GoalCategory.STEPS.icon,
                    contentDescription = null,
                    tint = ExerciseGreen,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Daily Step Target",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ForestEmeraldDark
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Regular walking strengthens cardiovascular endurance and metabolic rate.",
            fontSize = 13.sp,
            color = Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(20.dp))

        // Large Number Display
        Text(
            text = String.format(Locale.getDefault(), "%,d", currentSteps),
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            color = ExerciseGreen,
            letterSpacing = (-1).sp
        )
        Text(
            text = "steps per day",
            fontSize = 13.sp,
            color = Muted,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(6.dp))

        // Derived Metric Estimation
        val distKm = currentSteps * 0.00075
        val walkMin = currentSteps / 100L
        Text(
            text = "≈ ${String.format(Locale.getDefault(), "%.1f", distKm)} km • ~${walkMin} mins daily movement",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ForestEmeraldDark
        )

        Spacer(Modifier.height(20.dp))

        // Preset Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GoalPresets.stepPresets.forEach { preset ->
                val selected = currentSteps == preset.steps
                FilterChip(
                    selected = selected,
                    onClick = { onStepsChange(preset.steps) },
                    label = { Text(preset.label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ExerciseGreen.copy(alpha = 0.15f),
                        selectedLabelColor = ExerciseGreen
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = if (selected) ExerciseGreen else Color(0xFFD4E0DC)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Fine Tuning Controls (Stepper + Slider)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onStepsChange((currentSteps - 500L).coerceAtLeast(1_000L)) },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF0F4F2), CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease 500 steps", tint = ForestEmeraldDark)
            }

            Slider(
                value = currentSteps.toFloat(),
                onValueChange = { onStepsChange((it.toLong() / 250L) * 250L) },
                valueRange = 2_000f..25_000f,
                colors = SliderDefaults.colors(
                    thumbColor = ExerciseGreen,
                    activeTrackColor = ExerciseGreen,
                    inactiveTrackColor = Color(0xFFE2E8E6)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )

            IconButton(
                onClick = { onStepsChange((currentSteps + 500L).coerceAtMost(50_000L)) },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF0F4F2), CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase 500 steps", tint = ForestEmeraldDark)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Step 2: Active Calories
// ---------------------------------------------------------------------------
@Composable
private fun CalorieGoalPage(
    currentCalories: Long,
    onCaloriesChange: (Long) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = PulsePink.copy(alpha = 0.12f),
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    GoalCategory.CALORIES.icon,
                    contentDescription = null,
                    tint = PulsePink,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Active Calorie Burn",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ForestEmeraldDark
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Calories expended through workouts and active movement throughout the day.",
            fontSize = 13.sp,
            color = Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = String.format(Locale.getDefault(), "%,d", currentCalories),
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            color = PulsePink,
            letterSpacing = (-1).sp
        )
        Text(
            text = "kcal per day",
            fontSize = 13.sp,
            color = Muted,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(6.dp))

        val exerciseEquivalentMin = (currentCalories / 8.5).toInt()
        Text(
            text = "≈ ~$exerciseEquivalentMin mins of moderate cardio activity",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ForestEmeraldDark
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GoalPresets.caloriePresets.forEach { preset ->
                val selected = currentCalories == preset.calories
                FilterChip(
                    selected = selected,
                    onClick = { onCaloriesChange(preset.calories) },
                    label = { Text(preset.label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PulsePink.copy(alpha = 0.15f),
                        selectedLabelColor = PulsePink
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = if (selected) PulsePink else Color(0xFFD4E0DC)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onCaloriesChange((currentCalories - 50L).coerceAtLeast(100L)) },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF0F4F2), CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease 50 kcal", tint = ForestEmeraldDark)
            }

            Slider(
                value = currentCalories.toFloat(),
                onValueChange = { onCaloriesChange((it.toLong() / 25L) * 25L) },
                valueRange = 150f..1500f,
                colors = SliderDefaults.colors(
                    thumbColor = PulsePink,
                    activeTrackColor = PulsePink,
                    inactiveTrackColor = Color(0xFFE2E8E6)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )

            IconButton(
                onClick = { onCaloriesChange((currentCalories + 50L).coerceAtMost(3000L)) },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF0F4F2), CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase 50 kcal", tint = ForestEmeraldDark)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Step 3: Daily Hydration
// ---------------------------------------------------------------------------
@Composable
private fun WaterGoalPage(
    currentWaterLiters: Double,
    onWaterChange: (Double) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = WaterBlue.copy(alpha = 0.12f),
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    GoalCategory.WATER.icon,
                    contentDescription = null,
                    tint = WaterBlue,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Daily Hydration Target",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ForestEmeraldDark
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Adequate water intake aids nutrient transport, joint lubrication, and cognitive performance.",
            fontSize = 13.sp,
            color = Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = String.format(Locale.getDefault(), "%.1f", currentWaterLiters),
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            color = WaterBlue,
            letterSpacing = (-1).sp
        )
        Text(
            text = "liters per day",
            fontSize = 13.sp,
            color = Muted,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(6.dp))

        val glasses = (currentWaterLiters * 4.0).toInt()
        Text(
            text = "≈ $glasses standard glasses (250 mL each)",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ForestEmeraldDark
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GoalPresets.waterPresets.forEach { preset ->
                val selected = kotlin.math.abs(currentWaterLiters - preset.liters) < 0.05
                FilterChip(
                    selected = selected,
                    onClick = { onWaterChange(preset.liters) },
                    label = { Text(preset.label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WaterBlue.copy(alpha = 0.15f),
                        selectedLabelColor = WaterBlue
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = if (selected) WaterBlue else Color(0xFFD4E0DC)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onWaterChange(((currentWaterLiters - 0.25) * 4).toInt() / 4.0.coerceAtLeast(1.0)) },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF0F4F2), CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease 250 mL", tint = ForestEmeraldDark)
            }

            Slider(
                value = currentWaterLiters.toFloat(),
                onValueChange = { onWaterChange(((it * 4).toInt() / 4.0).coerceIn(1.0, 6.0)) },
                valueRange = 1.0f..5.0f,
                colors = SliderDefaults.colors(
                    thumbColor = WaterBlue,
                    activeTrackColor = WaterBlue,
                    inactiveTrackColor = Color(0xFFE2E8E6)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )

            IconButton(
                onClick = { onWaterChange(((currentWaterLiters + 0.25) * 4).toInt() / 4.0.coerceAtMost(6.0)) },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF0F4F2), CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase 250 mL", tint = ForestEmeraldDark)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Step 4: Sleep Duration
// ---------------------------------------------------------------------------
@Composable
private fun SleepGoalPage(
    currentSleepMinutes: Long,
    onSleepChange: (Long) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = SleepPurple.copy(alpha = 0.12f),
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    GoalCategory.SLEEP.icon,
                    contentDescription = null,
                    tint = SleepPurple,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Nightly Sleep Duration",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ForestEmeraldDark
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Sufficient deep and REM sleep enables tissue repair, immune vitality, and memory consolidation.",
            fontSize = 13.sp,
            color = Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(20.dp))

        val hrs = currentSleepMinutes / 60
        val mins = currentSleepMinutes % 60
        val formatted = if (mins == 0L) "${hrs}h" else "${hrs}h ${mins}m"

        Text(
            text = formatted,
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            color = SleepPurple,
            letterSpacing = (-1).sp
        )
        Text(
            text = "nightly duration",
            fontSize = 13.sp,
            color = Muted,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(6.dp))

        val cycles = (currentSleepMinutes / 90L).toInt()
        Text(
            text = "≈ $cycles complete 90-minute sleep cycles",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ForestEmeraldDark
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GoalPresets.sleepPresets.forEach { preset ->
                val selected = currentSleepMinutes == preset.minutes
                FilterChip(
                    selected = selected,
                    onClick = { onSleepChange(preset.minutes) },
                    label = { Text(preset.label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SleepPurple.copy(alpha = 0.15f),
                        selectedLabelColor = SleepPurple
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = if (selected) SleepPurple else Color(0xFFD4E0DC)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onSleepChange((currentSleepMinutes - 30L).coerceAtLeast(360L)) },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF0F4F2), CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease 30 mins", tint = ForestEmeraldDark)
            }

            Slider(
                value = currentSleepMinutes.toFloat(),
                onValueChange = { onSleepChange((it.toLong() / 15L) * 15L) },
                valueRange = 360f..660f,
                colors = SliderDefaults.colors(
                    thumbColor = SleepPurple,
                    activeTrackColor = SleepPurple,
                    inactiveTrackColor = Color(0xFFE2E8E6)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )

            IconButton(
                onClick = { onSleepChange((currentSleepMinutes + 30L).coerceAtMost(720L)) },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF0F4F2), CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase 30 mins", tint = ForestEmeraldDark)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Step 5: Review & Save Summary
// ---------------------------------------------------------------------------
@Composable
private fun ReviewGoalsPage(
    waterLiters: Double,
    steps: Long,
    calories: Long,
    sleepMinutes: Long,
    initialWaterLiters: Double,
    initialSteps: Long,
    initialCalories: Long,
    initialSleepMinutes: Long
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = ForestEmeraldLight,
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = ForestEmerald,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Review Daily Targets",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ForestEmeraldDark
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Your updated health targets will dynamically recalibrate daily activity rings, notifications, and analytics.",
            fontSize = 13.sp,
            color = Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(Modifier.height(20.dp))

        // Targets comparison cards list
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GoalReviewRow(
                category = GoalCategory.STEPS,
                newValue = "${String.format(Locale.getDefault(), "%,d", steps)} steps",
                initialValue = "${String.format(Locale.getDefault(), "%,d", initialSteps)} steps"
            )
            GoalReviewRow(
                category = GoalCategory.CALORIES,
                newValue = "${String.format(Locale.getDefault(), "%,d", calories)} kcal",
                initialValue = "${String.format(Locale.getDefault(), "%,d", initialCalories)} kcal"
            )
            GoalReviewRow(
                category = GoalCategory.WATER,
                newValue = "${String.format(Locale.getDefault(), "%.1f", waterLiters)} L",
                initialValue = "${String.format(Locale.getDefault(), "%.1f", initialWaterLiters)} L"
            )
            val hrs = sleepMinutes / 60
            val mins = sleepMinutes % 60
            val sleepFormatted = if (mins == 0L) "${hrs}h" else "${hrs}h ${mins}m"
            val initHrs = initialSleepMinutes / 60
            val initMins = initialSleepMinutes % 60
            val initFormatted = if (initMins == 0L) "${initHrs}h" else "${initHrs}h ${initMins}m"
            GoalReviewRow(
                category = GoalCategory.SLEEP,
                newValue = sleepFormatted,
                initialValue = initFormatted
            )
        }
    }
}

@Composable
private fun GoalReviewRow(
    category: GoalCategory,
    newValue: String,
    initialValue: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF7FAF9),
        border = BorderStroke(1.dp, Color(0xFFE4EDE9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = category.accentColor.copy(alpha = 0.14f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        category.icon,
                        contentDescription = null,
                        tint = category.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = category.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ForestEmeraldDark
                )
                Text(
                    text = "Previous: $initialValue",
                    fontSize = 11.sp,
                    color = Muted
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = category.accentColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = newValue,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = category.accentColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
