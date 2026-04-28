package com.example.flashcardapp.data.repository

import com.example.flashcardapp.data.datasource.local.dao.DeckDao
import com.example.flashcardapp.data.datasource.local.entity.DeckEntity
import com.example.flashcardapp.data.datasource.remote.api.DeckApiService
import com.example.flashcardapp.data.datasource.remote.model.toDto
import com.example.flashcardapp.domain.model.Deck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DeckRepository(
    private val deckApiService: DeckApiService,
    private val deckDao: DeckDao
) {

    // Lấy tất cả bộ thẻ từ API
    suspend fun getAllDecksFromApi(): Result<List<Deck>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deckApiService.getAllDecks()
                if (response.isSuccess() && response.data != null) {
                    val domainDecks = response.data.map { it.toDomain() }
                    domainDecks.forEach { deck ->
                        val deckEntity = DeckEntity(
                            id = deck.id,
                            categoryId = deck.categoryId,
                            name = deck.name,
                            description = deck.description,
                            isPublic = deck.isPublic,
                            createdAt = deck.createdAt,
                            updatedAt = deck.updatedAt
                        )
                        deckDao.insertDeck(deckEntity)
                    }
                    Result.success(domainDecks)
                } else {
                    Result.failure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Lấy bộ thẻ từ local database
    fun getAllDecksFromDb(): Flow<List<DeckEntity>> {
        return deckDao.getAllDecks()
    }

    // Lấy bộ thẻ kèm số lượng thẻ từ local database
    fun getAllDecksWithCardCountFromDb(): Flow<List<com.example.flashcardapp.data.datasource.local.entity.DeckWithCardCount>> {
        return deckDao.getAllDecksWithCardCount()
    }

    // Lấy bộ thẻ theo ID từ API
    suspend fun getDeckByIdFromApi(id: String): Result<Deck> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deckApiService.getDeckById(id)
                if (response.isSuccess() && response.data != null) {
                    val domainDeck = response.data.toDomain()
                    val deckEntity = DeckEntity(
                        id = domainDeck.id,
                        categoryId = domainDeck.categoryId,
                        name = domainDeck.name,
                        description = domainDeck.description,
                        isPublic = domainDeck.isPublic,
                        createdAt = domainDeck.createdAt,
                        updatedAt = domainDeck.updatedAt
                    )
                    deckDao.insertDeck(deckEntity)
                    Result.success(domainDeck)
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

    // Clone một bộ thẻ
    suspend fun cloneDeck(deckId: String): Result<Deck> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deckApiService.cloneDeck(deckId)
                if (response.isSuccess() && response.data != null) {
                    val domainDeck = response.data.toDomain()
                    val deckEntity = DeckEntity(
                        id = domainDeck.id,
                        categoryId = domainDeck.categoryId,
                        name = domainDeck.name,
                        description = domainDeck.description,
                        isPublic = domainDeck.isPublic,
                        createdAt = domainDeck.createdAt,
                        updatedAt = domainDeck.updatedAt
                    )
                    deckDao.insertDeck(deckEntity)
                    Result.success(domainDeck)
                } else {
                    Result.failure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Tạo bộ thẻ mới
    suspend fun createDeck(deck: Deck, isPublic: Boolean = true): Result<Deck> {
        return withContext(Dispatchers.IO) {
            try {
                if (isPublic) {
                    val response = deckApiService.createDeck(deck.toDto())
                    if (response.isSuccess() && response.data != null) {
                        val domainDeck = response.data.toDomain()
                        val deckEntity = DeckEntity(
                            id = domainDeck.id,
                            categoryId = domainDeck.categoryId,
                            name = domainDeck.name,
                            description = domainDeck.description,
                            isPublic = domainDeck.isPublic,
                            createdAt = domainDeck.createdAt,
                            updatedAt = domainDeck.updatedAt
                        )
                        deckDao.insertDeck(deckEntity)
                        Result.success(domainDeck)
                    } else {
                        Result.failure(Exception(response.message ?: "Unknown error"))
                    }
                } else {
                    val deckEntity = DeckEntity(
                        id = deck.id,
                        categoryId = deck.categoryId,
                        name = deck.name,
                        description = deck.description,
                        isPublic = deck.isPublic,
                        createdAt = deck.createdAt,
                        updatedAt = deck.updatedAt
                    )
                    deckDao.insertDeck(deckEntity)
                    Result.success(deck)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Cập nhật bộ thẻ
    suspend fun updateDeck(id: String, deck: Deck): Result<Deck> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deckApiService.updateDeck(id, deck.toDto())
                if (response.isSuccess() && response.data != null) {
                    val domainDeck = response.data.toDomain()
                    val deckEntity = DeckEntity(
                        id = domainDeck.id,
                        categoryId = domainDeck.categoryId,
                        name = domainDeck.name,
                        description = domainDeck.description,
                        isPublic = domainDeck.isPublic,
                        createdAt = domainDeck.createdAt,
                        updatedAt = domainDeck.updatedAt
                    )
                    deckDao.updateDeck(deckEntity)
                    Result.success(domainDeck)
                } else {
                    Result.failure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Cập nhật bộ thẻ tại local (Dành cho tracking lịch sử học tập)
    suspend fun updateDeckLocal(deck: Deck) = withContext(Dispatchers.IO) {
        val deckEntity = DeckEntity(
            id = deck.id,
            categoryId = deck.categoryId,
            name = deck.name,
            description = deck.description,
            isPublic = deck.isPublic,
            createdAt = deck.createdAt,
            updatedAt = deck.updatedAt
        )
        deckDao.updateDeck(deckEntity)
    }

    // Chỉ cập nhật thời gian học gần nhất của bộ thẻ tại local
    suspend fun touchDeckUpdatedAt(
        deckId: String,
        updatedAt: String = System.currentTimeMillis().toString()
    ) = withContext(Dispatchers.IO) {
        deckDao.touchDeckUpdatedAt(deckId, updatedAt)
    }

    // Xóa bộ thẻ
    suspend fun deleteDeck(id: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deckApiService.deleteDeck(id)
                if (response.isSuccess()) {
                    deckDao.getDeckById(id)?.let { deckDao.deleteDeck(it) }
                    Result.success(response.message ?: "Deleted successfully")
                } else {
                    Result.failure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Xóa bộ thẻ chỉ trên server (khi tắt public)
    suspend fun deleteDeckFromServerOnly(id: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {

                val response = deckApiService.deleteDeck(id)
                if (response.isSuccess()) {
                    Result.success(response.message ?: "Deleted successfully on server")
                } else {
                    Result.failure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
}
