package com.example.flashcardapp.domain.usecase.flashcard

import com.example.flashcardapp.data.repository.FlashCardRepository
import com.example.flashcardapp.domain.model.FlashCard

class GetCardsByDeckIdUseCase(
    private val flashCardRepository: FlashCardRepository
) {
    suspend operator fun invoke(deckId: String): Result<List<FlashCard>> {
        return try {
            val cards = flashCardRepository.getCardsByDeckIdFromApi(deckId)
            Result.success(cards)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

