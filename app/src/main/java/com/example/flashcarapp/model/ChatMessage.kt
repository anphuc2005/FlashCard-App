package com.example.flashcarapp.model

data class ChatMessage(
    val id: String,
    val message: String,
    val sender: String, // "user" or "bot"
    val timestamp: String? = null
)

