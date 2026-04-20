package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.data.datasource.remote.model.ApiResponse
import com.example.flashcardapp.domain.model.FlashCard
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CardApiService {

    @GET("cards/{id}")
    suspend fun getCardById(@Path("id") id: String): ApiResponse<FlashCard>

    @POST("cards")
    suspend fun createCard(@Body card: FlashCard): ApiResponse<FlashCard>

    @PUT("cards/{id}")
    suspend fun updateCard(@Path("id") id: String, @Body card: FlashCard): ApiResponse<FlashCard>

    @DELETE("cards/{id}")
    suspend fun deleteCard(@Path("id") id: String): ApiResponse<String>

    @GET("decks/{deckId}/cards")
    suspend fun getCardsByDeckId(@Path("deckId") deckId: String): ApiResponse<List<FlashCard>>
}
