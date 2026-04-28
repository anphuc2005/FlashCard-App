package com.example.flashcardapp.presentation.feature.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.data.repository.DeckRepository
import com.example.flashcardapp.domain.usecase.study.GetReviewedCardIdsUseCase

class DeckViewModelFactory(
    private val deckRepository: DeckRepository,
    private val getReviewedCardIdsUseCase: GetReviewedCardIdsUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeckViewModel::class.java)) {
            return DeckViewModel(deckRepository, getReviewedCardIdsUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
