package com.pulsehealth.app.ui.summary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.data.DashboardUiState
import com.pulsehealth.app.data.DataSource
import com.pulsehealth.app.data.TimeRange
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.PulseTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SummaryScreen(
    state: DashboardUiState,
    contentPadding: PaddingValues,
    onRange: (TimeRange) -> Unit,
    onWater: () -> Unit,
    onEdit: () -> Unit,
    onProfile: () -> Unit,
    onDetail: (String) -> Unit,
    onExitDemo: () -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit
) {
    val locale = LocalLocale.current.platformLocale
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = contentPadding.calculateTopPadding() + 14.dp,
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
                ) {
                    Icon(Icons.Default.Person, "Profile", Modifier.padding(11.dp), tint = Muted)
                }
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
        }
    }
}

@Composable
fun LoadingStateCard() {
    HealthCard(Modifier.fillMaxWidth().height(160.dp)) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = PulsePink,
                    strokeWidth = 3.dp
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
                    text = "Sync error",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = message,
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
fun RangeSelector(selected: TimeRange, onRange: (TimeRange) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(3.dp)
    ) {
        TimeRange.entries.forEach { range ->
            Surface(
                modifier = Modifier.weight(1f).clickable { onRange(range) },
                shape = RoundedCornerShape(10.dp),
                color = if (selected == range) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (selected == range) 1.dp else 0.dp
            ) {
                Text(
                    range.label,
                    Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    fontWeight = if (selected == range) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected == range) MaterialTheme.colorScheme.onSurface else Muted
                )
            }
        }
    }
}

@Composable
fun InsightCard(state: DashboardUiState) {
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
