package com.example.flashcardapp.domain.usecase.study

import com.example.flashcardapp.data.repository.StudyRepository
import com.example.flashcardapp.domain.model.study.StudyReview
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

private val ALLOWED_STUDY_RATINGS = listOf(1, 2, 4, 5)
private const val STUDY_TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'"

class SaveStudyReviewUseCase(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(
        cardId: String,
        deckId: String,
        studyMode: String,
        grade: Int,
        durationSeconds: Int? = null
    ): Result<StudyReview> {
        if (grade !in ALLOWED_STUDY_RATINGS) {
            return Result.failure(IllegalArgumentException("Invalid study rating"))
        }

        val review = StudyReview(
            id = UUID.randomUUID().toString(),
            cardId = cardId,
            deckId = deckId,
            studyMode = studyMode,
            grade = grade,
            studiedAt = currentUtcTimestamp(),
            durationSeconds = durationSeconds,
            isSynced = false
        )
        return studyRepository.saveReview(review)
    }

    private fun currentUtcTimestamp(): String {
        val formatter = SimpleDateFormat(STUDY_TIMESTAMP_PATTERN, Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date())
    }
}
