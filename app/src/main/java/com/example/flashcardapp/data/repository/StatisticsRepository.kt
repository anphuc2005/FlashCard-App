package com.example.flashcardapp.data.repository

import com.example.flashcardapp.core.utils.NetworkErrorHandler
import com.example.flashcardapp.core.utils.UserMessageMapper
import com.example.flashcardapp.data.datasource.remote.api.StatisticsApiService
import com.example.flashcardapp.data.datasource.remote.model.statistics.DailyActivityDto
import com.example.flashcardapp.data.datasource.remote.model.statistics.PageResponseDto
import com.example.flashcardapp.data.datasource.remote.model.statistics.StatisticsApiResponse
import com.example.flashcardapp.data.datasource.remote.model.statistics.StatisticsSummaryDto
import com.example.flashcardapp.data.datasource.remote.model.statistics.StudyLogItemDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class StatisticsRepository(
    private val statisticsApiService: StatisticsApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun getSummary(deckId: String? = null): Result<StatisticsSummaryDto> = withContext(ioDispatcher) {
        executeRequest { statisticsApiService.getSummary(deckId) }
    }

    suspend fun getActivityChart(days: Int = 30): Result<List<DailyActivityDto>> = withContext(ioDispatcher) {
        val normalizedDays = days.coerceIn(1, 365)
        executeRequest { statisticsApiService.getActivityChart(normalizedDays) }
            .map { it.sortedBy { item -> item.date } }
    }

    suspend fun getHistory(page: Int = 0, size: Int = 20): Result<PageResponseDto<StudyLogItemDto>> = withContext(ioDispatcher) {
        val normalizedPage = page.coerceAtLeast(0)
        val normalizedSize = size.coerceIn(1, 100)
        executeRequest { statisticsApiService.getHistory(normalizedPage, normalizedSize) }
    }

    private suspend fun <T> executeRequest(
        apiCall: suspend () -> Response<StatisticsApiResponse<T>>
    ): Result<T> {
        return try {
            val response = apiCall()

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                val message = UserMessageMapper.extractReadableMessage(errorBody)
                    ?: UserMessageMapper.extractReadableMessage(response.message())
                    ?: "Yêu cầu thống kê thất bại (mã ${response.code()})."
                return Result.failure(IllegalStateException(message))
            }

            val body = response.body()
            if (body == null) {
                return Result.failure(IllegalStateException("Phản hồi rỗng từ máy chủ thống kê."))
            }

            if (!body.success) {
                val message = UserMessageMapper.extractReadableMessage(body.message)
                    ?: "Không thể tải dữ liệu thống kê."
                return Result.failure(IllegalStateException(message))
            }

            val data = body.data
                ?: return Result.failure(IllegalStateException("Dữ liệu thống kê đang trống."))
            Result.success(data)
        } catch (throwable: Throwable) {
            Result.failure(IllegalStateException(NetworkErrorHandler.getErrorMessage(throwable)))
        }
    }
}
