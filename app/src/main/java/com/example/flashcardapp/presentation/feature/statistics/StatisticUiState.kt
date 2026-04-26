package com.example.flashcardapp.presentation.feature.statistics

import com.example.flashcardapp.domain.model.statistics.DeckStatistics
import com.example.flashcardapp.domain.model.statistics.StatisticsOverview
import com.example.flashcardapp.domain.model.statistics.TimeStatistics
import com.example.flashcardapp.presentation.feature.statistics.model.StatisticAchievementItem

data class StatisticUiState(
    val isLoading: Boolean = false,
    val isInitialized: Boolean = false,
    val overview: StatisticsOverview? = null,
    val timeStatistics: TimeStatistics? = null,
    val deckStatistics: List<DeckStatistics> = emptyList(),
    val achievements: List<StatisticAchievementItem> = emptyList(),
    val allAchievements: List<StatisticAchievementItem> = emptyList(),
    val errorMessage: String? = null
)
