package com.example.flashcardapp.domain.usecase.study

import com.example.flashcardapp.data.repository.StudyRepository

class DeleteStudySessionByDeckUseCase(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(deckId: String, mode: String): Result<Unit> {
        if (deckId.isBlank()) {
            return Result.failure(IllegalArgumentException("Deck id is required"))
        }
        if (mode.isBlank()) {
            return Result.failure(IllegalArgumentException("Study mode is required"))
        }
        return studyRepository.deleteSessionByDeck(deckId, mode)
    }
}
