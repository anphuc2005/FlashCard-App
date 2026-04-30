package com.example.flashcardapp.presentation.feature.learning

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.R
import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.domain.usecase.deck.GetDeckByIdUseCase
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
private const val TAG = "LearningViewModel"

class FlashCardViewModel(application: Application) : AndroidViewModel(application) {

    private val appContainer = (application as FlashcardApp).container
    private val getDeckByIdUseCase: GetDeckByIdUseCase = appContainer.getDeckByIdUseCase
    private val studyUseCases: StudyUseCases = appContainer.studyUseCases

    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    private var sessionStartedAt: Long = 0L
    private var timerJob: Job? = null

    fun loadDeck(deckId: String) {
        if (_uiState.value.deckId == deckId && _uiState.value.cards.isNotEmpty()) {
            Log.d(TAG, "loadDeck skipped: deckId=$deckId already loaded with ${_uiState.value.cards.size} cards")
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "loadDeck started: deckId=$deckId")
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
            // Use study/session API for learning module pre-load.
            val cardsResult = studyUseCases.getSessionCards(deckId, LearningStudyMode.SEQUENTIAL.sessionMode)
            val reviewedCardIds = studyUseCases.getReviewedCardIds(deckId).getOrDefault(emptySet())
            logDeckAndCardsResult(deckId, deckResult.getOrNull(), deckResult.exceptionOrNull(), cardsResult.getOrNull(), cardsResult.exceptionOrNull(), reviewedCardIds.size)

            val deck = deckResult.getOrNull()
            if (cardsResult.isFailure) {
                Log.e(
                    TAG,
                    "loadDeck failed to fetch cards: deckId=$deckId, message=${cardsResult.exceptionOrNull()?.message}",
                    cardsResult.exceptionOrNull()
                )
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
            Log.d(
                TAG,
                "loadDeck completed: deckId=$deckId, deckFound=${deck != null}, cards=${cards.size}, reviewed=${reviewedCardIds.size}, defaultLimit=$defaultLimit"
            )
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
        Log.d(TAG, "setCardLimit: requested=$limit, applied=$safeLimit, totalCards=${state.cards.size}")
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
        Log.d(TAG, "setOrder: order=$order, resolvedMode=$mode, filter=${state.settings.filter}")
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
        Log.d(TAG, "setFilter: filter=$filter, resolvedMode=$mode, reviewed=${state.reviewedCardIds.size}, totalCards=${state.cards.size}")
        _uiState.value = state.copy(
            settings = state.settings.copy(filter = filter, mode = mode)
        )
    }

    fun setStudyMode(mode: LearningStudyMode) {
        val state = _uiState.value
        Log.d(TAG, "setStudyMode: mode=$mode")
        val resolvedFilter = if (mode == LearningStudyMode.SPACED_REPETITION) {
            LearningCardFilter.ALL
        } else {
            state.settings.filter
        }
        _uiState.value = state.copy(
            settings = state.settings.copy(mode = mode, filter = resolvedFilter)
        )
    }

    fun setTimeLimitMinutes(minutes: Int) {
        val state = _uiState.value
        Log.d(TAG, "setTimeLimitMinutes: minutes=$minutes")
        _uiState.value = state.copy(
            settings = state.settings.copy(timeLimitMinutes = minutes)
        )
    }

    fun startSession(onSessionReady: (Boolean) -> Unit) {
        val state = _uiState.value
        val deckId = state.deckId
        Log.d(
            TAG,
            "startSession requested: deckId=$deckId, mode=${state.settings.mode}, order=${state.settings.order}, filter=${state.settings.filter}, cardLimit=${state.settings.cardLimit}, cachedCards=${state.cards.size}"
        )
        if (deckId.isNullOrBlank()) {
            Log.w(TAG, "startSession aborted: missing deckId")
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
            Log.d(
                TAG,
                "startSession sourceCards result: deckId=$deckId, success=${sourceCards.isSuccess}, reviewed=${reviewedCardIds.size}, sessionMode=${state.settings.mode.sessionMode}"
            )

            if (sourceCards.isFailure) {
                Log.e(
                    TAG,
                    "startSession remote source failed: deckId=$deckId, message=${sourceCards.exceptionOrNull()?.message}",
                    sourceCards.exceptionOrNull()
                )
                val fallbackCards = filterCards(
                    cards = state.cards,
                    filter = state.settings.filter,
                    reviewedCardIds = reviewedCardIds,
                    trustReviewCards = false
                )
                Log.w(
                    TAG,
                    "startSession using fallback cache: deckId=$deckId, fallbackCards=${fallbackCards.size}, filter=${state.settings.filter}"
                )
                if (fallbackCards.isNotEmpty()) {
                    startOfflineSession(fallbackCards, state, reviewedCardIds, onSessionReady)
                    return@launch
                } else {
                    Log.e(TAG, "startSession failed: no fallback cards available for deckId=$deckId")
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
            Log.d(
                TAG,
                "startSession filtered cards: deckId=$deckId, source=${sourceCards.getOrDefault(emptyList()).size}, available=${availableCards.size}, filter=${state.settings.filter}"
            )
            if (availableCards.isEmpty()) {
                Log.w(
                    TAG,
                    "startSession aborted: no available cards for deckId=$deckId, mode=${state.settings.mode}, filter=${state.settings.filter}"
                )
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
                Log.w(TAG, "startSession aborted: sessionCards empty after take, deckId=$deckId, cardLimit=${state.settings.cardLimit}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = stringRes(R.string.learning_no_cards)
                )
                onSessionReady(false)
                return@launch
            }

            sessionStartedAt = System.currentTimeMillis()
            Log.d(
                TAG,
                "startSession ready: deckId=$deckId, sessionCards=${sessionCards.size}, firstCardId=${sessionCards.firstOrNull()?.id}, timer=${initialTimeRemainingSeconds(state.settings)}"
            )
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
        if (state.isCompleted) {
            Log.w(TAG, "rateCurrentCard ignored: session already completed")
            return true
        }
        val currentCard = state.currentCard ?: run {
            Log.w(TAG, "rateCurrentCard ignored: currentCard is null")
            return false
        }
        val updatedRatings = state.ratings + (currentCard.id to rating)
        val nextIndex = state.currentIndex + 1
        val isCompleted = nextIndex >= state.sessionCards.size
        Log.d(
            TAG,
            "rateCurrentCard: cardId=${currentCard.id}, rating=$rating, currentIndex=${state.currentIndex}, nextIndex=$nextIndex, sessionSize=${state.sessionCards.size}, completed=$isCompleted"
        )

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
            val sessionDeckId = state.deckId
            val reviewDeckId = sessionDeckId ?: currentCard.deckId
            Log.d(
                TAG,
                "saveReview started: cardId=${currentCard.id}, cardDeckId=${currentCard.deckId}, sessionDeckId=$sessionDeckId, reviewDeckId=$reviewDeckId, mode=${state.settings.mode.syncMode}, grade=${rating.syncValue}"
            )
            val saveResult = studyUseCases.saveReview(
                cardId = currentCard.id,
                deckId = reviewDeckId,
                studyMode = state.settings.mode.syncMode,
                grade = rating.syncValue
            )
            saveResult
                .onSuccess {
                    Log.d(TAG, "saveReview success: cardId=${currentCard.id}, reviewDeckId=$reviewDeckId")
                }
                .onFailure { throwable ->
                    Log.e(
                        TAG,
                        "saveReview failure: cardId=${currentCard.id}, reviewDeckId=$reviewDeckId, message=${throwable.message}",
                        throwable
                    )
                }
            if (isCompleted) {
                Log.d(TAG, "saveReview completed final card, syncing reviews for deckId=$reviewDeckId")
                studyUseCases.syncReviews()
            }
        }

        return isCompleted
    }

    fun setCurrentIndex(index: Int) {
        val state = _uiState.value
        if (!state.isSessionStarted || state.sessionCards.isEmpty() || state.isCompleted) return
        val safeIndex = index.coerceIn(0, state.sessionCards.lastIndex)
        if (safeIndex == state.currentIndex) return
        Log.d(TAG, "setCurrentIndex: from=${state.currentIndex}, to=$safeIndex")
        _uiState.value = state.copy(currentIndex = safeIndex)
    }

    fun clearError() {
        Log.d(TAG, "clearError: previousMessage=${_uiState.value.errorMessage}")
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
            LearningCardFilter.NEW -> {
                if (trustReviewCards) {
                    cards.filter { it.repetition == 0 }
                } else {
                    cards.filterNot { reviewedCardIds.contains(it.id) }
                }
            }
            LearningCardFilter.REVIEW -> if (trustReviewCards) {
                cards.filter { it.repetition > 0 }
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
        Log.d(
            TAG,
            "startOfflineSession: sourceCards=${cards.size}, mode=${state.settings.mode}, order=${state.settings.order}, cardLimit=${state.settings.cardLimit}, reviewed=${reviewedCardIds.size}"
        )
        val orderedCards = when (state.settings.mode) {
            LearningStudyMode.RANDOM,
            LearningStudyMode.TIME_ATTACK -> cards.shuffled()
            LearningStudyMode.SEQUENTIAL,
            LearningStudyMode.SPACED_REPETITION -> cards
        }
        val sessionCards = orderedCards.take(state.settings.cardLimit)
        if (sessionCards.isEmpty()) {
            Log.w(TAG, "startOfflineSession aborted: no cards after ordering/take")
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = stringRes(R.string.learning_no_cards)
            )
            onSessionReady(false)
            return
        }

        sessionStartedAt = System.currentTimeMillis()
        Log.d(
            TAG,
            "startOfflineSession ready: sessionCards=${sessionCards.size}, firstCardId=${sessionCards.firstOrNull()?.id}, timer=${initialTimeRemainingSeconds(state.settings)}"
        )
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
            Log.d(TAG, "startTimerIfNeeded skipped: mode=${state.settings.mode}")
            return
        }

        val initialRemaining = state.timeRemainingSeconds ?: return
        Log.d(TAG, "startTimerIfNeeded started: initialRemaining=$initialRemaining")
        timerJob = viewModelScope.launch {
            var remainingSeconds = initialRemaining
            while (isActive && remainingSeconds > 0) {
                delay(1000)
                val currentState = _uiState.value
                if (!currentState.isSessionStarted || currentState.isCompleted) {
                    Log.d(TAG, "timer cancelled by state change: isSessionStarted=${currentState.isSessionStarted}, isCompleted=${currentState.isCompleted}")
                    return@launch
                }
                remainingSeconds -= 1
                _uiState.value = currentState.copy(timeRemainingSeconds = remainingSeconds)
            }

            if (_uiState.value.isSessionStarted && !_uiState.value.isCompleted) {
                Log.w(TAG, "timer expired, completing session from timeout")
                completeSessionFromTimeout()
            }
        }
    }

    private fun completeSessionFromTimeout() {
        timerJob?.cancel()
        val state = _uiState.value
        val studiedCount = state.ratings.size
        Log.w(
            TAG,
            "completeSessionFromTimeout: studied=$studiedCount, sessionSize=${state.sessionCards.size}, elapsed=${System.currentTimeMillis() - sessionStartedAt}"
        )
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
        runCatching {
            Log.d(TAG, "syncPendingReviews started")
            studyUseCases.syncReviews()
        }.onSuccess {
            Log.d(TAG, "syncPendingReviews completed")
        }.onFailure { throwable ->
            Log.e(TAG, "syncPendingReviews failed: ${throwable.message}", throwable)
        }
    }

    private fun stringRes(resId: Int): String {
        return getApplication<FlashcardApp>().getString(resId)
    }

    private fun logDeckAndCardsResult(
        deckId: String,
        deck: Deck?,
        deckError: Throwable?,
        cards: List<FlashCard>?,
        cardsError: Throwable?,
        reviewedCount: Int
    ) {
        Log.d(
            TAG,
            "loadDeck results: deckId=$deckId, deckFound=${deck != null}, deckError=${deckError?.message}, cards=${cards?.size ?: 0}, cardsError=${cardsError?.message}, reviewed=$reviewedCount"
        )
    }
}
