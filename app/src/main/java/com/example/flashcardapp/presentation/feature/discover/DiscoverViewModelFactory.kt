package com.example.flashcardapp.presentation.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.domain.usecase.category.GetAllCategoriesUseCase
import com.example.flashcardapp.domain.usecase.deck.CloneDeckUseCase
import com.example.flashcardapp.domain.usecase.deck.GetExploreDecksFromApiUseCase

class DiscoverViewModelFactory(
    private val getAllDecksFromApiUseCase: GetExploreDecksFromApiUseCase,
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val cloneDeckUseCase: CloneDeckUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiscoverViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiscoverViewModel(getAllDecksFromApiUseCase, getAllCategoriesUseCase, cloneDeckUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
