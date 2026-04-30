package com.example.flashcardapp.data.datasource.remote.model

import com.example.flashcardapp.domain.model.FlashCard
import com.google.gson.annotations.SerializedName

data class FlashCardDto(
    @SerializedName("id") val id: String,
    @SerializedName("term") val question: String,
    @SerializedName("definition") val answer: String,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("deckId") val deckId: String,
    @SerializedName("repetition") val repetition: Int = 0
) {
    fun toDomain(): FlashCard {
        return FlashCard(
            id = id,
            question = question,
            answer = answer,
            imageUrl = imageUrl,
            deckId = deckId,
            repetition = repetition
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
        repetition = repetition
    )
}
