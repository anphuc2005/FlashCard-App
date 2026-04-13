package com.example.flashcardapp.presentation.feature.aiChat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.model.ChatMessage
import com.example.flashcardapp.domain.usecase.chat.ChatUseCases
import com.example.flashcardapp.utils.MarkdownConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class MessageStatus {
    SENDING, SUCCESS, ERROR
}

class ChatAIViewModel(
    private val useCases: ChatUseCases
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        observeMessages()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            useCases.observeMessages().collect { messages ->
                _messages.value = messages.sortedBy { it.timestamp }
            }
        }
    }

    /**
     * Gửi tin nhắn
     */
    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        // Thêm tin nhắn người dùng vào danh sách
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            message = userText,
            sender = "user",
            timestamp = System.currentTimeMillis()
        )
        _messages.value = _messages.value + userMessage
        _error.value = null

        viewModelScope.launch {
            useCases.saveMessage(userMessage)
        }

        viewModelScope.launch {
            _isLoading.value = true

            val aiMessageId = UUID.randomUUID().toString()
            // Thêm tin nhắn AI với trạng thái SENDING
            val sendingMessage = ChatMessage(
                id = aiMessageId,
                message = "Đang suy nghĩ...",
                sender = "bot",
                timestamp = System.currentTimeMillis()
            )
            _messages.value = _messages.value + sendingMessage

            useCases.saveMessage(sendingMessage)

            val result = useCases.sendMessage(
                userMessage = userText,
                history = _messages.value
            )

            result.onSuccess { aiResponse ->
                // Cập nhật tin nhắn AI thành SUCCESS
                // Convert markdown response sang plain text
                val plainTextResponse = MarkdownConverter.markdownToPlainText(aiResponse)
                val successMessage = ChatMessage(
                    id = aiMessageId,
                    message = plainTextResponse,
                    sender = "bot",
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value.map { message ->
                    if (message.id == aiMessageId) {
                        successMessage
                    } else {
                        message
                    }
                }
                viewModelScope.launch {
                    useCases.saveMessage(successMessage)
                }
            }

            result.onFailure { exception ->
                // Cập nhật tin nhắn AI thành ERROR
                val errorMessage = ChatMessage(
                    id = aiMessageId,
                    message = "Xin lỗi, có lỗi xảy ra: ${exception.message}",
                    sender = "bot",
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value.map { message ->
                    if (message.id == aiMessageId) {
                        errorMessage
                    } else {
                        message
                    }
                }
                viewModelScope.launch {
                    useCases.saveMessage(errorMessage)
                }
                _error.value = exception.message ?: "Unknown error"
            }

            _isLoading.value = false
        }
    }

    /**
     * Xóa tất cả tin nhắn
     */
    fun clearChat() {
        _messages.value = emptyList()
        _error.value = null
        viewModelScope.launch {
            useCases.clearMessages()
        }
    }

    /**
     * Xóa tin nhắn cụ thể
     */
    fun deleteMessage(messageId: String) {
        val messageToDelete = _messages.value.find { it.id == messageId }
        _messages.value = _messages.value.filter { it.id != messageId }

        if (messageToDelete != null) {
            viewModelScope.launch {
                useCases.deleteMessage(messageToDelete.id)
            }
        }
    }

    /**
     * Thử lại tin nhắn cuối cùng
     */
    fun retryLastMessage() {
        val messages = _messages.value
        val lastUserMessage = messages.findLast { it.sender == "user" }

        if (lastUserMessage != null) {
            sendMessage(lastUserMessage.message)
        }
    }
}


class ChatAIViewModelFactory(
    private val useCases: ChatUseCases
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatAIViewModel(useCases) as T
    }
}

