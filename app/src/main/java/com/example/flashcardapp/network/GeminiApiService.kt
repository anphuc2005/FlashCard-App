package com.example.flashcardapp.network

import com.example.flashcardapp.data.network.GeminiRequest
import com.example.flashcardapp.data.network.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {
    @POST("chat/completions")
    suspend fun generateContent(
        @Query("model") model: String = "",
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

