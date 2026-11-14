package com.pulsehealth.app.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.DashboardUiState
import com.pulsehealth.app.data.HealthConnectAvailability
import com.pulsehealth.app.ui.components.AuthDialog
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.components.HealthConnectRationaleDialog
import com.pulsehealth.app.ui.theme.ActivityGradient
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.StandCyan
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
    var selectedSection by remember { mutableStateOf<ProfileSection?>(null) }
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }

    // Intercept hardware / system back navigation when drilled into a section
    BackHandler(enabled = selectedSection != null) {
        selectedSection = null
    }

    AnimatedContent(
        targetState = selectedSection,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ProfileSectionTransition"
    ) { section ->
        when (section) {
            ProfileSection.ACCOUNT -> {
                AccountSectionScreen(
                    state = state,
                    onBack = { selectedSection = null },
                    onSyncCloud = onSyncCloud,
                    onSignOut = onSignOut,
                    onOpenAuth = { showAuthDialog = true }
                )
            }
            ProfileSection.HEALTH_CONNECT -> {
                HealthConnectSectionScreen(
                    state = state,
                    onBack = { selectedSection = null },
                    onConnect = onConnect,
                    onSync = onSync,
                    onToggleSyncWater = onToggleSyncWater,
                    onRequestWritePermissions = onRequestWritePermissions,
                    onShowRationale = { showRationaleDialog = true }
                )
            }
            ProfileSection.NOTIFICATIONS -> {
                RemindersSectionScreen(
                    state = state,
                    onBack = { selectedSection = null },
                    onToggleReminders = onToggleReminders,
                    onSetReminderInterval = onSetReminderInterval,
                    onSendTestReminder = onSendTestReminder
                )
            }
            ProfileSection.DATA_PRIVACY -> {
                DataPrivacySectionScreen(
                    onBack = { selectedSection = null },
                    onExportData = onExportData,
                    onDeleteAllData = onDeleteAllData
                )
            }
            ProfileSection.PREFERENCES -> {
                PreferencesSectionScreen(
                    state = state,
                    onBack = { selectedSection = null },
                    onToggleDemo = onToggleDemo
                )
            }
            null -> {
                ProfileHubContent(
                    state = state,
                    onBack = onBack,
                    onOpenSection = { targetSection -> selectedSection = targetSection },
                    onOpenAuth = { showAuthDialog = true }
                )
            }
        }
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

@Composable
private fun ProfileHubContent(
    state: DashboardUiState,
    onBack: () -> Unit,
    onOpenSection: (ProfileSection) -> Unit,
    onOpenAuth: () -> Unit
) {
    val user = state.currentUser
    val isSignedIn = user != null && !user.isAnonymous

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hub Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to dashboard")
            }
            Spacer(Modifier.width(4.dp))
            Text("Profile & Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        // Hero Profile Card
        HealthCard(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { onOpenSection(ProfileSection.ACCOUNT) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isSignedIn) ExerciseGreen.copy(alpha = 0.12f) else PulsePink.copy(alpha = 0.12f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isSignedIn) {
                                (user.email?.firstOrNull() ?: 'U').uppercaseChar().toString()
                            } else "G",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = if (isSignedIn) ExerciseGreen else PulsePink
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (isSignedIn) (user.email ?: user.displayName ?: "User") else "Local Guest Vault",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(Modifier.height(3.dp))
                    StatusDotBadge(
                        label = if (isSignedIn) "Cloud Synced · Supabase" else "100% On-Device · Encrypted",
                        color = if (isSignedIn) ExerciseGreen else WarmOrange
                    )
                    if (isSignedIn && state.cloudFreshnessLabel != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Last synced: ${state.cloudFreshnessLabel}",
                            color = Muted,
                            fontSize = 11.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Muted.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Security & Health Ribbon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HealthCard(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(12.dp)
            ) {
                Text("Streak", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${state.streakInfo.currentStreakDays}d 🔥",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            HealthCard(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(12.dp)
            ) {
                Text("Storage", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Room v1",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            HealthCard(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(12.dp)
            ) {
                Text("Vault", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Keystore",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // Dedicated Settings Sections (Grouped Card Architecture)
        Column {
            Text(
                "PREFERENCES & SYSTEM",
                color = Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            HealthCard(
                Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(0.dp)
            ) {
                // Account Section Tile
                SettingsTile(
                    section = ProfileSection.ACCOUNT,
                    statusLabel = if (isSignedIn) "Connected" else "Guest",
                    statusColor = if (isSignedIn) ExerciseGreen else WarmOrange,
                    onClick = { onOpenSection(ProfileSection.ACCOUNT) }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 72.dp)
                )

                // Health Connect Section Tile
                SettingsTile(
                    section = ProfileSection.HEALTH_CONNECT,
                    statusLabel = when (state.healthConnectAvailability) {
                        HealthConnectAvailability.AVAILABLE -> if (state.hasHealthPermissions) "Synced" else "Setup"
                        HealthConnectAvailability.UPDATE_REQUIRED -> "Update"
                        HealthConnectAvailability.NOT_SUPPORTED -> "N/A"
                    },
                    statusColor = when (state.healthConnectAvailability) {
                        HealthConnectAvailability.AVAILABLE -> if (state.hasHealthPermissions) ExerciseGreen else WarmOrange
                        HealthConnectAvailability.UPDATE_REQUIRED -> WarmOrange
                        HealthConnectAvailability.NOT_SUPPORTED -> MaterialTheme.colorScheme.error
                    },
                    onClick = { onOpenSection(ProfileSection.HEALTH_CONNECT) }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 72.dp)
                )

                // Notifications Section Tile
                SettingsTile(
                    section = ProfileSection.NOTIFICATIONS,
                    statusLabel = if (state.remindersEnabled) "${state.reminderIntervalHours}h" else "Off",
                    statusColor = if (state.remindersEnabled) WaterBlue else Muted,
                    onClick = { onOpenSection(ProfileSection.NOTIFICATIONS) }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 72.dp)
                )

                // Data Privacy Section Tile
                SettingsTile(
                    section = ProfileSection.DATA_PRIVACY,
                    statusLabel = "AES-256",
                    statusColor = ExerciseGreen,
                    onClick = { onOpenSection(ProfileSection.DATA_PRIVACY) }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 72.dp)
                )

                // Preferences & About Section Tile
                SettingsTile(
                    section = ProfileSection.PREFERENCES,
                    statusLabel = if (state.isDemoMode) "Demo" else "v1.0.0",
                    statusColor = if (state.isDemoMode) WarmOrange else Muted,
                    onClick = { onOpenSection(ProfileSection.PREFERENCES) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
