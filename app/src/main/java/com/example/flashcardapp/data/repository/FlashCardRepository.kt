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

        flashCardDao.insertAllCards(cards.map { it.toEntity(isSynced = false) })
        syncPendingCards()
    }

    suspend fun syncPendingCards(): Result<Int> = withContext(Dispatchers.IO) {
        val unsyncedCards = flashCardDao.getUnsyncedCards()
        if (unsyncedCards.isEmpty()) {
            return@withContext Result.success(0)
        }

        try {
            val response = cardApiService.addCard(
                unsyncedCards.map { entity ->
                    entity.toDomain().toDto().copy(isDeleted = entity.isDeleted)
                }
            )
            if (response.isSuccess()) {
                val syncedCards = response.data
                    ?.map { dto -> dto.toDomain().toEntity(isSynced = true) }
                    ?: unsyncedCards.map { it.copy(isSynced = true) }
                flashCardDao.insertAllCards(syncedCards)
                Result.success(unsyncedCards.size)
            } else {
                Result.failure(Exception("Failed to sync cards to server: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPendingCardCount(): Int = withContext(Dispatchers.IO) {
        flashCardDao.getUnsyncedCardCount()
    }

    fun getCardsByDeckIdFromDb(deckId: String): Flow<List<FlashCardEntity>> {
        return flashCardDao.getCardsByDeckId(deckId)
    }

    suspend fun getCardsByDeckIdFromApi(deckId: String): List<FlashCard> {
        syncPendingCards()
        val response = cardApiService.getCardOfDeck(deckId)
        if (response.isSuccess() && response.data != null) {
            val listCards = response.data
                .filterNot { it.deleted == true || it.isDeleted == true }
                .map { it.toDomain() }

            val hasPendingCardsForDeck = flashCardDao.getUnsyncedCards()
                .any { it.deckId == deckId }
            if (!hasPendingCardsForDeck) {
                flashCardDao.deleteCardsByDeckId(deckId)
            }
            flashCardDao.insertAllCards(listCards.map { it.toEntity(isSynced = true) })
            return withContext(Dispatchers.IO) { listCards }
        } else {
            throw Exception("Failed to fetch cards: ${response.message}")
        }
    }

    suspend fun insertCard(card: FlashCard) = withContext(Dispatchers.IO) {
        flashCardDao.insertCard(card.toEntity(isSynced = false))
        syncPendingCards()
    }

    suspend fun updateCard(card: FlashCard) = withContext(Dispatchers.IO) {
        flashCardDao.insertCard(card.toEntity(isSynced = false))
        syncPendingCards()
    }

    suspend fun deleteCard(card: FlashCard) = withContext(Dispatchers.IO) {
        flashCardDao.insertCard(card.toEntity(isSynced = false, isDeleted = true))
        syncPendingCards()
    }

    suspend fun deleteCardsByDeckId(deckId: String) {
        flashCardDao.deleteCardsByDeckId(deckId)
    }

    private fun FlashCard.toEntity(
        isSynced: Boolean,
        isDeleted: Boolean = false
    ): FlashCardEntity {
        return FlashCardEntity(
            id = id,
            question = question,
            answer = answer,
            imageUrl = imageUrl,
            deckId = deckId,
            interval = interval,
            repetition = repetition,
            easeFactor = easeFactor,
            nextReviewDate = nextReviewDate,
            isSynced = isSynced,
            isDeleted = isDeleted
        )
    }

    private fun FlashCardEntity.toDomain(): FlashCard {
        return FlashCard(
            id = id,
            question = question,
            answer = answer,
            imageUrl = imageUrl,
            deckId = deckId,
            interval = interval,
            repetition = repetition,
            easeFactor = easeFactor,
            nextReviewDate = nextReviewDate
        )
    }
}
