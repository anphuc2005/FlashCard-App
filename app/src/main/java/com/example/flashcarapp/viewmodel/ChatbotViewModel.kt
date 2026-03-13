package com.example.flashcarapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcarapp.model.ChatMessage
import com.example.flashcarapp.repository.ChatbotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class ChatbotUiState {
    object Loading : ChatbotUiState()
    data class Success(val messages: List<ChatMessage>) : ChatbotUiState()
    data class Error(val message: String) : ChatbotUiState()
}

class ChatbotViewModel(private val chatbotRepository: ChatbotRepository) : ViewModel() {

    private val _chatbotUiState = MutableStateFlow<ChatbotUiState>(ChatbotUiState.Success(emptyList()))
    val chatbotUiState: StateFlow<ChatbotUiState> = _chatbotUiState.asStateFlow()

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            chatbotRepository.getAllMessagesFromDb().collect { messages ->
                _chatbotUiState.value = ChatbotUiState.Success(
                    messages.map { entity ->
                        ChatMessage(
                            id = entity.id,
                            message = entity.message,
                            sender = entity.sender,
                            timestamp = entity.timestamp
                        )
                    }
                )
            }
        }
    }

    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            try {
                // Thêm tin nhắn người dùng vào UI ngay lập tức
                val userChatMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    message = userMessage,
                    sender = "user",
                    timestamp = System.currentTimeMillis().toString()
                )
                chatbotRepository.insertMessage(userChatMessage)

                // Gửi tin nhắn đến API
                val result = chatbotRepository.sendMessageToApi(userChatMessage)
                result.onFailure { exception ->
                    _chatbotUiState.value = ChatbotUiState.Error(exception.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _chatbotUiState.value = ChatbotUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearMessages() {
        viewModelScope.launch {
            try {
                chatbotRepository.deleteAllMessages()
            } catch (e: Exception) {
                _chatbotUiState.value = ChatbotUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

