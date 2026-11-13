package com.pulsehealth.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.summary.MiniBars
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.SleepPurple
import com.pulsehealth.app.ui.theme.WarmOrange
import com.pulsehealth.app.ui.theme.WaterBlue

@Composable
fun DetailScreen(
    type: String,
    metrics: DashboardMetrics,
    isDemo: Boolean,
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
                    MiniBars(color, listOf(.3f, .45f, .38f, .62f, .54f, .8f, .67f, .9f, .76f, 1f, .84f, .94f))
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
