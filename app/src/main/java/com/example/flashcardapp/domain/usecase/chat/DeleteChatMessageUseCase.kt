package com.example.flashcardapp.domain.usecase.chat

import com.example.flashcardapp.domain.repository.ChatRepository

class DeleteChatMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(id: String) = repository.deleteMessageById(id)
}

