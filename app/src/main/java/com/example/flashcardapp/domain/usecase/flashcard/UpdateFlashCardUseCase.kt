package com.example.flashcardapp.domain.usecase.flashcard

import com.example.flashcardapp.data.repository.FlashCardRepository
import com.example.flashcardapp.domain.model.FlashCard

class UpdateFlashCardUseCase(
    private val repository: FlashCardRepository
) {
    suspend operator fun invoke(id: String, question: String, answer: String, deckId: String, imageUrl: String? = null): Result<Unit> {
        if (question.isBlank() || answer.isBlank()) {
            return Result.failure(IllegalArgumentException("Câu hỏi và câu trả lời không được để trống"))
        }

        val card = FlashCard(
            id = id,
            question = question,
            answer = answer,
            imageUrl = imageUrl,
            deckId = deckId
        )

        return try {
            repository.updateCard(card)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

