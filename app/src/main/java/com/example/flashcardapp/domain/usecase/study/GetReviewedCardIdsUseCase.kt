package com.example.flashcardapp.domain.usecase.study

import com.example.flashcardapp.data.repository.StudyRepository

class GetReviewedCardIdsUseCase(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(deckId: String): Result<Set<String>> {
        return studyRepository.getReviewedCardIds(deckId)
    }
}
