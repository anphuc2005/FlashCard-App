package com.example.flashcardapp.presentation.feature.addDeck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.domain.usecase.deck.AddDeckUseCase

class AddDeckViewModelFactory(
    private val addDeckUseCase: AddDeckUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddDeckViewModel::class.java)) {
            return AddDeckViewModel(addDeckUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
