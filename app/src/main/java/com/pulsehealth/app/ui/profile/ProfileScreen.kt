package com.pulsehealth.app.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.DashboardUiState
import com.pulsehealth.app.data.HealthConnectAvailability
import com.pulsehealth.app.ui.components.AuthDialog
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.components.HealthConnectRationaleDialog
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.WarmOrange
import com.pulsehealth.app.ui.theme.WaterBlue

@Composable
fun ProfileScreen(
    state: DashboardUiState,
    onBack: () -> Unit,
    onConnect: () -> Unit,
    onSync: () -> Unit,
    onToggleDemo: () -> Unit,
    onToggleSyncWater: (Boolean) -> Unit = {},
    onRequestWritePermissions: () -> Unit = {},
    onExportData: ((String) -> Unit) -> Unit = {},
    onDeleteAllData: () -> Unit = {},
    onSignIn: (email: String, password: String, onError: (String) -> Unit) -> Unit = { _, _, _ -> },
    onSignUp: (email: String, password: String, onError: (String) -> Unit) -> Unit = { _, _, _ -> },
    onSignInAsGuest: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Text("Profile & data", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(18.dp))

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
                Icon(Icons.Default.CloudDone, null, tint = PulsePink, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Account & Cloud Sync", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    val user = state.currentUser
                    if (user != null && !user.isAnonymous) {
                        Text("Signed in as ${user.email ?: user.displayName ?: "User"}", color = ExerciseGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("Connected to Supabase PostgreSQL", color = Muted, fontSize = 11.sp)
                    } else {
                        Text("Local guest mode (Offline only)", color = WarmOrange, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("Sign in to back up and sync records across devices", color = Muted, fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            val user = state.currentUser
            if (user != null && !user.isAnonymous) {
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Out")
                }
            } else {
                Button(
                    onClick = { showAuthDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PulsePink),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign In / Create Account")
                }
            }
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = if (state.hasHealthPermissions) onSync else { { showRationaleDialog = true } },
                            enabled = !state.isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = PulsePink)
                        ) {
                            Icon(if (state.hasHealthPermissions) Icons.Default.Sync else Icons.Default.Settings, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (state.hasHealthPermissions) "Sync now" else "Connect")
                        }

                        if (state.hasHealthPermissions) {
                            OutlinedButton(
                                onClick = {
                                    val settingsIntent = Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS").apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    runCatching { context.startActivity(settingsIntent) }
                                }
                            ) {
                                Text("Permissions", fontSize = 13.sp)
                            }
                        }
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
                Icon(Icons.Default.Lock, null, tint = PulsePink)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Privacy & Data Management", fontWeight = FontWeight.Bold)
                    Text("Manage, export, or permanently delete your local records.", color = Muted, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onExportData { jsonString ->
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, jsonString)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Export Health Data")
                            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            runCatching { context.startActivity(shareIntent) }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileDownload, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export (JSON)", fontSize = 12.sp)
                }

                Button(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.DeleteForever, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Delete data", fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = { showAboutDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Info, null, tint = Muted, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("About Pulse Health & Privacy Policy", color = Muted, fontSize = 13.sp)
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
        Spacer(Modifier.height(16.dp))
    }

    if (showRationaleDialog) {
        HealthConnectRationaleDialog(
            onDismiss = { showRationaleDialog = false },
            onContinue = {
                showRationaleDialog = false
                onConnect()
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete all local data?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will permanently erase all local hydration logs, stored health summaries, and preferences from this device. Health records in Health Connect will remain untouched.\n\nThis action cannot be undone.",
                    fontSize = 13.sp,
                    color = Muted,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteAllData()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Pulse Health Privacy & Info", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Pulse Health v1.0.0", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Package: com.pulsehealth.app", color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "• Privacy by Design: 100% on-device local storage. No remote servers, no user accounts, no telemetry.\n\n" +
                        "• Minimal Permissions: Pulse Health requests only the Health Connect permissions necessary for dashboard metrics.\n\n" +
                        "• Full Data Control: You can export your data to standard JSON or wipe all local storage at any time.\n\n" +
                        "• Open Source: Fully auditable local-first architecture.",
                        fontSize = 12.sp,
                        color = Muted,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showAuthDialog) {
        AuthDialog(
            onDismiss = { showAuthDialog = false },
            onSignIn = { email, password, onError ->
                onSignIn(email, password) { err ->
                    if (err.isEmpty()) showAuthDialog = false
                    else onError(err)
                }
            },
            onSignUp = { email, password, onError ->
                onSignUp(email, password) { err ->
                    if (err.isEmpty()) showAuthDialog = false
                    else onError(err)
                }
            },
            onGuest = {
                onSignInAsGuest()
                showAuthDialog = false
            }
        )
    }
}
