package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.data.datasource.remote.model.statistics.DailyActivityDto
import com.example.flashcardapp.data.datasource.remote.model.statistics.PageResponseDto
import com.example.flashcardapp.data.datasource.remote.model.statistics.StatisticsApiResponse
import com.example.flashcardapp.data.datasource.remote.model.statistics.StatisticsSummaryDto
import com.example.flashcardapp.data.datasource.remote.model.statistics.StudyLogItemDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface StatisticsApiService {

    @GET("statistics/summary")
    suspend fun getSummary(
        @Query("deckId") deckId: String? = null
    ): Response<StatisticsApiResponse<StatisticsSummaryDto>>

    @GET("statistics/chart")
    suspend fun getActivityChart(
        @Query("days") days: Int = 30
    ): Response<StatisticsApiResponse<List<DailyActivityDto>>>

    @GET("statistics/history")
    suspend fun getHistory(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<StatisticsApiResponse<PageResponseDto<StudyLogItemDto>>>
}
