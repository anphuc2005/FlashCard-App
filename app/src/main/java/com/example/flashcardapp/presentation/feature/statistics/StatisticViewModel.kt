package com.example.flashcardapp.presentation.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.R
import com.example.flashcardapp.data.datasource.remote.model.statistics.AchievementDto
import com.example.flashcardapp.data.repository.StatisticsRepository
import com.example.flashcardapp.domain.model.statistics.DeckStatistics
import com.example.flashcardapp.domain.model.statistics.StatisticsOverview
import com.example.flashcardapp.domain.model.statistics.TimeStatistics
import com.example.flashcardapp.presentation.feature.statistics.model.StatisticAchievementItem
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.math.roundToInt

const val STAT_RANGE_DAY = "day"
const val STAT_RANGE_WEEK = "week"
const val STAT_RANGE_MONTH = "month"

class StatisticViewModel(
    private val repository: StatisticsRepository
) : ViewModel() {

    private val formatter = StatisticFormatter()
    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow(StatisticUiState())
    val uiState: kotlinx.coroutines.flow.StateFlow<StatisticUiState> = _uiState

    private var activeRange: String = STAT_RANGE_WEEK
    private val localAchievementPlaceholders = listOf(
        achievementTemplate("first_study", "Khởi đầu", 0, 1, R.drawable.ic_book),
        achievementTemplate("learned_10", "10 thẻ đầu tiên", 0, 10, R.drawable.ic_book),
        achievementTemplate("learned_50", "50 thẻ đã học", 0, 50, R.drawable.ic_book),
        achievementTemplate("learned_100", "100 thẻ đã học", 0, 100, R.drawable.ic_book),
        achievementTemplate("learned_500", "500 thẻ đã học", 0, 500, R.drawable.ic_book),
        achievementTemplate("streak_3", "Chuyên cần 3 ngày", 0, 3, R.drawable.ic_fire),
        achievementTemplate("streak_7", "Chuyên cần 7 ngày", 0, 7, R.drawable.ic_fire),
        achievementTemplate("streak_30", "Chuyên cần 30 ngày", 0, 30, R.drawable.ic_fire),
        achievementTemplate("deck_first", "Bộ thẻ đầu tiên", 0, 1, R.drawable.ic_deck),
        achievementTemplate("deck_5", "Nhà sưu tầm", 0, 5, R.drawable.ic_deck),
        achievementTemplate("review_100", "100 lượt ôn tập", 0, 100, R.drawable.ic_check),
        achievementTemplate("retention_80", "Độ nhớ 80%", 0, 80, R.drawable.ic_check)
    )

    init {
        refreshData()
    }

    fun refreshData() {
        loadStatistics(activeRange)
    }

    fun changeRange(range: String) {
        activeRange = normalizeRange(range)
        loadStatistics(activeRange)
    }

    private fun loadStatistics(range: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            achievements = if (_uiState.value.achievements.isEmpty()) localAchievementPlaceholders else _uiState.value.achievements,
            allAchievements = if (_uiState.value.allAchievements.isEmpty()) localAchievementPlaceholders else _uiState.value.allAchievements
        )

        viewModelScope.launch {
            val days = rangeToDays(range)
            val dashboardResult = repository.getDashboard(days)

            dashboardResult.onSuccess { dashboard ->
                val summary = dashboard.summary
                val rangeSummary = dashboard.rangeSummary
                val chart = dashboard.chart.orEmpty()
                val (labels, values) = repository.mapChart(range, chart)

                val overview = StatisticsOverview(
                    userId = "me",
                    userName = "Bạn",
                    xpToday = rangeSummary?.totalStudied ?: 0,
                    streakDays = summary?.currentStreak ?: 0,
                    totalDecks = dashboard.deckStatistics?.size ?: 0,
                    totalCards = dashboard.deckStatistics?.sumOf { it.totalCards } ?: 0,
                    learnedCards = summary?.totalStudied ?: 0,
                    unlearnedCards = 0,
                    reviewCount = rangeSummary?.reviewCardsStudied ?: 0,
                    accuracy = summary?.retentionRate ?: 0.0,
                    correctAnswers = 0,
                    wrongAnswers = 0
                )

                val timeStatistics = TimeStatistics(
                    range = range,
                    labels = labels,
                    values = values,
                    totalStudySessions = rangeSummary?.activeDays ?: 0,
                    totalReviewedCards = rangeSummary?.totalStudied ?: 0,
                    totalStudyMinutes = rangeSummary?.studyTimeMinutes ?: 0
                )

                val deckStatistics = (dashboard.deckStatistics ?: emptyList()).map {
                    DeckStatistics(
                        deckId = it.deckId,
                        deckName = it.deckName,
                        totalCards = it.totalCards,
                        learnedCards = it.learnedCards,
                        progressPercent = it.progressPercent,
                        reviewCount = it.reviewCount
                    )
                }

                val allAchievements = mergeWithTemplates(
                    items = dashboard.achievements?.items.orEmpty().ifEmpty {
                        dashboard.achievements?.featured.orEmpty()
                    }
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isInitialized = true,
                    overview = overview,
                    timeStatistics = timeStatistics,
                    deckStatistics = deckStatistics,
                    achievements = allAchievements.take(4),
                    allAchievements = allAchievements,
                    errorMessage = null
                )
            }.onFailure {
                loadFallbackEndpoints(days)
            }
        }
    }

    private suspend fun loadFallbackEndpoints(days: Int) {
        val (achievementsResult, decksResult, rangeResult) = supervisorScope {
            val achievementsDeferred = async { repository.getAchievements(featuredLimit = 4) }
            val decksDeferred = async { repository.getDeckStatistics() }
            val rangeDeferred = async { repository.getRangeSummary(days) }
            Triple(
                achievementsDeferred.await(),
                decksDeferred.await(),
                rangeDeferred.await()
            )
        }

        if (rangeResult.isFailure && achievementsResult.isFailure && decksResult.isFailure) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isInitialized = true,
                errorMessage = rangeResult.exceptionOrNull()?.message
                    ?: achievementsResult.exceptionOrNull()?.message
                    ?: decksResult.exceptionOrNull()?.message
                    ?: "Không tải được dữ liệu thống kê"
            )
            return
        }

        val range = rangeResult.getOrNull()
        val overview = StatisticsOverview(
            userId = "me",
            userName = "Bạn",
            xpToday = range?.totalStudied ?: 0,
            streakDays = 0,
            totalDecks = decksResult.getOrDefault(emptyList()).size,
            totalCards = decksResult.getOrDefault(emptyList()).sumOf { it.totalCards },
            learnedCards = range?.totalStudied ?: 0,
            unlearnedCards = 0,
            reviewCount = range?.reviewCardsStudied ?: 0,
            accuracy = range?.retentionRate ?: 0.0,
            correctAnswers = 0,
            wrongAnswers = 0
        )

        val timeStatistics = TimeStatistics(
            range = activeRange,
            labels = emptyList(),
            values = emptyList(),
            totalStudySessions = range?.activeDays ?: 0,
            totalReviewedCards = range?.totalStudied ?: 0,
            totalStudyMinutes = range?.studyTimeMinutes ?: 0
        )

        val deckStatistics = decksResult.getOrDefault(emptyList()).map {
            DeckStatistics(
                deckId = it.deckId,
                deckName = it.deckName,
                totalCards = it.totalCards,
                learnedCards = it.learnedCards,
                progressPercent = it.progressPercent,
                reviewCount = it.reviewCount
            )
        }

        val allAchievements = mergeWithTemplates(achievementsResult.getOrDefault(emptyList()))

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isInitialized = true,
            overview = overview,
            timeStatistics = timeStatistics,
            deckStatistics = deckStatistics,
            achievements = allAchievements.take(4),
            allAchievements = allAchievements,
            errorMessage = null
        )
    }

    private fun mapAchievements(items: List<AchievementDto>): List<StatisticAchievementItem> {
        return items.map { item ->
            val target = item.target.coerceAtLeast(1)
            val current = item.current.coerceAtMost(target)
            val progressPercent = ((current.toFloat() / target.toFloat()) * 100f).roundToInt()
            StatisticAchievementItem(
                title = item.title,
                description = "${formatter.formatNumber(current)}/${formatter.formatNumber(target)}",
                introduction = item.introduction ?: item.description,
                condition = item.condition ?: item.description,
                iconResId = iconFromKey(item.iconKey),
                isUnlocked = item.isUnlocked,
                progressPercent = progressPercent
            )
        }
    }

    private fun mergeWithTemplates(items: List<AchievementDto>): List<StatisticAchievementItem> {
        if (items.isEmpty()) return localAchievementPlaceholders
        val mapped = mapAchievements(items)
        val mappedByKey = mapped.associateBy { it.title.lowercase() }
        return localAchievementPlaceholders.map { template ->
            mappedByKey[template.title.lowercase()] ?: template
        }
    }

    private fun achievementTemplate(
        code: String,
        title: String,
        current: Int,
        target: Int,
        iconRes: Int
    ): StatisticAchievementItem {
        val safeTarget = target.coerceAtLeast(1)
        val progressPercent = ((current.toFloat() / safeTarget.toFloat()) * 100f).roundToInt()
        return StatisticAchievementItem(
            title = title,
            description = "${formatter.formatNumber(current)}/${formatter.formatNumber(safeTarget)}",
            introduction = code,
            condition = code,
            iconResId = iconRes,
            isUnlocked = false,
            progressPercent = progressPercent
        )
    }

    private fun iconFromKey(iconKey: String?): Int {
        return when (iconKey) {
            "achievement_first_study", "achievement_learned_50", "achievement_learned_100", "achievement_learned_500", "achievement_learned_1000" -> R.drawable.ic_book
            "achievement_streak_3", "achievement_streak_7", "achievement_streak_30" -> R.drawable.ic_fire
            "achievement_deck_first", "achievement_deck_5" -> R.drawable.ic_deck
            "achievement_retention_80" -> R.drawable.ic_check
            else -> R.drawable.ic_trophy
        }
    }

    private fun normalizeRange(range: String): String {
        return when (range.lowercase()) {
            STAT_RANGE_DAY -> STAT_RANGE_DAY
            STAT_RANGE_MONTH -> STAT_RANGE_MONTH
            else -> STAT_RANGE_WEEK
        }
    }

    private fun rangeToDays(range: String): Int {
        return when (range) {
            STAT_RANGE_DAY -> 1
            STAT_RANGE_MONTH -> 30
            else -> 7
        }
    }
}
