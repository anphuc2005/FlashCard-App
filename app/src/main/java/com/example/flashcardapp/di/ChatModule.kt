package com.example.flashcardapp.di

import android.content.Context
import com.example.flashcardapp.BuildConfig
import com.example.flashcardapp.data.datasource.local.database.ChatMessageDatabase
import com.example.flashcardapp.data.datasource.remote.api.RetrofitClient
import com.example.flashcardapp.data.repository.GroqRepository
import com.example.flashcardapp.domain.repository.ChatRepository
import com.example.flashcardapp.domain.usecase.chat.ChatUseCases
import com.example.flashcardapp.domain.usecase.chat.ClearChatMessagesUseCase
import com.example.flashcardapp.domain.usecase.chat.DeleteChatMessageUseCase
import com.example.flashcardapp.domain.usecase.chat.ObserveChatMessagesUseCase
import com.example.flashcardapp.domain.usecase.chat.SaveChatMessageUseCase
import com.example.flashcardapp.domain.usecase.chat.SendChatMessageUseCase

object ChatModule {
    fun provideChatRepository(context: Context): ChatRepository {
        val chatDao = ChatMessageDatabase.getDatabase(context).chatMessageDao()
        return GroqRepository(
            apiService = RetrofitClient.groqApiService,
            apiKey = BuildConfig.GROQ_API_KEY,
            chatMessageDao = chatDao
        )
    }

    fun provideChatUseCases(context: Context): ChatUseCases {
        val repository = provideChatRepository(context)
        return ChatUseCases(
            observeMessages = ObserveChatMessagesUseCase(repository),
            saveMessage = SaveChatMessageUseCase(repository),
            deleteMessage = DeleteChatMessageUseCase(repository),
            clearMessages = ClearChatMessagesUseCase(repository),
            sendMessage = SendChatMessageUseCase(repository)
        )
    }
}

