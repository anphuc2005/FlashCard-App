package com.example.flashcardapp.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.flashcardapp.domain.model.study.StudyReview

@Entity(tableName = "study_review_table")
data class StudyReviewEntity(
    @PrimaryKey
    val id: String,
    val cardId: String,
    val deckId: String,
    val studyMode: String,
    val grade: Int,
    val studiedAt: String,
    val durationSeconds: Int? = null,
    val isSynced: Boolean = false
) {
    fun toDomain(): StudyReview {
        return StudyReview(
            id = id,
            cardId = cardId,
            deckId = deckId,
            studyMode = studyMode,
            grade = grade,
            studiedAt = studiedAt,
            durationSeconds = durationSeconds,
            isSynced = isSynced
        )
    }
}

fun StudyReview.toEntity(): StudyReviewEntity {
    return StudyReviewEntity(
        id = id,
        cardId = cardId,
        deckId = deckId,
        studyMode = studyMode,
        grade = grade,
        studiedAt = studiedAt,
        durationSeconds = durationSeconds,
        isSynced = isSynced
    )
}
