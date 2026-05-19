package com.example.flashcardapp.domain.usecase.deck

import com.example.flashcardapp.data.repository.DeckRepository
import com.example.flashcardapp.domain.model.Deck

class UpdateDeckUseCase(
    private val deckRepository: DeckRepository
) {
    suspend operator fun invoke(
        id: String,
        name: String,
        description: String,
        isPublic: Boolean,
        themeColor: String?,
        existingDeck: Deck
    ): Result<Deck> {
        val updatedDeck = existingDeck.copy(
            name = name,
            description = description,
            themeColor = themeColor,
            isPublic = isPublic,
            updatedAt = System.currentTimeMillis().toString()
        )

        return deckRepository.updateDeck(id, updatedDeck)
    }
}
