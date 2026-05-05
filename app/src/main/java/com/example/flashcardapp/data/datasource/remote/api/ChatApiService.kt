package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.data.datasource.remote.model.ApiResponse
import com.example.flashcardapp.data.datasource.remote.model.ChatMessageDto
import com.example.flashcardapp.data.datasource.remote.model.ChatSessionDto
import com.example.flashcardapp.data.datasource.remote.model.SendChatMessageRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApiService {

    @POST("chats")
    suspend fun createSession(): ApiResponse<ChatSessionDto>

    @GET("chats")
    suspend fun getSessions(): ApiResponse<List<ChatSessionDto>>

    @GET("chats/{sessionId}/messages")
    suspend fun getMessages(@Path("sessionId") sessionId: String): ApiResponse<List<ChatMessageDto>>

    @POST("chats/{sessionId}/messages")
    suspend fun sendMessage(
        @Path("sessionId") sessionId: String,
        @Body request: SendChatMessageRequest
    ): ApiResponse<ChatMessageDto>

    @DELETE("chats/{sessionId}")
    suspend fun deleteSession(@Path("sessionId") sessionId: String): ApiResponse<Any?>
}
