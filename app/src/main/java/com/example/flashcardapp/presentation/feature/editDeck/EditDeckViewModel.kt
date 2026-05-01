package com.example.flashcardapp.presentation.feature.editDeck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.domain.usecase.deck.GetDeckByIdUseCase
import com.example.flashcardapp.domain.usecase.deck.UpdateDeckUseCase
import com.example.flashcardapp.domain.usecase.flashcard.DeleteFlashCardUseCase
import com.example.flashcardapp.domain.usecase.flashcard.GetCardsByDeckIdUseCase
import com.example.flashcardapp.domain.model.FlashCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EditDeckState {
    object Idle : EditDeckState()
    object Loading : EditDeckState()
    data class Success(val deck: Deck) : EditDeckState()
    data class UpdateSuccess(val deck: Deck) : EditDeckState()
    data class Error(val message: String) : EditDeckState()
}

class EditDeckViewModel(
    private val getDeckByIdUseCase: GetDeckByIdUseCase,
    private val updateDeckUseCase: UpdateDeckUseCase,
    private val getCardsByDeckIdUseCase: GetCardsByDeckIdUseCase,
    private val deleteFlashCardUseCase: DeleteFlashCardUseCase
) : ViewModel() {

    private val _deckState = MutableStateFlow<EditDeckState>(EditDeckState.Idle)
    val deckState: StateFlow<EditDeckState> = _deckState.asStateFlow()

    private val _cardsState = MutableStateFlow<List<FlashCard>>(emptyList())
    val cardsState: StateFlow<List<FlashCard>> = _cardsState.asStateFlow()

    private var currentDeck: Deck? = null

    fun getDeckDetail(id: String) {
        viewModelScope.launch {
            _deckState.value = EditDeckState.Loading
            getDeckByIdUseCase(id)
                .onSuccess { deck ->
                    currentDeck = deck
                    _deckState.value = EditDeckState.Success(deck)

                    // Fetch cards associated with this deck
                    fetchCardsForDeck(id)
                }
                .onFailure { error ->
                    _deckState.value = EditDeckState.Error(error.message ?: "Lỗi tải bộ thẻ")
                }
        }
    }

    private fun fetchCardsForDeck(deckId: String) {
        viewModelScope.launch {
            getCardsByDeckIdUseCase(deckId).onSuccess { cards ->
                _cardsState.value = cards
            }.onFailure {
                // Optionally handle failure fetching cards (e.g. show toast or empty list)
            }
        }
    }

    fun deleteCard(card: FlashCard) {
        viewModelScope.launch {
            _deckState.value = EditDeckState.Loading
            deleteFlashCardUseCase(
                id = card.id,
                question = card.question,
                answer = card.answer,
                deckId = card.deckId
            ).onSuccess {
                // Refresh cards after delete
                currentDeck?.id?.let { deckId -> fetchCardsForDeck(deckId) }
                _deckState.value = EditDeckState.Success(currentDeck!!) // Back to normal state
            }.onFailure { error ->
                _deckState.value = EditDeckState.Error(error.message ?: "Lỗi xoá thẻ")
            }
        }
    }

    fun updateDeck(id: String, name: String, description: String, isPublic: Boolean, themeColor: String?) {
        val deck = currentDeck
        if (deck == null) {
            _deckState.value = EditDeckState.Error("Không tìm thấy bộ thẻ để cập nhật")
            return
        }

        viewModelScope.launch {
            _deckState.value = EditDeckState.Loading
            updateDeckUseCase(id, name, description, isPublic, themeColor, deck)
                .onSuccess { updatedDeck ->
                    currentDeck = updatedDeck
                    _deckState.value = EditDeckState.UpdateSuccess(updatedDeck)
                }
                .onFailure { error ->
                    _deckState.value = EditDeckState.Error(error.message ?: "Lỗi lữu bộ thẻ")
                }
        }
    }
}
