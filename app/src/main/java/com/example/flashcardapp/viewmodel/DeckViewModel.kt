package com.example.flashcardapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.model.Deck
import com.example.flashcardapp.repository.DeckRepository
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

class DeckViewModel(private val deckRepository: DeckRepository) : ViewModel() {

    private val _deckUiState = MutableStateFlow<DeckUiState>(DeckUiState.Loading)
    val deckUiState: StateFlow<DeckUiState> = _deckUiState.asStateFlow()

    init {
        getAllDecks()
    }

    fun getAllDecks() {
        viewModelScope.launch {
            _deckUiState.value = DeckUiState.Loading
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
            try {
                deckRepository.deleteDeck(id)
                getAllDecks()
            } catch (e: Exception) {
                _deckUiState.value = DeckUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

