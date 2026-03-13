package com.example.flashcarapp.network

import com.example.flashcarapp.model.ApiResponse
import com.example.flashcarapp.model.ChatMessage
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatbotApiService {

    @POST("chatbot/ask")
    suspend fun sendMessage(@Body message: ChatMessage): ApiResponse<ChatMessage>
}

