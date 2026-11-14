package com.pulsehealth.app.ui.profile

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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.DashboardUiState
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.WarmOrange

@Composable
fun AccountSectionScreen(
    state: DashboardUiState,
    onBack: () -> Unit,
    onSyncCloud: () -> Unit,
    onSignOut: () -> Unit,
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
        ProfileSectionHeader(
            title = "Account & Cloud Sync",
            subtitle = "Supabase PostgreSQL backup and multi-device sync",
            onBack = onBack
        )

        // Hero Account Card
        HealthCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = if (isSignedIn) ExerciseGreen.copy(alpha = 0.12f) else PulsePink.copy(alpha = 0.12f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isSignedIn) ExerciseGreen else PulsePink,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (isSignedIn) (user.email ?: user.displayName ?: "Authenticated User") else "Local Guest Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(Modifier.height(3.dp))
                    StatusDotBadge(
                        label = if (isSignedIn) "Cloud Connected" else "Offline Vault Only",
                        color = if (isSignedIn) ExerciseGreen else WarmOrange
                    )
                }

                if (state.isCloudSyncing) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
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
                Spacer(Modifier.height(12.dp))
                Text(
                    text = safeSyncError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(16.dp))

            if (isSignedIn) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                    onClick = onOpenAuth,
                    colors = ButtonDefaults.buttonColors(containerColor = PulsePink),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sign In or Register")
                }
            }
        }

        // Synchronization Health Details
        Column {
            Text(
                "CLOUD METADATA",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Cloud Freshness", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Last successful cloud backup event", color = Muted, fontSize = 11.sp)
                    }
                    Text(
                        text = state.cloudFreshnessLabel ?: "Not yet synced",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (state.cloudFreshnessLabel != null) ExerciseGreen else Muted
                    )
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Backend Infrastructure", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Managed cloud database platform", color = Muted, fontSize = 11.sp)
                    }
                    Text(
                        text = "Supabase PostgreSQL",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Access Control", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Hardware Keystore + Row-Level Security", color = Muted, fontSize = 11.sp)
                    }
                    Text(
                        text = "Enforced (RLS)",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = ExerciseGreen
                    )
                }
            }
        }

        // Security Guarantee Note
        HealthCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    tint = ExerciseGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Privacy & Security Guarantee", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Your credentials and tokens are protected inside the Android Keystore using hardware-backed EncryptedSharedPreferences. Local records are never sent anywhere without your explicit permission.",
                        color = Muted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
