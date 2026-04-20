package com.example.flashcardapp.domain.usecase.deck

import com.example.flashcardapp.data.repository.DeckRepository
import com.example.flashcardapp.domain.model.Deck

class CloneDeckUseCase(private val deckRepository: DeckRepository) {
    suspend operator fun invoke(deckId: String): Result<Deck> {
        return deckRepository.cloneDeck(deckId)
    }
}

