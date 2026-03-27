package com.example.flashcardapp.di

import android.content.Context
import com.example.flashcardapp.data.datasource.local.database.FlashCardDatabase
import com.example.flashcardapp.data.datasource.remote.api.RetrofitClient
import com.example.flashcardapp.data.repository.DeckRepository
import com.example.flashcardapp.data.repository.FlashCardRepository

/**
 * Dependency Injection Container
 * Chứa tất cả các dependencies cần thiết cho ứng dụng
 */
object DIContainer {

    fun getDeckRepository(context: Context): DeckRepository {
        val database = FlashCardDatabase.getInstance(context)

        return DeckRepository(
            RetrofitClient.deckApiService,
            database.deckDao()
        )
    }

    fun getFlashCardRepository(context: Context): FlashCardRepository {
        val database = FlashCardDatabase.getInstance(context)

        return FlashCardRepository(
            RetrofitClient.deckApiService,
            database.flashCardDao()
        )
    }
}

