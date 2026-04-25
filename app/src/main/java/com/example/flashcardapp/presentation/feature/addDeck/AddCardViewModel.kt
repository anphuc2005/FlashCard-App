package com.example.flashcardapp.presentation.feature.addDeck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.usecase.flashcard.AddFlashCardUseCase
import com.example.flashcardapp.domain.usecase.upload.UploadImageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class AddCardState {
    object Idle : AddCardState()
    object Loading : AddCardState()
    object Success : AddCardState()
    data class Error(val message: String) : AddCardState()
}

class AddCardViewModel(
    private val addFlashCardUseCase: AddFlashCardUseCase,
    private val uploadImageUseCase: UploadImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddCardState>(AddCardState.Idle)
    val uiState: StateFlow<AddCardState> = _uiState.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    fun resetState() {
        _uiState.value = AddCardState.Idle
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }

    fun submitCard(question: String, answer: String, deckId: String, imageFile: File? = null) {
        if (question.isBlank() || answer.isBlank()) {
            _uiState.value = AddCardState.Error("Câu hỏi và câu trả lời không được để trống")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddCardState.Loading
            
            var uploadedImageUrl: String? = null

            if (imageFile != null) {
                _uploadState.value = UploadState.Loading
                val uploadResult = uploadImageUseCase(imageFile)
                uploadResult.fold(
                    onSuccess = { url ->
                        _uploadState.value = UploadState.Success(url)
                        uploadedImageUrl = url
                    },
                    onFailure = { error ->
                        _uploadState.value = UploadState.Error(error.message ?: "Lỗi khi upload ảnh")
                        _uiState.value = AddCardState.Error("Lỗi tải ảnh: ${error.message}")
                        return@launch
                    }
                )
            }
            
            val result = addFlashCardUseCase(question, answer, deckId, uploadedImageUrl)

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

    fun uploadImage(file: File) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            val result = uploadImageUseCase(file)
            result.fold(
                onSuccess = { url ->
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
