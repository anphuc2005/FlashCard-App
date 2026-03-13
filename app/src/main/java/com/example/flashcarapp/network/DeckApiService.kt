package com.example.flashcarapp.network

import com.example.flashcarapp.model.ApiResponse
import com.example.flashcarapp.model.Deck
import com.example.flashcarapp.model.FlashCard
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DeckApiService {

    @GET("decks")
    suspend fun getAllDecks(): ApiResponse<List<Deck>>

    @GET("decks/{id}")
    suspend fun getDeckById(@Path("id") id: String): ApiResponse<Deck>

    @POST("decks")
    suspend fun createDeck(@Body deck: Deck): ApiResponse<Deck>

    @PUT("decks/{id}")
    suspend fun updateDeck(@Path("id") id: String, @Body deck: Deck): ApiResponse<Deck>

    @DELETE("decks/{id}")
    suspend fun deleteDeck(@Path("id") id: String): ApiResponse<String>

    @GET("decks/{deckId}/cards")
    suspend fun getCardsByDeckId(@Path("deckId") deckId: String): ApiResponse<List<FlashCard>>
}

