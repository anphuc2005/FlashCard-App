package com.example.flashcardapp.data.repository

import com.example.flashcardapp.data.datasource.remote.api.StatisticsApiService
import com.example.flashcardapp.data.datasource.remote.model.statistics.AchievementDto
import com.example.flashcardapp.data.datasource.remote.model.statistics.ChartPointDto
import com.example.flashcardapp.data.datasource.remote.model.statistics.DashboardDataDto
import com.example.flashcardapp.data.datasource.remote.model.statistics.RangeSummaryDto
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class StatisticsRepository(
    private val apiService: StatisticsApiService
) {

    suspend fun getDashboard(days: Int): Result<DashboardDataDto> {
        return runCatching {
            val response = apiService.getDashboard(days = days)
            if (response.isSuccess() && response.data != null) {
                response.data
            } else {
                throw IllegalStateException(response.message ?: "Không tải được dữ liệu thống kê")
            }
        }
    }

    suspend fun getAchievements(featuredLimit: Int = 4): Result<List<AchievementDto>> {
        return runCatching {
            val response = apiService.getAchievements(featuredLimit = featuredLimit, includeLocked = true)
            if (!response.isSuccess() || response.data == null) {
                throw IllegalStateException(response.message ?: "Không tải được thành tích")
            }
            if (response.data.items.isNotEmpty()) response.data.items else response.data.featured
        }
    }

    suspend fun getDeckStatistics(): Result<List<com.example.flashcardapp.data.datasource.remote.model.statistics.DeckStatisticsItemDto>> {
        return runCatching {
            val response = apiService.getDeckStatistics(page = 0, size = 20, includeEmpty = true)
            if (!response.isSuccess() || response.data == null) {
                throw IllegalStateException(response.message ?: "Không tải được tiến độ bộ thẻ")
            }
            response.data.content
        }
    }

    suspend fun getRangeSummary(days: Int): Result<RangeSummaryDto> {
        return runCatching {
            val response = apiService.getRangeSummary(days = days)
            if (!response.isSuccess() || response.data == null) {
                throw IllegalStateException(response.message ?: "Không tải được hoạt động học tập")
            }
            response.data
        }
    }

    fun mapChart(range: String, chart: List<ChartPointDto>): Pair<List<String>, List<Int>> {
        return when (range) {
            "day" -> {
                if (chart.isEmpty()) {
                    val labels = (1..24).map { it.toString() }
                    return labels to List(labels.size) { 0 }
                }
                val labels = chart.indices.map { "${it + 1}" }
                labels to chart.map { it.count }
            }
            "month" -> {
                if (chart.isEmpty()) {
                    val labels = (1..30).map { it.toString().padStart(2, '0') }
                    return labels to List(labels.size) { 0 }
                }
                chart.map { it.date.takeLast(2) } to chart.map { it.count }
            }
            else -> {
                if (chart.isEmpty()) {
                    val labels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                    return labels to List(labels.size) { 0 }
                }
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
                val labels = chart.map {
                    val date = runCatching { LocalDate.parse(it.date, formatter) }.getOrNull()
                    when (date?.dayOfWeek) {
                        DayOfWeek.MONDAY -> "T2"
                        DayOfWeek.TUESDAY -> "T3"
                        DayOfWeek.WEDNESDAY -> "T4"
                        DayOfWeek.THURSDAY -> "T5"
                        DayOfWeek.FRIDAY -> "T6"
                        DayOfWeek.SATURDAY -> "T7"
                        DayOfWeek.SUNDAY -> "CN"
                        else -> it.date.takeLast(2)
                    }
                }
                labels to chart.map { it.count }
            }
        }
    }
}
