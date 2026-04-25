package com.example.flashcardapp.domain.repository

import com.example.flashcardapp.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(): Flow<List<ChatMessage>>
    suspend fun saveMessage(message: ChatMessage)
    suspend fun deleteMessageById(id: String)
    suspend fun clearMessages()
    suspend fun sendMessage(userMessage: String, history: List<ChatMessage>, contextMessage: String? = null): Result<String>
}
