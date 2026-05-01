package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.data.datasource.remote.model.ApiResponse
import com.example.flashcardapp.data.datasource.remote.model.FlashCardDto
import com.example.flashcardapp.data.datasource.remote.model.StudyReviewDto
import com.example.flashcardapp.data.datasource.remote.model.StudyRecentSessionDto
import com.example.flashcardapp.data.datasource.remote.model.StudySessionStateDto
import com.example.flashcardapp.data.datasource.remote.model.StudySyncResponseDto
import com.example.flashcardapp.data.datasource.remote.model.UpsertStudySessionRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface StudyApiService {

    @GET("study/session")
    suspend fun getSessionCards(
        @Query("deckId") deckId: String,
        @Query("mode") mode: String
    ): ApiResponse<List<FlashCardDto>>

    @POST("study/sync")
    suspend fun syncReviews(@Body reviews: List<StudyReviewDto>): ApiResponse<StudySyncResponseDto>

    @GET("study/sessions/recent")
    suspend fun getRecentSession(): ApiResponse<StudyRecentSessionDto>

    @GET("study/sessions/deck/{deckId}")
    suspend fun getSessionByDeck(
        @Path("deckId") deckId: String,
        @Query("mode") mode: String
    ): ApiResponse<StudySessionStateDto>


    @PUT("study/sessions")
    suspend fun upsertSession(
        @Body request: UpsertStudySessionRequestDto
    ): ApiResponse<StudySessionStateDto>

    @DELETE("study/sessions/deck/{deckId}")
    suspend fun deleteSessionByDeck(
        @Path("deckId") deckId: String,
        @Query("mode") mode: String
    ): ApiResponse<String>

}
