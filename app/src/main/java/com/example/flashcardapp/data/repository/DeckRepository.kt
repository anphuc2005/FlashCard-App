package com.example.flashcardapp.data.repository

import com.example.flashcardapp.data.datasource.local.dao.DeckDao
import com.example.flashcardapp.data.datasource.local.dao.FlashCardDao
import com.example.flashcardapp.data.datasource.local.entity.DeckEntity
import com.example.flashcardapp.data.datasource.local.entity.FlashCardEntity
import com.example.flashcardapp.data.datasource.remote.api.CardApiService
import com.example.flashcardapp.data.datasource.remote.api.DeckApiService
import com.example.flashcardapp.data.datasource.remote.model.toDto
import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.domain.model.DeckExplorePage
import com.example.flashcardapp.domain.model.FlashCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

private const val OFFLINE_DECK_NAME_PREFIX = "B\u1ed9 th\u1ebb \u0111\u00e3 l\u01b0u"

class DeckRepository(
    private val deckApiService: DeckApiService,
    private val flashCardDao: FlashCardDao? = null,
    private val cardApiService: CardApiService? = null,
    private val deckDao: DeckDao? = null
) {

    suspend fun getAllDecksFromApi(): Result<List<Deck>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deckApiService.getAllDecks()
                if (response.isSuccess() && response.data != null) {
                    val decks = enrichDecksWithCardCount(response.data.map { it.toDomain() })
                    cacheDecks(decks)
                    Result.success(decks)
                } else {
                    cachedDecksOrFailure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                cachedDecksOrFailure(e)
            }
        }
    }

    suspend fun getDeckByIdFromApi(id: String): Result<Deck> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deckApiService.getDeckById(id)
                if (response.isSuccess() && response.data != null) {
                    val deck = enrichDeckWithCardCount(response.data.toDomain())
                    cacheDeck(deck)
                    Result.success(deck)
                } else {
                    cachedDeckByIdOrFailure(id, Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                cachedDeckByIdOrFailure(id, e)
            }
        }
    }

    suspend fun exploreDecks(): Result<List<Deck>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deckApiService.exploreDecks()
                if (response.isSuccess() && response.data != null) {
                    val decks = enrichDecksWithCardCount(response.data.map { it.toDomain() })
                    cacheDecks(decks)
                    Result.success(decks)
                } else {
                    cachedDecksOrFailure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                cachedDecksOrFailure(e)
            }
        }
    }

    suspend fun exploreDecksPaged(
        page: Int = 0,
        size: Int = 5,
        query: String? = null
    ): Result<DeckExplorePage> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deckApiService.exploreDecksPaged(
                    page = page,
                    size = size.coerceIn(1, 50),
                    query = query?.takeIf { it.isNotBlank() }
                )
                if (response.isSuccess() && response.data != null) {
                    val decks = enrichDecksWithCardCount(response.data.content.map { it.toDomain() })
                    cacheDecks(decks)
                    Result.success(response.data.toDomain(decks))
                } else {
                    cachedExplorePageOrFailure(
                        page = page,
                        size = size,
                        query = query,
                        error = Exception(response.message ?: "Unknown error")
                    )
                }
            } catch (e: Exception) {
                cachedExplorePageOrFailure(
                    page = page,
                    size = size,
                    query = query,
                    error = e
                )
            }
        }
    }

    suspend fun cloneDeck(deckId: String): Result<Deck> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deckApiService.cloneDeck(deckId)
                if (response.isSuccess() && response.data != null) {
                    val clonedDeck = enrichDeckWithCardCount(response.data.toDomain())
                    val cachedCardCount = cacheDeckCards(clonedDeck.id)
                    val deckToCache = cachedCardCount?.let {
                        clonedDeck.copy(customCardCount = it)
                    } ?: clonedDeck
                    cacheDeck(deckToCache)
                    Result.success(deckToCache)
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
                    val createdDeck = enrichDeckWithCardCount(response.data.toDomain())
                    cacheDeck(createdDeck)
                    Result.success(createdDeck)
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
                    val updatedDeck = enrichDeckWithCardCount(response.data.toDomain())
                    cacheDeck(updatedDeck)
                    Result.success(updatedDeck)
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
                    deckDao?.deleteDeckById(id)
                    flashCardDao?.deleteCardsByDeckId(id)
                    Result.success(response.message ?: "Deleted successfully")
                } else {
                    Result.failure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun enrichDecksWithCardCount(decks: List<Deck>): List<Deck> = coroutineScope {
        decks.map { deck ->
            async { enrichDeckWithCardCount(deck) }
        }.awaitAll()
    }

    private suspend fun enrichDeckWithCardCount(deck: Deck): Deck {
        val fallbackCount = deck.customCardCount ?: deck.cards.size
        val resolvedCount = runCatching {
            val response = deckApiService.getDeckCardCount(deck.id)
            if (response.isSuccess() && response.data != null) {
                response.data.totalCards
                    .coerceAtLeast(0L)
                    .coerceAtMost(Int.MAX_VALUE.toLong())
                    .toInt()
            } else {
                fallbackCount.coerceAtLeast(0)
            }
        }.getOrDefault(fallbackCount.coerceAtLeast(0))

        return deck.copy(customCardCount = resolvedCount)
    }

    private suspend fun cachedDecksOrFailure(error: Exception): Result<List<Deck>> {
        val cachedDecks = getCachedDecks()
        return if (cachedDecks.isNotEmpty()) {
            Result.success(cachedDecks)
        } else {
            Result.failure(error)
        }
    }

    private suspend fun cachedDeckByIdOrFailure(id: String, error: Exception): Result<Deck> {
        val cachedDeck = getCachedDecks().firstOrNull { it.id == id }
        return if (cachedDeck != null) {
            Result.success(cachedDeck)
        } else {
            Result.failure(error)
        }
    }

    private suspend fun cachedExplorePageOrFailure(
        page: Int,
        size: Int,
        query: String?,
        error: Exception
    ): Result<DeckExplorePage> {
        val normalizedQuery = query.orEmpty().trim()
        val cachedDecks = getCachedDecks()
            .filter { deck ->
                normalizedQuery.isBlank() ||
                    deck.name.contains(normalizedQuery, ignoreCase = true) ||
                    deck.description.orEmpty().contains(normalizedQuery, ignoreCase = true)
            }
        if (cachedDecks.isEmpty()) {
            return Result.failure(error)
        }

        val safeSize = size.coerceAtLeast(1)
        val totalElements = cachedDecks.size.toLong()
        val totalPages = ((cachedDecks.size + safeSize - 1) / safeSize).coerceAtLeast(1)
        val safePage = page.coerceIn(0, totalPages - 1)
        val fromIndex = safePage * safeSize
        val toIndex = (fromIndex + safeSize).coerceAtMost(cachedDecks.size)

        return Result.success(
            DeckExplorePage(
                content = cachedDecks.subList(fromIndex, toIndex),
                currentPage = safePage,
                pageSize = safeSize,
                totalElements = totalElements,
                totalPages = totalPages,
                first = safePage == 0,
                last = safePage >= totalPages - 1
            )
        )
    }

    private suspend fun getCachedDecks(): List<Deck> {
        val cardsByDeckId = flashCardDao
            ?.getAllCardsSnapshot()
            .orEmpty()
            .groupBy { it.deckId }

        val metadataDecks = deckDao
            ?.getAllDecksSnapshot()
            .orEmpty()
            .map { deckEntity ->
                val cards = cardsByDeckId[deckEntity.id].orEmpty().map { it.toDomain() }
                deckEntity.toDomain(
                    cards = cards,
                    cardCountOverride = cards.size.takeIf { it > 0 }
                )
            }

        val metadataDeckIds = metadataDecks.map { it.id }.toSet()
        val orphanCardDecks = cardsByDeckId
            .filterKeys { it !in metadataDeckIds }
            .entries
            .sortedBy { it.key }
            .mapIndexed { index, entry ->
                val cards = entry.value.map { it.toDomain() }
                Deck(
                    id = entry.key,
                    categoryId = null,
                    name = "$OFFLINE_DECK_NAME_PREFIX ${index + 1}",
                    cards = cards,
                    customCardCount = cards.size
                )
            }

        return metadataDecks + orphanCardDecks
    }

    private suspend fun cacheDeck(deck: Deck) {
        deckDao?.insertDeck(deck.toEntity())
    }

    private suspend fun cacheDecks(decks: List<Deck>) {
        if (decks.isNotEmpty()) {
            deckDao?.insertDecks(decks.map { it.toEntity() })
        }
    }

    private fun DeckEntity.toDomain(
        cards: List<FlashCard>,
        cardCountOverride: Int? = null
    ): Deck {
        return Deck(
            id = id,
            categoryId = categoryId,
            name = name,
            description = description,
            themeColor = themeColor,
            isPublic = isPublic,
            createdAt = createdAt,
            updatedAt = updatedAt,
            cards = cards,
            customCardCount = cardCountOverride ?: cardCount
        )
    }

    private fun Deck.toEntity(): DeckEntity {
        return DeckEntity(
            id = id,
            categoryId = categoryId,
            name = name,
            description = description,
            themeColor = themeColor,
            isPublic = isPublic,
            createdAt = createdAt,
            updatedAt = updatedAt,
            cardCount = customCardCount ?: cards.size
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

    private suspend fun cacheDeckCards(deckId: String): Int? {
        val dao = flashCardDao ?: return null
        val api = cardApiService ?: return null
        return runCatching {
            val response = api.getCardOfDeck(deckId)
            if (response.isSuccess() && response.data != null) {
                val cards = response.data.map { dto ->
                    dto.toDomain().copy(deckId = deckId).toEntity(isSynced = true)
                }
                if (cards.isNotEmpty()) {
                    dao.insertAllCards(cards)
                }
                cards.size
            } else {
                null
            }
        }.getOrNull()
    }

    private fun FlashCard.toEntity(isSynced: Boolean): FlashCardEntity {
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
            isSynced = isSynced
        )
    }
}
