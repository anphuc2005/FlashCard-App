package com.example.flashcardapp.repository

import com.example.flashcardapp.data.model.ChatMessage
import com.example.flashcardapp.data.model.MessageStatus
import com.example.flashcardapp.data.network.GeminiContent
import com.example.flashcardapp.data.network.GeminiPart
import com.example.flashcardapp.data.network.GeminiRequest
import com.example.flashcardapp.network.GeminiApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository giao tiếp với Gemini thông qua Retrofit
 */
class GeminiRepository(
    private val apiService: GeminiApiService,
    private val apiKey: String
) {

    /**
     * Gửi tin nhắn đến Gemini AI và nhận phản hồi
     * @param userMessage: nội dung tin nhắn từ người dùng
     * @param chatHistory: lịch sử chat để giữ context
     */
    suspend fun sendMessage(
        userMessage: String,
        chatHistory: List<ChatMessage>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val historyParts = chatHistory
                .filter { it.status != MessageStatus.ERROR }
                .map { message -> GeminiContent(parts = listOf(GeminiPart(text = message.text))) }

            val requestPayload = GeminiRequest(
                contents = historyParts + GeminiContent(parts = listOf(GeminiPart(userMessage)))
            )

            val response = apiService.generateContent(apiKey, requestPayload)
            val aiText = response.candidates
                ?.firstOrNull()
                ?.content?.parts
                ?.firstOrNull()
                ?.text
                ?: throw Exception("Empty response from AI")

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
                val payload = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = userMessage)))
                    )
                )
                val response = apiService.generateContent(apiKey, payload)
                val aiText = response.candidates
                    ?.firstOrNull()
                    ?.content?.parts
                    ?.firstOrNull()
                    ?.text
                    ?: throw Exception("Empty response from AI")

                Result.success(aiText)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
