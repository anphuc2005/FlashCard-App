package com.example.flashcarapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcard_table",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FlashCardEntity(
    @PrimaryKey
    val id: String,
    val question: String,
    val answer: String,
    val deckId: String
)

