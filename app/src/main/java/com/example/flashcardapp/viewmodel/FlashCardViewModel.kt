package com.example.flashcardapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.model.FlashCard
import com.example.flashcardapp.repository.FlashCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FlashCardUiState {
    object Loading : FlashCardUiState()
    data class Success(val cards: List<FlashCard>) : FlashCardUiState()
    data class Error(val message: String) : FlashCardUiState()
    object Empty : FlashCardUiState()
}

class FlashCardViewModel(private val flashCardRepository: FlashCardRepository) : ViewModel() {

    private val _flashCardUiState = MutableStateFlow<FlashCardUiState>(FlashCardUiState.Loading)
    val flashCardUiState: StateFlow<FlashCardUiState> = _flashCardUiState.asStateFlow()

    fun getCardsByDeckId(deckId: String) {
        viewModelScope.launch {
            _flashCardUiState.value = FlashCardUiState.Loading
            try {
                val result = flashCardRepository.getCardsByDeckIdFromApi(deckId)
                result.onSuccess { cards ->
                    _flashCardUiState.value = if (cards.isEmpty()) {
                        FlashCardUiState.Empty
                    } else {
                        FlashCardUiState.Success(cards)
                    }
                }
                result.onFailure { exception ->
                    _flashCardUiState.value = FlashCardUiState.Error(exception.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _flashCardUiState.value = FlashCardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addCard(card: FlashCard) {
        viewModelScope.launch {
            try {
                flashCardRepository.insertCard(card)
            } catch (e: Exception) {
                _flashCardUiState.value = FlashCardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateCard(card: FlashCard) {
        viewModelScope.launch {
            try {
                flashCardRepository.updateCard(card)
            } catch (e: Exception) {
                _flashCardUiState.value = FlashCardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteCard(card: FlashCard) {
        viewModelScope.launch {
            try {
                flashCardRepository.deleteCard(card)
            } catch (e: Exception) {
                _flashCardUiState.value = FlashCardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

