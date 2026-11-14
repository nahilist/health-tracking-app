package com.pulsehealth.app.ui.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.StandCyan
import com.pulsehealth.app.ui.theme.WarmOrange
import com.pulsehealth.app.ui.theme.WaterBlue

enum class ProfileSection(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    ACCOUNT(
        title = "Account & Cloud Sync",
        subtitle = "Supabase cloud backup, auth & multi-device sync",
        icon = Icons.Default.CloudDone,
        accentColor = PulsePink
    ),
    HEALTH_CONNECT(
        title = "Health Connect & Sensors",
        subtitle = "Android permissions, data sources & water write-back",
        icon = Icons.Default.MonitorHeart,
        accentColor = StandCyan
    ),
    NOTIFICATIONS(
        title = "Hydration Reminders",
        subtitle = "Local alerts, reminder intervals & quiet hours",
        icon = Icons.Default.NotificationsActive,
        accentColor = WaterBlue
    ),
    DATA_PRIVACY(
        title = "Data Vault & Privacy",
        subtitle = "JSON export, on-device encryption & local wipe",
        icon = Icons.Default.Lock,
        accentColor = ExerciseGreen
    ),
    PREFERENCES(
        title = "Preferences & About",
        subtitle = "Simulated demo mode, version & licenses",
        icon = Icons.Default.Tune,
        accentColor = WarmOrange
    )
}
