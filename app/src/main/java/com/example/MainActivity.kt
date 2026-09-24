package com.example

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.KendrikaranaStateHolder
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

    companion object {
        const val ACTION_END_KENDRIKARANA = "com.example.action.END_KENDRIKARANA"
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
    }

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Listen for accessibility service redirection to trigger End Kendrīkaraṇa dialog
        KendrikaranaStateHolder.addEndKendrikaranaListener {
            runOnUiThread {
                if (viewModel.uiState.value.activeSession is ActiveSessionState.Active) {
                    viewModel.requestEndEarlyConfirmation(true)
                }
            }
        }

        // Redirect back button to End Kendrīkaraṇa when focus is active
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.uiState.value.activeSession is ActiveSessionState.Active) {
                    viewModel.requestEndEarlyConfirmation(true)
                } else if (viewModel.uiState.value.isAppDrawerOpen) {
                    viewModel.toggleAppDrawer(false)
                } else if (viewModel.uiState.value.isSettingsOpen) {
                    viewModel.toggleSettings(false)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })

        setContent {
            EkagrataTheme {
                EkagrataApp(
                    activity = this,
                    viewModel = viewModel
                )
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (viewModel.uiState.value.activeSession is ActiveSessionState.Active) {
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_APP_SWITCH) {
                viewModel.requestEndEarlyConfirmation(true)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        viewModel.isLaunchingAllowedApp = false
        // Refresh installed applications when returning to launcher
        viewModel.refreshApps()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (viewModel.uiState.value.activeSession is ActiveSessionState.Active) {
            if (viewModel.isLaunchingAllowedApp) {
                // Legitimate switch to allowed Urvarā app: allow opening without pulling back
                viewModel.isLaunchingAllowedApp = false
                return
            }
            // Minimise button pressed: intercept, pull back to front and redirect to the button End Kendrīkaraṇa!
            viewModel.requestEndEarlyConfirmation(true)
            val bringBackIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(bringBackIntent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == ACTION_END_KENDRIKARANA) {
            val blockedPkg = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE)
            if (blockedPkg != null) {
                viewModel.showBlockedAppIntervention(blockedPkg)
            } else {
                viewModel.requestEndEarlyConfirmation(true)
            }
        } else if (viewModel.uiState.value.activeSession is ActiveSessionState.Active) {
            // Home or navigation button pressed: Redirect to End Kendrīkaraṇa!
            viewModel.requestEndEarlyConfirmation(true)
        }
        viewModel.isLaunchingAllowedApp = false
        viewModel.refreshApps()
    }
}

private enum class CurrentScreen {
    HOME,
    SETTINGS,
    APP_DRAWER,
    KENDRIKARANA_ACTIVE
}

@Composable
fun EkagrataApp(
    activity: ComponentActivity,
    viewModel: LauncherViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

    // Mutual exclusion: exactly ONE screen is active in the hierarchy at any time.
    // This guarantees that buttons from background screens can never be clicked!
    val currentScreen = when {
        uiState.activeSession is ActiveSessionState.Active -> CurrentScreen.KENDRIKARANA_ACTIVE
        uiState.isSettingsOpen -> CurrentScreen.SETTINGS
        uiState.isAppDrawerOpen -> CurrentScreen.APP_DRAWER
        else -> CurrentScreen.HOME
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            CurrentScreen.KENDRIKARANA_ACTIVE -> {
                val activeSession = uiState.activeSession as ActiveSessionState.Active
                KendrikaranaActiveScreen(
                    session = activeSession,
                    allowedApps = uiState.allowedFocusApps,
                    isStrictMode = uiState.userSettings.strictModeEnabled,
                    showSanskritMantras = uiState.userSettings.sanskritMantrasEnabled,
                    hanumanChalisaEnabled = uiState.userSettings.hanumanChalisaEnabled,
                    showExitConfirmation = uiState.showEndEarlyConfirmation,
                    onRequestExitConfirmation = { viewModel.requestEndEarlyConfirmation(it) },
                    onTogglePause = { viewModel.togglePauseResume() },
                    onEmergencyExit = { viewModel.endKendrikaranaEarly() },
                    onAppClick = { app -> viewModel.onAppClicked(context, app) }
                )
            }

            CurrentScreen.SETTINGS -> {
                SettingsScreen(
                    currentSettings = uiState.userSettings,
                    goals = uiState.allGoals,
                    allowedAppsCount = uiState.allowedFocusApps.size,
                    totalAppsCount = uiState.allApps.size,
                    onSaveSettings = { updatedSettings ->
                        viewModel.saveUserSettings(updatedSettings)
                    },
                    onOpenAppDrawer = {
                        viewModel.toggleSettings(false)
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

            CurrentScreen.APP_DRAWER -> {
                AppDrawerSheet(
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

            CurrentScreen.HOME -> {
                HomeScreen(
                    uiState = uiState,
                    onStartKendrikaranaClick = { viewModel.toggleKendrikaranaSetup(true) },
                    onOpenSettingsClick = { viewModel.toggleSettings(true) },
                    onOpenDrawerClick = { viewModel.toggleAppDrawer(true) },
                    onAppClick = { app -> viewModel.onAppClicked(context, app) },
                    onCreateGoalClick = { viewModel.toggleCreateGoal(true) }
                )
            }
        }

        // Overlay Dialogs: Kendrīkaraṇa Setup Dialog
        if (uiState.isKendrikaranaSetupOpen) {
            KendrikaranaDialog(
                activeGoal = uiState.activeGoal,
                goals = uiState.allGoals,
                allowedApps = uiState.allowedFocusApps,
                allApps = uiState.allApps,
                totalAppsCount = uiState.allApps.size,
                defaultMinutes = uiState.userSettings.defaultFocusDurationMinutes,
                onStart = { goalId, goalTitle, duration, mode, allowedPackages ->
                    viewModel.startKendrikarana(goalId, goalTitle, duration, mode, allowedPackages)
                },
                onDismiss = { viewModel.toggleKendrikaranaSetup(false) }
            )
        }

        // Overlay Dialogs: Session Completed Dialog
        val completedSession = uiState.activeSession
        if (completedSession is ActiveSessionState.Completed) {
            SessionCompletedDialog(
                session = completedSession,
                onDismiss = { viewModel.dismissCompletedSession() }
            )
        }

        // Distraction Intervention / Blocked App Friction Dialog
        uiState.blockedAppWarning?.let { blockedApp ->
            BlockedAppWarningDialog(
                app = blockedApp,
                activeGoalTitle = (uiState.activeSession as? ActiveSessionState.Active)?.goalTitle ?: "Deep Focus",
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
