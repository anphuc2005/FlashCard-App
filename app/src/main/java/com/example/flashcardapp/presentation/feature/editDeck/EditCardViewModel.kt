package com.example.flashcardapp.presentation.feature.editDeck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.usecase.flashcard.UpdateFlashCardUseCase
import com.example.flashcardapp.domain.usecase.flashcard.DeleteFlashCardUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EditCardState {
    object Idle : EditCardState()
    object Loading : EditCardState()
    object Success : EditCardState()
    object Deleted : EditCardState()
    data class Error(val message: String) : EditCardState()
}

class EditCardViewModel(
    private val updateFlashCardUseCase: UpdateFlashCardUseCase,
    private val deleteFlashCardUseCase: DeleteFlashCardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditCardState>(EditCardState.Idle)
    val uiState: StateFlow<EditCardState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = EditCardState.Idle
    }

    fun updateCard(id: String, question: String, answer: String, deckId: String, imageUrl: String?) {
        if (question.isBlank() || answer.isBlank()) {
            _uiState.value = EditCardState.Error("Câu hỏi và câu trả lời không được để trống")
            return
        }

        viewModelScope.launch {
            _uiState.value = EditCardState.Loading
            val result = updateFlashCardUseCase(id, question, answer, deckId, imageUrl)
            result.fold(
                onSuccess = { _uiState.value = EditCardState.Success },
                onFailure = { error -> _uiState.value = EditCardState.Error(error.message ?: "Có lỗi xảy ra khi cập nhật thẻ") }
            )
        }
    }

    fun deleteCard(id: String, question: String, answer: String, deckId: String) {
        viewModelScope.launch {
            _uiState.value = EditCardState.Loading
            val result = deleteFlashCardUseCase(id, question, answer, deckId)
            result.fold(
                onSuccess = { _uiState.value = EditCardState.Deleted },
                onFailure = { error -> _uiState.value = EditCardState.Error(error.message ?: "Có lỗi xảy ra khi xóa thẻ") }
            )
        }
    }
}

class EditCardViewModelFactory(
    private val updateUseCase: UpdateFlashCardUseCase,
    private val deleteUseCase: DeleteFlashCardUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditCardViewModel(updateUseCase, deleteUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

