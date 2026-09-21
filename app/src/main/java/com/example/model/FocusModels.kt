package com.example.model

enum class FocusMode(val title: String, val defaultMinutes: Int, val description: String) {
    QUICK("Quick Focus", 25, "Pomodoro interval for fast tasks"),
    DEEP("Deep Focus", 50, "Full immersion for complex problems"),
    STUDY("Study Session", 90, "Extended academic deep study"),
    READING("Reading Mode", 45, "Distraction-free literature & research"),
    CODING("Coding Mode", 60, "Technical flow state"),
    EXAM("Exam Prep", 120, "Strict study mode for tests"),
    CUSTOM("Custom Mode", 30, "Custom duration and rules")
}

data class Goal(
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val targetHours: Float,
    val completedHours: Float = 0f,
    val isCompleted: Boolean = false,
    val deadlineDays: Int = 7,
    val milestones: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val progressPercentage: Int
        get() = if (targetHours > 0) ((completedHours / targetHours) * 100).toInt().coerceIn(0, 100) else 0
}

data class FocusSession(
    val id: Long = 0L,
    val goalId: Long?,
    val goalTitle: String,
    val plannedMinutes: Int,
    val actualMinutes: Int,
    val mode: FocusMode,
    val isCompleted: Boolean,
    val interruptionsAvoided: Int = 0,
    val xpEarned: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
