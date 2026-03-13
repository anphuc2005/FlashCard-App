package com.example.flashcardapp.network

import com.example.flashcardapp.model.ApiResponse
import com.example.flashcardapp.model.ChatMessage
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatbotApiService {

    @POST("chatbot/ask")
    suspend fun sendMessage(@Body message: ChatMessage): ApiResponse<ChatMessage>
}

