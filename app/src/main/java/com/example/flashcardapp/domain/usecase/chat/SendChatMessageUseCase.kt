package com.example.flashcardapp.domain.usecase.chat

import com.example.flashcardapp.domain.model.ChatMessage
import com.example.flashcardapp.domain.repository.ChatRepository

class SendChatMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(userMessage: String, history: List<ChatMessage>) =
        repository.sendMessage(userMessage, history)
}

