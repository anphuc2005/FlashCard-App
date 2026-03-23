package com.example.flashcardapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.data.model.ChatMessage
import com.example.flashcardapp.data.model.MessageStatus
import com.example.flashcardapp.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatAIViewModel(private val geminiRepository: GeminiRepository) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Gửi tin nhắn từ người dùng
     */
    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        // Thêm tin nhắn người dùng vào danh sách
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = userText,
            isUser = true,
            status = MessageStatus.SUCCESS
        )
        _messages.value = _messages.value + userMessage
        _error.value = null

        // Gửi request đến AI
        viewModelScope.launch {
            _isLoading.value = true

            val aiMessageId = UUID.randomUUID().toString()
            // Thêm tin nhắn AI với trạng thái SENDING
            val sendingMessage = ChatMessage(
                id = aiMessageId,
                text = "Đang suy nghĩ...",
                isUser = false,
                status = MessageStatus.SENDING
            )
            _messages.value = _messages.value + sendingMessage

            val result = geminiRepository.sendMessage(userText, _messages.value)

            result.onSuccess { aiResponse ->
                // Cập nhật tin nhắn AI thành SUCCESS
                _messages.value = _messages.value.map { message ->
                    if (message.id == aiMessageId) {
                        message.copy(
                            text = aiResponse,
                            status = MessageStatus.SUCCESS
                        )
                    } else {
                        message
                    }
                }
            }

            result.onFailure { exception ->
                // Cập nhật tin nhắn AI thành ERROR
                _messages.value = _messages.value.map { message ->
                    if (message.id == aiMessageId) {
                        message.copy(
                            text = "Xin lỗi, có lỗi xảy ra: ${exception.message}",
                            status = MessageStatus.ERROR
                        )
                    } else {
                        message
                    }
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
    }

    /**
     * Xóa tin nhắn cụ thể
     */
    fun deleteMessage(messageId: String) {
        _messages.value = _messages.value.filter { it.id != messageId }
    }

    /**
     * Thử lại tin nhắn cuối cùng
     */
    fun retryLastMessage() {
        val messages = _messages.value
        val lastUserMessage = messages.findLast { it.isUser }

        if (lastUserMessage != null) {
            // Xóa tin nhắn AI cuối cùng nếu có lỗi
            val lastAiMessage = messages.findLast { !it.isUser }
            if (lastAiMessage?.status == MessageStatus.ERROR) {
                deleteMessage(lastAiMessage.id)
            }

            sendMessage(lastUserMessage.text)
        }
    }
}

