package com.example.flashcardapp.domain.usecase.deck

import com.example.flashcardapp.data.repository.DeckRepository
import com.example.flashcardapp.domain.model.Deck
import java.util.UUID

class AddDeckUseCase(
    private val deckRepository: DeckRepository
) {
    suspend operator fun invoke(name: String, description: String, isPublic: Boolean): Result<Deck> {
        val newDeck = Deck(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
        )
        return deckRepository.createDeck(newDeck, isPublic)
    }
}
