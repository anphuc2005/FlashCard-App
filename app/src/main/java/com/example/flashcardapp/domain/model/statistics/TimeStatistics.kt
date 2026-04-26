package com.example.flashcardapp.domain.model.statistics

// Domain model thống kê theo mốc thời gian.

data class TimeStatistics(
    val range: String,
    val labels: List<String>,
    val values: List<Int>,
    val totalStudySessions: Int,
    val totalReviewedCards: Int,
    val totalStudyMinutes: Int
)

