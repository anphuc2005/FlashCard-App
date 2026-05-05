package com.example.flashcardapp.data.repository

import com.example.flashcardapp.data.datasource.local.dao.FlashCardDao
import com.example.flashcardapp.data.datasource.local.entity.FlashCardEntity
import com.example.flashcardapp.data.datasource.remote.api.CardApiService
import com.example.flashcardapp.data.datasource.remote.model.toDto
import com.example.flashcardapp.domain.model.FlashCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FlashCardRepository(
    private val cardApiService: CardApiService,
    private val flashCardDao: FlashCardDao
) {
    suspend fun addCardsBulk(cards: List<FlashCard>) = withContext(Dispatchers.IO) {
        if (cards.isEmpty()) return@withContext

        val requestList = cards.map { it.toDto() }
        val response = cardApiService.addCard(requestList)
        if (!response.isSuccess()) {
            throw Exception("Failed to sync cards to server: ${response.message}")
        }

        val entities = cards.map {
            FlashCardEntity(
                id = it.id,
                question = it.question,
                answer = it.answer,
                imageUrl = it.imageUrl,
                deckId = it.deckId,
                interval = it.interval,
                repetition = it.repetition,
                easeFactor = it.easeFactor,
                nextReviewDate = it.nextReviewDate,
                isSynced = true
            )
        }
        flashCardDao.insertAllCards(entities)
    }

    // Lấy thẻ từ local database theo Deck ID
    fun getCardsByDeckIdFromDb(deckId: String): Flow<List<FlashCardEntity>> {
        return flashCardDao.getCardsByDeckId(deckId)
    }

    // Lấy thẻ từ API theo Deck ID
    suspend fun getCardsByDeckIdFromApi(deckId: String): List<FlashCard>  {
        val response = cardApiService.getCardOfDeck(deckId)
        if (response.isSuccess() && response.data != null) {
            val listCards = response.data.map { it.toDomain() }
            
            // Xoá và lưu đè lại vào bộ nhớ Local (tuỳ chọn)
            try {
                flashCardDao.deleteCardsByDeckId(deckId)
                val entities = listCards.map {
                    FlashCardEntity(
                        id = it.id,
                        question = it.question,
                        answer = it.answer,
                        imageUrl = it.imageUrl,
                        deckId = it.deckId,
                        interval = it.interval,
                        repetition = it.repetition,
                        easeFactor = it.easeFactor,
                        nextReviewDate = it.nextReviewDate,
                        isSynced = true
                    )
                }
                flashCardDao.insertAllCards(entities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            return withContext(Dispatchers.IO) {
                listCards
            }
        } else {
            throw Exception("Failed to fetch cards: ${response.message}")
        }
    }

    // Thêm thẻ vào local database
    suspend fun insertCard(card: FlashCard) = withContext(Dispatchers.IO) {
        val cardEntity = FlashCardEntity(
            id = card.id,
            question = card.question,
            answer = card.answer,
            imageUrl = card.imageUrl,
            deckId = card.deckId,
            interval = card.interval,
            repetition = card.repetition,
            easeFactor = card.easeFactor,
            nextReviewDate = card.nextReviewDate,
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
                    imageUrl = it.imageUrl,
                    deckId = it.deckId,
                    interval = it.interval,
                    repetition = it.repetition,
                    easeFactor = it.easeFactor,
                    nextReviewDate = it.nextReviewDate
                ).toDto()
            }

            // Gọi API đưa List này lên server
            val response = cardApiService.addCard(requestList)
            
            if (response.isSuccess()) {
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
    suspend fun updateCard(card: FlashCard) = withContext(Dispatchers.IO) {
        try {
            // Update to remote first
            val response = cardApiService.updateCard(card.id, card.toDto())
            if (response.isSuccess()) {
                val cardEntity = FlashCardEntity(
                    id = card.id,
                    question = card.question,
                    answer = card.answer,
                    imageUrl = card.imageUrl,
                    deckId = card.deckId,
                    interval = card.interval,
                    repetition = card.repetition,
                    easeFactor = card.easeFactor,
                    nextReviewDate = card.nextReviewDate
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
            val response = cardApiService.updateCard(
                card.id,
                card.toDto().copy(isDeleted = true)
            )
            if (response.isSuccess()) {
                val cardEntity = FlashCardEntity(
                    id = card.id,
                    question = card.question,
                    answer = card.answer,
                    imageUrl = card.imageUrl,
                    deckId = card.deckId,
                    interval = card.interval,
                    repetition = card.repetition,
                    easeFactor = card.easeFactor,
                    nextReviewDate = card.nextReviewDate
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
