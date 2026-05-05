package com.example.flashcardapp.domain.model.study

data class StudyReview(
    val id: String,
    val cardId: String,
    val deckId: String,
    val studyMode: String,
    val grade: Int,
    val studiedAt: String,
    val durationSeconds: Int? = null,
    val isSynced: Boolean = false
)
