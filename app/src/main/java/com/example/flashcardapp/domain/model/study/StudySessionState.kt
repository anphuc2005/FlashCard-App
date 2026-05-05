package com.example.flashcardapp.domain.model.study

data class StudyRecentSession(
    val deckId: String,
    val mode: String,
    val currentIndex: Int,
    val totalCards: Int
) {
    val canResume: Boolean
        get() = totalCards > 0 && currentIndex in 0 until totalCards
}

data class StudySessionState(
    val id: String,
    val deckId: String,
    val mode: String,
    val cardSequence: List<String>,
    val currentIndex: Int,
    val totalCards: Int
)
