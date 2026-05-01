package com.example.flashcardapp.domain.usecase.study

import com.example.flashcardapp.data.repository.StudyRepository
import com.example.flashcardapp.domain.model.study.StudySessionState

class GetStudySessionByDeckUseCase(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(deckId: String, mode: String): Result<StudySessionState?> {
        if (deckId.isBlank()) {
            return Result.failure(IllegalArgumentException("Deck id is required"))
        }
        if (mode.isBlank()) {
            return Result.failure(IllegalArgumentException("Study mode is required"))
        }
        return studyRepository.getSessionByDeck(deckId, mode)
    }
}
