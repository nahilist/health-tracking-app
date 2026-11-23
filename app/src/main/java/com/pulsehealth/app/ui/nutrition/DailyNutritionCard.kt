package com.pulsehealth.app.ui.nutrition

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.nutrition.DailyNutritionSummary
import com.pulsehealth.app.data.nutrition.MealLog
import com.pulsehealth.app.data.nutrition.MealType
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.theme.DeepCharcoal
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted

private val ProteinColor = Color(0xFF2563EB) // Royal Blue
private val CarbsColor = Color(0xFFD97706)   // Amber
private val FatColor = Color(0xFF059669)     // Emerald
private val MacroPillBg = Color(0xFFF8FAFC)

@Composable
fun DailyNutritionCard(
    summary: DailyNutritionSummary,
    onAddMealClick: (MealType) -> Unit,
    onDeleteMealClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalLocale.current.platformLocale
    val totalCal = summary.totalCalories
    val goalCal = summary.calorieGoal
    val netCal = summary.netCalories

    HealthCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Nutrition: $totalCal of $goalCal kcal consumed, $netCal net calories. Protein ${summary.totalProteinGrams.toInt()}g, Carbs ${summary.totalCarbsGrams.toInt()}g, Fat ${summary.totalFatGrams.toInt()}g"
            }
    ) {
        // Section Header with Clear Typography & Net Calorie Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFEEF2FF),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = ProteinColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = "Nutrition",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = DeepCharcoal,
                    maxLines = 1
                )
                Text(
                    text = "Daily fuel & macronutrients",
                    fontSize = 12.sp,
                    color = Muted,
                    maxLines = 1
                )
            }

            // High-Impact Net Calorie Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (summary.activeBurnedCalories > 0) ExerciseGreen.copy(alpha = 0.12f) else MacroPillBg,
                border = BorderStroke(1.dp, if (summary.activeBurnedCalories > 0) ExerciseGreen.copy(alpha = 0.3f) else Color(0xFFE2E8F0))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (summary.activeBurnedCalories > 0) ExerciseGreen else Muted)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = if (summary.activeBurnedCalories > 0) {
                            String.format(locale, "%,d Net Cal", netCal)
                        } else {
                            String.format(locale, "%,d Cal", totalCal)
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (summary.activeBurnedCalories > 0) ExerciseGreen else DeepCharcoal,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Large High-Contrast Calorie Display with Tabular Numerals
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = String.format(locale, "%,d", totalCal),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepCharcoal,
                    letterSpacing = (-0.5).sp,
                    maxLines = 1
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = String.format(locale, "/ %,d kcal", goalCal),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Muted,
                    modifier = Modifier.padding(bottom = 3.dp),
                    maxLines = 1
                )
            }
        }

        Text(
            text = if (totalCal >= goalCal) {
                "Daily caloric intake goal achieved"
            } else {
                "${String.format(locale, "%,d", summary.remainingCalories)} kcal remaining · ${summary.activeBurnedCalories} active burned"
            },
            fontSize = 12.sp,
            color = Muted,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(14.dp))

        // 3-Macro Distribution Pillars (Protein, Carbs, Fat)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MacroPillar(
                label = "Protein",
                currentGrams = summary.totalProteinGrams,
                goalGrams = summary.proteinGoalGrams,
                color = ProteinColor,
                progress = summary.proteinProgress,
                modifier = Modifier.weight(1f)
            )
            MacroPillar(
                label = "Carbs",
                currentGrams = summary.totalCarbsGrams,
                goalGrams = summary.carbsGoalGrams,
                color = CarbsColor,
                progress = summary.carbsProgress,
                modifier = Modifier.weight(1f)
            )
            MacroPillar(
                label = "Fat",
                currentGrams = summary.totalFatGrams,
                goalGrams = summary.fatGoalGrams,
                color = FatColor,
                progress = summary.fatProgress,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // 4 Meal Slots (Breakfast, Lunch, Dinner, Snacks)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MealType.entries.forEach { type ->
                val loggedMeals = summary.mealsByType[type].orEmpty()
                MealSlotRow(
                    mealType = type,
                    loggedMeals = loggedMeals,
                    onAddClick = { onAddMealClick(type) },
                    onDeleteClick = onDeleteMealClick
                )
            }
        }
    }
}

@Composable
private fun MacroPillar(
    label: String,
    currentGrams: Double,
    goalGrams: Double,
    color: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MacroPillBg,
        border = BorderStroke(1.dp, Color(0xFFEEF2F6))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Muted
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "${currentGrams.toInt()}g",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DeepCharcoal,
                maxLines = 1
            )

            Spacer(Modifier.height(4.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }
        }
    }
}

@Composable
private fun MealSlotRow(
    mealType: MealType,
    loggedMeals: List<MealLog>,
    onAddClick: () -> Unit,
    onDeleteClick: (String) -> Unit
) {
    val locale = LocalLocale.current.platformLocale
    val totalMealCal = loggedMeals.sumOf { it.calories }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (loggedMeals.isNotEmpty()) Color.White else Color(0xFFF9FAFB),
        border = BorderStroke(1.dp, if (loggedMeals.isNotEmpty()) Color(0xFFE5E7EB) else Color(0xFFEEF1F4)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = mealType.icon,
                    contentDescription = null,
                    tint = DeepCharcoal,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = mealType.label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DeepCharcoal,
                    modifier = Modifier.weight(1f)
                )

                if (loggedMeals.isNotEmpty()) {
                    Text(
                        text = String.format(locale, "%,d kcal", totalMealCal),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = DeepCharcoal
                    )
                    Spacer(Modifier.width(6.dp))
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onAddClick)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Add ${mealType.label}"
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = DeepCharcoal,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = if (loggedMeals.isEmpty()) "Add" else "Log",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepCharcoal
                        )
                    }
                }
            }

            // Preview logged items if present
            if (loggedMeals.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                loggedMeals.forEach { meal ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "• ${meal.name}",
                            fontSize = 12.sp,
                            color = Muted,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${meal.calories} kcal (${meal.proteinGrams.toInt()}g P)",
                            fontSize = 11.sp,
                            color = Muted
                        )
                        IconButton(
                            onClick = { onDeleteClick(meal.id) },
                            modifier = Modifier
                                .size(24.dp)
                                .semantics { contentDescription = "Delete ${meal.name}" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
