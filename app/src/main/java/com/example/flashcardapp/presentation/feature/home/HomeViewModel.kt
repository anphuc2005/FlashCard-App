package com.example.flashcardapp.presentation.feature.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.R
import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.domain.model.Shortcut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

data class HomeUiState(
    val isLoading: Boolean = false,
    val activeDeck: Deck? = null,
    val recentDecks: List<Deck> = emptyList(),
    val shortcuts: List<Shortcut> = emptyList(),
    val error: String? = null,
    val userStreak: Int = 0,
    val userGreeting: String = "",
    val userAvatarUrl: String? = null,
    val userProgress: Int = 0,
    val userProgressRaw: Float = 0f
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val appContainer = (application as FlashcardApp).container
    private val deckRepository = appContainer.deckRepository
    private val studyUseCases = appContainer.studyUseCases

    private val _uiState = MutableStateFlow(
        HomeUiState(
            isLoading = true,
            shortcuts = buildShortcuts(),
            userStreak = 0
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var cachedDecks: List<Deck> = emptyList()
    private var lastOpenedDeckId: String? = null
    private val apiCardCountByDeckId = mutableMapOf<String, Int>()

    init {
        observeLocalDecks()
        syncDecksFromApi()
    }

    private fun observeLocalDecks() {
        viewModelScope.launch {
            deckRepository.getAllDecksWithCardCountFromDb().collect { aggregations ->
                val baseDecks = aggregations.map { aggregation ->
                    val entity = aggregation.deck
                    val resolvedCardCount = maxOf(
                        aggregation.cardCount,
                        apiCardCountByDeckId[entity.id] ?: 0
                    )
                    Deck(
                        id = entity.id,
                        categoryId = entity.categoryId.orEmpty(),
                        name = entity.name,
                        description = entity.description,
                        isPublic = entity.isPublic,
                        createdAt = entity.createdAt,
                        updatedAt = entity.updatedAt,
                        customCardCount = resolvedCardCount
                    )
                }
                val decks = enrichDecksWithProgress(baseDecks)
                cachedDecks = decks
                publishDecks(decks)
            }
        }
    }

    private fun publishDecks(decks: List<Deck>) {
        val sortedDecks = decks.sortedByDescending { deck ->
            maxOf(
                parseTimestampMillis(deck.updatedAt),
                parseTimestampMillis(deck.createdAt)
            )
        }
        val activeDeck = findActiveDeck(sortedDecks)
        val progressRaw = calculateProgressPercent(
            studiedCount = activeDeck?.studiedCount ?: 0,
            totalCards = activeDeck?.cardCount ?: 0
        )
        val currentStreak = studyUseCases.getCurrentStreak()

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            activeDeck = activeDeck,
            recentDecks = sortedDecks,
            userStreak = currentStreak,
            userProgress = formatProgressPercent(progressRaw),
            userProgressRaw = progressRaw,
            error = null
        )
    }

    private fun syncDecksFromApi() {
        viewModelScope.launch {
            deckRepository.getAllDecksFromApi()
                .onSuccess { decks ->
                    apiCardCountByDeckId.clear()
                    decks.forEach { deck ->
                        apiCardCountByDeckId[deck.id] = deck.cardCount
                    }

                    if (cachedDecks.isNotEmpty()) {
                        val adjustedDecks = cachedDecks.map { deck ->
                            val resolvedCardCount = maxOf(
                                deck.cardCount,
                                apiCardCountByDeckId[deck.id] ?: 0
                            )
                            deck.copy(
                                customCardCount = resolvedCardCount,
                                customStudiedCount = deck.studiedCount.coerceIn(0, resolvedCardCount)
                            )
                        }
                        cachedDecks = adjustedDecks
                        publishDecks(adjustedDecks)
                    }
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = throwable.message ?: "Có lỗi xảy ra"
                    )
                }
        }
    }

    private fun buildShortcuts(): List<Shortcut> {
        return listOf(
            Shortcut(
                id = "1",
                title = "Tạo mới",
                iconResId = R.drawable.ic_create,
                backgroundResId = R.color.md_icon_blue_background,
                action = "CREATE"
            ),
            Shortcut(
                id = "2",
                title = "Thông báo",
                iconResId = R.drawable.ic_notif_shortcut,
                backgroundResId = R.color.md_icon_yellow_background,
                action = "NOTIFICATIONS"
            ),
            Shortcut(
                id = "3",
                title = "Xuất dữ liệu",
                iconResId = R.drawable.ic_export_data_shortcut,
                backgroundResId = R.color.md_icon_red_background,
                action = "EXPORT_DATA"
            ),
            Shortcut(
                id = "4",
                title = "Giao diện",
                iconResId = R.drawable.ic_theme_shortcut,
                backgroundResId = R.color.md_icon_purple_background,
                action = "CHANGE_THEME"
            )
        )
    }

    fun refreshData() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        syncDecksFromApi()
    }

    fun markDeckAsStudied(deckId: String) {
        lastOpenedDeckId = deckId

        viewModelScope.launch {
            deckRepository.touchDeckUpdatedAt(deckId)
            cachedDecks.firstOrNull { it.id == deckId }?.let { openedDeck ->
                val touchedDeck = openedDeck.copy(updatedAt = System.currentTimeMillis().toString())
                val mergedDecks = cachedDecks.map { deck ->
                    if (deck.id == deckId) touchedDeck else deck
                }
                cachedDecks = mergedDecks
                publishDecks(mergedDecks)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private suspend fun enrichDecksWithProgress(decks: List<Deck>): List<Deck> {
        return decks.map { deck ->
            val rawReviewedCount = studyUseCases.getReviewedCardIds(deck.id)
                .getOrDefault(emptySet())
                .size
            val effectiveCardCount = deck.cardCount.coerceAtLeast(0)
            val normalizedReviewedCount = if (effectiveCardCount > 0) {
                rawReviewedCount.coerceIn(0, effectiveCardCount)
            } else {
                0
            }
            deck.copy(
                customCardCount = effectiveCardCount,
                customStudiedCount = normalizedReviewedCount
            )
        }
    }

    private fun findActiveDeck(sortedDecks: List<Deck>): Deck? {
        val inProgressDeck = sortedDecks.firstOrNull { deck ->
            deck.cardCount > 0 && deck.studiedCount in 1 until deck.cardCount
        }
        if (inProgressDeck != null) return inProgressDeck
        return sortedDecks.firstOrNull { it.id == lastOpenedDeckId } ?: sortedDecks.firstOrNull()
    }

    private fun calculateProgressPercent(studiedCount: Int, totalCards: Int): Float {
        if (totalCards <= 0) {
            return 0f
        }
        return ((studiedCount.toFloat() / totalCards.toFloat()) * 100f)
            .coerceIn(0f, 100f)
    }

    private fun formatProgressPercent(progressPercent: Float): Int {
        return progressPercent
            .roundToInt()
            .coerceIn(0, 100)
    }

    private fun parseTimestampMillis(raw: String?): Long {
        if (raw.isNullOrBlank()) return Long.MIN_VALUE

        raw.toLongOrNull()?.let { return it }
        runCatching { Instant.parse(raw).toEpochMilli() }.getOrNull()?.let { return it }
        runCatching { OffsetDateTime.parse(raw).toInstant().toEpochMilli() }
            .getOrNull()
            ?.let { return it }
        runCatching {
            LocalDateTime.parse(raw, DateTimeFormatter.ISO_DATE_TIME)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrNull()?.let { return it }
        runCatching {
            LocalDate.parse(raw, DateTimeFormatter.ISO_DATE)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrNull()?.let { return it }

        return Long.MIN_VALUE
    }
}
