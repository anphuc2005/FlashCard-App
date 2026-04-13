package com.example.flashcardapp.presentation.feature.addDeck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.domain.usecase.deck.AddDeckUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AddDeckState {
    object Idle : AddDeckState()
    object Loading : AddDeckState()
    data class Success(val deck: Deck) : AddDeckState()
    data class Error(val message: String) : AddDeckState()
}

class AddDeckViewModel(
    private val addDeckUseCase: AddDeckUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddDeckState>(AddDeckState.Idle)
    val uiState: StateFlow<AddDeckState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = AddDeckState.Idle
    }

    fun createDeck(name: String, description: String, isPublic: Boolean) {
        if (name.isBlank()) {
            _uiState.value = AddDeckState.Error("Tên bộ thẻ không được để trống")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddDeckState.Loading

            val result = addDeckUseCase(name, description, isPublic)

            result.fold(
                onSuccess = { deck ->
                    _uiState.value = AddDeckState.Success(deck)
                },
                onFailure = { error ->
                    _uiState.value = AddDeckState.Error(error.message ?: "Có lỗi xảy ra khi tạo bộ thẻ")
                }
            )
        }
    }
}
