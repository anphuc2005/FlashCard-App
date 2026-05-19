package com.example.flashcardapp.domain.usecase.chat

import com.example.flashcardapp.domain.repository.ChatRepository

class SendChatMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(sessionId: String, content: String) =
        repository.sendMessage(sessionId, content)
}
