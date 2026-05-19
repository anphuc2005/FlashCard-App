package com.example.flashcardapp.presentation.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.R
import com.example.flashcardapp.data.datasource.remote.model.statistics.AchievementDto
import com.example.flashcardapp.data.datasource.remote.model.statistics.DashboardDataDto
import com.example.flashcardapp.data.repository.StatisticsRepository
import com.example.flashcardapp.domain.model.statistics.DeckStatistics
import com.example.flashcardapp.domain.model.statistics.StatisticsOverview
import com.example.flashcardapp.domain.model.statistics.TimeStatistics
import com.example.flashcardapp.presentation.feature.statistics.model.StatisticAchievementItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.math.roundToInt

const val STAT_RANGE_DAY = "day"
const val STAT_RANGE_WEEK = "week"
const val STAT_RANGE_MONTH = "month"
private const val FEATURED_ACHIEVEMENT_COUNT = 3

class StatisticViewModel(
    private val repository: StatisticsRepository
) : ViewModel() {

    private val formatter = StatisticFormatter()
    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow(StatisticUiState())
    val uiState: kotlinx.coroutines.flow.StateFlow<StatisticUiState> = _uiState

    private var activeRange: String = STAT_RANGE_WEEK
    private var loadJob: Job? = null
    private val localAchievementPlaceholders = listOf(
        achievementTemplate("first_study", "Khoi dau", 0, 1, R.drawable.ic_book),
        achievementTemplate("streak_7", "Chuyen can", 0, 7, R.drawable.ic_fire),
        achievementTemplate("review_100", "On tap", 0, 100, R.drawable.ic_check),
        achievementTemplate("deck_5", "Nha suu tam", 0, 5, R.drawable.ic_deck)
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
        loadJob?.cancel()
        val current = _uiState.value
        val placeholderAchievements = current.achievements.ifEmpty {
            localAchievementPlaceholders.take(FEATURED_ACHIEVEMENT_COUNT)
        }
        _uiState.value = current.copy(
            isLoading = true,
            errorMessage = null,
            achievements = placeholderAchievements,
            allAchievements = current.allAchievements.ifEmpty { localAchievementPlaceholders }
        )

        loadJob = viewModelScope.launch {
            val days = rangeToDays(range)
            val (dashboardResult, achievementsResult) = supervisorScope {
                val dashboardDeferred = async { repository.getDashboard(days) }
                val achievementsDeferred = async {
                    repository.getAchievements(featuredLimit = FEATURED_ACHIEVEMENT_COUNT)
                }
                dashboardDeferred.await() to achievementsDeferred.await()
            }

            dashboardResult.onSuccess { dashboard ->
                val allAchievements = resolveAchievements(
                    fullAchievements = achievementsResult.getOrNull(),
                    dashboard = dashboard
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isInitialized = true,
                    overview = buildOverview(dashboard),
                    timeStatistics = buildTimeStatistics(range, dashboard),
                    deckStatistics = buildDeckStatistics(dashboard),
                    achievements = allAchievements.take(FEATURED_ACHIEVEMENT_COUNT),
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
            val achievementsDeferred = async {
                repository.getAchievements(featuredLimit = FEATURED_ACHIEVEMENT_COUNT)
            }
            val decksDeferred = async { repository.getDeckStatistics() }
            val rangeDeferred = async { repository.getRangeSummary(days) }
            Triple(
                achievementsDeferred.await(),
                decksDeferred.await(),
                rangeDeferred.await()
            )
        }

        if (rangeResult.isFailure && achievementsResult.isFailure && decksResult.isFailure) {
            keepExistingStatisticsOrShowEmptyState()
            return
        }

        val range = rangeResult.getOrNull()
        val decks = decksResult.getOrDefault(emptyList())
        val overview = StatisticsOverview(
            userId = "me",
            userName = "Ban",
            xpToday = range?.totalStudied ?: 0,
            streakDays = 0,
            totalDecks = decks.size,
            totalCards = decks.sumOf { it.totalCards },
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
            rawDates = emptyList(),
            values = emptyList(),
            totalStudySessions = range?.activeDays ?: 0,
            totalReviewedCards = range?.totalStudied ?: 0,
            totalStudyMinutes = range?.studyTimeMinutes ?: 0
        )

        val deckStatistics = decks.map {
            DeckStatistics(
                deckId = it.deckId,
                deckName = it.deckName,
                totalCards = it.totalCards,
                learnedCards = it.learnedCards,
                progressPercent = it.progressPercent,
                reviewCount = it.reviewCount
            )
        }

        val allAchievements = mapAchievements(achievementsResult.getOrDefault(emptyList()))
            .ifEmpty { localAchievementPlaceholders }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isInitialized = true,
            overview = overview,
            timeStatistics = timeStatistics,
            deckStatistics = deckStatistics,
            achievements = allAchievements.take(FEATURED_ACHIEVEMENT_COUNT),
            allAchievements = allAchievements,
            errorMessage = null
        )
    }

    private fun keepExistingStatisticsOrShowEmptyState() {
        val currentState = _uiState.value
        _uiState.value = if (currentState.overview != null && currentState.timeStatistics != null) {
            currentState.copy(
                isLoading = false,
                isInitialized = true,
                errorMessage = null
            )
        } else {
            currentState.copy(
                isLoading = false,
                isInitialized = true,
                overview = emptyOverview(),
                timeStatistics = emptyTimeStatistics(),
                deckStatistics = emptyList(),
                achievements = localAchievementPlaceholders.take(FEATURED_ACHIEVEMENT_COUNT),
                allAchievements = localAchievementPlaceholders,
                errorMessage = null
            )
        }
    }

    private fun emptyOverview(): StatisticsOverview {
        return StatisticsOverview(
            userId = "me",
            userName = "Ban",
            xpToday = 0,
            streakDays = 0,
            totalDecks = 0,
            totalCards = 0,
            learnedCards = 0,
            unlearnedCards = 0,
            reviewCount = 0,
            accuracy = 0.0,
            correctAnswers = 0,
            wrongAnswers = 0
        )
    }

    private fun emptyTimeStatistics(): TimeStatistics {
        return TimeStatistics(
            range = activeRange,
            labels = emptyList(),
            rawDates = emptyList(),
            values = emptyList(),
            totalStudySessions = 0,
            totalReviewedCards = 0,
            totalStudyMinutes = 0
        )
    }

    private fun resolveAchievements(
        fullAchievements: List<AchievementDto>?,
        dashboard: DashboardDataDto
    ): List<StatisticAchievementItem> {
        val source = fullAchievements
            ?.takeIf { it.isNotEmpty() }
            ?: dashboard.achievements?.items?.takeIf { it.isNotEmpty() }
            ?: dashboard.achievements?.featured.orEmpty()
        return mapAchievements(source).ifEmpty { localAchievementPlaceholders }
    }

    private fun buildOverview(dashboard: DashboardDataDto): StatisticsOverview {
        val summary = dashboard.summary
        val rangeSummary = dashboard.rangeSummary
        return StatisticsOverview(
            userId = "me",
            userName = "Ban",
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
    }

    private fun buildTimeStatistics(range: String, dashboard: DashboardDataDto): TimeStatistics {
        val rangeSummary = dashboard.rangeSummary
        val (labels, values) = repository.mapChart(range, dashboard.chart.orEmpty())
        return TimeStatistics(
            range = range,
            labels = labels,
            rawDates = dashboard.chart.orEmpty().map { it.date },
            values = values,
            totalStudySessions = rangeSummary?.activeDays ?: 0,
            totalReviewedCards = rangeSummary?.totalStudied ?: 0,
            totalStudyMinutes = rangeSummary?.studyTimeMinutes ?: 0
        )
    }

    private fun buildDeckStatistics(dashboard: DashboardDataDto): List<DeckStatistics> {
        return (dashboard.deckStatistics ?: emptyList()).map {
            DeckStatistics(
                deckId = it.deckId,
                deckName = it.deckName,
                totalCards = it.totalCards,
                learnedCards = it.learnedCards,
                progressPercent = it.progressPercent,
                reviewCount = it.reviewCount
            )
        }
    }

    private fun mapAchievements(items: List<AchievementDto>): List<StatisticAchievementItem> {
        return items.map { item ->
            val target = item.target.coerceAtLeast(1)
            val current = item.current.coerceAtMost(target)
            val progressPercent = ((current.toFloat() / target.toFloat()) * 100f).roundToInt()
            val derivedUnlocked = item.isUnlocked || current >= target || progressPercent >= 100
            StatisticAchievementItem(
                title = item.title,
                description = "${formatter.formatNumber(current)}/${formatter.formatNumber(target)}",
                introduction = item.introduction ?: item.description,
                condition = item.condition ?: item.description,
                iconResId = iconFromKey(item.iconKey),
                isUnlocked = derivedUnlocked,
                progressPercent = progressPercent
            )
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
            "achievement_first_study",
            "achievement_learned_50",
            "achievement_learned_100",
            "achievement_learned_200",
            "achievement_learned_500",
            "achievement_learned_1000",
            "achievement_learned_2000",
            "achievement_learned_5000" -> R.drawable.ic_book

            "achievement_streak_3",
            "achievement_streak_7",
            "achievement_streak_14",
            "achievement_streak_30",
            "achievement_streak_60",
            "achievement_streak_100",
            "achievement_streak_365" -> R.drawable.ic_fire_achivement

            "achievement_deck_first",
            "achievement_deck_5",
            "achievement_deck_10",
            "achievement_deck_20" -> R.drawable.ic_deck

            "achievement_review_50",
            "achievement_review_100",
            "achievement_review_500",
            "achievement_review_1000",
            "achievement_review_5000",
            "achievement_retention_80",
            "achievement_retention_90",
            "achievement_retention_95" -> R.drawable.ic_pen_achivement

            "achievement_time_60",
            "achievement_time_300",
            "achievement_time_1000" -> R.drawable.ic_time

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
