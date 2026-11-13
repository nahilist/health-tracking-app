@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.pulsehealth.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.data.DashboardUiState
import com.pulsehealth.app.data.DataSource
import com.pulsehealth.app.data.TimeRange
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.PulseTheme
import com.pulsehealth.app.ui.theme.SleepPurple
import com.pulsehealth.app.ui.theme.StandCyan
import com.pulsehealth.app.ui.theme.WarmOrange
import com.pulsehealth.app.ui.theme.WaterBlue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private enum class AppTab(val label: String, val icon: ImageVector) {
    SUMMARY("Summary", Icons.Default.Favorite),
    SHARING("Sharing", Icons.Default.Groups),
    BROWSE("Browse", Icons.Default.GridView)
}

@Composable
fun PulseHealthApp(viewModel: DashboardViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(AppTab.SUMMARY) }
    var detail by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = viewModel.healthRepository.permissionContract()
    ) { viewModel.refreshPermissionsAndSync() }

    when {
        profile -> ProfileScreen(
            state = state,
            onBack = { profile = false },
            onConnect = { permissionLauncher.launch(viewModel.healthRepository.permissions) },
            onSync = viewModel::sync,
            onToggleDemo = viewModel::toggleDemoMode
        )
        detail != null -> DetailScreen(
            type = detail.orEmpty(),
            metrics = state.metrics,
            isDemo = state.isDemoMode,
            onBack = { detail = null }
        )
        else -> Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                PulseBottomBar(selected = tab, onSelected = { tab = it })
            }
        ) { padding ->
            when (tab) {
                AppTab.SUMMARY -> SummaryScreen(
                    state = state,
                    contentPadding = padding,
                    onRange = viewModel::selectRange,
                    onWater = viewModel::addWater,
                    onEdit = { editing = true },
                    onProfile = { profile = true },
                    onDetail = { detail = it },
                    onExitDemo = { viewModel.setDemoMode(false) }
                )
                AppTab.SHARING -> EmptyStateScreen(
                    padding, Icons.Default.Groups, "Health sharing",
                    "Share selected trends with people you trust. Your health data stays private until you choose otherwise."
                )
                AppTab.BROWSE -> BrowseScreen(padding) { detail = it }
            }
        }
    }

    if (editing) {
        FavoritesSheet(state.hiddenCards, viewModel::toggleCard) { editing = false }
    }
}

@Composable
private fun SummaryScreen(
    state: DashboardUiState,
    contentPadding: PaddingValues,
    onRange: (TimeRange) -> Unit,
    onWater: () -> Unit,
    onEdit: () -> Unit,
    onProfile: () -> Unit,
    onDetail: (String) -> Unit,
    onExitDemo: () -> Unit
) {
    val locale = LocalLocale.current.platformLocale
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp, top = contentPadding.calculateTopPadding() + 14.dp,
            bottom = contentPadding.calculateBottomPadding() + 18.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", locale)).uppercase(locale),
                        color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold
                    )
                    Text("Summary", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                }
                Surface(
                    modifier = Modifier.size(44.dp).clickable(onClick = onProfile),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface
                ) { Icon(Icons.Default.Person, "Profile", Modifier.padding(11.dp), tint = Muted) }
            }
        }
        item {
            SourceAndFreshnessBadge(state)
        }
        if (state.isDemoMode) {
            item {
                DemoModeBanner(onExit = onExitDemo)
            }
        }
        item { RangeSelector(state.selectedRange, onRange) }
        item { ActivityCard(state.metrics) { onDetail("Activity") } }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Favorites", Modifier.weight(1f), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("Edit", Modifier.clickable(onClick = onEdit).padding(8.dp), color = Color(0xFF087CF0), fontSize = 14.sp)
            }
        }
        item {
            val visible = listOf("Steps", "Heart rate", "Sleep", "Water").filterNot { it in state.hiddenCards }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                visible.chunked(2).forEach { rowCards ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowCards.forEach { id ->
                            Box(Modifier.weight(1f)) {
                                when (id) {
                                    "Steps" -> StepsCard(state.metrics) { onDetail(id) }
                                    "Heart rate" -> HeartCard(state.metrics) { onDetail(id) }
                                    "Sleep" -> SleepCard(state.metrics) { onDetail(id) }
                                    "Water" -> WaterCard(state.metrics, onWater) { onDetail(id) }
                                }
                            }
                        }
                        if (rowCards.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        item { InsightCard(state) }
        item {
            AnimatedVisibility(state.message != null) {
                Text(state.message.orEmpty(), color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun SourceAndFreshnessBadge(state: DashboardUiState) {
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
private fun DemoModeBanner(onExit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
        border = BorderStroke(1.dp, Color(0xFFFFEEBA))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF856404), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("DEMO MODE", color = Color(0xFF856404), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Showing sample health data for preview. Not real records.", color = Color(0xFF856404), fontSize = 12.sp)
            }
            Text(
                "Exit",
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onExit)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                color = Color(0xFF856404),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun RangeSelector(selected: TimeRange, onRange: (TimeRange) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(3.dp)
    ) {
        TimeRange.entries.forEach { range ->
            Surface(
                modifier = Modifier.weight(1f).clickable { onRange(range) },
                shape = RoundedCornerShape(10.dp),
                color = if (selected == range) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (selected == range) 1.dp else 0.dp
            ) {
                Text(
                    range.label, Modifier.padding(vertical = 8.dp), textAlign = TextAlign.Center, fontSize = 13.sp,
                    fontWeight = if (selected == range) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected == range) MaterialTheme.colorScheme.onSurface else Muted
                )
            }
        }
    }
}

@Composable
private fun ActivityCard(metrics: DashboardMetrics, onClick: () -> Unit) {
    HealthCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text("Activity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    if (metrics.activeCalories != null || metrics.exerciseMinutes != null || metrics.standHours != null) {
                        "Daily movement and exercise"
                    } else {
                        "No activity recorded today"
                    },
                    color = Muted,
                    fontSize = 12.sp
                )
            }
            Icon(Icons.Default.ChevronRight, "Open activity", tint = Muted)
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ActivityRings(metrics, Modifier.size(112.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                ActivityValue(
                    "Move",
                    metrics.activeCalories?.let { "$it / ${metrics.calorieGoal} kcal" } ?: "— / ${metrics.calorieGoal} kcal",
                    PulsePink
                )
                Row {
                    Box(Modifier.weight(1f)) {
                        ActivityValue(
                            "Exercise",
                            metrics.exerciseMinutes?.let { "$it / ${metrics.exerciseGoalMinutes} min" } ?: "— / ${metrics.exerciseGoalMinutes} min",
                            ExerciseGreen
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        ActivityValue(
                            "Stand",
                            metrics.standHours?.let { "$it / ${metrics.standGoalHours} hr" } ?: "— / ${metrics.standGoalHours} hr",
                            StandCyan
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityRings(metrics: DashboardMetrics, modifier: Modifier = Modifier) {
    val progresses = listOf(
        (metrics.activeCalories?.toFloat()?.div(metrics.calorieGoal) ?: 0f) to PulsePink,
        (metrics.exerciseMinutes?.toFloat()?.div(metrics.exerciseGoalMinutes) ?: 0f) to ExerciseGreen,
        (metrics.standHours?.toFloat()?.div(metrics.standGoalHours) ?: 0f) to StandCyan
    )
    Canvas(modifier.semantics { contentDescription = "Activity progress rings" }) {
        val stroke = size.minDimension * .105f
        progresses.forEachIndexed { index, pair ->
            val inset = stroke / 2 + index * stroke * 1.35f
            drawArc(
                pair.second.copy(alpha = .16f),
                -90f,
                360f,
                false,
                Offset(inset, inset),
                Size(size.width - inset * 2, size.height - inset * 2),
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
            if (pair.first > 0f) {
                drawArc(
                    pair.second,
                    -90f,
                    pair.first.coerceIn(0f, 1f) * 360f,
                    false,
                    Offset(inset, inset),
                    Size(size.width - inset * 2, size.height - inset * 2),
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
private fun ActivityValue(label: String, value: String, color: Color) {
    Column {
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StepsCard(metrics: DashboardMetrics, onClick: () -> Unit) {
    val locale = LocalLocale.current.platformLocale
    MetricCard("Steps", Icons.AutoMirrored.Filled.DirectionsRun, WarmOrange, onClick) {
        val steps = metrics.steps
        if (steps != null) {
            Text(String.format(locale, "%,d", steps), fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Text("of ${String.format(locale, "%,d", metrics.stepGoal)} steps", color = Muted, fontSize = 11.sp)
            MiniBars(WarmOrange, listOf(.25f,.4f,.33f,.7f,.5f,.86f,.65f,.48f,.72f,.55f,.9f,.61f,1f))
        } else {
            Text("—", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Muted)
            Text("No steps recorded", color = Muted, fontSize = 11.sp)
            EmptyMetricPlaceholder()
        }
    }
}

@Composable
private fun HeartCard(metrics: DashboardMetrics, onClick: () -> Unit) {
    MetricCard("Heart rate", Icons.Default.Favorite, PulsePink, onClick) {
        val hr = metrics.heartRate
        if (hr != null) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$hr", fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text(" bpm", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 5.dp))
            }
            Text("Resting", color = Muted, fontSize = 11.sp)
            Sparkline(PulsePink, listOf(.25f,.35f,.28f,.52f,.39f,.34f,.66f,.45f,.58f,.4f,.5f,.7f,.61f,.76f))
        } else {
            Text("—", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Muted)
            Text("No reading available", color = Muted, fontSize = 11.sp)
            EmptyMetricPlaceholder()
        }
    }
}

@Composable
private fun SleepCard(metrics: DashboardMetrics, onClick: () -> Unit) {
    MetricCard("Sleep", Icons.Default.NightsStay, SleepPurple, onClick) {
        val sleep = metrics.sleepMinutes
        if (sleep != null) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${sleep / 60}", fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text(" hr  ", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 5.dp))
                Text("${sleep % 60}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(" min", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 5.dp))
            }
            Text("Total sleep time", color = Muted, fontSize = 10.sp)
            SleepBlocks()
        } else {
            Text("—", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Muted)
            Text("No sleep recorded", color = Muted, fontSize = 11.sp)
            EmptyMetricPlaceholder()
        }
    }
}

@Composable
private fun WaterCard(metrics: DashboardMetrics, onAdd: () -> Unit, onClick: () -> Unit) {
    val locale = LocalLocale.current.platformLocale
    MetricCard("Water", Icons.Default.LocalDrink, WaterBlue, onClick) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(String.format(locale, "%.1f", metrics.waterLiters), fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Text(" L", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 5.dp))
        }
        Text("of ${metrics.waterGoalLiters.toInt()} L daily goal", color = Muted, fontSize = 11.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            val progress by animateFloatAsState(
                (metrics.waterLiters / metrics.waterGoalLiters).toFloat().coerceIn(0f, 1f),
                label = "water"
            )
            Box(Modifier.weight(1f).height(8.dp).clip(CircleShape).background(WaterBlue.copy(alpha = .15f))) {
                Box(Modifier.fillMaxWidth(progress).height(8.dp).clip(CircleShape).background(WaterBlue))
            }
            IconButton(onClick = onAdd, modifier = Modifier.size(36.dp)) {
                Surface(shape = CircleShape, color = WaterBlue.copy(alpha = .1f)) {
                    Icon(Icons.Default.Add, "Add 250 milliliters", Modifier.padding(6.dp), tint = WaterBlue)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    HealthCard(Modifier.fillMaxWidth().height(154.dp).clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(5.dp))
            Text(title, color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(7.dp))
        content()
    }
}

@Composable
private fun EmptyMetricPlaceholder() {
    Row(
        Modifier.fillMaxWidth().height(36.dp).padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(7) {
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Muted.copy(alpha = 0.25f))
            )
        }
    }
}

@Composable
private fun HealthCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun MiniBars(color: Color, values: List<Float>) {
    Row(Modifier.fillMaxWidth().height(36.dp).padding(top = 7.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
        values.forEachIndexed { i, value -> Box(Modifier.weight(1f).fillMaxSize(value).background(color.copy(alpha = if (i > values.lastIndex - 3) 1f else .45f), RoundedCornerShape(2.dp))) }
    }
}

@Composable
private fun Sparkline(color: Color, values: List<Float>) {
    Canvas(Modifier.fillMaxWidth().height(36.dp).padding(top = 9.dp)) {
        values.zipWithNext().forEachIndexed { index, pair ->
            val x1 = index * size.width / (values.size - 1)
            val x2 = (index + 1) * size.width / (values.size - 1)
            drawLine(color, Offset(x1, size.height * (1 - pair.first)), Offset(x2, size.height * (1 - pair.second)), strokeWidth = 2.5f)
        }
    }
}

@Composable
private fun SleepBlocks() {
    Row(Modifier.fillMaxWidth().height(38.dp).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf(.65f,.42f,.8f,.55f,.88f,.7f,.38f,.74f,.58f).forEach { value ->
            Box(Modifier.weight(1f).height((28 * value).dp).background(SleepPurple.copy(alpha = .75f), RoundedCornerShape(3.dp)))
        }
    }
}

@Composable
private fun InsightCard(state: DashboardUiState) {
    val locale = LocalLocale.current.platformLocale
    if (state.isDemoMode) {
        HealthCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Insights, null, tint = Color(0xFF159B72), modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sample Insight (Demo)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(Modifier.height(9.dp))
            Text("Sample data active. When real metrics are synced, personalized trends will appear here.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(9.dp))
            Surface(shape = CircleShape, color = Color(0xFFE3F7EF)) {
                Text("Demo Preview", Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = Color(0xFF159B72), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    } else {
        val steps = state.metrics.steps
        val water = state.metrics.waterLiters
        val goal = state.metrics.stepGoal
        HealthCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Insights, null, tint = Color(0xFF159B72), modifier = Modifier.size(19.dp))
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
            Spacer(Modifier.height(9.dp))
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
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun PulseBottomBar(selected: AppTab, onSelected: (AppTab) -> Unit) {
    Surface(tonalElevation = 4.dp, shadowElevation = 12.dp, color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().height(70.dp).padding(horizontal = 18.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceAround) {
            AppTab.entries.forEach { item ->
                val active = selected == item
                Column(
                    Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).clickable { onSelected(item) }.background(if (active) PulsePink.copy(alpha = .11f) else Color.Transparent).padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(item.icon, item.label, tint = if (active) PulsePink else Muted, modifier = Modifier.size(22.dp))
                    Text(item.label, color = if (active) PulsePink else Muted, fontSize = 10.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
private fun FavoritesSheet(hidden: Set<String>, onToggle: (String) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
            Text("Edit favorites", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Choose the metrics shown on your Summary.", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))
            listOf("Steps", "Heart rate", "Sleep", "Water").forEach { name ->
                Row(Modifier.fillMaxWidth().clickable { onToggle(name) }.padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(name, Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Text(if (name in hidden) "Add" else "Remove", color = if (name in hidden) Color(0xFF087CF0) else PulsePink)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .35f))
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    state: DashboardUiState,
    onBack: () -> Unit,
    onConnect: () -> Unit,
    onSync: () -> Unit,
    onToggleDemo: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Text("Profile & data", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(22.dp))
        HealthCard(Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Person, null, tint = PulsePink, modifier = Modifier.size(42.dp))
            Spacer(Modifier.height(10.dp))
            Text("Your health, under your control", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Pulse Health stores preferences only on this device and reads health metrics through Android Health Connect.", color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
        }
        Spacer(Modifier.height(16.dp))
        HealthCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MonitorHeart, null, tint = PulsePink)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Health Connect", fontWeight = FontWeight.Bold)
                    Text(when {
                        !state.healthConnectAvailable -> "Not available on this device"
                        state.hasHealthPermissions -> "Connected and ready"
                        else -> "Permission required"
                    }, color = Muted, fontSize = 12.sp)
                }
                if (state.isSyncing) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            }
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = if (state.hasHealthPermissions) onSync else onConnect,
                enabled = state.healthConnectAvailable && !state.isSyncing,
                colors = ButtonDefaults.buttonColors(containerColor = PulsePink)
            ) {
                Icon(if (state.hasHealthPermissions) Icons.Default.Sync else Icons.Default.Settings, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (state.hasHealthPermissions) "Sync now" else "Connect")
            }
        }
        Spacer(Modifier.height(16.dp))
        HealthCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, null, tint = Muted)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Sample Demo Mode", fontWeight = FontWeight.Bold)
                    Text("Display sample data to preview UI layouts without real health records.", color = Muted, fontSize = 12.sp)
                }
                Switch(
                    checked = state.isDemoMode,
                    onCheckedChange = { onToggleDemo() },
                    colors = SwitchDefaults.colors(checkedThumbColor = PulsePink, checkedTrackColor = PulsePink.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
private fun DetailScreen(type: String, metrics: DashboardMetrics, isDemo: Boolean, onBack: () -> Unit) {
    val locale = LocalLocale.current.platformLocale
    val color = when (type) { "Steps" -> WarmOrange; "Heart rate" -> PulsePink; "Sleep" -> SleepPurple; "Water" -> WaterBlue; else -> PulsePink }
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
        item {
            HealthCard(Modifier.fillMaxWidth()) {
                Text("TODAY", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                val (valueText, hasData) = when (type) {
                    "Steps" -> metrics.steps?.let { "${String.format(locale, "%,d", it)} steps" } to (metrics.steps != null)
                    "Heart rate" -> metrics.heartRate?.let { "$it bpm resting" } to (metrics.heartRate != null)
                    "Sleep" -> metrics.sleepMinutes?.let { "${it / 60} hr ${it % 60} min" } to (metrics.sleepMinutes != null)
                    "Water" -> "${String.format(locale, "%.1f", metrics.waterLiters)} L" to (metrics.waterLiters > 0.0)
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
                    MiniBars(color, listOf(.3f,.45f,.38f,.62f,.54f,.8f,.67f,.9f,.76f,1f,.84f,.94f))
                } else {
                    Text(
                        "No records found for today. New readings from Health Connect or local logs will appear here.",
                        color = Muted,
                        fontSize = 12.sp
                    )
                }
            }
        }
        items(listOf("7-day trend", "Goal progress", "Data sources")) { label ->
            HealthCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Default.ChevronRight, null, tint = Muted)
                }
            }
        }
    }
}

@Composable
private fun BrowseScreen(padding: PaddingValues, onOpen: (String) -> Unit) {
    val items = listOf(
        Triple("Activity", Icons.AutoMirrored.Filled.DirectionsRun, WarmOrange),
        Triple("Heart rate", Icons.Default.MonitorHeart, PulsePink),
        Triple("Sleep", Icons.Default.NightsStay, SleepPurple),
        Triple("Water", Icons.Default.LocalDrink, WaterBlue)
    )
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, padding.calculateTopPadding() + 18.dp, 20.dp, padding.calculateBottomPadding() + 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Browse", fontSize = 30.sp, fontWeight = FontWeight.Bold) }
        item { Text("Health categories", color = Muted) }
        items(items) { item ->
            HealthCard(Modifier.fillMaxWidth().clickable { onOpen(item.first) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = item.third.copy(alpha = .12f)) { Icon(item.second, null, Modifier.padding(10.dp), tint = item.third) }
                    Spacer(Modifier.width(12.dp))
                    Text(item.first, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Default.ChevronRight, null, tint = Muted)
                }
            }
        }
    }
}

@Composable
private fun EmptyStateScreen(padding: PaddingValues, icon: ImageVector, title: String, body: String) {
    Column(Modifier.fillMaxSize().padding(padding).padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(shape = CircleShape, color = PulsePink.copy(alpha = .1f)) { Icon(icon, null, Modifier.padding(20.dp).size(36.dp), tint = PulsePink) }
        Spacer(Modifier.height(18.dp))
        Text(title, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(body, color = Muted, textAlign = TextAlign.Center, lineHeight = 20.sp)
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
            onExitDemo = {}
        )
    }
}

@Preview(name = "Dashboard Demo Mode State", showBackground = true)
@Composable
private fun SummaryScreenDemoPreview() {
    PulseTheme {
        SummaryScreen(
            state = DashboardUiState(
                metrics = DashboardMetrics.demo(),
                isDemoMode = true,
                hasHealthPermissions = true,
                healthConnectAvailable = true
            ),
            contentPadding = PaddingValues(),
            onRange = {},
            onWater = {},
            onEdit = {},
            onProfile = {},
            onDetail = {},
            onExitDemo = {}
        )
    }
}
