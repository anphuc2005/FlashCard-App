package com.example.flashcardapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_message_table")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS"
)

