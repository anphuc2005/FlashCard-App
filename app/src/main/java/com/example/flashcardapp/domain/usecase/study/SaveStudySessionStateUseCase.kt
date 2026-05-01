package com.example.flashcardapp.domain.usecase.study

import com.example.flashcardapp.data.repository.StudyRepository
import com.example.flashcardapp.domain.model.study.StudySessionState

class SaveStudySessionStateUseCase(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(
        deckId: String,
        mode: String,
        cardSequence: List<String>,
        currentIndex: Int
    ): Result<StudySessionState> {
        if (deckId.isBlank()) {
            return Result.failure(IllegalArgumentException("Deck id is required"))
        }
        if (mode.isBlank()) {
            return Result.failure(IllegalArgumentException("Study mode is required"))
        }
        if (cardSequence.isEmpty()) {
            return Result.failure(IllegalArgumentException("Card sequence is required"))
        }
        return studyRepository.upsertSession(deckId, mode, cardSequence, currentIndex)
    }
}
