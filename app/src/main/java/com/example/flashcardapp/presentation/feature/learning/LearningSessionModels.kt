package com.example.flashcardapp.presentation.feature.learning

import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.domain.model.FlashCard

private const val DEFAULT_CARD_LIMIT = 20
private const val DEFAULT_TIME_ATTACK_MINUTES = 1

enum class LearningCardOrder {
    RANDOM,
    ORDERED
}

enum class LearningCardFilter {
    ALL,
    NEW,
    REVIEW
}

enum class LearningStudyMode(val sessionMode: String, val syncMode: String) {
    SEQUENTIAL("SEQUENTIAL", "STANDARD"),
    RANDOM("RANDOM", "STANDARD"),
    TIME_ATTACK("TIME_ATTACK", "STANDARD"),
    SPACED_REPETITION("SPACED_REPETITION", "SPACED_REPETITION")
}

enum class LearningRating(val syncValue: Int) {
    AGAIN(1),
    HARD(2),
    GOOD(4),
    EASY(5)
}

data class LearningSessionSettings(
    val cardLimit: Int = DEFAULT_CARD_LIMIT,
    val order: LearningCardOrder = LearningCardOrder.RANDOM,
    val filter: LearningCardFilter = LearningCardFilter.ALL,
    val mode: LearningStudyMode = LearningStudyMode.RANDOM,
    val timeLimitMinutes: Int? = DEFAULT_TIME_ATTACK_MINUTES
)

data class LearningResult(
    val studiedCount: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val elapsedSeconds: Long = 0L
)

data class LearningDeckSummary(
    val totalCards: Int = 0,
    val learnedCards: Int = 0,
    val newCards: Int = 0
)

data class LearningUiState(
    val deckId: String? = null,
    val deck: Deck? = null,
    val cards: List<FlashCard> = emptyList(),
    val reviewedCardIds: Set<String> = emptySet(),
    val sessionCards: List<FlashCard> = emptyList(),
    val currentIndex: Int = 0,
    val settings: LearningSessionSettings = LearningSessionSettings(),
    val ratings: Map<String, LearningRating> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSessionStarted: Boolean = false,
    val isCompleted: Boolean = false,
    val isTimeExpired: Boolean = false,
    val timeRemainingSeconds: Long? = null,
    val result: LearningResult = LearningResult()
) {
    val currentCard: FlashCard?
        get() = sessionCards.getOrNull(currentIndex)

    val previewCards: List<FlashCard>
        get() = when (settings.filter) {
            LearningCardFilter.ALL -> cards
            LearningCardFilter.NEW -> cards.filterNot { reviewedCardIds.contains(it.id) }
            LearningCardFilter.REVIEW -> cards.filter { reviewedCardIds.contains(it.id) }
        }

    val totalAvailableCards: Int
        get() = cards.size

    val totalSessionCards: Int
        get() = sessionCards.size

    val progressLabel: String
        get() = "${(currentIndex + 1).coerceAtMost(totalSessionCards)}/$totalSessionCards"

    val progressPercent: Float
        get() = if (totalSessionCards == 0) 0f else ((currentIndex + 1).toFloat() / totalSessionCards) * 100f

    val deckSummary: LearningDeckSummary
        get() = LearningDeckSummary(
            totalCards = cards.size,
            learnedCards = reviewedCardIds.size,
            newCards = (cards.size - reviewedCardIds.size).coerceAtLeast(0)
        )
}
