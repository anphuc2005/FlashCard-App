package com.example.flashcardapp.data.datasource.remote.model

import com.example.flashcardapp.domain.model.study.StudyReview
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyApiContractTest {

    @Test
    fun apiResponseAcceptsSuccessFlagOrHttpStyleStatus() {
        assertTrue(ApiResponse<Unit>(success = true).isSuccess())
        assertTrue(ApiResponse<Unit>(status = 200).isSuccess())
        assertFalse(ApiResponse<Unit>(status = 500, success = false).isSuccess())
    }

    @Test
    fun sessionDtosPreserveResumeState() {
        val session = StudySessionStateDto(
            id = "session-1",
            deckId = "deck-1",
            mode = "SEQUENTIAL",
            cardSequence = listOf("card-2", "card-1"),
            currentIndex = 1,
            totalCards = 2
        ).toDomain()

        assertEquals("deck-1", session.deckId)
        assertEquals(listOf("card-2", "card-1"), session.cardSequence)
        assertEquals(1, session.currentIndex)
        assertEquals(2, session.totalCards)
    }

    @Test
    fun progressDtoNormalizesRatioAndClampsInvalidCounts() {
        val progress = StudyProgressDto(
            deckId = "deck-1",
            mode = "RANDOM",
            studiedCards = -1,
            totalCards = -2,
            progressPercent = 0.25
        ).toDomain()

        assertEquals(0, progress.studiedCards)
        assertEquals(0, progress.totalCards)
        assertEquals(25f, progress.progressPercent)
    }

    @Test
    fun offlineReviewMapsToOnlineSyncPayload() {
        val dto = StudyReview(
            id = "review-1",
            cardId = "card-1",
            deckId = "deck-1",
            studyMode = "STANDARD",
            grade = 4,
            studiedAt = "2026-06-14T10:00:00Z",
            durationSeconds = 5,
            isSynced = false
        ).toDto()

        assertEquals("card-1", dto.cardId)
        assertEquals("deck-1", dto.deckId)
        assertEquals("STANDARD", dto.studyMode)
        assertEquals(4, dto.grade)
        assertEquals(5, dto.durationSeconds)
    }
}
