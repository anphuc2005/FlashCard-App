package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.data.datasource.remote.model.ApiResponse
import com.example.flashcardapp.data.datasource.remote.model.DeckCardCountDto
import com.example.flashcardapp.data.datasource.remote.model.DeckExplorePageDto
import com.example.flashcardapp.data.datasource.remote.model.DeckDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DeckApiService {

    @GET("decks")
    suspend fun getAllDecks(): ApiResponse<List<DeckDto>>

    @GET("decks/{id}")
    suspend fun getDeckById(@Path("id") id: String): ApiResponse<DeckDto>

    @GET("decks/{deckId}/card-count")
    suspend fun getDeckCardCount(@Path("deckId") deckId: String): ApiResponse<DeckCardCountDto>

    @GET("decks/explore")
    suspend fun exploreDecks(): ApiResponse<List<DeckDto>>

    @GET("decks/explore/paged")
    suspend fun exploreDecksPaged(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 5,
        @Query("query") query: String? = null
    ): ApiResponse<DeckExplorePageDto>

    @POST("decks")
    suspend fun createDeck(@Body deck: DeckDto): ApiResponse<DeckDto>

    @POST("decks/{id}/clone")
    suspend fun cloneDeck(@Path("id") deckId: String): ApiResponse<DeckDto>

    @PUT("decks/{id}")
    suspend fun updateDeck(@Path("id") id: String, @Body deck: DeckDto): ApiResponse<DeckDto>

    @DELETE("decks/{id}")
    suspend fun deleteDeck(@Path("id") id: String): ApiResponse<String>

}
