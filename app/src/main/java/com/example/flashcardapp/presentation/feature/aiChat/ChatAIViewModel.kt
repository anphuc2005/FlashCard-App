package com.example.flashcardapp.presentation.feature.aiChat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.data.datasource.local.database.ChatMessageDatabase
import com.example.flashcardapp.data.datasource.local.entity.ChatMessageEntity
import com.example.flashcardapp.domain.model.ChatMessage
import com.example.flashcardapp.data.model.ChatMessage as DataChatMessage
import com.example.flashcardapp.data.model.MessageStatus as DataMessageStatus
import com.example.flashcardapp.data.repository.GroqRepository
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
    private val groqRepository: GroqRepository,
    context: Context
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val messageDAO = ChatMessageDatabase.getDatabase(context).chatMessageDao()

    init {
        loadChatHistory()
    }

    private fun loadChatHistory() {
        viewModelScope.launch {
            messageDAO.getAllMessages().collect { entities ->
                val messages = entities
                    .map { it.toChatMessage() }
                    .sortedBy { it.timestamp }
                _messages.value = messages
            }
        }
    }

    private fun ChatMessageEntity.toChatMessage(): ChatMessage {
        return ChatMessage(
            id = this.id,
            message = this.text,
            sender = if (this.isUser) "user" else "bot",
            timestamp = this.timestamp.toString()
        )
    }

    private fun ChatMessage.toEntity(): ChatMessageEntity {
        return ChatMessageEntity(
            id = this.id,
            text = this.message,
            isUser = this.sender == "user",
            timestamp = this.timestamp?.toLongOrNull() ?: System.currentTimeMillis()
        )
    }

    private fun ChatMessage.toDataModel(): DataChatMessage {
        return DataChatMessage(
            id = this.id,
            text = this.message,
            isUser = this.sender == "user",
            timestamp = this.timestamp?.toLongOrNull() ?: System.currentTimeMillis(),
            status = DataMessageStatus.SUCCESS
        )
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
            timestamp = System.currentTimeMillis().toString()
        )
        _messages.value = _messages.value + userMessage
        _error.value = null

        // Lưu tin nhắn user vào database
        viewModelScope.launch {
            messageDAO.insertMessage(userMessage.toEntity())
        }

        // Gửi request đến AI
        viewModelScope.launch {
            _isLoading.value = true

            val aiMessageId = UUID.randomUUID().toString()
            // Thêm tin nhắn AI với trạng thái SENDING
            val sendingMessage = ChatMessage(
                id = aiMessageId,
                message = "Đang suy nghĩ...",
                sender = "bot",
                timestamp = System.currentTimeMillis().toString()
            )
            _messages.value = _messages.value + sendingMessage

            // Lưu tin nhắn AI loading vào database
            messageDAO.insertMessage(sendingMessage.toEntity())

            val result = groqRepository.sendMessage(
                userMessage = userText,
                chatHistory = _messages.value.map { it.toDataModel() }
            )

            result.onSuccess { aiResponse ->
                // Cập nhật tin nhắn AI thành SUCCESS
                // Convert markdown response sang plain text
                val plainTextResponse = MarkdownConverter.markdownToPlainText(aiResponse)
                val successMessage = ChatMessage(
                    id = aiMessageId,
                    message = plainTextResponse,
                    sender = "bot",
                    timestamp = System.currentTimeMillis().toString()
                )
                _messages.value = _messages.value.map { message ->
                    if (message.id == aiMessageId) {
                        successMessage
                    } else {
                        message
                    }
                }
                // Lưu tin nhắn AI SUCCESS vào database
                viewModelScope.launch {
                    messageDAO.insertMessage(successMessage.toEntity())
                }
            }

            result.onFailure { exception ->
                // Cập nhật tin nhắn AI thành ERROR
                val errorMessage = ChatMessage(
                    id = aiMessageId,
                    message = "Xin lỗi, có lỗi xảy ra: ${exception.message}",
                    sender = "bot",
                    timestamp = System.currentTimeMillis().toString()
                )
                _messages.value = _messages.value.map { message ->
                    if (message.id == aiMessageId) {
                        errorMessage
                    } else {
                        message
                    }
                }
                // Lưu tin nhắn AI ERROR vào database
                viewModelScope.launch {
                    messageDAO.insertMessage(errorMessage.toEntity())
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
            messageDAO.deleteAllMessages()
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
                messageDAO.deleteMessage(messageToDelete.toEntity())
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

