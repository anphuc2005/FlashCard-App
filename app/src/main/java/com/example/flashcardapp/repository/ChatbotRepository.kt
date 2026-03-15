package com.example.flashcardapp.repository

import com.example.flashcardapp.data.dao.ChatMessageDao
import com.example.flashcardapp.data.entity.ChatMessageEntity
import com.example.flashcardapp.model.ChatMessage
import com.example.flashcardapp.network.ChatbotApiService
import kotlinx.coroutines.flow.Flow

class ChatbotRepository(
    private val chatbotApiService: ChatbotApiService,
    private val chatMessageDao: ChatMessageDao
) {

    // Gửi tin nhắn đến API
    suspend fun sendMessageToApi(message: ChatMessage): Result<ChatMessage> {
        return try {
            val response = chatbotApiService.sendMessage(message)
            if (response.success && response.data != null) {
                // Lưu tin nhắn vào local database
                val messageEntity = ChatMessageEntity(
                    id = response.data.id,
                    message = response.data.message,
                    sender = response.data.sender,
                    timestamp = response.data.timestamp
                )
                chatMessageDao.insertMessage(messageEntity)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Lấy tất cả tin nhắn từ local database
    fun getAllMessagesFromDb(): Flow<List<ChatMessageEntity>> {
        return chatMessageDao.getAllMessages()
    }

    // Thêm tin nhắn vào local database
    suspend fun insertMessage(message: ChatMessage) {
        val messageEntity = ChatMessageEntity(
            id = message.id,
            message = message.message,
            sender = message.sender,
            timestamp = message.timestamp
        )
        chatMessageDao.insertMessage(messageEntity)
    }

    // Xóa tất cả tin nhắn
    suspend fun deleteAllMessages() {
        chatMessageDao.deleteAllMessages()
    }
}

