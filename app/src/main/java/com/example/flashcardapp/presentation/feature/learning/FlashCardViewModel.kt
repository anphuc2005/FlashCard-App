package com.example.flashcardapp.presentation.feature.learning

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.R
import com.example.flashcardapp.domain.usecase.deck.GetDeckByIdUseCase
import com.example.flashcardapp.domain.usecase.flashcard.GetCardsByDeckIdUseCase
import com.example.flashcardapp.domain.usecase.study.StudyUseCases
import com.example.flashcardapp.domain.model.FlashCard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.min

private const val DEFAULT_LEARNING_CARD_LIMIT = 20
private const val MIN_CARD_LIMIT = 1
private const val SECONDS_PER_MINUTE = 60L

class FlashCardViewModel(application: Application) : AndroidViewModel(application) {

    private val appContainer = (application as FlashcardApp).container
    private val getDeckByIdUseCase: GetDeckByIdUseCase = appContainer.getDeckByIdUseCase
    private val getCardsByDeckIdUseCase: GetCardsByDeckIdUseCase = appContainer.getCardsByDeckIdUseCase
    private val studyUseCases: StudyUseCases = appContainer.studyUseCases

    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    private var sessionStartedAt: Long = 0L
    private var timerJob: Job? = null

    fun loadDeck(deckId: String) {
        if (_uiState.value.deckId == deckId && _uiState.value.cards.isNotEmpty()) return

        viewModelScope.launch {
            timerJob?.cancel()
            syncPendingReviews()
            _uiState.value = _uiState.value.copy(
                deckId = deckId,
                isLoading = true,
                errorMessage = null,
                reviewedCardIds = emptySet(),
                isCompleted = false,
                isSessionStarted = false,
                isTimeExpired = false,
                timeRemainingSeconds = null
            )

            val deckResult = getDeckByIdUseCase(deckId)
            val cardsResult = getCardsByDeckIdUseCase(deckId)
            val reviewedCardIds = studyUseCases.getReviewedCardIds(deckId).getOrDefault(emptySet())

            val deck = deckResult.getOrNull()
            if (cardsResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    deck = deck,
                    cards = emptyList(),
                    reviewedCardIds = reviewedCardIds,
                    sessionCards = emptyList(),
                    isLoading = false,
                    errorMessage = cardsResult.exceptionOrNull()?.message
                        ?: stringRes(R.string.learning_load_cards_error)
                )
                return@launch
            }

            val cards = cardsResult.getOrDefault(emptyList())
            val defaultLimit = min(DEFAULT_LEARNING_CARD_LIMIT, cards.size.coerceAtLeast(1))
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                deck = deck,
                cards = cards,
                reviewedCardIds = reviewedCardIds,
                sessionCards = emptyList(),
                settings = currentState.settings.copy(cardLimit = defaultLimit),
                currentIndex = 0,
                ratings = emptyMap(),
                isLoading = false,
                errorMessage = deckResult.exceptionOrNull()?.message,
                isCompleted = false,
                isSessionStarted = false,
                isTimeExpired = false,
                timeRemainingSeconds = null,
                result = LearningResult()
            )
        }
    }

    fun setCardLimit(limit: Int) {
        val state = _uiState.value
        val safeLimit = limit.coerceIn(MIN_CARD_LIMIT, state.cards.size.coerceAtLeast(MIN_CARD_LIMIT))
        _uiState.value = state.copy(
            settings = state.settings.copy(cardLimit = safeLimit)
        )
    }

    fun setOrder(order: LearningCardOrder) {
        val state = _uiState.value
        val mode = if (state.settings.filter == LearningCardFilter.REVIEW) {
            LearningStudyMode.SPACED_REPETITION
        } else {
            when (order) {
                LearningCardOrder.RANDOM -> LearningStudyMode.RANDOM
                LearningCardOrder.ORDERED -> LearningStudyMode.SEQUENTIAL
            }
        }
        _uiState.value = state.copy(
            settings = state.settings.copy(order = order, mode = mode)
        )
    }

    fun setFilter(filter: LearningCardFilter) {
        val state = _uiState.value
        val mode = when (filter) {
            LearningCardFilter.REVIEW -> LearningStudyMode.SPACED_REPETITION
            LearningCardFilter.ALL,
            LearningCardFilter.NEW -> if (state.settings.mode == LearningStudyMode.TIME_ATTACK) {
                LearningStudyMode.TIME_ATTACK
            } else {
                when (state.settings.order) {
                    LearningCardOrder.RANDOM -> LearningStudyMode.RANDOM
                    LearningCardOrder.ORDERED -> LearningStudyMode.SEQUENTIAL
                }
            }
        }
        _uiState.value = state.copy(
            settings = state.settings.copy(filter = filter, mode = mode)
        )
    }

    fun setStudyMode(mode: LearningStudyMode) {
        val state = _uiState.value
        _uiState.value = state.copy(
            settings = state.settings.copy(mode = mode)
        )
    }

    fun setTimeLimitMinutes(minutes: Int) {
        val state = _uiState.value
        _uiState.value = state.copy(
            settings = state.settings.copy(timeLimitMinutes = minutes)
        )
    }

    fun startSession(onSessionReady: (Boolean) -> Unit) {
        val state = _uiState.value
        val deckId = state.deckId
        if (deckId.isNullOrBlank()) {
            _uiState.value = state.copy(
                errorMessage = stringRes(R.string.learning_missing_deck)
            )
            onSessionReady(false)
            return
        }

        viewModelScope.launch {
            timerJob?.cancel()
            syncPendingReviews()
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val reviewedCardIds = studyUseCases.getReviewedCardIds(deckId).getOrElse { state.reviewedCardIds }

            val sourceCards = studyUseCases.getSessionCards(deckId, state.settings.mode.sessionMode)

            if (sourceCards.isFailure) {
                val fallbackCards = filterCards(
                    cards = state.cards,
                    filter = state.settings.filter,
                    reviewedCardIds = reviewedCardIds,
                    trustReviewCards = false
                )
                if (fallbackCards.isNotEmpty()) {
                    startOfflineSession(fallbackCards, state, reviewedCardIds, onSessionReady)
                    return@launch
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = sourceCards.exceptionOrNull()?.message
                            ?: stringRes(R.string.learning_load_cards_error)
                    )
                    onSessionReady(false)
                    return@launch
                }
            }

            val availableCards = filterCards(
                cards = sourceCards.getOrDefault(emptyList()),
                filter = state.settings.filter,
                reviewedCardIds = reviewedCardIds,
                trustReviewCards = state.settings.filter == LearningCardFilter.REVIEW
            )
            if (availableCards.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = when (state.settings.mode) {
                        LearningStudyMode.SPACED_REPETITION -> stringRes(R.string.learning_due_completed)
                        else -> stringRes(R.string.learning_session_empty)
                    }
                )
                onSessionReady(false)
                return@launch
            }

            val sessionCards = availableCards.take(state.settings.cardLimit)

            if (sessionCards.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = stringRes(R.string.learning_no_cards)
                )
                onSessionReady(false)
                return@launch
            }

            sessionStartedAt = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(
                sessionCards = sessionCards,
                currentIndex = 0,
                ratings = emptyMap(),
                isLoading = false,
                isSessionStarted = true,
                isCompleted = false,
                isTimeExpired = false,
                reviewedCardIds = reviewedCardIds,
                timeRemainingSeconds = initialTimeRemainingSeconds(state.settings),
                errorMessage = null,
                result = LearningResult()
            )
            startTimerIfNeeded()
            onSessionReady(true)
        }
    }

    fun rateCurrentCard(rating: LearningRating): Boolean {
        val state = _uiState.value
        if (state.isCompleted) return true
        val currentCard = state.currentCard ?: return false
        val updatedRatings = state.ratings + (currentCard.id to rating)
        val nextIndex = state.currentIndex + 1
        val isCompleted = nextIndex >= state.sessionCards.size

        _uiState.value = if (isCompleted) {
            timerJob?.cancel()
            state.copy(
                ratings = updatedRatings,
                reviewedCardIds = state.reviewedCardIds + currentCard.id,
                isCompleted = true,
                isSessionStarted = false,
                result = buildResult(updatedRatings, state.sessionCards.size)
            )
        } else {
            state.copy(
                ratings = updatedRatings,
                reviewedCardIds = state.reviewedCardIds + currentCard.id,
                currentIndex = nextIndex
            )
        }

        viewModelScope.launch {
            studyUseCases.saveReview(
                cardId = currentCard.id,
                deckId = currentCard.deckId,
                studyMode = state.settings.mode.syncMode,
                grade = rating.syncValue
            )
            if (isCompleted) {
                studyUseCases.syncReviews()
            }
        }

        return isCompleted
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun filterCards(
        cards: List<FlashCard>,
        filter: LearningCardFilter,
        reviewedCardIds: Set<String>,
        trustReviewCards: Boolean
    ): List<FlashCard> {
        return when (filter) {
            LearningCardFilter.ALL -> cards
            LearningCardFilter.NEW -> cards.filterNot { reviewedCardIds.contains(it.id) }
            LearningCardFilter.REVIEW -> if (trustReviewCards) {
                cards
            } else {
                cards.filter { reviewedCardIds.contains(it.id) }
            }
        }
    }

    private fun buildResult(
        ratings: Map<String, LearningRating>,
        studiedCount: Int
    ): LearningResult {
        val correctCount = ratings.values.count {
            it == LearningRating.GOOD || it == LearningRating.EASY
        }
        val elapsedSeconds = ((System.currentTimeMillis() - sessionStartedAt) / 1000L).coerceAtLeast(0L)
        return LearningResult(
            studiedCount = studiedCount,
            correctCount = correctCount,
            incorrectCount = studiedCount - correctCount,
            elapsedSeconds = elapsedSeconds
        )
    }

    private fun startOfflineSession(
        cards: List<FlashCard>,
        state: LearningUiState,
        reviewedCardIds: Set<String>,
        onSessionReady: (Boolean) -> Unit
    ) {
        val orderedCards = when (state.settings.mode) {
            LearningStudyMode.RANDOM,
            LearningStudyMode.TIME_ATTACK -> cards.shuffled()
            LearningStudyMode.SEQUENTIAL,
            LearningStudyMode.SPACED_REPETITION -> cards
        }
        val sessionCards = orderedCards.take(state.settings.cardLimit)
        if (sessionCards.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = stringRes(R.string.learning_no_cards)
            )
            onSessionReady(false)
            return
        }

        sessionStartedAt = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(
            sessionCards = sessionCards,
            currentIndex = 0,
            ratings = emptyMap(),
            isLoading = false,
            isSessionStarted = true,
            isCompleted = false,
            isTimeExpired = false,
            reviewedCardIds = reviewedCardIds,
            timeRemainingSeconds = initialTimeRemainingSeconds(state.settings),
            errorMessage = null,
            result = LearningResult()
        )
        startTimerIfNeeded()
        onSessionReady(true)
    }

    private fun initialTimeRemainingSeconds(settings: LearningSessionSettings): Long? {
        return if (settings.mode == LearningStudyMode.TIME_ATTACK) {
            (settings.timeLimitMinutes ?: 1) * SECONDS_PER_MINUTE
        } else {
            null
        }
    }

    private fun startTimerIfNeeded() {
        timerJob?.cancel()
        val state = _uiState.value
        if (state.settings.mode != LearningStudyMode.TIME_ATTACK) {
            return
        }

        val initialRemaining = state.timeRemainingSeconds ?: return
        timerJob = viewModelScope.launch {
            var remainingSeconds = initialRemaining
            while (isActive && remainingSeconds > 0) {
                delay(1000)
                val currentState = _uiState.value
                if (!currentState.isSessionStarted || currentState.isCompleted) {
                    return@launch
                }
                remainingSeconds -= 1
                _uiState.value = currentState.copy(timeRemainingSeconds = remainingSeconds)
            }

            if (_uiState.value.isSessionStarted && !_uiState.value.isCompleted) {
                completeSessionFromTimeout()
            }
        }
    }

    private fun completeSessionFromTimeout() {
        timerJob?.cancel()
        val state = _uiState.value
        val studiedCount = state.ratings.size
        _uiState.value = state.copy(
            isSessionStarted = false,
            isCompleted = true,
            isTimeExpired = true,
            timeRemainingSeconds = 0L,
            result = buildResult(state.ratings, studiedCount)
        )
        viewModelScope.launch {
            syncPendingReviews()
        }
    }

    private suspend fun syncPendingReviews() {
        studyUseCases.syncReviews()
    }

    private fun stringRes(resId: Int): String {
        return getApplication<FlashcardApp>().getString(resId)
    }
}
