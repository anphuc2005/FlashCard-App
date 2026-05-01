package com.example.flashcardapp.data.datasource.remote.model.statistics

data class StatisticsApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

data class StatisticsSummaryDto(
    val totalStudied: Long,
    val retentionRate: Double,
    val currentStreak: Int,
    val longestStreak: Int
)

data class DailyActivityDto(
    val date: String,
    val count: Long
)

data class StudyLogItemDto(
    val id: String,
    val deckId: String,
    val cardId: String,
    val studyMode: String,
    val grade: Int,
    val studiedAt: String,
    val syncedAt: String
)

data class PageResponseDto<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Long,
    val last: Boolean,
    val first: Boolean,
    val numberOfElements: Int,
    val empty: Boolean
)
