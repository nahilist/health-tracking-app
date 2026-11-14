package com.pulsehealth.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pulsehealth.app.data.AuthResult
import com.pulsehealth.app.ui.browse.BrowseScreen
import com.pulsehealth.app.ui.components.EmptyStateScreen
import com.pulsehealth.app.ui.detail.DetailScreen
import com.pulsehealth.app.ui.sharing.SharingScreen
import com.pulsehealth.app.ui.navigation.PulseDestination
import com.pulsehealth.app.ui.navigation.TopLevelDestination
import com.pulsehealth.app.ui.onboarding.OnboardingScreen
import com.pulsehealth.app.ui.profile.ProfileScreen
import com.pulsehealth.app.ui.summary.FavoritesSheet
import com.pulsehealth.app.ui.summary.SummaryScreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import kotlinx.coroutines.launch

@Composable
fun PulseHealthApp(
    navController: NavHostController = rememberNavController(),
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val todayHydration by viewModel.todayHydrationEntries.collectAsStateWithLifecycle()
    val weeklyWater by viewModel.weeklyWaterLiters.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = viewModel.healthRepository.permissionContract()
    ) { viewModel.refreshPermissionsAndSync() }

    val writePermissionLauncher = rememberLauncherForActivityResult(
        contract = viewModel.healthRepository.permissionContract()
    ) { viewModel.refreshPermissionsAndSync() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelDestination = TopLevelDestination.entries.any { it.route == currentRoute }

    if (!state.onboardingCompleted) {
        OnboardingScreen(
            onComplete = { viewModel.setOnboardingCompleted(true) },
            onRequestPermissions = { permissionLauncher.launch(viewModel.healthRepository.readPermissions) }
        )
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (isTopLevelDestination) {
                    PulseBottomBar(
                        currentRoute = currentRoute,
                        onNavigateToDestination = { destination ->
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = PulseDestination.Summary.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(PulseDestination.Summary.route) {
                    SummaryScreen(
                        state = state,
                        contentPadding = padding,
                        onRange = viewModel::selectRange,
                        onWater = viewModel::addWater,
                        onEdit = { editing = true },
                        onProfile = { navController.navigate(PulseDestination.Profile.route) },
                        onDetail = { type ->
                            navController.navigate(PulseDestination.Detail.createRoute(type))
                        },
                        onExitDemo = { viewModel.setDemoMode(false) },
                        onRetry = viewModel::sync,
                        onDismissError = viewModel::clearError
                    )
                }
                composable(PulseDestination.Sharing.route) {
                    SharingScreen(
                        state = state,
                        padding = padding,
                        onAddContact = viewModel::addTrustedContact,
                        onUpdateMetrics = viewModel::updateContactMetrics,
                        onRevokeContact = viewModel::revokeContactSharing,
                        onDeleteContact = viewModel::deleteTrustedContact,
                        onExportPackage = { name, metrics, range ->
                            viewModel.createExportHealthPackage(name, metrics, range)
                        },
                        onFormatShareText = viewModel::formatShareText
                    )
                }
                composable(PulseDestination.Browse.route) {
                    BrowseScreen(
                        padding = padding,
                        metrics = state.metrics,
                        onOpen = { type ->
                            navController.navigate(PulseDestination.Detail.createRoute(type))
                        }
                    )
                }
                composable(
                    route = PulseDestination.Detail.route,
                    arguments = listOf(
                        navArgument(PulseDestination.Detail.ARG_METRIC_TYPE) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val metricType = backStackEntry.arguments?.getString(PulseDestination.Detail.ARG_METRIC_TYPE).orEmpty()
                    DetailScreen(
                        type = metricType,
                        metrics = state.metrics,
                        isDemo = state.isDemoMode,
                        selectedRange = state.selectedRange,
                        onSelectRange = viewModel::selectRange,
                        streakInfo = state.streakInfo,
                        weeklyHydrationProgress = state.weeklyHydrationProgress,
                        todayHydrationEntries = todayHydration,
                        weeklyWaterLiters = weeklyWater,
                        onAddWater = viewModel::addWater,
                        onEditWater = viewModel::editWater,
                        onDeleteWater = viewModel::deleteWater,
                        onSetWaterGoal = viewModel::setWaterGoal,
                        onBack = { navController.navigateUp() }
                    )
                }
                composable(PulseDestination.Profile.route) {
                    val coroutineScope = rememberCoroutineScope()
                    ProfileScreen(
                        state = state,
                        onBack = { navController.navigateUp() },
                        onConnect = { permissionLauncher.launch(viewModel.healthRepository.readPermissions) },
                        onSync = viewModel::sync,
                        onToggleDemo = viewModel::toggleDemoMode,
                        onToggleSyncWater = viewModel::setSyncWaterToHealthConnect,
                        onRequestWritePermissions = { writePermissionLauncher.launch(viewModel.healthRepository.writePermissions) },
                        onExportData = { onResult ->
                            coroutineScope.launch {
                                val json = viewModel.exportDataJson()
                                onResult(json)
                            }
                        },
                        onDeleteAllData = {
                            viewModel.deleteAllLocalData()
                        },
                        onSignIn = { email, password, onError ->
                            viewModel.signInWithEmail(email, password) { result ->
                                when (result) {
                                    is AuthResult.Success -> onError("")
                                    is AuthResult.Error -> onError(result.message)
                                }
                            }
                        },
                        onSignUp = { email, password, onError ->
                            viewModel.signUpWithEmail(email, password) { result ->
                                when (result) {
                                    is AuthResult.Success -> onError("")
                                    is AuthResult.Error -> onError(result.message)
                                }
                            }
                        },
                        onSignInAsGuest = {
                            viewModel.signInAsGuest()
                        },
                        onSignOut = {
                            viewModel.signOut()
                        },
                        onSyncCloud = {
                            viewModel.syncCloud()
                        },
                        onToggleReminders = viewModel::setRemindersEnabled,
                        onSetReminderInterval = viewModel::setReminderIntervalHours,
                        onSetQuietHours = viewModel::setQuietHours,
                        onSendTestReminder = { viewModel.sendTestReminder(context) }
                    )
                }
            }
        }
    }

    if (editing) {
        FavoritesSheet(state.hiddenCards, viewModel::toggleCard) { editing = false }
    }
}

@Composable
fun PulseBottomBar(
    currentRoute: String?,
    onNavigateToDestination: (TopLevelDestination) -> Unit
) {
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
            TopLevelDestination.entries.forEach { item ->
                val active = currentRoute == item.route
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onNavigateToDestination(item) }
                        .semantics {
                            role = Role.Tab
                            selected = active
                            contentDescription = "${item.label} tab"
                        }
                        .background(if (active) PulsePink.copy(alpha = 0.11f) else Color.Transparent)
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        tint = if (active) PulsePink else Muted,
                        modifier = Modifier.size(22.dp)
                    )
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
