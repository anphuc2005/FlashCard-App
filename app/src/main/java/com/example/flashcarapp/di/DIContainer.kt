package com.example.flashcarapp.di

import android.content.Context
import com.example.flashcarapp.data.database.FlashCardDatabase
import com.example.flashcarapp.network.RetrofitClient
import com.example.flashcarapp.repository.ChatbotRepository
import com.example.flashcarapp.repository.DeckRepository
import com.example.flashcarapp.repository.FlashCardRepository
import com.example.flashcarapp.viewmodel.ViewModelFactory

/**
 * Dependency Injection Container
 * Chứa tất cả các dependencies cần thiết cho ứng dụng
 */
object DIContainer {

    fun getViewModelFactory(context: Context): ViewModelFactory {
        val database = FlashCardDatabase.getInstance(context)

        val deckRepository = DeckRepository(
            RetrofitClient.deckApiService,
            database.deckDao()
        )

        val flashCardRepository = FlashCardRepository(
            RetrofitClient.deckApiService,
            database.flashCardDao()
        )

        val chatbotRepository = ChatbotRepository(
            RetrofitClient.chatbotApiService,
            database.chatMessageDao()
        )

        return ViewModelFactory(
            deckRepository,
            flashCardRepository,
            chatbotRepository
        )
    }
}

