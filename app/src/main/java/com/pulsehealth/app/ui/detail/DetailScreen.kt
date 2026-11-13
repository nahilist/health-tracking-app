package com.pulsehealth.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.data.local.HydrationEntryEntity
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.summary.MiniBars
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.SleepPurple
import com.pulsehealth.app.ui.theme.WarmOrange
import com.pulsehealth.app.ui.theme.WaterBlue
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DetailScreen(
    type: String,
    metrics: DashboardMetrics,
    isDemo: Boolean,
    todayHydrationEntries: List<HydrationEntryEntity> = emptyList(),
    weeklyWaterLiters: Double = 0.0,
    onAddWater: (Int) -> Unit = {},
    onEditWater: (String, Int) -> Unit = { _, _ -> },
    onDeleteWater: (String) -> Unit = {},
    onSetWaterGoal: (Double) -> Unit = {},
    onBack: () -> Unit
) {
    val locale = LocalLocale.current.platformLocale
    val color = when (type) {
        "Steps" -> WarmOrange
        "Heart rate" -> PulsePink
        "Sleep" -> SleepPurple
        "Water" -> WaterBlue
        else -> PulsePink
    }

    var editingEntry by remember { mutableStateOf<HydrationEntryEntity?>(null) }
    var showGoalDialog by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                Column(Modifier.weight(1f)) {
                    Text(type, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (isDemo) "Demo preview data" else "Source: ${metrics.source.label}",
                        color = Muted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (type == "Water") {
            item {
                HealthCard(Modifier.fillMaxWidth()) {
                    Text("TODAY'S INTAKE", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = String.format(locale, "%.2f L", metrics.waterLiters),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(
                            onClick = { showGoalDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Goal: ${String.format(locale, "%.1f", metrics.waterGoalLiters)} L")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    val progress = (metrics.waterLiters / metrics.waterGoalLiters).toFloat().coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = WaterBlue,
                        trackColor = WaterBlue.copy(alpha = 0.15f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            item {
                HealthCard(Modifier.fillMaxWidth()) {
                    Text("QUICK LOG", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(250, 500, 750).forEach { amount ->
                            FilledTonalButton(
                                onClick = { onAddWater(amount) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("+$amount mL", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            item {
                HealthCard(Modifier.fillMaxWidth()) {
                    Text("WEEKLY TOTAL", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = String.format(locale, "%.1f L", weeklyWaterLiters),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Past 7 days",
                                color = Muted,
                                fontSize = 12.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format(locale, "%.1f L / day", weeklyWaterLiters / 7.0),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Daily average",
                                color = Muted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Today's Log (${todayHydrationEntries.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (todayHydrationEntries.isEmpty()) {
                item {
                    HealthCard(Modifier.fillMaxWidth()) {
                        Text(
                            text = "No hydration entries recorded today.",
                            color = Muted,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Log water intake above to track daily hydration.",
                            color = Muted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                items(todayHydrationEntries, key = { it.id }) { entry ->
                    HealthCard(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = WaterBlue.copy(alpha = 0.12f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.LocalDrink,
                                        contentDescription = null,
                                        tint = WaterBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "${entry.amountMilliliters} mL",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                val time = Instant.ofEpochMilli(entry.recordedAtEpochMillis)
                                    .atZone(ZoneId.systemDefault())
                                    .format(DateTimeFormatter.ofPattern("h:mm a", locale))
                                Text(
                                    text = time,
                                    color = Muted,
                                    fontSize = 12.sp
                                )
                            }
                            IconButton(onClick = { editingEntry = entry }) {
                                Icon(Icons.Default.Edit, "Edit entry", tint = Muted, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onDeleteWater(entry.id) }) {
                                Icon(Icons.Default.DeleteOutline, "Delete entry", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        } else {
            item {
                HealthCard(Modifier.fillMaxWidth()) {
                    Text("TODAY", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val (valueText, hasData) = when (type) {
                        "Steps" -> metrics.steps?.let { "${String.format(locale, "%,d", it)} steps" } to (metrics.steps != null)
                        "Heart rate" -> metrics.heartRate?.let { "$it bpm resting" } to (metrics.heartRate != null)
                        "Sleep" -> metrics.sleepMinutes?.let { "${it / 60} hr ${it % 60} min" } to (metrics.sleepMinutes != null)
                        else -> metrics.activeCalories?.let { "$it active kcal" } to (metrics.activeCalories != null)
                    }
                    Text(
                        text = valueText ?: "No data recorded",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasData) MaterialTheme.colorScheme.onSurface else Muted
                    )
                    Spacer(Modifier.height(18.dp))
                    if (hasData) {
                        MiniBars(color, listOf(.3f, .45f, .38f, .62f, .54f, .8f, .67f, .9f, .76f, 1f, .84f, .94f))
                    } else {
                        Text(
                            "No records found for today. New readings from Health Connect will appear here.",
                            color = Muted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            items(listOf("7-day trend", "Goal progress")) { label ->
                HealthCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(label, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ChevronRight, null, tint = Muted)
                    }
                }
            }

            item {
                HealthCard(Modifier.fillMaxWidth()) {
                    Text("DATA SOURCES", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (metrics.contributingApps.isNotEmpty()) {
                        metrics.contributingApps.forEach { appName ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Apps, null, tint = color, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(appName, fontSize = 14.sp)
                            }
                        }
                    } else {
                        Text(
                            text = if (isDemo) "Demo sample data" else "No connected data sources for this range",
                            color = Muted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    editingEntry?.let { entry ->
        EditHydrationDialog(
            entry = entry,
            onDismiss = { editingEntry = null },
            onSave = { newAmount ->
                onEditWater(entry.id, newAmount)
                editingEntry = null
            }
        )
    }

    if (showGoalDialog) {
        SetWaterGoalDialog(
            currentGoal = metrics.waterGoalLiters,
            onDismiss = { showGoalDialog = false },
            onSave = { newGoal ->
                onSetWaterGoal(newGoal)
                showGoalDialog = false
            }
        )
    }
}

@Composable
fun EditHydrationDialog(
    entry: HydrationEntryEntity,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf(entry.amountMilliliters.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit hydration entry") },
        text = {
            Column {
                Text("Enter amount in milliliters (1–5000 mL):", fontSize = 13.sp, color = Muted)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        val parsed = it.toIntOrNull()
                        isError = parsed == null || parsed !in 1..5000
                    },
                    isError = isError,
                    label = { Text("Amount (mL)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                if (isError) {
                    Text("Please enter a valid amount between 1 and 5000 mL", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = textValue.toIntOrNull()
                    if (parsed != null && parsed in 1..5000) {
                        onSave(parsed)
                    }
                },
                enabled = !isError && textValue.toIntOrNull() != null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SetWaterGoalDialog(
    currentGoal: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    val locale = LocalLocale.current.platformLocale
    var textValue by remember { mutableStateOf(String.format(locale, "%.1f", currentGoal)) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set daily water goal") },
        text = {
            Column {
                Text("Target daily intake in liters (0.5–10.0 L):", fontSize = 13.sp, color = Muted)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        val parsed = it.toDoubleOrNull()
                        isError = parsed == null || parsed !in 0.5..10.0
                    },
                    isError = isError,
                    label = { Text("Daily goal (Liters)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                if (isError) {
                    Text("Please enter a goal between 0.5 and 10.0 L", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = textValue.toDoubleOrNull()
                    if (parsed != null && parsed in 0.5..10.0) {
                        onSave(parsed)
                    }
                },
                enabled = !isError && textValue.toDoubleOrNull() != null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
