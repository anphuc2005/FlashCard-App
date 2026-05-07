package com.example.flashcardapp.domain.usecase.chat

import com.example.flashcardapp.domain.repository.ChatRepository

class CreateChatSessionUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke() = repository.createSession()
}
