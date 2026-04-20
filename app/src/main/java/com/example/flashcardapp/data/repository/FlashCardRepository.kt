package com.example.flashcardapp.data.repository

import com.example.flashcardapp.data.datasource.local.dao.FlashCardDao
import com.example.flashcardapp.data.datasource.local.entity.FlashCardEntity
import com.example.flashcardapp.data.datasource.remote.api.CardApiService
import com.example.flashcardapp.data.datasource.remote.model.toDto
import com.example.flashcardapp.domain.model.FlashCard
import kotlinx.coroutines.flow.Flow

class FlashCardRepository(
    private val cardApiService: CardApiService,
    private val flashCardDao: FlashCardDao
) {
    // Lấy thẻ từ local database theo Deck ID
    fun getCardsByDeckIdFromDb(deckId: String): Flow<List<FlashCardEntity>> {
        return flashCardDao.getCardsByDeckId(deckId)
    }

    // Lấy thẻ từ API theo Deck ID
    suspend fun getCardsByDeckIdFromApi(deckId: String): List<FlashCard> {
        val response = cardApiService.getCardOfDeck(deckId)
        if (response.status == 200 && response.data != null) {
            val listCards = response.data.map { it.toDomain() }
            
            // Xoá và lưu đè lại vào bộ nhớ Local (tuỳ chọn)
            try {
                flashCardDao.deleteCardsByDeckId(deckId)
                val entities = listCards.map {
                    FlashCardEntity(
                        id = it.id,
                        question = it.question,
                        answer = it.answer,
                        deckId = it.deckId,
                        isSynced = true
                    )
                }
                flashCardDao.insertAllCards(entities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            return listCards
        } else {
            throw Exception("Failed to fetch cards: ${response.message}")
        }
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
            deckId = card.deckId,
            isSynced = false // Mặc định là chưa đồng bộ
        )
        // Lưu tạm vào Local để cập nhật giao diện (Offline-first)
        flashCardDao.insertCard(cardEntity)

        try {
            // Danh sách các thẻ chưa đồng bộ
            val unsyncedCards = flashCardDao.getUnsyncedCards()
            
            // Xây dựng List chứa các Unsynced Cards (nếu hệ thống API yêu cầu List)
            val requestList = unsyncedCards.map {
                FlashCard(
                    id = it.id, 
                    question = it.question, 
                    answer = it.answer, 
                    deckId = it.deckId
                ).toDto()
            }

            // Gọi API đưa List này lên server
            val response = cardApiService.addCard(requestList)
            
            if (response.status == 200 || response.status == 201) {
                // Đánh dấu tüm các thẻ này thành đã đồng bộ
                val syncedCards = unsyncedCards.map { it.copy(isSynced = true) }
                flashCardDao.insertAllCards(syncedCards)
            } else {
                // Failed API - nó vẫn được lưu ở Local với isSynced = false
                throw Exception("Failed to sync cards to server: ${response.message}")
            }
        } catch (e: Exception) {
            // Không ngắt ứng dụng, cho phép lưu offline, và khi có mạng (hoặc khi thêm thẻ tiếp theo) nó sẽ được retry push lên.
            e.printStackTrace()
        }
    }

    // Cập nhật thẻ
    suspend fun updateCard(card: FlashCard) {
        try {
            // Update to remote first
            val response = cardApiService.updateCard(card.id, card.toDto())
            if (response.status == 200 || response.status == 201) {
                val cardEntity = FlashCardEntity(
                    id = card.id,
                    question = card.question,
                    answer = card.answer,
                    deckId = card.deckId
                )
                flashCardDao.updateCard(cardEntity)
            } else {
                throw Exception("Failed to update card to server: ${response.message}")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    // Xóa thẻ
    suspend fun deleteCard(card: FlashCard) {
        try {
            val response = cardApiService.deleteCard(card.id)
            if (response.status == 200) {
                val cardEntity = FlashCardEntity(
                    id = card.id,
                    question = card.question,
                    answer = card.answer,
                    deckId = card.deckId
                )
                flashCardDao.deleteCard(cardEntity)
            } else {
                throw Exception("Failed to delete card on server: ${response.message}")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    // Xóa các thẻ của một Deck
    suspend fun deleteCardsByDeckId(deckId: String) {
        flashCardDao.deleteCardsByDeckId(deckId)
    }
}
