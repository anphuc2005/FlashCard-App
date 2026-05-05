package com.example.flashcardapp.data.datasource.remote.model

import com.example.flashcardapp.domain.model.FlashCard
import com.google.gson.annotations.SerializedName

data class FlashCardDto(
    @SerializedName("id") val id: String,
    @SerializedName("term") val question: String,
    @SerializedName("definition") val answer: String,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("deckId") val deckId: String,
    @SerializedName("interval") val interval: Int? = null,
    @SerializedName("repetition") val repetition: Int? = null,
    @SerializedName("easeFactor") val easeFactor: Double? = null,
    @SerializedName("nextReviewDate") val nextReviewDate: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean? = null,
    @SerializedName("deleted") val deleted: Boolean? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    fun toDomain(): FlashCard {
        return FlashCard(
            id = id,
            question = question,
            answer = answer,
            imageUrl = imageUrl,
            deckId = deckId,
            interval = interval ?: 0,
            repetition = repetition ?: 0,
            easeFactor = easeFactor ?: 2.5,
            nextReviewDate = nextReviewDate
        )
    }
}

fun FlashCard.toDto(): FlashCardDto {
    return FlashCardDto(
        id = id,
        question = question,
        answer = answer,
        imageUrl = imageUrl,
        deckId = deckId,
        interval = interval,
        repetition = repetition,
        easeFactor = easeFactor,
        nextReviewDate = nextReviewDate,
        isDeleted = false
    )
}
