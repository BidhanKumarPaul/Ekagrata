package com.example

import android.os.Bundle
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ActiveSessionState
import com.example.ui.AppDrawerSheet
import com.example.ui.BlockedAppWarningDialog
import com.example.ui.CreateGoalDialog
import com.example.ui.HomeScreen
import com.example.ui.LauncherViewModel
import com.example.ui.SessionCompletedDialog
import com.example.ui.TapasyaActiveScreen
import com.example.ui.TapasyaDialog
import com.example.ui.theme.EkagrataTheme

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EkagrataTheme {
                EkagrataApp(viewModel = viewModel)
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
fun EkagrataApp(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Back handling for Android Launcher:
    // Close drawer if open, otherwise consume back to keep launcher stable
    BackHandler(enabled = uiState.isAppDrawerOpen) {
        viewModel.toggleAppDrawer(false)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Home Screen
        HomeScreen(
            uiState = uiState,
            onStartTapasyaClick = { viewModel.toggleTapasyaSetup(true) },
            onOpenDrawerClick = { viewModel.toggleAppDrawer(true) },
            onAppClick = { app -> viewModel.onAppClicked(context, app) },
            onCreateGoalClick = { viewModel.toggleCreateGoal(true) }
        )

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

        // Tapasya Setup Dialog
        if (uiState.isTapasyaSetupOpen) {
            TapasyaDialog(
                activeGoal = uiState.activeGoal,
                goals = uiState.allGoals,
                allowedApps = uiState.allowedFocusApps,
                totalAppsCount = uiState.allApps.size,
                onStart = { goalId, goalTitle, duration, mode ->
                    viewModel.startTapasya(goalId, goalTitle, duration, mode)
                },
                onDismiss = { viewModel.toggleTapasyaSetup(false) }
            )
        }

        // Fullscreen Active Tapasya Mode
        val activeSession = uiState.activeSession
        if (activeSession is ActiveSessionState.Active) {
            TapasyaActiveScreen(
                session = activeSession,
                allowedApps = uiState.allowedFocusApps,
                onTogglePause = { viewModel.togglePauseResume() },
                onEmergencyExit = { viewModel.cancelTapasya() },
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}

