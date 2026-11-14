package com.pulsehealth.app.ui.profile

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.StandCyan
import com.pulsehealth.app.ui.theme.WarmOrange
import com.pulsehealth.app.ui.theme.WaterBlue

@Composable
fun HealthConnectSectionScreen(
    state: DashboardUiState,
    onBack: () -> Unit,
    onConnect: () -> Unit,
    onSync: () -> Unit,
    onToggleSyncWater: (Boolean) -> Unit,
    onRequestWritePermissions: () -> Unit,
    onShowRationale: () -> Unit
) {
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileSectionHeader(
            title = "Health Connect & Sensors",
            subtitle = "Android on-device health data integration",
            onBack = onBack
        )

        // Connection Status Hero
        HealthCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = StandCyan.copy(alpha = 0.12f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.MonitorHeart,
                            contentDescription = null,
                            tint = StandCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Android Health Connect",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(Modifier.height(3.dp))
                    StatusDotBadge(
                        label = when (state.healthConnectAvailability) {
                            HealthConnectAvailability.AVAILABLE -> if (state.hasHealthPermissions) "Connected & Synchronized" else "Permissions Needed"
                            HealthConnectAvailability.UPDATE_REQUIRED -> "Update Required"
                            HealthConnectAvailability.NOT_SUPPORTED -> "Not Supported"
                        },
                        color = when (state.healthConnectAvailability) {
                            HealthConnectAvailability.AVAILABLE -> if (state.hasHealthPermissions) ExerciseGreen else WarmOrange
                            HealthConnectAvailability.UPDATE_REQUIRED -> WarmOrange
                            HealthConnectAvailability.NOT_SUPPORTED -> MaterialTheme.colorScheme.error
                        }
                    )
                }

                if (state.isSyncing) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = when (state.healthConnectAvailability) {
                    HealthConnectAvailability.AVAILABLE -> if (state.hasHealthPermissions) {
                        "Pulse Health reads steps, calories, heart rate, and sleep directly from on-device Health Connect records."
                    } else {
                        "Grant Health Connect permissions to view your daily activity, resting heart rate, and sleep stats on the dashboard."
                    }
                    HealthConnectAvailability.UPDATE_REQUIRED -> "The Health Connect system service on your device requires an update from Google Play Store."
                    HealthConnectAvailability.NOT_SUPPORTED -> "Health Connect is not available on this Android device or OS version."
                },
                color = Muted,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(16.dp))

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = if (state.hasHealthPermissions) onSync else onShowRationale,
                            enabled = !state.isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = PulsePink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(if (state.hasHealthPermissions) Icons.Default.Sync else Icons.Default.Settings, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (state.hasHealthPermissions) "Sync Now" else "Connect")
                        }

                        if (state.hasHealthPermissions) {
                            OutlinedButton(
                                onClick = {
                                    val settingsIntent = Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS").apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    runCatching { context.startActivity(settingsIntent) }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Permissions")
                            }
                        }
                    }
                }
                HealthConnectAvailability.NOT_SUPPORTED -> Unit
            }
        }

        // Two-Way Water Synchronization
        Column {
            Text(
                "WRITE-BACK INTEGRATIONS",
                color = Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            HealthCard(
                Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = WaterBlue.copy(alpha = 0.12f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.WaterDrop, null, tint = WaterBlue, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(Modifier.weight(1f)) {
                        Text("Sync Water Intake", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(
                            "Export logged water into Health Connect so other fitness apps can access your hydration history.",
                            color = Muted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
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
        }

        // Permissions Matrix Overview
        Column {
            Text(
                "DATA TYPES READ & WRITTEN",
                color = Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            HealthCard(
                Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                listOf(
                    "Steps Record" to "Daily step count totals and cadence",
                    "Active Calories Burned" to "Energy expended during workouts",
                    "Exercise Sessions" to "Duration and workout intensity",
                    "Heart Rate" to "Resting heart rate in beats per minute",
                    "Sleep Sessions" to "Sleep duration and restful intervals",
                    "Hydration Record" to "Water intake logging and synchronization"
                ).forEachIndexed { index, (name, description) ->
                    if (index > 0) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(12.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (state.hasHealthPermissions) ExerciseGreen else Muted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(description, color = Muted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
