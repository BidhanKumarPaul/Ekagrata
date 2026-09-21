package com.example.model

enum class AppCategory(val displayName: String) {
    ALL("All"),
    ESSENTIAL("Essential"),
    STUDY("Study"),
    WORK("Work"),
    COMMUNICATION("Communication"),
    ENTERTAINMENT("Entertainment"),
    SOCIAL("Social"),
    GAMES("Games"),
    OTHER("Other")
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
