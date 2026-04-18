package com.example.flashcardapp.presentation.feature.editDeck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.domain.usecase.deck.GetDeckByIdUseCase
import com.example.flashcardapp.domain.usecase.deck.UpdateDeckUseCase
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
    private val updateDeckUseCase: UpdateDeckUseCase
) : ViewModel() {

    private val _deckState = MutableStateFlow<EditDeckState>(EditDeckState.Idle)
    val deckState: StateFlow<EditDeckState> = _deckState.asStateFlow()

    private var currentDeck: Deck? = null

    fun getDeckDetail(id: String) {
        viewModelScope.launch {
            _deckState.value = EditDeckState.Loading
            getDeckByIdUseCase(id)
                .onSuccess { deck ->
                    currentDeck = deck
                    _deckState.value = EditDeckState.Success(deck)
                }
                .onFailure { error ->
                    _deckState.value = EditDeckState.Error(error.message ?: "Lỗi tải bộ thẻ")
                }
        }
    }

    fun updateDeck(id: String, name: String, description: String, isPublic: Boolean) {
        val deck = currentDeck
        if (deck == null) {
            _deckState.value = EditDeckState.Error("Không tìm thấy bộ thẻ để cập nhật")
            return
        }

        viewModelScope.launch {
            _deckState.value = EditDeckState.Loading
            updateDeckUseCase(id, name, description, isPublic, deck)
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
