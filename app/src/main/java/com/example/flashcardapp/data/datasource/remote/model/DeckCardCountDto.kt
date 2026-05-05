package com.example.flashcardapp.data.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class DeckCardCountDto(
    @SerializedName("deckId") val deckId: String,
    @SerializedName("totalCards") val totalCards: Long = 0L
)
