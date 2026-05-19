package com.example.flashcardapp.domain.model.statistics

// Domain model for achievement progress in statistics screen.
data class StatisticAchievement(
    val code: String,
    val title: String,
    val category: Category,
    val current: Int,
    val target: Int,
    val progressLabel: String,
    val isUnlocked: Boolean
) {
    enum class Category {
        LEARNED,
        STREAK,
        DECK,
        REVIEW,
        ACCURACY,
        XP,
        STUDY_TIME,
        REVIEWED_CARDS
    }
}
