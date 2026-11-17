package com.pulsehealth.app.ui.summary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.WorkoutType
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.WarmOrange
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogWorkoutBottomSheet(
    onLogWorkout: (
        workoutType: WorkoutType,
        title: String?,
        durationMinutes: Long,
        activeCalories: Int?,
        distanceMeters: Double?,
        notes: String?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedType by remember { mutableStateOf(WorkoutType.RUNNING) }
    var durationMinutes by remember { mutableIntStateOf(30) }
    var customMinutesText by remember { mutableStateOf("30") }
    var isCustomMinutes by remember { mutableStateOf(false) }

    // Calories calculation / override
    val autoEstimatedCalories = (durationMinutes * selectedType.defaultCaloriesPerMinute).toInt()
    var caloriesText by remember(selectedType, durationMinutes) {
        mutableStateOf(autoEstimatedCalories.toString())
    }

    var distanceKmText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var workoutTitle by remember(selectedType) { mutableStateOf(selectedType.defaultTitle) }

    val supportsDistance = selectedType in listOf(WorkoutType.RUNNING, WorkoutType.WALKING, WorkoutType.CYCLING)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = WarmOrange.copy(alpha = 0.12f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            selectedType.icon,
                            contentDescription = null,
                            tint = WarmOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Log Workout",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "Record your physical exercise session",
                        style = MaterialTheme.typography.bodySmall.copy(color = Muted)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics {
                            contentDescription = "Close log workout dialog"
                            role = Role.Button
                        }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = Muted
                    )
                }
            }

            // Workout Type Selector Chips (Horizontal Scrollable)
            Text(
                text = "WORKOUT TYPE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Muted,
                    letterSpacing = 0.8.sp
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkoutType.entries.forEach { type ->
                    val isSelected = type == selectedType
                    val chipBg = if (isSelected) WarmOrange.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    val chipBorder = if (isSelected) WarmOrange else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    val contentColor = if (isSelected) WarmOrange else MaterialTheme.colorScheme.onSurface

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(chipBg)
                            .clickable {
                                selectedType = type
                                workoutTitle = type.defaultTitle
                            }
                            .semantics {
                                contentDescription = "${type.label} workout"
                                role = Role.Button
                            }
                            .defaultMinSize(minHeight = 44.dp)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            type.icon,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = type.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = contentColor
                        )
                    }
                }
            }

            // Duration Selector Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                ),
                border = BorderStroke(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "DURATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Muted,
                            letterSpacing = 0.8.sp
                        )
                    )

                    // Stepper Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                val next = (durationMinutes - 5).coerceAtLeast(1)
                                durationMinutes = next
                                customMinutesText = next.toString()
                            },
                            enabled = durationMinutes > 1,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .semantics {
                                    contentDescription = "Decrease duration by 5 minutes"
                                    role = Role.Button
                                }
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$durationMinutes min",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                )
                            )
                            Text(
                                text = "≈ $autoEstimatedCalories active kcal",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ExerciseGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        IconButton(
                            onClick = {
                                val next = (durationMinutes + 5).coerceAtMost(1440)
                                durationMinutes = next
                                customMinutesText = next.toString()
                            },
                            enabled = durationMinutes < 1440,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .semantics {
                                    contentDescription = "Increase duration by 5 minutes"
                                    role = Role.Button
                                }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }

                    // Quick duration presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 30, 45, 60).forEach { mins ->
                            val isPresetSelected = durationMinutes == mins
                            val btnBg = if (isPresetSelected) WarmOrange else MaterialTheme.colorScheme.surface
                            val btnText = if (isPresetSelected) Color.White else MaterialTheme.colorScheme.onSurface

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(btnBg)
                                    .clickable {
                                        durationMinutes = mins
                                        customMinutesText = mins.toString()
                                        isCustomMinutes = false
                                    }
                                    .semantics {
                                        contentDescription = "$mins minutes preset"
                                        role = Role.Button
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${mins}m",
                                    fontSize = 13.sp,
                                    fontWeight = if (isPresetSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = btnText
                                )
                            }
                        }
                    }
                }
            }

            // Active Calories & Distance Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Calories input
                OutlinedTextField(
                    value = caloriesText,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        caloriesText = digits
                    },
                    label = { Text("Active kcal") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = if (supportsDistance) ImeAction.Next else ImeAction.Done
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                // Optional distance input
                if (supportsDistance) {
                    OutlinedTextField(
                        value = distanceKmText,
                        onValueChange = { input ->
                            // Allow decimal numbers
                            if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d*$"""))) {
                                distanceKmText = input
                            }
                        },
                        label = { Text("Distance (km)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Title / Description
            OutlinedTextField(
                value = workoutTitle,
                onValueChange = { workoutTitle = it },
                label = { Text("Workout Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Save Action Button
            Button(
                onClick = {
                    val finalCalories = caloriesText.toIntOrNull() ?: autoEstimatedCalories
                    val finalDistanceMeters = distanceKmText.toDoubleOrNull()?.let { it * 1000.0 }
                    onLogWorkout(
                        selectedType,
                        workoutTitle.takeIf { it.isNotBlank() },
                        durationMinutes.toLong(),
                        finalCalories,
                        finalDistanceMeters,
                        notesText.takeIf { it.isNotBlank() }
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp)
                    .semantics {
                        contentDescription = "Confirm and log $durationMinutes minute ${selectedType.label} workout"
                        role = Role.Button
                    },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmOrange,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Save Workout • $durationMinutes min (${caloriesText.ifBlank { autoEstimatedCalories }} kcal)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
