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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MonitorHeart
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
import com.pulsehealth.app.data.WorkoutType
import com.pulsehealth.app.data.local.DailyHealthSummaryEntity
import com.pulsehealth.app.data.local.HydrationEntryEntity
import com.pulsehealth.app.data.local.WorkoutSessionEntity
import com.pulsehealth.app.data.local.SleepSessionWithStages
import com.pulsehealth.app.data.local.VitalRecordEntity
import com.pulsehealth.app.data.VitalType
import com.pulsehealth.app.data.MeasurementContext
import com.pulsehealth.app.data.BloodPressureClassification
import com.pulsehealth.app.data.HeartRateClassification
import com.pulsehealth.app.data.analytics.HealthInsight
import com.pulsehealth.app.ui.cardio.CardiovascularZonesCard
import com.pulsehealth.app.ui.insights.HealthInsightCard
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.components.MetricTrendChart
import com.pulsehealth.app.ui.summary.ActivityRings
import com.pulsehealth.app.ui.summary.LogWorkoutBottomSheet
import com.pulsehealth.app.ui.summary.RangeSelector
import com.pulsehealth.app.ui.vitals.LogVitalBottomSheet
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
    dailySummaries: List<DailyHealthSummaryEntity> = emptyList(),
    todayWorkouts: List<WorkoutSessionEntity> = emptyList(),
    todaySleepSession: SleepSessionWithStages? = null,
    vitalRecords: List<VitalRecordEntity> = emptyList(),
    insights: List<HealthInsight> = emptyList(),
    onAddWater: (Int) -> Unit = {},
    onEditWater: (String, Int) -> Unit = { _, _ -> },
    onDeleteWater: (String) -> Unit = {},
    onSetWaterGoal: (Double) -> Unit = {},
    onLogWorkout: (WorkoutType, String?, Long, Int?, Double?, String?) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteWorkout: (String) -> Unit = {},
    onLogBloodPressure: (systolic: Int, diastolic: Int, bpm: Int?, context: MeasurementContext, notes: String?) -> Unit = { _, _, _, _, _ -> },
    onLogHeartRate: (bpm: Int, context: MeasurementContext, notes: String?) -> Unit = { _, _, _ -> },
    onDeleteVital: (String) -> Unit = {},
    onBack: () -> Unit
) {
    val locale = LocalLocale.current.platformLocale
    val metricInfo = remember(type) { MetricRegistry.getById(type) }
    val normalizedId = remember(type) { MetricRegistry.normalizeId(type) }
    val color = metricInfo.color

    var editingEntry by remember { mutableStateOf<HydrationEntryEntity?>(null) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showWorkoutSheet by remember { mutableStateOf(false) }
    var showLogVitalSheet by remember { mutableStateOf(false) }
    var vitalSheetInitialType by remember { mutableStateOf(VitalType.BLOOD_PRESSURE) }
    var deletingVitalId by remember { mutableStateOf<String?>(null) }

    val formattedValue = remember(type, metrics, locale) {
        MetricRegistry.formatValue(type, metrics, locale)
    }
    val goalText = remember(type, metrics, locale) {
        MetricRegistry.getGoalText(type, metrics, locale)
    }
    val latestBpRecord = remember(vitalRecords) {
        vitalRecords.firstOrNull { it.vitalType == VitalType.BLOOD_PRESSURE.name }
    }
    val latestHrRecord = remember(vitalRecords) {
        vitalRecords.firstOrNull { it.vitalType == VitalType.HEART_RATE.name }
    }

    val displayValue = remember(type, metrics, latestBpRecord, latestHrRecord, locale, formattedValue) {
        when (normalizedId) {
            "Blood pressure" -> latestBpRecord?.let { "${it.systolic} / ${it.diastolic} mmHg" } ?: "120 / 80 mmHg"
            "Heart rate" -> metrics.heartRate?.let { "$it bpm" } ?: latestHrRecord?.heartRateBpm?.let { "$it bpm" } ?: "No data"
            else -> formattedValue
        }
    }

    val displayGoalText = remember(type, metrics, latestBpRecord, goalText) {
        when (normalizedId) {
            "Blood pressure" -> {
                if (latestBpRecord?.systolic != null && latestBpRecord.diastolic != null) {
                    val cat = BloodPressureClassification.classify(latestBpRecord.systolic, latestBpRecord.diastolic)
                    "${cat.label} · Target: <120/<80"
                } else {
                    goalText
                }
            }
            else -> goalText
        }
    }

    val progressRatio = remember(type, metrics) {
        MetricRegistry.getProgressRatio(type, metrics)
    }
    val summaryStats = remember(type, metrics, selectedRange, weeklyHydrationProgress, dailySummaries, locale) {
        MetricRegistry.getSummaryStats(type, metrics, selectedRange, weeklyHydrationProgress, locale, dailySummaries)
    }
    val trendPoints = remember(type, metrics, weeklyHydrationProgress, dailySummaries) {
        MetricRegistry.getTrendPoints(type, metrics, weeklyHydrationProgress, dailySummaries)
    }

    val metricInsights = remember(insights, type) {
        val targetName = when (type.lowercase()) {
            "heart rate", "resting heart rate" -> "resting heart rate"
            "blood pressure" -> "blood pressure"
            "sleep" -> "sleep"
            "steps" -> "steps"
            "hydration", "water" -> "hydration"
            else -> type.lowercase()
        }
        insights.filter { it.metricType.lowercase() == targetName }
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
                                text = displayValue,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (displayGoalText != null) {
                                Text(
                                    text = displayGoalText,
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

        // Quick log button for workouts / exercise
        if (normalizedId == "Exercise" || normalizedId == "Activity") {
            item {
                HealthCard(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("LOG WORKOUT", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Record running, cycling, strength, yoga or custom exercise",
                                color = Muted,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        FilledTonalButton(
                            onClick = { showWorkoutSheet = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = WarmOrange.copy(alpha = 0.15f),
                                contentColor = WarmOrange
                            )
                        ) {
                            Text("+ Log Workout", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick log button for Blood Pressure
        if (normalizedId == "Blood pressure") {
            item {
                HealthCard(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("LOG BLOOD PRESSURE", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Record systolic and diastolic pressure readings",
                                color = Muted,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        FilledTonalButton(
                            onClick = {
                                vitalSheetInitialType = VitalType.BLOOD_PRESSURE
                                showLogVitalSheet = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = PulsePink.copy(alpha = 0.15f),
                                contentColor = PulsePink
                            )
                        ) {
                            Text("+ Log Vital", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick log button for Heart Rate
        if (normalizedId == "Heart rate") {
            item {
                HealthCard(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("LOG HEART RATE", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Record resting or post-exercise pulse beats per minute",
                                color = Muted,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        FilledTonalButton(
                            onClick = {
                                vitalSheetInitialType = VitalType.HEART_RATE
                                showLogVitalSheet = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = PulsePink.copy(alpha = 0.15f),
                                contentColor = PulsePink
                            )
                        ) {
                            Text("+ Log Pulse", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Relevant Metric Insights
        if (metricInsights.isNotEmpty()) {
            items(metricInsights, key = { it.id }) { insight ->
                HealthInsightCard(insight = insight)
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

        // Deep Sleep Staging, Hypnogram & Restfulness (for Sleep)
        if (normalizedId == "Sleep") {
            item {
                SleepHypnogramCard(
                    sessionWithStages = todaySleepSession,
                    isDemo = isDemo
                )
            }
            item {
                SleepStageBreakdownCard(
                    sessionWithStages = todaySleepSession,
                    isDemo = isDemo
                )
            }
            item {
                SleepEfficiencyCard(
                    sessionWithStages = todaySleepSession,
                    isDemo = isDemo
                )
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
                item {
                    HealthCard(
                        Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        todayHydrationEntries.forEachIndexed { index, entry ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(start = 66.dp)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = WaterBlue.copy(alpha = 0.12f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.LocalDrink,
                                            contentDescription = null,
                                            tint = WaterBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "${entry.amountMilliliters} mL",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
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
                                    Icon(Icons.Default.Edit, "Edit entry", tint = Muted, modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { onDeleteWater(entry.id) }) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        "Delete entry",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Today's Workouts List (for Exercise & Activity)
        if (normalizedId == "Exercise" || normalizedId == "Activity") {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Today's Workouts (${todayWorkouts.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (todayWorkouts.isEmpty()) {
                item {
                    HealthCard(Modifier.fillMaxWidth()) {
                        Text(
                            text = "No workout sessions recorded today.",
                            color = Muted,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Log Workout' above to record an exercise session or sync with Health Connect.",
                            color = Muted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                item {
                    HealthCard(
                        Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        todayWorkouts.forEachIndexed { index, session ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(start = 66.dp)
                                )
                            }
                            val wType = WorkoutType.fromString(session.workoutType)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = WarmOrange.copy(alpha = 0.12f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            wType.icon,
                                            contentDescription = null,
                                            tint = WarmOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = session.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        val isHc = session.source.lowercase().contains("health")
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isHc) ExerciseGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = if (isHc) "Health Connect" else "Manual",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isHc) ExerciseGreen else Muted,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    val timeStr = Instant.ofEpochMilli(session.startTimeEpochMillis)
                                        .atZone(ZoneId.systemDefault())
                                        .format(DateTimeFormatter.ofPattern("h:mm a", locale))
                                    val detailsText = buildString {
                                        append("${session.durationMinutes} min")
                                        session.activeCalories?.let { append(" · $it kcal") }
                                        session.distanceMeters?.let {
                                            val km = it / 1000.0
                                            append(" · %.1f km".format(locale, km))
                                        }
                                        append(" · $timeStr")
                                    }
                                    Text(
                                        text = detailsText,
                                        color = Muted,
                                        fontSize = 12.sp
                                    )
                                }
                                if (!session.source.lowercase().contains("health")) {
                                    IconButton(onClick = { onDeleteWorkout(session.id) }) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            "Delete workout",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Blood Pressure Readings History
        if (normalizedId == "Blood pressure") {
            val bpRecords = vitalRecords.filter { it.vitalType == VitalType.BLOOD_PRESSURE.name }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Recent Blood Pressure Readings (${bpRecords.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (bpRecords.isEmpty()) {
                item {
                    HealthCard(Modifier.fillMaxWidth()) {
                        Text(
                            text = "No blood pressure records logged yet.",
                            color = Muted,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Log Vital' above to record systolic and diastolic measurements.",
                            color = Muted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                item {
                    HealthCard(
                        Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        bpRecords.forEachIndexed { index, record ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(start = 66.dp)
                                )
                            }
                            val category = BloodPressureClassification.classify(
                                record.systolic ?: 120,
                                record.diastolic ?: 80
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = category.color.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.MonitorHeart,
                                            contentDescription = null,
                                            tint = category.color,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${record.systolic ?: "—"} / ${record.diastolic ?: "—"} mmHg",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Surface(
                                              shape = RoundedCornerShape(6.dp),
                                              color = category.color.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = category.label,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = category.color,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    val timeStr = Instant.ofEpochMilli(record.recordedAtEpochMillis)
                                        .atZone(ZoneId.systemDefault())
                                        .format(DateTimeFormatter.ofPattern("MMM d, h:mm a", locale))
                                    val detailsText = buildString {
                                        append(record.measurementContext.lowercase(locale).replaceFirstChar { it.uppercase(locale) })
                                        record.heartRateBpm?.let { append(" · $it bpm pulse") }
                                        record.notes?.let { append(" · $it") }
                                        append(" · $timeStr")
                                    }
                                    Text(
                                        text = detailsText,
                                        color = Muted,
                                        fontSize = 12.sp
                                    )
                                }
                                IconButton(onClick = { deletingVitalId = record.id }) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete record",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Heart Rate Readings History
        if (normalizedId == "Heart rate") {
            val hrRecords = vitalRecords.filter { it.vitalType == VitalType.HEART_RATE.name }
            val latestBpm: Int = hrRecords.firstOrNull()?.heartRateBpm ?: metrics.heartRate?.toInt() ?: 68
            val readingValues = hrRecords.mapNotNull { it.heartRateBpm }

            item {
                CardiovascularZonesCard(
                    currentRestingBpm = latestBpm,
                    activeMinutesToday = metrics.exerciseMinutes?.toInt() ?: 30,
                    recentHeartRateReadings = readingValues
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Recent Heart Rate Readings (${hrRecords.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (hrRecords.isEmpty()) {
                item {
                    HealthCard(Modifier.fillMaxWidth()) {
                        Text(
                            text = "No manual heart rate records logged yet.",
                            color = Muted,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Log Pulse' above to record resting or post-exercise heart rate readings.",
                            color = Muted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                item {
                    HealthCard(
                        Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        hrRecords.forEachIndexed { index, record ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(start = 66.dp)
                                )
                            }
                            val bpm = record.heartRateBpm ?: 72
                            val status = HeartRateClassification.classifyRestingBpm(bpm)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = PulsePink.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Favorite,
                                            contentDescription = null,
                                            tint = PulsePink,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$bpm bpm",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = PulsePink.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = status,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PulsePink,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    val timeStr = Instant.ofEpochMilli(record.recordedAtEpochMillis)
                                        .atZone(ZoneId.systemDefault())
                                        .format(DateTimeFormatter.ofPattern("MMM d, h:mm a", locale))
                                    val detailsText = buildString {
                                        append(record.measurementContext.lowercase(locale).replaceFirstChar { it.uppercase(locale) })
                                        record.notes?.let { append(" · $it") }
                                        append(" · $timeStr")
                                    }
                                    Text(
                                        text = detailsText,
                                        color = Muted,
                                        fontSize = 12.sp
                                    )
                                }
                                IconButton(onClick = { deletingVitalId = record.id }) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete record",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
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

    if (showWorkoutSheet) {
        LogWorkoutBottomSheet(
            onLogWorkout = onLogWorkout,
            onDismiss = { showWorkoutSheet = false }
        )
    }

    if (showLogVitalSheet) {
        LogVitalBottomSheet(
            initialType = vitalSheetInitialType,
            onLogBloodPressure = onLogBloodPressure,
            onLogHeartRate = onLogHeartRate,
            onDismiss = { showLogVitalSheet = false }
        )
    }

    if (deletingVitalId != null) {
        AlertDialog(
            onDismissRequest = { deletingVitalId = null },
            title = { Text("Delete Vital Record?") },
            text = { Text("Are you sure you want to delete this recorded vital measurement?") },
            confirmButton = {
                Button(
                    onClick = {
                        deletingVitalId?.let { onDeleteVital(it) }
                        deletingVitalId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", color = androidx.compose.ui.graphics.Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingVitalId = null }) {
                    Text("Cancel")
                }
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
