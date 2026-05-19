package com.example.flashcardapp.data.datasource.remote.model

import com.example.flashcardapp.domain.model.study.StudyRecentSession
import com.example.flashcardapp.domain.model.study.StudySessionState
import com.google.gson.annotations.SerializedName

data class StudyRecentSessionDto(
    @SerializedName("deckId") val deckId: String,
    @SerializedName("mode") val mode: String,
    @SerializedName("currentIndex") val currentIndex: Int = 0,
    @SerializedName("totalCards") val totalCards: Int = 0
)

data class UpsertStudySessionRequestDto(
    @SerializedName("deckId") val deckId: String,
    @SerializedName("mode") val mode: String,
    @SerializedName("cardSequence") val cardSequence: List<String>,
    @SerializedName("currentIndex") val currentIndex: Int = 0
)

data class StudySessionStateDto(
    @SerializedName("id") val id: String,
    @SerializedName("deckId") val deckId: String,
    @SerializedName("mode") val mode: String,
    @SerializedName("cardSequence") val cardSequence: List<String> = emptyList(),
    @SerializedName("currentIndex") val currentIndex: Int = 0,
    @SerializedName("totalCards") val totalCards: Int = 0
)

fun StudyRecentSessionDto.toDomain(): StudyRecentSession {
    return StudyRecentSession(
        deckId = deckId,
        mode = mode,
        currentIndex = currentIndex,
        totalCards = totalCards
    )
}

fun StudySessionStateDto.toDomain(): StudySessionState {
    return StudySessionState(
        id = id,
        deckId = deckId,
        mode = mode,
        cardSequence = cardSequence,
        currentIndex = currentIndex,
        totalCards = totalCards
    )
}
