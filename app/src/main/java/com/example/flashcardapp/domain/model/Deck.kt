package com.example.flashcardapp.domain.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

data class Deck(
    val id: String,
    val categoryId: String = "",
    val name: String,
    val description: String? = null,
    @DrawableRes val iconResId: Int? = null,
    @ColorRes val backgroundResId: Int? = null,
    val isPublic: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val cards: List<FlashCard> = emptyList()
) {
    val cardCount: Int
        get() = cards.size
}
