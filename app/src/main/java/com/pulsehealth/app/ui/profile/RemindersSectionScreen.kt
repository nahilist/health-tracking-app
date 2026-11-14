package com.pulsehealth.app.ui.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.core.content.ContextCompat
import com.pulsehealth.app.data.DashboardUiState
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.WaterBlue

@Composable
fun RemindersSectionScreen(
    state: DashboardUiState,
    onBack: () -> Unit,
    onToggleReminders: (Boolean) -> Unit,
    onSetReminderInterval: (Int) -> Unit,
    onSendTestReminder: () -> Unit
) {
    val context = LocalContext.current

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
        ProfileSectionHeader(
            title = "Hydration Reminders",
            subtitle = "Local alerts to help you stay hydrated",
            onBack = onBack
        )

        // Master Switch Card
        HealthCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = WaterBlue.copy(alpha = 0.12f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = WaterBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(Modifier.weight(1f)) {
                    Text("Daily Notifications", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Spacer(Modifier.height(3.dp))
                    StatusDotBadge(
                        label = if (state.remindersEnabled) "Active (${state.reminderIntervalHours}h interval)" else "Turned Off",
                        color = if (state.remindersEnabled) ExerciseGreen else Muted
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
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PulsePink,
                        checkedTrackColor = PulsePink.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(Modifier.height(14.dp))
            Text(
                "Reminders run entirely on this device using Android AlarmManager. No external notification server is required.",
                color = Muted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }

        if (state.remindersEnabled) {
            // Frequency Settings
            Column {
                Text(
                    "REMINDER CADENCE",
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
                    Text("Notification Frequency", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("How often Pulse Health nudges you during awake hours", color = Muted, fontSize = 12.sp)

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            1 to "Every 1h",
                            2 to "Every 2h",
                            3 to "Every 3h",
                            4 to "Every 4h"
                        ).forEach { (hours, label) ->
                            val isSelected = state.reminderIntervalHours == hours
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSetReminderInterval(hours) },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PulsePink,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Approx. ${(14 / state.reminderIntervalHours).coerceAtLeast(1)} gentle reminders per day between quiet hours.",
                        color = Muted,
                        fontSize = 11.sp
                    )
                }
            }

            // Quiet Hours Schedule
            Column {
                Text(
                    "SLEEP PROTECTION",
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
                            color = Muted.copy(alpha = 0.12f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Bedtime, null, tint = Muted, modifier = Modifier.size(20.dp))
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Quiet Hours (${state.quietHoursStartHour}:00 – ${state.quietHoursEndHour}:00)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "All hydration alerts are automatically silenced overnight to ensure uninterrupted rest.",
                                color = Muted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Test Notification Action
            Column {
                Text(
                    "DIAGNOSTICS & TESTING",
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
                    Text("Verify Notification Delivery", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("Trigger an immediate test alert to verify notification sound and banner appearance on your device.", color = Muted, fontSize = 12.sp)

                    Spacer(Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = onSendTestReminder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Send Test Reminder Now")
                    }
                }
            }
        }
    }
}
