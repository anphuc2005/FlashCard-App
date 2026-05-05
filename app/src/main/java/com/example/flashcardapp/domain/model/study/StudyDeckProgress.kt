package com.example.flashcardapp.domain.model.study

data class StudyDeckProgress(
    val deckId: String = "",
    val mode: String = "",
    val studiedCards: Int = 0,
    val totalCards: Int = 0,
    val progressPercent: Float = 0f,
    val completed: Boolean = false
)
