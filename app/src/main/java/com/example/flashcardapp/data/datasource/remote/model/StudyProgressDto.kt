package com.example.flashcardapp.data.datasource.remote.model

import com.example.flashcardapp.domain.model.study.StudyDeckProgress
import com.google.gson.annotations.SerializedName

data class StudyProgressDto(
    @SerializedName("deckId")
    val deckId: String = "",
    @SerializedName("mode")
    val mode: String = "",
    @SerializedName(value = "studiedCards", alternate = ["learnedCards", "reviewedCards", "studiedCount"])
    val studiedCards: Int = 0,
    @SerializedName(value = "totalCards", alternate = ["cardCount"])
    val totalCards: Int = 0,
    @SerializedName(value = "progressPercent", alternate = ["percent", "progress"])
    val progressPercent: Double = 0.0,
    @SerializedName("completed")
    val completed: Boolean = false
) {
    fun toDomain(): StudyDeckProgress {
        val normalizedPercent = if (progressPercent in 0.0..1.0) {
            (progressPercent * 100.0).toFloat()
        } else {
            progressPercent.toFloat()
        }.coerceIn(0f, 100f)

        return StudyDeckProgress(
            deckId = deckId,
            mode = mode,
            studiedCards = studiedCards.coerceAtLeast(0),
            totalCards = totalCards.coerceAtLeast(0),
            progressPercent = normalizedPercent,
            completed = completed
        )
    }
}
