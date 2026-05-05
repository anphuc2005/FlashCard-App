package com.example.flashcardapp.di

import com.example.flashcardapp.data.datasource.remote.api.RetrofitClient
import com.example.flashcardapp.data.repository.BackendChatRepository
import com.example.flashcardapp.domain.repository.ChatRepository
import com.example.flashcardapp.domain.usecase.chat.ChatUseCases
import com.example.flashcardapp.domain.usecase.chat.CreateChatSessionUseCase
import com.example.flashcardapp.domain.usecase.chat.DeleteChatSessionUseCase
import com.example.flashcardapp.domain.usecase.chat.GetChatMessagesUseCase
import com.example.flashcardapp.domain.usecase.chat.GetChatSessionsUseCase
import com.example.flashcardapp.domain.usecase.chat.SendChatMessageUseCase

object ChatModule {
    fun provideChatRepository(): ChatRepository {
        return BackendChatRepository(
            chatApiService = RetrofitClient.chatApiService
        )
    }

    fun provideChatUseCases(): ChatUseCases {
        val repository = provideChatRepository()
        return ChatUseCases(
            createSession = CreateChatSessionUseCase(repository),
            getSessions = GetChatSessionsUseCase(repository),
            getMessages = GetChatMessagesUseCase(repository),
            sendMessage = SendChatMessageUseCase(repository),
            deleteSession = DeleteChatSessionUseCase(repository)
        )
    }
}
