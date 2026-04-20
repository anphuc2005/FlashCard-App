package com.example.flashcardapp.presentation.feature.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.core.utils.MockDeckData
import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.data.repository.DeckRepository
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

class DeckViewModel(private val deckRepository: DeckRepository?) : ViewModel() {

    private val _deckUiState = MutableStateFlow<DeckUiState>(DeckUiState.Loading)
    val deckUiState: StateFlow<DeckUiState> = _deckUiState.asStateFlow()

    private val _exploreState = MutableStateFlow<Result<List<Deck>>?>(null)
    val exploreState: StateFlow<Result<List<Deck>>?> = _exploreState.asStateFlow()

    private val _cloneState = MutableStateFlow<Result<Deck>?>(null)
    val cloneState: StateFlow<Result<Deck>?> = _cloneState.asStateFlow()

    init {
        // If repository is null, use mock data for testing
        if (deckRepository == null) {
            loadMockData()
        } else {
            observeLocalDecks()
            syncDecksFromApi()
        }
    }

    /**
     * Load mock data for UI testing - temporary
     */
    private fun loadMockData() {
        val mockDecks = MockDeckData.getMockDecks()

        _deckUiState.value = DeckUiState.Success(mockDecks)
    }

    private fun observeLocalDecks() {
        viewModelScope.launch {
            if (deckRepository == null) return@launch
            
            deckRepository.getAllDecksFromDb().collect { entities ->
                val decks = entities.map { entity ->
                    Deck(
                        id = entity.id,
                        name = entity.name,
                        description = entity.description,
                        createdAt = entity.createdAt,
                        updatedAt = entity.updatedAt
                    )
                }
                
                if (decks.isEmpty()) {
                    _deckUiState.value = DeckUiState.Empty
                } else {
                    _deckUiState.value = DeckUiState.Success(decks)
                }
            }
        }
    }

    fun syncDecksFromApi() {
        viewModelScope.launch {
            if (deckRepository == null) return@launch

            try {
                // The repository fetches from API and inserts into Room DB.
                // Our observeLocalDecks() will automatically pick up the changes.
                deckRepository.getAllDecksFromApi()
            } catch (exception: Exception) {
                // Optionally handle sync error without breaking local state
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
                // Flow from Room will automatically emit new list, no need to manually refresh
            }
        }
    }

    fun updateDeckLastStudied(deck: Deck) {
        viewModelScope.launch {
            if (deckRepository != null) {
                val updatedDeck = deck.copy(updatedAt = System.currentTimeMillis().toString())
                deckRepository.updateDeckLocal(updatedDeck)
            }
        }
    }

    fun exploreDecks() {
        viewModelScope.launch {
            if (deckRepository == null) return@launch
            try {
                _exploreState.value = deckRepository.exploreDecks()
            } catch (e: Exception) {
                _exploreState.value = Result.failure(e)
            }
        }
    }

    fun cloneDeck(deckId: String) {
        viewModelScope.launch {
            if (deckRepository == null) return@launch
            try {
                _cloneState.value = deckRepository.cloneDeck(deckId)
            } catch (e: Exception) {
                _cloneState.value = Result.failure(e)
            }
        }
    }
}
