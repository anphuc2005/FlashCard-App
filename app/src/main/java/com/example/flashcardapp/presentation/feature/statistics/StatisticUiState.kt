package com.example.flashcardapp.presentation.feature.statistics

import com.example.flashcardapp.data.datasource.remote.model.statistics.DailyActivityDto
import com.example.flashcardapp.data.datasource.remote.model.statistics.StatisticsSummaryDto
import com.example.flashcardapp.data.datasource.remote.model.statistics.StudyLogItemDto
import com.example.flashcardapp.domain.model.statistics.DeckStatistics
import com.example.flashcardapp.presentation.feature.statistics.model.StatisticAchievementItem

data class StatisticUiState(
    val isLoading: Boolean = false,
    val isInitialized: Boolean = false,
    val summary: StatisticsSummaryDto? = null,
    val chartData: List<DailyActivityDto> = emptyList(),
    val deckStatistics: List<DeckStatistics> = emptyList(),
    val achievements: List<StatisticAchievementItem> = emptyList(),
    val allAchievements: List<StatisticAchievementItem> = emptyList(),
    val historyItems: List<StudyLogItemDto> = emptyList(),
    val historyPage: Int = 0,
    val isHistoryLoading: Boolean = false,
    val isLastHistoryPage: Boolean = false,
    val errorMessage: String? = null
)
