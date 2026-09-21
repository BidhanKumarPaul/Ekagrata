package com.example.data.repository

import com.example.data.local.FocusSessionDao
import com.example.data.local.FocusSessionEntity
import com.example.model.FocusMode
import com.example.model.FocusSession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar

class FocusSessionRepository(
    private val sessionDao: FocusSessionDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val allSessions: Flow<List<FocusSession>> = sessionDao.getAllSessions().map { entities ->
        entities.map { it.toModel() }
    }

    fun getTodaySessions(): Flow<List<FocusSession>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        return sessionDao.getRecentSessions(startOfDay).map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun recordSession(
        goalId: Long?,
        goalTitle: String,
        plannedMinutes: Int,
        actualMinutes: Int,
        mode: FocusMode,
        isCompleted: Boolean,
        interruptionsAvoided: Int
    ): Long = withContext(ioDispatcher) {
        // Transparent XP calculation: 10 XP per 10 focused minutes + 50 bonus for completing full session + 5 XP per resisted interruption
        val baseXP = (actualMinutes / 10) * 10
        val completionBonus = if (isCompleted) 50 else 10
        val interruptionBonus = interruptionsAvoided * 5
        val totalXP = baseXP + completionBonus + interruptionBonus

        val entity = FocusSessionEntity(
            goalId = goalId,
            goalTitle = goalTitle,
            plannedMinutes = plannedMinutes,
            actualMinutes = actualMinutes,
            mode = mode.name,
            isCompleted = isCompleted,
            interruptionsAvoided = interruptionsAvoided,
            xpEarned = totalXP,
            timestamp = System.currentTimeMillis()
        )
        sessionDao.insertSession(entity)
    }

    suspend fun seedInitialSessionIfEmpty() = withContext(ioDispatcher) {
        // Add sample session so new users see realistic today focus & streak metrics right away
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        sessionDao.insertSession(
            FocusSessionEntity(
                goalId = 1L,
                goalTitle = "Complete Calculus Chapter 4",
                plannedMinutes = 50,
                actualMinutes = 50,
                mode = FocusMode.DEEP.name,
                isCompleted = true,
                interruptionsAvoided = 3,
                xpEarned = 115,
                timestamp = startOfDay + (3 * 3600 * 1000)
            )
        )
    }

    private fun FocusSessionEntity.toModel(): FocusSession {
        val fMode = runCatching { FocusMode.valueOf(mode) }.getOrDefault(FocusMode.DEEP)
        return FocusSession(
            id = id,
            goalId = goalId,
            goalTitle = goalTitle,
            plannedMinutes = plannedMinutes,
            actualMinutes = actualMinutes,
            mode = fMode,
            isCompleted = isCompleted,
            interruptionsAvoided = interruptionsAvoided,
            xpEarned = xpEarned,
            timestamp = timestamp
        )
    }
}
