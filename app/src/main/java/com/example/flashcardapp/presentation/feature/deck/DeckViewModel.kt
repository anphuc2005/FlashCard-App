package com.example.flashcardapp.presentation.feature.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.core.utils.MockDeckData
import com.example.flashcardapp.data.repository.DeckRepository
import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.domain.usecase.study.GetReviewedCardIdsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DeckUiState {
    object Loading : DeckUiState()
    data class Success(val decks: List<Deck>) : DeckUiState()
    data class Error(val message: String) : DeckUiState()
    object Empty : DeckUiState()
}

class DeckViewModel(
    private val deckRepository: DeckRepository?,
    private val getReviewedCardIdsUseCase: GetReviewedCardIdsUseCase? = null
) : ViewModel() {

    private val _deckUiState = MutableStateFlow<DeckUiState>(DeckUiState.Loading)
    val deckUiState: StateFlow<DeckUiState> = _deckUiState.asStateFlow()

    private val _exploreState = MutableStateFlow<Result<List<Deck>>?>(null)
    val exploreState: StateFlow<Result<List<Deck>>?> = _exploreState.asStateFlow()

    private val _cloneState = MutableStateFlow<Result<Deck>?>(null)
    val cloneState: StateFlow<Result<Deck>?> = _cloneState.asStateFlow()

    private val apiCardCountByDeckId = mutableMapOf<String, Int>()

    init {
        if (deckRepository == null) {
            loadMockData()
        } else {
            observeLocalDecks()
            syncDecksFromApi()
        }
    }

    private fun loadMockData() {
        _deckUiState.value = DeckUiState.Success(MockDeckData.getMockDecks())
    }

    private fun observeLocalDecks() {
        viewModelScope.launch {
            if (deckRepository == null) return@launch

            deckRepository.getAllDecksWithCardCountFromDb().collect { aggregations ->
                val decks = aggregations.map { aggregation ->
                    val entity = aggregation.deck
                    val resolvedCardCount = maxOf(
                        aggregation.cardCount,
                        apiCardCountByDeckId[entity.id] ?: 0
                    )
                    val reviewedCards = getReviewedCardIdsUseCase
                        ?.invoke(entity.id)
                        ?.getOrDefault(emptySet())
                        ?.size
                        ?: 0
                    val effectiveCardCount = resolvedCardCount.coerceAtLeast(0)
                    val normalizedReviewedCount = if (effectiveCardCount > 0) {
                        reviewedCards.coerceIn(0, effectiveCardCount)
                    } else {
                        0
                    }

                    Deck(
                        id = entity.id,
                        name = entity.name,
                        description = entity.description,
                        createdAt = entity.createdAt,
                        updatedAt = entity.updatedAt,
                        customCardCount = effectiveCardCount,
                        customStudiedCount = normalizedReviewedCount
                    )
                }

                _deckUiState.value = if (decks.isEmpty()) {
                    DeckUiState.Empty
                } else {
                    DeckUiState.Success(decks)
                }
            }
        }
    }

    fun syncDecksFromApi() {
        viewModelScope.launch {
            if (deckRepository == null) return@launch

            runCatching { deckRepository.getAllDecksFromApi() }
                .onSuccess { result ->
                    result.onSuccess { decks ->
                        apiCardCountByDeckId.clear()
                        decks.forEach { deck ->
                            apiCardCountByDeckId[deck.id] = deck.cardCount
                        }

                        val currentState = _deckUiState.value
                        if (currentState is DeckUiState.Success) {
                            val adjustedDecks = currentState.decks.map { deck ->
                                val resolvedCardCount = maxOf(
                                    deck.cardCount,
                                    apiCardCountByDeckId[deck.id] ?: 0
                                )
                                deck.copy(
                                    customCardCount = resolvedCardCount,
                                    customStudiedCount = deck.studiedCount.coerceIn(0, resolvedCardCount)
                                )
                            }
                            _deckUiState.value = DeckUiState.Success(adjustedDecks)
                        }
                    }
                }
        }
    }

    fun createDeck(name: String, description: String) {
        viewModelScope.launch {
            if (deckRepository != null) {
                val newDeck = Deck(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    description = description,
                    createdAt = System.currentTimeMillis().toString(),
                    updatedAt = System.currentTimeMillis().toString()
                )
                deckRepository.createDeck(newDeck, isPublic = true)
            }
        }
    }

    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            if (deckRepository != null) {
                deckRepository.deleteDeck(deckId)
            }
        }
    }

    fun updateDeckLastStudied(deck: Deck) {
        viewModelScope.launch {
            if (deckRepository != null) {
                deckRepository.touchDeckUpdatedAt(deck.id)
            }
        }
    }

    fun exploreDecks() {
        viewModelScope.launch {
            if (deckRepository == null) return@launch
            _exploreState.value = runCatching { deckRepository.exploreDecks() }
                .getOrElse { Result.failure(it) }
        }
    }

    fun cloneDeck(deckId: String) {
        viewModelScope.launch {
            if (deckRepository == null) return@launch
            _cloneState.value = runCatching { deckRepository.cloneDeck(deckId) }
                .getOrElse { Result.failure(it) }
        }
    }
}
