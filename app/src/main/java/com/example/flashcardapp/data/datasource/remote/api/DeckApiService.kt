package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.data.datasource.remote.model.ApiResponse
import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.domain.model.FlashCard
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

