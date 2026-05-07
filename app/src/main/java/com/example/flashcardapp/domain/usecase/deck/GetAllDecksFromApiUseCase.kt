package com.example.flashcardapp.domain.usecase.deck

import com.example.flashcardapp.data.repository.DeckRepository
import com.example.flashcardapp.domain.model.DeckExplorePage

class GetExploreDecksFromApiUseCase(private val deckRepository: DeckRepository) {
    suspend operator fun invoke(
        page: Int = 0,
        size: Int = 5,
        query: String? = null
    ): Result<DeckExplorePage> {
        return deckRepository.exploreDecksPaged(page = page, size = size, query = query)
    }
}
