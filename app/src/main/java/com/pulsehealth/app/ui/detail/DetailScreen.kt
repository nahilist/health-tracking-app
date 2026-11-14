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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.DailyHydrationProgress
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.data.HydrationStreakInfo
import com.pulsehealth.app.data.MetricRegistry
import com.pulsehealth.app.data.TimeRange
import com.pulsehealth.app.data.local.HydrationEntryEntity
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.components.MetricTrendChart
import com.pulsehealth.app.ui.summary.ActivityRings
import com.pulsehealth.app.ui.summary.RangeSelector
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.StandCyan
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
    selectedRange: TimeRange = TimeRange.TODAY,
    onSelectRange: (TimeRange) -> Unit = {},
    streakInfo: HydrationStreakInfo = HydrationStreakInfo(),
    weeklyHydrationProgress: List<DailyHydrationProgress> = emptyList(),
    todayHydrationEntries: List<HydrationEntryEntity> = emptyList(),
    weeklyWaterLiters: Double = 0.0,
    onAddWater: (Int) -> Unit = {},
    onEditWater: (String, Int) -> Unit = { _, _ -> },
    onDeleteWater: (String) -> Unit = {},
    onSetWaterGoal: (Double) -> Unit = {},
    onBack: () -> Unit
) {
    val locale = LocalLocale.current.platformLocale
    val metricInfo = remember(type) { MetricRegistry.getById(type) }
    val normalizedId = remember(type) { MetricRegistry.normalizeId(type) }
    val color = metricInfo.color

    var editingEntry by remember { mutableStateOf<HydrationEntryEntity?>(null) }
    var showGoalDialog by remember { mutableStateOf(false) }

    val formattedValue = remember(type, metrics, locale) {
        MetricRegistry.formatValue(type, metrics, locale)
    }
    val goalText = remember(type, metrics, locale) {
        MetricRegistry.getGoalText(type, metrics, locale)
    }
    val progressRatio = remember(type, metrics) {
        MetricRegistry.getProgressRatio(type, metrics)
    }
    val summaryStats = remember(type, metrics, selectedRange, weeklyHydrationProgress, locale) {
        MetricRegistry.getSummaryStats(type, metrics, selectedRange, weeklyHydrationProgress, locale)
    }
    val trendPoints = remember(type, metrics, weeklyHydrationProgress) {
        MetricRegistry.getTrendPoints(type, metrics, weeklyHydrationProgress)
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header with Back Navigation
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(6.dp))
                Column(Modifier.weight(1f)) {
                    Text(metricInfo.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (isDemo) "Demo preview data" else "Source: ${metrics.source.label}",
                        color = Muted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Range Selector
        item {
            RangeSelector(selected = selectedRange, onRange = onSelectRange)
        }

        // Hero Metric Card
        item {
            HealthCard(Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = color.copy(alpha = 0.12f),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = metricInfo.category.label.uppercase(locale),
                        color = color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (normalizedId == "Activity") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActivityRings(metrics = metrics, modifier = Modifier.size(112.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column {
                                Text("Move", color = PulsePink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    metrics.activeCalories?.let { "$it / ${metrics.calorieGoal} kcal" } ?: "— / ${metrics.calorieGoal} kcal",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text("Exercise", color = ExerciseGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    metrics.exerciseMinutes?.let { "$it / ${metrics.exerciseGoalMinutes} min" } ?: "— / ${metrics.exerciseGoalMinutes} min",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text("Stand", color = StandCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    metrics.standHours?.let { "$it / ${metrics.standGoalHours} hr" } ?: "— / ${metrics.standGoalHours} hr",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = formattedValue,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (goalText != null) {
                                Text(
                                    text = goalText,
                                    color = Muted,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        if (normalizedId == "Water") {
                            OutlinedButton(
                                onClick = { showGoalDialog = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Edit Goal")
                            }
                        }
                    }

                    if (progressRatio != null) {
                        Spacer(Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = { progressRatio.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape),
                            color = if (progressRatio >= 1.0f) ExerciseGreen else color,
                            trackColor = color.copy(alpha = 0.15f),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        // Quick log buttons for water intake
        if (normalizedId == "Water") {
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
        }

        // Interactive Trend Chart
        item {
            HealthCard(Modifier.fillMaxWidth()) {
                Text(
                    text = "7-DAY TREND",
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                MetricTrendChart(
                    points = trendPoints,
                    primaryColor = color,
                    targetLabel = goalText
                )
            }
        }

        // Summary Statistics Card
        item {
            HealthCard(Modifier.fillMaxWidth()) {
                Text("STATISTICS", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Daily Average", color = Muted, fontSize = 11.sp)
                        Text(summaryStats.averageLabel, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    if (summaryStats.totalLabel != null) {
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("Total", color = Muted, fontSize = 11.sp)
                            Text(summaryStats.totalLabel, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                if (summaryStats.highLabel != null || summaryStats.lowLabel != null) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (summaryStats.highLabel != null) {
                            Column(Modifier.weight(1f)) {
                                Text("High", color = Muted, fontSize = 11.sp)
                                Text(summaryStats.highLabel, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                        if (summaryStats.lowLabel != null) {
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Low", color = Muted, fontSize = 11.sp)
                                Text(summaryStats.lowLabel, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }

                if (summaryStats.completionRateLabel != null) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Status", color = Muted, fontSize = 11.sp)
                            Text(summaryStats.completionRateLabel, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
                        }
                    }
                }
            }
        }

        // Hydration Streak banner (for Water)
        if (normalizedId == "Water" && (streakInfo.currentStreakDays > 0 || streakInfo.isGoalMetToday)) {
            item {
                HealthCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = ExerciseGreen.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = ExerciseGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "${streakInfo.currentStreakDays}-Day Streak 🔥",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Best streak: ${streakInfo.bestStreakDays} days · ${streakInfo.daysMetThisWeek}/7 days met this week",
                                color = Muted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Today's Hydration Entries Log
        if (normalizedId == "Water") {
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
                            text = "Tap a quick log button above to record your water intake.",
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
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    "Delete entry",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Educational Non-Diagnostic Section
        item {
            HealthCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = metricInfo.aboutTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = metricInfo.aboutDescription,
                    color = Muted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        // Data Sources Section
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
