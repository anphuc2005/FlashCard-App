package com.example.flashcardapp.data.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.example.flashcardapp.data.datasource.local.dao.ChatMessageDao
import com.example.flashcardapp.data.datasource.local.entity.ChatMessageEntity
import com.example.flashcardapp.data.datasource.remote.api.GeminiApiService
import com.example.flashcardapp.data.datasource.remote.model.GeminiContent
import com.example.flashcardapp.data.datasource.remote.model.GeminiPart
import com.example.flashcardapp.data.datasource.remote.model.GeminiRequest
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

/** Chat repository implementation using Gemini backend and local cache. */
class GeminiRepository(
    private val apiService: GeminiApiService,
    private val apiKey: String,
    private val chatMessageDao: ChatMessageDao
) : ChatRepository {

    companion object {
        private const val MAX_HISTORY_MESSAGES = 10
        private const val MAX_CONTEXT_CHARS = 2400
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

            val promptBuilder = StringBuilder()
            promptBuilder.append("System:\n")
            promptBuilder.append(AISystemPrompts.getSystemPromptMessage())
            promptBuilder.append("\n\n")

            if (!contextMessage.isNullOrBlank()) {
                promptBuilder.append("System Context:\n")
                promptBuilder.append(truncate(contextMessage.trim(), MAX_CONTEXT_CHARS))
                promptBuilder.append("\n\n")
            }

            if (relevance.decision == TopicDecision.NEEDS_CLARIFICATION) {
                promptBuilder.append("System Clarification Hint:\n")
                promptBuilder.append(TopicValidator.getClarificationSystemHint())
                promptBuilder.append("\n\n")
            }

            if (history.isNotEmpty()) {
                promptBuilder.append("Lịch sử hội thoại gần đây:\n")
                history.takeLast(MAX_HISTORY_MESSAGES).forEach { chatMessage ->
                    val role = if (chatMessage.sender == "user") "Người dùng" else "Trợ lý"
                    promptBuilder.append("- $role: ${truncate(chatMessage.message, MAX_MESSAGE_CHARS)}\n")
                }
                promptBuilder.append("\n")
            }

            promptBuilder.append("Người dùng: ${truncate(userMessage, MAX_MESSAGE_CHARS)}\n")
            promptBuilder.append("Trợ lý:")

            val payload = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = promptBuilder.toString().trim())))
                )
            )
            val response = apiService.generateContent(apiKey = apiKey, request = payload)
            val aiText = response.candidates
                ?.firstOrNull()
                ?.content?.parts
                ?.firstOrNull()
                ?.text
                ?: throw Exception("Empty response from AI")

            Result.success(aiText)
        } catch (e: HttpException) {
            val errorDetail = e.response()
                ?.errorBody()
                ?.string()
                ?.replace(Regex("\\s+"), " ")
                ?.take(320)
                .orEmpty()

            val errorMessage = when (e.code()) {
                403 -> "Lỗi 403: API Key không hợp lệ hoặc không có quyền truy cập. Kiểm tra Gemini API Key trong local.properties"
                401 -> "Lỗi 401: Unauthorized - API Key không hợp lệ"
                400 -> if (errorDetail.isNotBlank()) {
                    "Lỗi 400: Request không hợp lệ. Chi tiết: $errorDetail"
                } else {
                    "Lỗi 400: Request không hợp lệ (có thể do payload quá dài hoặc format message sai)."
                }
                429 -> "Lỗi 429: Quá nhiều request - Chờ một lúc rồi thử lại"
                500 -> "Lỗi 500: Lỗi server của Gemini"
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
