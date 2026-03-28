package com.example.flashcardapp.domain.model

data class ChatMessage(
    val id: String,
    val message: String,
    val sender: String, // "user" or "bot"
    val timestamp: Long
)

