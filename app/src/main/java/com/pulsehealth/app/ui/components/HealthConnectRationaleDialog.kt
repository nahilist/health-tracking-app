package com.pulsehealth.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.SleepPurple
import com.pulsehealth.app.ui.theme.WarmOrange
import com.pulsehealth.app.ui.theme.WaterBlue

@Composable
fun HealthConnectRationaleDialog(
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Why Pulse Health needs Health Connect", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column {
                Text(
                    text = "Pulse Health accesses Android Health Connect to read your health metrics and display them on your private dashboard:",
                    fontSize = 13.sp,
                    color = Muted,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(14.dp))

                RationaleRow(
                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                    color = WarmOrange,
                    title = "Steps & Active Calories",
                    description = "Displays daily activity progress and movement rings."
                )
                Spacer(Modifier.height(10.dp))
                RationaleRow(
                    icon = Icons.Default.Favorite,
                    color = PulsePink,
                    title = "Heart Rate",
                    description = "Monitors daily resting heart rate trends."
                )
                Spacer(Modifier.height(10.dp))
                RationaleRow(
                    icon = Icons.Default.Bedtime,
                    color = SleepPurple,
                    title = "Sleep Sessions",
                    description = "Tracks restorative sleep duration and sleep consistency."
                )
                Spacer(Modifier.height(10.dp))
                RationaleRow(
                    icon = Icons.Default.WaterDrop,
                    color = WaterBlue,
                    title = "Hydration (Optional Write)",
                    description = "Syncs water logged in Pulse Health to Health Connect."
                )

                Spacer(Modifier.height(14.dp))
                Text(
                    text = "All health data is processed entirely on your device. You can manage or revoke permissions anytime in Android Settings.",
                    fontSize = 12.sp,
                    color = Muted,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = PulsePink)
            ) {
                Text("Continue to permissions")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not now")
            }
        }
    )
}

@Composable
private fun RationaleRow(
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(description, color = Muted, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}
