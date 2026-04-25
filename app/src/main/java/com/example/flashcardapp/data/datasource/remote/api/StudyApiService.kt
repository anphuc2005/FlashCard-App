package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.data.datasource.remote.model.ApiResponse
import com.example.flashcardapp.data.datasource.remote.model.FlashCardDto
import com.example.flashcardapp.data.datasource.remote.model.StudyReviewDto
import com.example.flashcardapp.data.datasource.remote.model.StudySyncResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface StudyApiService {

    @GET("study/session")
    suspend fun getSessionCards(
        @Query("deckId") deckId: String,
        @Query("mode") mode: String
    ): ApiResponse<List<FlashCardDto>>

    @POST("study/sync")
    suspend fun syncReviews(@Body reviews: List<StudyReviewDto>): ApiResponse<StudySyncResponseDto>
}
