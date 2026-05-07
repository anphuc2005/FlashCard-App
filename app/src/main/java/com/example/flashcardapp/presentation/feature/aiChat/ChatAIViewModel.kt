package com.example.flashcardapp.presentation.feature.aiChat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.model.ChatMessage
import com.example.flashcardapp.domain.model.ChatSession
import com.example.flashcardapp.domain.usecase.chat.ChatUseCases
import com.example.flashcardapp.presentation.feature.aiChat.model.ChatSessionUiModel
import com.example.flashcardapp.utils.MarkdownConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatAIViewModel(
    private val useCases: ChatUseCases
) : ViewModel() {

    companion object {
        private const val TAG = "ChatAIViewModel"
        private const val SESSION_EMPTY_PREVIEW = "Chưa có tin nhắn"
        private const val LOADING_MESSAGE = "Đang phản hồi..."
        private const val MAX_SESSION_PREVIEW_LENGTH = 80
    }

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _chatSessions = MutableStateFlow<List<ChatSessionUiModel>>(emptyList())
    val chatSessions: StateFlow<List<ChatSessionUiModel>> = _chatSessions.asStateFlow()

    private val _selectedSessionId = MutableStateFlow<String?>(null)
    val selectedSessionId: StateFlow<String?> = _selectedSessionId.asStateFlow()

    init {
        refreshSessions(createIfEmpty = true)
    }

    fun refreshSessions(createIfEmpty: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            Log.d(TAG, "refreshSessions(createIfEmpty=$createIfEmpty)")

            val result = useCases.getSessions()
            result.onSuccess { sessions ->
                Log.d(TAG, "refreshSessions success: sessionCount=${sessions.size}")
                val mappedSessions = sessions
                    .sortedByDescending { it.updatedAt }
                    .map { it.toUiModel() }
                _chatSessions.value = mappedSessions

                when {
                    mappedSessions.isEmpty() && createIfEmpty -> createSessionInternal()
                    mappedSessions.isEmpty() -> {
                        _selectedSessionId.value = null
                        _messages.value = emptyList()
                    }
                    else -> {
                        val nextSessionId = resolveSelectedSessionId(mappedSessions)
                        _selectedSessionId.value = nextSessionId
                        loadMessages(nextSessionId, shouldToggleLoading = false)
                    }
                }
            }.onFailure { throwable ->
                Log.e(TAG, "refreshSessions failed: ${throwable.message}", throwable)
                _error.value = throwable.message ?: "Không thể tải danh sách phiên chat."
            }

            _isLoading.value = false
        }
    }

    fun createNewSession() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            Log.d(TAG, "createNewSession()")
            createSessionInternal()
            _isLoading.value = false
        }
    }

    fun selectSession(sessionId: String) {
        if (_selectedSessionId.value == sessionId) return
        _selectedSessionId.value = sessionId
        loadMessages(sessionId, shouldToggleLoading = true)
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            Log.d(TAG, "deleteSession(sessionId=$sessionId)")

            useCases.deleteSession(sessionId)
                .onSuccess {
                    Log.d(TAG, "deleteSession success(sessionId=$sessionId)")
                    val remainedSessions = _chatSessions.value.filterNot { it.id == sessionId }
                    _chatSessions.value = remainedSessions

                    if (remainedSessions.isEmpty()) {
                        _messages.value = emptyList()
                        _selectedSessionId.value = null
                        createSessionInternal()
                    } else if (_selectedSessionId.value == sessionId) {
                        val nextSessionId = remainedSessions.first().id
                        _selectedSessionId.value = nextSessionId
                        loadMessages(nextSessionId, shouldToggleLoading = false)
                    }
                }
                .onFailure { throwable ->
                    Log.e(TAG, "deleteSession failed(sessionId=$sessionId): ${throwable.message}", throwable)
                    _error.value = throwable.message ?: "Không thể xóa phiên chat."
                }

            _isLoading.value = false
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        val sessionId = _selectedSessionId.value
        if (sessionId.isNullOrBlank()) {
            _error.value = "Phiên chat chưa sẵn sàng. Vui lòng thử lại."
            return
        }

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            message = userText,
            sender = "user",
            timestamp = System.currentTimeMillis()
        )
        _messages.value = _messages.value + userMessage
        touchSession(sessionId = sessionId, previewText = userText)

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            Log.d(TAG, "sendMessage(sessionId=$sessionId, textLength=${userText.length})")

            val typingMessageId = UUID.randomUUID().toString()
            val typingMessage = ChatMessage(
                id = typingMessageId,
                sessionId = sessionId,
                message = LOADING_MESSAGE,
                sender = "bot",
                timestamp = System.currentTimeMillis()
            )
            _messages.value = _messages.value + typingMessage

            useCases.sendMessage(sessionId = sessionId, content = userText)
                .onSuccess { assistantMessage ->
                    Log.d(TAG, "sendMessage success(sessionId=$sessionId, responseLength=${assistantMessage.message.length})")
                    val normalizedMessage = assistantMessage.copy(
                        message = MarkdownConverter.normalizeForChatDisplay(assistantMessage.message)
                    )
                    _messages.value = _messages.value.map { message ->
                        if (message.id == typingMessageId) normalizedMessage else message
                    }
                    touchSession(sessionId = sessionId, previewText = userText)
                }
                .onFailure { throwable ->
                    Log.e(TAG, "sendMessage failed(sessionId=$sessionId): ${throwable.message}", throwable)
                    _messages.value = _messages.value.filterNot { it.id == typingMessageId }
                    _error.value = throwable.message ?: "Không thể gửi tin nhắn."
                }

            _isLoading.value = false
        }
    }

    fun retryLastMessage() {
        val lastUserMessage = _messages.value.findLast { it.sender == "user" } ?: return
        sendMessage(lastUserMessage.message)
    }

    private suspend fun createSessionInternal() {
        useCases.createSession()
            .onSuccess { createdSession ->
                Log.d(TAG, "createSessionInternal success(sessionId=${createdSession.id})")
                val createdSessionUi = createdSession.toUiModel()
                _chatSessions.value = listOf(createdSessionUi) + _chatSessions.value
                _selectedSessionId.value = createdSessionUi.id
                _messages.value = emptyList()
            }
            .onFailure { throwable ->
                Log.e(TAG, "createSessionInternal failed: ${throwable.message}", throwable)
                _error.value = throwable.message ?: "Không thể tạo phiên chat mới."
            }
    }

    private fun loadMessages(sessionId: String, shouldToggleLoading: Boolean) {
        viewModelScope.launch {
            if (shouldToggleLoading) _isLoading.value = true
            _error.value = null
            Log.d(TAG, "loadMessages(sessionId=$sessionId, shouldToggleLoading=$shouldToggleLoading)")

            useCases.getMessages(sessionId)
                .onSuccess { loadedMessages ->
                    Log.d(TAG, "loadMessages success(sessionId=$sessionId, count=${loadedMessages.size})")
                    _messages.value = loadedMessages
                        .sortedBy { it.timestamp }
                        .map { message ->
                            if (message.sender == "bot") {
                                message.copy(
                                    message = MarkdownConverter.normalizeForChatDisplay(message.message)
                                )
                            } else {
                                message
                            }
                        }
                }
                .onFailure { throwable ->
                    Log.e(TAG, "loadMessages failed(sessionId=$sessionId): ${throwable.message}", throwable)
                    _messages.value = emptyList()
                    _error.value = throwable.message ?: "Không thể tải lịch sử tin nhắn."
                }

            if (shouldToggleLoading) _isLoading.value = false
        }
    }

    private fun resolveSelectedSessionId(sessions: List<ChatSessionUiModel>): String {
        val current = _selectedSessionId.value
        return if (current != null && sessions.any { it.id == current }) {
            current
        } else {
            sessions.first().id
        }
    }

    private fun touchSession(sessionId: String, previewText: String) {
        val sessions = _chatSessions.value
        val currentSession = sessions.firstOrNull { it.id == sessionId } ?: return
        val updatedSession = currentSession.copy(
            preview = sanitizeSessionPreview(previewText),
            updatedAt = System.currentTimeMillis()
        )

        _chatSessions.value = buildList {
            add(updatedSession)
            addAll(sessions.filterNot { it.id == sessionId })
        }
    }

    private fun sanitizeSessionPreview(text: String): String {
        val cleaned = text
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(MAX_SESSION_PREVIEW_LENGTH)
        return if (cleaned.isBlank()) SESSION_EMPTY_PREVIEW else cleaned
    }

    private fun ChatSession.toUiModel(): ChatSessionUiModel {
        return ChatSessionUiModel(
            id = id,
            title = title,
            preview = SESSION_EMPTY_PREVIEW,
            updatedAt = updatedAt
        )
    }
}

class ChatAIViewModelFactory(
    private val useCases: ChatUseCases
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatAIViewModel(useCases = useCases) as T
    }
}
