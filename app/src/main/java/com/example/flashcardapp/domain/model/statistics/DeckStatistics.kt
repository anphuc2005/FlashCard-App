package com.example.flashcardapp.domain.model.statistics

// Domain model thống kê theo từng bộ thẻ.

data class DeckStatistics(
    val deckId: String,
    val deckName: String,
    val totalCards: Int,
    val learnedCards: Int,
    val progressPercent: Int,
    val reviewCount: Int
)

