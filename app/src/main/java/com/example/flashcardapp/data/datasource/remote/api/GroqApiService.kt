package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.data.datasource.remote.model.GroqRequest
import com.example.flashcardapp.data.datasource.remote.model.GroqResponse
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

