package com.example.flashcardapp.data.datasource.remote.model

import com.example.flashcardapp.domain.model.study.StudyReview
import com.google.gson.annotations.SerializedName

data class StudyReviewDto(
    @SerializedName("cardId") val cardId: String,
    @SerializedName("deckId") val deckId: String,
    @SerializedName("studyMode") val studyMode: String,
    @SerializedName("grade") val grade: Int,
    @SerializedName("studiedAt") val studiedAt: String,
    @SerializedName("durationSeconds") val durationSeconds: Int? = null
)

data class StudySyncResponseDto(
    @SerializedName("totalLogsReceived") val totalLogsReceived: Int,
    @SerializedName("srCardsUpdated") val srCardsUpdated: Int,
    @SerializedName("standardLogsCount") val standardLogsCount: Int,
    @SerializedName("srLogsCount") val srLogsCount: Int,
    @SerializedName("syncedAt") val syncedAt: String
)

fun StudyReview.toDto(): StudyReviewDto {
    return StudyReviewDto(
        cardId = cardId,
        deckId = deckId,
        studyMode = studyMode,
        grade = grade,
        studiedAt = studiedAt,
        durationSeconds = durationSeconds
    )
}
