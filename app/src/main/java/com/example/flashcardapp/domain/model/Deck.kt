package com.example.flashcardapp.domain.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

data class Deck(
    val id: String,
    val name: String,
    val description: String? = null,
    val cards: List<FlashCard> = emptyList(),
    val cardCount: Int = cards.size,
    @DrawableRes val iconResId: Int? = null,
    @ColorRes val backgroundResId: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

