package com.pulsehealth.app.ui.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.SleepPurple
import com.pulsehealth.app.ui.theme.WarmOrange
import com.pulsehealth.app.ui.theme.WaterBlue

@Composable
fun BrowseScreen(padding: PaddingValues, onOpen: (String) -> Unit) {
    val items = listOf(
        Triple("Activity", Icons.AutoMirrored.Filled.DirectionsRun, WarmOrange),
        Triple("Heart rate", Icons.Default.MonitorHeart, PulsePink),
        Triple("Sleep", Icons.Default.NightsStay, SleepPurple),
        Triple("Water", Icons.Default.LocalDrink, WaterBlue)
    )
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            20.dp,
            padding.calculateTopPadding() + 18.dp,
            20.dp,
            padding.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Browse", fontSize = 30.sp, fontWeight = FontWeight.Bold) }
        item { Text("Health categories", color = Muted) }
        items(items) { item ->
            HealthCard(Modifier.fillMaxWidth().clickable { onOpen(item.first) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = item.third.copy(alpha = 0.12f)) {
                        Icon(item.second, null, Modifier.padding(10.dp), tint = item.third)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(item.first, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Default.ChevronRight, null, tint = Muted)
                }
            }
        }
    }
}
