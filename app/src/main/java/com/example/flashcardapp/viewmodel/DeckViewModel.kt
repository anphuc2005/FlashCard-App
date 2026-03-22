package com.example.flashcardapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.model.Deck
import com.example.flashcardapp.repository.DeckRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.flashcardapp.R
import com.example.flashcardapp.utils.MockDeckData

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
                    _deckUiState.value = DeckUiState.Error(exception.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _deckUiState.value = DeckUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun getDeckById(id: String) {
        viewModelScope.launch {
            _deckUiState.value = DeckUiState.Loading
            if (deckRepository == null) {
                _deckUiState.value = DeckUiState.Error("Repository not available")
                return@launch
            }
            try {
                val result = deckRepository.getDeckByIdFromApi(id)
                result.onSuccess { deck ->
                    _deckUiState.value = DeckUiState.Success(listOf(deck))
                }
                result.onFailure { exception ->
                    _deckUiState.value = DeckUiState.Error(exception.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _deckUiState.value = DeckUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun createDeck(deck: Deck) {
        viewModelScope.launch {
            if (deckRepository == null) {
                _deckUiState.value = DeckUiState.Error("Repository not available")
                return@launch
            }
            try {
                deckRepository.createDeck(deck)
                getAllDecks()
            } catch (e: Exception) {
                _deckUiState.value = DeckUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateDeck(id: String, deck: Deck) {
        viewModelScope.launch {
            if (deckRepository == null) {
                _deckUiState.value = DeckUiState.Error("Repository not available")
                return@launch
            }
            try {
                deckRepository.updateDeck(id, deck)
                getAllDecks()
            } catch (e: Exception) {
                _deckUiState.value = DeckUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteDeck(id: String) {
        viewModelScope.launch {
            if (deckRepository == null) {
                _deckUiState.value = DeckUiState.Error("Repository not available")
                return@launch
            }
            try {
                deckRepository.deleteDeck(id)
                getAllDecks()
            } catch (e: Exception) {
                _deckUiState.value = DeckUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

