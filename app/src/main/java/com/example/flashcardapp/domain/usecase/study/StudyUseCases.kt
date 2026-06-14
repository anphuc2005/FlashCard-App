package com.example.flashcardapp.domain.usecase.study

data class StudyUseCases(
    val getSessionCards: GetStudySessionCardsUseCase,
    val getCachedSessionCards: GetCachedStudySessionCardsUseCase,
    val getRecentSession: GetRecentStudySessionUseCase,
    val getSessionByDeck: GetStudySessionByDeckUseCase,
    val saveSessionState: SaveStudySessionStateUseCase,
    val deleteSessionByDeck: DeleteStudySessionByDeckUseCase,
    val getReviewedCardIds: GetReviewedCardIdsUseCase,
    val getRecentlyStudiedDeckIds: GetRecentlyStudiedDeckIdsUseCase,
    val saveReview: SaveStudyReviewUseCase,
    val syncReviews: SyncStudyReviewsUseCase,
    val getCurrentStreak: GetCurrentStudyStreakUseCase,
    val hasStudiedToday: HasStudiedTodayUseCase
)
