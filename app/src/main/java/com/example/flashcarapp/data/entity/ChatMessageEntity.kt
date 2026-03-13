package com.example.flashcarapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_message_table")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val message: String,
    val sender: String, // "user" or "bot"
    val timestamp: String? = null
)

