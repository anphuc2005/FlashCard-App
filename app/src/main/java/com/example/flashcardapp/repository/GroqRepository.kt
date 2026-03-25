package com.example.flashcardapp.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.example.flashcardapp.data.model.ChatMessage
import com.example.flashcardapp.data.model.MessageStatus
import com.example.flashcardapp.data.network.GroqMessage
import com.example.flashcardapp.data.network.GroqRequest
import com.example.flashcardapp.network.GroqApiService
import com.example.flashcardapp.utils.TopicValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Repository giao tiếp với Groq API (OpenAI-compatible)
 */
class GroqRepository(
    private val apiService: GroqApiService,
    private val apiKey: String
) {

    /**
     * Gửi tin nhắn đến Groq AI và nhận phản hồi
     * @param userMessage: nội dung tin nhắn từ người dùng
     * @param chatHistory: lịch sử chat để giữ context
     */
    suspend fun sendMessage(
        userMessage: String,
        chatHistory: List<ChatMessage>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Kiểm tra câu hỏi có liên quan đến FlashCard không
            if (!TopicValidator.isFlashCardRelated(userMessage)) {
                return@withContext Result.success(TopicValidator.getOutOfTopicMessage())
            }

            // Convert chat history to Groq message format
            val messages = mutableListOf<GroqMessage>()

            // Thêm lịch sử chat vào messages
            chatHistory
                .filter { it.status != MessageStatus.ERROR }
                .forEach { chatMessage ->
                    messages.add(
                        GroqMessage(
                            role = if (chatMessage.isUser) "user" else "assistant",
                            content = chatMessage.text
                        )
                    )
                }

            // Thêm user message hiện tại
            messages.add(
                GroqMessage(
                    role = "user",
                    content = userMessage
                )
            )

            // Tạo request payload
            val requestPayload = GroqRequest(
                messages = messages,
                temperature = 0.7,
                maxTokens = 1024
            )

            // Gửi request
            val response = apiService.generateContent(
                authorization = "Bearer $apiKey",
                request = requestPayload
            )

            // Lấy response text
            val aiText = response.choices
                ?.firstOrNull()
                ?.message?.content
                ?: throw Exception("Empty response from Groq AI")

            Result.success(aiText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gửi tin nhắn đơn giản không có lịch sử
     */
    suspend fun sendSimpleMessage(userMessage: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                // Kiểm tra câu hỏi có liên quan đến FlashCard không
                if (!TopicValidator.isFlashCardRelated(userMessage)) {
                    return@withContext Result.success(TopicValidator.getOutOfTopicMessage())
                }

                // Tạo request với chỉ user message
                val messages = listOf(
                    GroqMessage(
                        role = "user",
                        content = userMessage
                    )
                )

                val payload = GroqRequest(
                    messages = messages,
                    temperature = 0.7,
                    maxTokens = 1024
                )

                val response = apiService.generateContent(
                    authorization = "Bearer $apiKey",
                    request = payload
                )

                val aiText = response.choices
                    ?.firstOrNull()
                    ?.message?.content
                    ?: throw Exception("Empty response from Groq AI")

                Result.success(aiText)
            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    403 -> "Lỗi 403: API Key không hợp lệ hoặc không có quyền truy cập. Kiểm tra Groq API Key trong local.properties"
                    401 -> "Lỗi 401: Unauthorized - API Key không hợp lệ"
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
}

