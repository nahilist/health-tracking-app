package com.pulsehealth.app.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.core.content.ContextCompat
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
    onSignOut: () -> Unit = {},
    onSyncCloud: () -> Unit = {},
    onToggleReminders: (Boolean) -> Unit = {},
    onSetReminderInterval: (Int) -> Unit = {},
    onSetQuietHours: (startHour: Int, endHour: Int) -> Unit = { _, _ -> },
    onSendTestReminder: () -> Unit = {}
) {
    val context = LocalContext.current
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onToggleReminders(true)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Spacer(Modifier.width(4.dp))
            Text("Profile & Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        HealthCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val user = state.currentUser
                val isSignedIn = user != null && !user.isAnonymous
                Surface(
                    shape = CircleShape,
                    color = if (isSignedIn) ExerciseGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isSignedIn) ExerciseGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (isSignedIn) (user.email ?: user.displayName ?: "User") else "Local Guest",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        if (isSignedIn) {
                            val freshness = state.cloudFreshnessLabel
                            if (freshness != null) "Synced: $freshness" else "Connected to Supabase"
                        } else "Offline mode (On-device storage)",
                        color = Muted,
                        fontSize = 12.sp
                    )
                }
                if (state.isCloudSyncing) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            }

            if (state.cloudSyncError != null) {
                val safeSyncError = remember(state.cloudSyncError) {
                    val raw = state.cloudSyncError
                    val lower = raw.lowercase(java.util.Locale.ROOT)
                    if (lower.contains("apikey") || lower.contains("bearer") || lower.contains("header") || lower.contains("url:") || lower.contains("http")) {
                        "Cloud sync encountered an issue. Local records remain safe."
                    } else {
                        raw.substringBefore("\n").substringBefore("URL:").trim()
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = safeSyncError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(14.dp))
            val user = state.currentUser
            if (user != null && !user.isAnonymous) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSyncCloud,
                        enabled = !state.isCloudSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = PulsePink),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (state.isCloudSyncing) "Syncing..." else "Sync Now")
                    }
                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sign Out")
                    }
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
        // Section 1: DATA SOURCES & SYNC
        Column {
            Text(
                "DATA SOURCES & SYNC",
                color = Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            HealthCard(
                Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                // Health Connect Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = PulsePink.copy(alpha = 0.12f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MonitorHeart, null, tint = PulsePink, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Health Connect", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
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
                    if (state.isSyncing) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                }

                when (state.healthConnectAvailability) {
                    HealthConnectAvailability.UPDATE_REQUIRED -> {
                        Spacer(Modifier.height(10.dp))
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
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Update in Play Store", fontSize = 12.sp)
                        }
                    }
                    HealthConnectAvailability.AVAILABLE -> {
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = if (state.hasHealthPermissions) onSync else { { showRationaleDialog = true } },
                                enabled = !state.isSyncing,
                                colors = ButtonDefaults.buttonColors(containerColor = PulsePink)
                            ) {
                                Icon(if (state.hasHealthPermissions) Icons.Default.Sync else Icons.Default.Settings, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (state.hasHealthPermissions) "Sync now" else "Connect", fontSize = 12.sp)
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
                                    Text("Permissions", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    HealthConnectAvailability.NOT_SUPPORTED -> Unit
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(14.dp))

                // Water sync to Health Connect
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = WaterBlue.copy(alpha = 0.12f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.WaterDrop, null, tint = WaterBlue, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Sync Water to Health Connect", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Write hydration logs into Health Connect", color = Muted, fontSize = 12.sp)
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

        // Section 2: PREFERENCES & REMINDERS
        Column {
            Text(
                "PREFERENCES & REMINDERS",
                color = Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            HealthCard(
                Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                // Reminders Toggle Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = PulsePink.copy(alpha = 0.12f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.NotificationsActive, null, tint = PulsePink, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Hydration Reminders", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(
                            if (state.remindersEnabled) "Active every ${state.reminderIntervalHours}h"
                            else "Local notifications to stay hydrated",
                            color = Muted,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = state.remindersEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!hasPermission) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        onToggleReminders(true)
                                    }
                                } else {
                                    onToggleReminders(true)
                                }
                            } else {
                                onToggleReminders(false)
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PulsePink, checkedTrackColor = PulsePink.copy(alpha = 0.5f))
                    )
                }

                if (state.remindersEnabled) {
                    Spacer(Modifier.height(12.dp))
                    Text("Frequency", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Muted)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1 to "1h", 2 to "2h", 3 to "3h", 4 to "4h").forEach { (hours, label) ->
                            val selected = state.reminderIntervalHours == hours
                            FilterChip(
                                selected = selected,
                                onClick = { onSetReminderInterval(hours) },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bedtime, null, tint = Muted, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Quiet hours: ${state.quietHoursStartHour}:00 – ${state.quietHoursEndHour}:00",
                            color = Muted,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onSendTestReminder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send Test Reminder", fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(14.dp))

                // Demo mode row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Muted.copy(alpha = 0.12f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Tune, null, tint = Muted, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Sample Demo Mode", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Preview UI with simulated health metrics", color = Muted, fontSize = 12.sp)
                    }
                    Switch(
                        checked = state.isDemoMode,
                        onCheckedChange = { onToggleDemo() },
                        colors = SwitchDefaults.colors(checkedThumbColor = PulsePink, checkedTrackColor = PulsePink.copy(alpha = 0.5f))
                    )
                }
            }
        }

        // Section 3: DATA & PRIVACY
        Column {
            Text(
                "DATA & PRIVACY",
                color = Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            HealthCard(
                Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
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
                        Text("Export JSON", fontSize = 12.sp)
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
                        Text("Delete Data", fontSize = 12.sp)
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
