package com.example.flashcardapp.domain.usecase.chat

import com.example.flashcardapp.domain.repository.ChatRepository

class ObserveChatMessagesUseCase(private val repository: ChatRepository) {
    operator fun invoke() = repository.observeMessages()
}

