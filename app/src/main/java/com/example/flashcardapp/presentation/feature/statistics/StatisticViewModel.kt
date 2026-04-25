package com.example.flashcardapp.presentation.feature.statistics

import androidx.lifecycle.ViewModel
import com.example.flashcardapp.R
import com.example.flashcardapp.domain.model.statistics.DeckStatistics
import com.example.flashcardapp.domain.model.statistics.StatisticAchievement
import com.example.flashcardapp.domain.model.statistics.StatisticsOverview
import com.example.flashcardapp.domain.model.statistics.TimeStatistics
import com.example.flashcardapp.presentation.feature.statistics.model.StatisticAchievementItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.min

const val STAT_RANGE_DAY = "day"
const val STAT_RANGE_WEEK = "week"
const val STAT_RANGE_MONTH = "month"

class StatisticViewModel : ViewModel() {

    private val formatter = StatisticFormatter()
    private val _uiState = MutableStateFlow(StatisticUiState())
    val uiState: StateFlow<StatisticUiState> = _uiState.asStateFlow()

    private var activeRange: String = STAT_RANGE_WEEK

    init {
        loadStaticStatistics(activeRange)
    }

    fun refreshData() {
        loadStaticStatistics(activeRange)
    }

    fun changeRange(range: String) {
        activeRange = normalizeRange(range)
        loadStaticStatistics(activeRange)
    }

    private fun loadStaticStatistics(range: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        runCatching {
            val overview = mockOverview()
            val deckStatistics = mockDeckStatistics()
            val timeStatistics = mockTimeStatistics(range)
            val allAchievements = buildAchievementItems(overview, timeStatistics)

            _uiState.value.copy(
                isLoading = false,
                isInitialized = true,
                overview = overview,
                timeStatistics = timeStatistics,
                deckStatistics = deckStatistics,
                achievements = allAchievements.take(4),
                allAchievements = allAchievements,
                errorMessage = null
            )
        }.onSuccess { state ->
            _uiState.value = state
        }.onFailure { throwable ->
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isInitialized = true,
                errorMessage = throwable.message ?: "Có lỗi xảy ra"
            )
        }
    }

    private fun mockOverview(): StatisticsOverview {
        return StatisticsOverview(
            userId = "guest",
            userName = "Bạn",
            xpToday = 180,
            streakDays = 7,
            totalDecks = 6,
            totalCards = 160,
            learnedCards = 102,
            unlearnedCards = 58,
            reviewCount = 220,
            accuracy = 78.5,
            correctAnswers = 172,
            wrongAnswers = 48
        )
    }

    private fun mockDeckStatistics(): List<DeckStatistics> {
        return listOf(
            DeckStatistics("d1", "English Vocabulary", 40, 30, 75, 88),
            DeckStatistics("d2", "Math Basics", 30, 18, 60, 52),
            DeckStatistics("d3", "History Facts", 45, 27, 60, 46),
            DeckStatistics("d4", "Science Concepts", 45, 27, 60, 34)
        )
    }

    private fun mockTimeStatistics(range: String): TimeStatistics {
        return when (normalizeRange(range)) {
            STAT_RANGE_DAY -> TimeStatistics(
                range = STAT_RANGE_DAY,
                labels = listOf("0h", "4h", "8h", "12h", "16h", "20h"),
                values = listOf(0, 1, 3, 4, 2, 1),
                totalStudySessions = 11,
                totalReviewedCards = 24,
                totalStudyMinutes = 65
            )

            STAT_RANGE_MONTH -> TimeStatistics(
                range = STAT_RANGE_MONTH,
                labels = (1..30).map { it.toString() },
                values = listOf(
                    1, 0, 2, 1, 3, 2, 2, 1, 0, 2,
                    1, 3, 2, 1, 2, 0, 1, 2, 3, 2,
                    1, 0, 2, 1, 3, 2, 1, 2, 1, 2
                ),
                totalStudySessions = 44,
                totalReviewedCards = 138,
                totalStudyMinutes = 530
            )

            else -> TimeStatistics(
                range = STAT_RANGE_WEEK,
                labels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN"),
                values = listOf(2, 3, 1, 4, 2, 5, 3),
                totalStudySessions = 20,
                totalReviewedCards = 76,
                totalStudyMinutes = 240
            )
        }
    }

    private fun buildAchievementItems(
        overview: StatisticsOverview,
        timeStatistics: TimeStatistics
    ): List<StatisticAchievementItem> {
        val achievements = listOf(
            StatisticAchievement("learned", "Người chinh phục", StatisticAchievement.Category.LEARNED, overview.learnedCards, 120, "thẻ", overview.learnedCards >= 120),
            StatisticAchievement("streak", "Chuỗi vàng", StatisticAchievement.Category.STREAK, overview.streakDays, 10, "ngày", overview.streakDays >= 10),
            StatisticAchievement("deck", "Nhà sưu tầm", StatisticAchievement.Category.DECK, overview.totalDecks, 10, "bộ", overview.totalDecks >= 10),
            StatisticAchievement("review", "Ôn tập bền bỉ", StatisticAchievement.Category.REVIEW, overview.reviewCount, 300, "lượt", overview.reviewCount >= 300),
            StatisticAchievement("accuracy", "Xạ thủ chính xác", StatisticAchievement.Category.ACCURACY, overview.accuracy.toInt(), 85, "%", overview.accuracy >= 85.0),
            StatisticAchievement("xp", "Năng lượng cao", StatisticAchievement.Category.XP, overview.xpToday, 250, "XP", overview.xpToday >= 250),
            StatisticAchievement("time", "Học sâu", StatisticAchievement.Category.STUDY_TIME, timeStatistics.totalStudyMinutes, 600, "phút", timeStatistics.totalStudyMinutes >= 600),
            StatisticAchievement("reviewed_cards", "Tăng tốc", StatisticAchievement.Category.REVIEWED_CARDS, timeStatistics.totalReviewedCards, 160, "thẻ", timeStatistics.totalReviewedCards >= 160)
        )

        return achievements.map { achievement ->
            val cappedProgress = min(achievement.current, achievement.target)
            val progressLabel = achievement.progressLabel
            StatisticAchievementItem(
                title = achievement.title,
                description = "${formatter.formatNumber(cappedProgress)}/${formatter.formatNumber(achievement.target)} $progressLabel",
                introduction = introductionForCategory(achievement.category),
                condition = "Đạt ${formatter.formatNumber(achievement.target)} $progressLabel",
                iconResId = iconForCategory(achievement.category),
                isUnlocked = achievement.isUnlocked
            )
        }
    }

    private fun introductionForCategory(category: StatisticAchievement.Category): String {
        return when (category) {
            StatisticAchievement.Category.LEARNED -> "Mốc tích lũy số thẻ đã học của bạn."
            StatisticAchievement.Category.STREAK -> "Mốc duy trì chuỗi ngày học liên tục."
            StatisticAchievement.Category.DECK -> "Mốc mở rộng số lượng bộ thẻ bạn đã tạo."
            StatisticAchievement.Category.REVIEW -> "Mốc tổng số lần ôn tập bạn đã thực hiện."
            StatisticAchievement.Category.ACCURACY -> "Mốc độ chính xác trong quá trình học."
            StatisticAchievement.Category.XP -> "Mốc năng lượng học tập trong hôm nay."
            StatisticAchievement.Category.STUDY_TIME -> "Mốc thời lượng học tập trong khoảng thời gian đang chọn."
            StatisticAchievement.Category.REVIEWED_CARDS -> "Mốc số thẻ bạn đã học trong khoảng thời gian đang chọn."
        }
    }

    private fun iconForCategory(category: StatisticAchievement.Category): Int {
        return when (category) {
            StatisticAchievement.Category.LEARNED -> R.drawable.ic_book
            StatisticAchievement.Category.STREAK -> R.drawable.ic_trophy
            StatisticAchievement.Category.DECK -> R.drawable.ic_deck
            StatisticAchievement.Category.REVIEW -> R.drawable.ic_fire
            StatisticAchievement.Category.ACCURACY -> R.drawable.ic_check
            StatisticAchievement.Category.XP -> R.drawable.ic_star
            StatisticAchievement.Category.STUDY_TIME -> R.drawable.ic_calendar
            StatisticAchievement.Category.REVIEWED_CARDS -> R.drawable.ic_star
        }
    }

    private fun normalizeRange(range: String): String {
        return when (range.lowercase()) {
            STAT_RANGE_DAY -> STAT_RANGE_DAY
            STAT_RANGE_MONTH -> STAT_RANGE_MONTH
            else -> STAT_RANGE_WEEK
        }
    }
}
