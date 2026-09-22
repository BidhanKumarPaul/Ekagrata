package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppPreferenceDao {
    @Query("SELECT * FROM app_preferences")
    fun getAllPreferences(): Flow<List<AppPreferenceEntity>>

    @Query("SELECT * FROM app_preferences WHERE packageName = :packageName LIMIT 1")
    suspend fun getPreference(packageName: String): AppPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPreference(preference: AppPreferenceEntity)

    @Query("UPDATE app_preferences SET isFavorite = :isFavorite WHERE packageName = :packageName")
    suspend fun updateFavorite(packageName: String, isFavorite: Boolean)

    @Query("UPDATE app_preferences SET isAllowedInFocus = :isAllowed WHERE packageName = :packageName")
    suspend fun updateAllowedInFocus(packageName: String, isAllowed: Boolean)

    @Query("UPDATE app_preferences SET isEssential = :isEssential WHERE packageName = :packageName")
    suspend fun updateEssential(packageName: String, isEssential: Boolean)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE isCompleted = 0 ORDER BY createdAt DESC LIMIT 1")
    fun getActiveGoal(): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("UPDATE goals SET completedHours = completedHours + :additionalHours WHERE id = :goalId")
    suspend fun addFocusTime(goalId: Long, additionalHours: Float)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: Long)

    @Query("DELETE FROM goals WHERE id NOT IN (SELECT id FROM goals ORDER BY createdAt DESC LIMIT 7)")
    suspend fun pruneGoalsKeepNewest7()
}


@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getRecentSessions(sinceTimestamp: Long): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long
}

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getUserSettingsDirect(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSettings(settings: UserSettingsEntity)
}

