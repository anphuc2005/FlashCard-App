package com.example.flashcardapp.di

import android.content.Context
import com.example.flashcardapp.data.database.FlashCardDatabase
import com.example.flashcardapp.network.RetrofitClient
import com.example.flashcardapp.repository.ChatbotRepository
import com.example.flashcardapp.repository.DeckRepository
import com.example.flashcardapp.repository.FlashCardRepository
import com.example.flashcardapp.viewmodel.ViewModelFactory

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

