package com.example.flashcardapp.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: String,
    val message: String,
    val sender: String, // "user" or "bot"
    val timestamp: String? = null
)

