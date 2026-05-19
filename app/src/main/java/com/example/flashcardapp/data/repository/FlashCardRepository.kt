package com.example.flashcardapp.data.repository

import com.example.flashcardapp.data.datasource.local.dao.FlashCardDao
import com.example.flashcardapp.data.datasource.local.OfflineCardImageStore
import com.example.flashcardapp.data.datasource.local.entity.FlashCardEntity
import com.example.flashcardapp.data.datasource.remote.api.CardApiService
import com.example.flashcardapp.data.datasource.remote.model.toDto
import com.example.flashcardapp.domain.model.FlashCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FlashCardRepository(
    private val cardApiService: CardApiService,
    private val flashCardDao: FlashCardDao,
    private val offlineCardImageStore: OfflineCardImageStore
) {
    suspend fun addCardsBulk(cards: List<FlashCard>) = withContext(Dispatchers.IO) {
        if (cards.isEmpty()) return@withContext

        val requestList = cards.map { it.toDto() }
        val response = cardApiService.addCard(requestList)
        if (!response.isSuccess()) {
            throw Exception("Failed to sync cards to server: ${response.message}")
        }

        flashCardDao.insertAllCards(cards.map { it.toEntity(isSynced = true) })
    }

    fun getCardsByDeckIdFromDb(deckId: String): Flow<List<FlashCardEntity>> {
        return flashCardDao.getCardsByDeckId(deckId)
    }

    suspend fun getCardsByDeckIdFromApi(deckId: String): List<FlashCard> = withContext(Dispatchers.IO) {
        try {
            val response = cardApiService.getCardOfDeck(deckId)
            if (response.isSuccess() && response.data != null) {
                val cards = offlineCardImageStore.cacheImages(
                    response.data.map { it.toDomain().copy(deckId = deckId) }
                )
                flashCardDao.insertAllCards(cards.map { it.toEntity(isSynced = true) })
                cards
            } else {
                cachedCardsOrFailure(deckId, Exception("Failed to fetch cards: ${response.message}"))
            }
        } catch (e: Exception) {
            cachedCardsOrFailure(deckId, e)
        }
    }

    suspend fun insertCard(card: FlashCard) = withContext(Dispatchers.IO) {
        flashCardDao.insertCard(card.toEntity(isSynced = false))

        try {
            val unsyncedCards = flashCardDao.getUnsyncedCards()
            val response = cardApiService.addCard(
                unsyncedCards.map {
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
            )

            if (response.isSuccess()) {
                flashCardDao.insertAllCards(unsyncedCards.map { it.copy(isSynced = true) })
            } else {
                throw Exception("Failed to sync cards to server: ${response.message}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateCard(card: FlashCard) = withContext(Dispatchers.IO) {
        try {
            val response = cardApiService.updateCard(card.id, card.toDto())
            if (response.isSuccess()) {
                flashCardDao.updateCard(card.toEntity(isSynced = true))
            } else {
                throw Exception("Failed to update card to server: ${response.message}")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteCard(card: FlashCard) {
        try {
            val response = cardApiService.updateCard(
                card.id,
                card.toDto().copy(isDeleted = true)
            )
            if (response.isSuccess()) {
                flashCardDao.deleteCard(card.toEntity(isSynced = true))
            } else {
                throw Exception("Failed to delete card on server: ${response.message}")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteCardsByDeckId(deckId: String) {
        flashCardDao.deleteCardsByDeckId(deckId)
    }

    private suspend fun cachedCardsOrFailure(deckId: String, error: Exception): List<FlashCard> {
        val cachedCards = flashCardDao.getCardsSnapshotByDeckId(deckId).map { it.toDomain() }
        if (cachedCards.isNotEmpty()) {
            return cachedCards
        }
        throw error
    }

    private fun FlashCard.toEntity(isSynced: Boolean): FlashCardEntity {
        return FlashCardEntity(
            id = id,
            question = question,
            answer = answer,
            imageUrl = imageUrl,
            localImagePath = localImagePath,
            deckId = deckId,
            interval = interval,
            repetition = repetition,
            easeFactor = easeFactor,
            nextReviewDate = nextReviewDate,
            isSynced = isSynced
        )
    }

    private fun FlashCardEntity.toDomain(): FlashCard {
        return FlashCard(
            id = id,
            question = question,
            answer = answer,
            imageUrl = imageUrl,
            localImagePath = localImagePath,
            deckId = deckId,
            interval = interval,
            repetition = repetition,
            easeFactor = easeFactor,
            nextReviewDate = nextReviewDate
        )
    }
}
