package com.pulsehealth.app.ui.summary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
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
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.WaterBlue
import java.util.Locale

private data class HydrationPreset(
    val label: String,
    val amountMl: Int,
    val iconDescription: String
)

private val PRESETS = listOf(
    HydrationPreset("Cup", 150, "150 milliliters cup"),
    HydrationPreset("Glass", 250, "250 milliliters glass"),
    HydrationPreset("Bottle", 500, "500 milliliters bottle"),
    HydrationPreset("Flask", 750, "750 milliliters flask")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogHydrationBottomSheet(
    currentWaterLiters: Double,
    waterGoalLiters: Double,
    onLogWater: (amountMl: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedAmountMl by remember { mutableIntStateOf(250) }
    var customText by remember { mutableStateOf("250") }
    var isCustomMode by remember { mutableStateOf(false) }

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
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = WaterBlue.copy(alpha = 0.12f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = WaterBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Log Hydration",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Select a preset or enter custom volume",
                        fontSize = 12.sp,
                        color = Muted
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(40.dp).defaultMinSize(48.dp, 48.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close hydration logger",
                        tint = Muted
                    )
                }
            }

            // Quick Presets Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PRESETS.forEach { preset ->
                    val isSelected = !isCustomMode && selectedAmountMl == preset.amountMl
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                isCustomMode = false
                                selectedAmountMl = preset.amountMl
                                customText = preset.amountMl.toString()
                            }
                            .semantics {
                                role = Role.Button
                                contentDescription = "${preset.label}, ${preset.iconDescription}"
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) WaterBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) WaterBlue else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${preset.amountMl}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isSelected) WaterBlue else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "mL",
                                fontSize = 10.sp,
                                color = if (isSelected) WaterBlue else Muted
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = preset.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) WaterBlue else Muted
                            )
                        }
                    }
                }
            }

            // Stepper and Custom Volume Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Amount to Log",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = if (isCustomMode) "Custom" else "Preset",
                            fontSize = 11.sp,
                            color = WaterBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Decrement Button (-50 mL)
                        IconButton(
                            onClick = {
                                val next = (selectedAmountMl - 50).coerceAtLeast(50)
                                selectedAmountMl = next
                                customText = next.toString()
                                isCustomMode = true
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .defaultMinSize(48.dp, 48.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "Decrease by 50 milliliters",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Editable input
                        OutlinedTextField(
                            value = customText,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }.take(4)
                                customText = filtered
                                val parsed = filtered.toIntOrNull()
                                if (parsed != null && parsed in 1..5000) {
                                    selectedAmountMl = parsed
                                    isCustomMode = true
                                }
                            },
                            modifier = Modifier.width(130.dp),
                            textStyle = MaterialTheme.typography.titleLarge.copy(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            suffix = {
                                Text("mL", fontSize = 12.sp, color = Muted, fontWeight = FontWeight.Bold)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WaterBlue,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Increment Button (+50 mL)
                        IconButton(
                            onClick = {
                                val next = (selectedAmountMl + 50).coerceAtMost(5000)
                                selectedAmountMl = next
                                customText = next.toString()
                                isCustomMode = true
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .defaultMinSize(48.dp, 48.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Increase by 50 milliliters",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Calculation Preview Card
            val additionalLiters = selectedAmountMl / 1000.0
            val projectedTotalLiters = currentWaterLiters + additionalLiters
            val projectedPercent = if (waterGoalLiters > 0.0) {
                ((projectedTotalLiters / waterGoalLiters) * 100).toInt().coerceAtMost(200)
            } else 0

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = WaterBlue.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, WaterBlue.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocalDrink,
                        contentDescription = null,
                        tint = WaterBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Current: ${String.format(Locale.getDefault(), "%.1f", currentWaterLiters)}L → Projected: ${String.format(Locale.getDefault(), "%.2f", projectedTotalLiters)}L ($projectedPercent% of goal)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Primary Log Action
            Button(
                onClick = {
                    if (selectedAmountMl in 1..5000) {
                        onLogWater(selectedAmountMl)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WaterBlue),
                enabled = selectedAmountMl in 1..5000
            ) {
                Text(
                    text = "Log +$selectedAmountMl mL Water",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
