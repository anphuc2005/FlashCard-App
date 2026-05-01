package com.example.flashcardapp.domain.usecase.flashcard

import com.example.flashcardapp.data.repository.FlashCardRepository
import com.example.flashcardapp.domain.model.FlashCard

class AddFlashCardsBulkUseCase(
    private val repository: FlashCardRepository
) {
    suspend operator fun invoke(cards: List<FlashCard>): Result<Unit> {
        if (cards.isEmpty()) {
            return Result.failure(IllegalArgumentException("Danh sách thẻ trống"))
        }

        return try {
            repository.addCardsBulk(cards)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
