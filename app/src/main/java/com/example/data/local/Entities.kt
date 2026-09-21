package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_preferences")
data class AppPreferenceEntity(
    @PrimaryKey val packageName: String,
    val isFavorite: Boolean = false,
    val isAllowedInFocus: Boolean = false,
    val isEssential: Boolean = false,
    val customCategory: String? = null
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String = "",
    val targetHours: Float,
    val completedHours: Float = 0f,
    val isCompleted: Boolean = false,
    val deadlineDays: Int = 7,
    val milestones: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val goalId: Long?,
    val goalTitle: String,
    val plannedMinutes: Int,
    val actualMinutes: Int,
    val mode: String,
    val isCompleted: Boolean,
    val interruptionsAvoided: Int = 0,
    val xpEarned: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val dailyTargetMinutes: Int = 180,
    val defaultFocusDurationMinutes: Int = 45,
    val strictModeEnabled: Boolean = false,
    val soundChimeEnabled: Boolean = true,
    val sanskritMantrasEnabled: Boolean = true,
    val keepScreenOn: Boolean = true,
    val activeGoalId: Long? = null,
    val is24HourFormat: Boolean = false
)

