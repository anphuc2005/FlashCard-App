package com.example.flashcardapp.core.utils

import com.example.flashcardapp.R
import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.domain.model.FlashCard

@Suppress("UNUSED")
object MockDeckData {

    fun getMockDecks(): List<Deck> {
        return listOf(
            Deck(
                id = "1",
                name = "English Vocabulary",
                description = "Learn common English words",
                cards = getMockFlashCards(25),
                iconResId = R.drawable.ic_1,
                backgroundResId = R.color.md_icon_blue_background,
                createdAt = "2024-01-15",
                updatedAt = "2024-03-10"
            ),
            Deck(
                id = "2",
                name = "Math Basics",
                description = "Mathematics fundamentals",
                cards = getMockFlashCards(30),
                iconResId = R.drawable.ic_1,
                backgroundResId = R.color.md_icon_orange_background,
                createdAt = "2024-02-20",
                updatedAt = "2024-03-12"
            ),
            Deck(
                id = "3",
                name = "History Facts",
                description = "Important historical events",
                cards = getMockFlashCards(45),
                iconResId = R.drawable.ic_1,
                backgroundResId = R.color.md_icon_red_background,
                createdAt = "2024-01-05",
                updatedAt = "2024-03-08"
            ),
            Deck(
                id = "4",
                name = "Science Concepts",
                description = "General science knowledge",
                cards = getMockFlashCards(20),
                iconResId = R.drawable.ic_1,
                backgroundResId = R.color.md_icon_green_background,
                createdAt = "2024-02-10",
                updatedAt = "2024-03-11"
            ),
            Deck(
                id = "5",
                name = "Spanish Phrases",
                description = "Common Spanish expressions",
                cards = getMockFlashCards(35),
                iconResId = R.drawable.ic_1,
                backgroundResId = R.color.md_icon_purple_background,
                createdAt = "2024-01-25",
                updatedAt = "2024-03-09"
            ),
            Deck(
                id = "6",
                name = "Programming Basics",
                description = "Introduction to programming",
                cards = getMockFlashCards(40),
                iconResId = R.drawable.ic_1,
                backgroundResId = R.color.md_icon_gray_background,
                createdAt = "2024-02-05",
                updatedAt = "2024-03-13"
            )
        )
    }

    private fun getMockFlashCards(count: Int): List<FlashCard> {
        return (1..count).map { index ->
            FlashCard(
                id = "card_$index",
                deckId = "deck_id",
                question = "Question $index",
                answer = "Answer for question $index"
            )
        }
    }

    /**
     * Get recently studied decks (mock data)
     */
    fun getRecentDecks(): List<Deck> {
        return getMockDecks().take(3)
    }

    /**
     * Get active deck (currently studying)
     */
    fun getActiveDeck(): Deck? {
        return getMockDecks().firstOrNull()
    }
}

