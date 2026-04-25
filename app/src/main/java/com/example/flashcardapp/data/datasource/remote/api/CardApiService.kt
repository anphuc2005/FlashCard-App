package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.data.datasource.remote.model.ApiResponse
import com.example.flashcardapp.data.datasource.remote.model.FlashCardDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CardApiService {

    @GET("cards/deck/{deckId}")
    suspend fun getCardOfDeck(@Path("deckId") deckId: String): ApiResponse<List<FlashCardDto>>

    @POST("cards/bulk")
    suspend fun addCard(@Body card: List<FlashCardDto>): ApiResponse<FlashCardDto>

    @PUT("cards/{id}")
    suspend fun updateCard(@Path("id") id: String, @Body card: FlashCardDto): ApiResponse<FlashCardDto>

    @DELETE("cards/{id}")
    suspend fun deleteCard(@Path("id") id: String): ApiResponse<String>

}
