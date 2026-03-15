package com.example.flashcardapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.repository.ChatbotRepository
import com.example.flashcardapp.repository.DeckRepository
import com.example.flashcardapp.repository.FlashCardRepository

class ViewModelFactory(
    private val deckRepository: DeckRepository,
    private val flashCardRepository: FlashCardRepository,
    private val chatbotRepository: ChatbotRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DeckViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                DeckViewModel(deckRepository) as T
            }
            modelClass.isAssignableFrom(FlashCardViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                FlashCardViewModel(flashCardRepository) as T
            }
            modelClass.isAssignableFrom(ChatbotViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ChatbotViewModel(chatbotRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

