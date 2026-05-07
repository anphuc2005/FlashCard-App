package com.example.flashcardapp.presentation.feature.addDeck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.domain.usecase.flashcard.AddFlashCardsBulkUseCase
import com.example.flashcardapp.domain.usecase.upload.UploadImageUseCase

class AddCardViewModelFactory(
    private val addFlashCardsBulkUseCase: AddFlashCardsBulkUseCase,
    private val uploadImageUseCase: UploadImageUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddCardViewModel(addFlashCardsBulkUseCase, uploadImageUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
