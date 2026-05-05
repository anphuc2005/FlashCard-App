package com.example.flashcardapp.domain.repository

import com.example.flashcardapp.domain.model.ChatSession
import com.example.flashcardapp.domain.model.ChatMessage

interface ChatRepository {
    suspend fun createSession(): Result<ChatSession>
    suspend fun getSessions(): Result<List<ChatSession>>
    suspend fun getMessages(sessionId: String): Result<List<ChatMessage>>
    suspend fun sendMessage(sessionId: String, content: String): Result<ChatMessage>
    suspend fun deleteSession(sessionId: String): Result<Unit>
}
