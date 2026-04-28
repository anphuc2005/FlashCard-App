package com.example.flashcardapp.domain.usecase.deck

import com.example.flashcardapp.data.repository.DeckRepository
import com.example.flashcardapp.domain.model.Deck
import java.util.UUID

class AddDeckUseCase(
    private val deckRepository: DeckRepository
) {
    suspend operator fun invoke(name: String, description: String, isPublic: Boolean, categoryId: String): Result<Deck> {
        val currentTime = System.currentTimeMillis().toString()
        val newDeck = Deck(
            id = UUID.randomUUID().toString(),
            categoryId = categoryId,
            name = name,
            description = description,
            isPublic = isPublic,
            createdAt = currentTime,
            updatedAt = currentTime
        )
        return deckRepository.createDeck(newDeck)
    }
}
