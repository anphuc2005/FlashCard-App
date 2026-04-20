package com.example.flashcardapp.domain.usecase.deck

import com.example.flashcardapp.data.repository.DeckRepository
import com.example.flashcardapp.domain.model.Deck

class GetDeckByIdUseCase(
    private val deckRepository: DeckRepository
) {
    suspend operator fun invoke(id: String): Result<Deck> {
        return deckRepository.getDeckByIdFromApi(id)
    }
}
