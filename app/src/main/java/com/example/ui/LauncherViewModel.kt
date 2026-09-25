package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.EkagrataDatabase
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.AppRepository
import com.example.data.repository.FocusSessionRepository
import com.example.data.repository.GoalRepository
import com.example.data.repository.SettingsRepository
import com.example.model.AppCategory
import com.example.model.AppInfo
import com.example.model.FocusMode
import com.example.model.FocusSession
import com.example.model.Goal
import com.example.service.KendrikaranaStateHolder
import com.example.service.UsageStatsBlockerManager
import com.example.util.MindfulChimeHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface ActiveSessionState {
    object Idle : ActiveSessionState
    data class Active(
        val goalTitle: String,
        val goalId: Long?,
        val mode: FocusMode,
        val totalSeconds: Int,
        val remainingSeconds: Int,
        val isPaused: Boolean = false,
        val interruptionsAvoided: Int = 0
    ) : ActiveSessionState
    data class Completed(
        val goalTitle: String,
        val durationMinutes: Int,
        val xpEarned: Int,
        val interruptionsResisted: Int
    ) : ActiveSessionState
}

data class LauncherUiState(
    val searchQuery: String = "",
    val selectedCategory: AppCategory = AppCategory.ALL,
    val isAppDrawerOpen: Boolean = false,
    val isKendrikaranaSetupOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val isCreateGoalOpen: Boolean = false,
    val userSettings: UserSettingsEntity = UserSettingsEntity(),
    val activeGoal: Goal? = null,
    val allGoals: List<Goal> = emptyList(),
    val allApps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val favoriteApps: List<AppInfo> = emptyList(),
    val essentialApps: List<AppInfo> = emptyList(),
    val allowedFocusApps: List<AppInfo> = emptyList(),
    val todayFocusMinutes: Int = 180,
    val currentStreakDays: Int = 12,
    val totalXp: Int = 450,
    val activeSession: ActiveSessionState = ActiveSessionState.Idle,
    val blockedAppWarning: AppInfo? = null,
    val showEndEarlyConfirmation: Boolean = false
)

private data class BaseData(
    val apps: List<AppInfo> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val activeGoal: Goal? = null,
    val userSettings: UserSettingsEntity = UserSettingsEntity(),
    val todayMinutes: Int = 0,
    val totalXp: Int = 0
)

private data class UiControls(
    val searchQuery: String = "",
    val selectedCategory: AppCategory = AppCategory.ALL,
    val isAppDrawerOpen: Boolean = false,
    val isKendrikaranaSetupOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val isCreateGoalOpen: Boolean = false,
    val activeSession: ActiveSessionState = ActiveSessionState.Idle,
    val blockedAppWarning: AppInfo? = null,
    val showEndEarlyConfirmation: Boolean = false
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val database = EkagrataDatabase.getInstance(application)
    val appRepository = AppRepository(application, database.appPreferenceDao())
    val goalRepository = GoalRepository(database.goalDao())
    val focusSessionRepository = FocusSessionRepository(database.focusSessionDao())
    val settingsRepository = SettingsRepository(database.userSettingsDao())
    val usageStatsBlockerManager = UsageStatsBlockerManager(application)

    private val uiControls = MutableStateFlow(UiControls())
    private val customSettingsOverride = MutableStateFlow<UserSettingsEntity?>(null)

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            goalRepository.ensureDefaultGoal()
            focusSessionRepository.seedInitialSessionIfEmpty()
        }
    }

    private val sessionsStatsFlow = combine(
        focusSessionRepository.getTodaySessions(),
        focusSessionRepository.allSessions
    ) { todaySessions, allSessions ->
        val todayMins = todaySessions.sumOf { it.actualMinutes }
        val xp = allSessions.sumOf { it.xpEarned }
        Pair(todayMins, xp)
    }

    private val effectiveSettingsFlow = combine(
        settingsRepository.settingsFlow,
        customSettingsOverride
    ) { fromDb, override ->
        override ?: fromDb
    }

    private val baseDataFlow = combine(
        appRepository.appsFlow,
        goalRepository.allGoals,
        goalRepository.activeGoal,
        effectiveSettingsFlow,
        sessionsStatsFlow
    ) { apps, goals, activeGoalFromDao, settings, sessionStats ->
        val (todayMins, xp) = sessionStats

        // Find user-selected active goal if set in settings, otherwise DAO's active goal
        val resolvedActiveGoal = if (settings.activeGoalId != null) {
            goals.firstOrNull { it.id == settings.activeGoalId } ?: activeGoalFromDao ?: goals.firstOrNull()
        } else {
            activeGoalFromDao ?: goals.firstOrNull()
        }

        BaseData(
            apps = apps,
            goals = goals,
            activeGoal = resolvedActiveGoal,
            userSettings = settings,
            todayMinutes = if (todayMins > 0) todayMins else 180,
            totalXp = if (xp > 0) xp else 450
        )
    }


    val uiState: StateFlow<LauncherUiState> = combine(baseDataFlow, uiControls) { data, controls ->
        val query = controls.searchQuery.trim().lowercase()
        val filtered = data.apps.filter { app ->
            val matchesQuery = query.isEmpty() ||
                    app.label.lowercase().contains(query) ||
                    app.packageName.lowercase().contains(query)
            val matchesCat = when (controls.selectedCategory) {
                AppCategory.ALL -> true
                AppCategory.URVARA -> app.isAllowedInFocus || app.category == AppCategory.URVARA || app.isEssential
                else -> app.category == controls.selectedCategory && !app.isAllowedInFocus
            }
            matchesQuery && matchesCat
        }

        val favorites = data.apps.filter { it.isFavorite }
        val essentials = data.apps.filter { it.category == AppCategory.URVARA }
        val allowed = data.apps.filter { it.category == AppCategory.URVARA && it.isAllowedInFocus }

        LauncherUiState(
            searchQuery = controls.searchQuery,
            selectedCategory = controls.selectedCategory,
            isAppDrawerOpen = controls.isAppDrawerOpen,
            isKendrikaranaSetupOpen = controls.isKendrikaranaSetupOpen,
            isSettingsOpen = controls.isSettingsOpen,
            isCreateGoalOpen = controls.isCreateGoalOpen,
            userSettings = data.userSettings,
            activeGoal = data.activeGoal,
            allGoals = data.goals,
            allApps = data.apps,
            filteredApps = filtered,
            favoriteApps = favorites,
            essentialApps = essentials,
            allowedFocusApps = allowed,
            todayFocusMinutes = data.todayMinutes,
            currentStreakDays = 12,
            totalXp = data.totalXp,
            activeSession = controls.activeSession,
            blockedAppWarning = controls.blockedAppWarning,
            showEndEarlyConfirmation = controls.showEndEarlyConfirmation
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LauncherUiState()
    )

    fun onSearchQueryChange(query: String) {
        uiControls.update { it.copy(searchQuery = query) }
    }

    fun onCategorySelected(category: AppCategory) {
        uiControls.update { it.copy(selectedCategory = category) }
    }

    fun toggleAppDrawer(open: Boolean) {
        uiControls.update {
            it.copy(
                isAppDrawerOpen = open,
                searchQuery = if (!open) "" else it.searchQuery
            )
        }
    }

    fun toggleKendrikaranaSetup(open: Boolean) {
        uiControls.update { it.copy(isKendrikaranaSetupOpen = open) }
    }

    fun toggleSettings(open: Boolean) {
        uiControls.update { it.copy(isSettingsOpen = open) }
    }

    fun toggleCreateGoal(open: Boolean) {
        uiControls.update { it.copy(isCreateGoalOpen = open) }
    }

    fun saveUserSettings(settings: UserSettingsEntity) {
        customSettingsOverride.value = settings
        viewModelScope.launch {
            settingsRepository.saveSettings(settings)
        }
    }

    fun resetUserSettings() {
        val defaults = UserSettingsEntity()
        customSettingsOverride.value = defaults
        viewModelScope.launch {
            settingsRepository.resetToDefaults()
        }
    }

    fun toggleFavorite(packageName: String) {
        viewModelScope.launch {
            appRepository.toggleFavorite(packageName)
        }
    }

    fun toggleAllowedInFocus(packageName: String) {
        viewModelScope.launch {
            appRepository.toggleAllowedInFocus(packageName)
        }
    }

    fun setUrvaraAllowed(packageName: String, allowed: Boolean) {
        viewModelScope.launch {
            appRepository.setUrvaraAllowed(packageName, allowed)
        }
    }

    fun toggleEssential(packageName: String) {
        viewModelScope.launch {
            appRepository.toggleEssential(packageName)
        }
    }

    fun refreshApps() {
        appRepository.refreshInstalledApps()
    }

    var isLaunchingAllowedApp: Boolean = false

    fun onAppClicked(context: Context, app: AppInfo) {
        val currentSession = uiControls.value.activeSession
        if (currentSession is ActiveSessionState.Active) {
            val isUrvaraApp = app.category == AppCategory.URVARA && app.isAllowedInFocus
            if (!isUrvaraApp) {
                // Strictly block all non-Urvarā apps during Kendrīkaraṇa
                uiControls.update { it.copy(blockedAppWarning = app) }
                return
            }
            // Only genuine allowed Urvarā apps are permitted to be used in Kendrīkaraṇa mode
            isLaunchingAllowedApp = true
        }
        appRepository.launchApp(app.packageName, app.activityName)
    }

    fun confirmLaunchBlockedApp(context: Context, app: AppInfo) {
        // Close warning and end session; do NOT launch the blocked non-Urvarā app!
        uiControls.update { it.copy(blockedAppWarning = null) }
        endKendrikaranaEarly()
    }


    fun dismissBlockedAppWarning() {
        val session = uiControls.value.activeSession
        if (session is ActiveSessionState.Active) {
            uiControls.update {
                it.copy(
                    activeSession = session.copy(
                        interruptionsAvoided = session.interruptionsAvoided + 1
                    ),
                    blockedAppWarning = null
                )
            }
        } else {
            uiControls.update { it.copy(blockedAppWarning = null) }
        }
    }

    fun requestEndEarlyConfirmation(show: Boolean = true) {
        uiControls.update { it.copy(showEndEarlyConfirmation = show) }
    }

    fun startKendrikarana(
        goalId: Long?,
        goalTitle: String,
        durationMinutes: Int,
        mode: FocusMode,
        sessionAllowedPackageNames: Set<String>? = null
    ) {
        val totalSecs = durationMinutes * 60
        val settings = uiState.value.userSettings

        if (settings.soundChimeEnabled) {
            MindfulChimeHelper.playStartChime()
        }

        if (sessionAllowedPackageNames != null) {
            viewModelScope.launch {
                appRepository.setAllowedApps(sessionAllowedPackageNames, uiState.value.allApps)
            }
        }

        val allowedPkgs = sessionAllowedPackageNames ?: uiState.value.allowedFocusApps.map { it.packageName }.toSet()
        // Activate system-wide key event consumption and strict usage stats blocker
        KendrikaranaStateHolder.setSessionActive(true, allowedPkgs)
        usageStatsBlockerManager.startMonitoring()

        uiControls.update {
            it.copy(
                activeSession = ActiveSessionState.Active(
                    goalTitle = goalTitle,
                    goalId = goalId,
                    mode = mode,
                    totalSeconds = totalSecs,
                    remainingSeconds = totalSecs,
                    isPaused = false,
                    interruptionsAvoided = 0
                ),
                isKendrikaranaSetupOpen = false,
                isAppDrawerOpen = false,
                isSettingsOpen = false,
                showEndEarlyConfirmation = false
            )
        }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val current = uiControls.value.activeSession
                if (current is ActiveSessionState.Active) {
                    if (!current.isPaused) {
                        val nextSec = current.remainingSeconds - 1
                        if (nextSec <= 0) {
                            completeSession(current)
                            break
                        } else {
                            uiControls.update {
                                it.copy(activeSession = current.copy(remainingSeconds = nextSec))
                            }
                        }
                    }
                } else {
                    break
                }
            }
        }
    }

    fun togglePauseResume() {
        val current = uiControls.value.activeSession
        if (current is ActiveSessionState.Active) {
            uiControls.update {
                it.copy(activeSession = current.copy(isPaused = !current.isPaused))
            }
        }
    }

    fun cancelKendrikarana() {
        endKendrikaranaEarly()
    }

    fun endKendrikaranaEarly() {
        val session = uiControls.value.activeSession
        timerJob?.cancel()
        KendrikaranaStateHolder.setSessionActive(false, emptySet())
        usageStatsBlockerManager.stopMonitoring()

        if (session is ActiveSessionState.Active) {
            val elapsedSeconds = session.totalSeconds - session.remainingSeconds
            val elapsedMins = (elapsedSeconds + 59) / 60
            val actualMins = if (elapsedMins > 0) elapsedMins else 1
            val hoursToAdd = actualMins / 60f

            val settings = uiState.value.userSettings
            if (settings.soundChimeEnabled) {
                MindfulChimeHelper.playCompletionChime()
            }

            viewModelScope.launch {
                focusSessionRepository.recordSession(
                    goalId = session.goalId,
                    goalTitle = session.goalTitle,
                    plannedMinutes = session.totalSeconds / 60,
                    actualMinutes = actualMins,
                    mode = session.mode,
                    isCompleted = false,
                    interruptionsAvoided = session.interruptionsAvoided
                )
                goalRepository.addFocusHours(session.goalId, hoursToAdd)

                uiControls.update {
                    it.copy(
                        activeSession = ActiveSessionState.Completed(
                            goalTitle = session.goalTitle,
                            durationMinutes = actualMins,
                            xpEarned = 50 + (actualMins * 2),
                            interruptionsResisted = session.interruptionsAvoided
                        ),
                        showEndEarlyConfirmation = false
                    )
                }
            }
        } else {
            uiControls.update {
                it.copy(
                    activeSession = ActiveSessionState.Idle,
                    showEndEarlyConfirmation = false
                )
            }
        }
    }

    private fun completeSession(session: ActiveSessionState.Active) {
        timerJob?.cancel()
        KendrikaranaStateHolder.setSessionActive(false, emptySet())
        usageStatsBlockerManager.stopMonitoring()

        val plannedMins = session.totalSeconds / 60
        val actualMins = if (plannedMins > 0) plannedMins else 1

        val settings = uiState.value.userSettings
        if (settings.soundChimeEnabled) {
            MindfulChimeHelper.playCompletionChime()
        }

        viewModelScope.launch {
            focusSessionRepository.recordSession(
                goalId = session.goalId,
                goalTitle = session.goalTitle,
                plannedMinutes = plannedMins,
                actualMinutes = actualMins,
                mode = session.mode,
                isCompleted = true,
                interruptionsAvoided = session.interruptionsAvoided
            )
            goalRepository.addFocusHours(session.goalId, actualMins / 60f)

            uiControls.update {
                it.copy(
                    activeSession = ActiveSessionState.Completed(
                        goalTitle = session.goalTitle,
                        durationMinutes = actualMins,
                        xpEarned = 100 + (session.interruptionsAvoided * 10),
                        interruptionsResisted = session.interruptionsAvoided
                    ),
                    showEndEarlyConfirmation = false
                )
            }
        }
    }

    fun showBlockedAppIntervention(packageName: String) {
        val app = uiState.value.allApps.firstOrNull { it.packageName == packageName }
            ?: AppInfo(
                packageName = packageName,
                activityName = "",
                label = packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
            )
        uiControls.update {
            it.copy(
                blockedAppWarning = app,
                showEndEarlyConfirmation = true
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        KendrikaranaStateHolder.setSessionActive(false, emptySet())
        usageStatsBlockerManager.stopMonitoring()
    }

    fun playMotivationalSound() {
        MindfulChimeHelper.playMotivationalChime()
    }


    fun dismissCompletedSession() {
        uiControls.update { it.copy(activeSession = ActiveSessionState.Idle) }
    }

    fun createGoal(title: String, description: String, targetHours: Float, deadlineDays: Int) {
        viewModelScope.launch {
            goalRepository.createGoal(
                title = title.trim(),
                description = description.trim(),
                targetHours = targetHours,
                deadlineDays = deadlineDays
            )
            uiControls.update { it.copy(isCreateGoalOpen = false) }
        }
    }
}
