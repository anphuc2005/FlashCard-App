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
import com.example.flashcardapp.utils.TopicValidator
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
        chatHistory: List<ChatMessage>,
        contextMessage: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!TopicValidator.isFlashCardRelated(userMessage)) {
                return@withContext Result.success(TopicValidator.getOutOfTopicMessage())
            }

            val payload = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = userMessage)))
                )
            )
            val response = apiService.generateContent(apiKey = apiKey, request = payload)
            val aiText = response.candidates
                ?.firstOrNull()
                ?.content?.parts
                ?.firstOrNull()
                ?.text
                ?: throw Exception("Empty response from AI")

            val chatHistoryBuilder = StringBuilder()
            if (contextMessage != null) {
                chatHistoryBuilder.append("system: $contextMessage\n")
            }
            chatHistory.forEach { chatMessage ->
                chatHistoryBuilder.append("${chatMessage.sender}: ${chatMessage.message}\n")
            }

            Result.success(aiText)
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                403 -> "Lỗi 403: API Key không hợp lệ hoặc không có quyền truy cập. Kiểm tra Gemini API Key trong local.properties"
                401 -> "Lỗi 401: Unauthorized - API Key không hợp lệ"
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
}
