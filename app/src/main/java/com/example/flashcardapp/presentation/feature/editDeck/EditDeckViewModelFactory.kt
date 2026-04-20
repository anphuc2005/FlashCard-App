package com.example.flashcardapp.presentation.feature.editDeck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.domain.usecase.deck.GetDeckByIdUseCase
import com.example.flashcardapp.domain.usecase.deck.UpdateDeckUseCase
import com.example.flashcardapp.domain.usecase.flashcard.DeleteFlashCardUseCase
import com.example.flashcardapp.domain.usecase.flashcard.GetCardsByDeckIdUseCase

class EditDeckViewModelFactory(
    private val getDeckByIdUseCase: GetDeckByIdUseCase,
    private val updateDeckUseCase: UpdateDeckUseCase,
    private val getCardsByDeckIdUseCase: GetCardsByDeckIdUseCase,
    private val deleteFlashCardUseCase: DeleteFlashCardUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditDeckViewModel::class.java)) {
            return EditDeckViewModel(
                getDeckByIdUseCase,
                updateDeckUseCase,
                getCardsByDeckIdUseCase,
                deleteFlashCardUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
