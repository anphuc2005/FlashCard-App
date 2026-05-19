package com.example.flashcardapp.domain.model

data class FlashCard(
    val id: String,
    val question: String,
    val answer: String,
    val imageUrl: String? = null,
    val localImagePath: String? = null,
    val deckId: String,
    val interval: Int = 0,
    val repetition: Int = 0,
    val easeFactor: Double = 2.5,
    val nextReviewDate: String? = null
)
