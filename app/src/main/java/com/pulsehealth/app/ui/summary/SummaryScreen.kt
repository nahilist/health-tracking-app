package com.pulsehealth.app.ui.summary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.data.DashboardUiState
import com.pulsehealth.app.data.DataSource
import com.pulsehealth.app.data.FavoriteCardId
import com.pulsehealth.app.data.TimeRange
import com.pulsehealth.app.data.analytics.HealthInsight
import com.pulsehealth.app.data.medication.MedicationAdherenceSummary
import com.pulsehealth.app.data.medication.ScheduledDoseItem
import com.pulsehealth.app.ui.insights.InsightsSection
import com.pulsehealth.app.ui.medication.MedicationScheduleCard
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.components.NetworkStatusPill
import com.pulsehealth.app.ui.theme.AthleticOrange
import com.pulsehealth.app.ui.theme.AthleticOrangeLight
import com.pulsehealth.app.ui.theme.DeepCharcoal
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.PulseTheme
import com.pulsehealth.app.ui.theme.SoftPillBackground
import com.pulsehealth.app.ui.theme.WarmOrange
import com.pulsehealth.app.ui.theme.WaterBlue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SummaryScreen(
    state: DashboardUiState,
    contentPadding: PaddingValues,
    insights: List<HealthInsight> = emptyList(),
    medicationSchedule: List<ScheduledDoseItem> = emptyList(),
    medicationAdherence: MedicationAdherenceSummary = MedicationAdherenceSummary(0, 0, 0, 100, 0),
    onRange: (TimeRange) -> Unit,
    onWater: (Int) -> Unit = {},
    onTakeMedicationDose: (medicationId: String, scheduledTimeEpochMillis: Long) -> Unit = { _, _ -> },
    onSkipMedicationDose: (medicationId: String, scheduledTimeEpochMillis: Long) -> Unit = { _, _ -> },
    onUndoMedicationDose: (medicationId: String, scheduledTimeEpochMillis: Long) -> Unit = { _, _ -> },
    onAddMedicationClick: () -> Unit = {},
    onOpenGoalWizard: () -> Unit = {},
    onEdit: () -> Unit,
    onProfile: () -> Unit,
    onDetail: (String) -> Unit,
    onExitDemo: () -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit
) {
    var showHydrationLogger by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }
    val locale = LocalLocale.current.platformLocale
    val sourceLabel = when (state.metrics.source) {
        DataSource.HEALTH_CONNECT -> "Health Connect"
        DataSource.LOCAL -> "Local records"
        DataSource.DEMO -> "Demo sample data"
        DataSource.NONE -> if (state.hasHealthPermissions) "Health Connect connected" else "No sources connected"
    }
    val freshness = state.freshnessLabel
    val statusText = if (freshness != null && state.metrics.source != DataSource.NONE) {
        "$sourceLabel · $freshness"
    } else {
        sourceLabel
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = contentPadding.calculateTopPadding() + 14.dp,
            bottom = contentPadding.calculateBottomPadding() + 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Modern Athletic Brand Header & Profile Bar (Granger / Modern Health Layout)
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Brand Logo & Wordmark
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = AthleticOrangeLight,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = null,
                                tint = AthleticOrange,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Pulse",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepCharcoal,
                        letterSpacing = (-0.5).sp
                    )
                }

                Spacer(Modifier.weight(1f))

                // Profile Avatar Pod
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onProfile)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Open profile and settings"
                        },
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, Color(0xFFEEF1F4))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val initial = (state.currentUser?.email?.firstOrNull() ?: 'P').uppercaseChar().toString()
                        Text(
                            text = if (state.currentUser != null && !state.currentUser.isAnonymous) initial else "A",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AthleticOrange
                        )
                        if (state.currentUser != null && !state.currentUser.isAnonymous) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(3.dp)
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(if (state.isCloudSyncing) WarmOrange else ExerciseGreen)
                            )
                        }
                    }
                }
            }
        }

        // Personalized Greeting Header: "Hi, [Name]" + "Good Morning"
        item {
            val hour = remember { java.time.LocalTime.now().hour }
            val greeting = when (hour) {
                in 4..11 -> "Good Morning"
                in 12..16 -> "Good Afternoon"
                else -> "Good Evening"
            }
            val userName = state.currentUser?.displayName
                ?: state.currentUser?.email?.substringBefore('@')?.replaceFirstChar { it.uppercase() }
                ?: "Athlete"

            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                Text(
                    text = "Hi, $userName",
                    color = Muted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = greeting,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepCharcoal,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Summary",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepCharcoal
                        )
                        Text(
                            text = statusText,
                            color = Muted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Horizontal Category Pill Filter Bar (as in Image 1)
        item {
            val categories = listOf("All", "Activity", "Vitals", "Heart", "Sleep", "Water", "Meds")
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search circle icon button on left
                item {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFEEF1F4)),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onDetail("Activity") }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search and Browse Metrics",
                                tint = DeepCharcoal,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                items(categories.size) { idx ->
                    val cat = categories[idx]
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) DeepCharcoal else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) DeepCharcoal else Color(0xFFE5E7EB)),
                        shadowElevation = if (isSelected) 2.dp else 0.5.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else DeepCharcoal,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        if (state.isDemoMode) {
            item {
                DemoModeBanner(onExit = onExitDemo)
            }
        }
        if (!state.isOnline || state.isCloudSyncing) {
            item {
                NetworkStatusPill(
                    isOnline = state.isOnline,
                    isSyncing = state.isCloudSyncing
                )
            }
        }
        if (state.errorMessage != null) {
            item {
                ErrorBanner(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    onDismiss = onDismissError
                )
            }
        }

        item { RangeSelector(state.selectedRange, onRange) }

        if (state.isLoading && state.isEmpty) {
            item {
                LoadingStateCard()
            }
        } else {
            item { ActivityCard(state.metrics) { onDetail("Activity") } }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Favorites", Modifier.weight(1f), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Edit",
                        Modifier
                            .clickable(onClick = onEdit)
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        color = Color(0xFF0A84FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item {
                val visibleCards = FavoriteCardId.ALL.filterNot { state.isCardHidden(it) }
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val columns = if (maxWidth >= 600.dp) 4 else 2
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        visibleCards.chunked(columns).forEach { rowCards ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                rowCards.forEach { card ->
                                    Box(Modifier.weight(1f)) {
                                        when (card) {
                                            FavoriteCardId.STEPS -> StepsCard(state.metrics) { onDetail(card.metricRegistryId) }
                                            FavoriteCardId.HEART_RATE -> HeartCard(state.metrics) { onDetail(card.metricRegistryId) }
                                            FavoriteCardId.SLEEP -> SleepCard(state.metrics) { onDetail(card.metricRegistryId) }
                                            FavoriteCardId.WATER -> WaterCard(
                                                metrics = state.metrics,
                                                onAdd = { showHydrationLogger = true },
                                                onClick = { onDetail(card.metricRegistryId) }
                                            )
                                        }
                                    }
                                }
                                val emptySlots = columns - rowCards.size
                                repeat(emptySlots) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            if (insights.isNotEmpty()) {
                item {
                    InsightsSection(
                        insights = insights,
                        onInsightClick = { insight ->
                            onDetail(insight.metricType)
                        }
                    )
                }
            }

            item {
                MedicationScheduleCard(
                    schedule = medicationSchedule,
                    adherence = medicationAdherence,
                    onTakeDose = onTakeMedicationDose,
                    onSkipDose = onSkipMedicationDose,
                    onUndoDose = onUndoMedicationDose,
                    onAddMedicationClick = onAddMedicationClick
                )
            }

            item {
                com.pulsehealth.app.ui.goals.GoalProgressSummaryCard(
                    metrics = state.metrics,
                    onOpenWizard = onOpenGoalWizard
                )
            }

            if (state.weeklyWellnessDigest != null) {
                item { WeeklyDigestCard(digest = state.weeklyWellnessDigest) }
            }

            if (state.weeklyHydrationProgress.isNotEmpty()) {
                item { WeeklyHydrationTrendCard(state) }
            }
        }
    }

    if (showHydrationLogger) {
        LogHydrationBottomSheet(
            currentWaterLiters = state.metrics.waterLiters,
            waterGoalLiters = state.metrics.waterGoalLiters,
            onLogWater = { amountMl ->
                onWater(amountMl)
            },
            onDismiss = { showHydrationLogger = false }
        )
    }
}

@Composable
fun LoadingStateCard() {
    HealthCard(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = PulsePink,
                    strokeWidth = 2.5.dp
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Loading health metrics...",
                    color = Muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ErrorBanner(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    val safeMessage = remember(message) {
        val lower = message.lowercase(java.util.Locale.ROOT)
        if (lower.contains("apikey") || lower.contains("bearer") || lower.contains("header") || lower.contains("url:") || lower.contains("http")) {
            "Unable to synchronize records. Please verify your connection and try again."
        } else {
            message.substringBefore("\n").substringBefore("URL:").trim()
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Sync notification",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = safeMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onRetry, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Retry",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SourceAndFreshnessBadge(state: DashboardUiState) {
    val sourceLabel = when (state.metrics.source) {
        DataSource.HEALTH_CONNECT -> "Health Connect"
        DataSource.LOCAL -> "Local records"
        DataSource.DEMO -> "Demo sample data"
        DataSource.NONE -> if (state.hasHealthPermissions) "Health Connect connected" else "No sources connected"
    }
    val freshness = state.freshnessLabel
    val statusText = if (freshness != null && state.metrics.source != DataSource.NONE) {
        "$sourceLabel · $freshness"
    } else {
        sourceLabel
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Text(
            text = statusText,
            color = Muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DemoModeBanner(onExit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, null, tint = WarmOrange, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Demo preview data",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                "Exit",
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onExit)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = PulsePink,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun RangeSelector(selected: TimeRange, onRange: (TimeRange) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SoftPillBackground)
            .padding(4.dp)
    ) {
        TimeRange.entries.forEach { range ->
            val isCurrentRange = selected == range
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onRange(range) }
                    .semantics {
                        role = Role.Tab
                        this.selected = isCurrentRange
                        contentDescription = "${range.label} range"
                    },
                shape = RoundedCornerShape(16.dp),
                color = if (isCurrentRange) Color.White else Color.Transparent,
                shadowElevation = if (isCurrentRange) 2.dp else 0.dp
            ) {
                Text(
                    range.label,
                    Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    fontWeight = if (isCurrentRange) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCurrentRange) DeepCharcoal else Muted
                )
            }
        }
    }
}

@Composable
fun InsightCard(state: DashboardUiState) {
    val locale = LocalLocale.current.platformLocale
    val steps = state.metrics.steps
    val water = state.metrics.waterLiters
    val goal = state.metrics.stepGoal
    HealthCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Insights, null, tint = ExerciseGreen, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                when {
                    steps != null && steps >= goal -> "Goal achieved"
                    steps != null -> "Daily progress"
                    water > 0.0 -> "Hydration logged"
                    else -> "Data & privacy"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            when {
                steps != null && steps >= goal -> "You reached your step goal of ${String.format(locale, "%,d", goal)} steps today!"
                steps != null -> "${String.format(locale, "%,d", goal - steps)} steps remaining to reach your goal of ${String.format(locale, "%,d", goal)} steps."
                water > 0.0 -> "You have logged ${String.format(locale, "%.1f", water)} L of water today."
                state.hasHealthPermissions -> "Health Connect is connected. Sync to view your daily metrics."
                state.healthConnectAvailable -> "Connect Health Connect in Profile to see your steps, heart rate, and sleep."
                else -> "All health data is stored locally on this device."
            },
            color = Muted,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
}

@Composable
fun HydrationStreakBanner(state: DashboardUiState) {
    val streak = state.streakInfo
    val isMetToday = streak.isGoalMetToday
    val currentStreak = streak.currentStreakDays

    if (currentStreak == 0 && !isMetToday) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMetToday) ExerciseGreen.copy(alpha = 0.12f) else PulsePink.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            1.dp,
            if (isMetToday) ExerciseGreen.copy(alpha = 0.3f) else PulsePink.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isMetToday) Icons.Default.EmojiEvents else Icons.Default.Whatshot,
                contentDescription = null,
                tint = if (isMetToday) ExerciseGreen else PulsePink,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (isMetToday) "Daily Goal Achieved! 🎉" else "$currentStreak-Day Goal Streak 🔥",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isMetToday) {
                        "You reached your ${String.format(java.util.Locale.getDefault(), "%.1f", state.metrics.waterGoalLiters)}L goal!"
                    } else {
                        val remaining = (state.metrics.waterGoalLiters - state.metrics.waterLiters).coerceAtLeast(0.0)
                        "${String.format(java.util.Locale.getDefault(), "%.1f", remaining)}L remaining today"
                    },
                    fontSize = 12.sp,
                    color = Muted
                )
            }
        }
    }
}

@Composable
fun WeeklyHydrationTrendCard(state: DashboardUiState) {
    if (state.weeklyHydrationProgress.isEmpty()) return

    HealthCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = WaterBlue, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Weekly Hydration",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(Modifier.weight(1f))
            val streak = state.streakInfo.currentStreakDays
            if (streak > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PulsePink.copy(alpha = 0.12f)
                ) {
                    Text(
                        "$streak-day streak 🔥",
                        color = PulsePink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            } else {
                Text(
                    "${state.streakInfo.daysMetThisWeek}/7 days met",
                    color = if (state.streakInfo.daysMetThisWeek >= 4) ExerciseGreen else Muted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth().height(95.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            state.weeklyHydrationProgress.forEach { day ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).padding(horizontal = 2.5.dp)
                ) {
                    val ratio = day.progressRatio
                    val barHeightDp = (ratio.coerceIn(0.08f, 1.0f) * 54).dp

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(54.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        )
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(barHeightDp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (day.isGoalAchieved) ExerciseGreen else WaterBlue
                                )
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = day.dayLabel,
                        fontSize = 10.sp,
                        fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (day.isToday) PulsePink else Muted
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Target: ${String.format(java.util.Locale.getDefault(), "%.1f", state.metrics.waterGoalLiters)} L/day",
                fontSize = 11.sp,
                color = Muted
            )
            if (state.streakInfo.bestStreakDays > 0) {
                Text(
                    "Best: ${state.streakInfo.bestStreakDays} days",
                    fontSize = 11.sp,
                    color = Muted
                )
            }
        }
    }
}

@Preview(name = "Dashboard Real Empty State", showBackground = true)
@Composable
private fun SummaryScreenEmptyPreview() {
    PulseTheme {
        SummaryScreen(
            state = DashboardUiState(),
            contentPadding = PaddingValues(),
            onRange = {},
            onWater = {},
            onEdit = {},
            onProfile = {},
            onDetail = {},
            onExitDemo = {},
            onRetry = {},
            onDismissError = {}
        )
    }
}

@Preview(name = "Dashboard Error State", showBackground = true)
@Composable
private fun SummaryScreenErrorPreview() {
    PulseTheme {
        SummaryScreen(
            state = DashboardUiState(
                errorMessage = "Could not connect to Health Connect"
            ),
            contentPadding = PaddingValues(),
            onRange = {},
            onWater = {},
            onEdit = {},
            onProfile = {},
            onDetail = {},
            onExitDemo = {},
            onRetry = {},
            onDismissError = {}
        )
    }
}
