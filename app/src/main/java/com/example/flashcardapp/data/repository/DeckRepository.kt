package com.example.flashcardapp.data.repository

import com.example.flashcardapp.data.datasource.remote.api.DeckApiService
import com.example.flashcardapp.data.datasource.remote.model.toDto
import com.example.flashcardapp.domain.model.Deck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeckRepository(
    private val deckApiService: DeckApiService
) {

    suspend fun getAllDecksFromApi(): Result<List<Deck>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deckApiService.getAllDecks()
                if (response.isSuccess() && response.data != null) {
                    Result.success(response.data.map { it.toDomain() })
                } else {
                    Result.failure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getDeckByIdFromApi(id: String): Result<Deck> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deckApiService.getDeckById(id)
                if (response.isSuccess() && response.data != null) {
                    Result.success(response.data.toDomain())
                } else {
                    Result.failure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun exploreDecks(): Result<List<Deck>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deckApiService.exploreDecks()
                if (response.isSuccess() && response.data != null) {
                    Result.success(response.data.map { it.toDomain() })
                } else {
                    Result.failure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun cloneDeck(deckId: String): Result<Deck> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deckApiService.cloneDeck(deckId)
                if (response.isSuccess() && response.data != null) {
                    Result.success(response.data.toDomain())
                } else {
                    Result.failure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createDeck(deck: Deck): Result<Deck> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deckApiService.createDeck(deck.toDto())
                if (response.isSuccess() && response.data != null) {
                    Result.success(response.data.toDomain())
                } else {
                    Result.failure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateDeck(id: String, deck: Deck): Result<Deck> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deckApiService.updateDeck(id, deck.toDto())
                if (response.isSuccess() && response.data != null) {
                    Result.success(response.data.toDomain())
                } else {
                    Result.failure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteDeck(id: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deckApiService.deleteDeck(id)
                if (response.isSuccess()) {
                    Result.success(response.message ?: "Deleted successfully")
                } else {
                    Result.failure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
