package com.example.flashcardapp.presentation.feature.addDeck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.domain.usecase.flashcard.AddFlashCardUseCase
import com.example.flashcardapp.domain.usecase.upload.UploadImageUseCase

class AddCardViewModelFactory(
    private val addFlashCardUseCase: AddFlashCardUseCase,
    private val uploadImageUseCase: UploadImageUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddCardViewModel(addFlashCardUseCase, uploadImageUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
