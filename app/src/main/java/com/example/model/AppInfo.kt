package com.example.model

enum class AppCategory(val displayName: String, val categoryTitle: String = displayName) {
    ALL("All", "All"),
    URVARA("Urvara", "Urvarā"),
    STUDY("Study", "Study"),
    WORK("Work", "Work"),
    COMMUNICATION("Communication", "Communication"),
    ENTERTAINMENT("Entertainment", "Entertainment"),
    SOCIAL("Social", "Social"),
    GAMES("Games", "Games"),
    OTHER("Other", "Other")
}


data class AppInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val isFavorite: Boolean = false,
    val isAllowedInFocus: Boolean = false,
    val isEssential: Boolean = false,
    val category: AppCategory = AppCategory.OTHER,
    val installTime: Long = 0L
)
