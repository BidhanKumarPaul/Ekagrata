package com.example.data.repository

import com.example.data.local.UserSettingsDao
import com.example.data.local.UserSettingsEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SettingsRepository(
    private val userSettingsDao: UserSettingsDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val settingsFlow: Flow<UserSettingsEntity> = userSettingsDao.getUserSettings().map { entity ->
        entity ?: UserSettingsEntity()
    }

    suspend fun getSettingsDirect(): UserSettingsEntity = withContext(ioDispatcher) {
        userSettingsDao.getUserSettingsDirect() ?: UserSettingsEntity()
    }

    suspend fun saveSettings(settings: UserSettingsEntity) = withContext(ioDispatcher) {
        userSettingsDao.saveUserSettings(settings)
    }

    suspend fun updateActiveGoalId(goalId: Long?) = withContext(ioDispatcher) {
        val current = getSettingsDirect()
        userSettingsDao.saveUserSettings(current.copy(activeGoalId = goalId))
    }

    suspend fun updateDailyTargetMinutes(minutes: Int) = withContext(ioDispatcher) {
        val current = getSettingsDirect()
        userSettingsDao.saveUserSettings(current.copy(dailyTargetMinutes = minutes))
    }

    suspend fun updateDefaultDurationMinutes(minutes: Int) = withContext(ioDispatcher) {
        val current = getSettingsDirect()
        userSettingsDao.saveUserSettings(current.copy(defaultFocusDurationMinutes = minutes))
    }

    suspend fun updateStrictMode(enabled: Boolean) = withContext(ioDispatcher) {
        val current = getSettingsDirect()
        userSettingsDao.saveUserSettings(current.copy(strictModeEnabled = enabled))
    }

    suspend fun updateSoundChime(enabled: Boolean) = withContext(ioDispatcher) {
        val current = getSettingsDirect()
        userSettingsDao.saveUserSettings(current.copy(soundChimeEnabled = enabled))
    }

    suspend fun updateSanskritMantras(enabled: Boolean) = withContext(ioDispatcher) {
        val current = getSettingsDirect()
        userSettingsDao.saveUserSettings(current.copy(sanskritMantrasEnabled = enabled))
    }

    suspend fun updateKeepScreenOn(enabled: Boolean) = withContext(ioDispatcher) {
        val current = getSettingsDirect()
        userSettingsDao.saveUserSettings(current.copy(keepScreenOn = enabled))
    }

    suspend fun resetToDefaults() = withContext(ioDispatcher) {
        userSettingsDao.saveUserSettings(UserSettingsEntity())
    }
}
