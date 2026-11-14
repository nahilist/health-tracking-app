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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.DashboardUiState
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.WarmOrange

@Composable
fun PreferencesSectionScreen(
    state: DashboardUiState,
    onBack: () -> Unit,
    onToggleDemo: () -> Unit
) {
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
            title = "Preferences & About",
            subtitle = "Preview simulator and application specifications",
            onBack = onBack
        )

        // Sandbox Simulator Card
        HealthCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = WarmOrange.copy(alpha = 0.12f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            tint = WarmOrange,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(Modifier.weight(1f)) {
                    Text("Sample Demo Simulator", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Spacer(Modifier.height(3.dp))
                    StatusDotBadge(
                        label = if (state.isDemoMode) "Demo Simulator Active" else "Real Data Only",
                        color = if (state.isDemoMode) PulsePink else Muted
                    )
                }

                Switch(
                    checked = state.isDemoMode,
                    onCheckedChange = { onToggleDemo() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PulsePink,
                        checkedTrackColor = PulsePink.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(Modifier.height(14.dp))
            Text(
                "When enabled, Pulse Health renders simulated health metrics and hydration history so you can preview UI layouts without real health records on this device.",
                color = Muted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }

        // App Specifications
        Column {
            Text(
                "APPLICATION ARCHITECTURE",
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
                    "Product Name" to "Pulse Health",
                    "Version" to "1.0.0 (Production Release)",
                    "Package" to "com.pulsehealth.app",
                    "Compile Target" to "Android API 37 (VanillaIceCream)",
                    "Minimum Target" to "Android API 26 (Oreo)",
                    "UI Framework" to "Jetpack Compose + Material 3",
                    "Persistence Engine" to "Room SQLite v1 + Encrypted Keystore",
                    "Cloud Platform" to "Supabase PostgreSQL + RLS"
                ).forEachIndexed { index, (label, value) ->
                    if (index > 0) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(12.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(value, color = Muted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
