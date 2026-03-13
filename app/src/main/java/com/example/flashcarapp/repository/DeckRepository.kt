package com.example.flashcarapp.repository

import com.example.flashcarapp.data.dao.DeckDao
import com.example.flashcarapp.data.entity.DeckEntity
import com.example.flashcarapp.model.Deck
import com.example.flashcarapp.network.DeckApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeckRepository(
    private val deckApiService: DeckApiService,
    private val deckDao: DeckDao
) {

    // Lấy tất cả bộ thẻ từ API
    suspend fun getAllDecksFromApi(): Result<List<Deck>> {
        return try {
            val response = deckApiService.getAllDecks()
            if (response.success && response.data != null) {
                // Lưu vào local database
                response.data.forEach { deck ->
                    val deckEntity = DeckEntity(
                        id = deck.id,
                        name = deck.name,
                        description = deck.description,
                        createdAt = deck.createdAt,
                        updatedAt = deck.updatedAt
                    )
                    deckDao.insertDeck(deckEntity)
                }
                Result.success(response.data)
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
            if (response.success && response.data != null) {
                val deckEntity = DeckEntity(
                    id = response.data.id,
                    name = response.data.name,
                    description = response.data.description,
                    createdAt = response.data.createdAt,
                    updatedAt = response.data.updatedAt
                )
                deckDao.insertDeck(deckEntity)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Tạo bộ thẻ mới
    suspend fun createDeck(deck: Deck): Result<Deck> {
        return try {
            val response = deckApiService.createDeck(deck)
            if (response.success && response.data != null) {
                val deckEntity = DeckEntity(
                    id = response.data.id,
                    name = response.data.name,
                    description = response.data.description,
                    createdAt = response.data.createdAt,
                    updatedAt = response.data.updatedAt
                )
                deckDao.insertDeck(deckEntity)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cập nhật bộ thẻ
    suspend fun updateDeck(id: String, deck: Deck): Result<Deck> {
        return try {
            val response = deckApiService.updateDeck(id, deck)
            if (response.success && response.data != null) {
                val deckEntity = DeckEntity(
                    id = response.data.id,
                    name = response.data.name,
                    description = response.data.description,
                    createdAt = response.data.createdAt,
                    updatedAt = response.data.updatedAt
                )
                deckDao.updateDeck(deckEntity)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Xóa bộ thẻ
    suspend fun deleteDeck(id: String): Result<String> {
        return try {
            val response = deckApiService.deleteDeck(id)
            if (response.success) {
                deckDao.deleteDeckById(id)
                Result.success(response.message ?: "Deleted successfully")
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

