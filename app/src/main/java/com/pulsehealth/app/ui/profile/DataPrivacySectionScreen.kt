package com.pulsehealth.app.ui.profile

import android.content.Intent
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.pulsehealth.app.data.LockTimeout
import com.pulsehealth.app.security.BiometricAuthManager
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.ForestEmerald
import com.pulsehealth.app.ui.theme.ForestEmeraldDark
import com.pulsehealth.app.ui.theme.ForestEmeraldLight
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink

@Composable
fun DataPrivacySectionScreen(
    onBack: () -> Unit,
    biometricLockEnabled: Boolean = false,
    biometricLockTimeoutSeconds: Long = 0L,
    onToggleBiometricLock: (Boolean) -> Unit = {},
    onSetLockTimeoutSeconds: (Long) -> Unit = {},
    onExportData: ((String) -> Unit) -> Unit,
    onExportCsv: ((String) -> Unit) -> Unit = {},
    onExportClinicalSummary: ((String) -> Unit) -> Unit = {},
    onDeleteAllData: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    fun shareExportedText(text: String, chooserTitle: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, chooserTitle)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(shareIntent) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = 840.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileSectionHeader(
            title = "Data Vault & Privacy",
            subtitle = "Data sovereignty, export and permanent wipe",
            onBack = onBack
        )

        // Privacy Guarantee Hero Card
        HealthCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = ExerciseGreen.copy(alpha = 0.12f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = ExerciseGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(Modifier.weight(1f)) {
                    Text("100% User Owned Data", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Spacer(Modifier.height(3.dp))
                    StatusDotBadge(label = "Encrypted On-Device", color = ExerciseGreen)
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                "Pulse Health was engineered from the ground up to respect your digital autonomy. No trackers, no ad networks, no analytics SDKs, and no hidden telemetry.",
                color = Muted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }

        // Biometric Security Card
        Column {
            Text(
                "VAULT ACCESS CONTROL",
                color = Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            HealthCard(Modifier.fillMaxWidth()) {
                val biometricStatus = remember(context) { BiometricAuthManager.getBiometricStatus(context) }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = ForestEmeraldLight,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = ForestEmerald,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(Modifier.weight(1f)) {
                        Text("Biometric Vault Lock", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = ForestEmeraldDark)
                        Spacer(Modifier.height(3.dp))
                        StatusDotBadge(
                            label = if (biometricLockEnabled) "App Lock Active" else "Screen Lock Disabled",
                            color = if (biometricLockEnabled) ForestEmerald else Muted
                        )
                    }

                    Switch(
                        checked = biometricLockEnabled,
                        onCheckedChange = { targetState ->
                            onToggleBiometricLock(targetState)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ForestEmerald,
                            checkedTrackColor = ForestEmerald.copy(alpha = 0.5f)
                        )
                    )
                }

                Spacer(Modifier.height(14.dp))
                Text(
                    text = "When enabled, Pulse Health requires biometric authentication (Fingerprint, Face Unlock, or Device PIN) before revealing health metrics whenever the app is opened.",
                    color = Muted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0F4F2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = ForestEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = biometricStatus.description,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ForestEmeraldDark
                        )
                    }
                }

                if (biometricLockEnabled) {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "AUTO-LOCK TIMEOUT",
                        color = Muted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LockTimeout.entries.forEach { timeout ->
                            val selected = biometricLockTimeoutSeconds == timeout.seconds
                            FilterChip(
                                selected = selected,
                                onClick = { onSetLockTimeoutSeconds(timeout.seconds) },
                                label = { Text(timeout.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ForestEmeraldLight,
                                    selectedLabelColor = ForestEmeraldDark
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = if (selected) ForestEmerald else Color(0xFFD4E0DC)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Export Data Package Card
        Column {
            Text(
                "DATA PORTABILITY",
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
                Text("Export Personal Health Archive", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(
                    "Download your complete hydration history, daily aggregate summaries, and local settings as a standardized JSON package.",
                    color = Muted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = {
                        onExportData { jsonString ->
                            shareExportedText(jsonString, "Export Health Data (JSON)")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PulsePink)
                ) {
                    Icon(Icons.Default.FileDownload, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Export All Data (JSON)")
                }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        onExportCsv { csvString ->
                            shareExportedText(csvString, "Export Health Data (CSV)")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.TableChart, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Export Spreadsheets (CSV)")
                }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        onExportClinicalSummary { reportString ->
                            shareExportedText(reportString, "Export Doctor Summary Report")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Description, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Export Doctor Summary Report (TXT)")
                }
            }
        }

        // Erasure & Dangerous Actions
        Column {
            Text(
                "DATA ERASURE",
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
                Text("Erase Local Storage", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(
                    "Permanently purge all local hydration entries, daily summaries, cached data, and preferences from this device.",
                    color = Muted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.DeleteForever, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Everything From Device")
                }
            }
        }

        // Privacy Policy & Compliance
        Column {
            Text(
                "COMPLIANCE & AUDIT",
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
                TextButton(
                    onClick = { showAboutDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Info, null, tint = Muted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("View Privacy Policy & Open Source Details", color = Muted, fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(36.dp))
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Permanently delete local data?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This action permanently erases your local Room database, hydration logs, and preferences from this phone.\n\nHealth Connect records outside this app will remain untouched.\n\nThis cannot be undone.",
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
                    Text("Delete Permanently")
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
            title = { Text("Pulse Health Privacy & Architecture", fontWeight = FontWeight.Bold) },
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
}
