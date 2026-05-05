package com.example.flashcardapp.domain.usecase.chat

import com.example.flashcardapp.domain.repository.ChatRepository

class DeleteChatSessionUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(sessionId: String) = repository.deleteSession(sessionId)
}
