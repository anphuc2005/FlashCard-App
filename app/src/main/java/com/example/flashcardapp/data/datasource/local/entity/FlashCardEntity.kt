package com.example.flashcardapp.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcard_table",
    indices = [Index(value = ["deckId"])]
)
data class FlashCardEntity(
    @PrimaryKey
    val id: String,
    val question: String,
    val answer: String,
    val imageUrl: String? = null,
    val deckId: String,
    val interval: Int = 0,
    val repetition: Int = 0,
    val easeFactor: Double = 2.5,
    val nextReviewDate: String? = null,
    val isSynced: Boolean = true // Thêm trường này để đánh dấu đã đồng bộ với server hay chưa
)
