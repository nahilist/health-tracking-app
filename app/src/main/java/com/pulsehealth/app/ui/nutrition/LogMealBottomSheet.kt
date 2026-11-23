package com.pulsehealth.app.ui.nutrition

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.nutrition.MealType
import com.pulsehealth.app.ui.theme.AthleticOrange
import com.pulsehealth.app.ui.theme.DeepCharcoal
import com.pulsehealth.app.ui.theme.Muted

private val QuickMealPresets = mapOf(
    MealType.BREAKFAST to listOf("Oatmeal & Berries" to 380, "Eggs & Toast" to 420, "Protein Smoothie" to 350, "Avocado Toast" to 320),
    MealType.LUNCH to listOf("Chicken Salad Bowl" to 550, "Turkey Sandwich" to 480, "Quinoa & Veggie Bowl" to 460, "Rice & Salmon" to 620),
    MealType.DINNER to listOf("Steak & Sweet Potato" to 680, "Grilled Chicken & Rice" to 580, "Pasta with Turkey Bolognese" to 640, "Tofu Stir-fry" to 490),
    MealType.SNACK to listOf("Greek Yogurt & Honey" to 180, "Protein Bar" to 220, "Handful of Almonds" to 160, "Apple & Peanut Butter" to 210)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogMealBottomSheet(
    initialMealType: MealType = MealType.LUNCH,
    onLogMeal: (MealType, String, Int, Double, Double, Double, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedType by remember { mutableStateOf(initialMealType) }
    var mealName by remember { mutableStateOf("") }
    var calories by remember { mutableFloatStateOf(initialMealType.defaultTargetCalories.toFloat()) }
    var protein by remember { mutableFloatStateOf(25f) }
    var carbs by remember { mutableFloatStateOf(45f) }
    var fat by remember { mutableFloatStateOf(12f) }
    var notes by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 24.dp)
        ) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Log Meal",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepCharcoal
                    )
                    Text(
                        text = "Track daily fuel, calories & macros",
                        fontSize = 13.sp,
                        color = Muted
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.semantics { contentDescription = "Close sheet" }
                ) {
                    Icon(Icons.Default.Close, null, tint = Muted)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Meal Type Segmented Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MealType.entries.forEach { type ->
                    val isSelected = selectedType == type
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) DeepCharcoal else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedType = type
                                if (mealName.isBlank()) {
                                    calories = type.defaultTargetCalories.toFloat()
                                }
                            }
                            .semantics {
                                role = Role.Tab
                                selected = isSelected
                                contentDescription = "${type.label} tab"
                            }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = type.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else DeepCharcoal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = type.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else DeepCharcoal
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Quick Preset Chips
            Text(
                text = "POPULAR PRESETS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Muted,
                letterSpacing = 0.8.sp
            )
            Spacer(Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val presets = QuickMealPresets[selectedType].orEmpty()
                presets.forEach { (presetName, presetCal) ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                mealName = presetName
                                calories = presetCal.toFloat()
                            }
                    ) {
                        Text(
                            text = "$presetName (${presetCal} kcal)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = DeepCharcoal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Meal Name Input
            OutlinedTextField(
                value = mealName,
                onValueChange = { mealName = it },
                label = { Text("Meal or Food Description") },
                placeholder = { Text("e.g. Chicken Rice Bowl & Salad") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AthleticOrange,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Calorie Counter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, null, tint = AthleticOrange, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Calories", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepCharcoal)
                }
                Text(
                    text = "${calories.toInt()} kcal",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = AthleticOrange
                )
            }
            Slider(
                value = calories,
                onValueChange = { calories = it },
                valueRange = 50f..1800f,
                steps = 34,
                colors = SliderDefaults.colors(
                    thumbColor = AthleticOrange,
                    activeTrackColor = AthleticOrange,
                    inactiveTrackColor = Color(0xFFFFE0D3)
                )
            )

            Spacer(Modifier.height(12.dp))

            // Macro Sliders (Protein, Carbs, Fat)
            MacroSliderRow(
                label = "Protein",
                value = protein,
                unit = "g",
                color = Color(0xFF2563EB),
                range = 0f..150f,
                onValueChange = { protein = it }
            )

            Spacer(Modifier.height(8.dp))

            MacroSliderRow(
                label = "Carbohydrates",
                value = carbs,
                unit = "g",
                color = Color(0xFFD97706),
                range = 0f..250f,
                onValueChange = { carbs = it }
            )

            Spacer(Modifier.height(8.dp))

            MacroSliderRow(
                label = "Fats",
                value = fat,
                unit = "g",
                color = Color(0xFF059669),
                range = 0f..100f,
                onValueChange = { fat = it }
            )

            Spacer(Modifier.height(20.dp))

            // Save Meal CTA
            Button(
                onClick = {
                    val finalName = mealName.trim().ifEmpty { "${selectedType.label} Meal" }
                    onLogMeal(
                        selectedType,
                        finalName,
                        calories.toInt(),
                        protein.toDouble(),
                        carbs.toDouble(),
                        fat.toDouble(),
                        notes.ifBlank { null }
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .semantics { contentDescription = "Save ${selectedType.label}" },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AthleticOrange)
            ) {
                Text(
                    text = "Save ${selectedType.label} (${calories.toInt()} kcal)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun MacroSliderRow(
    label: String,
    value: Float,
    unit: String,
    color: Color,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DeepCharcoal)
            Text("${value.toInt()}$unit", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.2f)
            )
        )
    }
}
