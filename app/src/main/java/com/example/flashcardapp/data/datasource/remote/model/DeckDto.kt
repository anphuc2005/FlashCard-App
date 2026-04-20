package com.example.flashcardapp.data.datasource.remote.model

import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.domain.model.FlashCard
import com.google.gson.annotations.SerializedName

data class FlashCardDto(
    @SerializedName("id") val id: String,
    @SerializedName("question") val question: String,
    @SerializedName("answer") val answer: String,
    @SerializedName("deckId") val deckId: String
) {
    fun toDomain(): FlashCard {
        return FlashCard(
            id = id,
            question = question,
            answer = answer,
            deckId = deckId
        )
    }
}

fun FlashCard.toDto(): FlashCardDto {
    return FlashCardDto(
        id = id,
        question = question,
        answer = answer,
        deckId = deckId
    )
}

data class DeckDto(
    @SerializedName("id") val id: String,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("themeColor") val themeColor: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName(value = "isPublic", alternate = ["public"]) val isPublic: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    fun toDomain(): Deck {
        return Deck(
            id = id,
            categoryId = categoryId,
            name = name,
            description = description,
            themeColor = themeColor,
            isPublic = isPublic,
            iconResId = null,
            backgroundResId = null,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

fun Deck.toDto(): DeckDto {
    return DeckDto(
        id = id,
        name = name,
        description = description,
        categoryId = categoryId,
        themeColor = themeColor,
        isPublic = isPublic,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
