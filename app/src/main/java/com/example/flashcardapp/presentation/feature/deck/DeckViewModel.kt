package com.example.flashcardapp.presentation.feature.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.core.utils.MockDeckData
import com.example.flashcardapp.data.repository.DeckRepository
import com.example.flashcardapp.domain.model.Deck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.Locale

sealed class DeckUiState {
    object Loading : DeckUiState()
    data class Success(val decks: List<Deck>) : DeckUiState()
    data class Error(val message: String) : DeckUiState()
    object Empty : DeckUiState()
}

class DeckViewModel(
    private val deckRepository: DeckRepository?
) : ViewModel() {

    private val _deckUiState = MutableStateFlow<DeckUiState>(DeckUiState.Loading)
    val deckUiState: StateFlow<DeckUiState> = _deckUiState.asStateFlow()

    private val _exploreState = MutableStateFlow<Result<List<Deck>>?>(null)
    val exploreState: StateFlow<Result<List<Deck>>?> = _exploreState.asStateFlow()

    private val _cloneState = MutableStateFlow<Result<Deck>?>(null)
    val cloneState: StateFlow<Result<Deck>?> = _cloneState.asStateFlow()

    private var allDecks: List<Deck> = emptyList()
    private var currentSearchQuery: String = ""

    init {
        if (deckRepository == null) {
            loadMockData()
        } else {
            syncDecksFromApi()
        }
    }

    private fun loadMockData() {
        allDecks = MockDeckData.getMockDecks()
        publishFilteredDecks()
    }

    fun syncDecksFromApi() {
        viewModelScope.launch {
            if (deckRepository == null) return@launch

            _deckUiState.value = DeckUiState.Loading

            deckRepository.getAllDecksFromApi()
                .onSuccess { decks ->
                    allDecks = decks
                    publishFilteredDecks()
                }
                .onFailure { error ->
                    _deckUiState.value = DeckUiState.Error(
                        error.message ?: "Có lỗi xảy ra khi tải danh sách bộ thẻ"
                    )
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
                    updatedAt = System.currentTimeMillis().toString(),
                    isPublic = true
                )
                deckRepository.createDeck(newDeck)
                syncDecksFromApi()
            }
        }
    }

    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            if (deckRepository != null) {
                deckRepository.deleteDeck(deckId)
                syncDecksFromApi()
            }
        }
    }

    fun updateDeckLastStudied(deck: Deck) {
        val now = System.currentTimeMillis().toString()
        allDecks = allDecks.map {
            if (it.id == deck.id) it.copy(updatedAt = now) else it
        }.sortedByDescending { it.updatedAt ?: "" }

        publishFilteredDecks()
    }

    fun updateSearchQuery(query: String) {
        currentSearchQuery = query
        publishFilteredDecks()
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

    private fun publishFilteredDecks() {
        val normalizedQuery = normalizeForSearch(currentSearchQuery)
        val filteredDecks = if (normalizedQuery.isBlank()) {
            allDecks
        } else {
            allDecks.filter { deck ->
                val normalizedName = normalizeForSearch(deck.name)
                val normalizedDescription = normalizeForSearch(deck.description.orEmpty())
                normalizedName.contains(normalizedQuery) || normalizedDescription.contains(normalizedQuery)
            }
        }

        _deckUiState.value = if (filteredDecks.isEmpty()) {
            DeckUiState.Empty
        } else {
            DeckUiState.Success(filteredDecks)
        }
    }

    private fun normalizeForSearch(raw: String): String {
        val normalized = Normalizer.normalize(raw.lowercase(Locale.getDefault()), Normalizer.Form.NFD)
        return normalized.replace("\\p{M}+".toRegex(), "").trim()
    }
}
