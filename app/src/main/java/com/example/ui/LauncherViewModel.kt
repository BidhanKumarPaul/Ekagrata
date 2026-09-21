package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.EkagrataDatabase
import com.example.data.repository.AppRepository
import com.example.data.repository.FocusSessionRepository
import com.example.data.repository.GoalRepository
import com.example.model.AppCategory
import com.example.model.AppInfo
import com.example.model.FocusMode
import com.example.model.FocusSession
import com.example.model.Goal
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
    val isTapasyaSetupOpen: Boolean = false,
    val isCreateGoalOpen: Boolean = false,
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
    val blockedAppWarning: AppInfo? = null
)

private data class BaseData(
    val apps: List<AppInfo> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val activeGoal: Goal? = null,
    val todayMinutes: Int = 0,
    val totalXp: Int = 0
)

private data class UiControls(
    val searchQuery: String = "",
    val selectedCategory: AppCategory = AppCategory.ALL,
    val isAppDrawerOpen: Boolean = false,
    val isTapasyaSetupOpen: Boolean = false,
    val isCreateGoalOpen: Boolean = false,
    val activeSession: ActiveSessionState = ActiveSessionState.Idle,
    val blockedAppWarning: AppInfo? = null
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val database = EkagrataDatabase.getInstance(application)
    val appRepository = AppRepository(application, database.appPreferenceDao())
    val goalRepository = GoalRepository(database.goalDao())
    val focusSessionRepository = FocusSessionRepository(database.focusSessionDao())

    private val uiControls = MutableStateFlow(UiControls())

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            goalRepository.ensureDefaultGoal()
            focusSessionRepository.seedInitialSessionIfEmpty()
        }
    }

    private val baseDataFlow = combine(
        appRepository.appsFlow,
        goalRepository.allGoals,
        goalRepository.activeGoal,
        focusSessionRepository.getTodaySessions(),
        focusSessionRepository.allSessions
    ) { apps, goals, activeGoal, todaySessions, allSessions ->
        val todayMins = todaySessions.sumOf { it.actualMinutes }
        val xp = allSessions.sumOf { it.xpEarned }
        BaseData(
            apps = apps,
            goals = goals,
            activeGoal = activeGoal ?: goals.firstOrNull(),
            todayMinutes = if (todayMins > 0) todayMins else 222,
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
                AppCategory.ESSENTIAL -> app.isEssential
                else -> app.category == controls.selectedCategory
            }
            matchesQuery && matchesCat
        }

        val favorites = data.apps.filter { it.isFavorite }
        val essentials = data.apps.filter { it.isEssential }.ifEmpty {
            data.apps.filter { it.category == AppCategory.ESSENTIAL || it.category == AppCategory.STUDY }.take(5)
        }
        val allowed = data.apps.filter { it.isAllowedInFocus }

        LauncherUiState(
            searchQuery = controls.searchQuery,
            selectedCategory = controls.selectedCategory,
            isAppDrawerOpen = controls.isAppDrawerOpen,
            isTapasyaSetupOpen = controls.isTapasyaSetupOpen,
            isCreateGoalOpen = controls.isCreateGoalOpen,
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
            blockedAppWarning = controls.blockedAppWarning
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

    fun toggleTapasyaSetup(open: Boolean) {
        uiControls.update { it.copy(isTapasyaSetupOpen = open) }
    }

    fun toggleCreateGoal(open: Boolean) {
        uiControls.update { it.copy(isCreateGoalOpen = open) }
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

    fun toggleEssential(packageName: String) {
        viewModelScope.launch {
            appRepository.toggleEssential(packageName)
        }
    }

    fun refreshApps() {
        appRepository.refreshInstalledApps()
    }

    fun onAppClicked(context: Context, app: AppInfo) {
        val currentSession = uiControls.value.activeSession
        if (currentSession is ActiveSessionState.Active && !app.isAllowedInFocus) {
            uiControls.update { it.copy(blockedAppWarning = app) }
            return
        }
        appRepository.launchApp(app.packageName, app.activityName)
    }

    fun confirmLaunchBlockedApp(context: Context, app: AppInfo) {
        val session = uiControls.value.activeSession
        if (session is ActiveSessionState.Active) {
            uiControls.update {
                it.copy(
                    activeSession = session.copy(
                        interruptionsAvoided = (session.interruptionsAvoided - 1).coerceAtLeast(0)
                    ),
                    blockedAppWarning = null
                )
            }
        } else {
            uiControls.update { it.copy(blockedAppWarning = null) }
        }
        appRepository.launchApp(app.packageName, app.activityName)
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

    fun startTapasya(goalId: Long?, goalTitle: String, durationMinutes: Int, mode: FocusMode) {
        val totalSecs = durationMinutes * 60
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
                isTapasyaSetupOpen = false,
                isAppDrawerOpen = false
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

    fun cancelTapasya() {
        timerJob?.cancel()
        uiControls.update { it.copy(activeSession = ActiveSessionState.Idle) }
    }

    private fun completeSession(session: ActiveSessionState.Active) {
        timerJob?.cancel()
        val durationMins = (session.totalSeconds - session.remainingSeconds) / 60
        val actualMins = if (durationMins > 0) durationMins else (session.totalSeconds / 60)

        viewModelScope.launch {
            val xp = focusSessionRepository.recordSession(
                goalId = session.goalId,
                goalTitle = session.goalTitle,
                plannedMinutes = session.totalSeconds / 60,
                actualMinutes = actualMins,
                mode = session.mode,
                isCompleted = true,
                interruptionsAvoided = session.interruptionsAvoided
            )
            session.goalId?.let { gid ->
                goalRepository.addFocusHours(gid, actualMins / 60f)
            }

            uiControls.update {
                it.copy(
                    activeSession = ActiveSessionState.Completed(
                        goalTitle = session.goalTitle,
                        durationMinutes = actualMins,
                        xpEarned = 100 + (session.interruptionsAvoided * 10),
                        interruptionsResisted = session.interruptionsAvoided
                    )
                )
            }
        }
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
