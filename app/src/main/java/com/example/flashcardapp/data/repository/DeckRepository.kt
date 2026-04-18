package com.example.flashcardapp.data.repository

import com.example.flashcardapp.data.datasource.local.dao.DeckDao
import com.example.flashcardapp.data.datasource.local.entity.DeckEntity
import com.example.flashcardapp.data.datasource.remote.api.DeckApiService
import com.example.flashcardapp.data.datasource.remote.model.toDto
import com.example.flashcardapp.domain.model.Deck
import kotlinx.coroutines.flow.Flow

class DeckRepository(
    private val deckApiService: DeckApiService,
    private val deckDao: DeckDao
) {

    // Lấy tất cả bộ thẻ từ API
    suspend fun getAllDecksFromApi(): Result<List<Deck>> {
        return try {
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

    // Lấy bộ thẻ từ local database
    fun getAllDecksFromDb(): Flow<List<DeckEntity>> {
        return deckDao.getAllDecks()
    }

    // Lấy bộ thẻ theo ID từ API
    suspend fun getDeckByIdFromApi(id: String): Result<Deck> {
        return try {
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

    // Khám phá các bộ thẻ cộng đồng
    suspend fun exploreDecks(): Result<List<Deck>> {
        return try {
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

    // Clone một bộ thẻ
    suspend fun cloneDeck(deckId: String): Result<Deck> {
        return try {
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

    // Tạo bộ thẻ mới
    suspend fun createDeck(deck: Deck, isPublic: Boolean = true): Result<Deck> {
        return try {
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

    // Cập nhật bộ thẻ
    suspend fun updateDeck(id: String, deck: Deck): Result<Deck> {
        return try {
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

    // Cập nhật bộ thẻ tại local (Dành cho tracking lịch sử học tập)
    suspend fun updateDeckLocal(deck: Deck) {
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

    // Xóa bộ thẻ
    suspend fun deleteDeck(id: String): Result<String> {
        return try {
            val response = deckApiService.deleteDeck(id)
            if (response.isSuccess()) {
                deckDao.deleteDeckById(id)
                Result.success(response.message ?: "Deleted successfully")
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Xóa bộ thẻ chỉ trên server (khi tắt public)
    suspend fun deleteDeckFromServerOnly(id: String): Result<String> {
        return try {
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
