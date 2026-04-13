package com.example.flashcardapp.presentation.feature.addDeck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.usecase.flashcard.AddFlashCardUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AddCardState {
    object Idle : AddCardState()
    object Loading : AddCardState()
    object Success : AddCardState()
    data class Error(val message: String) : AddCardState()
}

class AddCardViewModel(
    private val addFlashCardUseCase: AddFlashCardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddCardState>(AddCardState.Idle)
    val uiState: StateFlow<AddCardState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = AddCardState.Idle
    }

    fun submitCard(question: String, answer: String, deckId: String) {
        if (question.isBlank() || answer.isBlank()) {
            _uiState.value = AddCardState.Error("Câu hỏi và câu trả lời không được để trống")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddCardState.Loading
            
            // Do hiện tại chưa có mạng, ta đang gọi thẳng Local + Fake
            // (Bạn có thể bỏ `delay` tùy thích trong repo, ở đây UseCase chạy rất nhanh)
            val result = addFlashCardUseCase(question, answer, deckId)

            result.fold(
                onSuccess = {
                    _uiState.value = AddCardState.Success
                },
                onFailure = { error ->
                    _uiState.value = AddCardState.Error(error.message ?: "Có lỗi xảy ra khi thêm thẻ")
                }
            )
        }
    }
}
