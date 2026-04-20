package com.example.flashcardapp.presentation.feature.editDeck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.domain.usecase.deck.GetDeckByIdUseCase
import com.example.flashcardapp.domain.usecase.deck.UpdateDeckUseCase

class EditDeckViewModelFactory(
    private val getDeckByIdUseCase: GetDeckByIdUseCase,
    private val updateDeckUseCase: UpdateDeckUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditDeckViewModel::class.java)) {
            return EditDeckViewModel(getDeckByIdUseCase, updateDeckUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
