package com.example.flashcardapp.domain.model

data class FlashCard(
    val id: String,
    val question: String,
    val answer: String,
    val deckId: String
)

