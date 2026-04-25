package com.example.flashcardapp.domain.model.statistics

// Domain model tổng quan các chỉ số thống kê.

data class StatisticsOverview(
    val userId: String,
    val userName: String,
    val xpToday: Int,
    val streakDays: Int,
    val totalDecks: Int,
    val totalCards: Int,
    val learnedCards: Int,
    val unlearnedCards: Int,
    val reviewCount: Int,
    val accuracy: Double,
    val correctAnswers: Int,
    val wrongAnswers: Int
)

