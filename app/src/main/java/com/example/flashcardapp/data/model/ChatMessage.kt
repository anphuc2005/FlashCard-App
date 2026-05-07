package com.example.flashcardapp.data.model

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val isUser: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SUCCESS
)

enum class MessageStatus {
    SENDING,
    SUCCESS,
    ERROR
}


