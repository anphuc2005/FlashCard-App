package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.data.datasource.remote.model.ApiResponse
import com.example.flashcardapp.data.datasource.remote.model.DeckDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DeckApiService {

    @GET("decks")
    suspend fun getAllDecks(): ApiResponse<List<DeckDto>>

    @GET("decks/{id}")
    suspend fun getDeckById(@Path("id") id: String): ApiResponse<DeckDto>

    @GET("decks/explore")
    suspend fun exploreDecks(): ApiResponse<List<DeckDto>>

    @POST("decks")
    suspend fun createDeck(@Body deck: DeckDto): ApiResponse<DeckDto>

    @POST("decks/{deckId}/clone")
    suspend fun cloneDeck(@Path("id") deckId: String): ApiResponse<DeckDto>

    @PUT("decks/{id}")
    suspend fun updateDeck(@Path("id") id: String, @Body deck: DeckDto): ApiResponse<DeckDto>

    @DELETE("decks/{id}")
    suspend fun deleteDeck(@Path("id") id: String): ApiResponse<String>

}
