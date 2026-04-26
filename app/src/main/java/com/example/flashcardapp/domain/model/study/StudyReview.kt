package com.example.flashcardapp.domain.model.study

data class StudyReview(
    val id: String,
    val cardId: String,
    val deckId: String,
    val studyMode: String,
    val grade: Int,
    val studiedAt: String,
    val isSynced: Boolean = false
)
