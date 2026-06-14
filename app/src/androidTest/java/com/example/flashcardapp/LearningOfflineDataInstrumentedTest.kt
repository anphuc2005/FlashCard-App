package com.example.flashcardapp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.flashcardapp.data.datasource.local.database.FlashCardDatabase
import com.example.flashcardapp.data.datasource.local.entity.DeckEntity
import com.example.flashcardapp.data.datasource.local.entity.FlashCardEntity
import com.example.flashcardapp.data.datasource.local.entity.StudyReviewEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LearningOfflineDataInstrumentedTest {

    private lateinit var database: FlashCardDatabase

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            FlashCardDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun cachedDeckAndCardsAreAvailableForOfflineLearning() = runBlocking {
        database.deckDao().insertDeck(deck("deck-1"))
        database.flashCardDao().insertAllCards(
            listOf(card("card-1", "deck-1"), card("card-2", "deck-1"))
        )

        val cachedDeck = database.deckDao().getDeckById("deck-1")
        val cachedCards = database.flashCardDao().getCardsSnapshotByDeckId("deck-1")

        assertEquals("Offline deck", cachedDeck?.name)
        assertEquals(listOf("card-1", "card-2"), cachedCards.map { it.id }.sorted())
    }

    @Test
    fun offlineReviewsRemainPendingUntilMarkedSynced() = runBlocking {
        val dao = database.studyReviewDao()
        val pending = review(
            id = "review-1",
            deckId = "deck-1",
            studiedAt = "2026-06-14T10:00:00Z",
            isSynced = false
        )
        dao.insertReview(pending)

        assertEquals(listOf("review-1"), dao.getUnsyncedReviews().map { it.id })

        dao.insertReview(pending.copy(isSynced = true))

        assertFalse(dao.getUnsyncedReviews().any { it.id == "review-1" })
    }

    @Test
    fun recentlyStudiedDecksAreOrderedByLatestOfflineReview() = runBlocking {
        val dao = database.studyReviewDao()
        dao.insertReviews(
            listOf(
                review("review-old", "deck-old", "2026-06-14T09:00:00Z"),
                review("review-new", "deck-new", "2026-06-14T11:00:00Z")
            )
        )

        assertEquals(
            listOf("deck-new", "deck-old"),
            dao.getRecentlyStudiedDeckIds()
        )
    }

    private fun deck(id: String) = DeckEntity(
        id = id,
        name = "Offline deck",
        cardCount = 2
    )

    private fun card(id: String, deckId: String) = FlashCardEntity(
        id = id,
        question = "Question $id",
        answer = "Answer $id",
        deckId = deckId,
        isSynced = true
    )

    private fun review(
        id: String,
        deckId: String,
        studiedAt: String,
        isSynced: Boolean = false
    ) = StudyReviewEntity(
        id = id,
        cardId = "card-$id",
        deckId = deckId,
        studyMode = "STANDARD",
        grade = 4,
        studiedAt = studiedAt,
        durationSeconds = 3,
        isSynced = isSynced
    )
}
