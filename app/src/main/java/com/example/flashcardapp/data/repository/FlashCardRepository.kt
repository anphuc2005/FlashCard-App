package com.example.flashcardapp.data.repository

import com.example.flashcardapp.data.datasource.local.dao.FlashCardDao
import com.example.flashcardapp.data.datasource.local.entity.FlashCardEntity
import com.example.flashcardapp.data.datasource.remote.api.DeckApiService
import com.example.flashcardapp.domain.model.FlashCard
import kotlinx.coroutines.flow.Flow

class FlashCardRepository(
    private val deckApiService: DeckApiService,
    private val flashCardDao: FlashCardDao
) {

    // Lấy tất cả thẻ từ API theo Deck ID
    suspend fun getCardsByDeckIdFromApi(deckId: String): Result<List<FlashCard>> {
        return try {
            val response = deckApiService.getCardsByDeckId(deckId)
            if (response.success && response.data != null) {
                // Lưu vào local database
                response.data.forEach { card ->
                    val cardEntity = FlashCardEntity(
                        id = card.id,
                        question = card.question,
                        answer = card.answer,
                        deckId = card.deckId
                    )
                    flashCardDao.insertCard(cardEntity)
                }
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Lấy thẻ từ local database theo Deck ID
    fun getCardsByDeckIdFromDb(deckId: String): Flow<List<FlashCardEntity>> {
        return flashCardDao.getCardsByDeckId(deckId)
    }

    // Lấy tất cả thẻ từ local database
    fun getAllCardsFromDb(): Flow<List<FlashCardEntity>> {
        return flashCardDao.getAllCards()
    }

    // Thêm thẻ vào local database
    suspend fun insertCard(card: FlashCard) {
        val cardEntity = FlashCardEntity(
            id = card.id,
            question = card.question,
            answer = card.answer,
            deckId = card.deckId
        )
        flashCardDao.insertCard(cardEntity)
    }

    // Cập nhật thẻ
    suspend fun updateCard(card: FlashCard) {
        val cardEntity = FlashCardEntity(
            id = card.id,
            question = card.question,
            answer = card.answer,
            deckId = card.deckId
        )
        flashCardDao.updateCard(cardEntity)
    }

    // Xóa thẻ
    suspend fun deleteCard(card: FlashCard) {
        val cardEntity = FlashCardEntity(
            id = card.id,
            question = card.question,
            answer = card.answer,
            deckId = card.deckId
        )
        flashCardDao.deleteCard(cardEntity)
    }

    // Xóa các thẻ của một Deck
    suspend fun deleteCardsByDeckId(deckId: String) {
        flashCardDao.deleteCardsByDeckId(deckId)
    }
}

