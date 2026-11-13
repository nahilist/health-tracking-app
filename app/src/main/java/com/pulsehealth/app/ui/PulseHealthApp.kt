package com.pulsehealth.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulsehealth.app.ui.browse.BrowseScreen
import com.pulsehealth.app.ui.components.EmptyStateScreen
import com.pulsehealth.app.ui.detail.DetailScreen
import com.pulsehealth.app.ui.profile.ProfileScreen
import com.pulsehealth.app.ui.summary.FavoritesSheet
import com.pulsehealth.app.ui.summary.SummaryScreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink

enum class AppTab(val label: String, val icon: ImageVector) {
    SUMMARY("Summary", Icons.Default.Favorite),
    SHARING("Sharing", Icons.Default.Groups),
    BROWSE("Browse", Icons.Default.GridView)
}

@Composable
fun PulseHealthApp(viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(AppTab.SUMMARY) }
    var detail by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = viewModel.healthRepository.permissionContract()
    ) { viewModel.refreshPermissionsAndSync() }

    when {
        profile -> ProfileScreen(
            state = state,
            onBack = { profile = false },
            onConnect = { permissionLauncher.launch(viewModel.healthRepository.permissions) },
            onSync = viewModel::sync,
            onToggleDemo = viewModel::toggleDemoMode
        )
        detail != null -> DetailScreen(
            type = detail.orEmpty(),
            metrics = state.metrics,
            isDemo = state.isDemoMode,
            onBack = { detail = null }
        )
        else -> Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                PulseBottomBar(selected = tab, onSelected = { tab = it })
            }
        ) { padding ->
            when (tab) {
                AppTab.SUMMARY -> SummaryScreen(
                    state = state,
                    contentPadding = padding,
                    onRange = viewModel::selectRange,
                    onWater = viewModel::addWater,
                    onEdit = { editing = true },
                    onProfile = { profile = true },
                    onDetail = { detail = it },
                    onExitDemo = { viewModel.setDemoMode(false) }
                )
                AppTab.SHARING -> EmptyStateScreen(
                    padding = padding,
                    icon = Icons.Default.Groups,
                    title = "Health sharing",
                    body = "Share selected trends with people you trust. Your health data stays private until you choose otherwise."
                )
                AppTab.BROWSE -> BrowseScreen(padding) { detail = it }
            }
        }
    }

    if (editing) {
        FavoritesSheet(state.hiddenCards, viewModel::toggleCard) { editing = false }
    }
}

@Composable
fun PulseBottomBar(selected: AppTab, onSelected: (AppTab) -> Unit) {
    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(70.dp)
                .padding(horizontal = 18.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            AppTab.entries.forEach { item ->
                val active = selected == item
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onSelected(item) }
                        .background(if (active) PulsePink.copy(alpha = 0.11f) else Color.Transparent)
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(item.icon, item.label, tint = if (active) PulsePink else Muted, modifier = Modifier.size(22.dp))
                    Text(
                        item.label,
                        color = if (active) PulsePink else Muted,
                        fontSize = 10.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
