package com.pulsehealth.app.ui.onboarding

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.components.HealthConnectRationaleDialog
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.WaterBlue

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    var showRationaleDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Surface(
            shape = CircleShape,
            color = PulsePink.copy(alpha = 0.12f),
            modifier = Modifier.size(72.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = PulsePink,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Welcome to Pulse Health",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Private, honest health tracking designed around you.",
            color = Muted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(28.dp))

        OnboardingCard(
            icon = Icons.Default.Lock,
            iconTint = PulsePink,
            title = "100% Local & Private",
            description = "Your health data stays strictly on your device. There are no remote accounts, no tracking analytics, and no cloud uploads."
        )

        Spacer(Modifier.height(16.dp))

        OnboardingCard(
            icon = Icons.Default.MonitorHeart,
            iconTint = PulsePink,
            title = "Android Health Connect",
            description = "Optionally sync steps, heart rate, sleep duration, and active calories through Android's secure Health Connect repository."
        )

        Spacer(Modifier.height(16.dp))

        OnboardingCard(
            icon = Icons.Default.WaterDrop,
            iconTint = WaterBlue,
            title = "Hydration Tracking",
            description = "Log your water intake with one tap and track progress toward your daily goal. Water records can sync back to Health Connect."
        )

        Spacer(Modifier.weight(1f, fill = false))
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { showRationaleDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PulsePink)
        ) {
            Text("Connect Health Connect", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue locally (Skip Health Connect)", color = Muted, fontSize = 14.sp)
        }
        Spacer(Modifier.height(12.dp))
    }

    if (showRationaleDialog) {
        HealthConnectRationaleDialog(
            onDismiss = {
                showRationaleDialog = false
            },
            onContinue = {
                showRationaleDialog = false
                onRequestPermissions()
                onComplete()
            }
        )
    }
}

@Composable
private fun OnboardingCard(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    description: String
) {
    HealthCard(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(3.dp))
                Text(description, color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
    }
}
