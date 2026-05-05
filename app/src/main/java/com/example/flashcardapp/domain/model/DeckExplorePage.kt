package com.example.flashcardapp.domain.model

data class DeckExplorePage(
    val content: List<Deck> = emptyList(),
    val currentPage: Int = 0,
    val pageSize: Int = 5,
    val totalElements: Long = 0L,
    val totalPages: Int = 0,
    val first: Boolean = true,
    val last: Boolean = true
)
