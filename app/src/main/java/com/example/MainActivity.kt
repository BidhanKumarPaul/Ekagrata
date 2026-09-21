package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ActiveSessionState
import com.example.ui.AppDrawerSheet
import com.example.ui.BlockedAppWarningDialog
import com.example.ui.CreateGoalDialog
import com.example.ui.HomeScreen
import com.example.ui.KendrikaranaActiveScreen
import com.example.ui.KendrikaranaDialog
import com.example.ui.LauncherViewModel
import com.example.ui.SessionCompletedDialog
import com.example.ui.SettingsScreen
import com.example.ui.theme.EkagrataTheme

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EkagrataTheme {
                EkagrataApp(
                    activity = this,
                    viewModel = viewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh installed applications when returning to launcher
        viewModel.refreshApps()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EkagrataApp(
    activity: ComponentActivity,
    viewModel: LauncherViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle Keep Screen Awake during Kendrīkaraṇa if enabled in Settings
    val isFocusActive = uiState.activeSession is ActiveSessionState.Active
    val keepAwakeEnabled = uiState.userSettings.keepScreenOn
    DisposableEffect(isFocusActive, keepAwakeEnabled) {
        if (isFocusActive && keepAwakeEnabled) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Back navigation handling:
    // Settings -> closes to Dashboard
    // Drawer -> closes to Dashboard
    BackHandler(enabled = uiState.isSettingsOpen) {
        viewModel.toggleSettings(false)
    }

    BackHandler(enabled = uiState.isAppDrawerOpen && !uiState.isSettingsOpen) {
        viewModel.toggleAppDrawer(false)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Dashboard: At the beginning, the dashboard is always at the starting point
        HomeScreen(
            uiState = uiState,
            onStartKendrikaranaClick = { viewModel.toggleKendrikaranaSetup(true) },
            onOpenSettingsClick = { viewModel.toggleSettings(true) },
            onOpenDrawerClick = { viewModel.toggleAppDrawer(true) },
            onAppClick = { app -> viewModel.onAppClicked(context, app) },
            onCreateGoalClick = { viewModel.toggleCreateGoal(true) }
        )

        // Settings Screen
        if (uiState.isSettingsOpen) {
            SettingsScreen(
                currentSettings = uiState.userSettings,
                goals = uiState.allGoals,
                allowedAppsCount = uiState.allowedFocusApps.size,
                totalAppsCount = uiState.allApps.size,
                onSaveSettings = { updatedSettings ->
                    viewModel.saveUserSettings(updatedSettings)
                },
                onOpenAppDrawer = {
                    viewModel.toggleAppDrawer(true)
                },
                onBackToDashboard = {
                    viewModel.toggleSettings(false)
                },
                onResetDefaults = {
                    viewModel.resetUserSettings()
                }
            )
        }

        // App Drawer Sheet
        if (uiState.isAppDrawerOpen) {
            AppDrawerSheet(
                sheetState = drawerSheetState,
                searchQuery = uiState.searchQuery,
                selectedCategory = uiState.selectedCategory,
                apps = uiState.filteredApps,
                onSearchChange = { viewModel.onSearchQueryChange(it) },
                onCategoryChange = { viewModel.onCategorySelected(it) },
                onAppClick = { app ->
                    viewModel.toggleAppDrawer(false)
                    viewModel.onAppClicked(context, app)
                },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onToggleAllowedInFocus = { viewModel.toggleAllowedInFocus(it) },
                onDismiss = { viewModel.toggleAppDrawer(false) }
            )
        }

        // Kendrīkaraṇa Setup Dialog
        if (uiState.isKendrikaranaSetupOpen) {
            KendrikaranaDialog(
                activeGoal = uiState.activeGoal,
                goals = uiState.allGoals,
                allowedApps = uiState.allowedFocusApps,
                totalAppsCount = uiState.allApps.size,
                defaultMinutes = uiState.userSettings.defaultFocusDurationMinutes,
                onStart = { goalId, goalTitle, duration, mode ->
                    viewModel.startKendrikarana(goalId, goalTitle, duration, mode)
                },
                onDismiss = { viewModel.toggleKendrikaranaSetup(false) }
            )
        }

        // Fullscreen Active Kendrīkaraṇa Mode
        val activeSession = uiState.activeSession
        if (activeSession is ActiveSessionState.Active) {
            KendrikaranaActiveScreen(
                session = activeSession,
                allowedApps = uiState.allowedFocusApps,
                showSanskritMantras = uiState.userSettings.sanskritMantrasEnabled,
                onTogglePause = { viewModel.togglePauseResume() },
                onEmergencyExit = { viewModel.cancelKendrikarana() },
                onAppClick = { app -> viewModel.onAppClicked(context, app) }
            )
        }

        // Session Completed Dialog
        if (activeSession is ActiveSessionState.Completed) {
            SessionCompletedDialog(
                session = activeSession,
                onDismiss = { viewModel.dismissCompletedSession() }
            )
        }

        // Distraction Intervention / Blocked App Friction Dialog
        uiState.blockedAppWarning?.let { blockedApp ->
            BlockedAppWarningDialog(
                app = blockedApp,
                activeGoalTitle = (activeSession as? ActiveSessionState.Active)?.goalTitle ?: "Deep Focus",
                onBackToFocus = { viewModel.dismissBlockedAppWarning() },
                onOpenAnyway = { viewModel.confirmLaunchBlockedApp(context, blockedApp) }
            )
        }

        // Create Goal Dialog
        if (uiState.isCreateGoalOpen) {
            CreateGoalDialog(
                onSave = { title, description, targetHours, deadlineDays ->
                    viewModel.createGoal(title, description, targetHours, deadlineDays)
                },
                onDismiss = { viewModel.toggleCreateGoal(false) }
            )
        }
    }
}
