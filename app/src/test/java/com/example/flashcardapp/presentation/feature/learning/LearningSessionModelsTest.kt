package com.example.flashcardapp.presentation.feature.learning

import com.example.flashcardapp.domain.model.FlashCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LearningSessionModelsTest {

    @Test
    fun progressIsZeroWhenSessionHasNoCards() {
        val state = LearningUiState()

        assertEquals("0/0", state.progressLabel)
        assertEquals(0f, state.progressPercent)
        assertNull(state.currentCard)
    }

    @Test
    fun progressAndCurrentCardFollowCurrentIndex() {
        val cards = listOf(card("card-1"), card("card-2"), card("card-3"))
        val state = LearningUiState(
            sessionCards = cards,
            currentIndex = 1
        )

        assertEquals("2/3", state.progressLabel)
        assertEquals(2f / 3f * 100f, state.progressPercent)
        assertEquals("card-2", state.currentCard?.id)
    }

    @Test
    fun previewFiltersUseLocallyReviewedCardIds() {
        val cards = listOf(card("new-card"), card("reviewed-card"))
        val newCards = LearningUiState(
            cards = cards,
            reviewedCardIds = setOf("reviewed-card"),
            settings = LearningSessionSettings(filter = LearningCardFilter.NEW)
        )
        val reviewCards = newCards.copy(
            settings = newCards.settings.copy(filter = LearningCardFilter.REVIEW)
        )

        assertEquals(listOf("new-card"), newCards.previewCards.map { it.id })
        assertEquals(listOf("reviewed-card"), reviewCards.previewCards.map { it.id })
        assertEquals(1, newCards.deckSummary.learnedCards)
        assertEquals(1, newCards.deckSummary.newCards)
    }

    @Test
    fun sessionModeParsingIsCaseInsensitiveAndRejectsUnknownValues() {
        assertEquals(
            LearningStudyMode.SPACED_REPETITION,
            LearningStudyMode.fromSessionMode("spaced_repetition")
        )
        assertEquals(
            LearningStudyMode.TIME_ATTACK,
            LearningStudyMode.fromSessionMode("TIME_ATTACK")
        )
        assertNull(LearningStudyMode.fromSessionMode("unknown"))
    }

    private fun card(id: String) = FlashCard(
        id = id,
        question = "Question $id",
        answer = "Answer $id",
        deckId = "deck-1"
    )
}
