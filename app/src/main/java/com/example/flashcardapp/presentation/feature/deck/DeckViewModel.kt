package com.example.flashcardapp.presentation.feature.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.core.utils.MockDeckData
import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.data.repository.DeckRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DeckUiState {
    object Loading : DeckUiState()
    data class Success(val decks: List<Deck>) : DeckUiState()
    data class Error(val message: String) : DeckUiState()
    object Empty : DeckUiState()
}

class DeckViewModel(private val deckRepository: DeckRepository?) : ViewModel() {

    private val _deckUiState = MutableStateFlow<DeckUiState>(DeckUiState.Loading)
    val deckUiState: StateFlow<DeckUiState> = _deckUiState.asStateFlow()

    init {
        // If repository is null, use mock data for testing
        if (deckRepository == null) {
            loadMockData()
        } else {
            getAllDecks()
        }
    }

    /**
     * Load mock data for UI testing - temporary
     */
    private fun loadMockData() {
        val mockDecks = MockDeckData.getMockDecks()

        _deckUiState.value = DeckUiState.Success(mockDecks)
    }

    fun getAllDecks() {
        viewModelScope.launch {
            _deckUiState.value = DeckUiState.Loading
            if (deckRepository == null) {
                loadMockData()
                return@launch
            }

            try {
                val result = deckRepository.getAllDecksFromApi()
                result.onSuccess { decks ->
                    _deckUiState.value = if (decks.isEmpty()) {
                        DeckUiState.Empty
                    } else {
                        DeckUiState.Success(decks)
                    }
                }
                result.onFailure { exception ->
                    _deckUiState.value =
                        DeckUiState.Error(exception.message ?: "Unknown error occurred")
                }
            } catch (exception: Exception) {
                _deckUiState.value = DeckUiState.Error(exception.message ?: "Unknown error")
            }
        }
    }

    fun createDeck(name: String, description: String) {
        viewModelScope.launch {
            // Implementation for creating new deck
        }
    }

    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            if (deckRepository != null) {
                deckRepository.deleteDeck(deckId)
                getAllDecks()
            }
        }
    }
}

