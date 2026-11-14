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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink

@Composable
fun DataPrivacySectionScreen(
    onBack: () -> Unit,
    onExportData: ((String) -> Unit) -> Unit,
    onDeleteAllData: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

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
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PulsePink)
                ) {
                    Icon(Icons.Default.FileDownload, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Export All Data (JSON)")
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
