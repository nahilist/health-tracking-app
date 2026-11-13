package com.pulsehealth.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Groups
import androidx.compose.ui.graphics.vector.ImageVector

sealed class PulseDestination(val route: String) {
    data object Summary : PulseDestination("summary")
    data object Sharing : PulseDestination("sharing")
    data object Browse : PulseDestination("browse")
    data object Profile : PulseDestination("profile")
    data object Detail : PulseDestination("detail/{metricType}") {
        const val ARG_METRIC_TYPE = "metricType"
        fun createRoute(metricType: String): String = "detail/$metricType"
    }
}

enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    SUMMARY(PulseDestination.Summary.route, "Summary", Icons.Default.Favorite),
    SHARING(PulseDestination.Sharing.route, "Sharing", Icons.Default.Groups),
    BROWSE(PulseDestination.Browse.route, "Browse", Icons.Default.GridView)
}
