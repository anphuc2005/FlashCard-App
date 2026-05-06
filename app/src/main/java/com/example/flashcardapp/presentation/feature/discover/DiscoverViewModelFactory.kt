package com.example.flashcardapp.presentation.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.domain.usecase.category.GetAllCategoriesUseCase
import com.example.flashcardapp.domain.usecase.deck.CloneDeckUseCase
import com.example.flashcardapp.domain.usecase.deck.GetExploreDecksFromApiUseCase
import com.example.flashcardapp.domain.usecase.report.SubmitDeckReportUseCase

class DiscoverViewModelFactory(
    private val getAllDecksFromApiUseCase: GetExploreDecksFromApiUseCase,
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val cloneDeckUseCase: CloneDeckUseCase,
    private val submitDeckReportUseCase: SubmitDeckReportUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiscoverViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiscoverViewModel(
                getAllDecksFromApiUseCase,
                getAllCategoriesUseCase,
                cloneDeckUseCase,
                submitDeckReportUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
