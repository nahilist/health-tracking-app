package com.pulsehealth.app.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.DashboardUiState
import com.pulsehealth.app.data.HealthConnectAvailability
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.WaterBlue

@Composable
fun ProfileScreen(
    state: DashboardUiState,
    onBack: () -> Unit,
    onConnect: () -> Unit,
    onSync: () -> Unit,
    onToggleDemo: () -> Unit,
    onToggleSyncWater: (Boolean) -> Unit = {},
    onRequestWritePermissions: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Text("Profile & data", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(22.dp))
        HealthCard(Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Person, null, tint = PulsePink, modifier = Modifier.size(42.dp))
            Spacer(Modifier.height(10.dp))
            Text("Your health, under your control", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                "Pulse Health stores preferences only on this device and reads health metrics through Android Health Connect.",
                color = Muted,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
        Spacer(Modifier.height(16.dp))
        HealthCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MonitorHeart, null, tint = PulsePink)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Health Connect", fontWeight = FontWeight.Bold)
                    Text(
                        when (state.healthConnectAvailability) {
                            HealthConnectAvailability.NOT_SUPPORTED -> "Not supported on this device"
                            HealthConnectAvailability.UPDATE_REQUIRED -> "Provider update required in Play Store"
                            HealthConnectAvailability.AVAILABLE -> if (state.hasHealthPermissions) {
                                "Connected and ready"
                            } else {
                                "Permissions needed to sync"
                            }
                        },
                        color = Muted,
                        fontSize = 12.sp
                    )
                }
                if (state.isSyncing) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            }
            Spacer(Modifier.height(14.dp))
            when (state.healthConnectAvailability) {
                HealthConnectAvailability.UPDATE_REQUIRED -> {
                    Button(
                        onClick = {
                            val playStoreIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=com.google.android.apps.healthdata")
                            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                            runCatching { context.startActivity(playStoreIntent) }.onFailure {
                                val webIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
                                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                runCatching { context.startActivity(webIntent) }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PulsePink)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Update in Play Store")
                    }
                }
                HealthConnectAvailability.AVAILABLE -> {
                    Button(
                        onClick = if (state.hasHealthPermissions) onSync else onConnect,
                        enabled = !state.isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = PulsePink)
                    ) {
                        Icon(if (state.hasHealthPermissions) Icons.Default.Sync else Icons.Default.Settings, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (state.hasHealthPermissions) "Sync now" else "Connect")
                    }
                }
                HealthConnectAvailability.NOT_SUPPORTED -> {
                    Text(
                        "Health Connect is not supported on this Android system version.",
                        color = Muted,
                        fontSize = 12.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        HealthCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WaterDrop, null, tint = WaterBlue)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Sync water to Health Connect", fontWeight = FontWeight.Bold)
                    Text("Save logged water into Health Connect so other fitness apps can access it.", color = Muted, fontSize = 12.sp)
                }
                Switch(
                    checked = state.syncWaterToHealthConnect,
                    onCheckedChange = { enabled ->
                        onToggleSyncWater(enabled)
                        if (enabled && !state.hasWriteHydrationPermission) {
                            onRequestWritePermissions()
                        }
                    },
                    enabled = state.healthConnectAvailable,
                    colors = SwitchDefaults.colors(checkedThumbColor = WaterBlue, checkedTrackColor = WaterBlue.copy(alpha = 0.5f))
                )
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
