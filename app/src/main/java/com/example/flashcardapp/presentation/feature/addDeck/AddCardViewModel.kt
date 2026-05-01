package com.example.flashcardapp.presentation.feature.addDeck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.usecase.flashcard.AddFlashCardsBulkUseCase
import com.example.flashcardapp.domain.model.FlashCard
import com.example.flashcardapp.domain.usecase.upload.UploadImageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class AddCardState {
    object Idle : AddCardState()
    object Loading : AddCardState()
    object CardQueued : AddCardState()
    object AllSaved : AddCardState()
    data class Error(val message: String) : AddCardState()
}

class AddCardViewModel(
    private val addFlashCardsBulkUseCase: AddFlashCardsBulkUseCase,
    private val uploadImageUseCase: UploadImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddCardState>(AddCardState.Idle)
    val uiState: StateFlow<AddCardState> = _uiState.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()
    private val pendingCards = mutableListOf<FlashCard>()
    private var currentUploadedImageUrl: String? = null

    fun resetState() {
        _uiState.value = AddCardState.Idle
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }

    fun addCardToPending(question: String, answer: String, deckId: String) {
        if (question.isBlank() || answer.isBlank()) {
            _uiState.value = AddCardState.Error("Câu hỏi và câu trả lời không được để trống")
            return
        }

        val card = FlashCard(
            id = java.util.UUID.randomUUID().toString(),
            question = question,
            answer = answer,
            imageUrl = currentUploadedImageUrl,
            deckId = deckId
        )
        pendingCards.add(card)
        currentUploadedImageUrl = null
        _uiState.value = AddCardState.CardQueued
    }

    fun saveAllPendingCards(question: String, answer: String, deckId: String) {
        val hasCurrentInput = question.isNotBlank() || answer.isNotBlank() || currentUploadedImageUrl != null
        if (hasCurrentInput) {
            if (question.isBlank() || answer.isBlank()) {
                _uiState.value = AddCardState.Error("Câu hỏi và câu trả lời không được để trống")
                return
            }
            pendingCards.add(
                FlashCard(
                    id = java.util.UUID.randomUUID().toString(),
                    question = question,
                    answer = answer,
                    imageUrl = currentUploadedImageUrl,
                    deckId = deckId
                )
            )
            currentUploadedImageUrl = null
        }

        if (pendingCards.isEmpty()) {
            _uiState.value = AddCardState.Error("Chưa có thẻ nào để lưu")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddCardState.Loading
            val cardsToSave = pendingCards.toList()
            val result = addFlashCardsBulkUseCase(cardsToSave)

            result.fold(
                onSuccess = {
                    pendingCards.clear()
                    _uiState.value = AddCardState.AllSaved
                },
                onFailure = { error ->
                    _uiState.value = AddCardState.Error(error.message ?: "Có lỗi xảy ra khi thêm thẻ")
                }
            )
        }
    }

    fun uploadImage(file: File) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            val result = uploadImageUseCase(file)
            result.fold(
                onSuccess = { url ->
                    currentUploadedImageUrl = url
                    _uploadState.value = UploadState.Success(url)
                },
                onFailure = { error ->
                    _uploadState.value = UploadState.Error(error.message ?: "Lỗi khi upload ảnh")
                }
            )
        }
    }
}

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Success(val url: String) : UploadState()
    data class Error(val message: String) : UploadState()
}
