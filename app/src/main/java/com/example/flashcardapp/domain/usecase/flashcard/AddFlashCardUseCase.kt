package com.example.flashcardapp.domain.usecase.flashcard

import com.example.flashcardapp.data.repository.FlashCardRepository
import com.example.flashcardapp.domain.model.FlashCard

class AddFlashCardUseCase(
    private val repository: FlashCardRepository
) {
    suspend operator fun invoke(question: String, answer: String, deckId: String): Result<Unit> {
        if (question.isBlank() || answer.isBlank()) {
            return Result.failure(IllegalArgumentException("Câu hỏi và câu trả lời không được để trống"))
        }
        
        val card = FlashCard(
            id = java.util.UUID.randomUUID().toString(),
            question = question,
            answer = answer,
            deckId = deckId
        )
        
        return try {
            repository.insertCard(card)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
