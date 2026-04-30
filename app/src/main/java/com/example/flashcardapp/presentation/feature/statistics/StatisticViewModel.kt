package com.example.flashcardapp.presentation.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.data.repository.StatisticsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val STAT_RANGE_DAY = "day"
const val STAT_RANGE_WEEK = "week"
const val STAT_RANGE_MONTH = "month"

class StatisticViewModel(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticUiState())
    val uiState: StateFlow<StatisticUiState> = _uiState.asStateFlow()

    private var activeRange: String = STAT_RANGE_WEEK
    private var activeDeckId: String? = null

    init {
        loadStatistics(activeRange, resetHistory = true)
    }

    fun refreshData(deckId: String? = activeDeckId) {
        activeDeckId = deckId
        loadStatistics(activeRange, resetHistory = true)
    }

    fun changeRange(range: String) {
        activeRange = normalizeRange(range)
        loadStatistics(activeRange, resetHistory = false)
    }

    fun loadMoreHistory(size: Int = 20) {
        val state = _uiState.value
        if (state.isHistoryLoading || state.isLastHistoryPage) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isHistoryLoading = true)

            statisticsRepository.getHistory(page = state.historyPage, size = size)
                .onSuccess { page ->
                    _uiState.value = _uiState.value.copy(
                        historyItems = _uiState.value.historyItems + page.content,
                        historyPage = state.historyPage + 1,
                        isLastHistoryPage = page.last,
                        isHistoryLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isHistoryLoading = false,
                        errorMessage = throwable.message ?: "Không thể tải lịch sử học tập"
                    )
                }
        }
    }

    private fun loadStatistics(range: String, resetHistory: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            runCatching {
                coroutineScope {
                    val summaryDeferred = async {
                        statisticsRepository.getSummary(activeDeckId).getOrThrow()
                    }
                    val chartDeferred = async {
                        statisticsRepository
                            .getActivityChart(chartDaysForRange(range))
                            .getOrThrow()
                    }

                    summaryDeferred.await() to chartDeferred.await()
                }
            }.onSuccess { (summary, chartData) ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isInitialized = true,
                    summary = summary,
                    chartData = chartData,
                    errorMessage = null,
                    deckStatistics = emptyList(),
                    achievements = emptyList(),
                    allAchievements = emptyList()
                )

                if (resetHistory) {
                    resetAndLoadHistory()
                }
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isInitialized = true,
                    errorMessage = throwable.message ?: "Không thể tải thống kê"
                )
            }
        }
    }

    private fun resetAndLoadHistory() {
        _uiState.value = _uiState.value.copy(
            historyItems = emptyList(),
            historyPage = 0,
            isLastHistoryPage = false,
            isHistoryLoading = false
        )
        loadMoreHistory()
    }

    private fun chartDaysForRange(range: String): Int {
        return when (normalizeRange(range)) {
            STAT_RANGE_DAY -> 7
            STAT_RANGE_MONTH -> 90
            else -> 30
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
