package com.example.flashcardapp.domain.model

data class FlashCard(
    val id: String,
    val question: String,
    val answer: String,
    val imageUrl: String? = null,
    val deckId: String,
    val repetition: Int = 0
)
