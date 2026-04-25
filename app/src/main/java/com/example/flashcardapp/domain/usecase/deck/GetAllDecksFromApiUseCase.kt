package com.example.flashcardapp.domain.usecase.deck

import com.example.flashcardapp.data.repository.DeckRepository
import com.example.flashcardapp.domain.model.Deck

class GetExploreDecksFromApiUseCase(private val deckRepository: DeckRepository) {
    suspend operator fun invoke(): Result<List<Deck>> {
        return deckRepository.exploreDecks()
    }
}

