package com.example.data.repository

import com.example.data.local.GoalDao
import com.example.data.local.GoalEntity
import com.example.model.Goal
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class GoalRepository(
    private val goalDao: GoalDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val allGoals: Flow<List<Goal>> = goalDao.getAllGoals().map { entities ->
        entities.map { it.toModel() }
    }

    val activeGoal: Flow<Goal?> = goalDao.getActiveGoal().map { entity ->
        entity?.toModel()
    }

    suspend fun createGoal(
        title: String,
        description: String,
        targetHours: Float,
        deadlineDays: Int = 7,
        milestones: List<String> = emptyList()
    ): Long = withContext(ioDispatcher) {
        val entity = GoalEntity(
            title = title,
            description = description,
            targetHours = targetHours,
            completedHours = 0f,
            isCompleted = false,
            deadlineDays = deadlineDays,
            milestones = milestones.joinToString(";;")
        )
        goalDao.insertGoal(entity)
    }

    suspend fun addFocusHours(goalId: Long, hours: Float) = withContext(ioDispatcher) {
        goalDao.addFocusTime(goalId, hours)
        // Check if completed
        val goal = goalDao.getGoalById(goalId)
        if (goal != null && goal.completedHours >= goal.targetHours && !goal.isCompleted) {
            goalDao.updateGoal(goal.copy(isCompleted = true))
        }
    }

    suspend fun ensureDefaultGoal() = withContext(ioDispatcher) {
        // Will be called on app init to seed initial goal if table empty
        val currentGoals = goalDao.getGoalById(1L)
        if (currentGoals == null) {
            goalDao.insertGoal(
                GoalEntity(
                    id = 1L,
                    title = "Complete Calculus Chapter 4",
                    description = "Integration techniques & trigonometric substitution",
                    targetHours = 5.0f,
                    completedHours = 4.0f, // 80% initial progress
                    isCompleted = false,
                    deadlineDays = 5,
                    milestones = "Integration by parts;;Partial fractions;;Trig integrals"
                )
            )
        }
    }

    private fun GoalEntity.toModel(): Goal {
        return Goal(
            id = id,
            title = title,
            description = description,
            targetHours = targetHours,
            completedHours = completedHours,
            isCompleted = isCompleted,
            deadlineDays = deadlineDays,
            milestones = if (milestones.isBlank()) emptyList() else milestones.split(";;"),
            createdAt = createdAt
        )
    }
}
