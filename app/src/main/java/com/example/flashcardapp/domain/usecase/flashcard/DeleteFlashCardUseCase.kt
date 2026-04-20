package com.example.flashcardapp.domain.usecase.flashcard

import com.example.flashcardapp.data.repository.FlashCardRepository
import com.example.flashcardapp.domain.model.FlashCard

class DeleteFlashCardUseCase(
    private val repository: FlashCardRepository
) {
    suspend operator fun invoke(id: String, question: String, answer: String, deckId: String): Result<Unit> {
        val card = FlashCard(
            id = id,
            question = question,
            answer = answer,
            deckId = deckId
        )

        return try {
            repository.deleteCard(card)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

