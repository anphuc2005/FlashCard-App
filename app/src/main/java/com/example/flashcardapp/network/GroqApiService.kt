package com.example.flashcardapp.network

import com.example.flashcardapp.data.network.GroqRequest
import com.example.flashcardapp.data.network.GroqResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApiService {
    @POST("chat/completions")
    suspend fun generateContent(
        @Header("Authorization") authorization: String,
        @Body request: GroqRequest
    ): GroqResponse
}

