package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.data.datasource.remote.model.statistics.StatisticsAchievementResponseDto
import com.example.flashcardapp.data.datasource.remote.model.statistics.StatisticsDashboardResponseDto
import com.example.flashcardapp.data.datasource.remote.model.statistics.StatisticsDecksResponseDto
import com.example.flashcardapp.data.datasource.remote.model.statistics.StatisticsRangeSummaryResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface StatisticsApiService {

    @GET("statistics/dashboard")
    suspend fun getDashboard(
        @Query("days") days: Int,
        @Query("deckId") deckId: String? = null,
        @Query("achievementLimit") achievementLimit: Int = 4,
        @Query("deckLimit") deckLimit: Int = 20
    ): StatisticsDashboardResponseDto

    @GET("statistics/achievements")
    suspend fun getAchievements(
        @Query("featuredLimit") featuredLimit: Int = 4,
        @Query("includeLocked") includeLocked: Boolean = true
    ): StatisticsAchievementResponseDto

    @GET("statistics/decks")
    suspend fun getDeckStatistics(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("includeEmpty") includeEmpty: Boolean = true
    ): StatisticsDecksResponseDto

    @GET("statistics/range-summary")
    suspend fun getRangeSummary(
        @Query("days") days: Int,
        @Query("deckId") deckId: String? = null
    ): StatisticsRangeSummaryResponseDto
}
