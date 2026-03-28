package com.example.flashcardapp.domain.usecase.chat

import com.example.flashcardapp.domain.model.ChatMessage
import com.example.flashcardapp.domain.repository.ChatRepository

class SaveChatMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(message: ChatMessage) = repository.saveMessage(message)
}

