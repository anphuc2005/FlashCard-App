package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.data.datasource.remote.model.GeminiRequest
import com.example.flashcardapp.data.datasource.remote.model.GeminiResponse
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

