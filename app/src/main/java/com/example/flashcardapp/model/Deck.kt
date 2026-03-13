package com.example.flashcardapp.model

data class Deck(
    val id: String,
    val name: String,
    val description: String? = null,
    val cards: List<FlashCard> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

