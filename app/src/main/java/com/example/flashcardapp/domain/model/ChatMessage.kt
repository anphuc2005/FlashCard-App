package com.example.flashcardapp.domain.model

data class ChatMessage(
    val id: String,
    val sessionId: String? = null,
    val message: String,
    val sender: String, // "user" or "bot"
    val timestamp: Long
)
