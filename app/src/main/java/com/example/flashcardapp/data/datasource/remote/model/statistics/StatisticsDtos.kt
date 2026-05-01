package com.example.flashcardapp.data.datasource.remote.model.statistics

import com.example.flashcardapp.data.datasource.remote.model.ApiResponse
import com.google.gson.annotations.SerializedName

typealias StatisticsDashboardResponseDto = ApiResponse<DashboardDataDto>
typealias StatisticsAchievementResponseDto = ApiResponse<AchievementsDataDto>
typealias StatisticsDecksResponseDto = ApiResponse<DeckStatisticsPageDto>
typealias StatisticsRangeSummaryResponseDto = ApiResponse<RangeSummaryDto>

data class DashboardDataDto(
    @SerializedName("summary") val summary: SummaryDto? = null,
    @SerializedName("rangeSummary") val rangeSummary: RangeSummaryDto? = null,
    @SerializedName("chart") val chart: List<ChartPointDto>? = null,
    @SerializedName("achievements") val achievements: AchievementsDataDto? = null,
    @SerializedName("deckStatistics") val deckStatistics: List<DeckStatisticsItemDto>? = null
)

data class SummaryDto(
    @SerializedName("totalStudied") val totalStudied: Int = 0,
    @SerializedName("retentionRate") val retentionRate: Double = 0.0,
    @SerializedName("currentStreak") val currentStreak: Int = 0,
    @SerializedName("longestStreak") val longestStreak: Int = 0
)

data class RangeSummaryDto(
    @SerializedName("days") val days: Int = 0,
    @SerializedName("totalStudied") val totalStudied: Int = 0,
    @SerializedName("newCardsStudied") val newCardsStudied: Int = 0,
    @SerializedName("reviewCardsStudied") val reviewCardsStudied: Int = 0,
    @SerializedName("studyTimeMinutes") val studyTimeMinutes: Int? = null,
    @SerializedName("retentionRate") val retentionRate: Double = 0.0,
    @SerializedName("activeDays") val activeDays: Int = 0,
    @SerializedName("averagePerDay") val averagePerDay: Double = 0.0
)

data class ChartPointDto(
    @SerializedName("date") val date: String,
    @SerializedName("count") val count: Int
)

data class AchievementsDataDto(
    @SerializedName("unlockedCount") val unlockedCount: Int = 0,
    @SerializedName("totalCount") val totalCount: Int = 0,
    @SerializedName("featured") val featured: List<AchievementDto> = emptyList(),
    @SerializedName("items") val items: List<AchievementDto> = emptyList()
)

data class AchievementDto(
    @SerializedName("code") val code: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("introduction") val introduction: String? = null,
    @SerializedName("condition") val condition: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("iconKey") val iconKey: String? = null,
    @SerializedName("current") val current: Int = 0,
    @SerializedName("target") val target: Int = 0,
    @SerializedName("progressPercent") val progressPercent: Int = 0,
    @SerializedName("progressLabel") val progressLabel: String? = null,
    @SerializedName("isUnlocked") val isUnlocked: Boolean = false
)

data class DeckStatisticsPageDto(
    @SerializedName("content") val content: List<DeckStatisticsItemDto> = emptyList(),
    @SerializedName("totalPages") val totalPages: Int = 0,
    @SerializedName("totalElements") val totalElements: Int = 0,
    @SerializedName("first") val first: Boolean = true,
    @SerializedName("last") val last: Boolean = true,
    @SerializedName("numberOfElements") val numberOfElements: Int = 0,
    @SerializedName("empty") val empty: Boolean = true
)

data class DeckStatisticsItemDto(
    @SerializedName("deckId") val deckId: String,
    @SerializedName("deckName") val deckName: String,
    @SerializedName("totalCards") val totalCards: Int = 0,
    @SerializedName("learnedCards") val learnedCards: Int = 0,
    @SerializedName("progressPercent") val progressPercent: Int = 0,
    @SerializedName("reviewCount") val reviewCount: Int = 0,
    @SerializedName("retentionRate") val retentionRate: Double? = null,
    @SerializedName("lastStudiedAt") val lastStudiedAt: String? = null
)
