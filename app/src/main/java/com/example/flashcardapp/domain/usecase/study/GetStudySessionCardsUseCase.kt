package com.example.flashcardapp.domain.usecase.study

import com.example.flashcardapp.data.repository.StudyRepository
import com.example.flashcardapp.domain.model.FlashCard

class GetStudySessionCardsUseCase(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(deckId: String, mode: String): Result<List<FlashCard>> {
        return studyRepository.getSessionCards(deckId, mode)
    }
}
