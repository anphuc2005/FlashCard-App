package com.example.flashcardapp.data.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.example.flashcardapp.data.datasource.local.dao.ChatMessageDao
import com.example.flashcardapp.data.datasource.local.entity.ChatMessageEntity
import com.example.flashcardapp.data.datasource.remote.api.GroqApiService
import com.example.flashcardapp.data.datasource.remote.model.GroqMessage
import com.example.flashcardapp.data.datasource.remote.model.GroqRequest
import com.example.flashcardapp.domain.model.ChatMessage
import com.example.flashcardapp.domain.repository.ChatRepository
import com.example.flashcardapp.utils.AISystemPrompts
import com.example.flashcardapp.utils.TopicValidator
import com.example.flashcardapp.utils.TopicValidator.TopicDecision
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Chat repository implementation using Groq backend and local cache.
 */
class GroqRepository(
    private val apiService: GroqApiService,
    private val apiKey: String,
    private val chatMessageDao: ChatMessageDao
) : ChatRepository {

    companion object {
        private const val MAX_HISTORY_MESSAGES = 12
        private const val MAX_CONTEXT_CHARS = 2500
        private const val MAX_MESSAGE_CHARS = 1600
    }

    override fun observeMessages(): Flow<List<ChatMessage>> =
        chatMessageDao.getAllMessages()
            .map { entities ->
                entities.sortedBy { it.timestamp }.map { it.toDomain() }
            }

    override suspend fun saveMessage(message: ChatMessage) {
        chatMessageDao.insertMessage(message.toEntity())
    }

    override suspend fun deleteMessageById(id: String) {
        chatMessageDao.getMessageById(id)?.let { chatMessageDao.deleteMessage(it) }
    }

    override suspend fun clearMessages() {
        chatMessageDao.deleteAllMessages()
    }

    override suspend fun sendMessage(
        userMessage: String,
        history: List<ChatMessage>,
        contextMessage: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val relevance = TopicValidator.evaluateRelevance(
                message = userMessage,
                conversationHistory = history.map { it.message }
            )

            if (relevance.decision == TopicDecision.OUT_OF_SCOPE) {
                return@withContext Result.success(TopicValidator.getOutOfTopicMessage())
            }

            val messages = mutableListOf<GroqMessage>()
            messages.add(
                GroqMessage(
                    role = "system",
                    content = AISystemPrompts.getSystemPromptMessage()
                )
            )

            if (contextMessage != null) {
                messages.add(
                    GroqMessage(
                        role = "system",
                        content = truncate(contextMessage, MAX_CONTEXT_CHARS)
                    )
                )
            }

            if (relevance.decision == TopicDecision.NEEDS_CLARIFICATION) {
                messages.add(
                    GroqMessage(
                        role = "system",
                        content = TopicValidator.getClarificationSystemHint()
                    )
                )
            }

            history.takeLast(MAX_HISTORY_MESSAGES).forEach { chatMessage ->
                messages.add(
                    GroqMessage(
                        role = if (chatMessage.sender == "user") "user" else "assistant",
                        content = truncate(chatMessage.message, MAX_MESSAGE_CHARS)
                    )
                )
            }

            messages.add(
                GroqMessage(
                    role = "user",
                    content = truncate(userMessage, MAX_MESSAGE_CHARS)
                )
            )

            val requestPayload = GroqRequest(
                messages = messages,
                temperature = 0.7,
                maxTokens = 1024
            )

            val response = apiService.generateContent(
                authorization = "Bearer $apiKey",
                request = requestPayload
            )

            val aiText = response.choices
                ?.firstOrNull()
                ?.message?.content
                ?: throw Exception("Empty response from Groq AI")

            Result.success(aiText)
        } catch (e: HttpException) {
            val errorDetail = e.response()
                ?.errorBody()
                ?.string()
                ?.replace(Regex("\\s+"), " ")
                ?.take(320)
                .orEmpty()

            val errorMessage = when (e.code()) {
                403 -> "Lỗi 403: API Key không hợp lệ hoặc không có quyền truy cập. Kiểm tra Groq API Key trong local.properties"
                401 -> "Lỗi 401: Unauthorized - API Key không hợp lệ"
                400 -> if (errorDetail.isNotBlank()) {
                    "Lỗi 400: Request không hợp lệ. Chi tiết: $errorDetail"
                } else {
                    "Lỗi 400: Request không hợp lệ (có thể do payload quá dài hoặc format message sai)."
                }
                429 -> "Lỗi 429: Quá nhiều request - Chờ một lúc rồi thử lại"
                500 -> "Lỗi 500: Lỗi server của Groq"
                else -> "Lỗi ${e.code()}: ${e.message}"
            }
            Log.e(TAG, errorMessage, e)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi gửi tin nhắn: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun ChatMessageEntity.toDomain(): ChatMessage = ChatMessage(
        id = id,
        message = text,
        sender = if (isUser) "user" else "bot",
        timestamp = timestamp
    )

    private fun ChatMessage.toEntity(): ChatMessageEntity = ChatMessageEntity(
        id = id,
        text = message,
        isUser = sender == "user",
        timestamp = timestamp,
        status = "SUCCESS"
    )

    private fun truncate(text: String, maxLength: Int): String {
        if (text.length <= maxLength) return text
        return text.take(maxLength).trimEnd() + "..."
    }
}
